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

    // ==========================================
    // PASO 4: ACCIÓN FINAL (Guardar)
    // ==========================================

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
                nombre = estado.nombre,
                apellido = estado.apellido,
                cedula = estado.cedula,
                telf = estado.telefono
            )

            // C. Insertamos y capturamos el ID generado
            val idGenerado = deudorDao.agregarDeudor(nuevoDeudor)

            // D. Actualizamos el estado para avisarle a la pantalla que ya terminamos
            // y le entregamos el ID para que salte a la pantalla de Deuda
            _uiState.update { it.copy(nuevoDeudorId = idGenerado.toInt()) }
        }
    }
}