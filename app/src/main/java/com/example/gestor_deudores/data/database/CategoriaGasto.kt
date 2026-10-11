package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categoria_gastos")
data class CategoriaGasto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val tipo: String = "COMPRA_INSUMO", // COMPRA_INSUMO, GASTO_TALLER
    val sobreAsociado: String? = null,  // MATERIAL, TRANSPORTE, INSUMOS, NONE
    val recurrente: Boolean = false,
    val activa: Boolean = true
)
