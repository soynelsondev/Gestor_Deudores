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

data class R_DeudaEstado(
    val idDeudor: Int = 0,
    val monto : String = "",
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

    fun GuardarDeuda(){
        val estado = _uiState.value

        // A. Validaciones críticas
        if (estado.idDeudor == 0) {
            _uiState.update { it.copy(error = "Error crítico: Cliente no identificado") }
            return
        }

        val montoDouble = estado.monto.toDoubleOrNull()
        if (montoDouble == null || montoDouble <= 0.0) {
            _uiState.update { it.copy(error = "Ingresa un monto válido mayor a cero") }
            return
        }

        if (estado.fecha.isBlank() || estado.tipoDeuda.isBlank()) {
            _uiState.update { it.copy(error = "La fecha y el tipo de deuda son obligatorios") }
            return
        }

        viewModelScope.launch {
            val nuevaDeuda = Deuda(
            idDeudor = estado.idDeudor,
                montoInicial = montoDouble,
                montoRestante = montoDouble,
                tipoDeuda = estado.tipoDeuda,
                fecha = estado.fecha,
                rol = estado.rol,
                descripcion = estado.descripcion,
                estado = "Pendiente" // Estado inicial automático
            )
            deudaDao.agregarDeuda(nuevaDeuda)

            _uiState.update { it.copy(guardadoExitoso = true) }

        }

    }
    // Limpieza tras el éxito
    fun reiniciarEstadoGuardado() {
        _uiState.update { it.copy(guardadoExitoso = false) }
    }

}