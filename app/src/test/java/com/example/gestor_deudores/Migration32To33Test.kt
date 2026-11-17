package com.example.gestor_deudores

import com.example.gestor_deudores.data.database.PedidoCostoLinea
import org.junit.Assert.assertEquals
import org.junit.Test

class Migration32To33Test {

    @Test
    fun testPedidoCostoLineaEstructuraYValoresPredeterminados() {
        val linea1 = PedidoCostoLinea(
            pedidoId = 10,
            indiceArticulo = 0,
            sobre = "MATERIAL",
            nombre = "Franela oversize",
            costoCentavos = 1500L, // $15.00
            tipoDetalle = "EXACTO"
        )

        val linea2 = PedidoCostoLinea(
            pedidoId = 10,
            indiceArticulo = 0,
            sobre = "INSUMOS",
            nombre = "DTF / impresión",
            costoCentavos = 500L, // $5.00
            tipoDetalle = "EXACTO"
        )

        val lineas = listOf(linea1, linea2)
        val sumaTotalCentavos = lineas.sumOf { it.costoCentavos }

        assertEquals(2000L, sumaTotalCentavos) // $20.00 total
        assertEquals(10, linea1.pedidoId)
        assertEquals("MATERIAL", linea1.sobre)
        assertEquals("Franela oversize", linea1.nombre)
        assertEquals("EXACTO", linea1.tipoDetalle)
        assertEquals(0, linea1.indiceArticulo)
    }

    @Test
    fun testValoresPredeterminadosPedidoCostoLinea() {
        val linea = PedidoCostoLinea(
            pedidoId = 5,
            sobre = "TRANSPORTE",
            nombre = "Pasaje Logístico",
            costoCentavos = 250L
        )

        assertEquals(0, linea.id)
        assertEquals(0, linea.indiceArticulo)
        assertEquals("EXACTO", linea.tipoDetalle)
        assertEquals(250L, linea.costoCentavos)
    }
}
