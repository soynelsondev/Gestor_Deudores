package com.example.gestor_deudores.data.repository

import android.content.Context
import com.example.gestor_deudores.data.api.Apitasas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasaBcvRepository(private val context: Context, private val api: Apitasas) {

    private val _tasaActual = MutableStateFlow(0.0)
    val tasaActual: StateFlow<Double> = _tasaActual.asStateFlow()

    suspend fun actualizarTasa() {
        val prefs = context.getSharedPreferences("TasasZubli", Context.MODE_PRIVATE)
        val tasaGuardada = prefs.getString("ultimoDolar", "0.0")?.toDoubleOrNull() ?: 0.0

        // Asignamos lo que hay en caché primero para que la app sea rápida
        if (tasaGuardada > 0.0) {
            _tasaActual.value = tasaGuardada
        }

        try {
            // Intentamos buscar la tasa fresca en internet
            val dolar = api.obtenerDolarOficial()
            if (dolar.promedio > 0.0 && dolar.promedio != _tasaActual.value) {
                _tasaActual.value = dolar.promedio

                // Guardamos en caché
                val formato = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                prefs.edit()
                    .putString("ultimoDolar", dolar.promedio.toString())
                    .putString("ultimaFecha", formato.format(Date()))
                    .apply()
            }
        } catch (e: Exception) {
            // No pasa nada si falla el internet, seguimos usando la de caché
        }
    }
}