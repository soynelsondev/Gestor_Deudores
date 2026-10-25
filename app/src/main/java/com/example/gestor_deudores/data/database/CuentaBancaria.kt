package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas_bancarias")
data class CuentaBancaria(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String, // Ej: "Banco Mercantil", "Zelle", "Efectivo"
    val tipo: String = "BANCO",   // "BANCO", "PAGO_MOVIL", "EFECTIVO", "BILLETERA", "BANCO_USD", "OTRA"
    val moneda: String = "USD", // "USD", "VES"
    val saldoActual: Double = 0.0,
    val saldoInicial: Double = 0.0,
    val activa: Boolean = true,
    val institucion: String? = null,
    val alias: String? = null,
    val uso: String? = null, // "Cobros", "Reposición", "Ahorro", "Gastos del taller", "Personal"
    val metaUsd: Double? = null,
    val datosDePago: String? = null,
    val color: String? = null,
    val icono: String? = null,
    val orden: Int = 0
)
