package com.example.gestor_deudores.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.Deuda
import com.example.gestor_deudores.data.DeudaDao
import com.example.gestor_deudores.data.Deudor
import com.example.gestor_deudores.data.DeudorDao
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
    HISTORIAL
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

    fun actualizarBuscador(nuevoTexto: String){
        _textoBusqueda.value   = nuevoTexto
    }
    // Esta función va dentro de tu clase HomeViewModel
    fun eliminarHistorialCompleto(deudor: Deudor) {
        viewModelScope.launch {
            // 1. Primero borramos todas sus deudas registradas usando la función que acabas de agregar
            dao2.eliminarDeudasDeUsuario(deudor.id)
            // 2. Luego borramos el perfil del deudor
            dao.eliminarDeudor(deudor)
        }
    }


    // buscadore de deudores
    val deudorFiltrados = combine(
        listaDeudores,
        _textoBusqueda,
        _pestañaActual,
        dao2.obtenerTodasLasDeudas()
    ){ listaDeDeudores: List<Deudor>, texto: String, pestaña: Pestaña, listaDeDeudas: List<Deuda> -> // <-- ¡LA SOLUCIÓN ESTÁ AQUÍ!

        // 1. Primero filtramos por el buscador de texto
        val filtradosPorTexto = if (texto.isBlank()) {
            listaDeDeudores
        } else {
            listaDeDeudores.filter { deudor ->
                deudor.nombre.contains(texto, ignoreCase = true) ||
                        deudor.apellido.contains(texto, ignoreCase = true)
            }
        }

        // 2. Preparamos una lista vacía para guardar nuestras cajas
        val listaListaParaLaUI = mutableListOf<DeudorDetalle>()

        // 3. Revisamos deudor por deudor para armar su paquete
        for (deudor in filtradosPorTexto) {

            // Buscamos sus deudas y calculamos el total
            val deudasDeEstaPersona = listaDeDeudas.filter { it.idDeudor == deudor.id }
            val deudaTotal = deudasDeEstaPersona.sumOf { it.montoRestante }

            // Tomamos la deuda más reciente para mostrar en la tarjeta (tipo, fecha, descripción)
            val deudaPrincipal = deudasDeEstaPersona.lastOrNull()

            // Si la persona tiene al menos una deuda registrada, evaluamos en qué pestaña va
            if (deudaPrincipal != null) {

                val perteneceAPestaña = if (pestaña == Pestaña.PENDIENTES) {
                    deudaTotal > 0.0
                } else {
                    deudaTotal <= 0.0
                }

                // Si pertenece a la pestaña seleccionada, armamos la caja y la guardamos en la lista
                if (perteneceAPestaña) {
                    listaListaParaLaUI.add(
                        DeudorDetalle(
                            deudor = deudor,
                            deuda = deudaPrincipal,
                            montoRestante = deudaTotal
                        )
                    )
                }
            }
        }

        // 4. Entregamos la lista final llena de cajas
        listaListaParaLaUI

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
            val fechaActual = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

            val nuevoAbono = Deuda(
                idDeudor = deudor.id,
                montoInicial = 0.0,
                montoRestante = -montoAbono, // ¡El truco de magia! El signo negativo restará la deuda total
                tipoDeuda = "Abono / Pago Parcial",
                fecha = fechaActual,
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
    val montoRestante: Double
)

