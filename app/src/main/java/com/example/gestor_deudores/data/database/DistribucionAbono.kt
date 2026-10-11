package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "distribucion_abonos",
    foreignKeys = [
        ForeignKey(
            entity = Deuda::class,
            parentColumns = ["id"],
            childColumns = ["abonoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["abonoId"]),
        Index(value = ["pedidoId"]),
        Index(value = ["fechaMillis"])
    ]
)
data class DistribucionAbono(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val abonoId: Int,
    val pedidoId: Int? = null,
    val tipoAbono: String = "VENTA_PEDIDO", // VENTA_PEDIDO, COBRO_MANUAL, NO_AFECTA
    val montoUsd: Double = 0.0,
    val material: Double = 0.0,
    val transporte: Double = 0.0,
    val insumos: Double = 0.0,
    val miPago: Double = 0.0,
    val ganancia: Double = 0.0,
    val origen: String = "EXACTO", // EXACTO, ESTIMADO
    val fechaMillis: Long = System.currentTimeMillis()
)
