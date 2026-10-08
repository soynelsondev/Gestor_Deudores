package com.example.gestor_deudores.data.database

import java.util.UUID

/**
 * REPRESENTA UN INSUMO O SERVICIO DINÁMICO DENTRO DE UNA PLANTILLA DE COTIZACIÓN.
 * Permite agregar cualquier tipo y cantidad de materiales o servicios (Taza, DTF, Empaque, Pasajes, etc.)
 */
data class InsumoCotizacion(
    val id: String = UUID.randomUUID().toString(),
    val insumoBibliotecaId: String? = null,
    val nombre: String = "",
    val categoria: String = "MATERIA_PRIMA", // "MATERIA_PRIMA", "INSUMO_EXTRA", "SERVICIO"
    val precioLote: Double = 0.0,
    val moneda: String = "USD", // "USD" o "VES"
    val rendimientoCantidad: Int = 1,
    val unidadLote: String = "Piezas",
    val unidadConsumo: String = "Piezas",
    val consumoPorPieza: Double = 1.0,
    val tipoCosto: String = "POR_PIEZA" // "POR_PIEZA" o "POR_PEDIDO"
) {
    fun calcularCostoUnitarioUsd(tasaBcv: Double = 1.0, cantidadPedido: Int = 1): Double {
        val tasaActiva = if (tasaBcv > 0.0) tasaBcv else 1.0
        val precioUsd = if (moneda == "VES") precioLote / tasaActiva else precioLote
        val cantLote = if (rendimientoCantidad > 0) rendimientoCantidad.toDouble() else 1.0

        // Conversión inteligente de unidades (m->cm, l->ml, kg->g)
        val factorConversion = when {
            unidadLote.contains("Metro", true) && unidadConsumo.contains("Centímetro", true) -> 100.0
            unidadLote.contains("Litro", true) && unidadConsumo.contains("Mililitro", true) -> 1000.0
            unidadLote.contains("Kilo", true) && unidadConsumo.contains("Gramo", true) -> 1000.0
            else -> 1.0
        }

        val capacidadTotalConsumo = cantLote * factorConversion
        val costoPorUnidadConsumo = precioUsd / capacidadTotalConsumo
        val costoPorPiezaBase = costoPorUnidadConsumo * (if (consumoPorPieza > 0) consumoPorPieza else 1.0)

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
