package com.example.gestor_deudores.data.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun normalizarTelefono(raw: String): String? {
    val d = raw.filter { it.isDigit() }
    if (d.isEmpty()) return null

    return when {
        d.startsWith("0") && d.length == 11 -> "58" + d.drop(1)
        d.length == 10 -> "58$d"
        d.length in 11..15 -> d
        else -> null
    }
}

fun abrirWhatsApp(context: Context, telefonoRaw: String, mensaje: String) {
    val telefonoLimpio = normalizarTelefono(telefonoRaw)

    // 1. CONDICIONAL: Si HAY número de teléfono registrado válido -> Abre chat directo
    if (telefonoLimpio != null && telefonoLimpio.isNotBlank()) {
        val url = "https://wa.me/$telefonoLimpio?text=${Uri.encode(mensaje)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
            return
        } catch (e: ActivityNotFoundException) {
            // Si falla abrir el link directo, continua al selector general
        }
    }

    // 2. CONDICIONAL: Si NO HAY número registrado (o está vacío/inválido) -> Abre el selector general de contactos de WhatsApp
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensaje)
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(Intent.createChooser(sendIntent, "Enviar mensaje por WhatsApp"))
    } catch (e: Exception) {
        // Fallback genérico a cualquier app de mensajería si no tiene WhatsApp instalado
        val generalIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        try {
            context.startActivity(Intent.createChooser(generalIntent, "Compartir resumen"))
        } catch (e2: Exception) {
            Toast.makeText(context, "No se encontró una aplicación de mensajería", Toast.LENGTH_LONG).show()
        }
    }
}

fun armarMensajeCobro(
    perfil: com.example.gestor_deudores.data.database.PerfilNegocio,
    clienteNombre: String,
    montoUsd: Double,
    montoBsText: String,
    cuotaUsd: Double,
    cuotaBsText: String,
    fechaVencimiento: String
): String {
    val saludo = perfil.saludoCobro.ifBlank { "¡Hola! Te escribimos con mucho gusto de parte de nuestro taller." }
    val cierre = perfil.cierreCobro.ifBlank { "Por favor indícanos cuándo podrías realizar el pago. ¡Muchas gracias!" }

    val strMontoUsd = String.format(java.util.Locale.US, "%.2f", montoUsd)
    val strCuotaUsd = String.format(java.util.Locale.US, "%.2f", cuotaUsd)

    return """
    |$saludo
    |
    |📋 *RESUMEN DE CUENTA*
    |👤 *Cliente:* $clienteNombre
    |💰 *Saldo Pendiente:* $$strMontoUsd USD$montoBsText
    |📅 *Próxima Cuota:* $$strCuotaUsd USD$cuotaBsText (Vence: $fechaVencimiento)
    |
    |$cierre
    """.trimMargin()
}

fun armarMensajePedidoListo(
    perfil: com.example.gestor_deudores.data.database.PerfilNegocio,
    clienteNombre: String,
    productoNombre: String,
    montoUsd: Double,
    montoBsText: String
): String {
    val saludo = perfil.saludoListo.ifBlank { "¡Hola! Te tenemos excelentes noticias de parte de nuestro taller." }
    val cierre = perfil.cierreListo.ifBlank { "Puedes pasar retirando tu pedido en nuestro horario habitual. ¡Te esperamos!" }

    val strMontoUsd = String.format(java.util.Locale.US, "%.2f", montoUsd)
    val textoSaldo = if (montoUsd > 0) "\n💰 *Saldo a Cancelar:* $$strMontoUsd USD$montoBsText" else "\n✅ *Estado:* Cancelado en su totalidad"

    return """
    |$saludo
    |
    |🎉 *¡TU PEDIDO ESTÁ LISTO!*
    |👤 *Cliente:* $clienteNombre
    |📦 *Producto:* $productoNombre$textoSaldo
    |
    |$cierre
    """.trimMargin()
}

fun armarMensajeResumenPedidoCliente(
    clienteNombre: String,
    articulosResumenText: String,
    totalUsd: Double,
    tasaBcv: Double,
    abonoUsd: Double,
    saldoUsd: Double,
    frecuenciaPago: String,
    fechaEntregaStr: String
): String {
    val totalBsStr = String.format(java.util.Locale.US, "%.2f", totalUsd * tasaBcv)
    val totalUsdStr = String.format(java.util.Locale.US, "%.2f", totalUsd)
    val abonoUsdStr = String.format(java.util.Locale.US, "%.2f", abonoUsd)
    val saldoUsdStr = String.format(java.util.Locale.US, "%.2f", saldoUsd)

    val textoModoPago = when (frecuenciaPago) {
        "AL_ENTREGAR" -> "se paga al retirar / entregar"
        "SEMANAL" -> "pago en cuotas semanales"
        "QUINCENAL" -> "pago en cuotas quincenales"
        "MENSUAL" -> "pago en cuotas mensuales"
        else -> "se paga al retirar"
    }

    val textoAbono = if (abonoUsd > 0) "Abono recibido: $$abonoUsdStr" else "Abono recibido: $0.00"
    val textoSaldo = if (saldoUsd > 0) "Saldo pendiente: $$saldoUsdStr — $textoModoPago" else "Estado: ✅ Cancelado en su totalidad"

    return """
    |Hola *$clienteNombre*, tu pedido quedó registrado con éxito:
    |
    |$articulosResumenText
    |
    |*Total:* $$totalUsdStr (Bs $totalBsStr)
    |*$textoAbono*
    |*$textoSaldo*
    |*Fecha de entrega:* $fechaEntregaStr
    |
    |Si necesitas cambiar algo, por favor escríbenos a la brevedad. ¡Gracias por tu confianza!
    """.trimMargin()
}
