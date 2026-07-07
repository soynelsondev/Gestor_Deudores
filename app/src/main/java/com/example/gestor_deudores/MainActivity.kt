package com.example.gestor_deudores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestor_deudores.data.DeudaDataBase
import com.example.gestor_deudores.ui.theme.Gestor_DeudoresTheme
import com.example.gestor_deudores.ui.Registro.RegistroDeudorViewModel
import com.example.gestor_deudores.ui.registroDeuda.RDeudaViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val baseDeDatos = DeudaDataBase.getDatabase(applicationContext)
// Como los ViewModels piden parámetros (los DAOs), necesitamos una fábrica para construirlos
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
                throw IllegalArgumentException("Clase ViewModel desconocida")
            }
        }

        val viewModelRegistro by viewModels<RegistroDeudorViewModel> { viewModelFactory }
        val viewModelDeuda by viewModels<RDeudaViewModel> { viewModelFactory }

        setContent {

            NavegacionPrincipal(
                viewModelRegistro = viewModelRegistro,
                viewModelDeuda = viewModelDeuda
            )
        }
    }
}

