package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas_bancarias")
data class CuentaBancaria(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String, // Ej: "Banco Mercantil", "Zelle", "Efectivo"
    val tipo: String = "BANCO",   // "BANCO", "BILLETERA", "EFECTIVO"
    val moneda: String = "USD", // "USD", "VES"
    val saldoActual: Double = 0.0
)
