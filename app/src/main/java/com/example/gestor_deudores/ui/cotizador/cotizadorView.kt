@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.cotizador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                onIrAInicio = { navController.navigate(rutas.HOME) },
                onIrAPedidos = { navController.navigate(rutas.PEDIDOS) },
                onIrACotizar = {} 
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
                        tasaBcvGlobal = tasaBcvGlobal,
                        expandida = estaExpandida,
                        onClickTarjeta = {
                            // Si tocas la que ya está abierta, se cierra. Si tocas otra, se abre esa.
                            idTarjetaExpandida = if (estaExpandida) null else plantilla.id
                        },
                        onEditarClick = {
                            viewModel.cargarPlantilla(plantilla)
                            mostrarFormulario = true
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
    tasaBcvGlobal: Double,
    expandida: Boolean,
    onClickTarjeta: () -> Unit,
    onEditarClick: () -> Unit
) {
    val formatoUSD = NumberFormat.getCurrencyInstance(Locale("en", "US"))
    
    // Matemática rápida para mostrar en la tarjeta sin tener que cargarla al ViewModel
    fun aUsd(valor: Double, moneda: String): Double = if (moneda == "VES") valor / tasaBcvGlobal else valor
    
    val costoPZ = aUsd(plantilla.precioPaquetePieza, plantilla.monedaPaquetePieza) / plantilla.cantidadPaquetePieza
    val costoEmp = aUsd(plantilla.precioPaqueteEmpaque, plantilla.monedaPaqueteEmpaque) / plantilla.cantidadPaqueteEmpaque
    val costoPapel = aUsd(plantilla.precioPaquetePapel, plantilla.monedaPaquetePapel) / plantilla.cantidadPaquetePapel
    val costoDtf = aUsd(plantilla.precioTotalDtf, plantilla.monedaDtf) / plantilla.rendimientoDtf
    val costoTrans = aUsd(plantilla.costoTransporte, plantilla.monedaTransporte) / plantilla.rendimientoTransporte
    val costoDiseno = aUsd(plantilla.costoDiseno, plantilla.monedaDiseno) / plantilla.rendimientoDiseno
    
    val subtotal = costoPZ + costoEmp + costoPapel + costoDtf + costoTrans + costoDiseno
    val costoTotal = subtotal + (subtotal * (plantilla.porcentajeOperativo / 100.0))
    
    val precioDetal = if(plantilla.porcentajeGananciaDetal < 100) costoTotal / (1.0 - (plantilla.porcentajeGananciaDetal / 100.0)) else 0.0
    val precioMayor = if(plantilla.porcentajeGananciaMayor < 100) costoTotal / (1.0 - (plantilla.porcentajeGananciaMayor / 100.0)) else 0.0

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
                    Text("Detal: ${formatoUSD.format(precioDetal)} | Mayor: ${formatoUSD.format(precioMayor)}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
                Icon(
                    imageVector = if (expandida) Icons.Default.Close else Icons.Default.Add, // Solo un icono indicativo
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
                        Text(formatoUSD.format(costoTotal), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // VENTA AL DETAL
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("VENTA AL DETAL (${plantilla.porcentajeGananciaDetal.toInt()}%):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(formatoUSD.format(precioDetal), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ganancia libre por pieza:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("+ " + formatoUSD.format(precioDetal - costoTotal), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // VENTA AL MAYOR
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.1f)).padding(8.dp)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("VENTA AL MAYOR (${plantilla.porcentajeGananciaMayor.toInt()}%):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(formatoUSD.format(precioMayor), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ganancia libre por pieza:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text("+ " + formatoUSD.format(precioMayor - costoTotal), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Editar
                    Button(
                        onClick = onEditarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = fondo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar Costos de la Plantilla", color = estados, fontWeight = FontWeight.Bold)
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

        // --- SECCIÓN 1: NOMBRE DE PLANTILLA ---
        item {
            OutlinedTextField(
                value = uiState.nombrePlantilla,
                onValueChange = { viewModel.onNombrePlantillaChange(it) },
                label = { Text("Nombre de la Plantilla") },
                placeholder = { Text("Ej. Taza Mágica, Franela DTF") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

            // --- SECCIÓN 3: MATERIA PRIMA (Pieza, Empaque y Extras) ---
            item {
                SeccionCard(titulo = "1. Materia Prima (Base)") {
                    // Pieza
                    Text("Pieza para sublimar (Franela, Taza, etc):", fontSize = 12.sp, color = Color.Gray)
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
                    Text("Empaque (Bolsa, Caja):", fontSize = 12.sp, color = Color.Gray)
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
                        Text("Papel de Sublimación (Resma):", fontSize = 12.sp, color = Color.Gray)
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

            // --- SECCIÓN 4: SERVICIOS DIRECTOS E INDIRECTOS ---
            item {
                SeccionCard(titulo = "2. Servicios Externos y DTF") {
                    // DTF
                    Text("Costo de Impresión / DTF:", fontSize = 12.sp, color = Color.Gray)
                    CampoMonedaCantidad(
                        precio = uiState.precioTotalDtf,
                        moneda = uiState.monedaDtf,
                        cantidad = uiState.rendimientoDtf, // Cantidad de piezas que salen
                        labelPrecio = "Costo Total",
                        labelCantidad = "Piezas",
                        onPrecioChange = { viewModel.onPrecioTotalDtfChange(it) },
                        onMonedaChange = { viewModel.onMonedaDtfChange(it) },
                        onCantidadChange = { viewModel.onRendimientoDtfChange(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Transporte
                    Text("Transporte/Envío:", fontSize = 12.sp, color = Color.Gray)
                    CampoMonedaCantidad(
                        precio = uiState.costoTransporte,
                        moneda = uiState.monedaTransporte,
                        cantidad = uiState.rendimientoTransporte,
                        labelPrecio = "Costo Total",
                        labelCantidad = "Piezas",
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
                        labelPrecio = "Costo Total",
                        labelCantidad = "Piezas",
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

            // --- SECCIÓN 5: PORCENTAJES (SLIDERS) ---
            item {
                SeccionCard(titulo = "3. Operatividad y Ganancia (%)") {
                    SliderPorcentaje(
                        titulo = "Costos Operativos (Luz/Merma):",
                        valor = uiState.porcentajeOperativo,
                        rango = 0f..50f,
                        colorActivo = Color.Gray,
                        onValorChange = { viewModel.onPorcentajeOperativoChange(it) }
                    )
                    SliderPorcentaje(
                        titulo = "Margen Ganancia (DETAL):",
                        valor = uiState.porcentajeGanancia,
                        rango = 0f..99f,
                        colorActivo = estados,
                        onValorChange = { viewModel.onPorcentajeGananciaChange(it) }
                    )
                    SliderPorcentaje(
                        titulo = "Margen Ganancia (MAYOR):",
                        valor = uiState.porcentajeGananciaMayor,
                        rango = 0f..99f,
                        colorActivo = componentes,
                        onValorChange = { viewModel.onPorcentajeGananciaMayorChange(it) }
                    )
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
                text = "Margen: %.0f%%".format(plantilla.porcentajeGananciaDetal),
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
    labelPrecio: String = "Precio Lote",
    labelCantidad: String = "Piezas",
    onPrecioChange: (String) -> Unit,
    onMonedaChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Campo de Precio
        OutlinedTextField(
            value = precio,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) onPrecioChange(it) },
            label = { Text(labelPrecio) },
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
            label = { Text(labelCantidad) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
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
        colors = CardDefaults.cardColors(containerColor = fondo2) // Un verde agua elegante
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("📊 RESUMEN DE COTIZACIÓN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Costo Real
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Costo Producción (Pieza + DTF + Luz):", color = Color.White, fontSize = 12.sp)
                Text(formatoUSD.format(uiState.costoTotalProduccionUsd), color = Color.White, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

            // VENTA AL DETAL
            Text("VENTA AL DETAL (Margen: ${uiState.porcentajeGanancia.toInt()}%)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Precio Sugerido:", color = Color.White, fontSize = 16.sp)
                Text(formatoUSD.format(uiState.precioSugeridoUsd), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tu ganancia libre:", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                Text("+ " + formatoUSD.format(uiState.gananciaNetaUsd), color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // VENTA AL MAYOR
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.1f)).padding(8.dp)) {
                Column {
                    Text("VENTA AL MAYOR (Margen: ${uiState.porcentajeGananciaMayor.toInt()}%)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Precio Sugerido:", color = Color.White, fontSize = 14.sp)
                        Text(formatoUSD.format(uiState.precioSugeridoMayorUsd), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tu ganancia libre:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("+ " + formatoUSD.format(uiState.gananciaNetaMayorUsd), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
