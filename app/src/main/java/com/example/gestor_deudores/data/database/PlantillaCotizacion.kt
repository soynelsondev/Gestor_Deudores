package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plantillas_cotizacion")
data class PlantillaCotizacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombrePlantilla: String, // Ej: "Franela Algodón + DTF"
    
    // 1. Materiales (Pieza Base)
    val precioPaquetePieza: Double,
    val monedaPaquetePieza: String,
    val cantidadPaquetePieza: Int,
    
    // 2. Materiales (Empaque)
    val precioPaqueteEmpaque: Double,
    val monedaPaqueteEmpaque: String,
    val cantidadPaqueteEmpaque: Int,

    // 3. Servicios Directos
    val costoDtf: Double,
    val monedaDtf: String,
    val costoTransporte: Double,
    val monedaTransporte: String,
    val costoDiseno: Double,
    val monedaDiseno: String,

    // 4. Operatividad y Ganancia
    val porcentajeOperativo: Float,
    val porcentajeGananciaDetal: Float,
    val porcentajeGananciaMayor: Float
)
