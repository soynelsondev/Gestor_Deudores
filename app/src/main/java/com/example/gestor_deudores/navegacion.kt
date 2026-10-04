package com.example.gestor_deudores

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// ¡Importa tus ViewModels y tus vistas Principal aquí!
// Importamos con un alias (as) para que no haya conflicto si ambas funciones se llaman "Principal"
import com.example.gestor_deudores.ui.Registro.Principal as PantallaRegistroDeudor
import com.example.gestor_deudores.ui.Registro.RegistroDeudorViewModel
import com.example.gestor_deudores.ui.home.HistorialDeudorView
import com.example.gestor_deudores.ui.home.HomeViewModel
import com.example.gestor_deudores.ui.cotizador.CotizadorViewModel
import com.example.gestor_deudores.ui.cotizador.CotizadorPrincipal
import com.example.gestor_deudores.ui.home.homePrincipal
import com.example.gestor_deudores.ui.pedidos.PedidoViewModel
import com.example.gestor_deudores.ui.pedidos.PedidosPrincipal
import com.example.gestor_deudores.ui.Resumen.ResumenViewModel
import com.example.gestor_deudores.ui.registroDeuda.Principal as PantallaRegistroDeuda
import com.example.gestor_deudores.ui.registroDeuda.RDeudaViewModel
import com.example.gestor_deudores.ui.rutas

@Composable
fun NavegacionPrincipal(
    viewModelRegistro: RegistroDeudorViewModel,
    viewModelDeuda: RDeudaViewModel,
    viewModelHome: HomeViewModel,
    viewModelPedido: PedidoViewModel,
    viewModelCotizador: CotizadorViewModel,
    viewModelResumen: ResumenViewModel
) {
    val navController = rememberNavController()

    // Arrancamos directamente en el registro para probar
    NavHost(navController = navController, startDestination = rutas.HOME) {


        composable(rutas.HOME){
            homePrincipal(
                viewModel= viewModelHome,
                navController = navController
            )
        }

        // ==========================================
        // 1. PANTALLA: REGISTRO DE DEUDOR (DESDE MIS COBROS)
        // ==========================================
        composable(rutas.REGISTRO) {
            viewModelRegistro.limpiarFormulario()
            PantallaRegistroDeudor(
                viewModel = viewModelRegistro,
                tituloPantalla = "NUEVO CLIENTE / VENTA",
                onNavegarADeuda = { idGenerado ->
                    // Usamos tu función constructora de rutas para saltar a crear deuda (Flujo Venta Rápida)
                    navController.navigate(rutas.crearRutaRegistroDeuda(idGenerado)) {
                        popUpTo(rutas.HOME) // Para que si da atrás no vuelva al registro
                    }
                }
            )
        }

        // ==========================================
        // NUEVO: REGISTRO DE DEUDOR EXPRESS (DESDE BARRA INFERIOR O PEDIDOS)
        // ==========================================
        composable(rutas.REGISTRO_EXPRESS) {
            viewModelRegistro.limpiarFormulario()
            PantallaRegistroDeudor(
                viewModel = viewModelRegistro,
                tituloPantalla = "NUEVO CLIENTE",
                onNavegarADeuda = { 
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // PANTALLA: PEDIDOS
        // ==========================================
        composable(rutas.PEDIDOS) {
            PedidosPrincipal(viewModel = viewModelPedido, navController = navController)
        }

        // ==========================================
        // PANTALLAS EN CONSTRUCCIÓN (COTIZADOR Y RESUMEN)
        // ==========================================
        composable(rutas.COTIZADOR) {
            val tasaBcvActual by viewModelHome.tasaBcvActual.collectAsState()
            CotizadorPrincipal(
                viewModel = viewModelCotizador, 
                navController = navController,
                tasaBcvGlobal = tasaBcvActual
            )
        }

        composable(rutas.RESUMEN) {
            val tasaBcvActual by viewModelHome.tasaBcvActual.collectAsState()
            viewModelResumen.actualizarTasaBcv(tasaBcvActual)
            com.example.gestor_deudores.ui.Resumen.ResumenPrincipal(
                viewModel = viewModelResumen,
                navController = navController
            )
        }

        composable(
            route = rutas.EDITAR_DEUDOR_TEMPLATE,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("idDeuda") { type = NavType.IntType }
            )
        ) { backStackEntry ->

            // Atrapamos el ID que mandaste desde el botón del lapicito
            val idAEditar = backStackEntry.arguments?.getInt("id") ?: 0
            val idDeuda = backStackEntry.arguments?.getInt("idDeuda") ?: 0

            viewModelRegistro.cargarDeudor(idAEditar)

            PantallaRegistroDeudor(
                viewModel = viewModelRegistro,
                onNavegarADeuda = {
                    // Al terminar de editar el deudor, navegamos a editar la deuda
                    navController.navigate(rutas.crearRutaEditarDeuda(idDeuda))
                }
            )
        }

        // ==========================================
        // 1.5. PANTALLA: EDITAR DEUDA
        // ==========================================
        composable(
            route = rutas.EDITAR_DEUDA_TEMPLATE,
            arguments = listOf(navArgument("idDeuda") { type = NavType.IntType })
        ) { backStackEntry ->

            val idDeuda = backStackEntry.arguments?.getInt("idDeuda") ?: 0

            // Le inyectamos el ID de la deuda al ViewModel
            viewModelDeuda.cargarDeuda(idDeuda)

            PantallaRegistroDeuda(
                viewModel = viewModelDeuda,
                onNavegarAtras = {
                    // Cuando guarde con éxito, volvemos al inicio
                    navController.popBackStack(rutas.HOME, inclusive = false)
                }
            )
        }







        // ==========================================
        // 2. PANTALLA: REGISTRO DE DEUDA
        // ==========================================
        composable(
            route = rutas.REGISTRO_DEUDA_TEMPLATE, // Usamos tu plantilla con {id}
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->

            val idRecibido = backStackEntry.arguments?.getInt("id") ?: 0

            // Le inyectamos el ID al ViewModel de la deuda
            viewModelDeuda.inicializarIdDeudor(idRecibido)

            PantallaRegistroDeuda(
                viewModel = viewModelDeuda,
                onNavegarAtras = {
                    // Cuando guarde con éxito, volvemos al inicio
                    navController.popBackStack(rutas.HOME, inclusive = false)
                }
            )
        }

        // ==========================================
        // 3. PANTALLA: HISTORIAL DE DEUDOR
        // ==========================================
        composable(
            route = rutas.HISTORIAL_DEUDOR_TEMPLATE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val idRecibido = backStackEntry.arguments?.getInt("id") ?: 0

            HistorialDeudorView(
                idDeudor = idRecibido,
                viewModel = viewModelHome,
                onVolver = {
                    navController.popBackStack() // Nos devuelve al inicio
                }
            )
        }
    }
}