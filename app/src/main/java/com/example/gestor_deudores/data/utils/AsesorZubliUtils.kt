package com.example.gestor_deudores.data.utils

import com.example.gestor_deudores.ui.Resumen.AlertaFinanciera
import kotlin.random.Random

/**
 * MOTOR DE INTELIGENCIA DE NEGOCIO (ASESOR ZUBLI)
 * Analiza las variables financieras y devuelve estrategias y consejos avanzados
 * de sublimación y personalización.
 */
object AsesorZubliUtils {

    fun generarConsejos(
        ingresosCobradosUsd: Double,
        gananciaNetaUsd: Double,
        porCobrarUsd: Double,
        costoPasajesUsd: Double,
        topProductoNombre: String?,
        clienteMasDeudorNombre: String?,
        clienteMasDeudorMonto: Double,
        clienteVipNombre: String?,
        clienteVipMonto: Double
    ): List<AlertaFinanciera> {
        val alertas = mutableListOf<AlertaFinanciera>()

        if (ingresosCobradosUsd <= 0.0 && porCobrarUsd <= 0.0) {
            alertas.add(
                AlertaFinanciera(
                    titulo = "¡Bienvenido al Dashboard!",
                    mensaje = "💡 Registra tus pedidos y abonos para que el Asesor Zubli empiece a analizar tus finanzas y te dé estrategias de negocio.",
                    nivel = "INFO"
                )
            )
            return alertas
        }

        val margenGanancia = if (ingresosCobradosUsd > 0) (gananciaNetaUsd / ingresosCobradosUsd) * 100 else 0.0
        val pctPasajes = if (ingresosCobradosUsd > 0) (costoPasajesUsd / ingresosCobradosUsd) * 100 else 0.0
        val totalVendido = ingresosCobradosUsd + porCobrarUsd
        val pctDeuda = if (totalVendido > 0) (porCobrarUsd / totalVendido) * 100 else 0.0
        
        val productoFuerte = topProductoNombre ?: "tus productos"

        // =====================================
        // REGLA 1: SANGRADO POR LOGÍSTICA (PASAJES > 12%)
        // =====================================
        if (pctPasajes > 12.0) {
            val consejosLogistica = listOf(
                "⚠️ Los envíos y viajes te comen el ${String.format(java.util.Locale.US, "%.1f", pctPasajes)}% del dinero. Estrategia: Agrupa tus entregas un solo día a la semana o cobra un 'Delivery Tarifa Plana' a tus clientes.",
                "⚠️ Gastas mucho en logística. Estrategia: Junta varios pedidos de clientes y manda a imprimir un Metro Lineal de DTF completo de una vez. Bajarás el costo de pasajes y de impresión drásticamente.",
                "⚠️ Fuga de dinero en transporte detectada. Considera sumar siempre un porcentaje fijo por concepto de 'movilización' en tus próximas cotizaciones de $productoFuerte."
            )
            alertas.add(AlertaFinanciera("Fuga por Logística y Envíos", consejosLogistica.random(), "WARNING"))
        }

        // =====================================
        // REGLA 2: MÁRGENES DE GANANCIA (RENTABILIDAD)
        // =====================================
        if (ingresosCobradosUsd > 0) {
            if (margenGanancia < 35.0) { // Peligro: Ganando muy poco
                val consejosBajoMargen = listOf(
                    "🔴 Tu margen actual es del ${String.format(java.util.Locale.US, "%.1f", margenGanancia)}%. Estás regalando tu trabajo. Estrategia: No subas el precio de golpe; empieza a crear Combos (Ej: $productoFuerte + Llavero) donde el llavero tiene alto margen.",
                    "🔴 Estás compitiendo solo por precio (${String.format(java.util.Locale.US, "%.1f", margenGanancia)}% libre). Estrategia: Incluye un empaque llamativo (que cuesta $0.30) y súbele $2.00 al precio final. Vende 'Regalos Listos', no productos crudos.",
                    "🔴 Trabajando mucho para ganar poco. Tu ganancia libre es inferior al 35%. Revisa el Cotizador urgente y asegúrate de estar cobrando el porcentaje de desgaste (luz/plancha)."
                )
                alertas.add(AlertaFinanciera("Rentabilidad Crítica", consejosBajoMargen.random(), "DANGER"))
            } else if (margenGanancia in 35.0..50.0) { // Regular: Puede mejorar
                val consejosMargenMedio = listOf(
                    "🟡 Margen de ganancia estable (${String.format(java.util.Locale.US, "%.1f", margenGanancia)}%), pero mejorable. Estrategia: Si $productoFuerte se vende bien, intenta comprar los insumos al mayor para la próxima semana y saltarás al 50% de ganancia libre.",
                    "🟡 Rentabilidad moderada. Sugerencia: Prueba ofrecer personalizaciones 'Premium' (con nombres dorados o empaques de regalo) en $productoFuerte para aumentar tu ganancia sin trabajar doble."
                )
                alertas.add(AlertaFinanciera("Margen Estable pero Mejorable", consejosMargenMedio.random(), "INFO"))
            } else { // Excelente: Negocio Sano
                val consejosBuenMargen = listOf(
                    "🟢 ¡Tus finanzas están perfectas! Margen libre del ${String.format(java.util.Locale.US, "%.1f", margenGanancia)}%. Estrategia: Separa el 5% de tus ganancias de este mes para pagar publicidad. Tienes capacidad de escalar.",
                    "🟢 Negocio Sano. Aprovecha este buen flujo de caja ($${String.format(java.util.Locale.US, "%.2f", gananciaNetaUsd)} libres) para armar inventario de $productoFuerte antes de temporada alta.",
                    "🟢 ¡Excelente estructura de costos! Tienes buena rentabilidad. Mantén la disciplina de separar el fondo de reposición cada vez que recibas un abono."
                )
                alertas.add(AlertaFinanciera("¡Rentabilidad Excelente!", consejosBuenMargen.random(), "SUCCESS"))
            }
        }

        // =====================================
        // REGLA 3: FLUJO DE CAJA (CUENTAS POR COBRAR) Y RIESGO DE CLIENTES
        // =====================================
        if (clienteMasDeudorNombre != null && clienteMasDeudorMonto > 20.0) {
            val consejosRiesgo = listOf(
                "🚨 Alerta de Crédito: '$clienteMasDeudorNombre' concentra el mayor riesgo de deuda en tu negocio ($${String.format(java.util.Locale.US, "%.2f", clienteMasDeudorMonto)}). Estrategia: Evita tomarle nuevos pedidos a crédito hasta que no liquide. Mándale un WhatsApp de cobranza hoy.",
                "🚨 Dinero Estancado. $clienteMasDeudorNombre te debe $${String.format(java.util.Locale.US, "%.2f", clienteMasDeudorMonto)}. Tu capital de trabajo se está quedando atrapado. Ofrece un pequeño descuento si paga el total hoy."
            )
            alertas.add(AlertaFinanciera("Riesgo de Cartera (Cliente)", consejosRiesgo.random(), "DANGER"))
        } else if (porCobrarUsd > 15.0 && pctDeuda > 30.0) {
            val consejosCobranza = listOf(
                "🟡 Tienes $${String.format(java.util.Locale.US, "%.2f", porCobrarUsd)} en la calle (${String.format(java.util.Locale.US, "%.1f", pctDeuda)}% de tus ventas). Estrategia: No inicies trabajos sin un 60% de abono. Tu capital se está quedando atrapado.",
                "🟡 Mucho saldo pendiente global. Estrategia: Envía un WhatsApp a los deudores diciendo: 'Hola, estamos limpiando inventario. Si liquidas tu saldo hoy, te llevas un obsequio sorpresa'. Recupera efectivo rápido."
            )
            alertas.add(AlertaFinanciera("Dinero Estancado (Global)", consejosCobranza.random(), "WARNING"))
        }

        // =====================================
        // REGLA 4: CLIENTE VIP / FIDELIZACIÓN
        // =====================================
        if (clienteVipNombre != null && clienteVipMonto > 50.0) {
            val consejosVip = listOf(
                "🏆 Fidelización: '$clienteVipNombre' es tu cliente VIP del período (Abonó $${String.format(java.util.Locale.US, "%.2f", clienteVipMonto)}). Estrategia: Escríbele un mensaje de agradecimiento y ofrécele un 10% de descuento en su próxima compra para asegurar su lealtad.",
                "🏆 Retención de Clientes. $clienteVipNombre te generó el mayor ingreso este mes. Considéralo para regalos corporativos de tu marca en navidad. Los buenos clientes hay que cuidarlos."
            )
            alertas.add(AlertaFinanciera("Oportunidad de Fidelización (VIP)", consejosVip.random(), "SUCCESS"))
        }

        return alertas
    }
}
