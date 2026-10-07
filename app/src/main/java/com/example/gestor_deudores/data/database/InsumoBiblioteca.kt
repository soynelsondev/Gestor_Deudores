package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "biblioteca_insumos")
data class InsumoBiblioteca(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val categoria: String = "MATERIA_PRIMA", // "MATERIA_PRIMA", "INSUMO_EXTRA", "SERVICIO"
    val unidadLote: String = "Piezas",
    val precioLote: Double = 0.0,
    val cantidadLote: Int = 1,
    val moneda: String = "USD",
    val fechaActualizacion: String = ""
)
