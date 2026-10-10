package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedido_imagenes",
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pedidoId"])]
)
data class PedidoImagen(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pedidoId: Int,
    val itemId: String? = null,
    val ruta: String,
    val rutaMiniatura: String = "",
    val tipo: String = "DISENO", // DISENO, COMPROBANTE, ENTREGA
    val fechaMillis: Long = System.currentTimeMillis()
)
