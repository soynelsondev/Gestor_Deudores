package com.example.gestor_deudores.data.utils

import com.example.gestor_deudores.data.database.Deuda
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class EstadoCobro(
    val saldo: Double,
    val proximaCuotaMonto: Double,
    val proximoVencimientoMillis: Long?,
    val vencida: Boolean,
    val diasAtraso: Long
)

fun Double.redondear2(): Double =
    BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()

fun obtenerFechaMillisValida(deuda: Deuda): Long {
    if (deuda.fechaMillis > 0L) {
        return deuda.fechaMillis
    }

    if (deuda.fecha.isNotBlank()) {
        val formatos = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
        )

        for (formato in formatos) {
            try {
                val parsed = formato.parse(deuda.fecha)
                if (parsed != null && parsed.time > 0L) {
                    return parsed.time
                }
            } catch (e: Exception) {
                // Continuar
            }
        }
    }

    return System.currentTimeMillis()
}

fun calcularEstadoCobro(deudas: List<Deuda>): EstadoCobro {
    val abonos = deudas.filter { it.tipo == "ABONO" }
    val cargos = deudas.filter { it.tipo == "CARGO" }.sortedBy { obtenerFechaMillisValida(it) }

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

            val fechaCargoValida = obtenerFechaMillisValida(cargo)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = fechaCargoValida
                set(Calendar.HOUR_OF_DAY, 12)
            }

            if (cargo.frecuencia.uppercase() == "AL_ENTREGAR") {
                proximoVencimiento = fechaCargoValida
            } else {
                for (i in 1..cuotaActual) {
                    when (cargo.frecuencia.uppercase()) {
                        "SEMANAL" -> cal.add(Calendar.DAY_OF_YEAR, 7)
                        "QUINCENAL" -> cal.add(Calendar.DAY_OF_YEAR, 15)
                        "MENSUAL" -> cal.add(Calendar.MONTH, 1)
                        else -> cal.add(Calendar.MONTH, 1)
                    }
                }
                proximoVencimiento = cal.timeInMillis
            }

            val calHoy = Calendar.getInstance().apply {
                timeInMillis = hoyMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calVenc = Calendar.getInstance().apply {
                timeInMillis = proximoVencimiento!!
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

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

data class DesgloseSobresPedido(
    val sobreInsumosMateriales: Double,
    val sobreLogisticaEnvio: Double,
    val sobreGananciaNeta: Double
)

fun desglosarSobresPedido(
    abonoTotalRecibidoUsd: Double,
    costoMateriaPrimaTotalUsd: Double,
    costoEnvioUsd: Double = 0.0
): DesgloseSobresPedido {
    if (abonoTotalRecibidoUsd <= 0.0) {
        return DesgloseSobresPedido(0.0, 0.0, 0.0)
    }

    val sobreInsumos = Math.min(abonoTotalRecibidoUsd, costoMateriaPrimaTotalUsd).redondear2()
    var remanente = abonoTotalRecibidoUsd - sobreInsumos

    val sobreEnvio = Math.min(remanente, costoEnvioUsd).redondear2()
    remanente -= sobreEnvio

    val sobreGanancia = Math.max(0.0, remanente).redondear2()

    return DesgloseSobresPedido(
        sobreInsumosMateriales = sobreInsumos,
        sobreLogisticaEnvio = sobreEnvio,
        sobreGananciaNeta = sobreGanancia
    )
}

// ==========================================
// SECCIÓN 11: PROYECCIÓN DE COBROS Y PAGOS
// ==========================================
data class ProyeccionCobrosPagos(
    val cobros7DiasUsd: Double,
    val cobros30DiasUsd: Double,
    val pagos7DiasUsd: Double,
    val pagos30DiasUsd: Double,
    val listaCobrosDetalle7Dias: List<Pair<String, Double>>,
    val listaCobrosDetalle30Dias: List<Pair<String, Double>>
)

fun calcularProyeccionCobros(
    deudas: List<Deuda>,
    deudores: List<com.example.gestor_deudores.data.database.Deudor>
): ProyeccionCobrosPagos {
    val hoyMillis = System.currentTimeMillis()
    val limite7Dias = hoyMillis + (7 * 24 * 60 * 60 * 1000L)
    val limite30Dias = hoyMillis + (30 * 24 * 60 * 60 * 1000L)

    var cobros7 = 0.0
    var cobros30 = 0.0
    val detalle7 = mutableListOf<Pair<String, Double>>()
    val detalle30 = mutableListOf<Pair<String, Double>>()

    val deudoresGroup = deudas.groupBy { it.idDeudor }
    for ((idDeudor, deudasCliente) in deudoresGroup) {
        val estadoCobro = calcularEstadoCobro(deudasCliente)
        val cliente = deudores.find { it.id == idDeudor }
        val nombreCliente = if (cliente != null) "${cliente.nombre} ${cliente.apellido}" else "Cliente #$idDeudor"

        if (estadoCobro.proximoVencimientoMillis != null && estadoCobro.proximaCuotaMonto > 0.0) {
            val vencMillis = estadoCobro.proximoVencimientoMillis
            if (vencMillis >= hoyMillis) {
                if (vencMillis <= limite7Dias) {
                    cobros7 += estadoCobro.proximaCuotaMonto
                    detalle7.add(nombreCliente to estadoCobro.proximaCuotaMonto)
                }
                if (vencMillis <= limite30Dias) {
                    cobros30 += estadoCobro.proximaCuotaMonto
                    detalle30.add(nombreCliente to estadoCobro.proximaCuotaMonto)
                }
            }
        }
    }

    return ProyeccionCobrosPagos(
        cobros7DiasUsd = cobros7.redondear2(),
        cobros30DiasUsd = cobros30.redondear2(),
        pagos7DiasUsd = 0.0,
        pagos30DiasUsd = 0.0,
        listaCobrosDetalle7Dias = detalle7,
        listaCobrosDetalle30Dias = detalle30
    )
}

// ==========================================
// SECCIÓN 12: ANTIGÜEDAD DE CARTERA Y PUNTUALIDAD
// ==========================================
data class GrupoAntiguedadCartera(
    val alDiaMontoUsd: Double,
    val alDiaClientesCount: Int,
    val atraso1a7MontoUsd: Double,
    val atraso1a7ClientesCount: Int,
    val atraso8a30MontoUsd: Double,
    val atraso8a30ClientesCount: Int,
    val atrasoMas30MontoUsd: Double,
    val atrasoMas30ClientesCount: Int,
    val porcentajePuntualidad: Double
)

fun calcularAntiguedadCartera(
    deudas: List<Deuda>,
    diasGracia: Int = 2
): GrupoAntiguedadCartera {
    var alDiaM = 0.0
    var alDiaC = 0
    var a1a7M = 0.0
    var a1a7C = 0
    var a8a30M = 0.0
    var a8a30C = 0
    var aMas30M = 0.0
    var aMas30C = 0

    var totalCuotasVencidas = 0
    var cuotasATiempo = 0

    val deudoresGroup = deudas.groupBy { it.idDeudor }
    for ((_, deudasCliente) in deudoresGroup) {
        val estadoCobro = calcularEstadoCobro(deudasCliente)
        if (estadoCobro.saldo <= 0.0) {
            alDiaC++
            continue
        }

        if (!estadoCobro.vencida || estadoCobro.diasAtraso <= 0) {
            alDiaM += estadoCobro.saldo
            alDiaC++
        } else {
            totalCuotasVencidas++
            val atrasoConGracia = (estadoCobro.diasAtraso - diasGracia).coerceAtLeast(0)

            if (atrasoConGracia <= 0) {
                cuotasATiempo++
                alDiaM += estadoCobro.saldo
                alDiaC++
            } else if (atrasoConGracia in 1..7) {
                a1a7M += estadoCobro.saldo
                a1a7C++
            } else if (atrasoConGracia in 8..30) {
                a8a30M += estadoCobro.saldo
                a8a30C++
            } else {
                aMas30M += estadoCobro.saldo
                aMas30C++
            }
        }
    }

    val pctPuntualidad = if (totalCuotasVencidas > 0) {
        (cuotasATiempo.toDouble() / totalCuotasVencidas * 100.0).redondear2()
    } else 100.0

    return GrupoAntiguedadCartera(
        alDiaMontoUsd = alDiaM.redondear2(),
        alDiaClientesCount = alDiaC,
        atraso1a7MontoUsd = a1a7M.redondear2(),
        atraso1a7ClientesCount = a1a7C,
        atraso8a30MontoUsd = a8a30M.redondear2(),
        atraso8a30ClientesCount = a8a30C,
        atrasoMas30MontoUsd = aMas30M.redondear2(),
        atrasoMas30ClientesCount = aMas30C,
        porcentajePuntualidad = pctPuntualidad
    )
}
