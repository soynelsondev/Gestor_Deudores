package com.example.gestor_deudores.data

import java.util.Calendar
import java.math.BigDecimal
import java.math.RoundingMode

data class EstadoCobro(
    val saldo: Double,
    val proximaCuotaMonto: Double,
    val proximoVencimientoMillis: Long?,
    val vencida: Boolean,
    val diasAtraso: Long
)

fun Double.redondear2(): Double =
    BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()

fun calcularEstadoCobro(deudas: List<Deuda>): EstadoCobro {
    val abonos = deudas.filter { it.tipo == "ABONO" }
    val cargos = deudas.filter { it.tipo == "CARGO" }.sortedBy { it.fechaMillis }
    
    val totalAbonos = abonos.sumOf { Math.abs(it.montoRestante) }.redondear2()
    var abonoRestante = totalAbonos
    var proximaCuotaMonto = 0.0
    var proximoVencimiento: Long? = null
    var vencida = false
    var diasAtraso = 0L
    var saldoTotal = 0.0
    val hoyMillis = System.currentTimeMillis()

    for (cargo in cargos) {
        val montoCargo = cargo.montoInicial.redondear2()
        saldoTotal += montoCargo
        
        if (abonoRestante >= montoCargo) {
            abonoRestante = (abonoRestante - montoCargo).redondear2()
            continue
        }
        
        val saldoCargo = (montoCargo - abonoRestante).redondear2()
        abonoRestante = 0.0
        
        val numCuotas = if (cargo.numCuotas > 0) cargo.numCuotas else 1
        val montoCuota = (montoCargo / numCuotas).redondear2()
        
        val cuotasCubiertas = if (montoCuota > 0) Math.floor((montoCargo - saldoCargo) / montoCuota).toInt() else 0
        val cuotaActual = cuotasCubiertas + 1
        
        if (cuotaActual <= numCuotas && proximoVencimiento == null) {
            proximaCuotaMonto = if (cuotaActual == numCuotas) saldoCargo else montoCuota
            
            // ¡EL ARREGLO! Convertimos la fecha base (que estaba en texto en la UI) a UTC para no perder días.
            // O mejor aún, como fechaMillis ya viene del calendario, solo sumamos los días exactos sin importar la hora local.
            val cal = Calendar.getInstance().apply { 
                timeInMillis = cargo.fechaMillis 
                // Reseteamos horas para cálculo puro
                set(Calendar.HOUR_OF_DAY, 12) 
            }
            
            for (i in 1..cuotaActual) {
                when (cargo.frecuencia.uppercase()) {
                    "SEMANAL" -> cal.add(Calendar.DAY_OF_YEAR, 7)
                    "QUINCENAL" -> cal.add(Calendar.DAY_OF_YEAR, 15)
                    "MENSUAL" -> cal.add(Calendar.MONTH, 1)
                    else -> cal.add(Calendar.MONTH, 1)
                }
            }
            proximoVencimiento = cal.timeInMillis
            
            val calHoy = Calendar.getInstance().apply { timeInMillis = hoyMillis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            val calVenc = Calendar.getInstance().apply { timeInMillis = proximoVencimiento!!; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            
            if (calVenc.timeInMillis < calHoy.timeInMillis) {
                vencida = true
                diasAtraso = (calHoy.timeInMillis - calVenc.timeInMillis) / (1000 * 60 * 60 * 24)
            }
        }
    }
    
    saldoTotal = (saldoTotal - totalAbonos).redondear2()
    if (saldoTotal < 0) saldoTotal = 0.0
    
    return EstadoCobro(saldoTotal, proximaCuotaMonto, proximoVencimiento, vencida, diasAtraso)
}