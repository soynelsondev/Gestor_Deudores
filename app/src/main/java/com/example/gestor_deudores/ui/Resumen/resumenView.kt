package com.example.gestor_deudores.ui.Resumen

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
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.gestor_deudores.data.database.MovimientoCuenta
import com.example.gestor_deudores.data.utils.ExportarCsvUtils
import com.example.gestor_deudores.data.utils.redondear2
import com.example.gestor_deudores.data.database.CuentaBancaria
import com.example.gestor_deudores.ui.home.BarraNavegacionInferior
import com.example.gestor_deudores.ui.rutas
import com.example.gestor_deudores.ui.theme.fondo2
import com.example.gestor_deudores.ui.theme.estados
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenPrincipal(
    viewModel: ResumenViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoAgregarCuenta by remember { mutableStateOf(false) }
    var cuentaParaEditar by remember { mutableStateOf<CuentaBancaria?>(null) }
    var cuentaParaDetalle by remember { mutableStateOf<CuentaBancaria?>(null) }
    var cuentaParaAjustarSaldo by remember { mutableStateOf<CuentaBancaria?>(null) }
    var cuentaParaTransferir by remember { mutableStateOf<CuentaBancaria?>(null) }
    var cuentaParaIngresoGasto by remember { mutableStateOf<Pair<CuentaBancaria, Boolean>?>(null) }
    var mostrarDialogoAjustarCostos by remember { mutableStateOf(false) }
    var mostrarDialogoRegistrarGasto by remember { mutableStateOf(false) }
    var mostrarDialogoRegistrarRetiro by remember { mutableStateOf(false) }
    var montosOcultos by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RESUMEN FINANCIERO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = fondo2,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                rutaActual = rutas.RESUMEN,
                onIrAInicio = { navController.navigate(rutas.HOME) },
                onIrAPedidos = { navController.navigate(rutas.PEDIDOS) },
                onIrACotizar = { navController.navigate(rutas.COTIZADOR) },
                onIrAResumen = { }
            )
        }
    ) { paddingValues ->

        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Filtros de Período y Tasa BCV
                SeccionFiltrosYEncabezado(
                    periodoSeleccionado = uiState.periodoSeleccionado,
                    tasaBcv = uiState.tasaBcv,
                    onPeriodoCambiado = { viewModel.cambiarPeriodo(it) }
                )

                // 2. BARRA DE HERRAMIENTAS Y ACCIONES
                SeccionBarraHerramientas(
                    monedaActual = uiState.monedaVista,
                    montosOcultos = montosOcultos,
                    onCambiarMoneda = { viewModel.cambiarMonedaVista(it) },
                    onToggleMontosOcultos = { montosOcultos = !montosOcultos },
                    onExportarPdf = { com.example.gestor_deudores.data.utils.generarYCompartirPdf(navController.context, uiState) },
                    onExportarCsv = { com.example.gestor_deudores.data.utils.ExportarCsvUtils.exportarResumenCsv(navController.context, uiState, anonimizarClientes = true) }
                )

                // 3. DISPONIBLE PARA RETIRAR (SECCIÓN 9 Y FASE 2)
                TarjetaDisponibleParaRetirar(
                    uiState = uiState,
                    montosOcultos = montosOcultos,
                    onAbrirRetiro = { mostrarDialogoRegistrarRetiro = true },
                    onAbrirGasto = { mostrarDialogoRegistrarGasto = true }
                )

                // 4. Proyección de Cobros a 7 y 30 Días (Sección 11)
                SeccionProyeccionCobros(proyeccion = uiState.proyeccionCobros, montosOcultos = montosOcultos)

                // 5. Antigüedad de Cartera y Puntualidad (Sección 12)
                SeccionAntiguedadCartera(
                    cartera = uiState.antiguedadCartera,
                    montosOcultos = montosOcultos,
                    onEnviarRecordatorios = { navController.navigate(rutas.PEDIDOS) }
                )

                // 6. Alertas Financieras de Salud
                if (uiState.alertasFinancieras.isNotEmpty()) {
                    SeccionAlertasFinancieras(alertas = uiState.alertasFinancieras)
                }

                // 7. Tarjetas KPI Totales
                TarjetasKpiTotales(uiState = uiState, montosOcultos = montosOcultos)

                // 8. GRÁFICA VISUAL: Evaluación de Ganancias vs. Costos de Producción
                GraficaPerdidasGanancias(uiState = uiState)

                // 9. Sobres en Cuadrícula 2x2
                SeccionCarruselSobres(fondos = uiState.fondosInsumos, monedaVista = uiState.monedaVista, tasaBcv = uiState.tasaBcv, montosOcultos = montosOcultos)

                // 7. Radiografía de Pago Individual y Consolidado con Selector de Cliente
                SeccionRadiografiaPagos(
                    uiState = uiState,
                    onSeleccionarAbono = { index -> viewModel.seleccionarAbono(index) },
                    onAbrirAjusteCostos = { mostrarDialogoAjustarCostos = true }
                )

                // 8. Sección Bancos y Billeteras con Rediseño Zubli
                SeccionCuentasBancarias(
                    cuentas = uiState.cuentasBancarias,
                    movimientos = uiState.movimientosCuenta,
                    periodoSeleccionado = uiState.periodoSeleccionado,
                    tasaBcv = uiState.tasaBcv,
                    onAgregarCuentaClick = { mostrarDialogoAgregarCuenta = true },
                    onCuentaClick = { cuenta -> cuentaParaDetalle = cuenta },
                    onIngresoClick = { cuenta -> cuentaParaIngresoGasto = Pair(cuenta, true) },
                    onGastoClick = { cuenta -> cuentaParaIngresoGasto = Pair(cuenta, false) },
                    onTransferirClick = { cuenta -> cuentaParaTransferir = cuenta },
                    onEditarClick = { cuenta -> cuentaParaEditar = cuenta },
                    onArchivarClick = { cuenta -> viewModel.archivarCuentaBancaria(cuenta) },
                    onCopiarDatosPago = { cuenta ->
                        if (!cuenta.datosDePago.isNullOrBlank()) {
                            val msg = "Hola! Aquí tienes los datos para realizar el pago a nuestra cuenta:\n\n📌 *${cuenta.nombre}*\n${cuenta.datosDePago}\n\n¡Muchas gracias!"
                            com.example.gestor_deudores.data.utils.abrirWhatsApp(navController.context, "", msg)
                        }
                    }
                )

                // 9. Resumen de Estado de Pedidos
                SeccionEstadoPedidos(uiState = uiState)
            }
        }

        // Diálogo modal para registrar compras y gastos del taller (Fase 2)
        if (mostrarDialogoRegistrarGasto) {
            DialogoRegistrarGasto(
                categorias = uiState.listaCategorias,
                tasaBcv = uiState.tasaBcv,
                onGuardarGasto = { catId, desc, montoUsd, moneda, montoOrig, tasa, prov ->
                    mostrarDialogoRegistrarGasto = false
                    viewModel.agregarGasto(catId, desc, montoUsd, moneda, montoOrig, tasa, null, prov)
                },
                onDismiss = { mostrarDialogoRegistrarGasto = false }
            )
        }

        // Diálogo modal para registrar retiros de ganancia del dueño (Fase 2)
        if (mostrarDialogoRegistrarRetiro) {
            DialogoRegistrarRetiro(
                tasaBcv = uiState.tasaBcv,
                onGuardarRetiro = { montoUsd, nota ->
                    mostrarDialogoRegistrarRetiro = false
                    viewModel.agregarRetiro(montoUsd, "USD", 0.0, 0.0, null, nota)
                },
                onDismiss = { mostrarDialogoRegistrarRetiro = false }
            )
        }

        // Diálogos de Gestión de Cuentas (Zubli)
        if (mostrarDialogoAgregarCuenta) {
            DialogoCrearEditarCuenta(
                cuentaExistente = null,
                onDismiss = { mostrarDialogoAgregarCuenta = false },
                onGuardar = { nombre, tipo, moneda, saldoInicial, inst, alias, uso, meta, datosPago, color, icono ->
                    viewModel.agregarCuentaBancaria(nombre, tipo, moneda, saldoInicial, inst, alias, uso, meta, datosPago, color, icono)
                    mostrarDialogoAgregarCuenta = false
                }
            )
        }

        cuentaParaEditar?.let { cuenta ->
            DialogoCrearEditarCuenta(
                cuentaExistente = cuenta,
                onDismiss = { cuentaParaEditar = null },
                onGuardar = { nombre, tipo, moneda, _, inst, alias, uso, meta, datosPago, color, icono ->
                    val cuentaAct = cuenta.copy(
                        nombre = nombre,
                        tipo = tipo,
                        moneda = moneda,
                        institucion = inst,
                        alias = alias,
                        uso = uso,
                        metaUsd = meta,
                        datosDePago = datosPago,
                        color = color,
                        icono = icono
                    )
                    viewModel.editarCuentaBancaria(cuentaAct)
                    cuentaParaEditar = null
                }
            )
        }

        cuentaParaDetalle?.let { cuenta ->
            DialogoDetalleCuenta(
                cuenta = cuenta,
                uiState = uiState,
                viewModel = viewModel,
                onIngresoClick = {
                    cuentaParaIngresoGasto = Pair(cuenta, true)
                },
                onGastoClick = {
                    cuentaParaIngresoGasto = Pair(cuenta, false)
                },
                onTransferirClick = {
                    cuentaParaTransferir = cuenta
                },
                onAjustarSaldoClick = {
                    cuentaParaAjustarSaldo = cuenta
                },
                onEditarClick = {
                    cuentaParaEditar = cuenta
                },
                onArchivarClick = {
                    viewModel.archivarCuentaBancaria(cuenta)
                    cuentaParaDetalle = null
                },
                onDismiss = { cuentaParaDetalle = null }
            )
        }

        cuentaParaIngresoGasto?.let { (cuenta, esIngreso) ->
            DialogoRegistrarMovimientoRapido(
                cuenta = cuenta,
                esIngreso = esIngreso,
                onDismiss = { cuentaParaIngresoGasto = null },
                onGuardar = { monto, concepto, ref, foto ->
                    if (esIngreso) {
                        viewModel.registrarIngresoCuenta(cuenta, monto, concepto, ref, foto)
                    } else {
                        viewModel.registrarGastoCuenta(cuenta, monto, concepto, ref, foto)
                    }
                    cuentaParaIngresoGasto = null
                }
            )
        }

        cuentaParaTransferir?.let { cuenta ->
            DialogoTransferirEntreCuentas(
                cuentaOrigenInicial = cuenta,
                cuentasDisponibles = uiState.cuentasBancarias.filter { it.activa },
                tasaBcv = uiState.tasaBcv,
                onDismiss = { cuentaParaTransferir = null },
                onTransferir = { cOrigen, cDest, mOrig, mDest, tasa, concepto, ref ->
                    viewModel.transferirEntreCuentas(cOrigen, cDest, mOrig, mDest, tasa, concepto, ref)
                    cuentaParaTransferir = null
                }
            )
        }

        cuentaParaAjustarSaldo?.let { cuenta ->
            DialogoAjustarSaldoCuenta(
                cuenta = cuenta,
                onDismiss = { cuentaParaAjustarSaldo = null },
                onGuardarAjuste = { nuevoSaldo, motivo ->
                    viewModel.ajustarSaldoCuenta(cuenta, nuevoSaldo, motivo)
                    cuentaParaAjustarSaldo = null
                }
            )
        }

        // Diálogo modal para ajustar costos reales ($3.90 camisa, pasaje, etc.)
        if (mostrarDialogoAjustarCostos) {
            DialogoAjustarCostosBase(
                config = uiState.configuracionCostos,
                onDismiss = { mostrarDialogoAjustarCostos = false },
                onGuardarConfig = { pctTextil, pctPasaje, pctInsumos, pctGanancia ->
                    viewModel.guardarConfiguracionCostos(pctTextil, pctPasaje, pctInsumos, pctGanancia)
                    mostrarDialogoAjustarCostos = false
                }
            )
        }
    }
}

