package com.example.gestor_deudores

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestor_deudores.data.database.DeudaDataBase
import com.example.gestor_deudores.debug.RadiografiaDebugExporter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RadiografiaBaselineExportTest {

    @Test
    fun testExportarBaselineRadiografiaJson() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = DeudaDataBase.getDatabase(context)

        val json = RadiografiaDebugExporter.exportarBaselineRadiografiaJson(context, db)

        assertNotNull(json)
        assertTrue(json.isNotBlank())
        assertTrue(json.contains("filasDistribucionAbono"))
        assertTrue(json.contains("totalesPorPeriodo"))
        assertTrue(json.contains("abonosDesgloseCalculados"))
    }
}
