package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil_negocio")
data class PerfilNegocio(
    @PrimaryKey val id: Int = 1,
    val nombreNegocio: String = "Mi Taller de Personalización",
    val rifCedula: String = "",
    val telefonoContacto: String = "",
    val eslogan: String = "",
    val plantillaMensajeCobro: String = "Hola {cliente}, te recordamos tu saldo pendiente de \${monto} en tu pedido {producto}. Saludos de {negocio}.",
    val plantillaMensajeListo: String = "Hola {cliente}, ¡tu pedido de {producto} ya está listo! Saldo pendiente: \${monto}. Saludos de {negocio}."
)
