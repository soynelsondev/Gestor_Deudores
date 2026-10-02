@file:kotlin.OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestor_deudores.data.database.PedidoConCliente
import com.example.gestor_deudores.data.utils.abrirWhatsApp
import com.example.gestor_deudores.ui.theme.componentes
import com.example.gestor_deudores.ui.theme.estados
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2
import com.example.gestor_deudores.ui.theme.fondo_claro
import com.example.gestor_deudores.ui.theme.textoInactivo
import com.example.gestor_deudores.ui.theme.verdeWhatsapp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidosPrincipal(viewModel: PedidoViewModel) {
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()
    val listaPedidos by viewModel.listaPedidos.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PEDIDOS DE CLIENTES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fondo2)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(fondo)
        ) {
            // 1. Buscador de Pedidos
            BuscadorPedidos(
                textoActual = textoBusqueda,
                onTextoCambiado = { viewModel.onTextoBusquedaChange(it) }
            )

            // 2. Filtros por Estado (Chips)
            FiltrosEstadoPedidos(
                estadoActual = filtroEstado,
                onEstadoSeleccionado = { viewModel.onFiltroEstadoChange(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Lista de Pedidos
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(listaPedidos) { item ->
                    TarjetaPedido(
                        item = item,
                        onCambiarEstado = { nuevoEstado ->
                            viewModel.cambiarEstadoPedido(item, nuevoEstado)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BuscadorPedidos(textoActual: String, onTextoCambiado: (String) -> Unit) {
    TextField(
        value = textoActual,
        onValueChange = onTextoCambiado,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(text = "Buscar producto o cliente...", color = Color.Gray) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            if (textoActual.isNotEmpty()) {
                IconButton(onClick = { onTextoCambiado("") }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                }
            }
        },
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fondo2,
            unfocusedContainerColor = fondo2,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun FiltrosEstadoPedidos(
    estadoActual: FiltroEstadoPedido,
    onEstadoSeleccionado: (FiltroEstadoPedido) -> Unit
) {
    val opciones = listOf(
        FiltroEstadoPedido.TODOS to "Todos",
        FiltroEstadoPedido.RECIBIDO to "Recibido",
        FiltroEstadoPedido.EN_PRODUCCION to "En Producción",
        FiltroEstadoPedido.LISTO to "Listo",
        FiltroEstadoPedido.ENTREGADO to "Entregado"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones) { (estado, label) ->
            val seleccionado = estadoActual == estado
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (seleccionado) estados else fondo2)
                    .clickable { onEstadoSeleccionado(estado) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (seleccionado) Color.White else Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun TarjetaPedido(
    item: PedidoConCliente,
    onCambiarEstado: (String) -> Unit
) {
    val context = LocalContext.current
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaCreacionStr = formatoFecha.format(Date(item.fechaCreacionMillis))
    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(item.totalUsd)

    // Colores basados en la paleta oficial de la app
    val colorEstado = when (item.estado) {
        "RECIBIDO" -> fondo2
        "EN_PRODUCCION" -> componentes
        "LISTO" -> estados
        "ENTREGADO" -> textoInactivo
        else -> estados
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila Superior: Cliente y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${item.clienteNombre} ${item.clienteApellido}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Pedido del $fechaCreacionStr",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Etiqueta de Estado
                Box(
                    modifier = Modifier
                        .background(colorEstado.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = item.estado.replace("_", " "),
                        color = colorEstado,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Producto y Detalles
            Text(
                text = "${item.producto} (x${item.cantidad})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $formatoUSD",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = componentes
                )

                // Botón de WhatsApp si el pedido está LISTO
                if (item.estado == "LISTO" && item.clienteTelefono.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val msg = "Hola ${item.clienteNombre}, te escribimos para avisarte que tu pedido de *${item.producto}* ya está ¡LISTO para retirar/entregar! ¡Muchas gracias!"
                            abrirWhatsApp(context, item.clienteTelefono, msg)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(verdeWhatsapp, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Avisar que está listo",
                            tint = Color.White
                        )
                    }
                }
            }

            if (item.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Notas: ${item.notas}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector Rápido para Cambiar Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Cambiar estado:", fontSize = 12.sp, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("RECIBIDO", "EN_PRODUCCION", "LISTO", "ENTREGADO").forEach { e ->
                        val esActual = item.estado == e
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (esActual) estados else fondo2)
                                .clickable { onCambiarEstado(e) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (e) {
                                    "RECIBIDO" -> "Recibido"
                                    "EN_PRODUCCION" -> "En Prod."
                                    "LISTO" -> "Listo"
                                    "ENTREGADO" -> "Entregado"
                                    else -> e
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (esActual) Color.White else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}