fun formatMonto(montoUsd: Double, monedaVista: String, tasaBcv: Double, montosOcultos: Boolean): String {
    if (montosOcultos) return "••••••"
    val monto = if (monedaVista == "VES" && tasaBcv > 0) montoUsd * tasaBcv else montoUsd
    val simbolo = if (monedaVista == "VES") "Bs" else "$"
    return "$simbolo ${String.format(Locale.US, "%.2f", monto)}"
}

@Composable
fun SeccionBarraHerramientas(
    monedaActual: String,
    montosOcultos: Boolean,
    onCambiarMoneda: (String) -> Unit,
    onToggleMontosOcultos: () -> Unit,
    onExportarPdf: () -> Unit,
    onExportarCsv: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = fondo2.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonConmutadorMoneda(
                monedaActual = monedaActual,
                onCambiarMoneda = onCambiarMoneda
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (montosOcultos) Color(0xFFFFE0B2) else Color.White)
                        .clickable { onToggleMontosOcultos() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(if (montosOcultos) "🔒 Oculto" else "👁️ Montos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (montosOcultos) Color(0xFFE65100) else Color.DarkGray)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { onExportarCsv() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("📊 Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = estados)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { onExportarPdf() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("📄 PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = estados)
                }
            }
        }
    }
}

@Composable
fun TarjetaDisponibleParaRetirar(
    uiState: ResumenUiState,
    montosOcultos: Boolean = false,
    onAbrirRetiro: () -> Unit,
    onAbrirGasto: () -> Unit
) {
    val textoMonto = formatMonto(uiState.disponibleParaRetirarUsd, uiState.monedaVista, uiState.tasaBcv, montosOcultos)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text("💎 DISPONIBLE PARA RETIRAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                Text(textoMonto, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAbrirGasto,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Compras/Gastos", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Button(
                    onClick = onAbrirRetiro,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retirar Ganancia", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            if (uiState.gastosTallerUsd > 0 || uiState.comprasInsumosUsd > 0) {
                Text(
                    text = "Ganancia Libre $${String.format(Locale.getDefault(), "%.2f", uiState.gananciaLibreUsd)} − Gastos Taller $${String.format(Locale.getDefault(), "%.2f", uiState.gastosTallerUsd)} = Ganancia Neta $${String.format(Locale.getDefault(), "%.2f", uiState.gananciaNetaUsd)}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            } else {
                Text(
                    text = "Registra compras de insumos o gastos del taller para calcular tu ganancia neta real y reservas de caja.",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun DialogoRegistrarGasto(
    categorias: List<com.example.gestor_deudores.data.database.CategoriaGasto>,
    tasaBcv: Double,
    onGuardarGasto: (categoriaId: Int, desc: String, montoUsd: Double, moneda: String, montoOrig: Double, tasa: Double, proveedor: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var categoriaSeleccionada by remember { mutableStateOf(categorias.firstOrNull()?.id ?: 1) }
    var descripcion by remember { mutableStateOf("") }
    var montoTexto by remember { mutableStateOf("") }
    var esBs by remember { mutableStateOf(false) }
    var proveedor by remember { mutableStateOf("") }

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
                Text("Registrar Compra / Gasto del Taller", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Text("Categoría:", fontSize = 11.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categorias) { cat ->
                        val sel = categoriaSeleccionada == cat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (sel) estados else com.example.gestor_deudores.ui.theme.fondo_claro)
                                .clickable { categoriaSeleccionada = cat.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(cat.nombre, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else Color.DarkGray)
                        }
                    }
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej. 10 Tazas Blancas, Recibo Luz, Impresión DTF") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (!esBs) estados else com.example.gestor_deudores.ui.theme.fondo_claro).clickable { esBs = false }.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("USD ($)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!esBs) Color.White else Color.DarkGray)
                    }
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (esBs) estados else com.example.gestor_deudores.ui.theme.fondo_claro).clickable { esBs = true }.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bolívares (Bs)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (esBs) Color.White else Color.DarkGray)
                    }
                }

                val montoVal = montoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                val montoUsdCalc = if (esBs && tasaBcv > 0) montoVal / tasaBcv else montoVal

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text(if (esBs) "Monto en Bs" else "Monto en USD") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (esBs && tasaBcv > 0) {
                    Text("Equivalente: $${String.format(Locale.US, "%.2f", montoUsdCalc)} USD (Tasa BCV: $tasaBcv)", fontSize = 11.sp, color = Color(0xFF4CAF50))
                }

                OutlinedTextField(
                    value = proveedor,
                    onValueChange = { proveedor = it },
                    label = { Text("Proveedor / Comercio (Opcional)") },
                    placeholder = { Text("Ej. Dist. Sublimación C.A.") },
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
                            if (montoUsdCalc > 0 && descripcion.isNotBlank()) {
                                onGuardarGasto(
                                    categoriaSeleccionada,
                                    descripcion,
                                    montoUsdCalc,
                                    if (esBs) "VES" else "USD",
                                    montoVal,
                                    if (esBs) tasaBcv else 0.0,
                                    proveedor.ifBlank { null }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = estados)
                    ) {
                        Text("Guardar Compra/Gasto", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoRegistrarRetiro(
    tasaBcv: Double,
    onGuardarRetiro: (montoUsd: Double, nota: String) -> Unit,
    onDismiss: () -> Unit
) {
    var montoTexto by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }

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
                Text("💎 Registrar Retiro de Ganancia (Sueldo Dueño)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Registra el dinero que retiras para ti como ganancia personal del taller.", fontSize = 12.sp, color = Color.LightGray)

                val montoVal = montoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text("Monto a Retirar ($ USD)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota / Motivo (Opcional)") },
                    placeholder = { Text("Ej. Sueldo personal quincena") },
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
                            if (montoVal > 0) {
                                onGuardarRetiro(montoVal, nota)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Confirmar Retiro", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTE 1: CONMUTADOR DE MONEDA
// ==========================================
@Composable
fun BotonConmutadorMoneda(
    monedaActual: String,
    onCambiarMoneda: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (monedaActual == "USD") Color.White else Color.Transparent)
                .clickable { onCambiarMoneda("USD") }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "USD ($)",
                color = if (monedaActual == "USD") estados else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (monedaActual == "VES") Color.White else Color.Transparent)
                .clickable { onCambiarMoneda("VES") }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Bs",
                color = if (monedaActual == "VES") estados else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

// ==========================================
// COMPONENTE 2: SECCIÓN DE FILTROS DE PERÍODO
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionFiltrosYEncabezado(
    periodoSeleccionado: PeriodoFiltro,
    tasaBcv: Double,
    onPeriodoCambiado: (PeriodoFiltro) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Período Evaluado",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            if (tasaBcv > 0) {
                Text(
                    text = "Tasa BCV: Bs ${String.format(Locale.getDefault(), "%.2f", tasaBcv)}",
                    fontSize = 12.sp,
                    color = estados,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = periodoSeleccionado == PeriodoFiltro.HOY,
                    onClick = { onPeriodoCambiado(PeriodoFiltro.HOY) },
                    label = { Text("Hoy") }
                )
            }
            item {
                FilterChip(
                    selected = periodoSeleccionado == PeriodoFiltro.ESTA_SEMANA,
                    onClick = { onPeriodoCambiado(PeriodoFiltro.ESTA_SEMANA) },
                    label = { Text("Esta Semana") }
                )
            }
            item {
                FilterChip(
                    selected = periodoSeleccionado == PeriodoFiltro.ESTE_MES,
                    onClick = { onPeriodoCambiado(PeriodoFiltro.ESTE_MES) },
                    label = { Text("Este Mes") }
                )
            }
            item {
                FilterChip(
                    selected = periodoSeleccionado == PeriodoFiltro.MES_ANTERIOR,
                    onClick = { onPeriodoCambiado(PeriodoFiltro.MES_ANTERIOR) },
                    label = { Text("Mes Anterior") }
                )
            }
            item {
                FilterChip(
                    selected = periodoSeleccionado == PeriodoFiltro.ULTIMOS_3_MESES,
                    onClick = { onPeriodoCambiado(PeriodoFiltro.ULTIMOS_3_MESES) },
                    label = { Text("3 Meses") }
                )
            }
            item {
                FilterChip(
                    selected = periodoSeleccionado == PeriodoFiltro.TODO_EL_HISTORICO,
                    onClick = { onPeriodoCambiado(PeriodoFiltro.TODO_EL_HISTORICO) },
                    label = { Text("Histórico") }
                )
            }
        }
    }
}

