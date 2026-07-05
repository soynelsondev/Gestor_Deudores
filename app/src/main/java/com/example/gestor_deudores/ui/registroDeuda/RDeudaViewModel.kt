package com.example.gestor_deudores.ui.registroDeuda

import androidx.lifecycle.ViewModel
import com.example.gestor_deudores.data.DeudaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class R_DeudaEstado(
    val idDeudor: Int = 0,
    val monto : String = "",
    val tipoDeuda: String = "",
    val fecha: String = "",
    val rol: String = "Cliente",
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


}