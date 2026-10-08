package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "historial_precios_insumo")
data class HistorialPrecioInsumo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val insumoId: String = "",
    val precioLote: Double = 0.0,
    val cantidadLote: Int = 1,
    val fecha: String = ""
)
