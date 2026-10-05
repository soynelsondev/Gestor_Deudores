package com.example.gestor_deudores.data.utils

import com.example.gestor_deudores.ui.Resumen.AlertaFinanciera
import java.util.Locale

/**
 * MOTOR DE INTELIGENCIA DE NEGOCIO (ASESOR ZUBLI - NÚMEROS PUROS)
 * Analiza las variables financieras y devuelve explicaciones numéricas exactas
 * basadas 100% en los datos reales registrados en SQLite.
 */
object AsesorZubliUtils {

    fun generarConsejos(
        ingresosCobradosUsd: Double,
        gananciaNetaUsd: Double,
        porCobrarUsd: Double,
        costoPasajesUsd: Double,
        costoInsumosUsd: Double,
        topProductos: List<Pair<String, Int>>,
        clienteMasDeudorNombre: String?,
        clienteMasDeudorMonto: Double,
        clienteVipNombre: String?,
        clienteVipMonto: Double,
        gananciaNetaMesAnterior: Double = 0.0,
        ingresosCobradosMesAnterior: Double = 0.0,
        costoPasajesMesAnterior: Double = 0.0,
        costoInsumosMesAnterior: Double = 0.0
    ): List<AlertaFinanciera> {
        val alertas = mutableListOf<AlertaFinanciera>()

        if (ingresosCobradosUsd <= 0.0 && porCobrarUsd <= 0.0) {
            alertas.add(
                AlertaFinanciera(
                    titulo = "¡Bienvenido al Dashboard!",
                    mensaje = "💡 Registra tus pedidos y abonos para que el Asesor Zubli empiece a analizar tus finanzas con datos numéricos reales.",
                    nivel = "INFO"
                )
            )
            return alertas
        }

        val pctPasajes = if (ingresosCobradosUsd > 0) (costoPasajesUsd / ingresosCobradosUsd) * 100 else 0.0
        val prodText = if (topProductos.isNotEmpty()) {
            topProductos.take(2).joinToString(" y ") { "${it.first} (${it.second} pcs)" }
        } else {
            "tus productos generales"
        }

        // =====================================
        // REGLA 1: ANÁLISIS DE COMPARATIVA INTERMENSUAL (NÚMEROS PUROS)
        // =====================================
        if (ingresosCobradosMesAnterior > 0.0) {
            val diffGanancia = gananciaNetaUsd - gananciaNetaMesAnterior
            val diffIngresos = ingresosCobradosUsd - ingresosCobradosMesAnterior
            val diffPasajes = costoPasajesUsd - costoPasajesMesAnterior
            val diffInsumos = costoInsumosUsd - costoInsumosMesAnterior

            if (kotlin.math.abs(diffGanancia) <= 2.5) {
                // ESCENARIO: RENDIMIENTO ESTABLE / PAREJO
                val msgEstable = "📊 Análisis Numérico vs Mes Pasado: RENDIMIENTO ESTABLE. Cobraste $${String.format(Locale.US, "%.2f", ingresosCobradosUsd)} (Dif: $${String.format(Locale.US, "%+.2f", diffIngresos)}) con una ganancia neta libre de $${String.format(Locale.US, "%.2f", gananciaNetaUsd)} (Variación: 0.0%). Los costos se mantuvieron equilibrados: Insumos $${String.format(Locale.US, "%.2f", costoInsumosUsd)} y Logística $${String.format(Locale.US, "%.2f", costoPasajesUsd)}. Productos principales: $prodText."
                alertas.add(AlertaFinanciera("Desempeño Numérico Estable", msgEstable, "INFO"))
            } else if (diffGanancia > 2.5) {
                // ESCENARIO: CRECIMIENTO
                val pctIncr = (diffGanancia / gananciaNetaMesAnterior) * 100
                val msgMejor = "📊 Análisis Numérico vs Mes Pasado: CRECIMIENTO DE MARGEN. Cobraste $${String.format(Locale.US, "%.2f", ingresosCobradosUsd)} (+$${String.format(Locale.US, "%.2f", diffIngresos)}) y obtuviste $${String.format(Locale.US, "%.2f", gananciaNetaUsd)} de ganancia neta (+$${String.format(Locale.US, "%.2f", diffGanancia)} | +${String.format(Locale.US, "%.1f", pctIncr)}%). Insumos: $${String.format(Locale.US, "%.2f", costoInsumosUsd)} | Logística: $${String.format(Locale.US, "%.2f", costoPasajesUsd)}. Motor principal de movimiento: $prodText."
                alertas.add(AlertaFinanciera("Incremento Numérico de Margen", msgMejor, "SUCCESS"))
            } else {
                // ESCENARIO: DISMINUCIÓN
                val absDiffG = kotlin.math.abs(diffGanancia)
                val pctDecr = (absDiffG / gananciaNetaMesAnterior) * 100
                val msgPeor = "📊 Análisis Numérico vs Mes Pasado: DISMINUCIÓN DE MARGEN. Cobraste $${String.format(Locale.US, "%.2f", ingresosCobradosUsd)} (Dif: $${String.format(Locale.US, "%.2f", diffIngresos)}) y la ganancia neta bajó a $${String.format(Locale.US, "%.2f", gananciaNetaUsd)} (-$${String.format(Locale.US, "%.2f", absDiffG)} | -${String.format(Locale.US, "%.1f", pctDecr)}%). Explicación de los números: El gasto en transporte fue de $${String.format(Locale.US, "%.2f", costoPasajesUsd)} (Dif: $${String.format(Locale.US, "%+.2f", diffPasajes)}) e insumos $${String.format(Locale.US, "%.2f", costoInsumosUsd)} (Dif: $${String.format(Locale.US, "%+.2f", diffInsumos)})."
                alertas.add(AlertaFinanciera("Ajuste Numérico de Margen", msgPeor, "WARNING"))
            }
        }

        // =====================================
        // REGLA 2: SANGRADO POR LOGÍSTICA (PASAJES > 12%)
        // =====================================
        if (pctPasajes > 12.0) {
            val msgLogistica = "⚠️ Explicación Numérica de Logística: Los pasajes y envíos representaron el ${String.format(Locale.US, "%.1f", pctPasajes)}% ($${String.format(Locale.US, "%.2f", costoPasajesUsd)}) de tus ingresos totales de $${String.format(Locale.US, "%.2f", ingresosCobradosUsd)}."
            alertas.add(AlertaFinanciera("Impacto Numérico de Logística", msgLogistica, "WARNING"))
        }

        // =====================================
        // REGLA 3: RIESGO DE CLIENTES Y COBRANZAS
        // =====================================
        if (clienteMasDeudorNombre != null && clienteMasDeudorMonto > 20.0) {
            val msgRiesgo = "🚨 Balance de Deuda de Cliente: '$clienteMasDeudorNombre' concentra el mayor saldo pendiente individual ($${String.format(Locale.US, "%.2f", clienteMasDeudorMonto)}) dentro del total por cobrar de $${String.format(Locale.US, "%.2f", porCobrarUsd)}."
            alertas.add(AlertaFinanciera("Concentración de Cartera", msgRiesgo, "DANGER"))
        }

        // =====================================
        // REGLA 4: CLIENTE VIP DE MAYOR APORTE
        // =====================================
        if (clienteVipNombre != null && clienteVipMonto > 40.0) {
            val pctAporte = (clienteVipMonto / ingresosCobradosUsd.coerceAtLeast(0.01)) * 100
            val msgVip = "🏆 Cliente de Mayor Aporte: '$clienteVipNombre' representó el ${String.format(Locale.US, "%.1f", pctAporte)}% ($${String.format(Locale.US, "%.2f", clienteVipMonto)}) del dinero cobrado en caja en este período."
            alertas.add(AlertaFinanciera("Aporte Principal de Caja", msgVip, "SUCCESS"))
        }

        return alertas
    }
}