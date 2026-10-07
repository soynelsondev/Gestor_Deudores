package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plantillas_cotizacion")
data class PlantillaCotizacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombrePlantilla: String,
    
    val precioPaquetePieza: Double,
    val monedaPaquetePieza: String,
    val cantidadPaquetePieza: Int,
    
    val precioPaqueteEmpaque: Double,
    val monedaPaqueteEmpaque: String,
    val cantidadPaqueteEmpaque: Int,
    
    val precioPaquetePapel: Double,
    val monedaPaquetePapel: String,
    val cantidadPaquetePapel: Int,

    val precioTotalDtf: Double,
    val monedaDtf: String,
    val rendimientoDtf: Int,
    
    val costoTransporte: Double,
    val monedaTransporte: String,
    val rendimientoTransporte: Int,
    
    val costoDiseno: Double,
    val monedaDiseno: String,
    val rendimientoDiseno: Int,

    val costoExtra: Double,
    val monedaExtra: String,
    val rendimientoExtra: Int,

    val porcentajeOperativo: Float,
    val porcentajeGanancia: Float,
    val esPlantillaMayor: Boolean = false,
    val minimoUnidadesMayor: Int = 6,
    val costosAdicionalesJson: String = "[]",
    val minutosPorPieza: Double = 0.0,
    val tarifaPorHoraUsd: Double = 0.0,
    val comisionPorcentaje: Float = 0f,
    val escalasPrecioJson: String = "[]"
)

