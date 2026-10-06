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

    if (telefonoLimpio == null) {
        Toast.makeText(context, "El número de teléfono no es válido para WhatsApp", Toast.LENGTH_LONG).show()
        return
    }

    val url = "https://wa.me/$telefonoLimpio?text=${Uri.encode(mensaje)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "WhatsApp no está instalado en este dispositivo", Toast.LENGTH_LONG).show()
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
