package com.example.gestor_deudores.data.database

import java.util.UUID

/**
 * REPRESENTA UN INSUMO O SERVICIO DINÁMICO DENTRO DE UNA PLANTILLA DE COTIZACIÓN.
 * Permite agregar cualquier tipo y cantidad de materiales o servicios (Taza, DTF, Empaque, Pasajes, etc.)
 */
data class InsumoCotizacion(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val categoria: String = "MATERIA_PRIMA", // "MATERIA_PRIMA", "INSUMO_EXTRA", "SERVICIO"
    val precioLote: Double = 0.0,
    val moneda: String = "USD", // "USD" o "VES"
    val rendimientoCantidad: Int = 1,
    val tipoCosto: String = "POR_PIEZA" // "POR_PIEZA" o "POR_PEDIDO"
) {
    fun calcularCostoUnitarioUsd(tasaBcv: Double = 1.0, cantidadPedido: Int = 1): Double {
        val tasaActiva = if (tasaBcv > 0.0) tasaBcv else 1.0
        val precioUsd = if (moneda == "VES") precioLote / tasaActiva else precioLote
        val cant = if (rendimientoCantidad > 0) rendimientoCantidad else 1
        val costoPorPiezaBase = precioUsd / cant
        
        val unitarioFinal = if (tipoCosto == "POR_PEDIDO") {
            val q = if (cantidadPedido > 0) cantidadPedido else 1
            costoPorPiezaBase / q
        } else {
            costoPorPiezaBase
        }
        
        return java.math.BigDecimal.valueOf(unitarioFinal)
            .setScale(2, java.math.RoundingMode.HALF_UP)
            .toDouble()
    }
}
