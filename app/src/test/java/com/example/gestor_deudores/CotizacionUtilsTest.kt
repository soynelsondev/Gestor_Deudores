package com.example.gestor_deudores

import com.example.gestor_deudores.data.database.PlantillaCotizacion
import com.example.gestor_deudores.data.utils.TipoGanancia
import com.example.gestor_deudores.data.utils.calcularCostosPlantilla
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PRUEBAS UNITARIAS OBLIGATORIAS DE LA SECCIÓN 14 DEL MANUAL DEL COTIZADOR ZUBLI.
 * Verifica la exactitud matemática de costos, ganancias, escalas y comisiones.
 */
class CotizacionUtilsTest {

    @Test
    fun testCalculoCostosYGananciaSobreCosto() {
        // Ejemplo de Sección 4.3: Franela $3.90, Empaque $0.0554, DTF $1.4167, Transporte $0.8333 -> Subtotal $6.2054
        // + Operatividad 10% -> Costo total $6.83
        val plantilla = PlantillaCotizacion(
            id = 1,
            nombrePlantilla = "Franela Prueba",
            precioPaquetePieza = 23.40,
            monedaPaquetePieza = "USD",
            cantidadPaquetePieza = 6,
            precioPaqueteEmpaque = 5.54,
            monedaPaqueteEmpaque = "USD",
            cantidadPaqueteEmpaque = 100,
            precioPaquetePapel = 0.0,
            monedaPaquetePapel = "USD",
            cantidadPaquetePapel = 100,
            precioTotalDtf = 8.50,
            monedaDtf = "USD",
            rendimientoDtf = 6,
            costoTransporte = 5.0,
            monedaTransporte = "USD",
            rendimientoTransporte = 6,
            costoDiseno = 0.0,
            monedaDiseno = "USD",
            rendimientoDiseno = 1,
            costoExtra = 0.0,
            monedaExtra = "USD",
            rendimientoExtra = 1,
            porcentajeOperativo = 10f,
            porcentajeGanancia = 40f
        )

        val resultado = calcularCostosPlantilla(plantilla, tasaBcv = 1.0, tipoGanancia = TipoGanancia.SOBRE_COSTO)

        // Verificamos costo total
        assertEquals(6.83, resultado.costoTotalProduccionUsd, 0.05)
        
        // Verificamos ganancia e incremento sobre costo
        assertEquals(9.56, resultado.precioSugeridoUsd, 0.05)
        assertEquals(2.73, resultado.gananciaUsd, 0.05)
        assertFalse(resultado.esPerdida)
    }

    @Test
    fun testAvisoDePerdida() {
        val plantillaPerdida = PlantillaCotizacion(
            id = 2,
            nombrePlantilla = "Perdida",
            precioPaquetePieza = 10.0,
            monedaPaquetePieza = "USD",
            cantidadPaquetePieza = 1,
            precioPaqueteEmpaque = 0.0,
            monedaPaqueteEmpaque = "USD",
            cantidadPaqueteEmpaque = 1,
            precioPaquetePapel = 0.0,
            monedaPaquetePapel = "USD",
            cantidadPaquetePapel = 100,
            precioTotalDtf = 0.0,
            monedaDtf = "USD",
            rendimientoDtf = 1,
            costoTransporte = 0.0,
            monedaTransporte = "USD",
            rendimientoTransporte = 1,
            costoDiseno = 0.0,
            monedaDiseno = "USD",
            rendimientoDiseno = 1,
            costoExtra = 0.0,
            monedaExtra = "USD",
            rendimientoExtra = 1,
            porcentajeOperativo = 0f,
            porcentajeGanancia = 0f
        )

        val resultado = calcularCostosPlantilla(plantillaPerdida, tasaBcv = 1.0, tipoGanancia = TipoGanancia.SOBRE_COSTO)

        assertTrue(resultado.esPerdida)
        assertEquals(10.0, resultado.costoTotalProduccionUsd, 0.01)
        assertEquals(10.0, resultado.precioSugeridoUsd, 0.01)
        assertEquals(0.0, resultado.gananciaUsd, 0.01)
    }

    @Test
    fun testGananciaFijaYComision() {
        val plantillaComision = PlantillaCotizacion(
            id = 3,
            nombrePlantilla = "Comisión",
            precioPaquetePieza = 10.0,
            monedaPaquetePieza = "USD",
            cantidadPaquetePieza = 1,
            precioPaqueteEmpaque = 0.0,
            monedaPaqueteEmpaque = "USD",
            cantidadPaqueteEmpaque = 1,
            precioPaquetePapel = 0.0,
            monedaPaquetePapel = "USD",
            cantidadPaquetePapel = 100,
            precioTotalDtf = 0.0,
            monedaDtf = "USD",
            rendimientoDtf = 1,
            costoTransporte = 0.0,
            monedaTransporte = "USD",
            rendimientoTransporte = 1,
            costoDiseno = 0.0,
            monedaDiseno = "USD",
            rendimientoDiseno = 1,
            costoExtra = 0.0,
            monedaExtra = "USD",
            rendimientoExtra = 1,
            porcentajeOperativo = 0f,
            porcentajeGanancia = 0f,
            comisionPorcentaje = 5f
        )

        val resultado = calcularCostosPlantilla(
            plantilla = plantillaComision,
            tasaBcv = 1.0,
            tipoGanancia = TipoGanancia.GANANCIA_FIJA,
            gananciaFijaUsd = 5.0
        )

        assertEquals(10.0, resultado.costoTotalProduccionUsd, 0.01)
        assertEquals(5.0, resultado.gananciaUsd, 0.01)
        // Precio sin comisión $15, con 5% de comisión = 15 / 0.95 = 15.79
        assertEquals(15.79, resultado.precioSugeridoUsd, 0.05)
    }
}
