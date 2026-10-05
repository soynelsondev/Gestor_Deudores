package com.example.gestor_deudores.data.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.gestor_deudores.ui.Resumen.ResumenUiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generarYCompartirPdf(context: Context, uiState: ResumenUiState) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Tamaño A4
    var page = document.startPage(pageInfo)
    var canvas = page.canvas

    val paint = Paint()
    var yPos = 50f
    val margin = 40f
    val lineSpacing = 20f

    fun nuevaPagina() {
        document.finishPage(page)
        page = document.startPage(pageInfo)
        canvas = page.canvas
        yPos = 50f
    }

    fun checkY(increment: Float) {
        if (yPos + increment > 800f) {
            nuevaPagina()
        }
    }

    // =====================================
    // ENCABEZADO EJECUTIVO
    // =====================================
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 22f
    paint.color = Color.rgb(0, 109, 119) // Color "estados"
    canvas.drawText("ZUBLI - REPORTE FINANCIERO Y AUDITORÍA", margin, yPos, paint)
    yPos += 30f

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textSize = 12f
    paint.color = Color.DKGRAY
    
    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    val fechaImpresion = sdf.format(Date())
    val periodoText = uiState.periodoSeleccionado.name.replace("_", " ")
    
    canvas.drawText("Período Evaluado: $periodoText", margin, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Fecha de Generación: $fechaImpresion", margin, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Tasa de Cambio Base (BCV): Bs ${String.format(Locale.US, "%.2f", uiState.tasaBcv)}", margin, yPos, paint)
    yPos += 40f

    // =====================================
    // SECCIÓN 1: MÉTRICAS GLOBALES
    // =====================================
    checkY(100f)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 14f
    paint.color = Color.BLACK
    canvas.drawText("1. TABLERO DE MANDO (GLOBAL)", margin, yPos, paint)
    yPos += 5f
    paint.strokeWidth = 1f
    canvas.drawLine(margin, yPos, 555f, yPos, paint)
    yPos += 25f

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textSize = 12f
    canvas.drawText("• Ingresos Cobrados (Dinero Real): $${String.format(Locale.US, "%.2f", uiState.ingresosCobradosUsd)}", margin, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("• Cuentas por Cobrar (En la calle): $${String.format(Locale.US, "%.2f", uiState.porCobrarUsd)}", margin, yPos, paint)
    yPos += lineSpacing
    val rentabilidad = if (uiState.ingresosCobradosUsd > 0) (uiState.gananciaNetaUsd / uiState.ingresosCobradosUsd) * 100 else 0.0
    canvas.drawText("• Margen de Rentabilidad Neta: ${String.format(Locale.US, "%.1f", rentabilidad)}%", margin, yPos, paint)
    yPos += 40f

    // =====================================
    // SECCIÓN 2: DISTRIBUCIÓN DE FONDOS
    // =====================================
    checkY(150f)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 14f
    canvas.drawText("2. SEPARACIÓN DE FONDOS (ASIGNACIÓN DE CAPITAL)", margin, yPos, paint)
    yPos += 5f
    canvas.drawLine(margin, yPos, 555f, yPos, paint)
    yPos += 20f

    // Cabecera de Tabla
    val col1 = margin
    val col2 = 300f
    val col3 = 450f
    
    paint.color = Color.rgb(220, 220, 220)
    canvas.drawRect(col1, yPos - 12f, 555f, yPos + 6f, paint)
    paint.color = Color.BLACK
    paint.textSize = 11f
    canvas.drawText("Cuenta / Destino", col1 + 5f, yPos, paint)
    canvas.drawText("Monto Asignado", col2, yPos, paint)
    canvas.drawText("% de Ingresos", col3, yPos, paint)
    yPos += lineSpacing + 5f

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    uiState.fondosInsumos.forEachIndexed { index, fondo ->
        checkY(lineSpacing)
        if (index % 2 != 0) {
            paint.color = Color.rgb(245, 245, 245)
            canvas.drawRect(col1, yPos - 12f, 555f, yPos + 6f, paint)
        }
        paint.color = Color.BLACK
        canvas.drawText(fondo.nombre, col1 + 5f, yPos, paint)
        canvas.drawText("$${String.format(Locale.US, "%.2f", fondo.montoUsd)}", col2, yPos, paint)
        canvas.drawText("${String.format(Locale.US, "%.1f", fondo.porcentajeDelTotal)}%", col3, yPos, paint)
        yPos += lineSpacing
    }
    yPos += 20f

    // =====================================
    // SECCIÓN 3: PRODUCTIVIDAD Y PEDIDOS
    // =====================================
    checkY(150f)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 14f
    canvas.drawText("3. DESEMPEÑO COMERCIAL Y CATÁLOGO", margin, yPos, paint)
    yPos += 5f
    canvas.drawLine(margin, yPos, 555f, yPos, paint)
    yPos += 25f
    
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textSize = 12f
    canvas.drawText("Estado del Embudo de Pedidos:", margin, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("  Recibidos: ${uiState.pedidosRecibidosCount} | En Producción: ${uiState.pedidosProduccionCount} | Listos: ${uiState.pedidosListosCount} | Entregados: ${uiState.pedidosEntregadosCount}", margin + 10f, yPos, paint)
    yPos += lineSpacing * 2
    
    canvas.drawText("Ranking de Productos Más Vendidos:", margin, yPos, paint)
    yPos += lineSpacing
    if (uiState.topProductos.isEmpty()) {
        canvas.drawText("  (Sin datos de productos en este período)", margin + 10f, yPos, paint)
        yPos += lineSpacing
    } else {
        uiState.topProductos.forEachIndexed { index, pair ->
            checkY(lineSpacing)
            canvas.drawText("  ${index + 1}. ${pair.first}: ${pair.second} unidades", margin + 10f, yPos, paint)
            yPos += lineSpacing
        }
    }
    yPos += 30f

    // =====================================
    // SECCIÓN 4: AUDITORÍA TRANSACCIONAL DE CLIENTES (TABLA)
    // =====================================
    checkY(100f)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 14f
    canvas.drawText("4. AUDITORÍA TRANSACCIONAL DE CLIENTES", margin, yPos, paint)
    yPos += 5f
    canvas.drawLine(margin, yPos, 555f, yPos, paint)
    yPos += 20f

    val colA = margin
    val colB = 120f
    val colC = 380f
    
    // Encabezado de la tabla de clientes
    paint.color = Color.rgb(220, 220, 220)
    canvas.drawRect(colA, yPos - 12f, 555f, yPos + 6f, paint)
    paint.color = Color.BLACK
    paint.textSize = 11f
    canvas.drawText("Fecha", colA + 5f, yPos, paint)
    canvas.drawText("Cliente", colB, yPos, paint)
    canvas.drawText("Abonado", colC, yPos, paint)
    yPos += lineSpacing + 5f

    // Filtramos para quitar el "Consolidado Global" y dejar solo clientes reales
    val abonosIndividuales = uiState.listaAbonosDesglose.filter { it.idDeuda != -1 }
    if (abonosIndividuales.isEmpty()) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        canvas.drawText("Sin abonos individuales recientes registrados.", margin, yPos, paint)
        yPos += lineSpacing
    } else {
        abonosIndividuales.forEachIndexed { index, abono ->
            checkY(30f) // Prevemos espacio para el desglose interno
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 11f
            
            if (index % 2 != 0) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(colA, yPos - 12f, 555f, yPos + 6f, paint)
            }
            
            paint.color = Color.BLACK
            // Acortamos la fecha para que quepa bien
            val fechaCorta = if(abono.fechaStr.length > 10) abono.fechaStr.substring(0, 10) else abono.fechaStr
            canvas.drawText(fechaCorta, colA + 5f, yPos, paint)
            
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(abono.clienteNombre, colB, yPos, paint)
            
            paint.color = Color.rgb(46, 125, 50) // Verde oscuro para el dinero
            canvas.drawText("$${String.format(Locale.US, "%.2f", abono.montoTotalAbonoUsd)}", colC, yPos, paint)
            yPos += lineSpacing
        }
    }
    yPos += 20f

    // =====================================
    // SECCIÓN 5: ASESOR ZUBLI Y ANÁLISIS DE CLIENTES
    // =====================================
    checkY(150f)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 14f
    paint.color = Color.rgb(0, 109, 119)
    canvas.drawText("5. PRE-DIAGNÓSTICO (ASESOR ZUBLI)", margin, yPos, paint)
    yPos += 5f
    canvas.drawLine(margin, yPos, 555f, yPos, paint)
    yPos += 25f

    paint.color = Color.BLACK
    if (uiState.alertasFinancieras.isEmpty()) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        canvas.drawText("No hay alertas de rentabilidad disponibles para este período.", margin, yPos, paint)
        yPos += lineSpacing
    } else {
        uiState.alertasFinancieras.forEach { alerta ->
            checkY(60f)
            
            val colorBorde = when (alerta.nivel) {
                "DANGER" -> Color.rgb(211, 47, 47) // Rojo oscuro
                "WARNING" -> Color.rgb(245, 124, 0) // Naranja
                "SUCCESS" -> Color.rgb(56, 142, 60) // Verde
                else -> Color.rgb(25, 118, 210) // Azul
            }
            
            // Dibujamos un rectángulo tipo tarjeta
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = colorBorde
            canvas.drawRoundRect(margin, yPos - 15f, 555f, yPos + 40f, 8f, 8f, paint)
            
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            paint.color = colorBorde
            canvas.drawText(">> ${alerta.titulo}", margin + 10f, yPos, paint)
            yPos += 15f
            
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = Color.DKGRAY
            
            // Formateador simple de saltos de línea para el PDF
            val words = alerta.mensaje.split(" ")
            var line = ""
            for (word in words) {
                if (paint.measureText("$line $word") < (555f - margin - 30f)) {
                    line += "$word "
                } else {
                    canvas.drawText(line, margin + 10f, yPos, paint)
                    yPos += 15f
                    checkY(20f)
                    line = "$word "
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, margin + 10f, yPos, paint)
                yPos += 30f
            }
        }
    }

    document.finishPage(page)

    // =====================================
    // GUARDAR Y COMPARTIR
    // =====================================
    try {
        val cachePath = File(context.cacheDir, "reportes_pdf")
        cachePath.mkdirs()
        val file = File(cachePath, "Zubli_Reporte_Financiero.pdf")
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.close()

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Zubli - Reporte Financiero")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte PDF"))

    } catch (e: Exception) {
        e.printStackTrace()
        document.close()
    }
}