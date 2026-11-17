package com.example.gestor_deudores.data.utils

/**
 * REPARTO PROPORCIONAL EN CENTAVOS ENTEROS (LONG).
 * Reparte totalCentavos entre las posiciones según sus pesos (ponderaciones),
 * utilizando el método del mayor resto (Hamilton / Hare-Niemeyer) para garantizar
 * que la suma del desglose sea exactamente igual a totalCentavos sin perder ni inventar centavos.
 */
fun repartirProporcional(totalCentavos: Long, pesos: List<Long>): List<Long> {
    require(totalCentavos >= 0L) { "El total no puede ser negativo: $totalCentavos" }
    require(pesos.all { it >= 0L }) { "Los pesos no pueden ser negativos: $pesos" }

    if (pesos.isEmpty()) return emptyList()
    val sumaPesos = pesos.sum()
    if (sumaPesos == 0L || totalCentavos == 0L) return List(pesos.size) { 0L }

    val base = pesos.map { (it * totalCentavos) / sumaPesos }.toMutableList()
    var faltan = totalCentavos - base.sum()

    val porResiduo = pesos
        .mapIndexed { i, p -> i to ((p * totalCentavos) % sumaPesos) }
        .sortedByDescending { it.second }

    var k = 0
    while (faltan > 0L) {
        base[porResiduo[k % porResiduo.size].first] += 1L
        faltan--
        k++
    }
    return base
}
