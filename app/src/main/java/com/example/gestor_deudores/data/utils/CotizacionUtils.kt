package com.example.gestor_deudores.data.utils

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
 * Toma una plantilla y la tasa BCV actual (para convertir los insumos que estén en Bs a USD)
 * y devuelve todos los costos y ganancias desglosadas.
 * Se usa en el Cotizador, en los Pedidos y en el Resumen para garantizar que todos usen la misma fórmula.
 */
fun calcularCostosPlantilla(plantilla: PlantillaCotizacion, tasaBcv: Double = 1.0): DesgloseCostosPlantilla {
    val tasaActiva = if (tasaBcv > 0.0) tasaBcv else 1.0

    fun parseCant(cant: Int): Double = if (cant > 0) cant.toDouble() else 1.0
    fun aUsd(valor: Double, moneda: String): Double = if (moneda == "VES") valor / tasaActiva else valor

    // 1. Extraemos los costos unitarios convirtiendo a USD si es necesario
    val costoUnitarioPieza = aUsd(plantilla.precioPaquetePieza / parseCant(plantilla.cantidadPaquetePieza), plantilla.monedaPaquetePieza)
    val costoUnitarioEmpaque = aUsd(plantilla.precioPaqueteEmpaque / parseCant(plantilla.cantidadPaqueteEmpaque), plantilla.monedaPaqueteEmpaque)
    val costoUnitarioPapel = aUsd(plantilla.precioPaquetePapel / parseCant(plantilla.cantidadPaquetePapel), plantilla.monedaPaquetePapel)
    
    val dtfPorPieza = aUsd(plantilla.precioTotalDtf / parseCant(plantilla.rendimientoDtf), plantilla.monedaDtf)
    val transportePorPieza = aUsd(plantilla.costoTransporte / parseCant(plantilla.rendimientoTransporte), plantilla.monedaTransporte)
    val disenoPorPieza = aUsd(plantilla.costoDiseno / parseCant(plantilla.rendimientoDiseno), plantilla.monedaDiseno)
    val extraPorPieza = aUsd(plantilla.costoExtra / parseCant(plantilla.rendimientoExtra), plantilla.monedaExtra)

    // 2. Agrupación por categorías (para los sobres/fondos del Resumen)
    val costoInsumosBase = costoUnitarioEmpaque + costoUnitarioPapel + dtfPorPieza + disenoPorPieza + extraPorPieza
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
