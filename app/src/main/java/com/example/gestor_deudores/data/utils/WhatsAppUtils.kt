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
