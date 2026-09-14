package com.example.gestor_deudores.ui.registroDeuda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.Deuda
import com.example.gestor_deudores.data.DeudaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class R_DeudaEstado(
    val idDeudaActual: Int = 0,
    val idDeudor: Int = 0,
    val monto : String = "",
    val abonoInicial: String = "",
    val cuotas: String = "",
    val frecuenciaPago: String = "SEMANAL", // <-- NUEVO
    val tipoDeuda: String = "",
    val fecha: String = "",
    val rol: String = "CLIENTE",
    val descripcion: String = "",
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

class RDeudaViewModel(private val deudaDao: DeudaDao): ViewModel(){

    private val _uiState= MutableStateFlow(R_DeudaEstado())
    val uiEstado: StateFlow<R_DeudaEstado> =  _uiState.asStateFlow()

    // Esta función es vital: la llamas apenas abres la pantalla para inyectarle el ID del cliente
    fun inicializarIdDeudor(id: Int) {
        _uiState.update { it.copy(idDeudor = id) }
    }

    fun cargarDeuda(idDeuda: Int) {
        viewModelScope.launch {
            val deuda = deudaDao.obtenerDeudaPorId(idDeuda)
            if (deuda != null) {
                _uiState.update {
                    it.copy(
                        idDeudaActual = deuda.id,
                        idDeudor = deuda.idDeudor,
                        monto = deuda.montoInicial.toString(),
                        tipoDeuda = deuda.tipoDeuda,
                        fecha = deuda.fecha,
                        rol = deuda.rol,
                        descripcion = deuda.descripcion.substringBefore(" | Paga en")
                    )
                }
            }
        }
    }

    fun onMontoChange(nuevoMonto: String) {
        _uiState.update { it.copy(monto = nuevoMonto, error = null) }
    }

    fun onTipoDeudaChange(nuevoTipo: String) {
        _uiState.update { it.copy(tipoDeuda = nuevoTipo, error = null) }
    }

    fun onFechaChange(nuevaFecha: String) {
        _uiState.update { it.copy(fecha = nuevaFecha, error = null) }
    }

    fun onDescripcionChange(nuevaDescripcion: String) {
        _uiState.update { it.copy(descripcion = nuevaDescripcion, error = null) }
    }

    fun onRolChange(nuevoRol: String) {
        _uiState.update { it.copy(rol = nuevoRol, error = null) }
    }

    fun onFrecuenciaChange(nuevaFrecuencia: String) {
        _uiState.update { it.copy(frecuenciaPago = nuevaFrecuencia, error = null) }
    }

    fun onAbonoChange(nuevoAbono: String) {
        _uiState.update { it.copy(abonoInicial = nuevoAbono, error = null) }
    }

    fun onCuotasChange(nuevasCuotas: String) {
        // Solo permitimos números enteros para las cuotas
        if (nuevasCuotas.all { it.isDigit() }) {
            _uiState.update { it.copy(cuotas = nuevasCuotas, error = null) }
        }
    }

    fun GuardarDeuda(){
        val estado = _uiState.value

        val montoLimpio = estado.monto.replace(".", "").replace(",", ".")
        val monto_Double = montoLimpio.toDoubleOrNull() ?: 0.0

        val abonoLimpio = estado.abonoInicial.replace(".", "").replace(",", ".")
        val abono_Double = abonoLimpio.toDoubleOrNull() ?: 0.0

        if (monto_Double <= 0.0) {
            _uiState.update { it.copy(error = "Ingresa un monto válido mayor a cero") }
            return
        }
        if (estado.idDeudor == 0 || estado.fecha.isBlank() || estado.tipoDeuda.isBlank()) {
            _uiState.update { it.copy(error = "Faltan datos obligatorios") }
            return
        }

        // --- CÁLCULO INTELIGENTE DE CUOTAS Y FECHAS ---
        val numCuotas = estado.cuotas.toIntOrNull() ?: 1
        var textoCuotas = ""

        if (numCuotas > 1) {
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaBase = try { formato.parse(estado.fecha) } catch (e: Exception) {
                Date()
            }
            val calendario = Calendar.getInstance().apply { time = fechaBase }

            val fechasList = mutableListOf<String>()
            for (i in 1..numCuotas) {
                when (estado.frecuenciaPago) {
                    "SEMANAL" -> calendario.add(Calendar.DAY_OF_YEAR, 7)
                    "QUINCENAL" -> calendario.add(Calendar.DAY_OF_YEAR, 15)
                    "MENSUAL" -> calendario.add(Calendar.MONTH, 1)
                }
                fechasList.add(formato.format(calendario.time))
            }
            textoCuotas = " | Paga en $numCuotas cuotas (${estado.frecuenciaPago.lowercase()}). Vencimientos: ${fechasList.joinToString(", ")}"
        }

        val descripcionFinal = "${estado.descripcion}$textoCuotas"

        viewModelScope.launch {
            if (estado.idDeudaActual > 0) {
                // MODO EDICIÓN
                val deudaExistente = deudaDao.obtenerDeudaPorId(estado.idDeudaActual)
                if (deudaExistente != null) {
                    val diferencia = monto_Double - deudaExistente.montoInicial
                    val deudaEditada = deudaExistente.copy(
                        montoInicial = monto_Double,
                        montoRestante = deudaExistente.montoRestante + diferencia,
                        tipoDeuda = estado.tipoDeuda,
                        fecha = estado.fecha,
                        rol = estado.rol,
                        descripcion = descripcionFinal
                    )
                    deudaDao.actualizarDeuda(deudaEditada)
                    
                    // Si se hace un nuevo abono durante la edición (aunque usualmente se hace desde el home, lo soportamos)
                    if (abono_Double > 0.0) {
                        val reciboAdelanto = Deuda(
                            idDeudor = estado.idDeudor,
                            montoInicial = 0.0,
                            montoRestante = -abono_Double,
                            tipoDeuda = "Abono Adicional",
                            fecha = estado.fecha,
                            rol = "PAGO",
                            descripcion = "Abono agregado en edición",
                            estado = "Activo"
                        )
                        deudaDao.agregarDeuda(reciboAdelanto)
                    }
                }
            } else {
                // MODO CREACIÓN
                val nuevaDeuda = Deuda(
                    idDeudor = estado.idDeudor,
                    montoInicial = monto_Double,
                    montoRestante = monto_Double,
                    tipoDeuda = estado.tipoDeuda,
                    fecha = estado.fecha,
                    rol = estado.rol,
                    descripcion = descripcionFinal,
                    estado = "Pendiente"
                )
                deudaDao.agregarDeuda(nuevaDeuda)

                if (abono_Double > 0.0) {
                    val reciboAdelanto = Deuda(
                        idDeudor = estado.idDeudor,
                        montoInicial = 0.0,
                        montoRestante = -abono_Double,
                        tipoDeuda = "Abono Inicial",
                        fecha = estado.fecha,
                        rol = "PAGO",
                        descripcion = "Adelanto entregado al registrar el pedido",
                        estado = "Activo"
                    )
                    deudaDao.agregarDeuda(reciboAdelanto)
                }
            }

            _uiState.update { it.copy(guardadoExitoso = true) }
        }
    }
    // Limpieza tras el éxito
    fun reiniciarEstadoGuardado() {
        _uiState.value = R_DeudaEstado()
    }

}