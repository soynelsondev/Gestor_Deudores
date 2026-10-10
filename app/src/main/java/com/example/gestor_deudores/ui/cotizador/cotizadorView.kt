@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.cotizador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gestor_deudores.data.database.PlantillaCotizacion
import com.example.gestor_deudores.ui.home.BarraNavegacionInferior
import com.example.gestor_deudores.ui.rutas
import com.example.gestor_deudores.ui.theme.componentes
import com.example.gestor_deudores.ui.theme.estados
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2
import com.example.gestor_deudores.ui.theme.fondo_claro
import com.example.gestor_deudores.ui.theme.textoInactivo
import java.text.NumberFormat
import java.util.Locale

// ====================================================================
// 1. PANTALLA PRINCIPAL DEL COTIZADOR (CATÁLOGO)
// ====================================================================
@Composable
fun CotizadorPrincipal(
    viewModel: CotizadorViewModel, 
    navController: NavController,
    tasaBcvGlobal: Double
) {
    val uiState by viewModel.uiState.collectAsState()
    val plantillas by viewModel.listaPlantillas.collectAsState()
    
    // Controlamos qué tarjeta está expandida (guardamos su ID)
    var idTarjetaExpandida by remember { mutableStateOf<Int?>(null) }
    
    // Controlamos si mostramos la ventana modal gigante del formulario
    var mostrarFormulario by remember { mutableStateOf(false) }

    LaunchedEffect(tasaBcvGlobal) {
        viewModel.actualizarTasaBcv(tasaBcvGlobal)
    }

    // El Formulario Gigante (Oculto por defecto, se abre como un Dialog o BottomSheet)
    if (mostrarFormulario) {
        Dialog(onDismissRequest = { 
            mostrarFormulario = false
            viewModel.limpiarFormulario()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f) // Ocupa casi toda la pantalla
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = fondo)
            ) {
                // Aquí metemos TODO el formulario viejo (LazyColumn original)
                FormularioCotizacion(
                    viewModel = viewModel,
                    uiState = uiState,
                    onCerrar = { mostrarFormulario = false }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MIS COTIZACIONES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fondo2)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    viewModel.limpiarFormulario()
                    mostrarFormulario = true 
                },
                containerColor = estados,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva Plantilla")
            }
        },
        bottomBar = {
            BarraNavegacionInferior(
                rutaActual = rutas.COTIZADOR,
                onIrAInicio = { navController.navigate(rutas.HOME) },
                onIrAPedidos = { navController.navigate(rutas.PEDIDOS) },
                onIrACotizar = {},
                onIrAResumen = { navController.navigate(rutas.RESUMEN) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Un pequeño aviso sobre la tasa BCV activa
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Catálogo de Precios", fontWeight = FontWeight.Bold, color = estados, fontSize = 16.sp)
                    Text("Tasa BCV: Bs. $tasaBcvGlobal", fontSize = 12.sp, color = Color.Gray)
                }
            }

            if (plantillas.isEmpty()) {
                item {
                    Text("Aún no tienes plantillas de cotización creadas.\nToca el botón '+' para crear tu primera fórmula.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 40.dp))
                }
            } else {
                items(plantillas) { plantilla ->
                    val estaExpandida = idTarjetaExpandida == plantilla.id
                    
                    TarjetaPlantillaExpandible(
                        plantilla = plantilla,
                        viewModel = viewModel,
                        expandida = estaExpandida,
                        onClickTarjeta = {
                            idTarjetaExpandida = if (estaExpandida) null else plantilla.id
                        },
                        onEditarClick = {
                            viewModel.cargarPlantilla(plantilla)
                            mostrarFormulario = true
                        },
                        onDuplicarClick = {
                            viewModel.duplicarPlantilla(plantilla)
                        }
                    )
                }
            }
        }
    }
}

