package com.example.gestor_deudores

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestor_deudores.data.database.DeudaDataBase
import com.example.gestor_deudores.debug.RadiografiaDebugExporter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticoVinculosTest {

    @Test
    fun testDiagnosticarCalidadVinculosAbonoPedido() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = DeudaDataBase.getDatabase(context)

        val jsonReport = RadiografiaDebugExporter.exportarDiagnosticoVinculosJson(context, db)

        assertNotNull(jsonReport)
        println("=== DIAGNÓSTICO T0.4: CALIDAD DE VÍNCULOS ABONO-PEDIDO ===")
        println(jsonReport)
        println("==========================================================")
    }
}