// ==========================================
// COMPONENTE 3: GRÁFICA DE EVALUACIÓN (TORTA / DONUT)
// ==========================================
@Composable
fun GraficaPerdidasGanancias(uiState: ResumenUiState) {
    val totalFacturado = uiState.ingresosCobradosUsd.coerceAtLeast(0.01).toFloat()
    
    // Si no hay ingresos, no dibujamos la torta o la pintamos gris
    if (totalFacturado <= 0.01f) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay abonos en este período para graficar.", color = Color.Gray, fontSize = 13.sp)
            }
        }
        return
    }

    // Buscamos los fondos específicos que calcula el ViewModel
    val fondoGanancia = uiState.fondosInsumos.find { it.id == "ganancia" }?.montoUsd?.toFloat() ?: 0f
    val fondoTextil = uiState.fondosInsumos.find { it.id == "textil" }?.montoUsd?.toFloat() ?: 0f
    val fondoTintas = uiState.fondosInsumos.find { it.id == "tintas" }?.montoUsd?.toFloat() ?: 0f
    val fondoPasaje = uiState.fondosInsumos.find { it.id == "pasaje" }?.montoUsd?.toFloat() ?: 0f

    val pctGanancia = (fondoGanancia / totalFacturado) * 360f
    val pctTextil = (fondoTextil / totalFacturado) * 360f
    val pctTintas = (fondoTintas / totalFacturado) * 360f
    val pctPasaje = (fondoPasaje / totalFacturado) * 360f

    val colorGanancia = Color(0xFF2E7D32)
    val colorTextil = Color(0xFF1565C0)
    val colorTintas = Color(0xFF8E24AA)
    val colorPasaje = Color(0xFFF57C00)

    val monedaSimbolo = if (uiState.monedaVista == "VES") "Bs" else "$"
    val montoCentroMostrado = if (uiState.monedaVista == "VES" && uiState.tasaBcv > 0) fondoGanancia * uiState.tasaBcv else fondoGanancia.toDouble()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Flujo de Dinero y Rentabilidad",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // GRÁFICO DE DONA (CANVAS)
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 35f
                    var startAngle = -90f

                    // Dibuja Ganancia
                    drawArc(
                        color = colorGanancia,
                        startAngle = startAngle,
                        sweepAngle = pctGanancia,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                    )
                    startAngle += pctGanancia

                    // Dibuja Textil
                    drawArc(
                        color = colorTextil,
                        startAngle = startAngle,
                        sweepAngle = pctTextil,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                    )
                    startAngle += pctTextil

                    // Dibuja Tintas
                    drawArc(
                        color = colorTintas,
                        startAngle = startAngle,
                        sweepAngle = pctTintas,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                    )
                    startAngle += pctTintas

                    // Dibuja Pasaje
                    drawArc(
                        color = colorPasaje,
                        startAngle = startAngle,
                        sweepAngle = pctPasaje,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                    )
                }

                // Textos en el centro del Donut
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Libre", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "$monedaSimbolo ${String.format(Locale.getDefault(), "%.2f", montoCentroMostrado)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorGanancia
                    )
                    Text(text = "${String.format(Locale.getDefault(), "%.1f", (fondoGanancia / totalFacturado) * 100)}%", fontSize = 11.sp, color = colorGanancia, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LEYENDAS INFERIORES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ItemLeyendaTorta(colorGanancia, "Ganancia Neta", (fondoGanancia / totalFacturado) * 100)
                    ItemLeyendaTorta(colorTextil, "Mat. Base", (fondoTextil / totalFacturado) * 100)
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ItemLeyendaTorta(colorTintas, "Tintas & Extras", (fondoTintas / totalFacturado) * 100)
                    ItemLeyendaTorta(colorPasaje, "Transporte", (fondoPasaje / totalFacturado) * 100)
                }
            }
        }
    }
}

