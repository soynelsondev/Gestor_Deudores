package com.example.gestor_deudores

import com.example.gestor_deudores.data.database.InsumoCotizacion
import com.example.gestor_deudores.data.database.PlantillaCotizacion
import com.example.gestor_deudores.data.utils.calcularCostosPlantilla
import com.example.gestor_deudores.data.utils.desglosarLineasPlantilla
import com.example.gestor_deudores.data.utils.redondear2
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class LineasCostoPlantillaTest {

    @Test
    fun testEquivalenciaPlantillaTradicional() {
        val plantilla = PlantillaCotizacion(
            nombrePlantilla = "Franela Sublimada",
            precioPaquetePieza = 39.0, // $3.90 c/u (lote 10)
            monedaPaquetePieza = "USD",
            cantidadPaquetePieza = 10,
            precioPaqueteEmpaque = 10.0, // $1.00 c/u
            monedaPaqueteEmpaque = "USD",
            cantidadPaqueteEmpaque = 10,
            precioPaquetePapel = 5.0, // $0.50 c/u
            monedaPaquetePapel = "USD",
            cantidadPaquetePapel = 10,
            precioTotalDtf = 12.0, // $1.20 c/u
            monedaDtf = "USD",
            rendimientoDtf = 10,
            costoTransporte = 10.0, // $1.00 c/u
            monedaTransporte = "USD",
            rendimientoTransporte = 10,
            costoDiseno = 20.0, // $2.00 c/u
            monedaDiseno = "USD",
            rendimientoDiseno = 10,
            costoExtra = 6.0, // $0.60 c/u
            monedaExtra = "USD",
            rendimientoExtra = 10,
            porcentajeOperativo = 10f, // 10%
            porcentajeGanancia = 40f
        )

        val costosCalculados = calcularCostosPlantilla(plantilla, tasaBcv = 1.0, cantidadPedido = 1)
        val lineas = desglosarLineasPlantilla(plantilla, tasaBcv = 1.0, cantidadPedido = 1)

        val sumaMaterialSinRedondear = lineas.filter { it.sobre == "MATERIAL" }.sumOf { it.costoUnitarioSinRedondearUsd }
        val sumaTransporteSinRedondear = lineas.filter { it.sobre == "TRANSPORTE" }.sumOf { it.costoUnitarioSinRedondearUsd }
        val sumaInsumosSinRedondear = lineas.filter { it.sobre == "INSUMOS" }.sumOf { it.costoUnitarioSinRedondearUsd }

        assertTrue("Material debe coincidir", abs(sumaMaterialSinRedondear.redondear2() - costosCalculados.costoPiezaBaseUsd) <= 0.006)
        assertTrue("Transporte debe coincidir", abs(sumaTransporteSinRedondear.redondear2() - costosCalculados.costoPasajeUsd) <= 0.006)
        assertTrue("Insumos debe coincidir", abs(sumaInsumosSinRedondear.redondear2() - costosCalculados.costoInsumosUsd) <= 0.006)
    }

    @Test
    fun testEquivalenciaPlantillaConInsumosDinamicos() {
        val insumosDinamicos = listOf(
            InsumoCotizacion(nombre = "Taza 11oz Blanca", categoria = "MATERIA_PRIMA", precioLote = 15.0, rendimientoCantidad = 10),
            InsumoCotizacion(nombre = "Cinta Térmica", categoria = "INSUMO_EXTRA", precioLote = 5.0, rendimientoCantidad = 20),
            InsumoCotizacion(nombre = "Flete Delivery", categoria = "SERVICIO", precioLote = 10.0, rendimientoCantidad = 5)
        )
        val gson = Gson()

        val plantilla = PlantillaCotizacion(
            nombrePlantilla = "Taza Sublimada Especial",
            precioPaquetePieza = 0.0,
            monedaPaquetePieza = "USD",
            cantidadPaquetePieza = 1,
            precioPaqueteEmpaque = 0.0,
            monedaPaqueteEmpaque = "USD",
            cantidadPaqueteEmpaque = 1,
            precioPaquetePapel = 0.0,
            monedaPaquetePapel = "USD",
            cantidadPaquetePapel = 1,
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
            porcentajeOperativo = 15f,
            porcentajeGanancia = 50f,
            costosAdicionalesJson = gson.toJson(insumosDinamicos)
        )

        val costosCalculados = calcularCostosPlantilla(plantilla, tasaBcv = 1.0, cantidadPedido = 10)
        val lineas = desglosarLineasPlantilla(plantilla, tasaBcv = 1.0, cantidadPedido = 10)

        val sumaMaterial = lineas.filter { it.sobre == "MATERIAL" }.sumOf { it.costoUnitarioSinRedondearUsd }
        val sumaTransporte = lineas.filter { it.sobre == "TRANSPORTE" }.sumOf { it.costoUnitarioSinRedondearUsd }
        val sumaInsumos = lineas.filter { it.sobre == "INSUMOS" }.sumOf { it.costoUnitarioSinRedondearUsd }

        assertTrue("Material dinámico coincide", abs(sumaMaterial.redondear2() - costosCalculados.costoPiezaBaseUsd) <= 0.006)
        assertTrue("Transporte dinámico coincide", abs(sumaTransporte.redondear2() - costosCalculados.costoPasajeUsd) <= 0.006)
        assertTrue("Insumos dinámicos coinciden", abs(sumaInsumos.redondear2() - costosCalculados.costoInsumosUsd) <= 0.006)
    }

    @Test
    fun testEquivalenciaConConversionBolivares() {
        val plantilla = PlantillaCotizacion(
            nombrePlantilla = "Gorra Bordada en Bs",
            precioPaquetePieza = 300.0, // 300 Bs (tasa 60 = $5.00)
            monedaPaquetePieza = "VES",
            cantidadPaquetePieza = 1,
            precioPaqueteEmpaque = 0.0,
            monedaPaqueteEmpaque = "USD",
            cantidadPaqueteEmpaque = 1,
            precioPaquetePapel = 0.0,
            monedaPaquetePapel = "USD",
            cantidadPaquetePapel = 1,
            precioTotalDtf = 0.0,
            monedaDtf = "USD",
            rendimientoDtf = 1,
            costoTransporte = 120.0, // 120 Bs (tasa 60 = $2.00)
            monedaTransporte = "VES",
            rendimientoTransporte = 1,
            costoDiseno = 0.0,
            monedaDiseno = "USD",
            rendimientoDiseno = 1,
            costoExtra = 0.0,
            monedaExtra = "USD",
            rendimientoExtra = 1,
            porcentajeOperativo = 0f,
            porcentajeGanancia = 30f
        )

        val tasa = 60.0
        val costosCalculados = calcularCostosPlantilla(plantilla, tasaBcv = tasa, cantidadPedido = 1)
        val lineas = desglosarLineasPlantilla(plantilla, tasaBcv = tasa, cantidadPedido = 1)

        val sumaMat = lineas.filter { it.sobre == "MATERIAL" }.sumOf { it.costoUnitarioSinRedondearUsd }
        val sumaTrans = lineas.filter { it.sobre == "TRANSPORTE" }.sumOf { it.costoUnitarioSinRedondearUsd }

        assertEquals(costosCalculados.costoPiezaBaseUsd, sumaMat.redondear2(), 0.01)
        assertEquals(costosCalculados.costoPasajeUsd, sumaTrans.redondear2(), 0.01)
    }
}
