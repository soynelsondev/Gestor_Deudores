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
    val porcentajeGananciaDetal: Float,
    val porcentajeGananciaMayor: Float
) {
    // Calculamos el costo base sumando todo
    fun calcularCostoProduccionBase(): Double {
        val costoPieza = precioPaquetePieza / if (cantidadPaquetePieza > 0) cantidadPaquetePieza else 1
        val costoEmpaque = precioPaqueteEmpaque / if (cantidadPaqueteEmpaque > 0) cantidadPaqueteEmpaque else 1
        val costoPapel = precioPaquetePapel / if (cantidadPaquetePapel > 0) cantidadPaquetePapel else 1
        val costoDtf = precioTotalDtf / if (rendimientoDtf > 0) rendimientoDtf else 1
        val costoTransp = costoTransporte / if (rendimientoTransporte > 0) rendimientoTransporte else 1
        val costoDis = costoDiseno / if (rendimientoDiseno > 0) rendimientoDiseno else 1
        val costoExt = costoExtra / if (rendimientoExtra > 0) rendimientoExtra else 1

        val costoBase = costoPieza + costoEmpaque + costoPapel + costoDtf + costoTransp + costoDis + costoExt
        val margenOp = porcentajeOperativo / 100f
        return costoBase / (1 - margenOp)
    }

    // Calcula el precio de Venta Sugerida al Detal para Auto-completarlo en Pedidos
    fun calcularPrecioDetalUsd(): Double {
        val costoProd = calcularCostoProduccionBase()
        val margen = porcentajeGananciaDetal / 100f
        return costoProd / (1 - margen)
    }
}
