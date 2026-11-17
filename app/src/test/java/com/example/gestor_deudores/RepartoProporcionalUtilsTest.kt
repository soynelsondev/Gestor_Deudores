package com.example.gestor_deudores

import com.example.gestor_deudores.data.utils.repartirProporcional
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class RepartoProporcionalUtilsTest {

    @Test
    fun testRepartoEjemploCamisaYDtf() {
        val totalAbonoCentavos = 1000L // $10.00
        val pesosCostos = listOf(1500L, 500L) // $15.00 camisa, $5.00 DTF

        val resultado = repartirProporcional(totalAbonoCentavos, pesosCostos)

        assertEquals(2, resultado.size)
        assertEquals(750L, resultado[0]) // $7.50
        assertEquals(250L, resultado[1]) // $2.50
        assertEquals(totalAbonoCentavos, resultado.sum())
    }

    @Test
    fun testRepartoConResiduosImparesYCuadreExacto() {
        val totalCentavos = 10L // 10 centavos entre 3 partes iguales
        val pesos = listOf(100L, 100L, 100L)

        val resultado = repartirProporcional(totalCentavos, pesos)

        assertEquals(3, resultado.size)
        assertEquals(totalCentavos, resultado.sum()) // Suma debe ser exactamente 10
    }

    @Test
    fun testListaVacia() {
        val resultado = repartirProporcional(1000L, emptyList())
        assertEquals(emptyList<Long>(), resultado)
    }

    @Test
    fun testTotalCero() {
        val resultado = repartirProporcional(0L, listOf(100L, 200L, 300L))
        assertEquals(listOf(0L, 0L, 0L), resultado)
    }

    @Test
    fun testTodosPesosCero() {
        val resultado = repartirProporcional(500L, listOf(0L, 0L, 0L))
        assertEquals(listOf(0L, 0L, 0L), resultado)
    }

    @Test
    fun testUnSoloElemento() {
        val resultado = repartirProporcional(12345L, listOf(999L))
        assertEquals(listOf(12345L), resultado)
    }

    @Test
    fun testPesosConElementosCeroYPositivos() {
        val totalCentavos = 3000L // $30.00
        val pesos = listOf(0L, 1000L, 2000L) // Elemento 0 tiene costo 0

        val resultado = repartirProporcional(totalCentavos, pesos)

        assertEquals(0L, resultado[0])
        assertEquals(1000L, resultado[1])
        assertEquals(2000L, resultado[2])
        assertEquals(totalCentavos, resultado.sum())
    }

    @Test
    fun testMuchosCasosAleatoriosCuadranExactamente() {
        val random = Random(42)

        repeat(200) {
            val total = random.nextLong(0L, 1_000_000L) // Hasta $10,000.00
            val cantidadPesos = random.nextInt(1, 15)
            val pesos = List(cantidadPesos) { random.nextLong(0L, 500_000L) }

            val resultado = repartirProporcional(total, pesos)

            assertEquals(pesos.size, resultado.size)
            assertEquals("La suma del reparto debe igualar exactamente a totalCentavos", total, resultado.sum())
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExcepcionSiTotalEsNegativo() {
        repartirProporcional(-1L, listOf(100L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExcepcionSiPesosContienenNegativo() {
        repartirProporcional(100L, listOf(50L, -10L))
    }
}
