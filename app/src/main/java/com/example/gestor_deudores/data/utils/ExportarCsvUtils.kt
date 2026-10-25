package com.example.gestor_deudores.data.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.gestor_deudores.data.database.MovimientoCuenta
import com.example.gestor_deudores.ui.Resumen.ResumenUiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportarCsvUtils {

    fun exportarResumenCsv(
        context: Context,
        uiState: ResumenUiState,
        anonimizarClientes: Boolean = false
    ): Uri? {
        return try {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaGeneracionStr = formatoFecha.format(Date())

            val sb = StringBuilder()
            sb.append("REPORTE FINANCIERO Y CONTABLE - ZUBLI\n")
            sb.append("Fecha de Generación:,$fechaGeneracionStr\n")
            sb.append("Período Evaluado:,${uiState.periodoSeleccionado.name}\n")
            sb.append("Moneda:,${uiState.monedaVista}\n")
            sb.append("\n")

            sb.append("MÉTRICAS CLAVE (KPI)\n")
            sb.append("Cobrado USD,$${uiState.ingresosCobradosUsd}\n")
            sb.append("Ganancia Libre USD,$${uiState.gananciaLibreUsd}\n")
            sb.append("Gastos Taller USD,$${uiState.gastosTallerUsd}\n")
            sb.append("Ganancia Neta USD,$${uiState.gananciaNetaUsd}\n")
            sb.append("Por Cobrar USD,$${uiState.porCobrarUsd}\n")
            sb.append("Disponible para Retirar USD,$${uiState.disponibleParaRetirarUsd}\n")
            sb.append("\n")

            sb.append("DIVISIÓNDESOBRES (REPOSICIÓN)\n")
            uiState.fondosInsumos.forEach { fondo ->
                sb.append("${fondo.nombre},$${fondo.montoUsd},${fondo.porcentajeDelTotal}%\n")
            }
            sb.append("\n")

            sb.append("RADIOGRAFÍA DE ABONOS Y COBROS\n")
            sb.append("ID Deuda,Cliente,Monto USD,Fecha\n")
            uiState.listaAbonosDesglose.forEach { abono ->
                val nombreClienteStr = if (anonimizarClientes && abono.idDeuda != -1) {
                    "Cliente #${abono.idDeuda}"
                } else {
                    abono.clienteNombre.replace(",", " ")
                }
                sb.append("${abono.idDeuda},\"$nombreClienteStr\",$${abono.montoTotalAbonoUsd},${abono.fechaStr}\n")
            }

            val archivoCsv = File(context.cacheDir, "reporte_financiero_zubli.csv")
            FileOutputStream(archivoCsv).use { out ->
                out.write(sb.toString().toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivoCsv)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte Financiero Zubli")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Exportar Reporte CSV / Excel"))

            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportarExtractoCuentaCsv(
        context: Context,
        cuentaNombre: String,
        cuentaTipo: String,
        moneda: String,
        uso: String?,
        periodoTexto: String,
        saldoInicialPeriodo: Double,
        entradasPeriodo: Double,
        salidasPeriodo: Double,
        saldoFinalPeriodo: Double,
        movimientos: List<Pair<MovimientoCuenta, Double>>
    ): Uri? {
        return try {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaGeneracionStr = formatoFecha.format(Date())

            val sb = StringBuilder()
            sb.append("EXTRACTO DE CUENTA BANCARIA / BILLETERA - ZUBLI\n")
            sb.append("Cuenta:,\"$cuentaNombre\"\n")
            sb.append("Tipo / Moneda:,$cuentaTipo · $moneda\n")
            if (!uso.isNullOrBlank()) sb.append("Etiqueta de Uso:,\"$uso\"\n")
            sb.append("Período Evaluado:,$periodoTexto\n")
            sb.append("Fecha de Generación:,$fechaGeneracionStr\n")
            sb.append("\n")

            sb.append("CONCILIACIÓN Y SALDOS DEL PERÍODO\n")
            sb.append("Saldo al Inicio del Período,$moneda $saldoInicialPeriodo\n")
            sb.append("(+) Entradas del Período,+$entradasPeriodo\n")
            sb.append("(-) Salidas del Período,-$salidasPeriodo\n")
            sb.append("(=) Saldo al Final del Período,$moneda $saldoFinalPeriodo\n")
            sb.append("\n")

            sb.append("HISTORIAL DE MOVIMIENTOS Y SALDO CORRIDO\n")
            sb.append("ID,Fecha,Tipo,Concepto,Referencia,Monto $moneda,Saldo Corrido $moneda\n")

            val formatoMovFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            movimientos.forEach { (mov, saldoCorrido) ->
                val fechaStr = formatoMovFecha.format(Date(mov.fechaMillis))
                val conceptoLimpio = (mov.concepto.ifBlank { mov.nota }).replace(",", " ").replace("\"", "'")
                val refStr = (mov.referenciaPago ?: "").replace(",", " ")
                val signoStr = if (mov.monto >= 0) "+${mov.monto}" else "${mov.monto}"
                sb.append("${mov.id},$fechaStr,${mov.tipo},\"$conceptoLimpio\",\"$refStr\",$signoStr,$saldoCorrido\n")
            }

            val nombreArchivo = "extracto_${cuentaNombre.lowercase().replace(" ", "_")}.csv"
            val archivoCsv = File(context.cacheDir, nombreArchivo)
            FileOutputStream(archivoCsv).use { out ->
                out.write(sb.toString().toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivoCsv)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Extracto de Cuenta: $cuentaNombre - Zubli")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Exportar Extracto de Cuenta (CSV / Excel)"))

            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
