package com.example.gestor_deudores.ui.Registro

import androidx.lifecycle.ViewModel
import com.example.gestor_deudores.data.DeudorDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class RegistroDeudorUiState(
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val cedula: String = "",
    val error: String? = null,
    // Usamos esto para guardar el ID recién creado y pasarlo a la otra pantalla
    val nuevoDeudorId: Int? = null
)


class registroViewModel(private val deudorDao: DeudorDao): ViewModel(){
    private val estado = MutableStateFlow(RegistroDeudorUiState())
    val iuEstado: StateFlow<RegistroDeudorUiState> = estado.asStateFlow()
    
}