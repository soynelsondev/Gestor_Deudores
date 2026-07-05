package com.example.gestor_deudores.ui.registroDeuda

data class R_DeudaEstado(
    val montoInicial : Double = 0.0,
    val montoRestante: Double= 0.0,
    val tipoDeuda: String = "",
    val fecha: String = "",
    val rol: String = "",
    val descripcion: String = "",
    val estado : String = "",
    val error: String? = null,
)