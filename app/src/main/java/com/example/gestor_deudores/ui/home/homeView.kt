@file:kotlin.OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.home

import androidx.annotation.OptIn
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Dialog
import com.example.gestor_deudores.data.database.PerfilNegocio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.gestor_deudores.ui.rutas
import com.example.gestor_deudores.ui.theme.componentes
import com.example.gestor_deudores.ui.theme.estados
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2
import com.example.gestor_deudores.ui.theme.fondo_claro
import com.example.gestor_deudores.data.utils.abrirWhatsApp
import com.example.gestor_deudores.data.utils.RespaldoUtils
import com.example.gestor_deudores.data.utils.EstadoCobro
import com.example.gestor_deudores.data.database.Deuda
import com.example.gestor_deudores.data.database.Deudor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun homePrincipal(viewModel: HomeViewModel, navController: NavController){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
    val pestañaActual by viewModel.pestañaActual.collectAsState()
    val listaDeudores by viewModel.deudorFiltrados.collectAsState()
    
    // --- ESTADOS DE LA TASA BCV Y MONEDA ---
    val monedaVista by viewModel.monedaVista.collectAsState()
    val precioDolarBCV by viewModel.precioDolarBCV.collectAsState()
    val cargandoTasa by viewModel.cargandoTasa.collectAsState()
    val errorTasa by viewModel.errorTasa.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.iniciarTasaBCV(context)
    }

    val perfilNegocio by viewModel.perfilNegocio.collectAsState()
    var mostrarDialogoPerfil by remember { mutableStateOf(false) }
    var deudaMaximaPermitida by remember { mutableStateOf(0.0) }
    
    // --- ESTADOS Y LAUNCHERS PARA RESPALDOS ---
    var uriImportarPendiente by remember { mutableStateOf<Uri?>(null) }
    var mostrarConfirmacionImportar by remember { mutableStateOf(false) }
    val fechaHoyStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val launcherExportar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val exito = RespaldoUtils.exportarBaseDatos(context, uri)
            if (exito) {
                Toast.makeText(context, "Respaldo creado con éxito", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error al crear el respaldo", Toast.LENGTH_LONG).show()
            }
        }
    }

    val launcherImportar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            if (RespaldoUtils.esBaseDatosValida(context, uri)) {
                uriImportarPendiente = uri
                mostrarConfirmacionImportar = true
            } else {
                Toast.makeText(context, "El archivo seleccionado NO es un respaldo válido de la app", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (mostrarConfirmacionImportar && uriImportarPendiente != null) {
        DialogoConfirmacion(
            titulo = "Restaurar copia de seguridad",
            mensaje = "¡ATENCIÓN! Se reemplazarán TODOS los clientes y ventas actuales por los datos de la copia de seguridad. Esta acción no se puede deshacer.",
            onConfirmar = {
                val exito = RespaldoUtils.importarBaseDatos(context, uriImportarPendiente!!)
                mostrarConfirmacionImportar = false
                if (exito) {
                    Toast.makeText(context, "Respaldo restaurado con éxito", Toast.LENGTH_LONG).show()
                    (context as? Activity)?.recreate()
                } else {
                    Toast.makeText(context, "Error al restaurar el respaldo", Toast.LENGTH_LONG).show()
                }
            },
            onCancelar = {
                mostrarConfirmacionImportar = false
                uriImportarPendiente = null
            }
        )
    }

    // --- NUEVOS INTERRUPTORES PARA EL DIALOG ---
    var mostrarDialogoAbono by remember { mutableStateOf(false) }
    var mostrarDialogoArchivar by remember { mutableStateOf(false) } // <-- NUEVO
    var deudorSeleccionado by remember { mutableStateOf<Deudor?>(null) }

    // Si el interruptor está encendido y hay un deudor, dibujamos la ventana por encima de todo
    if (mostrarDialogoAbono && deudorSeleccionado != null) {
        DialogoAbono(
            nombreDeudor = deudorSeleccionado!!.nombre,
            deudaMaximaUsd = deudaMaximaPermitida,
            monedaVista = monedaVista,
            tasaBCV = precioDolarBCV,
            onDismiss = {
                mostrarDialogoAbono = false // Apagamos el interruptor
                deudorSeleccionado = null
            },
            onConfirmar = { monto ->
                viewModel.registrarAbono(deudorSeleccionado!!, monto)
                mostrarDialogoAbono = false
                deudorSeleccionado = null
            }
        )
    }

    if (mostrarDialogoArchivar && deudorSeleccionado != null) {
        DialogoConfirmacion(
            titulo = if (pestañaActual == Pestaña.ARCHIVADOS) "Eliminar definitivamente" else "Archivar cliente",
            mensaje = if (pestañaActual == Pestaña.ARCHIVADOS) 
                "¿Estás seguro de eliminar a ${deudorSeleccionado!!.nombre} para siempre? Esta acción NO se puede deshacer."
                else "Se archivará a ${deudorSeleccionado!!.nombre}. Podrás restaurarlo desde la pestaña Archivados.",
            onConfirmar = {
                if (pestañaActual == Pestaña.ARCHIVADOS) {
                    viewModel.eliminarDeudorDefinitivamente(deudorSeleccionado!!)
                } else {
                    viewModel.archivarDeudor(deudorSeleccionado!!)
                }
                mostrarDialogoArchivar = false
                deudorSeleccionado = null
            },
            onCancelar = {
                mostrarDialogoArchivar = false
                deudorSeleccionado = null
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = fondo,
                modifier = Modifier.width(300.dp)
            ) {
                // Encabezado con los colores de tu tema (estados y fondo2)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(estados),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(fondo2),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val nombreNegocioTxt = perfilNegocio?.nombreNegocio?.takeIf { it.isNotBlank() } ?: "Mi Taller"
                        Text(
                            text = "Zubli - $nombreNegocioTxt",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text("Gestión de Negocio", color = fondo_claro, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RESPALDOS Y SEGURIDAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // Opción 1: Crear copia de seguridad
                NavigationDrawerItem(
                    label = { Text("Crear copia de seguridad", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = estados) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        launcherExportar.launch("respaldo_zubli_$fechaHoyStr.db")
                    },
                    icon = { Text("📂", fontSize = 18.sp) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Opción 2: Restaurar copia de seguridad
                NavigationDrawerItem(
                    label = { Text("Restaurar copia de seguridad", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = estados) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        launcherImportar.launch(arrayOf("*/*"))
                    },
                    icon = { Text("📥", fontSize = 18.sp) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CONFIGURACIÓN DE MARCA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // Opción 3: Configurar Mi Negocio y WhatsApp
                NavigationDrawerItem(
                    label = { Text("Configurar Mi Negocio y WhatsApp", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = estados) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogoPerfil = true
                    },
                    icon = { Text("⚙️", fontSize = 18.sp) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold (
            topBar = {
                toolbar(
                    titulo = "Mis cobros",
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(rutas.REGISTRO) },
                    containerColor = estados,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Cliente / Deuda")
                }
            },
            bottomBar = {
                BarraNavegacionInferior(
                    rutaActual = rutas.HOME,
                    onIrAInicio = {},
                    onIrAPedidos = {
                        navController.navigate(rutas.PEDIDOS)
                    },
                    onIrACotizar = {
                        navController.navigate(rutas.COTIZADOR)
                    },
                    onIrAResumen = {
                        navController.navigate(rutas.RESUMEN)
                    }
                )
            }
            ){ innerPadding ->

        if (mostrarDialogoPerfil) {
            DialogoConfigurarPerfilNegocio(
                perfilActual = perfilNegocio ?: PerfilNegocio(),
                onDismiss = { mostrarDialogoPerfil = false },
                onGuardar = { nuevoPerfil ->
                    viewModel.guardarPerfilNegocio(nuevoPerfil)
                }
            )
        }

        Column (modifier = Modifier.fillMaxSize().padding(innerPadding) .background(fondo))
        {
            BuscadorDeudores(
                textoActual = textoBusqueda,
                onTextoCambiado = { nuevoTexto -> viewModel.actualizarBuscador(nuevoTexto) }
            )
            SelectorPestañas(
                pestañaActual = pestañaActual,
                onPestañaSeleccionada = { nuevaPestaña -> viewModel.cambiarPestaña(nuevaPestaña) }
            )

            // --- NUEVO: SELECTOR DE MONEDA (USD / VES) Y TASA BCV ---
            SelectorMoneda(
                monedaActual = monedaVista,
                tasaBCV = precioDolarBCV,
                cargando = cargandoTasa,
                errorMsg = errorTasa,
                onMonedaSeleccionada = { nuevaMoneda ->
                    viewModel.cambiarMonedaVista(nuevaMoneda, context)
                },
                onRefrescarTasa = {
                    viewModel.actualizarTasaBCV(context)
                }
            )

// 5. La lista que dibuja las tarjetas automáticamente
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Iteramos sobre la lista de "paquetes" que nos mandó el ViewModel
                items(listaDeudores) { paquete ->

                    // Le entregamos a la tarjeta exactamente lo que pide sacándolo del paquete
                    carDeudores(
                        deudor = paquete.deudor,
                        deuda = paquete.deuda,
                        montoRestante = paquete.montoRestante,
                        estadoCobro = paquete.estadoCobro,
                        pestañaActual = pestañaActual,
                        monedaVista = monedaVista,
                        tasaBCV = precioDolarBCV,
                        perfilNegocio = perfilNegocio ?: com.example.gestor_deudores.data.database.PerfilNegocio(),
                        onEditarClick = {
                            // Para editar, viajamos a la ruta de registro PERO enviándole el ID
                            // (Tendremos que ajustar rutas.kt para que acepte este ID)
                            navController.navigate(rutas.crearRutaEditarDeudor(paquete.deudor.id, paquete.deuda.id))
                        },

                        onAbonarClick = {
                            deudorSeleccionado = paquete.deudor
                            deudaMaximaPermitida = paquete.montoRestante // Guardamos el límite
                            mostrarDialogoAbono = true
                        },
                        onEliminarClick = {
                            deudorSeleccionado = paquete.deudor
                            mostrarDialogoArchivar = true
                        },
                        onRestaurarClick = {
                            viewModel.restaurarDeudor(paquete.deudor)
                        },
                        onHistorialClick= {
                            navController.navigate(rutas.crearRutaHistorial(paquete.deudor.id))
                        }
                    )

                }
            }
        }
    }
}
}


@Composable
fun BarraNavegacionInferior(
    rutaActual: String = rutas.HOME,
    onIrAInicio: () -> Unit,
    onIrAPedidos: () -> Unit,
    onIrACotizar: () -> Unit,
    onIrAResumen: () -> Unit
) {
    NavigationBar(
        containerColor = fondo2,
    ) {
        // Ítem 1: Inicio
        NavigationBarItem(
            selected = (rutaActual == rutas.HOME),
            onClick = { onIrAInicio() },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = estados,
                selectedTextColor = estados,
                indicatorColor = Color.White
            )
        )

        // Ítem 2: Pedidos
        NavigationBarItem(
            selected = (rutaActual == rutas.PEDIDOS),
            onClick = { onIrAPedidos() },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedidos") },
            label = { Text("Pedidos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = estados,
                selectedTextColor = estados,
                indicatorColor = Color.White
            )
        )

        // Ítem 3: Cotizar
        NavigationBarItem(
            selected = (rutaActual == rutas.COTIZADOR),
            onClick = { onIrACotizar() },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Cotizar") },
            label = { Text("Cotizar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = estados,
                selectedTextColor = estados,
                indicatorColor = Color.White
            )
        )

        // Ítem 4: Resumen
        NavigationBarItem(
            selected = (rutaActual == rutas.RESUMEN),
            onClick = { onIrAResumen() },
            icon = { Icon(Icons.Default.Star, contentDescription = "Resumen") },
            label = { Text("Resumen") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = estados,
                selectedTextColor = estados,
                indicatorColor = Color.White
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun toolbar(
    titulo: String = "MIS COBROS",
    onMenuClick: (() -> Unit)? = null
){
    CenterAlignedTopAppBar(
        title = { Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
        navigationIcon = {
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú de Opciones",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fondo2)
    )
}


@Composable
fun SelectorPestañas(
    pestañaActual: Pestaña, // La pestaña que el ViewModel dice que está activa
    onPestañaSeleccionada: (Pestaña) -> Unit // La función para avisarle al ViewModel que tocamos otra
) {



    val colorTextoInactivo = Color.Gray // Gris para texto inactivo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(fondo2, shape = RoundedCornerShape(50))
            .padding(4.dp), // Pequeño espacio interno para que el botón no toque los bordes
        verticalAlignment = Alignment.CenterVertically
    ) {

        // --- BOTÓN PENDIENTES ---
        Box(
            modifier = Modifier
                .weight(1f) // Magia: Esto hace que ocupe exactamente el 50% del ancho
                .clip(RoundedCornerShape(50))
                .background(
                    // Condición: Si está seleccionada, pintamos el fondo, si no, transparente
                    if (pestañaActual == Pestaña.PENDIENTES) fondo_claro else Color.Transparent
                )
                .clickable { onPestañaSeleccionada(Pestaña.PENDIENTES) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PENDIENTES",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (pestañaActual == Pestaña.PENDIENTES) estados else colorTextoInactivo
            )
        }

        // --- BOTÓN HISTORIAL ---
        Box(
            modifier = Modifier
                .weight(1f) // Magia: El otro ancho
                .clip(RoundedCornerShape(50))
                .background(
                    // Condición: Si está seleccionada, pintamos el fondo, si no, transparente
                    if (pestañaActual == Pestaña.HISTORIAL) fondo_claro else Color.Transparent
                )
                .clickable { onPestañaSeleccionada(Pestaña.HISTORIAL) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HISTORIAL",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (pestañaActual == Pestaña.HISTORIAL) estados else colorTextoInactivo
            )
        }
        
        // --- BOTÓN ARCHIVADOS ---
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(
                    if (pestañaActual == Pestaña.ARCHIVADOS) fondo_claro else Color.Transparent
                )
                .clickable { onPestañaSeleccionada(Pestaña.ARCHIVADOS) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ARCHIVADOS",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (pestañaActual == Pestaña.ARCHIVADOS) estados else colorTextoInactivo
            )
        }
    }
}


@Composable
fun BuscadorDeudores(
    textoActual: String, // El texto que se va a mostrar
    onTextoCambiado: (String) -> Unit // La función que avisa que el usuario escribió algo
) {
    TextField(
        value = textoActual,
        onValueChange = { nuevoTexto -> onTextoCambiado(nuevoTexto) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(text = "Buscar cliente...", color = Color.Gray) },

        // El icono de la lupita a la izquierda
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Icono de buscar",
                tint = Color.Gray
            )
        },

        // (Opcional) Un botón de "X" a la derecha para borrar rápido si hay texto
        trailingIcon = {
            if (textoActual.isNotEmpty()) {
                IconButton(onClick = { onTextoCambiado("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Borrar texto",
                        tint = Color.Gray
                    )
                }
            }
        },

        // Bordes bien redondeados
        shape = RoundedCornerShape(50),

        // Colores personalizados (Aquí puedes usar 'fondo2' o tus variables de color)
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fondo2, // Ejemplo de un color de tu paleta
            unfocusedContainerColor = fondo2,
            // Esto quita la línea fea de abajo del TextField
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}


@Composable
fun carDeudores(
    deudor: Deudor,
    deuda: Deuda,
    montoRestante: Double,
    estadoCobro: EstadoCobro?, // <-- AHORA RECIBE EL CÁLCULO
    pestañaActual: Pestaña,
    monedaVista: String = "USD",
    tasaBCV: Double = 0.0,
    perfilNegocio: com.example.gestor_deudores.data.database.PerfilNegocio = com.example.gestor_deudores.data.database.PerfilNegocio(),
    onEditarClick: () -> Unit,
    onAbonarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onRestaurarClick: () -> Unit,
    onHistorialClick: () -> Unit 
){
    val contexto = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // --- PARTE SUPERIOR: Avatar, Nombre y Rol ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                //avatar
                avatar(deudor)

                Spacer(modifier = Modifier.width(12.dp))
                // 2. Nombre y etiqueta de rol
                Column {
                    Text(
                        text = "${deudor.nombre} ${deudor.apellido}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    // La etiqueta redondita de "CLIENTE"
                    Box(
                        modifier = Modifier
                            .background(fondo2, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = deuda.rol.uppercase(),
                            fontSize = 10.sp,
                            color = estados,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PARTE INFERIOR: Detalles de la deuda ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Monto Restante:", fontSize = 12.sp, color = Color.Gray)

                    val montoBs = montoRestante * tasaBCV
                    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(montoRestante)
                    val formatoBs = "Bs. ${"%.2f".format(montoBs)}"

                    val textoPrincipal = if (monedaVista == "VES" && tasaBCV > 0) formatoBs else formatoUSD
                    val textoSecundario = if (monedaVista == "VES") formatoUSD else formatoBs

                    Text(
                        text = textoPrincipal,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = componentes
                    )
                    if (tasaBCV > 0) {
                        Text(
                            text = "($textoSecundario)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = estados
                        )
                    }
                }

                // --- NUEVO: ETIQUETA DE VENCIMIENTO ---
                if (estadoCobro != null && montoRestante > 0.0) {
                    if (estadoCobro.vencida) {
                        // Etiqueta Roja (Vencida)
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            val textoAtraso = if (estadoCobro.diasAtraso == 1L) "hace 1 día" else "hace ${estadoCobro.diasAtraso} días"
                            Text(
                                text = "VENCIDA $textoAtraso",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (estadoCobro.proximoVencimientoMillis != null) {
                        // Etiqueta Verde (Al día)
                        val fechaFormateada = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(estadoCobro.proximoVencimientoMillis))
                        val cuotaFormateada = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(estadoCobro.proximaCuotaMonto)
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Próxima cuota", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "$cuotaFormateada",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = estados
                            )
                            Text(
                                text = "vence el $fechaFormateada",
                                fontSize = 11.sp,
                                color = estados
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Colocamos el resto de los datos dinámicos concatenando textos
            Text(text = "Pedido/Producto: ${deuda.tipoDeuda}", fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Fecha: ${deuda.fecha}", fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Teléfono: ${deudor.telf}", fontSize = 14.sp, color = Color.DarkGray)

            // Validamos que si no hay descripción, no se vea feo
            if (deuda.descripcion.isNotBlank()) {
                Text(text = "Detalles: ${deuda.descripcion}", fontSize = 14.sp, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NUEVO: Fila de Botones de Acción ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Alineamos los botones a la derecha
            ){
                if (pestañaActual == Pestaña.ARCHIVADOS) {
                    // --- BOTÓN RESTAURAR ---
                    IconButton(
                        onClick = { onRestaurarClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE8F5E9), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Restaurar cliente",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // --- BOTÓN ELIMINAR DEFINITIVAMENTE ---
                    IconButton(
                        onClick = { onEliminarClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFEBEE), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar definitivamente",
                            tint = Color.Red
                        )
                    }
                } else {
                    // --- BOTÓN WHATSAPP (VERDE) ---
                    val tieneTelefono = deudor.telf.isNotBlank()
                    
                    IconButton(
                        onClick = {
                            if (!tieneTelefono) {
                                Toast.makeText(contexto, "El cliente no tiene teléfono registrado", Toast.LENGTH_SHORT).show()
                            } else {
                                val saldoFormateado = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(montoRestante)
                                val cuotaFormateada = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(estadoCobro?.proximaCuotaMonto ?: 0.0)
                                val fechaFormateada = if (estadoCobro?.proximoVencimientoMillis != null) 
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(estadoCobro.proximoVencimientoMillis)) 
                                    else "la brevedad"

                                val montoBsText = if (tasaBCV > 0) " (equivale a Bs. ${"%.2f".format(montoRestante * tasaBCV)})" else ""
                                val cuotaBsText = if (tasaBCV > 0 && estadoCobro?.proximaCuotaMonto != null) " (Bs. ${"%.2f".format(estadoCobro.proximaCuotaMonto * tasaBCV)})" else ""

                                val mensaje = com.example.gestor_deudores.data.utils.armarMensajeCobro(
                                    perfil = perfilNegocio ?: com.example.gestor_deudores.data.database.PerfilNegocio(),
                                    clienteNombre = "${deudor.nombre} ${deudor.apellido}".trim(),
                                    montoUsd = montoRestante,
                                    montoBsText = montoBsText,
                                    cuotaUsd = estadoCobro?.proximaCuotaMonto ?: 0.0,
                                    cuotaBsText = cuotaBsText,
                                    fechaVencimiento = fechaFormateada
                                )

                                abrirWhatsApp(contexto, deudor.telf, mensaje)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (tieneTelefono) Color(0xFF25D366) else Color(0xFFCCCCCC), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Enviar mensaje por WhatsApp",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // --- BOTÓN VER HISTORIAL ---
                    IconButton(
                        onClick = { onHistorialClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE0F7FA), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // O puedes usar Icons.Default.List si lo importas
                            contentDescription = "Ver Historial",
                            tint = Color(0xFF00ACC1)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // --- BOTÓN ARCHIVAR/ELIMINAR ---
                    IconButton(
                        onClick = { onEliminarClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFEBEE), shape = CircleShape) // Un fondo rojito claro
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Archivar cliente",
                            tint = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Botón Editar
                    IconButton(
                        onClick = { onEditarClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(fondo2, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar cliente",
                            tint = estados
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Botón Abonar
                    IconButton(
                        onClick = { onAbonarClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(componentes, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Abonar a la deuda",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun avatar(deudor: Deudor){
    // 1. El Avatar (Círculo con iniciales)
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(estados, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val iniciales = "${deudor.nombre.firstOrNull()?.uppercase() ?: ""}${deudor.apellido.firstOrNull()?.uppercase() ?: ""}"
        Text(text = iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun DialogoAbono(
    nombreDeudor: String,
    deudaMaximaUsd: Double,
    monedaVista: String,
    tasaBCV: Double,
    onDismiss: () -> Unit, // Cuando toca fuera de la caja o cancela
    onConfirmar: (Double) -> Unit // Cuando le da a guardar pasamos el monto en USD
) {
    var monedaAbono by remember { mutableStateOf(if (tasaBCV > 0 && monedaVista == "VES") "VES" else "USD") }
    var montoAbonoTexto by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    val limiteMaximo = if (monedaAbono == "VES" && tasaBCV > 0) deudaMaximaUsd * tasaBCV else deudaMaximaUsd

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Abonar a $nombreDeudor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                val limiteFormateado = if (monedaAbono == "VES" && tasaBCV > 0) 
                    "Bs. ${"%.2f".format(deudaMaximaUsd * tasaBCV)}" 
                    else NumberFormat.getCurrencyInstance(Locale("en", "US")).format(deudaMaximaUsd)

                Text(text = "Deuda actual: $limiteFormateado", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de moneda de abono (USD / VES) si hay tasa activa
                if (tasaBCV > 0) {
                    Row(
                        modifier = Modifier
                            .background(fondo2, shape = RoundedCornerShape(50))
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (monedaAbono == "USD") estados else Color.Transparent)
                                .clickable { 
                                    monedaAbono = "USD"
                                    montoAbonoTexto = ""
                                    mensajeError = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💵 USD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (monedaAbono == "USD") Color.White else Color.DarkGray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (monedaAbono == "VES") estados else Color.Transparent)
                                .clickable { 
                                    monedaAbono = "VES"
                                    montoAbonoTexto = ""
                                    mensajeError = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🇻🇪 Bs.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (monedaAbono == "VES") Color.White else Color.DarkGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = montoAbonoTexto,
                    onValueChange = { 
                        montoAbonoTexto = it
                        mensajeError = ""
                    },
                    label = { Text("Monto del abono (${if (monedaAbono == "VES") "Bs." else "USD"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Muestra equivalencia en vivo
                val montoIngresado = montoAbonoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                if (montoIngresado > 0.0 && tasaBCV > 0) {
                    val equivalenciaTexto = if (monedaAbono == "VES") {
                        val equivUsd = montoIngresado / tasaBCV
                        "Equivale a $${"%.2f".format(equivUsd)} USD"
                    } else {
                        val equivBs = montoIngresado * tasaBCV
                        "Equivale a Bs. ${"%.2f".format(equivBs)}"
                    }
                    Text(text = equivalenciaTexto, color = estados, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }

                // Muestra mensaje de error en rojo si se pasa
                if (mensajeError.isNotEmpty()) {
                    Text(text = mensajeError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancelar", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            val montoLimpio = montoAbonoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                            
                            if (montoLimpio <= 0) {
                                mensajeError = "Ingresa un monto válido"
                            } else if (montoLimpio > limiteMaximo + 0.01) {
                                mensajeError = "No puedes abonar más de la deuda"
                            } else {
                                // Convertir a USD si se ingresó en Bs.
                                val montoUsdFinal = if (monedaAbono == "VES" && tasaBCV > 0) {
                                    montoLimpio / tasaBCV
                                } else {
                                    montoLimpio
                                }
                                onConfirmar(montoUsdFinal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = fondo2)
                    ) {
                        Text("Guardar Abono", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoConfigurarPerfilNegocio(
    perfilActual: com.example.gestor_deudores.data.database.PerfilNegocio,
    onDismiss: () -> Unit,
    onGuardar: (com.example.gestor_deudores.data.database.PerfilNegocio) -> Unit
) {
    var nombreNegocio by remember { mutableStateOf(perfilActual.nombreNegocio) }
    var rifCedula by remember { mutableStateOf(perfilActual.rifCedula) }
    var telefonoContacto by remember { mutableStateOf(perfilActual.telefonoContacto) }
    var eslogan by remember { mutableStateOf(perfilActual.eslogan) }
    
    var saludoCobro by remember { mutableStateOf(perfilActual.saludoCobro) }
    var cierreCobro by remember { mutableStateOf(perfilActual.cierreCobro) }
    
    var saludoListo by remember { mutableStateOf(perfilActual.saludoListo) }
    var cierreListo by remember { mutableStateOf(perfilActual.cierreListo) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "⚙️ Personalizar Taller & WhatsApp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = estados
                )
                Text(
                    text = "Los datos de tu negocio se usarán en los reportes PDF y mensajes de WhatsApp.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = nombreNegocio,
                    onValueChange = { nombreNegocio = it },
                    label = { Text("Nombre Comercial del Negocio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rifCedula,
                        onValueChange = { rifCedula = it },
                        label = { Text("RIF / Cedula") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = telefonoContacto,
                        onValueChange = { telefonoContacto = it },
                        label = { Text("Teléfono WhatsApp") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = eslogan,
                    onValueChange = { eslogan = it },
                    label = { Text("Eslogan o Ciudad (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💬 Mensajes de WhatsApp (A Prueba de Error)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = estados
                )
                Text(
                    text = "Escribe tu saludo y tu mensaje de pago. Los saldos y nombres se insertan automáticamente sin llaves ni códigos.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // --- MENSAJE 1: RECORDATORIO DE DEUDA ---
                Text("1. Recordatorio de Deuda", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = saludoCobro,
                    onValueChange = { saludoCobro = it },
                    label = { Text("Saludo de Inicio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Vista previa bloqueada de la ficha
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📋 FICHA AUTOMÁTICA DE DEUDA\n👤 Cliente: Nombre del Deudor\n💰 Saldo: $45.00 USD (Bs. 2.250,00)\n📅 Próxima Cuota: $15.00 USD",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }

                OutlinedTextField(
                    value = cierreCobro,
                    onValueChange = { cierreCobro = it },
                    label = { Text("Mensaje de Cierre / Datos de Pago") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- MENSAJE 2: PEDIDO LISTO ---
                Text("2. Notificación de Pedido Listo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = saludoListo,
                    onValueChange = { saludoListo = it },
                    label = { Text("Saludo de Inicio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "🎉 FICHA AUTOMÁTICA DE PEDIDO\n👤 Cliente: Nombre del Cliente\n📦 Producto: 10x Franelas\n💰 Saldo a Cancelar: $20.00 USD",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }

                OutlinedTextField(
                    value = cierreListo,
                    onValueChange = { cierreListo = it },
                    label = { Text("Mensaje de Cierre / Retiro") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancelar", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            onGuardar(
                                perfilActual.copy(
                                    nombreNegocio = nombreNegocio.ifBlank { "Mi Taller" },
                                    rifCedula = rifCedula,
                                    telefonoContacto = telefonoContacto,
                                    eslogan = eslogan,
                                    saludoCobro = saludoCobro,
                                    cierreCobro = cierreCobro,
                                    saludoListo = saludoListo,
                                    cierreListo = cierreListo
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = estados)
                    ) {
                        Text("Guardar Cambios", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectorMoneda(
    monedaActual: String,
    tasaBCV: Double,
    cargando: Boolean,
    errorMsg: String?,
    onMonedaSeleccionada: (String) -> Unit,
    onRefrescarTasa: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botones de cambio USD / VES
        Row(
            modifier = Modifier
                .background(fondo2, shape = RoundedCornerShape(50))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (monedaActual == "USD") estados else Color.Transparent)
                    .clickable { onMonedaSeleccionada("USD") }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💵 USD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (monedaActual == "USD") Color.White else Color.DarkGray
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (monedaActual == "VES") estados else Color.Transparent)
                    .clickable { onMonedaSeleccionada("VES") }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🇻🇪 Bs.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (monedaActual == "VES") Color.White else Color.DarkGray
                )
            }
        }

        // Indicador de la tasa BCV
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onRefrescarTasa() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val textoTasa = if (tasaBCV > 0) "Tasa BCV: ${"%.2f".format(tasaBCV)} Bs" else "Cargando..."
            
            Text(
                text = if (errorMsg != null && tasaBCV > 0) "BCV: ${"%.2f".format(tasaBCV)} Bs" else textoTasa,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (errorMsg != null) componentes else estados
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar tasa",
                modifier = Modifier.size(16.dp),
                tint = estados
            )
        }
    }
}

@Composable
fun TarjetaResumen(
    totalPersonas: Int,
    dineroTotalUsd: Double,
    tasaBCV: Double = 0.0,
    monedaVista: String = "USD"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL POR COBRAR",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "$totalPersonas clientes pendientes",
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )
            }

            val totalBs = dineroTotalUsd * tasaBCV
            val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US")).format(dineroTotalUsd)
            val formatoBs = "Bs. ${"%.2f".format(totalBs)}"

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (monedaVista == "VES" && tasaBCV > 0) formatoBs else formatoUSD,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (tasaBCV > 0) {
                    Text(
                        text = if (monedaVista == "VES") "($formatoUSD)" else "($formatoBs)",
                        color = estados,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onCancelar() },
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        text = { Text(mensaje) },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Confirmar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = fondo
    )
}