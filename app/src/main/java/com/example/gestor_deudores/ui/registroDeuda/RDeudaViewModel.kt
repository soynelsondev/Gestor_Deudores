package com.example.gestor_deudores.ui.registroDeuda

import androidx.lifecycle.ViewModel
import com.example.gestor_deudores.data.DeudaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private val _uiEstado= MutableStateFlow(R_DeudaEstado())
    val uiEstado: StateFlow<R_DeudaEstado> =  _uiEstado.asStateFlow()

    


}