@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.home



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestor_deudores.ui.home.HomeViewModel
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistorialDeudorView(idDeudor: Int, viewModel: HomeViewModel, onVolver: () -> Unit) {
    // Ejecutamos la consulta y la convertimos en un estado reactivo
    val listaHistorial by viewModel.obtenerHistorialDeudor(idDeudor).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HISTORIAL DE PAGOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fondo2)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(fondo)
                .padding(16.dp)
        ) {
            items(listaHistorial) { registro ->
                val esAbono = registro.montoRestante <= 0
                // Si es abono (negativo), lo mostramos en verde. Si es deuda, en negro/rojo
                val colorMonto = if (esAbono) Color(0xFF388E3C) else Color.Black

                val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US"))
                val montoMostrado = formatoUSD.format(Math.abs(registro.montoRestante))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = registro.tipoDeuda.uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = fondo2,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = registro.fecha, color = Color.Gray, fontSize = 12.sp)
                            if (registro.descripcion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = registro.descripcion, color = Color.DarkGray, fontSize = 12.sp)
                            }
                        }

                        // El signo + para deudas nuevas, y el signo - para pagos
                        Text(
                            text = "${if (esAbono) "-" else "+"}$montoMostrado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorMonto
                        )
                    }
                }
            }
        }
    }
}