@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.gestor_deudores.data.database.Deudor
import com.example.gestor_deudores.data.database.PedidoConCliente
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
fun PedidosPrincipal(viewModel: PedidoViewModel, navController: NavController) {
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()
    val listaPedidos by viewModel.listaPedidos.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    if (mostrarDialogoCrear) {
        DialogoCrearPedido(
            viewModel = viewModel,
            navController = navController,
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
fun DialogoCrearPedido(
    viewModel: PedidoViewModel,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val clientes by viewModel.listaClientes.collectAsState()
    val plantillasCotizador by viewModel.listaPlantillas.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var deudorSeleccionado by remember { mutableStateOf<Deudor?>(null) }
    var expandidoClientes by remember { mutableStateOf(false) }

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

    // --- NUEVA LISTA DINÁMICA DE ARTÍCULOS ---
    val listaArticulos = remember { mutableStateListOf(ArticuloPedido()) }

    var abonoInicialTexto by remember { mutableStateOf("") }
    var frecuenciaSeleccionada by remember { mutableStateOf("AL_ENTREGAR") }
    var numCuotasTexto by remember { mutableStateOf("1") }
    var fechaEntregaMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var notas by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    // Función auxiliar para calcular el total
    fun calcularTotalGeneral(): Double {
        return listaArticulos.sumOf { it.obtenerSubtotal() }
    }

    val totalUsd = calcularTotalGeneral()
    val totalPiezasGral = listaArticulos.sumOf { it.obtenerCantidadTotal() }
    val resumenProductos = "${listaArticulos.size} prod · $totalPiezasGral pcs · Total ${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(totalUsd)}"
    val abonoInicial = abonoInicialTexto.replace(",", ".").toDoubleOrNull() ?: 0.0

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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("NUEVO PEDIDO", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = estados)
                }

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

                                                // Botón Eliminar (4.2)
                                                if (listaArticulos.size > 1) {
                                                    IconButton(
                                                        onClick = { listaArticulos.removeAt(index) },
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
                                                                    val sugerido = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(plantilla).precioSugeridoUsd
                                                                    Text("Sugerido: $${String.format(java.util.Locale.US, "%.2f", sugerido)}", fontSize = 10.sp, color = Color.Gray)
                                                                }
                                                            },
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    val costosDesglosados = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(plantilla)
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
                                                                (p.nombrePlantilla.contains(prodNombre, ignoreCase = true) || prodNombre.contains(p.nombrePlantilla, ignoreCase = true)) &&
                                                                if (p.esPlantillaMayor) cantInt >= p.minimoUnidadesMayor else true
                                                            }

                                                            if (plantillaAplica != null && prodNombre.isNotBlank() && !articulo.precioManualEditado) {
                                                                val costos = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(plantillaAplica)
                                                                listaArticulos[index] = articulo.copy(
                                                                    cantidadTexto = inputCant,
                                                                    precioUnitarioTexto = String.format(java.util.Locale.US, "%.2f", costos.precioSugeridoUsd),
                                                                    costoPiezaBaseUnitario = costos.costoPiezaBaseUsd,
                                                                    nombrePiezaBase = plantillaAplica.nombrePlantilla,
                                                                    costoPasajeUnitario = costos.costoPasajeUsd,
                                                                    costoInsumosUnitario = costos.costoInsumosUsd
                                                                )
                                                            } else {
                                                                listaArticulos[index] = articulo.copy(cantidadTexto = inputCant)
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
                                                text = "Subtotal: $${NumberFormat.getCurrencyInstance(Locale("en", "US")).format(articulo.obtenerSubtotal())}",
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
                                                                                val listAux = articulo.variantes.toMutableList()
                                                                                listAux[vIndex] = variante.copy(cantidadTexto = nuevaCant)
                                                                                listaArticulos[index] = articulo.copy(variantes = listAux)
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

                // 4. TOTAL CALCULADO AUTOMÁTICAMENTE
                item {
                    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(totalUsd)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = fondo2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total del pedido:", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(formatoUSD, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5. ABONO INICIAL (ADELANTO)
                item {
                    OutlinedTextField(
                        value = abonoInicialTexto,
                        onValueChange = { abonoInicialTexto = it },
                        label = { Text("Abono / Adelanto inicial ($) (Opcional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 6. FORMA DE PAGO DEL SALDO
                item {
                    Text("Forma de pago del saldo restante:", fontSize = 12.sp, color = Color.Gray)
                    val opcionesFrecuencia = listOf(
                        "AL_ENTREGAR" to "Al retirar / entregar",
                        "SEMANAL" to "Semanal",
                        "QUINCENAL" to "Quincenal",
                        "MENSUAL" to "Mensual"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(opcionesFrecuencia) { (clave, label) ->
                            val sel = frecuenciaSeleccionada == clave
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (sel) estados else fondo2)
                                    .clickable { frecuenciaSeleccionada = clave }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 11.sp, color = if (sel) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (frecuenciaSeleccionada != "AL_ENTREGAR") {
                    item {
                        OutlinedTextField(
                            value = numCuotasTexto,
                            onValueChange = { if (it.all { c -> c.isDigit() }) numCuotasTexto = it },
                            label = { Text("Número de Cuotas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // 7. FECHA DE ENTREGA
                item {
                    val fechaEntregaStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaEntregaMillis))
                    OutlinedTextField(
                        value = fechaEntregaStr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de entrega") },
                        trailingIcon = {
                            IconButton(onClick = { mostrarCalendario = true }) {
                                Icon(Icons.Default.DateRange, null, tint = estados)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 8. NOTAS DE PRODUCCIÓN
                item {
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas de producción (Talla, diseño...)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (errorMsg.isNotEmpty()) {
                    item {
                        Text(errorMsg, color = Color.Red, fontSize = 12.sp)
                    }
                }

                // BOTONES CANCELAR Y GUARDAR
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                if (deudorSeleccionado == null) {
                                    errorMsg = "Debes seleccionar un cliente"
                                } else if (listaArticulos.any { it.producto.isBlank() }) {
                                    errorMsg = "Completa el nombre de todos los productos"
                                } else if (totalUsd <= 0.0) {
                                    errorMsg = "El precio total debe ser mayor a cero"
                                } else {
                                    val numCuotas = if (frecuenciaSeleccionada == "AL_ENTREGAR") 1 else (numCuotasTexto.toIntOrNull() ?: 1)
                                    
                                    // Consolidar la lista de productos en un solo String para la base de datos
                                    val nombresProductos = listaArticulos.joinToString(", ") { art ->
                                        val cant = art.obtenerCantidadTotal()
                                        val descVars = if (art.usarVariantes && art.variantes.isNotEmpty()) {
                                            " [" + art.variantes.joinToString("; ") { "${it.etiqueta} x${it.cantidadTexto}" + if (it.textoEstampar.isNotBlank()) " (${it.textoEstampar})" else "" } + "]"
                                        } else ""
                                        "${art.producto} (x$cant)$descVars"
                                    }
                                    
                                    // Consolidar detalles, estampados y notas de los artículos
                                    val detallesArticulosText = listaArticulos.mapIndexedNotNull { idx, art ->
                                        val partes = mutableListOf<String>()
                                        if (art.detalle.isNotBlank()) partes.add("Detalle: ${art.detalle}")
                                        if (art.textoEstampar.isNotBlank()) partes.add("Estampa: ${art.textoEstampar}")
                                        if (art.notaItem.isNotBlank()) partes.add("Nota: ${art.notaItem}")
                                        if (partes.isNotEmpty()) "Prod #${idx + 1} (${art.producto}): " + partes.joinToString(" | ") else null
                                    }.joinToString("\n")

                                    val notasConsolidadas = listOf(notas, detallesArticulosText).filter { it.isNotBlank() }.joinToString("\n---\n")

                                    val cantidadTotalGral = listaArticulos.sumOf { it.obtenerCantidadTotal() }

                                    // Promediamos o sumamos los costos reales consolidados del pedido
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

                                    viewModel.crearNuevoPedido(
                                        deudorId = deudorSeleccionado!!.id,
                                        producto = nombresProductos,
                                        cantidad = cantidadTotalGral,
                                        precioUnitarioUsd = totalUsd / (if(cantidadTotalGral > 0) cantidadTotalGral else 1),
                                        abonoInicialUsd = abonoInicial,
                                        numCuotas = numCuotas,
                                        frecuencia = frecuenciaSeleccionada,
                                        fechaEntregaMillis = fechaEntregaMillis,
                                        notas = notasConsolidadas,
                                        costoPiezaBaseUnitario = unitarioBaseGral,
                                        nombrePiezaBase = if(listaArticulos.size == 1) listaArticulos[0].nombrePiezaBase else "Múltiples Productos",
                                        costoPasajeUnitario = unitarioPasajeGral,
                                        costoInsumosUnitario = unitarioInsumosGral,
                                        onExito = onDismiss
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = estados)
                        ) {
                            Text("Guardar Pedido", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
