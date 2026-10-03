@file:OptIn(ExperimentalMaterial3Api::class)
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            NavigationBar(
                containerColor = fondo2,
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(rutas.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedidos") },
                    label = { Text("Pedidos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = estados,
                        selectedTextColor = estados,
                        indicatorColor = Color.White
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(rutas.REGISTRO_EXPRESS) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Cliente") },
                    label = { Text("Cliente") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
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

data class ArticuloPedido(
    var producto: String = "",
    var cantidadTexto: String = "1",
    var precioUnitarioTexto: String = ""
)

@Composable
fun DialogoCrearPedido(
    viewModel: PedidoViewModel,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val clientes by viewModel.listaClientes.collectAsState()
    val sugerencias by viewModel.sugerenciasProductos.collectAsState()

    var deudorSeleccionado by remember { mutableStateOf<Deudor?>(null) }
    var expandidoClientes by remember { mutableStateOf(false) }

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
        return listaArticulos.sumOf {
            val cant = it.cantidadTexto.toIntOrNull() ?: 1
            val precio = it.precioUnitarioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
            cant * precio
        }
    }

    val totalUsd = calcularTotalGeneral()
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

                // 1. SELECTOR DE CLIENTE Y BOTÓN NUEVO CLIENTE
                item {
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
                                value = if (deudorSeleccionado != null) "${deudorSeleccionado!!.nombre} ${deudorSeleccionado!!.apellido}" else "Selecciona un cliente",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandidoClientes,
                                onDismissRequest = { expandidoClientes = false }
                            ) {
                                clientes.forEach { cliente ->
                                    DropdownMenuItem(
                                        text = { Text("${cliente.nombre} ${cliente.apellido}") },
                                        onClick = {
                                            deudorSeleccionado = cliente
                                            expandidoClientes = false
                                        }
                                    )
                                }
                            }
                        }

                        // Botón de nuevo cliente
                        Button(
                            onClick = { navController.navigate(rutas.REGISTRO_EXPRESS) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = fondo2)
                        ) {
                            Text("+ Nuevo", color = Color.White)
                        }
                    }
                }

                // 2 Y 3. LISTA DINÁMICA DE PRODUCTOS
                items(listaArticulos.size) { index ->
                    val articulo = listaArticulos[index]
                    var expandidoProductos by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = fondo_claro),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Cabecera del Artículo con Botón de Eliminar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Artículo ${index + 1}", fontWeight = FontWeight.Bold, color = estados)
                                if (listaArticulos.size > 1) {
                                    IconButton(
                                        onClick = { listaArticulos.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "Eliminar", tint = Color.Red)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Input de Producto
                            ExposedDropdownMenuBox(
                                expanded = expandidoProductos,
                                onExpandedChange = { expandidoProductos = !expandidoProductos }
                            ) {
                                OutlinedTextField(
                                    value = articulo.producto,
                                    onValueChange = { 
                                        listaArticulos[index] = articulo.copy(producto = it) 
                                    },
                                    placeholder = { Text("Ej. Taza Mágica, Franela") },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                val sugFiltradas = sugerencias.filter { it.contains(articulo.producto, ignoreCase = true) && it != articulo.producto }
                                if (sugFiltradas.isNotEmpty()) {
                                    ExposedDropdownMenu(
                                        expanded = expandidoProductos,
                                        onDismissRequest = { expandidoProductos = false }
                                    ) {
                                        sugFiltradas.forEach { sug ->
                                            DropdownMenuItem(
                                                text = { Text(sug) },
                                                onClick = {
                                                    listaArticulos[index] = articulo.copy(producto = sug)
                                                    expandidoProductos = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Inputs de Cantidad y Precio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = articulo.cantidadTexto,
                                    onValueChange = { 
                                        if (it.all { c -> c.isDigit() }) {
                                            listaArticulos[index] = articulo.copy(cantidadTexto = it)
                                        }
                                    },
                                    label = { Text("Cant.") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = articulo.precioUnitarioTexto,
                                    onValueChange = { 
                                        listaArticulos[index] = articulo.copy(precioUnitarioTexto = it) 
                                    },
                                    label = { Text("Precio Un. ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                // Botón + Agregar otro producto
                item {
                    TextButton(
                        onClick = { listaArticulos.add(ArticuloPedido()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Agregar otro producto", color = estados, fontWeight = FontWeight.Bold)
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
                                    val nombresProductos = listaArticulos.joinToString(", ") { "${it.producto} (x${it.cantidadTexto})" }
                                    
                                    viewModel.crearNuevoPedido(
                                        deudorId = deudorSeleccionado!!.id,
                                        producto = nombresProductos,
                                        cantidad = 1, // En el historial queda como 1 paquete consolidado
                                        precioUnitarioUsd = totalUsd, // El precio unitario de este paquete consolidado es el total
                                        abonoInicialUsd = abonoInicial,
                                        numCuotas = numCuotas,
                                        frecuencia = frecuenciaSeleccionada,
                                        fechaEntregaMillis = fechaEntregaMillis,
                                        notas = notas,
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
