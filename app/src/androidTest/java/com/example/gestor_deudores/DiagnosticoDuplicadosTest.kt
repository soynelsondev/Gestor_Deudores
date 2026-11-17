package com.example.gestor_deudores

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestor_deudores.data.database.DeudaDataBase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticoDuplicadosTest {

    @Test
    fun testDiagnosticarDuplicadosAbonoId() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = DeudaDataBase.getDatabase(context)

        val duplicados = db.distribucionAbonoDao().obtenerDuplicadosAbonoId()

        assertNotNull(duplicados)
        println("=== DIAGNÓSTICO T0.3: FILAS DUPLICADAS EN distribucion_abonos ===")
        if (duplicados.isEmpty()) {
            println("✅ RESULTADO: No se encontraron filas duplicadas por abonoId en la base de datos.")
        } else {
            println("⚠️ ATENCIÓN: Se encontraron ${duplicados.size} abonoId con filas duplicadas:")
            duplicados.forEach { info ->
                println(" - abonoId: ${info.abonoId} -> Se repite ${info.cantidad} veces")
            }
        }
        println("==================================================================")
    }
}
