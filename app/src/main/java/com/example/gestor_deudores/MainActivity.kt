package com.example.gestor_deudores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestor_deudores.data.database.DeudaDataBase
import com.example.gestor_deudores.ui.Registro.RegistroDeudorViewModel
import com.example.gestor_deudores.ui.registroDeuda.RDeudaViewModel
import com.example.gestor_deudores.ui.home.HomeViewModel
import com.example.gestor_deudores.ui.cotizador.CotizadorViewModel
import com.example.gestor_deudores.ui.pedidos.PedidoViewModel
import com.example.gestor_deudores.ui.Resumen.ResumenViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicitar permisos de notificación para Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Programar notificación periódica de cierre de mes
        com.example.gestor_deudores.data.utils.NotificacionUtils.programarNotificacionMensual(applicationContext)

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
                    return HomeViewModel(
                        baseDeDatos.deudorDao(),
                        baseDeDatos.deudaDao(),
                        baseDeDatos.perfilNegocioDao()
                    ) as T
                }
                if (modelClass.isAssignableFrom(PedidoViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PedidoViewModel(
                        baseDeDatos.pedidoDao(),
                        baseDeDatos.deudorDao(),
                        baseDeDatos.deudaDao(),
                        baseDeDatos.plantillaDao()
                    ) as T
                }
                if (modelClass.isAssignableFrom(CotizadorViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return CotizadorViewModel(
                        baseDeDatos.plantillaDao(),
                        baseDeDatos.insumoBibliotecaDao(),
                        baseDeDatos.historialPrecioInsumoDao()
                    ) as T
                }
                if (modelClass.isAssignableFrom(ResumenViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ResumenViewModel(
                        baseDeDatos.deudaDao(),
                        baseDeDatos.deudorDao(),
                        baseDeDatos.pedidoDao(),
                        baseDeDatos.plantillaDao(),
                        baseDeDatos.cuentaBancariaDao(),
                        baseDeDatos.perfilNegocioDao()
                    ) as T
                }
                throw IllegalArgumentException("Clase ViewModel desconocida")
            }
        }

        val viewModelRegistro by viewModels<RegistroDeudorViewModel> { viewModelFactory }
        val viewModelDeuda by viewModels<RDeudaViewModel> { viewModelFactory }
        val viewModelHome by viewModels<HomeViewModel> { viewModelFactory }
        val viewModelPedido by viewModels<PedidoViewModel> { viewModelFactory }
        val viewModelCotizador by viewModels<CotizadorViewModel> { viewModelFactory }
        val viewModelResumen by viewModels<ResumenViewModel> { viewModelFactory }

        setContent {
            NavegacionPrincipal(
                viewModelRegistro = viewModelRegistro,
                viewModelDeuda = viewModelDeuda,
                viewModelHome = viewModelHome,
                viewModelPedido = viewModelPedido,
                viewModelCotizador = viewModelCotizador,
                viewModelResumen = viewModelResumen
            )
        }
    }
}
