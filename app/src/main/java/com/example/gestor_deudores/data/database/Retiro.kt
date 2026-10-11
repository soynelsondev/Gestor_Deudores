package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "retiros",
    indices = [
        Index(value = ["fechaMillis"])
    ]
)
data class Retiro(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val montoUsd: Double,
    val moneda: String = "USD",
    val montoOriginal: Double = 0.0,
    val tasa: Double = 0.0,
    val cuentaId: Int? = null,
    val nota: String = "",
    val fechaMillis: Long = System.currentTimeMillis()
)
