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
    
    // Plantillas de Cobro por Bloques
    val saludoCobro: String = "¡Hola! Te escribimos con mucho gusto de parte de nuestro taller.",
    val cierreCobro: String = "Por favor indícanos cuándo podrías realizar el pago. Puedes pagar por Pago Móvil o Zelle. ¡Muchas gracias!",
    
    // Plantillas de Pedido Listo por Bloques
    val saludoListo: String = "¡Hola! Te tenemos excelentes noticias de parte de nuestro taller.",
    val cierreListo: String = "Puedes pasar retirando tu pedido en nuestro horario habitual. ¡Te esperamos!"
)
