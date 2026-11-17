package com.example.gestor_deudores.debug

import android.content.Context
import com.example.gestor_deudores.data.database.DeudaDataBase
import com.example.gestor_deudores.data.database.DistribucionAbono
import com.example.gestor_deudores.data.utils.obtenerFechaMillisValida
import com.example.gestor_deudores.data.utils.redondear2
import com.example.gestor_deudores.ui.Resumen.ConfiguracionCostos
import com.example.gestor_deudores.ui.Resumen.PeriodoFiltro
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Calendar

/**
 * HERRAMIENTA DE DEPURACIÓN (VARIANTE DEBUG)
 * Exporta en formato JSON el estado baseline completo del cálculo actual de la Radiografía del Resumen Financiero.
 * Incluye:
 * 1. Filas completas de la tabla distribucion_abonos.
 * 2. Totales calculados por ResumenViewModel para los 6 valores de PeriodoFiltro.
 * 3. Desglose detallado recalculado por abono (cliente, fecha, monto USD, pedido deducido, material, transporte, insumos, ganancia, origen).
 */
data class DataBaselineAbono(
    val idDeuda: Int,
    val clienteNombre: String,
    val fechaStr: String,
    val montoTotalAbonoUsd: Double,
    val pedidoIdDeducido: Int?,
    val materialUsd: Double,
    val transporteUsd: Double,
    val insumosUsd: Double,
    val gananciaUsd: Double,
    val origen: String
)

data class DataBaselineTotalesPeriodo(
    val periodoNombre: String,
    val ingresosCobradosUsd: Double,
    val asignadoMaterialUsd: Double,
    val asignadoTransporteUsd: Double,
    val asignadoInsumosUsd: Double,
    val gananciaLibreUsd: Double,
    val gastosTallerUsd: Double,
    val gananciaNetaUsd: Double,
    val porCobrarUsd: Double,
    val abonosCount: Int
)

data class RadiografiaBaselineExport(
    val timestampMillis: Long = System.currentTimeMillis(),
    val filasDistribucionAbono: List<DistribucionAbono>,
    val totalesPorPeriodo: Map<String, DataBaselineTotalesPeriodo>,
    val abonosDesgloseCalculados: List<DataBaselineAbono>
)

object RadiografiaDebugExporter {

