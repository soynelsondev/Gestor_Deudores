package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedido_costo_lineas",
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
data class PedidoCostoLinea(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pedidoId: Int,
    val indiceArticulo: Int = 0,
    val sobre: String, // "MATERIAL", "TRANSPORTE", "INSUMOS"
    val nombre: String,
    val costoCentavos: Long,
    val tipoDetalle: String = "EXACTO" // "EXACTO", "RECUPERADO", "SIN_DETALLE"
)
