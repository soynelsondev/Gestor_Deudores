package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos_cuenta",
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
data class MovimientoCuenta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cuentaId: Int,
    val tipo: String, // ABONO, GASTO, RETIRO, TRANSFERENCIA_ENTRADA, TRANSFERENCIA_SALIDA, AJUSTE
    val monto: Double, // Positivo para entrada, negativo para salida
    val moneda: String = "USD",
    val tasa: Double = 0.0,
    val referenciaId: Int? = null,
    val nota: String = "",
    val fechaMillis: Long = System.currentTimeMillis()
)
