package com.example.gestor_deudores.data.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
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
}
