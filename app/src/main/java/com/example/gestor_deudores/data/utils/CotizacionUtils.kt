package com.example.gestor_deudores.data.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.gestor_deudores.data.database.InsumoCotizacion
import com.example.gestor_deudores.data.database.PlantillaCotizacion

data class DesgloseCostosPlantilla(
    val costoPiezaBaseUsd: Double,
    val costoPasajeUsd: Double,
    val costoInsumosUsd: Double,
    val costoTotalProduccionUsd: Double,
    val precioSugeridoUsd: Double,
    val gananciaUsd: Double
)

/**
 * CEREBRO MATEMÁTICO CENTRALIZADO PARA COTIZACIONES.
 * Soporta Insumos Dinámicos ilimitados (Materia Prima, Insumos Extras, Servicios)
 * y mantiene retrocompatibilidad con los campos tradicionales.
 */
fun calcularCostosPlantilla(plantilla: PlantillaCotizacion, tasaBcv: Double = 1.0): DesgloseCostosPlantilla {
    val tasaActiva = if (tasaBcv > 0.0) tasaBcv else 1.0

    // Intentamos parsear insumos dinámicos desde JSON
    val insumosDinamicos: List<InsumoCotizacion> = try {
        if (plantilla.costosAdicionalesJson.isNotBlank() && plantilla.costosAdicionalesJson != "[]") {
            val type = object : TypeToken<List<InsumoCotizacion>>() {}.type
            Gson().fromJson(plantilla.costosAdicionalesJson, type) ?: emptyList()
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val costoUnitarioPieza: Double
    val transportePorPieza: Double
    val costoInsumosBase: Double

    if (insumosDinamicos.isNotEmpty()) {
        // --- CÁLCULO VÍA INSUMOS DINÁMICOS ILIMITADOS ---
        costoUnitarioPieza = insumosDinamicos.filter { it.categoria == "MATERIA_PRIMA" }
            .sumOf { it.calcularCostoUnitarioUsd(tasaActiva) }
        transportePorPieza = insumosDinamicos.filter { it.categoria == "SERVICIO" }
            .sumOf { it.calcularCostoUnitarioUsd(tasaActiva) }
        costoInsumosBase = insumosDinamicos.filter { it.categoria == "INSUMO_EXTRA" }
            .sumOf { it.calcularCostoUnitarioUsd(tasaActiva) }
    } else {
        // --- CÁLCULO TRADICIONAL (FALLBACK) ---
        fun parseCant(cant: Int): Double = if (cant > 0) cant.toDouble() else 1.0
        fun aUsd(valor: Double, moneda: String): Double = if (moneda == "VES") valor / tasaActiva else valor

        val cPieza = aUsd(plantilla.precioPaquetePieza / parseCant(plantilla.cantidadPaquetePieza), plantilla.monedaPaquetePieza)
        val cEmpaque = aUsd(plantilla.precioPaqueteEmpaque / parseCant(plantilla.cantidadPaqueteEmpaque), plantilla.monedaPaqueteEmpaque)
        val cPapel = aUsd(plantilla.precioPaquetePapel / parseCant(plantilla.cantidadPaquetePapel), plantilla.monedaPaquetePapel)
        val dtf = aUsd(plantilla.precioTotalDtf / parseCant(plantilla.rendimientoDtf), plantilla.monedaDtf)
        val transp = aUsd(plantilla.costoTransporte / parseCant(plantilla.rendimientoTransporte), plantilla.monedaTransporte)
        val diseno = aUsd(plantilla.costoDiseno / parseCant(plantilla.rendimientoDiseno), plantilla.monedaDiseno)
        val extra = aUsd(plantilla.costoExtra / parseCant(plantilla.rendimientoExtra), plantilla.monedaExtra)

        costoUnitarioPieza = cPieza
        transportePorPieza = transp
        costoInsumosBase = cEmpaque + cPapel + dtf + diseno + extra
    }

    // 2. Subtotal Materiales
    val subtotalMateriales = costoUnitarioPieza + transportePorPieza + costoInsumosBase
    
    // 3. Costo Operativo
    val costoOperativo = subtotalMateriales * (plantilla.porcentajeOperativo / 100.0)
    val costoTotalProduccion = subtotalMateriales + costoOperativo
    
    // 4. Agrupamos el operativo dentro de "Insumos" para tener los 3 grandes bloques
    val costoInsumosFinal = costoInsumosBase + costoOperativo

    // 5. Cálculos de Venta y Ganancia
    var precioVentaSug = 0.0
    var ganancia = 0.0

    if (costoTotalProduccion > 0.0) {
        val margenDecimal = plantilla.porcentajeGanancia / 100.0
        precioVentaSug = if (margenDecimal < 1.0) costoTotalProduccion / (1.0 - margenDecimal) else 0.0
        ganancia = precioVentaSug - costoTotalProduccion
    }

    return DesgloseCostosPlantilla(
        costoPiezaBaseUsd = costoUnitarioPieza.redondear2(),
        costoPasajeUsd = transportePorPieza.redondear2(),
        costoInsumosUsd = costoInsumosFinal.redondear2(),
        costoTotalProduccionUsd = costoTotalProduccion.redondear2(),
        precioSugeridoUsd = precioVentaSug.redondear2(),
        gananciaUsd = ganancia.redondear2()
    )
}
