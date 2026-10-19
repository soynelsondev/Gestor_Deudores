@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.pedidos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.navigation.NavController
import com.example.gestor_deudores.ui.rutas
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.FileProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import java.io.File
import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gestor_deudores.data.database.Deudor
import com.example.gestor_deudores.data.database.PedidoConCliente
import com.example.gestor_deudores.data.database.PlantillaCotizacion
import com.example.gestor_deudores.data.utils.abrirWhatsApp
import com.example.gestor_deudores.ui.theme.componentes
import com.example.gestor_deudores.ui.theme.estados
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2
import com.example.gestor_deudores.ui.home.BarraNavegacionInferior
import com.example.gestor_deudores.ui.theme.fondo_claro
import com.example.gestor_deudores.ui.theme.textoInactivo
import com.example.gestor_deudores.ui.theme.verdeWhatsapp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PedidosPrincipal(viewModel: PedidoViewModel, navController: NavController, tasaBcvGlobal: Double = 40.0) {
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()
    val listaPedidos by viewModel.listaPedidos.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    if (mostrarDialogoCrear) {
        DialogoCrearPedido(
            viewModel = viewModel,
            navController = navController,
            tasaBcvGlobal = tasaBcvGlobal,
            onDismiss = { mostrarDialogoCrear = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PEDIDOS DE CLIENTES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fondo2)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = estados,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Pedido")
            }
        },
        bottomBar = {
            BarraNavegacionInferior(
                rutaActual = rutas.PEDIDOS,
                onIrAInicio = { navController.navigate(rutas.HOME) },
                onIrAPedidos = {}, // Ya estamos aquí
                onIrACotizar = { navController.navigate(rutas.COTIZADOR) },
                onIrAResumen = { navController.navigate(rutas.RESUMEN) }
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

            // Banner de Borrador Guardado
            val borradorActual by viewModel.borradorActual.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.cargarBorradorPersistido(context)
            }

            if (borradorActual != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📝 Borrador de Pedido Guardado", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB71C1C))
                            val clienteDesc = if (!borradorActual?.clienteNombre.isNullOrBlank()) "Cliente: ${borradorActual?.clienteNombre}" else "Sin cliente asignado"
                            Text(clienteDesc, fontSize = 11.sp, color = Color.DarkGray)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.descartarBorrador(context) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Descartar", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                            Button(
                                onClick = { mostrarDialogoCrear = true },
                                colors = ButtonDefaults.buttonColors(containerColor = estados)
                            ) {
                                Text("Continuar", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Lista de Pedidos
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(listaPedidos) { item ->
                    TarjetaPedido(
                        item = item,
                        viewModel = viewModel,
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
fun DialogoVisorImagen(
    imagen: com.example.gestor_deudores.data.database.PedidoImagen,
    onCambiarTipo: (String) -> Unit,
    onEliminar: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = rememberBitmapFromUri(imagen.ruta)
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    // Pinch to zoom state (Sección 6.4)
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    if (mostrarConfirmarEliminar) {
        Dialog(onDismissRequest = { mostrarConfirmarEliminar = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = fondo)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("¿Eliminar esta foto?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Esta acción eliminará el archivo de imagen del pedido permanentemente.", fontSize = 12.sp, color = Color.LightGray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { mostrarConfirmarEliminar = false }) {
                            Text("Cancelar", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                mostrarConfirmarEliminar = false
                                onEliminar()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera con Chip de Tipo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visor de Foto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                when (imagen.tipo) {
                                    "DISENO" -> Color(0xFF1976D2)
                                    "COMPROBANTE" -> Color(0xFFF57F17)
                                    "ENTREGA" -> Color(0xFF2E7D32)
                                    else -> estados
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (imagen.tipo) {
                                "DISENO" -> "Diseño"
                                "COMPROBANTE" -> "Comprobante"
                                "ENTREGA" -> "Entrega"
                                else -> imagen.tipo
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Imagen en grande con Pinch-To-Zoom (Sección 6.4)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Visor de foto con zoom",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        )
                    } else {
                        Text("No se pudo cargar la imagen", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                // Botón Compartir Foto por WhatsApp (Sección 6.4)
                Button(
                    onClick = {
                        try {
                            val archivo = File(imagen.ruta)
                            if (archivo.exists()) {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/*"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_TEXT, "Te compartimos esta imagen del pedido.")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir imagen"))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = verdeWhatsapp)
                ) {
                    Text("📲 Compartir Foto por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Selector para Cambiar Tipo de Imagen (Sección 6.3)
                Text("Cambiar tipo de imagen:", fontSize = 11.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("DISENO" to "Diseño", "COMPROBANTE" to "Comprobante", "ENTREGA" to "Entrega").forEach { (tipoKey, label) ->
                        val esActual = imagen.tipo == tipoKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (esActual) estados else fondo_claro)
                                .clickable { onCambiarTipo(tipoKey) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (esActual) Color.White else Color.DarkGray
                            )
                        }
                    }
                }

                // Botones Eliminar y Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { mostrarConfirmarEliminar = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = fondo2)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoConfirmarProduccionSinDiseno(
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("⚠️ Diseño no aprobado", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("El diseño de este pedido aún figura como PENDIENTE. ¿Deseas pasar a producción sin diseño aprobado?", fontSize = 12.sp, color = Color.LightGray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.LightGray)
                    }

                    Button(
                        onClick = onConfirmar,
                        colors = ButtonDefaults.buttonColors(containerColor = estados)
                    ) {
                        Text("Producir de todos modos", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoCancelarPedidoConAbono(
    pedido: PedidoConCliente,
    onConfirmarCancelacion: (devolverAbono: Boolean, nota: String) -> Unit,
    onEliminarDefinitivamente: () -> Unit,
    onDismiss: () -> Unit
) {
    var notaCancelacion by remember { mutableStateOf("") }
    var devolverAbono by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Cancelar Pedido #${pedido.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                Text("El pedido pasará a estado CANCELADO. ¿Qué deseas hacer con el abono recibido?", fontSize = 12.sp, color = Color.LightGray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (devolverAbono) estados else fondo_claro)
                            .clickable { devolverAbono = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Devolver Abono", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (devolverAbono) Color.White else Color.DarkGray)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!devolverAbono) estados else fondo_claro)
                            .clickable { devolverAbono = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Retener Abono", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!devolverAbono) Color.White else Color.DarkGray)
                    }
                }

                OutlinedTextField(
                    value = notaCancelacion,
                    onValueChange = { notaCancelacion = it },
                    label = { Text("Motivo de cancelación") },
                    placeholder = { Text("Ej. Cliente solicitó cancelar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Volver", color = Color.Gray)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = onEliminarDefinitivamente,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("🗑️ Eliminar", color = Color.Red, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onConfirmarCancelacion(devolverAbono, notaCancelacion) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoRegistrarCobroPedido(
    pedido: PedidoConCliente,
    saldoRestanteUsd: Double,
    tasaBcvActual: Double = 40.0,
    onConfirmarCobro: (montoUsd: Double, metodo: String, ref: String, esBs: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var montoTexto by remember { mutableStateOf(String.format(java.util.Locale.US, "%.2f", saldoRestanteUsd)) }
    var esBs by remember { mutableStateOf(false) }
    var metodoPago by remember { mutableStateOf("Efectivo ($)") }
    var referencia by remember { mutableStateOf("") }
    var errorMsgCobro by remember { mutableStateOf("") }

    val metodosDisponibles = listOf("Efectivo ($)", "Pago Móvil (Bs)", "Zelle ($)", "Transferencia (Bs)", "Punto de Venta")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Registrar Cobro · Pedido #${pedido.id}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Cliente: ${pedido.clienteNombre} ${pedido.clienteApellido} · Saldo Pendiente: $${String.format(java.util.Locale.US, "%.2f", saldoRestanteUsd)} USD", fontSize = 12.sp, color = Color.LightGray)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!esBs) estados else fondo_claro)
                            .clickable { esBs = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("USD ($)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!esBs) Color.White else Color.DarkGray)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (esBs) estados else fondo_claro)
                            .clickable { esBs = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bolívares (Bs)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (esBs) Color.White else Color.DarkGray)
                    }
                }

                val montoVal = montoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                val montoCalcUsd = if (esBs && tasaBcvActual > 0) montoVal / tasaBcvActual else montoVal

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { 
                        montoTexto = it
                        errorMsgCobro = ""
                    },
                    label = { Text(if (esBs) "Monto Pagado en Bs" else "Monto Pagado en USD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (esBs && tasaBcvActual > 0) {
                    Text("Equivalente: $${String.format(java.util.Locale.US, "%.2f", montoCalcUsd)} USD (Tasa BCV: $tasaBcvActual)", fontSize = 11.sp, color = Color(0xFF4CAF50))
                }

                if (errorMsgCobro.isNotEmpty()) {
                    Text(errorMsgCobro, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text("Método de Pago:", fontSize = 11.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(metodosDisponibles) { m ->
                        val sel = metodoPago == m
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (sel) estados else fondo_claro)
                                .clickable { metodoPago = m }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(m, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else Color.DarkGray)
                        }
                    }
                }

                OutlinedTextField(
                    value = referencia,
                    onValueChange = { referencia = it },
                    label = { Text("Número de Referencia (Opcional)") },
                    placeholder = { Text("Ej. 123456") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.LightGray)
                    }

                    Button(
                        onClick = {
                            if (montoCalcUsd <= 0.0) {
                                errorMsgCobro = "Ingresa un monto válido"
                            } else if (montoCalcUsd > saldoRestanteUsd + 0.01) {
                                errorMsgCobro = "El cobro no puede ser mayor al saldo pendiente de este pedido ($${String.format(java.util.Locale.US, "%.2f", saldoRestanteUsd)})"
                            } else {
                                onConfirmarCobro(montoCalcUsd, metodoPago, referencia, esBs)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Registrar Pago", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaPedido(
    item: PedidoConCliente,
    viewModel: PedidoViewModel,
    onCambiarEstado: (String) -> Unit
) {
    val context = LocalContext.current
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaCreacionStr = formatoFecha.format(Date(item.fechaCreacionMillis))
    val fechaEntregaStr = if (item.fechaEntregaMillis > 0) formatoFecha.format(Date(item.fechaEntregaMillis)) else "Sin fecha"
    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(item.totalUsd)

    val hoyMillis = System.currentTimeMillis()
    val esVencido = item.estado != "ENTREGADO" && item.estado != "CANCELADO" && item.fechaEntregaMillis > 0 && item.fechaEntregaMillis < hoyMillis

    val imagenesPedido by viewModel.obtenerImagenesPorPedido(item.id).collectAsState(initial = emptyList())
    var imagenSeleccionadaVisor by remember { mutableStateOf<com.example.gestor_deudores.data.database.PedidoImagen?>(null) }

    val deudasCliente by remember(item.deudorId) {
        viewModel.obtenerDeudasPorDeudor(item.deudorId)
    }.collectAsState(initial = emptyList())

    val saldoRestantePedido = remember(deudasCliente, item) {
        viewModel.obtenerSaldoRestantePedido(item.id, item.totalUsd, deudasCliente)
    }

    var mostrarConfirmarProduccion by remember { mutableStateOf(false) }
    var mostrarCancelarPedido by remember { mutableStateOf(false) }
    var mostrarDialogoRegistrarCobro by remember { mutableStateOf(false) }

    if (mostrarDialogoRegistrarCobro) {
        DialogoRegistrarCobroPedido(
            pedido = item,
            saldoRestanteUsd = saldoRestantePedido,
            tasaBcvActual = 40.0,
            onConfirmarCobro = { montoUsd, metodo, ref, esBs ->
                mostrarDialogoRegistrarCobro = false
                viewModel.registrarCobroSaldoPedido(
                    deudorId = item.deudorId,
                    pedidoId = item.id,
                    montoUsd = montoUsd,
                    metodoPago = metodo,
                    referencia = ref,
                    esBs = esBs,
                    tasaBcv = 40.0
                ) {}
            },
            onDismiss = { mostrarDialogoRegistrarCobro = false }
        )
    }

    val colorEstado = when (item.estado) {
        "RECIBIDO" -> fondo2
        "EN_PRODUCCION" -> componentes
        "LISTO" -> estados
        "ENTREGADO" -> textoInactivo
        "CANCELADO" -> Color.Red
        else -> estados
    }

    if (mostrarConfirmarProduccion) {
        DialogoConfirmarProduccionSinDiseno(
            onConfirmar = {
                mostrarConfirmarProduccion = false
                onCambiarEstado("EN_PRODUCCION")
            },
            onDismiss = { mostrarConfirmarProduccion = false }
        )
    }

    if (mostrarCancelarPedido) {
        DialogoCancelarPedidoConAbono(
            pedido = item,
            onConfirmarCancelacion = { devolver, nota ->
                mostrarCancelarPedido = false
                viewModel.cancelarPedidoConDecisionAbono(item, devolver, nota) {}
            },
            onEliminarDefinitivamente = {
                mostrarCancelarPedido = false
                viewModel.eliminarPedidoDefinitivamente(item)
            },
            onDismiss = { mostrarCancelarPedido = false }
        )
    }

    if (imagenSeleccionadaVisor != null) {
        DialogoVisorImagen(
            imagen = imagenSeleccionadaVisor!!,
            onCambiarTipo = { nuevoTipo ->
                viewModel.cambiarTipoImagen(imagenSeleccionadaVisor!!.id, nuevoTipo)
                imagenSeleccionadaVisor = null
            },
            onEliminar = {
                val imgAE = imagenSeleccionadaVisor!!
                viewModel.eliminarImagen(imgAE)
                imagenSeleccionadaVisor = null
            },
            onDismiss = { imagenSeleccionadaVisor = null }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. CABECERA: Nombre Cliente + Estado Pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${item.clienteNombre} ${item.clienteApellido}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (item.urgente) {
                            Text("⚡ URGENTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }
                    Text(
                        text = "Pedido: $fechaCreacionStr · Entrega: $fechaEntregaStr",
                        fontSize = 11.sp,
                        color = if (esVencido) Color.Red else Color.Gray,
                        fontWeight = if (esVencido) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .background(colorEstado.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = item.estado.replace("_", " "),
                        color = colorEstado,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ALERTA DE VENCIMIENTO
            if (esVencido) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = "⚠️ ¡ENTREGA VENCIDA! Debió entregarse el $fechaEntregaStr",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // 2. CHIP DE DISEÑO HORIZONTAL (SECCIÓN 8.1 - NUNCA SE DEFORMA VERTICALMENTE)
            val colorDiseno = when (item.estadoDiseno) {
                "APROBADO" -> Color(0xFF2E7D32)
                "ENVIADO" -> Color(0xFF1976D2)
                else -> Color(0xFFF57F17)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colorDiseno.copy(alpha = 0.15f))
                        .clickable {
                            val sigEstado = when (item.estadoDiseno) {
                                "PENDIENTE" -> "ENVIADO"
                                "ENVIADO" -> "APROBADO"
                                else -> "PENDIENTE"
                            }
                            viewModel.cambiarEstadoDiseno(item.id, sigEstado)
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Diseño: ${item.estadoDiseno}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorDiseno,
                        maxLines = 1
                    )
                }
            }

            // 3. PRODUCTOS Y DETALLES A ANCHO COMPLETO CON DESGLOSE
            var mostrarDetalleProductos by remember { mutableStateOf(false) }

            Text(
                text = "${item.producto} (x${item.cantidad})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { mostrarDetalleProductos = !mostrarDetalleProductos },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (mostrarDetalleProductos) "▲ Ocultar desglose de productos" else "▼ 🔍 Ver desglose de productos",
                    fontSize = 11.sp,
                    color = estados,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = mostrarDetalleProductos) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = fondo2.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Desglose de Productos:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        val lineasEspecificaciones = item.notas.split("\n---\n").find { it.contains("Prod #") || it.contains("Detalle:") || it.contains("Estampa:") }

                        if (lineasEspecificaciones != null && lineasEspecificaciones.isNotBlank()) {
                            val itemsEspecificaciones = lineasEspecificaciones.split("\n")
                            itemsEspecificaciones.forEach { espec ->
                                Text(
                                    text = espec,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                text = "• ${item.producto} — Cantidad: ${item.cantidad} pieza(s)",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // VISTA DE IMÁGENES ASOCIADAS Y SUS TIPOS (SECCIONES 6.3 Y 6.4)
            if (imagenesPedido.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(imagenesPedido) { imgRecord ->
                        val bitmap = rememberBitmapFromUri(imgRecord.rutaMiniatura.ifBlank { imgRecord.ruta })
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { imagenSeleccionadaVisor = imgRecord }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray)
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = imgRecord.tipo,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (imgRecord.tipo) {
                                            "DISENO" -> Color(0xFF1976D2)
                                            "COMPROBANTE" -> Color(0xFFF57F17)
                                            "ENTREGA" -> Color(0xFF2E7D32)
                                            else -> estados
                                        }
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (imgRecord.tipo) {
                                        "DISENO" -> "Diseño"
                                        "COMPROBANTE" -> "Pago"
                                        "ENTREGA" -> "Entrega"
                                        else -> imgRecord.tipo
                                    },
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // TOTAL Y BARRAS DE ACCIÓN (SECCIÓN 9)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(item.totalUsd)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = componentes
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (saldoRestantePedido > 0.01) {
                        TextButton(
                            onClick = { mostrarDialogoRegistrarCobro = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("💰 Cobrar ($${String.format(java.util.Locale.US, "%.2f", saldoRestantePedido)})", fontSize = 11.sp, color = estados, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("✅ 100% Pagado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    if (item.estado == "LISTO") {
                        IconButton(
                            onClick = {
                                val msg = "Hola ${item.clienteNombre}, te escribimos para avisarte que tu pedido de *${item.producto}* ya está ¡LISTO para retirar/entregar! ¡Muchas gracias!"
                                abrirWhatsApp(context, item.clienteTelefono, msg)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(verdeWhatsapp, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Avisar que está listo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (item.estado == "CANCELADO") {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { viewModel.eliminarPedidoDefinitivamente(item) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar pedido definitivamente", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            // CAMBIAR ESTADO DE PEDIDO (8.1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Estado:", fontSize = 11.sp, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("RECIBIDO", "EN_PRODUCCION", "LISTO", "ENTREGADO", "CANCELADO").forEach { e ->
                        val esActual = item.estado == e
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (esActual) (if (e == "CANCELADO") Color.Red else estados) else fondo2)
                                .clickable {
                                    if (e == "EN_PRODUCCION" && item.estadoDiseno != "APROBADO") {
                                        mostrarConfirmarProduccion = true
                                    } else if (e == "CANCELADO") {
                                        mostrarCancelarPedido = true
                                    } else {
                                        onCambiarEstado(e)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (e) {
                                    "RECIBIDO" -> "Recibido"
                                    "EN_PRODUCCION" -> "En Prod."
                                    "LISTO" -> "Listo"
                                    "ENTREGADO" -> "Entregado"
                                    "CANCELADO" -> "Cancelar"
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

data class VarianteArticulo(
    val id: String = java.util.UUID.randomUUID().toString(),
    var etiqueta: String = "", // Ej: "S", "Azul", "Diseño 1", "Para Ana"
    var cantidadTexto: String = "1",
    var textoEstampar: String = "", // Texto a estampar individual (opcional)
    var ajustePrecio: Double = 0.0 // Opcional +$1.00
)

data class ArticuloPedido(
    val id: String = java.util.UUID.randomUUID().toString(),
    var producto: String = "",
    var plantillaId: Int? = null,
    var cantidadTexto: String = "1",
    var precioUnitarioTexto: String = "",
    var precioManualEditado: Boolean = false,
    var detalle: String = "", // Color, modelo, observaciones del artículo
    var textoEstampar: String = "", // Texto o nombre general a estampar
    var notaItem: String = "", // Notas propias de este artículo
    var usarVariantes: Boolean = false,
    var variantes: MutableList<VarianteArticulo> = mutableListOf(),
    var fotosUris: MutableList<String> = mutableListOf(), // Fotos del diseño (hasta 4)
    // Costos reales arrastrados del cotizador para la radiografía
    var costoPiezaBaseUnitario: Double = 0.0,
    var nombrePiezaBase: String = "Insumo Base",
    var costoPasajeUnitario: Double = 0.0,
    var costoInsumosUnitario: Double = 0.0
) {
    fun obtenerCantidadTotal(): Int {
        return if (usarVariantes && variantes.isNotEmpty()) {
            variantes.sumOf { it.cantidadTexto.toIntOrNull() ?: 0 }
        } else {
            cantidadTexto.toIntOrNull() ?: 1
        }
    }

    fun obtenerSubtotal(): Double {
        return if (usarVariantes && variantes.isNotEmpty()) {
            val precioBase = precioUnitarioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
            variantes.sumOf { varItem ->
                val cant = varItem.cantidadTexto.toIntOrNull() ?: 0
                val precioVar = precioBase + varItem.ajustePrecio
                cant * precioVar
            }
        } else {
            val cant = cantidadTexto.toIntOrNull() ?: 1
            val precio = precioUnitarioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
            cant * precio
        }
    }

    fun obtenerPrecioUnitarioPromedio(): Double {
        val totalCant = obtenerCantidadTotal()
        if (totalCant <= 0) return 0.0
        return obtenerSubtotal() / totalCant
    }
}

@Composable
fun rememberBitmapFromUri(uriString: String): androidx.compose.ui.graphics.ImageBitmap? {
    val context = LocalContext.current
    return remember(uriString) {
        try {
            if (uriString.isBlank()) null
            else {
                val file = File(uriString)
                val bitmap = if (file.exists() && file.isFile) {
                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    val uri = if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                        Uri.parse(uriString)
                    } else {
                        Uri.fromFile(file)
                    }
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        android.graphics.BitmapFactory.decodeStream(inputStream)
                    }
                }
                bitmap?.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun SeccionPlegablePedido(
    titulo: String,
    resumen: String,
    expandido: Boolean,
    onToggleExpandir: () -> Unit,
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null,
    completado: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = fondo2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpandir() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (icono != null) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = estados,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = titulo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (completado) {
                                Text(
                                    text = "✓",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        if (!expandido && resumen.isNotBlank()) {
                            Text(
                                text = resumen,
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onToggleExpandir,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Plegar" else "Desplegar",
                        tint = Color.White
                    )
                }
            }

            if (expandido) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
fun IndicadorPrecioYGananciaArticulo(
    articulo: ArticuloPedido,
    plantillasCotizador: List<PlantillaCotizacion>,
    listaArticulos: List<ArticuloPedido> = emptyList(),
    tasaBcvGlobal: Double = 1.0,
    onRestablecerPrecioSugerido: (Double) -> Unit
) {
    val prodNombre = articulo.producto.trim()

    val plantillaAplica = plantillasCotizador.find { p ->
        p.id == articulo.plantillaId ||
        (prodNombre.isNotBlank() && (p.nombrePlantilla.contains(prodNombre, ignoreCase = true) || prodNombre.contains(p.nombrePlantilla, ignoreCase = true)))
    }

    val totalPiezasPlantilla = if (listaArticulos.isNotEmpty()) {
        listaArticulos.filter { art ->
            art.plantillaId == articulo.plantillaId ||
            (art.producto.isNotBlank() && prodNombre.isNotBlank() &&
                (art.producto.contains(prodNombre, ignoreCase = true) || prodNombre.contains(art.producto, ignoreCase = true)))
        }.sumOf { it.obtenerCantidadTotal() }.coerceAtLeast(articulo.obtenerCantidadTotal())
    } else {
        articulo.obtenerCantidadTotal()
    }

    val sugeridoVal = if (plantillaAplica != null && prodNombre.isNotBlank()) {
        com.example.gestor_deudores.data.utils.calcularCostosPlantilla(
            plantilla = plantillaAplica,
            tasaBcv = tasaBcvGlobal,
            cantidadPedido = totalPiezasPlantilla
        ).precioSugeridoUsd
    } else null

    val precioIngresado = articulo.precioUnitarioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
    val costoUnitarioTotal = articulo.costoPiezaBaseUnitario + articulo.costoPasajeUnitario + articulo.costoInsumosUnitario
    val gananciaPorPieza = precioIngresado - costoUnitarioTotal
    val porcentajeMargen = if (precioIngresado > 0) (gananciaPorPieza / precioIngresado) * 100.0 else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 1. Badge de Escala y Precio Sugerido
        if (sugeridoVal != null && plantillaAplica != null) {
            val minMayor = if (plantillaAplica.minimoUnidadesMayor > 0) plantillaAplica.minimoUnidadesMayor else 6
            val esMayorAplica = plantillaAplica.esPlantillaMayor || (plantillaAplica.porcentajeGananciaMayor > 0f && totalPiezasPlantilla >= minMayor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Precio sugerido $${String.format(java.util.Locale.US, "%.2f", sugeridoVal)} · ${if (esMayorAplica) "Tarifa al mayor (≥$minMayor pcs)" else "Tarifa al detal"}",
                    color = Color(0xFF1976D2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (articulo.precioManualEditado) {
                    TextButton(
                        onClick = { onRestablecerPrecioSugerido(sugeridoVal) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("↺ Usar sugerido", fontSize = 11.sp, color = estados, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Métrica de Ganancia o Alerta de Pérdida por Pieza (Sección 5)
        if (costoUnitarioTotal > 0 && precioIngresado > 0) {
            val esPerdida = precioIngresado < costoUnitarioTotal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (esPerdida) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (esPerdida) {
                            "Pierdes -${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(kotlin.math.abs(gananciaPorPieza))} / pc (Costo ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(costoUnitarioTotal)})"
                        } else {
                            "Ganas +${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(gananciaPorPieza)} / pc (Margen ${String.format(java.util.Locale.US, "%.0f", porcentajeMargen)}%)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esPerdida) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun DialogoExitoPedidoConWhatsApp(
    clienteNombre: String,
    clienteTelefono: String,
    mensajeWhatsApp: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Éxito",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(52.dp)
                )

                Text(
                    text = "¡Pedido Registrado con Éxito!",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Resumen para enviar a $clienteNombre por WhatsApp:",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = mensajeWhatsApp,
                        fontSize = 11.sp,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", color = Color.LightGray)
                    }

                    Button(
                        onClick = {
                            com.example.gestor_deudores.data.utils.abrirWhatsApp(context, clienteTelefono, mensajeWhatsApp)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = verdeWhatsapp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enviar por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoConfirmarBorrador(
    onGuardarBorrador: () -> Unit,
    onDescartar: () -> Unit,
    onSeguirEditando: () -> Unit
) {
    Dialog(onDismissRequest = onSeguirEditando) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "¿Guardar como borrador?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Tienes datos en el pedido. ¿Deseas guardarlo como borrador para continuar después?",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onGuardarBorrador,
                        colors = ButtonDefaults.buttonColors(containerColor = estados),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar Borrador", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDescartar) {
                            Text("Descartar", color = Color(0xFFFF5252))
                        }

                        TextButton(onClick = onSeguirEditando) {
                            Text("Seguir editando", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoCrearPedido(
    viewModel: PedidoViewModel,
    navController: NavController,
    tasaBcvGlobal: Double = 40.0,
    onDismiss: () -> Unit
) {
    val clientes by viewModel.listaClientes.collectAsState()
    val plantillasCotizador by viewModel.listaPlantillas.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var deudorSeleccionado by remember { mutableStateOf<Deudor?>(null) }
    var expandidoClientes by remember { mutableStateOf(false) }

    // Diálogos de Confirmación WhatsApp y Borrador (4.8 y 4.9)
    var mostrarDialogoExitoWhatsApp by remember { mutableStateOf(false) }
    var mensajeWhatsAppGenerado by remember { mutableStateOf("") }
    var clienteNombreWhatsApp by remember { mutableStateOf("") }
    var clienteTelfWhatsApp by remember { mutableStateOf("") }
    var mostrarDialogoBorradorConfirm by remember { mutableStateOf(false) }
    // --- NUEVA LISTA DINÁMICA DE ARTÍCULOS ---
    val listaArticulos = remember { mutableStateListOf(ArticuloPedido()) }

    var articuloAEliminarIndex by remember { mutableStateOf<Int?>(null) }

    if (articuloAEliminarIndex != null) {
        val artDel = listaArticulos.getOrNull(articuloAEliminarIndex!!)
        Dialog(onDismissRequest = { articuloAEliminarIndex = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = fondo2)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("¿Eliminar ${artDel?.producto?.ifBlank { "este artículo" }}?", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    if (artDel != null && artDel.fotosUris.isNotEmpty()) {
                        Text("Este artículo tiene ${artDel.fotosUris.size} foto(s) de diseño. Se quitarán del pedido.", fontSize = 12.sp, color = Color.LightGray)
                    } else {
                        Text("Se quitará este producto de la lista del pedido.", fontSize = 12.sp, color = Color.LightGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { articuloAEliminarIndex = null }) {
                            Text("Cancelar", color = Color.LightGray)
                        }

                        Button(
                            onClick = {
                                articuloAEliminarIndex?.let { idx ->
                                    if (idx in listaArticulos.indices) {
                                        listaArticulos.removeAt(idx)
                                    }
                                }
                                articuloAEliminarIndex = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Launchers de Galería y Cámara para fotos de diseño (Sección 6.1)
    val context = LocalContext.current
    var articuloIndexFotoActivo by remember { mutableStateOf<Int?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(4)
    ) { uris ->
        articuloIndexFotoActivo?.let { index ->
            if (index in listaArticulos.indices) {
                val art = listaArticulos[index]
                val fotosActuales = art.fotosUris.toMutableList()
                uris.forEach { u ->
                    if (fotosActuales.size < 4) {
                        fotosActuales.add(u.toString())
                    }
                }
                listaArticulos[index] = art.copy(fotosUris = fotosActuales)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            articuloIndexFotoActivo?.let { index ->
                if (index in listaArticulos.indices) {
                    val art = listaArticulos[index]
                    val fotosActuales = art.fotosUris.toMutableList()
                    if (fotosActuales.size < 4) {
                        fotosActuales.add(tempCameraUri.toString())
                        listaArticulos[index] = art.copy(fotosUris = fotosActuales)
                    }
                }
            }
        }
    }

    fun tomarFotoCamara(index: Int) {
        articuloIndexFotoActivo = index
        try {
            val tempFile = File.createTempFile("diseno_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seleccionarFotosGaleria(index: Int) {
        articuloIndexFotoActivo = index
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Control de secciones plegables
    var seccionClienteExpandida by remember { mutableStateOf(true) }
    var seccionProductosExpandida by remember { mutableStateOf(true) }

    // Variable de búsqueda
    var busquedaCliente by remember { mutableStateOf("") }
    
    // Lista filtrada en base a la búsqueda
    val clientesFiltrados = clientes.filter {
        it.nombre.contains(busquedaCliente, ignoreCase = true) ||
        it.apellido.contains(busquedaCliente, ignoreCase = true)
    }

    // Obtenemos deudas del cliente seleccionado en tiempo real
    val deudasCliente by remember(deudorSeleccionado?.id) {
        if (deudorSeleccionado != null) {
            viewModel.obtenerDeudasPorDeudor(deudorSeleccionado!!.id)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val estadoCobroCliente = remember(deudasCliente) {
        com.example.gestor_deudores.data.utils.calcularEstadoCobro(deudasCliente)
    }

    val resumenCliente = if (deudorSeleccionado != null) {
        val textoDeuda = if (estadoCobroCliente.saldo > 0) {
            val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(estadoCobroCliente.saldo)
            if (estadoCobroCliente.vencida) " · Debe $formatoUSD (Vencido)" else " · Debe $formatoUSD"
        } else {
            " · Al día"
        }
        "${deudorSeleccionado!!.nombre} ${deudorSeleccionado!!.apellido}$textoDeuda"
    } else {
        "Seleccionar cliente (Obligatorio)"
    }

    // Efecto para actualizar el texto si el usuario elige uno del menú
    LaunchedEffect(deudorSeleccionado) {
        if (deudorSeleccionado != null) {
            busquedaCliente = "${deudorSeleccionado!!.nombre} ${deudorSeleccionado!!.apellido}"
        }
    }

    // Control de sección 3 (Adicionales y Descuentos)
    var seccionExtrasExpandida by remember { mutableStateOf(false) }

    // Líneas extra (Sección 4.3)
    var descuentoTexto by remember { mutableStateOf("") }
    var esDescuentoPorcentaje by remember { mutableStateOf(false) } // false = $, true = %
    var cargoDisenoTexto by remember { mutableStateOf("") }
    var cargoEnvioTexto by remember { mutableStateOf("") }

    var abonoInicialTexto by remember { mutableStateOf("") }
    var frecuenciaSeleccionada by remember { mutableStateOf("AL_ENTREGAR") }
    var numCuotasTexto by remember { mutableStateOf("1") }
    var mostrarCalendario by remember { mutableStateOf(false) }
    var notas by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    // Control de Secciones 4, 5 y 6
    var seccionPagoExpandida by remember { mutableStateOf(false) }
    var seccionEntregaExpandida by remember { mutableStateOf(false) }
    var seccionNotasExpandida by remember { mutableStateOf(false) }

    // Abono y Pago (4.4)
    var esAbonoBs by remember { mutableStateOf(false) } // false = $, true = %
    var metodoPagoAbono by remember { mutableStateOf("Efectivo") }
    var referenciaAbono by remember { mutableStateOf("") }
    val tasaBcvActual = if (tasaBcvGlobal > 0.0) tasaBcvGlobal else 40.0

    // Saldo y Cuotas (4.4)
    var fechaPrimeraCuotaMillis by remember { mutableStateOf(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)) }
    var mostrarCalendarioCuota by remember { mutableStateOf(false) }
    val datePickerStateCuota = rememberDatePickerState(initialSelectedDateMillis = fechaPrimeraCuotaMillis)

    // Entrega (4.5)
    var tipoEntrega by remember { mutableStateOf("RETIRO") } // RETIRO vs DELIVERY
    var direccionEntrega by remember { mutableStateOf("") }
    var esUrgente by remember { mutableStateOf(false) }

    // Fecha de entrega obligatoria (por defecto 3 días a futuro)
    val tresDiasFuturo = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L)
    var fechaEntregaMillis by remember { mutableStateOf<Long?>(tresDiasFuturo) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tresDiasFuturo)

    // Restablecer Borrador guardado si existe
    val borradorActual by viewModel.borradorActual.collectAsState()

    LaunchedEffect(Unit) {
        if (borradorActual != null) {
            val b = borradorActual!!
            if (b.deudorId != null) {
                deudorSeleccionado = clientes.find { it.id == b.deudorId }
            }
            if (b.busquedaCliente.isNotBlank()) busquedaCliente = b.busquedaCliente
            if (b.abonoInicialTexto.isNotBlank()) abonoInicialTexto = b.abonoInicialTexto
            if (b.frecuenciaSeleccionada.isNotBlank()) frecuenciaSeleccionada = b.frecuenciaSeleccionada
            if (b.numCuotasTexto.isNotBlank()) numCuotasTexto = b.numCuotasTexto
            if (b.fechaEntregaMillis != null) fechaEntregaMillis = b.fechaEntregaMillis
            if (b.notas.isNotBlank()) notas = b.notas
            if (b.descuentoTexto.isNotBlank()) descuentoTexto = b.descuentoTexto
            esDescuentoPorcentaje = b.esDescuentoPorcentaje
            if (b.cargoDisenoTexto.isNotBlank()) cargoDisenoTexto = b.cargoDisenoTexto
            if (b.cargoEnvioTexto.isNotBlank()) cargoEnvioTexto = b.cargoEnvioTexto
            if (b.tipoEntrega.isNotBlank()) tipoEntrega = b.tipoEntrega
            if (b.direccionEntrega.isNotBlank()) direccionEntrega = b.direccionEntrega
            esUrgente = b.esUrgente

            if (b.listaArticulosJson.isNotBlank()) {
                try {
                    val type = object : com.google.gson.reflect.TypeToken<List<ArticuloPedido>>() {}.type
                    val arts: List<ArticuloPedido> = com.google.gson.Gson().fromJson(b.listaArticulosJson, type) ?: emptyList()
                    if (arts.isNotEmpty()) {
                        listaArticulos.clear()
                        listaArticulos.addAll(arts)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Cálculos de la Sección 4.3
    fun calcularSubtotalArticulos(): Double = listaArticulos.sumOf { it.obtenerSubtotal() }

    fun calcularMontoDescuento(): Double {
        val subtotal = calcularSubtotalArticulos()
        val valDesc = descuentoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
        return if (esDescuentoPorcentaje) {
            (subtotal * valDesc / 100.0).coerceAtMost(subtotal)
        } else {
            valDesc.coerceAtMost(subtotal)
        }
    }

    fun calcularMontoDiseno(): Double = cargoDisenoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0

    fun calcularMontoEnvio(): Double = cargoEnvioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0

    fun calcularTotalGeneral(): Double {
        val subtotal = calcularSubtotalArticulos()
        val desc = calcularMontoDescuento()
        val diseno = calcularMontoDiseno()
        val envio = calcularMontoEnvio()
        val total = (subtotal - desc) + diseno + envio
        return if (total < 0) 0.0 else total
    }

    val subtotalArticulos = calcularSubtotalArticulos()
    val montoDescuentoCalculado = calcularMontoDescuento()
    val montoDisenoCalculado = calcularMontoDiseno()
    val montoEnvioCalculado = calcularMontoEnvio()
    val totalUsd = calcularTotalGeneral()

    val totalPiezasGral = listaArticulos.sumOf { it.obtenerCantidadTotal() }
    val resumenProductos = "${listaArticulos.size} prod · $totalPiezasGral pcs · Subtotal ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(subtotalArticulos)}"

    val resumenExtras = if (montoDescuentoCalculado > 0 || montoDisenoCalculado > 0 || montoEnvioCalculado > 0) {
        val partes = mutableListOf<String>()
        if (montoDescuentoCalculado > 0) partes.add("Desc -$${String.format(java.util.Locale.US, "%.2f", montoDescuentoCalculado)}")
        if (montoDisenoCalculado > 0) partes.add("Diseño +$${String.format(java.util.Locale.US, "%.2f", montoDisenoCalculado)}")
        if (montoEnvioCalculado > 0) partes.add("Envío +$${String.format(java.util.Locale.US, "%.2f", montoEnvioCalculado)}")
        partes.joinToString(" · ")
    } else {
        "Sin cargos extra ni descuentos"
    }

    // Cálculos de Abono y Saldo
    val abonoInputVal = abonoInicialTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
    val abonoCalculadoUsd = if (esAbonoBs && tasaBcvActual > 0) abonoInputVal / tasaBcvActual else abonoInputVal
    val saldoRestanteCalculado = (totalUsd - abonoCalculadoUsd).coerceAtLeast(0.0)

    val costoMateriaPrimaTotal = listaArticulos.sumOf { (it.costoPiezaBaseUnitario + it.costoPasajeUnitario + it.costoInsumosUnitario) * it.obtenerCantidadTotal() }
    val gananciaEstimadaUsd = (totalUsd - costoMateriaPrimaTotal).coerceAtLeast(0.0)
    var mostrarGananciaEstimada by remember { mutableStateOf(false) }

    val abonoInicial = abonoCalculadoUsd

    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaEntregaMillis = millis
                    }
                    mostrarCalendario = false
                }) {
                    Text("Aceptar", color = estados)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // CABECERA DEL DIÁLOGO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NUEVO PEDIDO", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = estados)
                    IconButton(onClick = {
                        if (deudorSeleccionado != null || listaArticulos.any { it.producto.isNotBlank() }) {
                            mostrarDialogoBorradorConfirm = true
                        } else {
                            onDismiss()
                        }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                // ÁREA DE FORMULARIO CON SCROLL
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                // 1. SECCIÓN CLIENTE (SECCIÓN 4.1)
                item {
                    SeccionPlegablePedido(
                        titulo = "1. CLIENTE",
                        resumen = resumenCliente,
                        expandido = seccionClienteExpandida,
                        onToggleExpandir = { seccionClienteExpandida = !seccionClienteExpandida },
                        icono = Icons.Default.Person,
                        completado = deudorSeleccionado != null
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Cliente", fontSize = 12.sp, color = Color.Gray)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = expandidoClientes,
                                    onExpandedChange = { expandidoClientes = !expandidoClientes },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = busquedaCliente,
                                        onValueChange = { 
                                            busquedaCliente = it
                                            if (deudorSeleccionado != null && "${deudorSeleccionado!!.nombre} ${deudorSeleccionado!!.apellido}" != it) {
                                                deudorSeleccionado = null
                                            }
                                            expandidoClientes = true
                                        },
                                        placeholder = { Text("Buscar cliente...") },
                                        readOnly = false,
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    if (clientesFiltrados.isNotEmpty()) {
                                        ExposedDropdownMenu(
                                            expanded = expandidoClientes,
                                            onDismissRequest = { expandidoClientes = false }
                                        ) {
                                            clientesFiltrados.forEach { cliente ->
                                                DropdownMenuItem(
                                                    text = { Text("${cliente.nombre} ${cliente.apellido}") },
                                                    onClick = {
                                                        deudorSeleccionado = cliente
                                                        busquedaCliente = "${cliente.nombre} ${cliente.apellido}"
                                                        expandidoClientes = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { navController.navigate(rutas.REGISTRO_EXPRESS) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = fondo2)
                                ) {
                                    Text("+ Nuevo", color = Color.White)
                                }
                            }

                            // LÍNEA / BANNER INFORMATIVO DEL DEUDOR (4.1)
                            if (deudorSeleccionado != null) {
                                val saldoFormatted = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(estadoCobroCliente.saldo)
                                val esVencido = estadoCobroCliente.vencida

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (esVencido) Color(0xFFFFEBEE) 
                                                         else if (estadoCobroCliente.saldo > 0) Color(0xFFFFF8E1) 
                                                         else Color(0xFFE8F5E9)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (esVencido) Icons.Default.Warning 
                                                         else if (estadoCobroCliente.saldo > 0) Icons.Default.Info 
                                                         else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (esVencido) Color(0xFFD32F2F) 
                                                   else if (estadoCobroCliente.saldo > 0) Color(0xFFF57F17) 
                                                   else Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (estadoCobroCliente.saldo > 0) {
                                                if (esVencido) {
                                                    "Este cliente debe $saldoFormatted · ${estadoCobroCliente.diasAtraso} días de atraso (Cuota vencida)"
                                                } else {
                                                    "Este cliente debe $saldoFormatted · Al día"
                                                }
                                            } else {
                                                "Cliente al día (sin saldo pendiente)"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (esVencido) Color(0xFFD32F2F) 
                                                    else if (estadoCobroCliente.saldo > 0) Color(0xFFF57F17) 
                                                    else Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. SECCIÓN PRODUCTOS / ARTÍCULOS (SECCIONES 4.2 Y 4.2.1)
                item {
                    SeccionPlegablePedido(
                        titulo = "2. ARTÍCULOS Y PRODUCTOS",
                        resumen = resumenProductos,
                        expandido = seccionProductosExpandida,
                        onToggleExpandir = { seccionProductosExpandida = !seccionProductosExpandida },
                        icono = Icons.Default.ShoppingCart,
                        completado = listaArticulos.isNotEmpty() && listaArticulos.none { it.producto.isBlank() }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listaArticulos.forEachIndexed { index, articulo ->
                                var expandidoProductos by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = fondo_claro),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // CABECERA DEL ARTÍCULO: Nombre + Botones Duplicar y Eliminar
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (articulo.producto.isNotBlank()) "Artículo ${index + 1}: ${articulo.producto}" else "Artículo ${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                color = estados,
                                                fontSize = 14.sp
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                // Botón Duplicar (4.2)
                                                IconButton(
                                                    onClick = {
                                                        val duplicado = articulo.copy(
                                                            id = java.util.UUID.randomUUID().toString(),
                                                            variantes = articulo.variantes.map { it.copy(id = java.util.UUID.randomUUID().toString()) }.toMutableList()
                                                        )
                                                        listaArticulos.add(index + 1, duplicado)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Text("📋", fontSize = 14.sp)
                                                }

                                                // Botón Eliminar (4.2 y Sección 10)
                                                if (listaArticulos.size > 1) {
                                                    IconButton(
                                                        onClick = {
                                                            if (articulo.producto.isNotBlank() || articulo.fotosUris.isNotEmpty()) {
                                                                articuloAEliminarIndex = index
                                                            } else {
                                                                listaArticulos.removeAt(index)
                                                            }
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Eliminar",
                                                            tint = Color.Red,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // BUSCADOR DE PLANTILLA O PRODUCTO LIBRE
                                        ExposedDropdownMenuBox(
                                            expanded = expandidoProductos,
                                            onExpandedChange = { expandidoProductos = !expandidoProductos }
                                        ) {
                                            OutlinedTextField(
                                                value = articulo.producto,
                                                onValueChange = { nuevoProd ->
                                                    listaArticulos[index] = articulo.copy(
                                                        producto = nuevoProd,
                                                        plantillaId = null,
                                                        costoPiezaBaseUnitario = 0.0,
                                                        costoPasajeUnitario = 0.0,
                                                        costoInsumosUnitario = 0.0,
                                                        nombrePiezaBase = "Insumo Base"
                                                    )
                                                },
                                                placeholder = { Text("Ej. Taza Mágica, Franela DTF, Cuadro") },
                                                label = { Text("Producto (Plantilla o Producto libre)") },
                                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            val sugFiltradas = plantillasCotizador.filter {
                                                it.nombrePlantilla.contains(articulo.producto, ignoreCase = true)
                                            }
                                            if (sugFiltradas.isNotEmpty()) {
                                                ExposedDropdownMenu(
                                                    expanded = expandidoProductos,
                                                    onDismissRequest = { expandidoProductos = false }
                                                ) {
                                                    sugFiltradas.forEach { plantilla ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Column {
                                                                    Text(plantilla.nombrePlantilla, fontWeight = FontWeight.Bold)
                                                                    val cantTotalActual = articulo.obtenerCantidadTotal().coerceAtLeast(1)
                                                                    val sugerido = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(
                                                                        plantilla = plantilla,
                                                                        tasaBcv = tasaBcvGlobal,
                                                                        cantidadPedido = cantTotalActual
                                                                    ).precioSugeridoUsd
                                                                    Text("Sugerido: $${String.format(java.util.Locale.US, "%.2f", sugerido)}", fontSize = 10.sp, color = Color.Gray)
                                                                }
                                                            },
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    val cantTotalActual = articulo.obtenerCantidadTotal().coerceAtLeast(1)
                                                                    val costosDesglosados = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(
                                                                        plantilla = plantilla,
                                                                        tasaBcv = tasaBcvGlobal,
                                                                        cantidadPedido = cantTotalActual
                                                                    )
                                                                    val atributosConOpciones = viewModel.obtenerAtributosConOpciones(plantilla.id)

                                                                    val atrReparte = atributosConOpciones.find { it.atributo.tipo == "REPARTE_CANTIDAD" }
                                                                    val usarVars = atrReparte != null && atrReparte.opciones.isNotEmpty()
                                                                    val varsCalculadas = if (atrReparte != null && atrReparte.opciones.isNotEmpty()) {
                                                                        atrReparte.opciones.map { op ->
                                                                            VarianteArticulo(
                                                                                etiqueta = op.etiqueta,
                                                                                cantidadTexto = "0",
                                                                                ajustePrecio = op.ajustePrecio
                                                                            )
                                                                        }.toMutableList()
                                                                    } else {
                                                                        articulo.variantes
                                                                    }

                                                                    listaArticulos[index] = articulo.copy(
                                                                        producto = plantilla.nombrePlantilla,
                                                                        plantillaId = plantilla.id,
                                                                        precioUnitarioTexto = String.format(java.util.Locale.US, "%.2f", costosDesglosados.precioSugeridoUsd),
                                                                        precioManualEditado = false,
                                                                        usarVariantes = usarVars,
                                                                        variantes = varsCalculadas,
                                                                        costoPiezaBaseUnitario = costosDesglosados.costoPiezaBaseUsd,
                                                                        nombrePiezaBase = plantilla.nombrePlantilla,
                                                                        costoPasajeUnitario = costosDesglosados.costoPasajeUsd,
                                                                        costoInsumosUnitario = costosDesglosados.costoInsumosUsd
                                                                    )
                                                                    expandidoProductos = false
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // CANTIDAD Y PRECIO UNITARIO
                                        if (!articulo.usarVariantes) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = articulo.cantidadTexto,
                                                    onValueChange = { inputCant ->
                                                        if (inputCant.all { c -> c.isDigit() }) {
                                                            val cantInt = inputCant.toIntOrNull() ?: 1
                                                            val prodNombre = articulo.producto.trim()

                                                            val plantillaAplica = plantillasCotizador.find { p ->
                                                                p.id == articulo.plantillaId ||
                                                                (prodNombre.isNotBlank() && (p.nombrePlantilla.contains(prodNombre, ignoreCase = true) || prodNombre.contains(p.nombrePlantilla, ignoreCase = true)))
                                                            }

                                                            val artAux = articulo.copy(cantidadTexto = inputCant)
                                                            val listAux = listaArticulos.toMutableList()
                                                            listAux[index] = artAux

                                                            if (plantillaAplica != null && prodNombre.isNotBlank()) {
                                                                val totalPiezasPlantilla = listAux.filter { art ->
                                                                    art.plantillaId == plantillaAplica.id ||
                                                                    (art.producto.isNotBlank() && (art.producto.contains(prodNombre, ignoreCase = true) || prodNombre.contains(art.producto, ignoreCase = true)))
                                                                }.sumOf { it.obtenerCantidadTotal() }.coerceAtLeast(cantInt)

                                                                val costos = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(
                                                                    plantilla = plantillaAplica,
                                                                    tasaBcv = tasaBcvGlobal,
                                                                    cantidadPedido = totalPiezasPlantilla
                                                                )

                                                                listaArticulos[index] = artAux.copy(
                                                                    precioUnitarioTexto = if (!articulo.precioManualEditado) String.format(java.util.Locale.US, "%.2f", costos.precioSugeridoUsd) else articulo.precioUnitarioTexto,
                                                                    costoPiezaBaseUnitario = costos.costoPiezaBaseUsd,
                                                                    nombrePiezaBase = plantillaAplica.nombrePlantilla,
                                                                    costoPasajeUnitario = costos.costoPasajeUsd,
                                                                    costoInsumosUnitario = costos.costoInsumosUsd
                                                                )

                                                                listAux.forEachIndexed { i, a ->
                                                                    if (i != index && (a.plantillaId == plantillaAplica.id || (a.producto.isNotBlank() && (a.producto.contains(prodNombre, ignoreCase = true) || prodNombre.contains(a.producto, ignoreCase = true)))) && !a.precioManualEditado) {
                                                                        listaArticulos[i] = a.copy(
                                                                            precioUnitarioTexto = String.format(java.util.Locale.US, "%.2f", costos.precioSugeridoUsd),
                                                                            costoPiezaBaseUnitario = costos.costoPiezaBaseUsd,
                                                                            costoPasajeUnitario = costos.costoPasajeUsd,
                                                                            costoInsumosUnitario = costos.costoInsumosUsd
                                                                        )
                                                                    }
                                                                }
                                                            } else {
                                                                listaArticulos[index] = artAux
                                                            }
                                                        }
                                                    },
                                                    label = { Text("Cant.") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )

                                                OutlinedTextField(
                                                    value = articulo.precioUnitarioTexto,
                                                    onValueChange = { nuevoPrecio ->
                                                        listaArticulos[index] = articulo.copy(
                                                            precioUnitarioTexto = nuevoPrecio,
                                                            precioManualEditado = true
                                                        )
                                                    },
                                                    label = { Text("Precio Un. ($)") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }
                                        } else {
                                            // SI USA VARIANTES: Cantidad calculada automáticamente
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = fondo2),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text("Cant. Total", fontSize = 10.sp, color = Color.Gray)
                                                        Text("${articulo.obtenerCantidadTotal()} pcs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }

                                                OutlinedTextField(
                                                    value = articulo.precioUnitarioTexto,
                                                    onValueChange = { nuevoPrecio ->
                                                        listaArticulos[index] = articulo.copy(
                                                            precioUnitarioTexto = nuevoPrecio,
                                                            precioManualEditado = true
                                                        )
                                                    },
                                                    label = { Text("Precio Base Un. ($)") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }
                                        }

                                        // INDICADOR DE PRECIO SUGERIDO, ESCALA Y GANANCIA POR PIEZA (SECCIÓN 5)
                                        IndicadorPrecioYGananciaArticulo(
                                            articulo = articulo,
                                            plantillasCotizador = plantillasCotizador,
                                            listaArticulos = listaArticulos,
                                            tasaBcvGlobal = tasaBcvGlobal,
                                            onRestablecerPrecioSugerido = { precioSug ->
                                                listaArticulos[index] = articulo.copy(
                                                    precioUnitarioTexto = String.format(java.util.Locale.US, "%.2f", precioSug),
                                                    precioManualEditado = false
                                                )
                                            }
                                        )

                                        // BOTÓN Y DESGLOSE DE VARIANTES LIBRES (4.2.1)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    val nuevoEstadoVars = !articulo.usarVariantes
                                                    val varsActuales = if (nuevoEstadoVars && articulo.variantes.isEmpty()) {
                                                        mutableListOf(VarianteArticulo(etiqueta = "Variante 1", cantidadTexto = "1"))
                                                    } else articulo.variantes

                                                    listaArticulos[index] = articulo.copy(
                                                        usarVariantes = nuevoEstadoVars,
                                                        variantes = varsActuales
                                                    )
                                                },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = if (articulo.usarVariantes) "✓ Quitar variantes" else "+ Variantes / Tallas",
                                                    color = if (articulo.usarVariantes) Color(0xFF2E7D32) else estados,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "Subtotal: ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(articulo.obtenerSubtotal())}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = componentes,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        if (articulo.usarVariantes) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = fondo2.copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Desglose de Variantes",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )

                                                    articulo.variantes.forEachIndexed { vIndex, variante ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.cardColors(containerColor = fondo_claro),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.padding(8.dp),
                                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                // Fila 1: Variante + Cantidad + Eliminar
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    OutlinedTextField(
                                                                        value = variante.etiqueta,
                                                                        onValueChange = { nuevaEtiqueta ->
                                                                            val listAux = articulo.variantes.toMutableList()
                                                                            listAux[vIndex] = variante.copy(etiqueta = nuevaEtiqueta)
                                                                            listaArticulos[index] = articulo.copy(variantes = listAux)
                                                                        },
                                                                        label = { Text("Variante", fontSize = 11.sp) },
                                                                        placeholder = { Text("Ej. S, M, Azul, Modelo 1", fontSize = 11.sp) },
                                                                        singleLine = true,
                                                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                                                        modifier = Modifier.weight(1f),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )

                                                                    OutlinedTextField(
                                                                        value = variante.cantidadTexto,
                                                                        onValueChange = { nuevaCant ->
                                                                            if (nuevaCant.all { c -> c.isDigit() }) {
                                                                                val listAuxVars = articulo.variantes.toMutableList()
                                                                                listAuxVars[vIndex] = variante.copy(cantidadTexto = nuevaCant)
                                                                                val artAux = articulo.copy(variantes = listAuxVars)

                                                                                val prodNombre = artAux.producto.trim()
                                                                                val plantillaAplica = plantillasCotizador.find { p ->
                                                                                    p.id == artAux.plantillaId ||
                                                                                    (prodNombre.isNotBlank() && (p.nombrePlantilla.contains(prodNombre, ignoreCase = true) || prodNombre.contains(p.nombrePlantilla, ignoreCase = true)))
                                                                                }

                                                                                if (plantillaAplica != null && prodNombre.isNotBlank()) {
                                                                                    val listAuxArts = listaArticulos.toMutableList()
                                                                                    listAuxArts[index] = artAux

                                                                                    val totalPiezasPlantilla = listAuxArts.filter { art ->
                                                                                        art.plantillaId == plantillaAplica.id ||
                                                                                        (art.producto.isNotBlank() && (art.producto.contains(prodNombre, ignoreCase = true) || prodNombre.contains(art.producto, ignoreCase = true)))
                                                                                    }.sumOf { it.obtenerCantidadTotal() }.coerceAtLeast(1)

                                                                                    val costos = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(
                                                                                        plantilla = plantillaAplica,
                                                                                        tasaBcv = tasaBcvGlobal,
                                                                                        cantidadPedido = totalPiezasPlantilla
                                                                                    )

                                                                                    listaArticulos[index] = artAux.copy(
                                                                                        precioUnitarioTexto = if (!artAux.precioManualEditado) String.format(java.util.Locale.US, "%.2f", costos.precioSugeridoUsd) else artAux.precioUnitarioTexto,
                                                                                        costoPiezaBaseUnitario = costos.costoPiezaBaseUsd,
                                                                                        nombrePiezaBase = plantillaAplica.nombrePlantilla,
                                                                                        costoPasajeUnitario = costos.costoPasajeUsd,
                                                                                        costoInsumosUnitario = costos.costoInsumosUsd
                                                                                    )

                                                                                    listAuxArts.forEachIndexed { i, a ->
                                                                                        if (i != index && (a.plantillaId == plantillaAplica.id || (a.producto.isNotBlank() && (a.producto.contains(prodNombre, ignoreCase = true) || prodNombre.contains(a.producto, ignoreCase = true)))) && !a.precioManualEditado) {
                                                                                            listaArticulos[i] = a.copy(
                                                                                                precioUnitarioTexto = String.format(java.util.Locale.US, "%.2f", costos.precioSugeridoUsd),
                                                                                                costoPiezaBaseUnitario = costos.costoPiezaBaseUsd,
                                                                                                costoPasajeUnitario = costos.costoPasajeUsd,
                                                                                                costoInsumosUnitario = costos.costoInsumosUsd
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    listaArticulos[index] = artAux
                                                                                }
                                                                            }
                                                                        },
                                                                        label = { Text("Cant.", fontSize = 11.sp) },
                                                                        singleLine = true,
                                                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                                        modifier = Modifier.width(60.dp),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )

                                                                    if (articulo.variantes.size > 1) {
                                                                        IconButton(
                                                                            onClick = {
                                                                                val listAux = articulo.variantes.toMutableList()
                                                                                listAux.removeAt(vIndex)
                                                                                listaArticulos[index] = articulo.copy(variantes = listAux)
                                                                            },
                                                                            modifier = Modifier.size(32.dp)
                                                                        ) {
                                                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(18.dp))
                                                                        }
                                                                    }
                                                                }

                                                                // Fila 2: Texto o Nombre a estampar en esta pieza
                                                                OutlinedTextField(
                                                                    value = variante.textoEstampar,
                                                                    onValueChange = { nuevoTexto ->
                                                                        val listAux = articulo.variantes.toMutableList()
                                                                        listAux[vIndex] = variante.copy(textoEstampar = nuevoTexto)
                                                                        listaArticulos[index] = articulo.copy(variantes = listAux)
                                                                    },
                                                                    label = { Text("Texto a estampar", fontSize = 11.sp) },
                                                                    placeholder = { Text("Nombre o frase (Opcional)", fontSize = 11.sp) },
                                                                    singleLine = true,
                                                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    shape = RoundedCornerShape(8.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    TextButton(
                                                        onClick = {
                                                            val listAux = articulo.variantes.toMutableList()
                                                            listAux.add(VarianteArticulo(etiqueta = "Variante ${listAux.size + 1}", cantidadTexto = "1"))
                                                            listaArticulos[index] = articulo.copy(variantes = listAux)
                                                        }
                                                    ) {
                                                        Text("+ Agregar otra variante", fontSize = 11.sp, color = estados, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        // CAMPOS OPCIONALES DEL ARTÍCULO (4.2)
                                        OutlinedTextField(
                                            value = articulo.detalle,
                                            onValueChange = { nuevoDetalle ->
                                                listaArticulos[index] = articulo.copy(detalle = nuevoDetalle)
                                            },
                                            placeholder = { Text("Ej. Franela de algodón, manga corta, azul marino") },
                                            label = { Text("Detalle / Color / Modelo (Opcional)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        OutlinedTextField(
                                            value = articulo.textoEstampar,
                                            onValueChange = { nuevoTexto ->
                                                listaArticulos[index] = articulo.copy(textoEstampar = nuevoTexto)
                                            },
                                            placeholder = { Text("Ej. Frase: 'Súper Papá #1'") },
                                            label = { Text("Texto o nombre a estampar (Opcional)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        OutlinedTextField(
                                            value = articulo.notaItem,
                                            onValueChange = { nuevaNota ->
                                                listaArticulos[index] = articulo.copy(notaItem = nuevaNota)
                                            },
                                            placeholder = { Text("Ej. Empaque individual con cinta de regalo") },
                                            label = { Text("Nota del artículo (Opcional)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // SECCIÓN FOTOS DEL DISEÑO DEL CLIENTE (SECCIÓN 6.1)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(fondo2.copy(alpha = 0.3f))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Fotos del Diseño (${articulo.fotosUris.size}/4)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )

                                                if (articulo.fotosUris.size < 4) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        TextButton(
                                                            onClick = { seleccionarFotosGaleria(index) },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("Galería", fontSize = 11.sp, color = estados, fontWeight = FontWeight.Bold)
                                                        }

                                                        TextButton(
                                                            onClick = { tomarFotoCamara(index) },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("Cámara", fontSize = 11.sp, color = estados, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                } else {
                                                    Text("Máximo 4 fotos", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            }

                                            if (articulo.fotosUris.isNotEmpty()) {
                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    items(articulo.fotosUris.size) { fIdx ->
                                                        val uriStr = articulo.fotosUris[fIdx]
                                                        val bitmap = rememberBitmapFromUri(uriStr)

                                                        Box(
                                                            modifier = Modifier
                                                                .size(60.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color.DarkGray)
                                                        ) {
                                                            if (bitmap != null) {
                                                                Image(
                                                                    bitmap = bitmap,
                                                                    contentDescription = "Foto diseño ${fIdx + 1}",
                                                                    contentScale = ContentScale.Crop,
                                                                    modifier = Modifier.fillMaxSize()
                                                                )
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    val listFotos = articulo.fotosUris.toMutableList()
                                                                    listFotos.removeAt(fIdx)
                                                                    listaArticulos[index] = articulo.copy(fotosUris = listFotos)
                                                                },
                                                                modifier = Modifier
                                                                    .size(20.dp)
                                                                    .align(Alignment.TopEnd)
                                                                    .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Eliminar foto",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // BOTÓN + AGREGAR OTRO PRODUCTO
                            TextButton(
                                onClick = { listaArticulos.add(ArticuloPedido()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Agregar otro producto", color = estados, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. SECCIÓN LÍNEAS EXTRA DEL PEDIDO (SECCIÓN 4.3)
                item {
                    SeccionPlegablePedido(
                        titulo = "3. LÍNEAS EXTRA / DESCUENTO",
                        resumen = resumenExtras,
                        expandido = seccionExtrasExpandida,
                        onToggleExpandir = { seccionExtrasExpandida = !seccionExtrasExpandida },
                        icono = Icons.Default.Star,
                        completado = montoDescuentoCalculado > 0 || montoDisenoCalculado > 0 || montoEnvioCalculado > 0
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 1. DESCUENTO
                            Text("Descuento del Pedido", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = descuentoTexto,
                                    onValueChange = { descuentoTexto = it },
                                    label = { Text(if (esDescuentoPorcentaje) "Descuento (%)" else "Descuento ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Selector $ vs %
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(fondo_claro)
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (!esDescuentoPorcentaje) estados else Color.Transparent)
                                            .clickable { esDescuentoPorcentaje = false }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("$", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!esDescuentoPorcentaje) Color.White else Color.Gray)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (esDescuentoPorcentaje) estados else Color.Transparent)
                                            .clickable { esDescuentoPorcentaje = true }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (esDescuentoPorcentaje) Color.White else Color.Gray)
                                    }
                                }
                            }

                            if (montoDescuentoCalculado > 0) {
                                Text(
                                    text = "Descuento aplicado: -$${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(montoDescuentoCalculado)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }

                            // 2. DISEÑO PERSONALIZADO
                            OutlinedTextField(
                                value = cargoDisenoTexto,
                                onValueChange = { cargoDisenoTexto = it },
                                label = { Text("Diseño personalizado / Vectorización ($)") },
                                placeholder = { Text("Ej. 5.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 3. ENVÍO / DELIVERY
                            OutlinedTextField(
                                value = cargoEnvioTexto,
                                onValueChange = { cargoEnvioTexto = it },
                                label = { Text("Envío / Delivery ($)") },
                                placeholder = { Text("Ej. 3.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // RESUMEN DE DESGLOSE DE LÍNEAS EXTRA
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = fondo_claro),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Subtotal Productos:", fontSize = 12.sp, color = Color.DarkGray)
                                        Text("$${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(subtotalArticulos)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    }
                                    if (montoDescuentoCalculado > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Descuento:", fontSize = 12.sp, color = Color(0xFF2E7D32))
                                            Text("-$${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(montoDescuentoCalculado)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                    }
                                    if (montoDisenoCalculado > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Diseño Personalizado:", fontSize = 12.sp, color = Color.DarkGray)
                                            Text("+$${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(montoDisenoCalculado)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                    }
                                    if (montoEnvioCalculado > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Envío / Delivery:", fontSize = 12.sp, color = Color.DarkGray)
                                            Text("+$${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(montoEnvioCalculado)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("TOTAL PEDIDO:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = estados)
                                        Text("$${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(totalUsd)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = estados)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. SECCIÓN PAGO Y ABONO INICIAL (SECCIÓN 4.4)
                item {
                    val resumenPago = "Abono $${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(abonoCalculadoUsd)} · Saldo $${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(saldoRestanteCalculado)} (${if (frecuenciaSeleccionada == "AL_ENTREGAR") "Al retirar" else "$numCuotasTexto cuotas"})"

                    SeccionPlegablePedido(
                        titulo = "4. PAGO Y ABONO INICIAL",
                        resumen = resumenPago,
                        expandido = seccionPagoExpandida,
                        onToggleExpandir = { seccionPagoExpandida = !seccionPagoExpandida },
                        icono = Icons.Default.ShoppingCart,
                        completado = abonoCalculadoUsd > 0 || totalUsd > 0
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // LÍNEA INFORMATIVA DE COBERTURA DE MATERIA PRIMA (4.4)
                            if (costoMateriaPrimaTotal > 0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = fondo_claro),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Con $${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(costoMateriaPrimaTotal)} de abono cubres la materia prima de este pedido",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF57F17)
                                        )
                                    }
                                }
                            }

                            // BOTÓN PEDIR 50% DE ABONO
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Abono / Adelanto inicial", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                TextButton(
                                    onClick = {
                                        val mitad = totalUsd * 0.5
                                        val montoAjustado = if (esAbonoBs && tasaBcvActual > 0) mitad * tasaBcvActual else mitad
                                        abonoInicialTexto = String.format(java.util.Locale.US, "%.2f", montoAjustado)
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Pedir 50% ($${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(totalUsd * 0.5)})", fontSize = 11.sp, color = estados, fontWeight = FontWeight.Bold)
                                }
                            }

                            // INPUT MONTO DE ABONO + TOGGLE USD / BS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = abonoInicialTexto,
                                    onValueChange = { abonoInicialTexto = it },
                                    label = { Text(if (esAbonoBs) "Abono (Bs)" else "Abono ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Selector USD vs Bs
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(fondo_claro)
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (!esAbonoBs) estados else Color.Transparent)
                                            .clickable { esAbonoBs = false }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("$ USD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!esAbonoBs) Color.White else Color.Gray)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (esAbonoBs) estados else Color.Transparent)
                                            .clickable { esAbonoBs = true }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("Bs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (esAbonoBs) Color.White else Color.Gray)
                                    }
                                }
                            }

                            if (esAbonoBs && abonoCalculadoUsd > 0) {
                                Text(
                                    text = "Equivalente: $${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(abonoCalculadoUsd)} USD (Tasa Bs ${tasaBcvActual})",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            // MÉTODO DE PAGO Y REFERENCIA
                            Text("Método de pago del abono:", fontSize = 11.sp, color = Color.Gray)
                            val metodosDisponibles = listOf("Efectivo", "Transferencia", "Pago Móvil", "Zinli", "Binance", "Otro")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(metodosDisponibles) { m ->
                                    val sel = metodoPagoAbono == m
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (sel) estados else fondo_claro)
                                            .clickable { metodoPagoAbono = m }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(m, fontSize = 11.sp, color = if (sel) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = referenciaAbono,
                                onValueChange = { referenciaAbono = it },
                                label = { Text("Referencia / N° Comprobante (Opcional)") },
                                placeholder = { Text("Ej. Ref. 987654") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // FORMA DE PAGO DEL SALDO RESTANTE (CHIPS MULTILÍNEA)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Forma de pago del saldo restante:", fontSize = 11.sp, color = Color.Gray)
                            val opcionesFrecuencia = listOf(
                                "AL_ENTREGAR" to "Al retirar / entregar",
                                "SEMANAL" to "Semanal",
                                "QUINCENAL" to "Quincenal",
                                "MENSUAL" to "Mensual"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    opcionesFrecuencia.take(2).forEach { (clave, label) ->
                                        val sel = frecuenciaSeleccionada == clave
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(if (sel) estados else fondo_claro)
                                                .clickable { frecuenciaSeleccionada = clave }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(label, fontSize = 11.sp, color = if (sel) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    opcionesFrecuencia.drop(2).forEach { (clave, label) ->
                                        val sel = frecuenciaSeleccionada == clave
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(if (sel) estados else fondo_claro)
                                                .clickable { frecuenciaSeleccionada = clave }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(label, fontSize = 11.sp, color = if (sel) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (frecuenciaSeleccionada != "AL_ENTREGAR") {
                                val numC = numCuotasTexto.toIntOrNull() ?: 1
                                val montoCuota = if (numC > 0) saldoRestanteCalculado / numC else 0.0
                                val fecha1raStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaPrimeraCuotaMillis))

                                OutlinedTextField(
                                    value = numCuotasTexto,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) numCuotasTexto = it },
                                    label = { Text("Número de Cuotas") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("1ra Cuota: $fecha1raStr", fontSize = 12.sp, color = Color.White)
                                    TextButton(onClick = { mostrarCalendarioCuota = true }) {
                                        Text("Cambiar fecha", fontSize = 11.sp, color = estados)
                                    }
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = fondo_claro)
                                ) {
                                    Text(
                                        text = "$numC cuota(s) de $${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(montoCuota)} / c.u. (1ra cuota: $fecha1raStr)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. SECCIÓN FECHA Y DATOS DE ENTREGA (SECCIÓN 4.5)
                item {
                    val fechaEntregaFormatted = if (fechaEntregaMillis != null) {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaEntregaMillis!!))
                    } else {
                        "¡Obligatoria!"
                    }
                    val resumenEntrega = "Entrega: $fechaEntregaFormatted · ${if (tipoEntrega == "DELIVERY") "Delivery" else "Retiro"}${if (esUrgente) " · URGENTE" else ""}"

                    SeccionPlegablePedido(
                        titulo = "5. DATOS DE ENTREGA",
                        resumen = resumenEntrega,
                        expandido = seccionEntregaExpandida,
                        onToggleExpandir = { seccionEntregaExpandida = !seccionEntregaExpandida },
                        icono = Icons.Default.DateRange,
                        completado = fechaEntregaMillis != null
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // FECHA DE ENTREGA (OBLIGATORIA)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Fecha de entrega (Obligatoria)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(if (fechaEntregaMillis != null) "Seleccionada: $fechaEntregaFormatted" else "Sin fecha", fontSize = 11.sp, color = if (fechaEntregaMillis != null) Color.LightGray else Color.Red)
                                }

                                Button(
                                    onClick = { mostrarCalendario = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = fondo_claro)
                                ) {
                                    Text("Cambiar", color = Color.DarkGray, fontSize = 11.sp)
                                }
                            }

                            // TIPO DE ENTREGA (RETIRO VS DELIVERY)
                            Text("Modalidad de Despacho:", fontSize = 11.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (tipoEntrega == "RETIRO") estados else fondo_claro)
                                        .clickable { 
                                            tipoEntrega = "RETIRO"
                                            cargoEnvioTexto = ""
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Retiro en Taller", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (tipoEntrega == "RETIRO") Color.White else Color.DarkGray)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (tipoEntrega == "DELIVERY") estados else fondo_claro)
                                        .clickable { tipoEntrega = "DELIVERY" }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Delivery / Envío", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (tipoEntrega == "DELIVERY") Color.White else Color.DarkGray)
                                }
                            }

                            if (tipoEntrega == "DELIVERY") {
                                OutlinedTextField(
                                    value = direccionEntrega,
                                    onValueChange = { direccionEntrega = it },
                                    label = { Text("Dirección de envío / Referencia") },
                                    placeholder = { Text("Ej. Urb. Las Flores, Calle 3, Casa #12") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // CHECKBOX URGENTE
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (esUrgente) Color(0xFFFFF3E0) else Color.Transparent)
                                    .clickable { esUrgente = !esUrgente }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(if (esUrgente) "Pedido Marcado como URGENTE" else "Marcar como Pedido Urgente", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (esUrgente) Color(0xFFE65100) else Color.LightGray)
                            }
                        }
                    }
                }

                // 6. SECCIÓN NOTAS GENERALES (SECCIÓN 4.6)
                item {
                    SeccionPlegablePedido(
                        titulo = "6. NOTAS GENERALES",
                        resumen = if (notas.isNotBlank()) notas else "Sin notas adicionales",
                        expandido = seccionNotasExpandida,
                        onToggleExpandir = { seccionNotasExpandida = !seccionNotasExpandida },
                        icono = Icons.Default.Star,
                        completado = notas.isNotBlank()
                    ) {
                        OutlinedTextField(
                            value = notas,
                            onValueChange = { notas = it },
                            label = { Text("Notas generales del pedido") },
                            placeholder = { Text("Condiciones, acuerdos o especificaciones de entrega...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 7. BARRA INFERIOR FIJA PLEGABLE Y COMPACTA (SECCIÓN 4.7)
            var detallesBarraExpandidos by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = fondo2),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Fila Principal Resumida: TOTAL · Abono · Saldo + Botón Desplegar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL: ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(totalUsd)}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bs ${String.format(java.util.Locale.US, "%.2f", totalUsd * tasaBcvActual)}",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }

                            Text("·", color = Color.Gray)

                            Column {
                                Text(
                                    text = "Abono: ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(abonoCalculadoUsd)}",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Saldo: ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(saldoRestanteCalculado)}",
                                    color = componentes,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { detallesBarraExpandidos = !detallesBarraExpandidos },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (detallesBarraExpandidos) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Detalles financieros",
                                tint = Color.LightGray
                            )
                        }
                    }

                    // Detalles Financieros Plegables (Ganancia Estimada y Alertas)
                    if (detallesBarraExpandidos) {
                        Column(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (totalUsd > 0 && totalUsd < costoMateriaPrimaTotal) {
                                Text(
                                    text = "⚠️ Pierdes dinero (Costo insumos: ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(costoMateriaPrimaTotal)})",
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ganancia estimada:", fontSize = 10.sp, color = Color.LightGray)
                                Text(
                                    text = "${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(gananciaEstimadaUsd)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }

                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (deudorSeleccionado != null || listaArticulos.any { it.producto.isNotBlank() }) {
                                    mostrarDialogoBorradorConfirm = true
                                } else {
                                    onDismiss()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Cancelar", color = Color.Gray, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val hoyInicioMillis = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                                    set(java.util.Calendar.MINUTE, 0)
                                    set(java.util.Calendar.SECOND, 0)
                                    set(java.util.Calendar.MILLISECOND, 0)
                                }.timeInMillis

                                val usableBytes = context.filesDir.usableSpace

                                if (deudorSeleccionado == null) {
                                    errorMsg = "Debes seleccionar un cliente (Obligatorio)"
                                } else if (listaArticulos.isEmpty() || listaArticulos.any { it.producto.isBlank() }) {
                                    errorMsg = "Completa el nombre de todos los productos (Obligatorio)"
                                } else if (listaArticulos.any { (it.cantidadTexto.toIntOrNull() ?: 0) <= 0 && (!it.usarVariantes || it.obtenerCantidadTotal() <= 0) }) {
                                    errorMsg = "La cantidad de cada producto debe ser un número entero positivo (≥ 1)"
                                } else if (totalUsd <= 0.0) {
                                    errorMsg = "El precio total del pedido debe ser mayor a cero"
                                } else if (abonoCalculadoUsd > totalUsd) {
                                    errorMsg = "El abono (${String.format(java.util.Locale.US, "%.2f", abonoCalculadoUsd)}) no puede ser mayor que el total (${String.format(java.util.Locale.US, "%.2f", totalUsd)})"
                                } else if (fechaEntregaMillis == null || fechaEntregaMillis!! < hoyInicioMillis) {
                                    errorMsg = "Debes seleccionar una fecha de entrega válida (hoy o futura)"
                                } else if (frecuenciaSeleccionada != "AL_ENTREGAR" && (numCuotasTexto.toIntOrNull() ?: 0) < 1) {
                                    errorMsg = "El número de cuotas debe ser al menos 1"
                                } else if (usableBytes < 5 * 1024 * 1024) {
                                    errorMsg = "Almacenamiento del teléfono lleno. Libera espacio antes de guardar."
                                } else {
                                    val numCuotas = if (frecuenciaSeleccionada == "AL_ENTREGAR") 1 else (numCuotasTexto.toIntOrNull() ?: 1)

                                    val nombresProductos = listaArticulos.joinToString(", ") { art ->
                                        val cant = art.obtenerCantidadTotal()
                                        val descVars = if (art.usarVariantes && art.variantes.isNotEmpty()) {
                                            " [" + art.variantes.joinToString("; ") { "${it.etiqueta} x${it.cantidadTexto}" + if (it.textoEstampar.isNotBlank()) " (${it.textoEstampar})" else "" } + "]"
                                        } else ""
                                        "${art.producto} (x$cant)$descVars"
                                    }

                                    val detallesArticulosText = listaArticulos.mapIndexedNotNull { idx, art ->
                                        val partes = mutableListOf<String>()
                                        if (art.detalle.isNotBlank()) partes.add("Detalle: ${art.detalle}")
                                        if (art.textoEstampar.isNotBlank()) partes.add("Estampa: ${art.textoEstampar}")
                                        if (art.notaItem.isNotBlank()) partes.add("Nota: ${art.notaItem}")
                                        if (partes.isNotEmpty()) "Prod #${idx + 1} (${art.producto}): " + partes.joinToString(" | ") else null
                                    }.joinToString("\n")

                                    val infoEntregaText = if (tipoEntrega == "DELIVERY" && direccionEntrega.isNotBlank()) "Dirección Delivery: $direccionEntrega" else null
                                    val infoUrgenteText = if (esUrgente) "⚡ PEDIDO URGENTE" else null
                                    val infoMetodoText = if (abonoCalculadoUsd > 0) "Método Abono: $metodoPagoAbono" + (if (referenciaAbono.isNotBlank()) " (Ref. $referenciaAbono)" else "") else null

                                    val notasConsolidadas = listOfNotNull(notas.ifBlank { null }, detallesArticulosText.ifBlank { null }, infoEntregaText, infoUrgenteText, infoMetodoText).joinToString("\n---\n")

                                    val cantidadTotalGral = listaArticulos.sumOf { it.obtenerCantidadTotal() }

                                    val costoBaseTotal = listaArticulos.sumOf { 
                                        val c = it.obtenerCantidadTotal()
                                        it.costoPiezaBaseUnitario * c 
                                    }
                                    val costoPasajeTotal = listaArticulos.sumOf { 
                                        val c = it.obtenerCantidadTotal()
                                        it.costoPasajeUnitario * c 
                                    }
                                    val costoInsumosTotal = listaArticulos.sumOf { 
                                        val c = it.obtenerCantidadTotal()
                                        it.costoInsumosUnitario * c 
                                    }

                                    val unitarioBaseGral = if(cantidadTotalGral > 0) costoBaseTotal / cantidadTotalGral else 0.0
                                    val unitarioPasajeGral = if(cantidadTotalGral > 0) costoPasajeTotal / cantidadTotalGral else 0.0
                                    val unitarioInsumosGral = if(cantidadTotalGral > 0) costoInsumosTotal / cantidadTotalGral else 0.0

                                    val fechaEntregaStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaEntregaMillis!!))

                                    val articulosResumenText = listaArticulos.joinToString("\n") { art ->
                                        val cant = art.obtenerCantidadTotal()
                                        val precioUnStr = String.format(java.util.Locale.US, "%.2f", art.obtenerPrecioUnitarioPromedio())
                                        val descVars = if (art.usarVariantes && art.variantes.isNotEmpty()) {
                                            val varsFiltradas = art.variantes.filter { (it.cantidadTexto.toIntOrNull() ?: 0) > 0 }
                                            if (varsFiltradas.isNotEmpty()) {
                                                " (" + varsFiltradas.joinToString(", ") { "${it.etiqueta} ×${it.cantidadTexto}" } + ")"
                                            } else ""
                                        } else ""
                                        
                                        val textoEstamparStr = if (art.textoEstampar.isNotBlank()) " [Estampa: \"${art.textoEstampar}\"]" else ""
                                        "• $cant ${art.producto}$descVars$textoEstamparStr — $precioUnStr c/u"
                                    }

                                    val msgWhatsApp = com.example.gestor_deudores.data.utils.armarMensajeResumenPedidoCliente(
                                        clienteNombre = "${deudorSeleccionado!!.nombre} ${deudorSeleccionado!!.apellido}",
                                        articulosResumenText = articulosResumenText,
                                        totalUsd = totalUsd,
                                        tasaBcv = tasaBcvActual,
                                        abonoUsd = abonoCalculadoUsd,
                                        saldoUsd = saldoRestanteCalculado,
                                        frecuenciaPago = frecuenciaSeleccionada,
                                        fechaEntregaStr = fechaEntregaStr
                                    )

                                    viewModel.crearNuevoPedido(
                                        context = context,
                                        deudorId = deudorSeleccionado!!.id,
                                        producto = nombresProductos,
                                        cantidad = cantidadTotalGral,
                                        precioUnitarioUsd = totalUsd / (if(cantidadTotalGral > 0) cantidadTotalGral else 1),
                                        abonoInicialUsd = abonoCalculadoUsd,
                                        numCuotas = numCuotas,
                                        frecuencia = frecuenciaSeleccionada,
                                        fechaEntregaMillis = fechaEntregaMillis!!,
                                        notas = notasConsolidadas,
                                        costoPiezaBaseUnitario = unitarioBaseGral,
                                        nombrePiezaBase = if(listaArticulos.size == 1) listaArticulos[0].nombrePiezaBase else "Múltiples Productos",
                                        costoPasajeUnitario = unitarioPasajeGral,
                                        costoInsumosUnitario = unitarioInsumosGral,
                                        descuento = montoDescuentoCalculado,
                                        costoDiseno = montoDisenoCalculado,
                                        costoEnvio = montoEnvioCalculado,
                                        totalCalculadoUsd = totalUsd,
                                        listaArticulos = listaArticulos,
                                        onExito = {
                                            viewModel.descartarBorrador(context)
                                            clienteNombreWhatsApp = "${deudorSeleccionado!!.nombre} ${deudorSeleccionado!!.apellido}"
                                            clienteTelfWhatsApp = deudorSeleccionado!!.telf
                                            mensajeWhatsAppGenerado = msgWhatsApp
                                            mostrarDialogoExitoWhatsApp = true
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = estados),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Guardar Pedido", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoExitoWhatsApp) {
        DialogoExitoPedidoConWhatsApp(
            clienteNombre = clienteNombreWhatsApp,
            clienteTelefono = clienteTelfWhatsApp,
            mensajeWhatsApp = mensajeWhatsAppGenerado,
            onDismiss = {
                mostrarDialogoExitoWhatsApp = false
                onDismiss()
            }
        )
    }

    if (mostrarDialogoBorradorConfirm) {
        DialogoConfirmarBorrador(
            onGuardarBorrador = {
                val borrador = PedidoBorradorState(
                    deudorId = deudorSeleccionado?.id,
                    clienteNombre = deudorSeleccionado?.let { "${it.nombre} ${it.apellido}" } ?: "",
                    busquedaCliente = busquedaCliente,
                    abonoInicialTexto = abonoInicialTexto,
                    frecuenciaSeleccionada = frecuenciaSeleccionada,
                    numCuotasTexto = numCuotasTexto,
                    fechaEntregaMillis = fechaEntregaMillis,
                    notas = notas,
                    descuentoTexto = descuentoTexto,
                    esDescuentoPorcentaje = esDescuentoPorcentaje,
                    cargoDisenoTexto = cargoDisenoTexto,
                    cargoEnvioTexto = cargoEnvioTexto,
                    tipoEntrega = tipoEntrega,
                    direccionEntrega = direccionEntrega,
                    esUrgente = esUrgente,
                    listaArticulosJson = com.google.gson.Gson().toJson(listaArticulos)
                )
                viewModel.guardarBorrador(context, borrador)
                mostrarDialogoBorradorConfirm = false
                onDismiss()
            },
            onDescartar = {
                viewModel.descartarBorrador(context)
                mostrarDialogoBorradorConfirm = false
                onDismiss()
            },
            onSeguirEditando = {
                mostrarDialogoBorradorConfirm = false
            }
        )
    }
}
}
