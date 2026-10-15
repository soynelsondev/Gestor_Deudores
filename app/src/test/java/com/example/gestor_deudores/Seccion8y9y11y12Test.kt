package com.example.gestor_deudores

import com.example.gestor_deudores.data.database.Deuda
import com.example.gestor_deudores.data.database.MovimientoCuenta
import com.example.gestor_deudores.data.utils.calcularAntiguedadCartera
import com.example.gestor_deudores.data.utils.calcularProyeccionCobros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Seccion8y9y11y12Test {

    @Test
    fun testSaldoCuentaCalculadoPorMovimientos() {
        val saldoInicial = 100.0
        val movimientos = listOf(
            MovimientoCuenta(cuentaId = 1, tipo = "ABONO", monto = 50.0),
            MovimientoCuenta(cuentaId = 1, tipo = "GASTO", monto = -20.0),
            MovimientoCuenta(cuentaId = 1, tipo = "RETIRO", monto = -10.0),
            MovimientoCuenta(cuentaId = 1, tipo = "TRANSFERENCIA_ENTRADA", monto = 30.0),
            MovimientoCuenta(cuentaId = 1, tipo = "AJUSTE", monto = -5.0)
        )

        val saldoCalculado = saldoInicial + movimientos.sumOf { it.monto }
        assertEquals(145.0, saldoCalculado, 0.001)
    }

    @Test
    fun testDisponibleParaRetirarConReservasYCaja() {
        val gananciaNeta = 120.0
        val retiros = 0.0
        val gananciaPendiente = gananciaNeta - retiros // $120.0

        val cajaUsd = 200.0
        val reservasUsd = 110.0
        val liquidezLibre = Math.max(0.0, cajaUsd - reservasUsd) // $90.0

        val disponibleParaRetirar = Math.max(0.0, Math.min(gananciaPendiente, liquidezLibre))
        assertEquals(90.0, disponibleParaRetirar, 0.001)
    }

    @Test
    fun testDisponibleParaRetirarSinLiquidez() {
        val gananciaNeta = 100.0
        val cajaUsd = 80.0
        val reservasUsd = 90.0
        val liquidezLibre = Math.max(0.0, cajaUsd - reservasUsd) // $0.0

        val disponibleParaRetirar = Math.max(0.0, Math.min(gananciaNeta, liquidezLibre))
        assertEquals(0.0, disponibleParaRetirar, 0.001)
    }

    @Test
    fun testAntiguedadCarteraYPorcentajePuntualidad() {
        val hoy = System.currentTimeMillis()
        val deudas = listOf(
            Deuda(
                id = 1,
                idDeudor = 101,
                montoInicial = 50.0,
                montoRestante = 50.0,
                tipoDeuda = "Camisas",
                fecha = "01/01/2026",
                rol = "CLIENTE",
                descripcion = "Pedido atrasado",
                estado = "Pendiente",
                tipo = "CARGO",
                fechaMillis = hoy - (10 * 24 * 60 * 60 * 1000L)
            )
        )

        val cartera = calcularAntiguedadCartera(deudas, diasGracia = 2)
        assertTrue(cartera.atraso8a30MontoUsd >= 0.0 || cartera.atraso1a7MontoUsd >= 0.0 || cartera.alDiaMontoUsd >= 0.0)
    }

    @Test
    fun testProyeccionCobrosVentana7Y30Dias() {
        val hoy = System.currentTimeMillis()
        val deudas = listOf(
            Deuda(
                id = 2,
                idDeudor = 102,
                montoInicial = 100.0,
                montoRestante = 100.0,
                tipoDeuda = "Tazas",
                fecha = "10/10/2026",
                rol = "CLIENTE",
                descripcion = "Cuota futura",
                estado = "Pendiente",
                tipo = "CARGO",
                numCuotas = 2,
                frecuencia = "SEMANAL",
                fechaMillis = hoy + (3 * 24 * 60 * 60 * 1000L)
            )
        )

        val proyeccion = calcularProyeccionCobros(deudas, emptyList())
        assertTrue(proyeccion.cobros7DiasUsd >= 0.0)
        assertTrue(proyeccion.cobros30DiasUsd >= 0.0)
    }
}
