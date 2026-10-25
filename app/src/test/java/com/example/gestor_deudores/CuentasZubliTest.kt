package com.example.gestor_deudores

import com.example.gestor_deudores.data.database.CuentaBancaria
import com.example.gestor_deudores.data.database.MovimientoCuenta
import org.junit.Assert.assertEquals
import org.junit.Test

class CuentasZubliTest {

    private fun Double.redondear2(): Double {
        return java.math.BigDecimal.valueOf(this)
            .setScale(2, java.math.RoundingMode.HALF_UP)
            .toDouble()
    }

    @Test
    fun testCalculoSaldoCuenta_Ejemplo1() {
        val saldoInicial = 10.0
        val movimientos = listOf(
            MovimientoCuenta(cuentaId = 1, tipo = "ABONO", monto = 5.0),
            MovimientoCuenta(cuentaId = 1, tipo = "GASTO", monto = -2.0)
        )
        val saldoCalculado = saldoInicial + movimientos.sumOf { it.monto }
        assertEquals(13.0, saldoCalculado.redondear2(), 0.001)
    }

    @Test
    fun testCalculoSaldoCuenta_Ejemplo2() {
        val saldoInicial = 0.0
        val movimientos = listOf(
            MovimientoCuenta(cuentaId = 1, tipo = "ABONO", monto = 20.00),
            MovimientoCuenta(cuentaId = 1, tipo = "GASTO", monto = -5.23)
        )
        val saldoCalculado = saldoInicial + movimientos.sumOf { it.monto }
        assertEquals(14.77, saldoCalculado.redondear2(), 0.001)
    }

    @Test
    fun testConciliacionPeriodo() {
        val saldoInicialPeriodo = 0.0
        val entradasPeriodo = 20.00
        val salidasPeriodo = 5.23
        val saldoFinalPeriodo = (saldoInicialPeriodo + entradasPeriodo - salidasPeriodo).redondear2()
        assertEquals(14.77, saldoFinalPeriodo, 0.001)
    }

    @Test
    fun testSaldoCorridoDespuesDeCadaMovimiento() {
        val saldoInicial = 10.0
        val movs = listOf(
            MovimientoCuenta(id = 1, cuentaId = 1, tipo = "ABONO", monto = 5.0, fechaMillis = 1000L),
            MovimientoCuenta(id = 2, cuentaId = 1, tipo = "GASTO", monto = -2.0, fechaMillis = 2000L)
        )

        var saldoAcumulado = saldoInicial
        val saldosCorridos = movs.map { mov ->
            saldoAcumulado = (saldoAcumulado + mov.monto).redondear2()
            mov.id to saldoAcumulado
        }.toMap()

        assertEquals(15.0, saldosCorridos[1]!!, 0.001)
        assertEquals(13.0, saldosCorridos[2]!!, 0.001)
    }

    @Test
    fun testTransferenciaMismaMonedaUsd() {
        val cuentaA = CuentaBancaria(id = 1, nombre = "Binance", moneda = "USD", saldoActual = 100.0)
        val cuentaB = CuentaBancaria(id = 2, nombre = "Zelle", moneda = "USD", saldoActual = 50.0)
        val montoTransferir = 3.0

        val nuevoSaldoA = (cuentaA.saldoActual - montoTransferir).redondear2()
        val nuevoSaldoB = (cuentaB.saldoActual + montoTransferir).redondear2()
        val totalAntes = cuentaA.saldoActual + cuentaB.saldoActual
        val totalDespues = nuevoSaldoA + nuevoSaldoB

        assertEquals(97.0, nuevoSaldoA, 0.001)
        assertEquals(53.0, nuevoSaldoB, 0.001)
        assertEquals(totalAntes, totalDespues, 0.001)
    }

    @Test
    fun testTransferenciaDistintaMonedaUsdAves() {
        val tasaBcv = 875.65
        val montoUsd = 10.0
        val montoBs = (montoUsd * tasaBcv).redondear2()

        assertEquals(8756.50, montoBs, 0.001)
    }

    @Test
    fun testTotalEnCuentasUsdConConversion() {
        val tasaBcv = 875.65
        val saldoUsd1 = 14.77
        val saldoUsd2 = 4.23
        val saldoBs = 1000.0

        val equivalenciaBsAUsd = (saldoBs / tasaBcv).redondear2()
        val totalCuentasUsd = (saldoUsd1 + saldoUsd2 + equivalenciaBsAUsd).redondear2()

        assertEquals(1.14, equivalenciaBsAUsd, 0.01)
        assertEquals(20.14, totalCuentasUsd, 0.05)
    }

    @Test
    fun testAjusteDeSaldoGeneraMovimientoCorrecto() {
        val saldoActualCalculado = 15.0
        val nuevoSaldoReal = 12.0
        val diferenciaAjuste = (nuevoSaldoReal - saldoActualCalculado).redondear2()

        assertEquals(-3.0, diferenciaAjuste, 0.001)
        val nuevoSaldoConMov = (saldoActualCalculado + diferenciaAjuste).redondear2()
        assertEquals(nuevoSaldoReal, nuevoSaldoConMov, 0.001)
    }
}