// ====================================================================
// NUEVO COMPONENTE: TARJETA EXPANDIBLE DEL CATÁLOGO
// ====================================================================
@Composable
fun TarjetaPlantillaExpandible(
    plantilla: PlantillaCotizacion,
    viewModel: CotizadorViewModel,
    expandida: Boolean,
    onClickTarjeta: () -> Unit,
    onEditarClick: () -> Unit,
    onDuplicarClick: () -> Unit
) {
    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US"))
    
    // Obtenemos los resultados calculados directamente desde el ViewModel (Cero fórmulas en la Vista)
    val res = viewModel.calcularResultadosPlantilla(plantilla)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Hace la animación suave al expandir/contraer
            .clickable { onClickTarjeta() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (expandida) estados else fondo2),
        elevation = CardDefaults.cardElevation(if (expandida) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera siempre visible
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(plantilla.nombrePlantilla, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Precio: ${formatoUSD.format(res.precioUsd)}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        if (plantilla.esPlantillaMayor) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🏷️ MAYOR (≥${plantilla.minimoUnidadesMayor} pcs)", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Icon(
                    imageVector = if (expandida) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            // Cuerpo oculto (La Radiografía)
            AnimatedVisibility(visible = expandida) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("RADIOGRAFÍA FINANCIERA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Costo de Producción Unitario:", color = Color.White, fontSize = 14.sp)
                        Text(formatoUSD.format(res.costoTotalProduccionUsd), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // PRECIO DE VENTA Y GANANCIA
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PRECIO DE VENTA (${plantilla.porcentajeGanancia.toInt()}%):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(formatoUSD.format(res.precioUsd), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ganancia libre por pieza:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("+ " + formatoUSD.format(res.gananciaUsd), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones Duplicar y Editar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDuplicarClick,
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Text("📋 Copiar", color = Color.White, fontSize = 11.sp)
                        }
                        Button(
                            onClick = onEditarClick,
                            colors = ButtonDefaults.buttonColors(containerColor = fondo),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Editar Costos", color = estados, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 3. EL FORMULARIO GIGANTE DE EDICIÓN (Oculto en un Dialog)
// ====================================================================
@Composable
fun FormularioCotizacion(
    viewModel: CotizadorViewModel,
    uiState: CotizadorUiState,
    onCerrar: () -> Unit
) {
    val plantillas by viewModel.listaPlantillas.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo_claro)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (uiState.idActual > 0) "EDITAR PLANTILLA" else "NUEVA PLANTILLA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = estados)
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }

        // --- SECCIÓN 1: NOMBRE DE PLANTILLA Y PRESETS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.nombrePlantilla,
                    onValueChange = { viewModel.onNombrePlantillaChange(it) },
                    label = { Text("Nombre de la Plantilla") },
                    placeholder = { Text("Ej. Taza Mágica, Franela DTF") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("⚡ Cargar Receta Prearmada (1 Clic):", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(com.example.gestor_deudores.data.utils.CotizadorPresetsUtils.obtenerPresets()) { preset ->
                        AssistChip(
                            onClick = { viewModel.cargarPreset(preset) },
                            label = { Text("${preset.icono} ${preset.titulo}", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // --- SECCIÓN 1: MATERIA PRIMA (Pieza, Empaque y Extras) ---
        item {
            SeccionCard(titulo = "1. Materia Prima (Base)") {
                    // Pieza
                    TextoTituloConAyuda(
                        titulo = "Pieza para sublimar (Franela, Taza, etc):",
                        mensajeAyuda = "Compraste una caja de 36 tazas a $45. Escribe 45 en ¿Cuánto pagaste? y 36 en Piezas. Zubli calculará el costo exacto por unidad."
                    )
                    CampoMonedaCantidad(
                        precio = uiState.precioPaquetePieza,
                        moneda = uiState.monedaPaquetePieza,
                        cantidad = uiState.cantidadPaquetePieza,
                        onPrecioChange = { viewModel.onPrecioPaquetePiezaChange(it) },
                        onMonedaChange = { viewModel.onMonedaPaquetePiezaChange(it) },
                        onCantidadChange = { viewModel.onCantidadPaquetePiezaChange(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Empaque
                    TextoTituloConAyuda(
                        titulo = "Empaque (Bolsa, Caja):",
                        mensajeAyuda = "Compraste un paquete de 100 bolsas a $5. Escribe 5 en ¿Cuánto pagaste? y 100 en Piezas."
                    )
                    CampoMonedaCantidad(
                        precio = uiState.precioPaqueteEmpaque,
                        moneda = uiState.monedaPaqueteEmpaque,
                        cantidad = uiState.cantidadPaqueteEmpaque,
                        onPrecioChange = { viewModel.onPrecioPaqueteEmpaqueChange(it) },
                        onMonedaChange = { viewModel.onMonedaPaqueteEmpaqueChange(it) },
                        onCantidadChange = { viewModel.onCantidadPaqueteEmpaqueChange(it) }
                    )
                    
                    // --- NUEVO: Papel de Sublimación Ocultable ---
                    Spacer(modifier = Modifier.height(8.dp))
                    var mostrarPapel by remember { mutableStateOf(uiState.precioPaquetePapel.isNotEmpty()) }
                    
                    if (mostrarPapel) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextoTituloConAyuda(
                            titulo = "Papel de Sublimación (Resma):",
                            mensajeAyuda = "Si 1 resma de 100 hojas te costó $10 y usas 1/3 de hoja por taza, escribe $10 y 300 piezas (100x3)."
                        )
                        CampoMonedaCantidad(
                            precio = uiState.precioPaquetePapel,
                            moneda = uiState.monedaPaquetePapel,
                            cantidad = uiState.cantidadPaquetePapel,
                            onPrecioChange = { viewModel.onPrecioPaquetePapelChange(it) },
                            onMonedaChange = { viewModel.onMonedaPaquetePapelChange(it) },
                            onCantidadChange = { viewModel.onCantidadPaquetePapelChange(it) }
                        )
                        TextButton(
                            onClick = { 
                                mostrarPapel = false
                                viewModel.onPrecioPaquetePapelChange("") // Lo borramos al ocultarlo
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Quitar papel", color = Color.Red, fontSize = 11.sp)
                        }
                    } else {
                        TextButton(
                            onClick = { mostrarPapel = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(" Agregar costo de Papel")
                        }
                    }
                }
            }

            // --- SECCIÓN 2: SERVICIOS DIRECTOS (DTF, Pasajes, Diseño) ---
            item {
                SeccionCard(titulo = "2. Servicios Directos (DTF, Pasajes, Diseño)") {
                    // DTF
                    TextoTituloConAyuda(
                        titulo = "Costo de Impresión / DTF por Metro:",
                        mensajeAyuda = "Escribe el precio del metro de DTF (ej. $8.50) y cuántas estampas de tu diseño salen por metro. Usa el calculador de abajo si no sabes cuántas entran."
                    )
                    CampoMonedaCantidad(
                        precio = uiState.precioTotalDtf,
                        moneda = uiState.monedaDtf,
                        cantidad = uiState.rendimientoDtf,
                        onPrecioChange = { viewModel.onPrecioTotalDtfChange(it) },
                        onMonedaChange = { viewModel.onMonedaDtfChange(it) },
                        onCantidadChange = { viewModel.onRendimientoDtfChange(it) }
                    )

                    // --- AYUDANTE CALCULADOR DE DTF EN 2 FILAS ---
                    var mostrarAyudanteDtf by remember { mutableStateOf(false) }
                    var anchoRolloDtf by remember { mutableStateOf("60") }
                    var largoRolloDtf by remember { mutableStateOf("100") }
                    var anchoDisenoDtf by remember { mutableStateOf("10") }
                    var altoDisenoDtf by remember { mutableStateOf("10") }

                    if (mostrarAyudanteDtf) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.04f))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📐 Calculador de Estampas por Rollo DTF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = estados)
                                
                                // FILA 1: MEDIDAS DEL ROLLO
                                Text("Rollo de Film (cm):", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = anchoRolloDtf,
                                        onValueChange = { 
                                            if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) {
                                                anchoRolloDtf = it
                                                viewModel.calcularAyudanteDtf(anchoRolloDtf, largoRolloDtf, anchoDisenoDtf, altoDisenoDtf)
                                            }
                                        },
                                        placeholder = { Text("Ancho", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = largoRolloDtf,
                                        onValueChange = { 
                                            if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) {
                                                largoRolloDtf = it
                                                viewModel.calcularAyudanteDtf(anchoRolloDtf, largoRolloDtf, anchoDisenoDtf, altoDisenoDtf)
                                            }
                                        },
                                        placeholder = { Text("Largo", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }

                                // FILA 2: MEDIDAS DEL ESTAMPADO
                                Text("Tu Estampado (cm):", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = anchoDisenoDtf,
                                        onValueChange = { 
                                            if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) {
                                                anchoDisenoDtf = it
                                                viewModel.calcularAyudanteDtf(anchoRolloDtf, largoRolloDtf, anchoDisenoDtf, altoDisenoDtf)
                                            }
                                        },
                                        placeholder = { Text("Ancho", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = altoDisenoDtf,
                                        onValueChange = { 
                                            if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) {
                                                altoDisenoDtf = it
                                                viewModel.calcularAyudanteDtf(anchoRolloDtf, largoRolloDtf, anchoDisenoDtf, altoDisenoDtf)
                                            }
                                        },
                                        placeholder = { Text("Alto", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    } else {
                        TextButton(onClick = { mostrarAyudanteDtf = true }) {
                            Text("📐 ¿No sabes cuántas estampas salen por metro? Calcular aquí", fontSize = 11.sp, color = estados)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transporte
                    Text("Transporte/Envío:", fontSize = 12.sp, color = Color.Gray)
                    CampoMonedaCantidad(
                        precio = uiState.costoTransporte,
                        moneda = uiState.monedaTransporte,
                        cantidad = uiState.rendimientoTransporte,
                        onPrecioChange = { viewModel.onCostoTransporteChange(it) },
                        onMonedaChange = { viewModel.onMonedaTransporteChange(it) },
                        onCantidadChange = { viewModel.onRendimientoTransporteChange(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Diseño
                    Text("Diseño extra:", fontSize = 12.sp, color = Color.Gray)
                    CampoMonedaCantidad(
                        precio = uiState.costoDiseno,
                        moneda = uiState.monedaDiseno,
                        cantidad = uiState.rendimientoDiseno,
                        onPrecioChange = { viewModel.onCostoDisenoChange(it) },
                        onMonedaChange = { viewModel.onMonedaDisenoChange(it) },
                        onCantidadChange = { viewModel.onRendimientoDisenoChange(it) }
                    )
                    
                    // --- NUEVO: Insumos Extra (Imán, Resina, etc) ---
                    Spacer(modifier = Modifier.height(8.dp))
                    var mostrarExtra by remember { mutableStateOf(uiState.costoExtra.isNotEmpty()) }
                    
                    if (mostrarExtra) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Insumos Extra (Imán, Resina, Ganchos, etc):", fontSize = 12.sp, color = Color.Gray)
                        CampoMonedaCantidad(
                            precio = uiState.costoExtra,
                            moneda = uiState.monedaExtra,
                            cantidad = uiState.rendimientoExtra,
                            labelPrecio = "Costo Total",
                            labelCantidad = "Piezas",
                            onPrecioChange = { viewModel.onCostoExtraChange(it) },
                            onMonedaChange = { viewModel.onMonedaExtraChange(it) },
                            onCantidadChange = { viewModel.onRendimientoExtraChange(it) }
                        )
                        TextButton(
                            onClick = { 
                                mostrarExtra = false
                                viewModel.onCostoExtraChange("") // Lo borramos
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Quitar extras", color = Color.Red, fontSize = 11.sp)
                        }
                    } else {
                        TextButton(
                            onClick = { mostrarExtra = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(" Agregar insumos extra (Imanes, etc)")
                        }
                    }
                }
            }

            // --- SECCIÓN 3: INSUMOS EXTRAS ADICIONALES (Dynamic Lego Style) ---
            item {
                var mostrarDialogoInsumo by remember { mutableStateOf(false) }

                var insumoAEditar by remember { mutableStateOf<com.example.gestor_deudores.data.database.InsumoCotizacion?>(null) }

                if (mostrarDialogoInsumo) {
                    DialogoAgregarInsumo(
                        viewModel = viewModel,
                        onDismiss = { mostrarDialogoInsumo = false },
                        onAgregar = { nuevoInsumo ->
                            viewModel.agregarInsumo(nuevoInsumo)
                        }
                    )
                }

                if (insumoAEditar != null) {
                    DialogoAgregarInsumo(
                        viewModel = viewModel,
                        insumoInicial = insumoAEditar,
                        onDismiss = { insumoAEditar = null },
                        onAgregar = { insumoActualizado ->
                            viewModel.editarInsumo(insumoActualizado)
                            insumoAEditar = null
                        }
                    )
                }

                SeccionCard(titulo = "3. Insumos Extras Adicionales") {
                    if (uiState.listaInsumos.isEmpty()) {
                        Text(
                            text = "Agrega cualquier insumo adicional (Imanes, Resina, Cintas, Cajas especiales, etc.)",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        uiState.listaInsumos.forEach { insumo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.04f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val iconoCat = when (insumo.categoria) {
                                        "MATERIA_PRIMA" -> "📦"
                                        "INSUMO_EXTRA" -> "🖨️"
                                        else -> "🚚"
                                    }
                                    Text("$iconoCat ${insumo.nombre}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "Lote: $${insumo.precioLote} ${insumo.moneda} ÷ ${insumo.rendimientoCantidad} pcs = $${String.format(Locale.US, "%.2f", insumo.calcularCostoUnitarioUsd(1.0))}/u",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                Row {
                                    IconButton(onClick = { insumoAEditar = insumo }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = estados, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { viewModel.eliminarInsumo(insumo.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { mostrarDialogoInsumo = true },
                        colors = ButtonDefaults.buttonColors(containerColor = estados),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("➕ Agregar Insumo Extra", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- SECCIÓN 4: PORCENTAJES Y GANANCIA (Fases 1 a 4) ---
            item {
                SeccionCard(titulo = "4. Operatividad y Modo de Ganancia") {
                    Text("Modo de Cotización:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = uiState.modoCosteo == "RAPIDO",
                            onClick = { viewModel.onModoCosteoChange("RAPIDO") },
                            label = { Text("⚡ Modo Rápido", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = uiState.modoCosteo == "DETALLADO",
                            onClick = { viewModel.onModoCosteoChange("DETALLADO") },
                            label = { Text("🔬 Modo Detallado", fontSize = 11.sp) }
                        )
                    }

                    if (uiState.alertaDobleConteo) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Nota: Tienes un % Operativo activo además de insumos detallados. Asegúrate de no estar contando dos veces los mismos gastos.",
                                color = Color(0xFFE65100),
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (uiState.modoCosteo == "DETALLADO") {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🔬 Consumibles y Desgaste de Equipos:", fontSize = 12.sp, color = estados, fontWeight = FontWeight.Bold)

                        

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tinta de Sublimación (Botella/ml):", fontSize = 11.sp, color = Color.Gray)
                        CampoMonedaCantidad(
                            precio = uiState.costoExtra,
                            moneda = uiState.monedaExtra,
                            cantidad = uiState.rendimientoExtra,
                            onPrecioChange = { viewModel.onCostoExtraChange(it) },
                            onMonedaChange = { viewModel.onMonedaExtraChange(it) },
                            onCantidadChange = { viewModel.onRendimientoExtraChange(it) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Desgaste de Prensa / Plancha ($/pcs):", fontSize = 11.sp, color = Color.Gray)
                        CampoMonedaCantidad(
                            precio = uiState.costoDiseno,
                            moneda = uiState.monedaDiseno,
                            cantidad = uiState.rendimientoDiseno,
                            onPrecioChange = { viewModel.onCostoDisenoChange(it) },
                            onMonedaChange = { viewModel.onMonedaDisenoChange(it) },
                            onCantidadChange = { viewModel.onRendimientoDisenoChange(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SliderPorcentaje(
                        titulo = "Costos Operativos (Luz/Merma):",
                        valor = uiState.porcentajeOperativo,
                        rango = 0f..50f,
                        colorActivo = Color.Gray,
                        onValorChange = { viewModel.onPorcentajeOperativoChange(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Método de Ganancia:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = uiState.tipoGanancia == com.example.gestor_deudores.data.utils.TipoGanancia.SOBRE_COSTO,
                            onClick = { viewModel.onTipoGananciaChange(com.example.gestor_deudores.data.utils.TipoGanancia.SOBRE_COSTO) },
                            label = { Text("Por Costo", fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.tipoGanancia == com.example.gestor_deudores.data.utils.TipoGanancia.SOBRE_VENTA,
                            onClick = { viewModel.onTipoGananciaChange(com.example.gestor_deudores.data.utils.TipoGanancia.SOBRE_VENTA) },
                            label = { Text("En Venta", fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.tipoGanancia == com.example.gestor_deudores.data.utils.TipoGanancia.GANANCIA_FIJA,
                            onClick = { viewModel.onTipoGananciaChange(com.example.gestor_deudores.data.utils.TipoGanancia.GANANCIA_FIJA) },
                            label = { Text("$ Fija ", fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (uiState.tipoGanancia == com.example.gestor_deudores.data.utils.TipoGanancia.GANANCIA_FIJA) {
                        OutlinedTextField(
                            value = uiState.gananciaFijaUsd,
                            onValueChange = { nuevoTexto ->
                                if (nuevoTexto.all { c -> c.isDigit() || c == '.' || c == ',' } &&
                                    nuevoTexto.count { c -> c == '.' || c == ',' } <= 1) {
                                    viewModel.onGananciaFijaChange(nuevoTexto)
                                }
                            },
                            placeholder = { Text("Ej. 3.50", fontSize = 12.sp, color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        SliderPorcentaje(
                            titulo = "Ganancia al Detal:",
                            valor = uiState.porcentajeGanancia,
                            rango = 0f..300f,
                            colorActivo = estados,
                            onValorChange = { viewModel.onPorcentajeGananciaChange(it) }
                        )

                        SliderPorcentaje(
                            titulo = "Ganancia al Mayor:",
                            valor = uiState.porcentajeGananciaMayor,
                            rango = 0f..100f,
                            colorActivo = Color(0xFFE29578),
                            onValorChange = { viewModel.onPorcentajeGananciaMayorChange(it) }
                        )
                    }

                    // --- MANO DE OBRA Y COMISIÓN OPCIONAL ---
                    var mostrarAvanzado by remember { mutableStateOf(uiState.minutosPorPieza.isNotEmpty() || uiState.comisionPorcentaje > 0) }

                    if (mostrarAvanzado) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("🛠️ Mano de Obra y Comisión de Cobro (Avanzado):", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.minutosPorPieza,
                                onValueChange = { viewModel.onMinutosPorPiezaChange(it) },
                                label = { Text("Minutos / Pieza") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = uiState.tarifaPorHoraUsd,
                                onValueChange = { viewModel.onTarifaPorHoraChange(it) },
                                label = { Text("Tarifa $/Hora") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        SliderPorcentaje(
                            titulo = "Comisión de Cobro Punto/Tarjeta (%):",
                            valor = uiState.comisionPorcentaje,
                            rango = 0f..20f,
                            colorActivo = Color.DarkGray,
                            onValorChange = { viewModel.onComisionPorcentajeChange(it) }
                        )

                        TextButton(
                            onClick = { 
                                mostrarAvanzado = false
                                viewModel.onMinutosPorPiezaChange("")
                                viewModel.onTarifaPorHoraChange("")
                                viewModel.onComisionPorcentajeChange(0f)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Ocultar avanzado", color = Color.Red, fontSize = 11.sp)
                        }
                    } else {
                        TextButton(
                            onClick = { mostrarAvanzado = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(" Agregar Mano de Obra o Comisión de Punto")
                        }
                    }
                }
            }

            // --- SECCIÓN 6: TARJETA DE RESULTADOS FINANCIEROS ---
            item {
                TarjetaResultadosFinancieros(uiState)
            }

            // --- SECCIÓN 7: BOTONES DE ACCIÓN ---
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Botón Eliminar o Limpiar
                    if (uiState.idActual > 0) {
                        OutlinedButton(
                            onClick = { 
                                // Eliminar y limpiar
                                val plantilla = plantillas.find { it.id == uiState.idActual }
                                if (plantilla != null) viewModel.eliminarPlantilla(plantilla)
                                viewModel.limpiarFormulario()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    } else {
                        TextButton(onClick = { viewModel.limpiarFormulario() }) {
                            Text("Limpiar", color = Color.Gray)
                        }
                    }

                    // Botón Guardar / Actualizar
                    Button(
                        onClick = { viewModel.guardarPlantilla(onExito = onCerrar) },
                        colors = ButtonDefaults.buttonColors(containerColor = estados),
                        enabled = uiState.nombrePlantilla.isNotBlank()
                    ) {
                        Text(if (uiState.idActual > 0) "Actualizar Plantilla" else "Guardar Plantilla", color = Color.White)
                    }
                }
                
                // Espacio final
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

// 2. COMPONENTES REUTILIZABLES (UI LIMPIA)
@Composable
fun SeccionCard(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, color = estados, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// Tarjeta que representa una plantilla guardada en el carrusel superior
@Composable
fun TarjetaPlantillaGuardada(plantilla: PlantillaCotizacion, seleccionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (seleccionada) estados else fondo2)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = plantilla.nombrePlantilla,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Margen: %.0f%%".format(plantilla.porcentajeGanancia),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

// Selector de precio, moneda y cantidad en una sola fila
@Composable
fun CampoMonedaCantidad(
    precio: String,
    moneda: String,
    cantidad: String,
    labelPrecio: String = "¿Cuánto pagaste?",
    labelCantidad: String = "Piezas",
    onPrecioChange: (String) -> Unit,
    onMonedaChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de Precio
            OutlinedTextField(
                value = precio,
                onValueChange = { nuevoTexto ->
                    if (nuevoTexto.all { c -> c.isDigit() || c == '.' || c == ',' } &&
                        nuevoTexto.count { c -> c == '.' || c == ',' } <= 1) {
                        onPrecioChange(nuevoTexto)
                    }
                },
                placeholder = { Text(labelPrecio, fontSize = 12.sp, color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Selector USD / VES
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(fondo2)
                    .clickable { onMonedaChange(if (moneda == "USD") "VES" else "USD") }
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Text(if (moneda == "USD") "USD" else "Bs", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Campo de Cantidad
            OutlinedTextField(
                value = cantidad,
                onValueChange = { if (it.all { c -> c.isDigit() }) onCantidadChange(it) },
                placeholder = { Text(labelCantidad, fontSize = 12.sp, color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Vista previa inmediata por línea (= $X.XX por pieza)
        val pD = precio.replace(",", ".").toDoubleOrNull() ?: 0.0
        val cI = cantidad.toIntOrNull() ?: 1
        if (pD > 0.0) {
            val unitario = pD / (if (cI > 0) cI else 1)
            Text(
                text = "= $${String.format(Locale.US, "%.2f", unitario)} por pieza",
                color = Color(0xFF2E7D32),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

// Selector de precio y moneda simple (sin cantidad)
@Composable
fun CampoMonedaSimple(
    precio: String,
    moneda: String,
    labelPrecio: String = "Costo Total",
    onPrecioChange: (String) -> Unit,
    onMonedaChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = precio,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) onPrecioChange(it) },
            label = { Text(labelPrecio) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(fondo2)
                .clickable { onMonedaChange(if (moneda == "USD") "VES" else "USD") }
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(if (moneda == "USD") "USD" else "Bs", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// Barra deslizable para escoger porcentajes
@Composable
fun SliderPorcentaje(
    titulo: String,
    valor: Float,
    rango: ClosedFloatingPointRange<Float>,
    colorActivo: Color,
    onValorChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(titulo, fontSize = 13.sp, color = Color.DarkGray)
            Text("${valor.toInt()}%", fontWeight = FontWeight.Bold, color = colorActivo)
        }
        Slider(
            value = valor,
            onValueChange = onValorChange,
            valueRange = rango,
            colors = SliderDefaults.colors(thumbColor = colorActivo, activeTrackColor = colorActivo)
        )
    }
}

// Tarjeta gigante de resultados financieros
@Composable
fun TarjetaResultadosFinancieros(uiState: CotizadorUiState) {
    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (uiState.esPerdida) Color(0xFFC62828) else fondo2)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("📊 RESUMEN DE COTIZACIÓN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.costoTotalProduccionUsd == 0.0) {
                Text(
                    text = "💡 Completa la prenda o base para ver tu precio sugerido y ganancia.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            } else {
                if (uiState.esPerdida) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠️ ATENCIÓN: Con este precio pierdes dinero. El precio sugerido debe superar el costo de producción ($${uiState.costoTotalProduccionUsd}).",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Costo Real
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Costo Producción por Pieza:", color = Color.White, fontSize = 13.sp)
                    Text(formatoUSD.format(uiState.costoTotalProduccionUsd), color = Color.White, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                // PRECIO DE VENTA DETAL
                val descGanancia = when (uiState.tipoGanancia) {
                    com.example.gestor_deudores.data.utils.TipoGanancia.SOBRE_COSTO -> "${uiState.porcentajeGanancia.toInt()}% Ganancia"
                    com.example.gestor_deudores.data.utils.TipoGanancia.SOBRE_VENTA -> "${uiState.porcentajeGanancia.toInt()}% Margen"
                    else -> "$${uiState.gananciaFijaUsd} Fijo/pc"
                }

                Text("PRECIO VENTA DETAL ($descGanancia):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Precio Sugerido Detal:", color = Color.White, fontSize = 14.sp)
                    Text(formatoUSD.format(uiState.precioSugeridoUsd), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tu ganancia libre por pieza:", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    Text("+ " + formatoUSD.format(uiState.gananciaNetaUsd), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // PRECIO DE VENTA MAYOR (6+ pcs)
                Text("PRECIO VENTA MAYOR (${uiState.porcentajeGananciaMayor.toInt()}% Ganancia):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Precio Sugerido Mayor:", color = Color.White, fontSize = 13.sp)
                    Text(formatoUSD.format(uiState.precioSugeridoMayorUsd), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tu ganancia libre por pieza:", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    Text("+ " + formatoUSD.format(uiState.gananciaNetaMayorUsd), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Botón Tabla de Escalas por Cantidad
                var mostrarTablaEscalas by remember { mutableStateOf(false) }

                if (mostrarTablaEscalas) {
                    DialogoTablaEscalas(
                        tabla = uiState.tablaEscalasResultado,
                        onDismiss = { mostrarTablaEscalas = false }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { mostrarTablaEscalas = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text("📊 Ver Tabla de Precios por Cantidad (1, 6, 12, 24, 50 pcs)", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DialogoTablaEscalas(
    tabla: List<com.example.gestor_deudores.data.utils.ResultadoEscalaCantidad>,
    onDismiss: () -> Unit
) {
    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US"))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                Text("📊 Tabla de Precios por Cantidad", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = estados)
                Text("Precios unitarios y totales según el volumen del pedido.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                // Encabezados
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f)).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Volumen", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text("Costo /u", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text("Precio /u", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text("Ganancia /u", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                    Text("Total Lote", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                }

                tabla.forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${fila.desdeCantidad} pcs", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Text(formatoUSD.format(fila.costoUnitarioUsd), fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                        Text(formatoUSD.format(fila.precioUnitarioUsd), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = estados, modifier = Modifier.weight(1f))
                        Text("+${formatoUSD.format(fila.gananciaUnitarioUsd)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                        Text(formatoUSD.format(fila.precioTotalLoteUsd), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cerrar", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DialogoAgregarInsumo(
    viewModel: CotizadorViewModel,
    insumoInicial: com.example.gestor_deudores.data.database.InsumoCotizacion? = null,
    onDismiss: () -> Unit,
    onAgregar: (com.example.gestor_deudores.data.database.InsumoCotizacion) -> Unit
) {
    val biblioteca by viewModel.listaInsumosBiblioteca.collectAsState()
    var expandidoBiblioteca by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf(insumoInicial?.nombre ?: "") }
    var categoria by remember { mutableStateOf(insumoInicial?.categoria ?: "MATERIA_PRIMA") }
    var tipoCosto by remember { mutableStateOf(insumoInicial?.tipoCosto ?: "POR_PIEZA") }
    var precioLoteTexto by remember { mutableStateOf(insumoInicial?.precioLote?.let { if (it > 0) it.toString() else "" } ?: "") }
    var moneda by remember { mutableStateOf(insumoInicial?.moneda ?: "USD") }
    var rendimientoTexto by remember { mutableStateOf(insumoInicial?.rendimientoCantidad?.toString() ?: "1") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                Text("➕ Agregar Insumo o Servicio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = estados)
                Spacer(modifier = Modifier.height(12.dp))

                if (biblioteca.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandidoBiblioteca = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("📚 Elegir de Mis Insumos Guardados", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = expandidoBiblioteca,
                            onDismissRequest = { expandidoBiblioteca = false }
                        ) {
                            biblioteca.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.nombre} ($${item.precioLote} ${item.moneda} / ${item.unidadLote})") },
                                    onClick = {
                                        nombre = item.nombre
                                        categoria = item.categoria
                                        precioLoteTexto = item.precioLote.toString()
                                        moneda = item.moneda
                                        rendimientoTexto = item.cantidadLote.toString()
                                        expandidoBiblioteca = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Insumo") },
                    placeholder = { Text("Ej. Taza, Metro DTF, Caja, Pasaje") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Categoría (para los Sobres del Resumen):", fontSize = 12.sp, color = Color.Gray)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = categoria == "MATERIA_PRIMA",
                        onClick = { categoria = "MATERIA_PRIMA" },
                        label = { Text("📦 Base", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = categoria == "INSUMO_EXTRA",
                        onClick = { categoria = "INSUMO_EXTRA" },
                        label = { Text("🖨️ Insumo", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = categoria == "SERVICIO",
                        onClick = { categoria = "SERVICIO" },
                        label = { Text("🚚 Servicio", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Asignación del Costo:", fontSize = 12.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = tipoCosto == "POR_PIEZA",
                        onClick = { tipoCosto = "POR_PIEZA" },
                        label = { Text("🔘 Por Pieza", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = tipoCosto == "POR_PEDIDO",
                        onClick = { tipoCosto = "POR_PEDIDO" },
                        label = { Text("📦 Fijo por Pedido", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                CampoMonedaCantidad(
                    precio = precioLoteTexto,
                    moneda = moneda,
                    cantidad = rendimientoTexto,
                    labelPrecio = "Precio Lote",
                    labelCantidad = "Piezas Rinde",
                    onPrecioChange = { precioLoteTexto = it },
                    onMonedaChange = { moneda = it },
                    onCantidadChange = { rendimientoTexto = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                    Button(
                        onClick = {
                            val pD = precioLoteTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val rI = rendimientoTexto.toIntOrNull() ?: 1
                            if (nombre.isNotBlank() && pD > 0) {
                                onAgregar(
                                    (insumoInicial ?: com.example.gestor_deudores.data.database.InsumoCotizacion()).copy(
                                        nombre = nombre.trim(),
                                        categoria = categoria,
                                        precioLote = pD,
                                        moneda = moneda,
                                        rendimientoCantidad = if (rI > 0) rI else 1,
                                        tipoCosto = tipoCosto
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = estados)
                    ) {
                        Text("Agregar a la Receta", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TextoTituloConAyuda(
    titulo: String,
    mensajeAyuda: String
) {
    var mostrarAyuda by remember { mutableStateOf(false) }

    if (mostrarAyuda) {
        AlertDialog(
            onDismissRequest = { mostrarAyuda = false },
            title = { Text("💡 Ayuda del Campo", fontWeight = FontWeight.Bold, color = estados) },
            text = { Text(mensajeAyuda, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { mostrarAyuda = false }) {
                    Text("Entendido", color = estados, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Ayuda",
            tint = estados,
            modifier = Modifier
                .size(16.dp)
                .clickable { mostrarAyuda = true }
        )
    }
}
