package com.example.gestor_deudores.ui.Registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.Deudor
import com.example.gestor_deudores.data.DeudorDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class RegistroDeudorUiState(
    val idActual: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val cedula: String = "",
    val error: String? = null,
    // Usamos esto para guardar el ID recién creado y pasarlo a la otra pantalla
    val nuevoDeudorId: Int = 0
)


// PASO 2: DEPENDENCIAS (Pedimos el DeudorDao)
class RegistroDeudorViewModel(private val deudorDao: DeudorDao) : ViewModel() {

    // Inicializamos el Estado (Privado para modificar, Público para leer)
    private val _uiState = MutableStateFlow(RegistroDeudorUiState())
    val uiState: StateFlow<RegistroDeudorUiState> = _uiState.asStateFlow()

    // ==========================================
    // PASO 3: EVENTOS (El usuario escribe)
    // ==========================================

    fun onNombreChange(nuevoNombre: String) {
        _uiState.update { it.copy(nombre = nuevoNombre, error = null) }
    }

    fun onApellidoChange(nuevoApellido: String) {
        _uiState.update { it.copy(apellido = nuevoApellido, error = null) }
    }

    fun onTelefonoChange(nuevoTelefono: String) {
        _uiState.update { it.copy(telefono = nuevoTelefono, error = null) }
    }

    fun onCedulaChange(nuevaCedula: String) {
        _uiState.update { it.copy(cedula = nuevaCedula, error = null) }
    }
    fun resetNuevoDeudorId() {
        _uiState.update { it.copy(nuevoDeudorId = 0) }
    }
    fun limpiarFormulario() {
        _uiState.update { RegistroDeudorUiState() }
    }

    fun cargarDeudor(id: Int) {
        viewModelScope.launch {
            val deudor = deudorDao.obtenerDeudorPorId(id)
            if (deudor != null) {
                // Rellenamos los campos de texto con los datos que vinieron de la base de datos
                _uiState.update {
                    it.copy(
                        idActual = deudor.id,
                        nombre = deudor.nombre,
                        apellido = deudor.apellido,
                        cedula = deudor.cedula,
                        telefono = deudor.telf
                    )
                }
            }
        }
    }



    fun guardarUsuario() {
        val estado = _uiState.value

        // A. Validación: Asegurarnos de que no deje el nombre vacío
        if (estado.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return // Cortamos la función aquí
        }

        // B. Guardar en segundo plano (Corrutina)
        viewModelScope.launch {
            // Armamos el objeto tal cual lo pide tu Entity
            val nuevoDeudor = Deudor(
                id = if(estado.idActual >0 ) estado.idActual else 0,
                nombre = estado.nombre,
                apellido = estado.apellido,
                cedula = estado.cedula,
                telf = estado.telefono
            )
            if (estado.idActual > 0) {
            // MODO EDICIÓN
            deudorDao.actualizarDeudor(nuevoDeudor)
            // Le avisamos a la pantalla que terminamos usando el mismo trigger (le pasamos el ID que editamos)
            _uiState.update { it.copy(nuevoDeudorId = estado.idActual) }
        } else {

            // C. Insertamos y capturamos el ID generado
            val idGenerado = deudorDao.agregarDeudor(nuevoDeudor)

            // D. Actualizamos el estado para avisarle a la pantalla que ya terminamos
            // y le entregamos el ID para que salte a la pantalla de Deuda
            _uiState.update { it.copy(nuevoDeudorId = idGenerado.toInt()) }
        }
        }
    }
}