package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaGasto::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoriaId"]),
        Index(value = ["fechaMillis"])
    ]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoriaId: Int,
    val descripcion: String,
    val montoUsd: Double,
    val moneda: String = "USD",
    val montoOriginal: Double = 0.0,
    val tasa: Double = 0.0,
    val cuentaId: Int? = null,
    val proveedor: String? = null,
    val fechaMillis: Long = System.currentTimeMillis()
)