    suspend fun exportarBaselineRadiografiaJson(
        context: Context,
        db: DeudaDataBase,
        config: ConfiguracionCostos = ConfiguracionCostos()
    ): String {
        val deudas = db.deudaDao().obtenerTodasLasDeudas().first()
        val deudores = db.deudorDao().obtenerDeudores().first()
        val pedidos = db.pedidoDao().obtenerPedidosConCliente().first()
        val distribuciones = db.distribucionAbonoDao().obtenerTodas().first()
        val gastos = db.gastoDao().obtenerTodosLosGastos().first()

        // 1. Filas directas de la tabla distribucion_abonos
        val filasDistribucion = distribuciones.sortedBy { it.id }

        // 2. Desglose recalculado por abono individual (todos los abonos históricos)
        val abonosHistoricos = deudas.filter { it.tipo == "ABONO" }
            .sortedByDescending { obtenerFechaMillisValida(it) }

        val cargosActivos = deudas.filter { it.tipo == "CARGO" && it.estado != "Cancelado" }

        val desgloseCalculadoList = mutableListOf<DataBaselineAbono>()

        for (abono in abonosHistoricos) {
            val cliente = deudores.find { it.id == abono.idDeudor }
            val clienteNombre = if (cliente != null) "${cliente.nombre} ${cliente.apellido}" else "Cliente ID ${abono.idDeudor}"
            val montoAbono = Math.abs(abono.montoRestante).redondear2()
            val distExistente = distribuciones.find { it.abonoId == abono.id }

            val mat: Double
            val trans: Double
            val ins: Double
            val gan: Double
            val origenStr: String
            val pedidoIdDeducido: Int?

            if (distExistente != null && distExistente.tipoAbono == "NO_AFECTA") {
                mat = distExistente.material
                trans = distExistente.transporte
                ins = distExistente.insumos
                gan = distExistente.ganancia
                origenStr = "NO_AFECTA"
                pedidoIdDeducido = distExistente.pedidoId
            } else {
                val cargoOriginal = cargosActivos.find { it.idDeudor == abono.idDeudor && it.tipoDeuda != "Abono / Pago Parcial" }
                val pedidoFotografia = pedidos.find { it.deudorId == abono.idDeudor && it.totalUsd == cargoOriginal?.montoInicial }
                pedidoIdDeducido = pedidoFotografia?.id

                if (pedidoFotografia != null && (pedidoFotografia.costoPiezaBaseUsd > 0.0 || pedidoFotografia.costoInsumosUsd > 0.0)) {
                    val prop = if (pedidoFotografia.totalUsd > 0.0) montoAbono / pedidoFotografia.totalUsd else 1.0
                    mat = (pedidoFotografia.costoPiezaBaseUsd * prop).redondear2()
                    trans = (pedidoFotografia.costoPasajeUsd * prop).redondear2()
                    ins = (pedidoFotografia.costoInsumosUsd * prop).redondear2()
                    gan = (montoAbono - (mat + trans + ins)).coerceAtLeast(0.0).redondear2()
                    origenStr = "EXACTO"
                } else {
                    mat = (montoAbono * (config.pctTextil / 100.0)).redondear2()
                    trans = (montoAbono * (config.pctPasaje / 100.0)).redondear2()
                    ins = (montoAbono * (config.pctInsumos / 100.0)).redondear2()
                    gan = (montoAbono - (mat + trans + ins)).coerceAtLeast(0.0).redondear2()
                    origenStr = "ESTIMADO"
                }
            }

            desgloseCalculadoList.add(
                DataBaselineAbono(
                    idDeuda = abono.id,
                    clienteNombre = clienteNombre,
                    fechaStr = abono.fecha,
                    montoTotalAbonoUsd = montoAbono,
                    pedidoIdDeducido = pedidoIdDeducido,
                    materialUsd = mat,
                    transporteUsd = trans,
                    insumosUsd = ins,
                    gananciaUsd = gan,
                    origen = origenStr
                )
            )
        }

        // 3. Totales calculados por cada uno de los 6 periodos de PeriodoFiltro
        val mapaTotalesPeriodos = mutableMapOf<String, DataBaselineTotalesPeriodo>()

        for (periodo in PeriodoFiltro.values()) {
            val (inicioMillis, finMillis) = calcularRangoMillis(periodo)

            val deudasEnPeriodo = deudas.filter { deuda ->
                val fechaValida = obtenerFechaMillisValida(deuda)
                fechaValida in inicioMillis..finMillis
            }

            val abonosEnPeriodo = deudasEnPeriodo.filter { it.tipo == "ABONO" }
            val ingresosCobradosUsd = abonosEnPeriodo.sumOf { Math.abs(it.montoRestante) }.redondear2()

            val totalCargosUsd = cargosActivos.sumOf { it.montoInicial }.redondear2()
            val totalAbonosHistoricosUsd = deudas.filter { it.tipo == "ABONO" }.sumOf { Math.abs(it.montoRestante) }.redondear2()
            val porCobrarUsd = (totalCargosUsd - totalAbonosHistoricosUsd).coerceAtLeast(0.0).redondear2()

            var asignadoMat = 0.0
            var asignadoTrans = 0.0
            var asignadoIns = 0.0
            var ganLibre = 0.0

            for (abono in abonosEnPeriodo) {
                val montoAbono = Math.abs(abono.montoRestante).redondear2()
                val distExistente = distribuciones.find { it.abonoId == abono.id }

                if (distExistente != null && distExistente.tipoAbono == "NO_AFECTA") {
                    asignadoMat += distExistente.material
                    asignadoTrans += distExistente.transporte
                    asignadoIns += distExistente.insumos
                    ganLibre += distExistente.ganancia
                } else {
                    val cargoOriginal = cargosActivos.find { it.idDeudor == abono.idDeudor && it.tipoDeuda != "Abono / Pago Parcial" }
                    val pedidoFotografia = pedidos.find { it.deudorId == abono.idDeudor && it.totalUsd == cargoOriginal?.montoInicial }

                    if (pedidoFotografia != null && (pedidoFotografia.costoPiezaBaseUsd > 0.0 || pedidoFotografia.costoInsumosUsd > 0.0)) {
                        val prop = if (pedidoFotografia.totalUsd > 0.0) montoAbono / pedidoFotografia.totalUsd else 1.0
                        val m = (pedidoFotografia.costoPiezaBaseUsd * prop).redondear2()
                        val t = (pedidoFotografia.costoPasajeUsd * prop).redondear2()
                        val i = (pedidoFotografia.costoInsumosUsd * prop).redondear2()
                        val g = (montoAbono - (m + t + i)).coerceAtLeast(0.0).redondear2()

                        asignadoMat += m
                        asignadoTrans += t
                        asignadoIns += i
                        ganLibre += g
                    } else {
                        val m = (montoAbono * (config.pctTextil / 100.0)).redondear2()
                        val t = (montoAbono * (config.pctPasaje / 100.0)).redondear2()
                        val i = (montoAbono * (config.pctInsumos / 100.0)).redondear2()
                        val g = (montoAbono - (m + t + i)).coerceAtLeast(0.0).redondear2()

                        asignadoMat += m
                        asignadoTrans += t
                        asignadoIns += i
                        ganLibre += g
                    }
                }
            }

            val gastosEnPeriodo = gastos.filter { it.fechaMillis in inicioMillis..finMillis }
            val gastosTallerUsd = gastosEnPeriodo.filter { it.categoriaTipo == "GASTO_TALLER" }.sumOf { it.montoUsd }.redondear2()
            val gananciaNetaUsd = (ganLibre - gastosTallerUsd).coerceAtLeast(0.0).redondear2()

            mapaTotalesPeriodos[periodo.name] = DataBaselineTotalesPeriodo(
                periodoNombre = periodo.name,
                ingresosCobradosUsd = ingresosCobradosUsd,
                asignadoMaterialUsd = asignadoMat.redondear2(),
                asignadoTransporteUsd = asignadoTrans.redondear2(),
                asignadoInsumosUsd = asignadoIns.redondear2(),
                gananciaLibreUsd = ganLibre.redondear2(),
                gastosTallerUsd = gastosTallerUsd,
                gananciaNetaUsd = gananciaNetaUsd,
                porCobrarUsd = porCobrarUsd,
                abonosCount = abonosEnPeriodo.size
            )
        }

        val exportContainer = RadiografiaBaselineExport(
            filasDistribucionAbono = filasDistribucion,
            totalesPorPeriodo = mapaTotalesPeriodos,
            abonosDesgloseCalculados = desgloseCalculadoList
        )

        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonOutput = gson.toJson(exportContainer)

        try {
            val fileDir = context.getExternalFilesDir(null) ?: context.filesDir
            val destFile = File(fileDir, "radiografia_baseline_export.json")
            destFile.writeText(jsonOutput)
        } catch (e: Exception) {
            // Manejo silencioso en caso de error I/O
        }

        return jsonOutput
    }

    private fun calcularRangoMillis(periodo: PeriodoFiltro): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val finMillis = System.currentTimeMillis()

        return when (periodo) {
            PeriodoFiltro.HOY -> {
                cal.timeInMillis = finMillis
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, finMillis)
            }
            PeriodoFiltro.ESTA_SEMANA -> {
                cal.timeInMillis = finMillis
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, finMillis)
            }
            PeriodoFiltro.ESTE_MES -> {
                cal.timeInMillis = finMillis
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, finMillis)
            }
            PeriodoFiltro.MES_ANTERIOR -> {
                cal.timeInMillis = finMillis
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val inicioAnterior = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                Pair(inicioAnterior, cal.timeInMillis)
            }
            PeriodoFiltro.ULTIMOS_3_MESES -> {
                cal.timeInMillis = finMillis
                cal.add(Calendar.MONTH, -3)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, finMillis)
            }
            PeriodoFiltro.TODO_EL_HISTORICO -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
