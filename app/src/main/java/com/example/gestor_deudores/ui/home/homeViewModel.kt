package com.example.gestor_deudores.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.database.Deuda
import com.example.gestor_deudores.data.database.DeudaDao
import com.example.gestor_deudores.data.database.Deudor
import com.example.gestor_deudores.data.database.DeudorDao
import com.example.gestor_deudores.data.utils.EstadoCobro
import com.example.gestor_deudores.data.utils.calcularEstadoCobro
import android.content.Context
import com.example.gestor_deudores.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Pestaña{
    PENDIENTES,
    HISTORIAL,
    ARCHIVADOS
}



class HomeViewModel(private val dao: DeudorDao,private val dao2: DeudaDao) : ViewModel() {



    val listaDeudores = dao.obtenerDeudores().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue= emptyList()
    )

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda = _textoBusqueda.asStateFlow()
    private val _pestañaActual = MutableStateFlow(Pestaña.PENDIENTES)
    val pestañaActual = _pestañaActual.asStateFlow()

    // --- ESTADOS Y LÓGICA DE TASA BCV Y MONEDA DE VISTA ---
    private val _precioDolarBCV = MutableStateFlow(0.0)
    val precioDolarBCV = _precioDolarBCV.asStateFlow()

    private val _monedaVista = MutableStateFlow("USD") // "USD" o "VES"
    val monedaVista = _monedaVista.asStateFlow()

    private val _fechaTasaBCV = MutableStateFlow("Pendiente")
    val fechaTasaBCV = _fechaTasaBCV.asStateFlow()

    private val _cargandoTasa = MutableStateFlow(false)
    val cargandoTasa = _cargandoTasa.asStateFlow()

    private val _errorTasa = MutableStateFlow<String?>(null)
    val errorTasa = _errorTasa.asStateFlow()

    fun iniciarTasaBCV(contexto: Context) {
        val prefs = contexto.getSharedPreferences("TasasZubli", Context.MODE_PRIVATE)
        val ultimoDolarStr = prefs.getString("ultimoDolar", "0.0") ?: "0.0"
        _precioDolarBCV.value = ultimoDolarStr.toDoubleOrNull() ?: 0.0
        _monedaVista.value = prefs.getString("monedaVista", "USD") ?: "USD"
        _fechaTasaBCV.value = prefs.getString("ultimaFecha", "Sin actualizar") ?: "Sin actualizar"

        actualizarTasaBCV(contexto)
    }

    fun cambiarMonedaVista(nuevaMoneda: String, contexto: Context) {
        _monedaVista.value = nuevaMoneda
        val prefs = contexto.getSharedPreferences("TasasZubli", Context.MODE_PRIVATE)
        prefs.edit().putString("monedaVista", nuevaMoneda).apply()
    }

    fun actualizarTasaBCV(contexto: Context) {
        viewModelScope.launch {
            _cargandoTasa.value = true
            _errorTasa.value = null
            try {
                val dolar = RetrofitClient.apiService.obtenerDolarOficial()
                val tasaPromedio = dolar.promedio
                _precioDolarBCV.value = tasaPromedio

                val formato = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                val fechaActual = formato.format(Date())
                _fechaTasaBCV.value = fechaActual

                val prefs = contexto.getSharedPreferences("TasasZubli", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("ultimoDolar", tasaPromedio.toString())
                    .putString("ultimaFecha", fechaActual)
                    .apply()
            } catch (e: Exception) {
                _errorTasa.value = "Sin internet: Usando última tasa guardada"
            } finally {
                _cargandoTasa.value = false
            }
        }
    }

    fun actualizarBuscador(nuevoTexto: String){
        _textoBusqueda.value   = nuevoTexto
    }
    // Archivar en lugar de borrar
    fun archivarDeudor(deudor: Deudor) {
        viewModelScope.launch {
            val deudorArchivado = deudor.copy(archivado = true)
            dao.actualizarDeudor(deudorArchivado)
        }
    }

    // Restaurar deudor
    fun restaurarDeudor(deudor: Deudor) {
        viewModelScope.launch {
            val deudorRestaurado = deudor.copy(archivado = false)
            dao.actualizarDeudor(deudorRestaurado)
        }
    }

    // Si de verdad quieres borrar (solo en Archivados)
    fun eliminarDeudorDefinitivamente(deudor: Deudor) {
        viewModelScope.launch {
            dao2.eliminarDeudasDeUsuario(deudor.id)
            dao.eliminarDeudor(deudor)
        }
    }
    // --- NUEVO: Obtener el historial completo de UNA sola persona ---
    fun obtenerHistorialDeudor(idDeudor: Int) = dao2.obtenerDeudasPorDeudor(idDeudor)


    // buscadore de deudores
    val deudorFiltrados = combine(
        listaDeudores,
        _textoBusqueda,
        _pestañaActual,
        dao2.obtenerTodasLasDeudas()
    ){ listaDeDeudores: List<Deudor>, texto: String, pestaña: Pestaña, listaDeDeudas: List<Deuda> -> // <-- ¡LA SOLUCIÓN ESTÁ AQUÍ!

        // 1. Primero filtramos por el buscador de texto y por el estado de archivado
        val filtradosPorTextoYArchivo = listaDeDeudores.filter { deudor ->
            // Filtro de archivado
            val coincideArchivo = if (pestaña == Pestaña.ARCHIVADOS) {
                deudor.archivado // Si estamos en archivados, solo mostramos los archivados
            } else {
                !deudor.archivado // Si no, mostramos solo los activos
            }
            
            // Filtro de texto
            val coincideTexto = if (texto.isBlank()) true else {
                deudor.nombre.contains(texto, ignoreCase = true) ||
                deudor.apellido.contains(texto, ignoreCase = true)
            }
            
            coincideArchivo && coincideTexto
        }

        // 2. Preparamos una lista vacía para guardar nuestras cajas
        val listaListaParaLaUI = mutableListOf<DeudorDetalle>()

        // 3. Revisamos deudor por deudor para armar su paquete
        for (deudor in filtradosPorTextoYArchivo) {

            // Buscamos sus deudas y calculamos el total
            val deudasDeEstaPersona = listaDeDeudas.filter { it.idDeudor == deudor.id }

            // Tomamos la deuda más reciente para mostrar en la tarjeta
            val deudaPrincipal = deudasDeEstaPersona.lastOrNull { it.rol != "PAGO" } 
                ?: deudasDeEstaPersona.lastOrNull()
            
            // Si no tiene deuda (ej. recién creado), le creamos una ficticia para que igual se muestre
            val deudaParaMostrar = deudaPrincipal ?: Deuda(idDeudor = deudor.id, montoInicial = 0.0, montoRestante = 0.0, tipoDeuda = "Sin registro", fecha = "", rol = "CLIENTE", descripcion = "", estado = "")

            // Calculamos matemáticamente las cuotas y vencimientos usando nuestro nuevo archivo CobroUtils
            val estadoCobro = calcularEstadoCobro(deudasDeEstaPersona)

            val perteneceAPestaña = when (pestaña) {
                Pestaña.PENDIENTES -> estadoCobro.saldo > 0.0
                Pestaña.HISTORIAL -> estadoCobro.saldo <= 0.0
                Pestaña.ARCHIVADOS -> true // En archivados ya filtramos antes
            }

            // Si pertenece a la pestaña seleccionada, armamos la caja y la guardamos en la lista
            if (perteneceAPestaña) {
                listaListaParaLaUI.add(
                    DeudorDetalle(
                        deudor = deudor,
                        deuda = deudaParaMostrar,
                        montoRestante = estadoCobro.saldo,
                        estadoCobro = estadoCobro // <-- Agregamos el cálculo
                    )
                )
            }
        }

        // --- ORDEN DE PRIORIDAD (La magia de la urgencia) ---
        // 1. Vencidos arriba
        // 2. Si no están vencidos, ordenamos por fecha de próximo vencimiento
        val listaOrdenada = listaListaParaLaUI.sortedWith(compareByDescending<DeudorDetalle> { 
            it.estadoCobro.vencida 
        }.thenBy { 
            it.estadoCobro.proximoVencimientoMillis ?: Long.MAX_VALUE 
        })

        // 4. Entregamos la lista final llena de cajas
        listaOrdenada

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    fun cambiarPestaña(nuevaPestaña: Pestaña){
        _pestañaActual.value = nuevaPestaña
    }

    fun registrarAbono(deudor: Deudor, montoAbono: Double) {
        viewModelScope.launch {
            // Obtenemos la fecha actual del teléfono para que el abono quede registrado con el día exacto
            val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val nuevoAbono = Deuda(
                idDeudor = deudor.id,
                montoInicial = 0.0,
                montoRestante = -montoAbono, // ¡El truco de magia! El signo negativo restará la deuda total
                tipoDeuda = "Abono / Pago Parcial",
                fecha = fechaActual,
                fechaMillis = System.currentTimeMillis(),
                tipo = "ABONO",
                descripcion = "Abono registrado desde la pantalla principal",
                rol = "PAGO", // Puedes usar un rol especial o dejarlo vacío
                estado = "Activo" // Debe coincidir con lo que suma tu DAO
            )

            // Insertamos el recibo en la base de datos
            dao2.agregarDeuda(nuevoAbono)
        }
    }

}
data class DeudorDetalle(
    val deudor: Deudor,
    val deuda: Deuda,
    val montoRestante: Double,
    val estadoCobro: EstadoCobro
)

