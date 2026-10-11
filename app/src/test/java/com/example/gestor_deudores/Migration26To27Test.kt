package com.example.gestor_deudores

import com.example.gestor_deudores.data.database.DistribucionAbono
import org.junit.Assert.assertEquals
import org.junit.Test

class Migration26To27Test {

    @Test
    fun testDistribucionAbonoTotalesIdenticosPostMigracion() {
        val abonoId = 1
        val pedidoId = 10
        val montoUsd = 100.0
        val totalPedidoUsd = 100.0
        val costoMaterial = 40.0
        val costoPasaje = 10.0
        val costoInsumos = 10.0

        val proporcion = montoUsd / totalPedidoUsd
        val mat = Math.round(costoMaterial * proporcion * 100.0) / 100.0
        val trans = Math.round(costoPasaje * proporcion * 100.0) / 100.0
        val ins = Math.round(costoInsumos * proporcion * 100.0) / 100.0
        val gan = Math.round((montoUsd - (mat + trans + ins)) * 100.0) / 100.0

        val distribucion = DistribucionAbono(
            abonoId = abonoId,
            pedidoId = pedidoId,
            tipoAbono = "VENTA_PEDIDO",
            montoUsd = montoUsd,
            material = mat,
            transporte = trans,
            insumos = ins,
            miPago = 0.0,
            ganancia = gan,
            origen = "EXACTO",
            fechaMillis = System.currentTimeMillis()
        )

        val sumaSobres = distribucion.material + distribucion.transporte + distribucion.insumos + distribucion.miPago + distribucion.ganancia
        assertEquals(montoUsd, sumaSobres, 0.001)

        assertEquals("EXACTO", distribucion.origen)
        assertEquals(40.0, distribucion.material, 0.001)
        assertEquals(10.0, distribucion.transporte, 0.001)
        assertEquals(10.0, distribucion.insumos, 0.001)
        assertEquals(40.0, distribucion.ganancia, 0.001)
    }

    @Test
    fun testDistribucionAbonoCobroManualEstimado() {
        val abonoId = 2
        val montoUsd = 50.0

        val mat = Math.round(montoUsd * 0.50 * 100.0) / 100.0
        val trans = Math.round(montoUsd * 0.10 * 100.0) / 100.0
        val ins = Math.round(montoUsd * 0.10 * 100.0) / 100.0
        val gan = Math.round((montoUsd - (mat + trans + ins)) * 100.0) / 100.0

        val distribucion = DistribucionAbono(
            abonoId = abonoId,
            pedidoId = null,
            tipoAbono = "COBRO_MANUAL",
            montoUsd = montoUsd,
            material = mat,
            transporte = trans,
            insumos = ins,
            miPago = 0.0,
            ganancia = gan,
            origen = "ESTIMADO",
            fechaMillis = System.currentTimeMillis()
        )

        val sumaSobres = distribucion.material + distribucion.transporte + distribucion.insumos + distribucion.miPago + distribucion.ganancia
        assertEquals(montoUsd, sumaSobres, 0.001)
        assertEquals("ESTIMADO", distribucion.origen)
    }
}