@Composable
fun ItemLeyendaTorta(color: Color, texto: String, porcentaje: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$texto (${String.format(Locale.getDefault(), "%.1f", porcentaje)}%)",
            fontSize = 11.sp,
            color = if (texto.contains("Ganancia")) color else Color.DarkGray,
            fontWeight = if (texto.contains("Ganancia")) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ==========================================
// COMPONENTE 4: ALERTAS DE SALUD FINANCIERA
// ==========================================
@Composable
fun SeccionAlertasFinancieras(alertas: List<AlertaFinanciera>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        alertas.forEach { alerta ->
            val colorFondo = when (alerta.nivel) {
                "DANGER" -> Color(0xFFFFEBEE)
                "WARNING" -> Color(0xFFFFF8E1)
                else -> Color(0xFFE8F5E9)
            }
            val colorTexto = when (alerta.nivel) {
                "DANGER" -> Color(0xFFC62828)
                "WARNING" -> Color(0xFFF57F17)
                else -> Color(0xFF2E7D32)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = colorFondo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (alerta.nivel == "DANGER") Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = colorTexto,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = alerta.titulo,
                            fontWeight = FontWeight.Bold,
                            color = colorTexto,
                            fontSize = 14.sp
                        )
                        Text(
                            text = alerta.mensaje,
                            color = colorTexto.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTE 5: TARJETAS KPI TOTALES (GLOSARIO Y ETIQUETAS DE PERÍODO)
// ==========================================
@Composable
fun TarjetasKpiTotales(uiState: ResumenUiState, montosOcultos: Boolean = false) {
    val etiquetaPeriodo = when (uiState.periodoSeleccionado) {
        PeriodoFiltro.HOY -> "Hoy"
        PeriodoFiltro.ESTA_SEMANA -> "Esta semana"
        PeriodoFiltro.ESTE_MES -> "Este mes"
        PeriodoFiltro.MES_ANTERIOR -> "Mes anterior"
        PeriodoFiltro.ULTIMOS_3_MESES -> "Últimos 3 meses"
        PeriodoFiltro.TODO_EL_HISTORICO -> "Histórico"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaKpiItem(
                titulo = "Cobrado",
                subtitulo = "Abonos ($etiquetaPeriodo)",
                montoUsd = uiState.ingresosCobradosUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFF2E7D32),
                montosOcultos = montosOcultos,
                modifier = Modifier.weight(1f)
            )
            TarjetaKpiItem(
                titulo = "Reposición",
                subtitulo = "Insumos ($etiquetaPeriodo)",
                montoUsd = uiState.fondoReposicionUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFF1565C0),
                montosOcultos = montosOcultos,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaKpiItem(
                titulo = "Ganancia Libre",
                subtitulo = "Antes de gastos ($etiquetaPeriodo)",
                montoUsd = uiState.gananciaNetaUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFFC2185B),
                montosOcultos = montosOcultos,
                modifier = Modifier.weight(1f)
            )
            TarjetaKpiItem(
                titulo = "Por Cobrar",
                subtitulo = "Saldo en la calle (Hoy)",
                montoUsd = uiState.porCobrarUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFFE65100),
                montosOcultos = montosOcultos,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TarjetaKpiItem(
    titulo: String,
    subtitulo: String,
    montoUsd: Double,
    monedaVista: String,
    tasaBcv: Double,
    color: Color,
    montosOcultos: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textoMonto = formatMonto(montoUsd, monedaVista, tasaBcv, montosOcultos)

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitulo,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = textoMonto,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ==========================================
// COMPONENTE 6: SOBRES EN CUADRÍCULA 2x2 (SECCIÓN 19)
// ==========================================
@Composable
fun SeccionCarruselSobres(
    fondos: List<FondoInsumoItem>,
    monedaVista: String,
    tasaBcv: Double,
    montosOcultos: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "División de Fondos por Insumos (Sobres)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val lista0y1 = fondos.take(2)
            val lista2y3 = fondos.drop(2).take(2)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lista0y1.forEach { fondo ->
                    TarjetaSobreCuadriculaItem(
                        fondo = fondo,
                        monedaVista = monedaVista,
                        tasaBcv = tasaBcv,
                        montosOcultos = montosOcultos,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (lista2y3.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    lista2y3.forEach { fondo ->
                        TarjetaSobreCuadriculaItem(
                            fondo = fondo,
                            monedaVista = monedaVista,
                            tasaBcv = tasaBcv,
                            montosOcultos = montosOcultos,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaSobreCuadriculaItem(
    fondo: FondoInsumoItem,
    monedaVista: String,
    tasaBcv: Double,
    montosOcultos: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textoMonto = formatMonto(fondo.montoUsd, monedaVista, tasaBcv, montosOcultos)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = fondo.icono, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fondo.nombre,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(
                text = textoMonto,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                progress = { (fondo.porcentajeDelTotal / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", fondo.porcentajeDelTotal)}% del total",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// COMPONENTE 7: RADIOGRAFÍA DE PAGOS CON SELECTOR MULTIPLE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionRadiografiaPagos(
    uiState: ResumenUiState,
    onSeleccionarAbono: (Int) -> Unit,
    onAbrirAjusteCostos: () -> Unit
) {
    val abonos = uiState.listaAbonosDesglose

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔍 Radiografía de Abonos Recibidos",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = onAbrirAjusteCostos,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Ajustar Costos Base",
                    tint = estados,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (abonos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Sin abonos registrados en este período",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Al recibir abonos de tus clientes, aquí verás la separación de fondos automatizada basada en tus cotizaciones y pedidos. Toca ⚙️ arriba para ajustar los costos base.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Selector Chips para elegir cuál abono inspeccionar (Consolidado o cliente)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(abonos) { index, abono ->
                    FilterChip(
                        selected = index == uiState.abonoSeleccionadoIndex,
                        onClick = { onSeleccionarAbono(index) },
                        label = { Text("${abono.clienteNombre} ($${String.format(Locale.getDefault(), "%.2f", abono.montoTotalAbonoUsd)})") }
                    )
                }
            }

            // Tarjeta con el desglose del abono o consolidado seleccionado
            uiState.abonoSeleccionado?.let { desglose ->
                TarjetaRadiografiaPagoItem(
                    desglose = desglose,
                    monedaVista = uiState.monedaVista,
                    tasaBcv = uiState.tasaBcv
                )
            }
        }
    }
}

@Composable
fun TarjetaRadiografiaPagoItem(
    desglose: DesglosePagoIndividual,
    monedaVista: String,
    tasaBcv: Double
) {
    val montoMostrar = if (monedaVista == "VES" && tasaBcv > 0) desglose.montoTotalAbonoUsd * tasaBcv else desglose.montoTotalAbonoUsd
    val simbolo = if (monedaVista == "VES") "Bs" else "$"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = desglose.clienteNombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = desglose.fechaStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }

            Text(
                text = "Monto Recibido: $simbolo ${String.format(Locale.getDefault(), "%.2f", montoMostrar)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                desglose.desgloseItems.forEach { item ->
                    ItemDesgloseAcordeon(item = item, monedaVista = monedaVista, tasaBcv = tasaBcv, simbolo = simbolo)
                }
            }
        }
    }
}

@Composable
fun ItemDesgloseAcordeon(
    item: DesglosePagoItem,
    monedaVista: String,
    tasaBcv: Double,
    simbolo: String
) {
    var expandido by remember { mutableStateOf(false) }
    val tieneSubitems = item.subItems.isNotEmpty()
    val itemMonto = if (monedaVista == "VES" && tasaBcv > 0) item.montoUsd * tasaBcv else item.montoUsd

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = tieneSubitems) { expandido = !expandido }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "• ${item.concepto}", 
                    fontSize = 12.sp, 
                    fontWeight = if(tieneSubitems) FontWeight.Bold else FontWeight.Normal
                )
                if (tieneSubitems) {
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = "$simbolo ${String.format(Locale.getDefault(), "%.2f", itemMonto)} (${String.format(Locale.getDefault(), "%.1f", item.porcentaje)}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if(tieneSubitems) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        }

        androidx.compose.animation.AnimatedVisibility(visible = expandido && tieneSubitems) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item.subItems.forEach { subItem ->
                    val subMonto = if (monedaVista == "VES" && tasaBcv > 0) subItem.montoUsd * tasaBcv else subItem.montoUsd
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "└ ${subItem.concepto}", fontSize = 11.sp, color = Color.DarkGray)
                        Text(
                            text = "$simbolo ${String.format(Locale.getDefault(), "%.2f", subMonto)} (${String.format(Locale.getDefault(), "%.1f", subItem.porcentaje)}%)",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ==========================================
// COMPONENTE 8: SECCIÓN BANCOS Y BILLETERAS (ZUBLI)
// ==========================================

fun obtenerColorYIconoCuenta(tipo: String): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (tipo.uppercase()) {
        "BANCO" -> Triple(Color(0xFF1E5BC6), Color(0xFFE3EDFB), Icons.Default.Info)
        "PAGO_MOVIL" -> Triple(Color(0xFF00707A), Color(0xFFDDF0EE), Icons.Default.Phone)
        "EFECTIVO" -> Triple(Color(0xFF2E7D32), Color(0xFFE4F2E5), Icons.Default.ShoppingCart)
        "BILLETERA" -> Triple(Color(0xFF6B4FA3), Color(0xFFECE6F5), Icons.Default.Person)
        "BANCO_USD" -> Triple(Color(0xFF0B5C73), Color(0xFFDDEEF3), Icons.Default.Info)
        else -> Triple(Color(0xFF5F6B6D), Color(0xFFECEFEF), Icons.Default.Info)
    }
}

fun obtenerColorChipUso(uso: String): Pair<Color, Color> {
    return when (uso.lowercase()) {
        "cobros" -> Pair(Color(0xFF00707A), Color(0xFFDDF0EE))
        "reposición", "reposicion" -> Pair(Color(0xFF1E5BC6), Color(0xFFE3EDFB))
        "ahorro" -> Pair(Color(0xFF2E7D32), Color(0xFFE4F2E5))
        "gastos del taller", "gastos taller" -> Pair(Color(0xFFB23C00), Color(0xFFFDEBDD))
        "personal" -> Pair(Color(0xFF6B4FA3), Color(0xFFECE6F5))
        else -> Pair(Color(0xFF5F6B6D), Color(0xFFECEFEF))
    }
}

@Composable
fun SeccionCuentasBancarias(
    cuentas: List<CuentaBancaria>,
    movimientos: List<MovimientoCuenta>,
    periodoSeleccionado: PeriodoFiltro,
    tasaBcv: Double,
    onAgregarCuentaClick: () -> Unit,
    onCuentaClick: (CuentaBancaria) -> Unit,
    onIngresoClick: (CuentaBancaria) -> Unit,
    onGastoClick: (CuentaBancaria) -> Unit,
    onTransferirClick: (CuentaBancaria) -> Unit,
    onEditarClick: (CuentaBancaria) -> Unit,
    onArchivarClick: (CuentaBancaria) -> Unit,
    onCopiarDatosPago: (CuentaBancaria) -> Unit
) {
    val cuentasActivas = cuentas.filter { it.activa }
    var expandirResumenHeader by remember { mutableStateOf(false) }

    val rangoMillis = remember(periodoSeleccionado) {
        val cal = java.util.Calendar.getInstance()
        val finM = System.currentTimeMillis()
        val iniM = when (periodoSeleccionado) {
            PeriodoFiltro.HOY -> {
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            PeriodoFiltro.ESTA_SEMANA -> {
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.timeInMillis
            }
            PeriodoFiltro.ESTE_MES -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.timeInMillis
            }
            PeriodoFiltro.ULTIMOS_3_MESES -> {
                cal.add(java.util.Calendar.MONTH, -3)
                cal.timeInMillis
            }
            else -> 0L
        }
        Pair(iniM, finM)
    }
    val inicioPeriodo = rangoMillis.first
    val finPeriodo = rangoMillis.second

    val totalCuentasUsd = remember(cuentasActivas, movimientos, tasaBcv) {
        cuentasActivas.sumOf { cuenta ->
            val saldo = cuenta.saldoInicial + movimientos.filter { it.cuentaId == cuenta.id && it.tipo != "SALDO_INICIAL" }.sumOf { it.monto }
            if (cuenta.moneda == "VES" && tasaBcv > 0) saldo / tasaBcv else saldo
        }.redondear2()
    }

    val totalUsdSolo = remember(cuentasActivas, movimientos) {
        cuentasActivas.filter { it.moneda == "USD" }.sumOf { cuenta ->
            cuenta.saldoInicial + movimientos.filter { it.cuentaId == cuenta.id && it.tipo != "SALDO_INICIAL" }.sumOf { it.monto }
        }.redondear2()
    }

    val totalVesSolo = remember(cuentasActivas, movimientos) {
        cuentasActivas.filter { it.moneda == "VES" }.sumOf { cuenta ->
            cuenta.saldoInicial + movimientos.filter { it.cuentaId == cuenta.id && it.tipo != "SALDO_INICIAL" }.sumOf { it.monto }
        }.redondear2()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // CABECERA RESUMEN DE CUENTAS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandirResumenHeader = !expandirResumenHeader },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🏦 Bancos y Billeteras", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text("Total en cuentas: $${String.format(Locale.US, "%.2f", totalCuentasUsd)} USD", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F), fontSize = 13.sp)
                        if (tasaBcv > 0) {
                            Text("Tasa BCV: Bs. ${String.format(Locale.US, "%.2f", tasaBcv)}", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }

                    Button(
                        onClick = onAgregarCuentaClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4FA3)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Nueva cuenta", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (expandirResumenHeader) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cuentas en USD:", fontSize = 11.sp, color = Color.LightGray)
                        Text("$${String.format(Locale.US, "%.2f", totalUsdSolo)} USD", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cuentas en Bolívares:", fontSize = 11.sp, color = Color.LightGray)
                        val equivBsAUsd = if (tasaBcv > 0) totalVesSolo / tasaBcv else 0.0
                        Text("Bs ${String.format(Locale.US, "%.2f", totalVesSolo)} (≈ $${String.format(Locale.US, "%.2f", equivBsAUsd)})", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (cuentasActivas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Agrega tu primera cuenta para saber dónde está tu dinero",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cuentasActivas) { cuenta ->
                    val movsCuenta = movimientos.filter { it.cuentaId == cuenta.id }
                    
                    val saldoCalculado = (cuenta.saldoInicial + movsCuenta.filter { it.tipo != "SALDO_INICIAL" }.sumOf { it.monto }).redondear2()

                    val movsPeriodo = movsCuenta.filter { it.fechaMillis in inicioPeriodo..finPeriodo }
                    val entroPeriodo = movsPeriodo.filter { it.monto > 0 }.sumOf { it.monto }.redondear2()
                    val salioPeriodo = movsPeriodo.filter { it.monto < 0 }.sumOf { Math.abs(it.monto) }.redondear2()

                    val ultimoMov = movsCuenta.maxByOrNull { it.fechaMillis }

                    TarjetaCuentaZubli(
                        cuenta = cuenta,
                        saldoCalculado = saldoCalculado,
                        entroPeriodo = entroPeriodo,
                        salioPeriodo = salioPeriodo,
                        ultimoMovimiento = ultimoMov,
                        tasaBcv = tasaBcv,
                        onClickTarjeta = { onCuentaClick(cuenta) },
                        onIngresoClick = { onIngresoClick(cuenta) },
                        onGastoClick = { onGastoClick(cuenta) },
                        onTransferirClick = { onTransferirClick(cuenta) },
                        onEditarClick = { onEditarClick(cuenta) },
                        onArchivarClick = { onArchivarClick(cuenta) },
                        onCopiarDatosPago = { onCopiarDatosPago(cuenta) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaCuentaZubli(
    cuenta: CuentaBancaria,
    saldoCalculado: Double,
    entroPeriodo: Double,
    salioPeriodo: Double,
    ultimoMovimiento: MovimientoCuenta?,
    tasaBcv: Double,
    onClickTarjeta: () -> Unit,
    onIngresoClick: () -> Unit,
    onGastoClick: () -> Unit,
    onTransferirClick: () -> Unit,
    onEditarClick: () -> Unit,
    onArchivarClick: () -> Unit,
    onCopiarDatosPago: () -> Unit
) {
    val (colorTexto, colorBg, icono) = obtenerColorYIconoCuenta(cuenta.tipo)
    val simboloMoneda = if (cuenta.moneda == "VES") "Bs" else "$"
    var menuExpandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClickTarjeta() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icono, contentDescription = cuenta.tipo, tint = colorTexto, modifier = Modifier.size(20.dp))
                    }

                    Column {
                        Text(
                            text = cuenta.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color(0xFF1C1B1F)
                        )
                        Text(
                            text = "${cuenta.tipo.replace("_", " ")} · ${cuenta.moneda}",
                            fontSize = 11.sp,
                            color = Color(0xFF5F6B6D)
                        )
                    }
                }

                if (!cuenta.uso.isNullOrBlank()) {
                    val (usoTxtColor, usoBgColor) = obtenerColorChipUso(cuenta.uso)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(usoBgColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(cuenta.uso, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = usoTxtColor)
                    }
                }
            }

            Column {
                Text(
                    text = "$simboloMoneda ${String.format(Locale.US, "%.2f", saldoCalculado)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF1C1B1F)
                )

                if (cuenta.moneda == "VES" && tasaBcv > 0) {
                    val equivUsd = (saldoCalculado / tasaBcv).redondear2()
                    Text(
                        text = "≈ $${String.format(Locale.US, "%.2f", equivUsd)} (tasa ${String.format(Locale.US, "%.2f", tasaBcv)})",
                        fontSize = 11.sp,
                        color = Color(0xFF5F6B6D)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE4F2E5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "↓ Entró +$simboloMoneda${String.format(Locale.US, "%.2f", entroPeriodo)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFBE4E4))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "↑ Salió −$simboloMoneda${String.format(Locale.US, "%.2f", salioPeriodo)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
            }

            if (cuenta.metaUsd != null && cuenta.metaUsd > 0) {
                val metaVal = cuenta.metaUsd
                val saldoUsd = if (cuenta.moneda == "VES" && tasaBcv > 0) saldoCalculado / tasaBcv else saldoCalculado
                val pct = ((saldoUsd / metaVal) * 100.0).coerceIn(0.0, 100.0).toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                        color = com.example.gestor_deudores.ui.theme.estados,
                        trackColor = com.example.gestor_deudores.ui.theme.fondo_claro
                    )
                    Text("${pct.toInt()}% de tu meta de $${String.format(Locale.US, "%.2f", metaVal)}", fontSize = 10.sp, color = Color.Gray)
                }
            }

            val textoUltimoMov = if (ultimoMovimiento != null) {
                val formatoF = SimpleDateFormat("dd/MM", Locale.getDefault())
                val fechaM = formatoF.format(Date(ultimoMovimiento.fechaMillis))
                val sgn = if (ultimoMovimiento.monto >= 0) "+$simboloMoneda" else "−$simboloMoneda"
                "$fechaM · ${ultimoMovimiento.concepto.ifBlank { ultimoMovimiento.tipo }} · $sgn${String.format(Locale.US, "%.2f", Math.abs(ultimoMovimiento.monto))}"
            } else {
                "Sin movimientos"
            }
            Text("Último: $textoUltimoMov", fontSize = 10.sp, color = Color(0xFF5F6B6D), maxLines = 1, overflow = TextOverflow.Ellipsis)

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onIngresoClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text("+ Ingreso", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onGastoClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text("− Gasto", fontSize = 10.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onTransferirClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text("⇄ Mover", fontSize = 10.sp, color = Color(0xFF00707A), fontWeight = FontWeight.Bold)
                }

                Box {
                    IconButton(onClick = { menuExpandido = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones", modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                        DropdownMenuItem(
                            text = { Text("✏️ Editar datos") },
                            onClick = {
                                menuExpandido = false
                                onEditarClick()
                            }
                        )
                        if (!cuenta.datosDePago.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("📋 Copiar datos de pago") },
                                onClick = {
                                    menuExpandido = false
                                    onCopiarDatosPago()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("📦 Archivar cuenta", color = Color.Red) },
                            onClick = {
                                menuExpandido = false
                                onArchivarClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoDetalleCuenta(
    cuenta: CuentaBancaria,
    uiState: ResumenUiState,
    viewModel: ResumenViewModel,
    onIngresoClick: () -> Unit,
    onGastoClick: () -> Unit,
    onTransferirClick: () -> Unit,
    onAjustarSaldoClick: () -> Unit,
    onEditarClick: () -> Unit,
    onArchivarClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val movsCuenta = uiState.movimientosCuenta.filter { it.cuentaId == cuenta.id }
    val (colorTexto, colorBg, icono) = obtenerColorYIconoCuenta(cuenta.tipo)
    val simboloMoneda = if (cuenta.moneda == "VES") "Bs" else "$"

    val rangoMillis = remember(uiState.periodoSeleccionado) {
        val cal = java.util.Calendar.getInstance()
        val finM = System.currentTimeMillis()
        val iniM = when (uiState.periodoSeleccionado) {
            PeriodoFiltro.HOY -> {
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            PeriodoFiltro.ESTA_SEMANA -> {
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.timeInMillis
            }
            PeriodoFiltro.ESTE_MES -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.timeInMillis
            }
            PeriodoFiltro.ULTIMOS_3_MESES -> {
                cal.add(java.util.Calendar.MONTH, -3)
                cal.timeInMillis
            }
            else -> 0L
        }
        Pair(iniM, finM)
    }
    val inicioPeriodo = rangoMillis.first
    val finPeriodo = rangoMillis.second

    val movsAnteriores = movsCuenta.filter { it.fechaMillis < inicioPeriodo }
    val saldoInicialPeriodo = (cuenta.saldoInicial + movsAnteriores.sumOf { it.monto }).redondear2()

    val movsPeriodo = movsCuenta.filter { it.fechaMillis in inicioPeriodo..finPeriodo }
    val entradasPeriodo = movsPeriodo.filter { it.monto > 0 }.sumOf { it.monto }.redondear2()
    val salidasPeriodo = movsPeriodo.filter { it.monto < 0 }.sumOf { Math.abs(it.monto) }.redondear2()
    val saldoFinalPeriodo = (saldoInicialPeriodo + entradasPeriodo - salidasPeriodo).redondear2()

    var acumulado = cuenta.saldoInicial
    val movsConSaldoCorridoAsc = movsCuenta.sortedWith(compareBy<MovimientoCuenta> { it.fechaMillis }.thenBy { it.id }).map { mov ->
        acumulado = (acumulado + mov.monto).redondear2()
        mov to acumulado
    }
    val movsConSaldoCorridoDesc = movsConSaldoCorridoAsc.reversed()

    var filtroTipo by remember { mutableStateOf("TODOS") }
    var busquedaTexto by remember { mutableStateOf("") }

    val movsFiltrados = movsConSaldoCorridoDesc.filter { (mov, _) ->
        val coincideTipo = when (filtroTipo) {
            "ENTRADAS" -> mov.monto > 0
            "SALIDAS" -> mov.monto < 0
            "TRANSFERENCIAS" -> mov.tipo.contains("TRANSFERENCIA")
            "AJUSTES" -> mov.tipo == "AJUSTE"
            else -> true
        }
        val coincideTexto = busquedaTexto.isBlank() ||
                mov.concepto.contains(busquedaTexto, ignoreCase = true) ||
                mov.nota.contains(busquedaTexto, ignoreCase = true) ||
                (mov.referenciaPago ?: "").contains(busquedaTexto, ignoreCase = true)
        coincideTipo && coincideTexto
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colorBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(cuenta.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("${cuenta.tipo.replace("_", " ")} · ${cuenta.moneda}", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!cuenta.datosDePago.isNullOrBlank()) {
                        item {
                            Button(
                                onClick = {
                                    val msg = "Hola! Aquí tienes los datos para realizar el pago a nuestra cuenta:\n\n📌 *${cuenta.nombre}*\n${cuenta.datosDePago}\n\n¡Muchas gracias!"
                                    clipboardManager.setText(AnnotatedString(cuenta.datosDePago))
                                    com.example.gestor_deudores.data.utils.abrirWhatsApp(context, "", msg)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00707A)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("📋 Copiar datos de pago para WhatsApp", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("📊 CONCILIACIÓN Y SALDOS DEL PERÍODO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = com.example.gestor_deudores.ui.theme.estados)
                                Spacer(modifier = Modifier.height(2.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Saldo al inicio del período:", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("$simboloMoneda ${String.format(Locale.US, "%.2f", saldoInicialPeriodo)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("(+) Entradas del período:", fontSize = 12.sp, color = Color(0xFF2E7D32))
                                    Text("+$simboloMoneda ${String.format(Locale.US, "%.2f", entradasPeriodo)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("(-) Salidas del período:", fontSize = 12.sp, color = Color(0xFFC62828))
                                    Text("−$simboloMoneda ${String.format(Locale.US, "%.2f", salidasPeriodo)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("(=) Saldo al final del período:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
                                    Text("$simboloMoneda ${String.format(Locale.US, "%.2f", saldoFinalPeriodo)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00707A))
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📜 HISTORIAL DE MOVIMIENTOS (${movsFiltrados.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)

                            OutlinedTextField(
                                value = busquedaTexto,
                                onValueChange = { busquedaTexto = it },
                                placeholder = { Text("Buscar concepto, cliente o referencia...", fontSize = 11.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val opcionesFiltro = listOf(
                                    "TODOS" to "Todos",
                                    "ENTRADAS" to "↓ Entradas",
                                    "SALIDAS" to "↑ Salidas",
                                    "TRANSFERENCIAS" to "⇄ Transferencias",
                                    "AJUSTES" to "⚙️ Ajustes"
                                )
                                items(opcionesFiltro) { (clave, label) ->
                                    FilterChip(
                                        selected = filtroTipo == clave,
                                        onClick = { filtroTipo = clave },
                                        label = { Text(label, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }

                    if (movsFiltrados.isEmpty()) {
                        item {
                            Text("Aún no hay movimientos registrados para este filtro.", fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))
                        }
                    } else {
                        items(movsFiltrados) { (mov, saldoCorrido) ->
                            val formatoF = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val fechaM = formatoF.format(Date(mov.fechaMillis))
                            val esPositivo = mov.monto >= 0
                            val colorMonto = if (esPositivo) Color(0xFF2E7D32) else Color(0xFFC62828)
                            val sgn = if (esPositivo) "+$simboloMoneda" else "−$simboloMoneda"

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text = mov.concepto.ifBlank { mov.nota.ifBlank { mov.tipo } },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1C1B1F),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "$sgn${String.format(Locale.US, "%.2f", Math.abs(mov.monto))}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = colorMonto
                                        )
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(fechaM, fontSize = 10.sp, color = Color.Gray)
                                        Text("Saldo después: $simboloMoneda${String.format(Locale.US, "%.2f", saldoCorrido)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00707A))
                                    }

                                    if (!mov.referenciaPago.isNullOrBlank()) {
                                        Text("Ref: ${mov.referenciaPago}", fontSize = 10.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onIngresoClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Ingreso", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onGastoClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("− Gasto", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onTransferirClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00707A)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⇄ Mover", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = onAjustarSaldoClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⚙️ Ajustar Saldo", fontSize = 10.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                ExportarCsvUtils.exportarExtractoCuentaCsv(
                                    context = context,
                                    cuentaNombre = cuenta.nombre,
                                    cuentaTipo = cuenta.tipo,
                                    moneda = cuenta.moneda,
                                    uso = cuenta.uso,
                                    periodoTexto = uiState.periodoSeleccionado.name,
                                    saldoInicialPeriodo = saldoInicialPeriodo,
                                    entradasPeriodo = entradasPeriodo,
                                    salidasPeriodo = salidasPeriodo,
                                    saldoFinalPeriodo = saldoFinalPeriodo,
                                    movimientos = movsConSaldoCorridoDesc
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📄 Extracto CSV", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoCrearEditarCuenta(
    cuentaExistente: CuentaBancaria?,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        tipo: String,
        moneda: String,
        saldoInicial: Double,
        institucion: String?,
        alias: String?,
        uso: String?,
        metaUsd: Double?,
        datosDePago: String?,
        color: String?,
        icono: String?
    ) -> Unit
) {
    var nombre by remember { mutableStateOf(cuentaExistente?.nombre ?: "") }
    var tipo by remember { mutableStateOf(cuentaExistente?.tipo ?: "BANCO") }
    var moneda by remember { mutableStateOf(cuentaExistente?.moneda ?: "USD") }
    var saldoInicialStr by remember { mutableStateOf(cuentaExistente?.saldoInicial?.toString() ?: "0.0") }
    var institucion by remember { mutableStateOf(cuentaExistente?.institucion ?: "") }
    var alias by remember { mutableStateOf(cuentaExistente?.alias ?: "") }
    var uso by remember { mutableStateOf(cuentaExistente?.uso ?: "Cobros") }
    var metaUsdStr by remember { mutableStateOf(cuentaExistente?.metaUsd?.toString() ?: "") }
    var datosDePago by remember { mutableStateOf(cuentaExistente?.datosDePago ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (cuentaExistente != null) "✏️ Editar Cuenta / Banco" else "➕ Nueva Cuenta o Banco",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = com.example.gestor_deudores.ui.theme.estados
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la cuenta (ej. Mercantil Corriente)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Tipo de Cuenta:", fontSize = 11.sp, color = Color.Gray)
                val tiposDisponibles = listOf("BANCO" to "Banco Nacional", "PAGO_MOVIL" to "Pago Móvil", "EFECTIVO" to "Efectivo", "BILLETERA" to "Billetera Virtual", "BANCO_USD" to "Banco en $ / Zelle", "OTRA" to "Otra")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(tiposDisponibles) { (clave, label) ->
                        FilterChip(
                            selected = tipo == clave,
                            onClick = { tipo = clave },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Moneda:", fontSize = 12.sp, color = Color.Gray)
                    Row {
                        FilterChip(
                            selected = moneda == "USD",
                            onClick = { moneda = "USD" },
                            label = { Text("USD ($)", fontSize = 11.sp) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = moneda == "VES",
                            onClick = { moneda = "VES" },
                            label = { Text("VES (Bs)", fontSize = 11.sp) }
                        )
                    }
                }

                Text("Etiqueta de Uso:", fontSize = 11.sp, color = Color.Gray)
                val usosDisponibles = listOf("Cobros", "Reposición", "Ahorro", "Gastos del taller", "Personal")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(usosDisponibles) { u ->
                        FilterChip(
                            selected = uso == u,
                            onClick = { uso = u },
                            label = { Text(u, fontSize = 10.sp) }
                        )
                    }
                }

                if (cuentaExistente == null) {
                    OutlinedTextField(
                        value = saldoInicialStr,
                        onValueChange = { saldoInicialStr = it },
                        label = { Text("Saldo Inicial ($moneda)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = datosDePago,
                    onValueChange = { datosDePago = it },
                    label = { Text("Datos para recibir pagos (Teléfono, CI, Correo, etc.)") },
                    placeholder = { Text("Ej. CI 12345678 · 04121234567 · Mercantil") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = metaUsdStr,
                    onValueChange = { metaUsdStr = it },
                    label = { Text("Meta opcional de saldo ($)") },
                    placeholder = { Text("Ej. 50.00") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                    Button(
                        onClick = {
                            if (nombre.isNotBlank()) {
                                val sInit = saldoInicialStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val metaVal = metaUsdStr.replace(",", ".").toDoubleOrNull()
                                onGuardar(nombre, tipo, moneda, sInit, institucion, alias, uso, metaVal, datosDePago, null, null)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.gestor_deudores.ui.theme.estados)
                    ) {
                        Text("Guardar Cuenta", color = Color.White)
                    }
                }
            }
        }
    }
}

fun sanitizarTextoMonto(input: String): String {
    if (input.isBlank()) return ""
    var reemp = input.replace(',', '.')
    var limpio = reemp.filter { it.isDigit() || it == '.' }
    val primerPunto = limpio.indexOf('.')
    if (primerPunto != -1) {
        val parteEntera = limpio.substring(0, primerPunto)
        val parteDecimal = limpio.substring(primerPunto + 1).replace(".", "")
        val decimalCortado = if (parteDecimal.length > 2) parteDecimal.substring(0, 2) else parteDecimal
        limpio = "$parteEntera.$decimalCortado"
    }
    return limpio
}

@Composable
fun DialogoRegistrarMovimientoRapido(
    cuenta: CuentaBancaria,
    esIngreso: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (monto: Double, concepto: String, ref: String?, foto: String?) -> Unit
) {
    var montoTexto by remember { mutableStateOf("") }
    var conceptoTexto by remember { mutableStateOf("") }
    var referenciaTexto by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (esIngreso) "➕ Registrar Ingreso · ${cuenta.nombre}" else "− Registrar Gasto · ${cuenta.nombre}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = sanitizarTextoMonto(it) },
                    label = { Text("Monto (${cuenta.moneda})") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = conceptoTexto,
                    onValueChange = { conceptoTexto = it },
                    label = { Text("Concepto / Descripción") },
                    placeholder = { Text(if (esIngreso) "Ej. Abono cliente, Venta contado" else "Ej. Compra de tintas, Alquiler") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = referenciaTexto,
                    onValueChange = { referenciaTexto = it },
                    label = { Text("Número de Referencia (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.LightGray) }
                    Button(
                        onClick = {
                            val mVal = montoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (mVal > 0) {
                                onGuardar(mVal, conceptoTexto, referenciaTexto, null)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (esIngreso) Color(0xFF2E7D32) else Color(0xFFC62828))
                    ) {
                        Text(if (esIngreso) "Guardar Ingreso" else "Guardar Gasto", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoTransferirEntreCuentas(
    cuentaOrigenInicial: CuentaBancaria,
    cuentasDisponibles: List<CuentaBancaria>,
    tasaBcv: Double,
    onDismiss: () -> Unit,
    onTransferir: (
        cuentaOrigen: CuentaBancaria,
        cuentaDestino: CuentaBancaria,
        montoOrigen: Double,
        montoDestino: Double,
        tasaUsada: Double,
        concepto: String,
        ref: String?
    ) -> Unit
) {
    var cuentaOrigen by remember { mutableStateOf(cuentaOrigenInicial) }
    var cuentaDestino by remember {
        mutableStateOf(cuentasDisponibles.firstOrNull { it.id != cuentaOrigenInicial.id } ?: cuentaOrigenInicial)
    }

    var montoOrigenTexto by remember { mutableStateOf("") }
    var conceptoTexto by remember { mutableStateOf("") }
    var referenciaTexto by remember { mutableStateOf("") }

    val mOrigVal = montoOrigenTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
    val mDestVal = remember(mOrigVal, cuentaOrigen, cuentaDestino, tasaBcv) {
        if (cuentaOrigen.moneda == cuentaDestino.moneda) {
            mOrigVal
        } else if (cuentaOrigen.moneda == "USD" && cuentaDestino.moneda == "VES") {
            (mOrigVal * tasaBcv).redondear2()
        } else if (cuentaOrigen.moneda == "VES" && cuentaDestino.moneda == "USD" && tasaBcv > 0) {
            (mOrigVal / tasaBcv).redondear2()
        } else {
            mOrigVal
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⇄ Mover / Transferir Dinero", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)

                Text("Cuenta Origen (Sale dinero):", fontSize = 11.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(cuentasDisponibles) { c ->
                        FilterChip(
                            selected = cuentaOrigen.id == c.id,
                            onClick = { cuentaOrigen = c },
                            label = { Text("${c.nombre} (${c.moneda})", fontSize = 10.sp) }
                        )
                    }
                }

                Text("Cuenta Destino (Entra dinero):", fontSize = 11.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(cuentasDisponibles.filter { it.id != cuentaOrigen.id }) { c ->
                        FilterChip(
                            selected = cuentaDestino.id == c.id,
                            onClick = { cuentaDestino = c },
                            label = { Text("${c.nombre} (${c.moneda})", fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = montoOrigenTexto,
                    onValueChange = { montoOrigenTexto = sanitizarTextoMonto(it) },
                    label = { Text("Monto a transferir (${cuentaOrigen.moneda})") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (cuentaOrigen.moneda != cuentaDestino.moneda) {
                    Text(
                        text = "Monto estimado en destino: ${cuentaDestino.moneda} ${String.format(Locale.US, "%.2f", mDestVal)} (Tasa: $tasaBcv)",
                        fontSize = 11.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = conceptoTexto,
                    onValueChange = { conceptoTexto = it },
                    label = { Text("Motivo / Concepto (Opcional)") },
                    placeholder = { Text("Ej. Fondeo de caja chica, Cambio a Bs") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = referenciaTexto,
                    onValueChange = { referenciaTexto = it },
                    label = { Text("Número de Referencia (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.LightGray) }
                    Button(
                        onClick = {
                            if (mOrigVal > 0 && cuentaOrigen.id != cuentaDestino.id) {
                                onTransferir(cuentaOrigen, cuentaDestino, mOrigVal, mDestVal, tasaBcv, conceptoTexto, referenciaTexto)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00707A))
                    ) {
                        Text("Confirmar Transferencia", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoAjustarSaldoCuenta(
    cuenta: CuentaBancaria,
    onDismiss: () -> Unit,
    onGuardarAjuste: (nuevoSaldo: Double, motivo: String) -> Unit
) {
    var nuevoSaldoTexto by remember { mutableStateOf(cuenta.saldoActual.toString()) }
    var motivoTexto by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val nVal = nuevoSaldoTexto.replace(",", ".").toDoubleOrNull() ?: cuenta.saldoActual
    val dif = (nVal - cuenta.saldoActual).redondear2()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = fondo2)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚙️ Ajustar Saldo · ${cuenta.nombre}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Text("Crea un movimiento de ajuste contable sin alterar el historial existente.", fontSize = 11.sp, color = Color.LightGray)

                Text("Saldo actual en sistema: ${cuenta.moneda} ${String.format(Locale.US, "%.2f", cuenta.saldoActual)}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = nuevoSaldoTexto,
                    onValueChange = { nuevoSaldoTexto = sanitizarTextoMonto(it) },
                    label = { Text("Nuevo Saldo Real (${cuenta.moneda})") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Diferencia a registrar: ${if (dif >= 0) "+$dif" else "$dif"} ${cuenta.moneda}",
                    fontSize = 11.sp,
                    color = if (dif >= 0) Color(0xFF81C784) else Color(0xFFFF7043),
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = motivoTexto,
                    onValueChange = {
                        motivoTexto = it
                        if (errorMsg.isNotEmpty()) errorMsg = ""
                    },
                    label = { Text("Motivo / Razón Obligatoria del Ajuste") },
                    placeholder = { Text("Ej. Arqueo de caja, Corrección por comisión no registrada") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.LightGray) }
                    Button(
                        onClick = {
                            if (motivoTexto.isBlank()) {
                                errorMsg = "Por favor ingresa el motivo del ajuste"
                            } else {
                                onGuardarAjuste(nVal, motivoTexto)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.gestor_deudores.ui.theme.estados)
                    ) {
                        Text("Guardar Ajuste", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTE 9: ESTADO DE PEDIDOS (FUNNEL)
// ==========================================
@Composable
fun SeccionEstadoPedidos(uiState: ResumenUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "📦 Estado de Pedidos del Período",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ItemEstadoPedido("Recibidos", uiState.pedidosRecibidosCount, Color(0xFF1976D2), Modifier.weight(1f))
            ItemEstadoPedido("En Proceso", uiState.pedidosProduccionCount, Color(0xFFF57C00), Modifier.weight(1f))
            ItemEstadoPedido("Listos", uiState.pedidosListosCount, Color(0xFF388E3C), Modifier.weight(1f))
            ItemEstadoPedido("Entregados", uiState.pedidosEntregadosCount, Color(0xFF7B1FA2), Modifier.weight(1f))
        }
    }
}

@Composable
fun ItemEstadoPedido(titulo: String, cantidad: Int, color: Color, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = cantidad.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(text = titulo, fontSize = 10.sp, color = color)
        }
    }
}

@Composable
fun DialogoAjustarCostosBase(
    config: ConfiguracionCostos,
    onDismiss: () -> Unit,
    onGuardarConfig: (pctTextil: Float, pctPasaje: Float, pctInsumos: Float, pctGanancia: Float) -> Unit
) {
    var pctTextilStr by remember { mutableStateOf(config.pctTextil.toString()) }
    var pctPasajeStr by remember { mutableStateOf(config.pctPasaje.toString()) }
    var pctInsumosStr by remember { mutableStateOf(config.pctInsumos.toString()) }
    var pctGananciaStr by remember { mutableStateOf(config.pctGanancia.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚙️ Configurar Costos Base Real", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ajusta el porcentaje de costo real de tus productos (ej. camisa $3.90, pasaje, insumos y ganancia):",
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = pctTextilStr,
                    onValueChange = { pctTextilStr = it },
                    label = { Text("% Costo Camisa / Textil (ej: 32.5% = $3.90 en $12)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pctPasajeStr,
                    onValueChange = { pctPasajeStr = it },
                    label = { Text("% Costo Pasaje / Logística (ej: 8.3% = $1.00 en $12)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pctInsumosStr,
                    onValueChange = { pctInsumosStr = it },
                    label = { Text("% Tintas, Papel & Insumos (ej: 15.0%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pctGananciaStr,
                    onValueChange = { pctGananciaStr = it },
                    label = { Text("% Ganancia Neta Libre (ej: 44.2% = $5.30 en $12)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pTextil = pctTextilStr.toFloatOrNull() ?: config.pctTextil
                    val pPasaje = pctPasajeStr.toFloatOrNull() ?: config.pctPasaje
                    val pInsumos = pctInsumosStr.toFloatOrNull() ?: config.pctInsumos
                    val pGanancia = pctGananciaStr.toFloatOrNull() ?: config.pctGanancia
                    onGuardarConfig(pTextil, pPasaje, pInsumos, pGanancia)
                }
            ) {
                Text("Guardar Costos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// COMPONENTE 11: PROYECCIÓN DE COBROS (SECCIÓN 11)
// ==========================================
@Composable
fun SeccionProyeccionCobros(
    proyeccion: com.example.gestor_deudores.data.utils.ProyeccionCobrosPagos,
    montosOcultos: Boolean = false
) {
    val cobros7Text = if (montosOcultos) "••••••" else "$${String.format(Locale.getDefault(), "%.2f", proyeccion.cobros7DiasUsd)} USD"
    val cobros30Text = if (montosOcultos) "••••••" else "$${String.format(Locale.getDefault(), "%.2f", proyeccion.cobros30DiasUsd)} USD"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📈 Proyección de Cobros (Esperado)", fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Próximos 7 Días:", fontSize = 11.sp, color = Color.Gray)
                    Text(cobros7Text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Próximos 30 Días:", fontSize = 11.sp, color = Color.Gray)
                    Text(cobros30Text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                }
            }

            Text("Esta semana esperas cobrar unos $cobros7Text de cuotas y entregas.", fontSize = 11.sp, color = Color.DarkGray)
        }
    }
}

// ==========================================
// COMPONENTE 12: ANTIGÜEDAD DE CARTERA (SECCIÓN 12)
// ==========================================
@Composable
fun SeccionAntiguedadCartera(
    cartera: com.example.gestor_deudores.data.utils.GrupoAntiguedadCartera,
    montosOcultos: Boolean = false,
    onEnviarRecordatorios: () -> Unit
) {
    val alDiaText = if (montosOcultos) "••••••" else "$${cartera.alDiaMontoUsd} USD"
    val a1a7Text = if (montosOcultos) "••••••" else "$${cartera.atraso1a7MontoUsd} USD"
    val a8a30Text = if (montosOcultos) "••••••" else "$${cartera.atraso8a30MontoUsd} USD"
    val aMas30Text = if (montosOcultos) "••••••" else "$${cartera.atrasoMas30MontoUsd} USD"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋 Antigüedad de Cartera", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (cartera.porcentajePuntualidad >= 80) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Puntualidad: ${cartera.porcentajePuntualidad}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (cartera.porcentajePuntualidad >= 80) Color(0xFF2E7D32) else Color(0xFFD32F2F))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("• Al día: $alDiaText (${cartera.alDiaClientesCount} clientes)", fontSize = 11.sp, color = Color(0xFF2E7D32))
                Text("• 1 a 7 días de atraso: $a1a7Text (${cartera.atraso1a7ClientesCount} clientes)", fontSize = 11.sp, color = Color(0xFFF57F17))
                Text("• 8 a 30 días de atraso: $a8a30Text (${cartera.atraso8a30ClientesCount} clientes)", fontSize = 11.sp, color = Color(0xFFE65100))
                Text("• Más de 30 días de atraso: $aMas30Text (${cartera.atrasoMas30ClientesCount} clientes)", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        }
    }
}
