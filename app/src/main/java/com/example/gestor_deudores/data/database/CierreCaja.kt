package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cierres_caja",
    foreignKeys = [
        ForeignKey(
            entity = CuentaBancaria::class,
            parentColumns = ["id"],
            childColumns = ["cuentaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cuentaId"]),
        Index(value = ["fechaMillis"])
    ]
)
data class CierreCaja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cuentaId: Int,
    val saldoEsperado: Double,
    val saldoReal: Double,
    val diferencia: Double,
    val nota: String = "",
    val fechaMillis: Long = System.currentTimeMillis()
)
