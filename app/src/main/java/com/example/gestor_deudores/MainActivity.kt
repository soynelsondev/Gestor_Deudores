package com.example.gestor_deudores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestor_deudores.data.DeudaDataBase
import com.example.gestor_deudores.ui.Registro.RegistroDeudorViewModel
import com.example.gestor_deudores.ui.registroDeuda.RDeudaViewModel
// Asegúrate de importar el HomeViewModel
import com.example.gestor_deudores.ui.home.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val baseDeDatos = DeudaDataBase.getDatabase(applicationContext)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RegistroDeudorViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RegistroDeudorViewModel(baseDeDatos.deudorDao()) as T
                }
                if (modelClass.isAssignableFrom(RDeudaViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RDeudaViewModel(baseDeDatos.deudaDao()) as T
                }
                // --- NUEVO: Le enseñamos a crear el HomeViewModel ---
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    // Ojo: el HomeViewModel necesita ambos DAOs
                    return HomeViewModel(baseDeDatos.deudorDao(), baseDeDatos.deudaDao()) as T
                }
                throw IllegalArgumentException("Clase ViewModel desconocida")
            }
        }

        val viewModelRegistro by viewModels<RegistroDeudorViewModel> { viewModelFactory }
        val viewModelDeuda by viewModels<RDeudaViewModel> { viewModelFactory }
        // --- NUEVO: Instanciamos el HomeViewModel ---
        val viewModelHome by viewModels<HomeViewModel> { viewModelFactory }

        setContent {
            NavegacionPrincipal(
                viewModelRegistro = viewModelRegistro,
                viewModelDeuda = viewModelDeuda,
                viewModelHome = viewModelHome // Lo enviamos a la navegación
            )
        }
    }
}
