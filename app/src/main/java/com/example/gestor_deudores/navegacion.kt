package com.example.gestor_deudores

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// ¡Importa tus ViewModels y tus vistas Principal aquí!
// Importamos con un alias (as) para que no haya conflicto si ambas funciones se llaman "Principal"
import com.example.gestor_deudores.ui.Registro.Principal as PantallaRegistroDeudor
import com.example.gestor_deudores.ui.Registro.RegistroDeudorViewModel
import com.example.gestor_deudores.ui.registroDeuda.Principal as PantallaRegistroDeuda
import com.example.gestor_deudores.ui.registroDeuda.RDeudaViewModel
import com.example.gestor_deudores.ui.rutas

@Composable
fun NavegacionPrincipal(
    viewModelRegistro: RegistroDeudorViewModel,
    viewModelDeuda: RDeudaViewModel
) {
    val navController = rememberNavController()

    // Arrancamos directamente en el registro para probar
    NavHost(navController = navController, startDestination = rutas.REGISTRO) {

        // ==========================================
        // 1. PANTALLA: REGISTRO DE DEUDOR
        // ==========================================
        composable(rutas.REGISTRO) {
            PantallaRegistroDeudor(
                viewModel = viewModelRegistro,
                onNavegarADeuda = { idGenerado ->
                    // Usamos tu función constructora de rutas
                    navController.navigate(rutas.crearRutaRegistroDeuda(idGenerado))
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
                    navController.popBackStack(rutas.REGISTRO, inclusive = false)
                }
            )
        }
    }
}