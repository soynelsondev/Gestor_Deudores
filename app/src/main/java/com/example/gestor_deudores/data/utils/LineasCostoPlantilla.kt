package com.example.gestor_deudores.data.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.gestor_deudores.data.database.InsumoCotizacion
import com.example.gestor_deudores.data.database.PlantillaCotizacion

/**
 * REPRESENTA UNA LÍNEA INDIVIDUAL DE COSTO EXTRAÍDA DE UNA PLANTILLA DE COTIZACIÓN.
 * sobre: "MATERIAL", "TRANSPORTE" o "INSUMOS"
 */
data class LineaCostoUnitaria(
    val sobre: String,
    val nombre: String,
    val costoUnitarioSinRedondearUsd: Double
)

/**
 * DESGLOSA UNA PLANTILLA EN SUS LÍNEAS REALES DE COSTO UNITARIO SIN REDONDEAR.
 * Replica exactamente la aritmética de calcularCostosPlantilla (CotizacionUtils.kt)
 * sin modificar ninguna función del cotizador ni alterar el cálculo original.
 */
fun desglosarLineasPlantilla(
    plantilla: PlantillaCotizacion,
    tasaBcv: Double = 1.0,
    cantidadPedido: Int = 1
): List<LineaCostoUnitaria> {
    val tasaActiva = if (tasaBcv > 0.0) tasaBcv else 1.0

    val insumosDinamicos: List<InsumoCotizacion> = try {
        if (plantilla.costosAdicionalesJson.isNotBlank() && plantilla.costosAdicionalesJson != "[]") {
            val type = object : TypeToken<List<InsumoCotizacion>>() {}.type
            Gson().fromJson(plantilla.costosAdicionalesJson, type) ?: emptyList()
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    fun parseCant(cant: Int): Double = if (cant > 0) cant.toDouble() else 1.0
    fun aUsd(valor: Double, moneda: String): Double = if (moneda == "VES") valor / tasaActiva else valor

    val lineas = mutableListOf<LineaCostoUnitaria>()

    // 1. SOBRE MATERIAL: Pieza tradicional y dinámicos MATERIA_PRIMA
    val cPiezaTrad = aUsd(plantilla.precioPaquetePieza / parseCant(plantilla.cantidadPaquetePieza), plantilla.monedaPaquetePieza)
    if (cPiezaTrad > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "MATERIAL",
                nombre = plantilla.nombrePlantilla.ifBlank { "Pieza Base" },
                costoUnitarioSinRedondearUsd = cPiezaTrad
            )
        )
    }

    val insumosMateriaPrima = insumosDinamicos.filter { it.categoria == "MATERIA_PRIMA" }
    for (insumo in insumosMateriaPrima) {
        val c = insumo.calcularCostoUnitarioUsd(tasaActiva, cantidadPedido)
        if (c > 0.0) {
            lineas.add(
                LineaCostoUnitaria(
                    sobre = "MATERIAL",
                    nombre = insumo.nombre.ifBlank { "Materia Prima" },
                    costoUnitarioSinRedondearUsd = c
                )
            )
        }
    }

    // 2. SOBRE TRANSPORTE: Transporte tradicional y dinámicos SERVICIO
    val transpTrad = aUsd(plantilla.costoTransporte / parseCant(plantilla.rendimientoTransporte), plantilla.monedaTransporte)
    if (transpTrad > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "TRANSPORTE",
                nombre = "Transporte",
                costoUnitarioSinRedondearUsd = transpTrad
            )
        )
    }

    val insumosServicio = insumosDinamicos.filter { it.categoria == "SERVICIO" }
    for (insumo in insumosServicio) {
        val c = insumo.calcularCostoUnitarioUsd(tasaActiva, cantidadPedido)
        if (c > 0.0) {
            lineas.add(
                LineaCostoUnitaria(
                    sobre = "TRANSPORTE",
                    nombre = insumo.nombre.ifBlank { "Servicio / Transporte" },
                    costoUnitarioSinRedondearUsd = c
                )
            )
        }
    }

    // 3. SOBRE INSUMOS: Empaque, papel, DTF, diseño, extra tradicional, dinámicos INSUMO_EXTRA y Operatividad
    val cEmpaqueTrad = aUsd(plantilla.precioPaqueteEmpaque / parseCant(plantilla.cantidadPaqueteEmpaque), plantilla.monedaPaqueteEmpaque)
    if (cEmpaqueTrad > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "INSUMOS",
                nombre = "Empaque",
                costoUnitarioSinRedondearUsd = cEmpaqueTrad
            )
        )
    }

    val cPapelTrad = aUsd(plantilla.precioPaquetePapel / parseCant(plantilla.cantidadPaquetePapel), plantilla.monedaPaquetePapel)
    if (cPapelTrad > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "INSUMOS",
                nombre = "Papel de sublimación",
                costoUnitarioSinRedondearUsd = cPapelTrad
            )
        )
    }

    val dtfTrad = aUsd(plantilla.precioTotalDtf / parseCant(plantilla.rendimientoDtf), plantilla.monedaDtf)
    if (dtfTrad > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "INSUMOS",
                nombre = "DTF / impresión",
                costoUnitarioSinRedondearUsd = dtfTrad
            )
        )
    }

    val disenoTrad = aUsd(plantilla.costoDiseno / parseCant(plantilla.rendimientoDiseno), plantilla.monedaDiseno)
    if (disenoTrad > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "INSUMOS",
                nombre = "Diseño",
                costoUnitarioSinRedondearUsd = disenoTrad
            )
        )
    }

    val extraTrad = aUsd(plantilla.costoExtra / parseCant(plantilla.rendimientoExtra), plantilla.monedaExtra)
    if (extraTrad > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "INSUMOS",
                nombre = "Insumos Extra",
                costoUnitarioSinRedondearUsd = extraTrad
            )
        )
    }

    val insumosExtraDin = insumosDinamicos.filter { it.categoria == "INSUMO_EXTRA" }
    for (insumo in insumosExtraDin) {
        val c = insumo.calcularCostoUnitarioUsd(tasaActiva, cantidadPedido)
        if (c > 0.0) {
            lineas.add(
                LineaCostoUnitaria(
                    sobre = "INSUMOS",
                    nombre = insumo.nombre.ifBlank { "Insumo Extra" },
                    costoUnitarioSinRedondearUsd = c
                )
            )
        }
    }

    // Operatividad: porcentaje sobre subtotal de materiales (Material + Transporte + Insumos Base)
    val cPiezaDinSum = insumosMateriaPrima.sumOf { it.calcularCostoUnitarioUsd(tasaActiva, cantidadPedido) }
    val transpDinSum = insumosServicio.sumOf { it.calcularCostoUnitarioUsd(tasaActiva, cantidadPedido) }
    val cInsumosDinSum = insumosExtraDin.sumOf { it.calcularCostoUnitarioUsd(tasaActiva, cantidadPedido) }

    val costoUnitarioPiezaTotal = cPiezaTrad + cPiezaDinSum
    val transportePorPiezaTotal = transpTrad + transpDinSum
    val costoInsumosBaseTotal = cEmpaqueTrad + cPapelTrad + dtfTrad + disenoTrad + extraTrad + cInsumosDinSum

    val subtotalMateriales = costoUnitarioPiezaTotal + transportePorPiezaTotal + costoInsumosBaseTotal
    val costoOperativo = subtotalMateriales * (plantilla.porcentajeOperativo / 100.0)

    if (costoOperativo > 0.0) {
        lineas.add(
            LineaCostoUnitaria(
                sobre = "INSUMOS",
                nombre = "Operatividad (luz, merma y desgaste)",
                costoUnitarioSinRedondearUsd = costoOperativo
            )
        )
    }

    return lineas
}
