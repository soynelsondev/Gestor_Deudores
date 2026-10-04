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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
    var cuentaParaEditarSaldo by remember { mutableStateOf<CuentaBancaria?>(null) }
    var mostrarDialogoAjustarCostos by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "RESUMEN FINANCIERO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        BotonConmutadorMoneda(
                            monedaActual = uiState.monedaVista,
                            onCambiarMoneda = { viewModel.cambiarMonedaVista(it) }
                        )
                    }
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

                // 2. Alertas Financieras de Salud
                if (uiState.alertasFinancieras.isNotEmpty()) {
                    SeccionAlertasFinancieras(alertas = uiState.alertasFinancieras)
                }

                // 3. Tarjetas KPI Totales
                TarjetasKpiTotales(uiState = uiState)

                // 4. GRÁFICA VISUAL: Evaluación de Ganancias vs. Costos de Producción
                GraficaPerdidasGanancias(uiState = uiState)

                // 5. Carrusel de Sobres / Fondos de Insumos
                SeccionCarruselSobres(fondos = uiState.fondosInsumos, monedaVista = uiState.monedaVista, tasaBcv = uiState.tasaBcv)

                // 6. Radiografía de Pago Individual y Consolidado con Selector de Cliente
                SeccionRadiografiaPagos(
                    uiState = uiState,
                    onSeleccionarAbono = { index -> viewModel.seleccionarAbono(index) },
                    onAbrirAjusteCostos = { mostrarDialogoAjustarCostos = true }
                )

                // 7. Sección Bancos y Billeteras con Edición/Ajuste de Saldo
                SeccionCuentasBancarias(
                    cuentas = uiState.cuentasBancarias,
                    monedaVista = uiState.monedaVista,
                    tasaBcv = uiState.tasaBcv,
                    onAgregarCuentaClick = { mostrarDialogoAgregarCuenta = true },
                    onEditarSaldoClick = { cuenta -> cuentaParaEditarSaldo = cuenta },
                    onEliminarCuentaClick = { viewModel.eliminarCuentaBancaria(it) }
                )

                // 8. Resumen de Estado de Pedidos
                SeccionEstadoPedidos(uiState = uiState)
            }
        }

        // Diálogo modal para agregar nueva cuenta bancaria
        if (mostrarDialogoAgregarCuenta) {
            DialogoAgregarCuentaBancaria(
                onDismiss = { mostrarDialogoAgregarCuenta = false },
                onGuardarCuenta = { nombre, tipo, moneda, saldo ->
                    viewModel.agregarCuentaBancaria(nombre, tipo, moneda, saldo)
                    mostrarDialogoAgregarCuenta = false
                }
            )
        }

        // Diálogo modal para editar/ajustar saldo de una cuenta existente
        cuentaParaEditarSaldo?.let { cuenta ->
            DialogoAjustarSaldoCuenta(
                cuenta = cuenta,
                onDismiss = { cuentaParaEditarSaldo = null },
                onGuardarSaldo = { nuevoSaldo ->
                    viewModel.actualizarSaldoCuenta(cuenta, nuevoSaldo)
                    cuentaParaEditarSaldo = null
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
// COMPONENTE 3: GRÁFICA DE EVALUACIÓN
// ==========================================
@Composable
fun GraficaPerdidasGanancias(uiState: ResumenUiState) {
    val totalFacturado = uiState.ingresosCobradosUsd.coerceAtLeast(0.01)
    val pctCostos = ((uiState.fondoReposicionUsd / totalFacturado) * 100).coerceIn(0.0, 100.0)
    val pctGanancia = ((uiState.gananciaNetaUsd / totalFacturado) * 100).coerceIn(0.0, 100.0)

    val colorGanancia = Color(0xFF2E7D32)
    val colorCostos = Color(0xFF1565C0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    text = "Evaluación de Rentabilidad",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Margen: ${String.format(Locale.getDefault(), "%.1f", pctGanancia)}%",
                    fontWeight = FontWeight.Bold,
                    color = colorGanancia,
                    fontSize = 13.sp
                )
            }

            // Barra gráfica doble
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.LightGray)
            ) {
                if (pctCostos > 0) {
                    Box(
                        modifier = Modifier
                            .weight(pctCostos.toFloat())
                            .fillMaxSize()
                            .background(colorCostos)
                    )
                }
                if (pctGanancia > 0) {
                    Box(
                        modifier = Modifier
                            .weight(pctGanancia.toFloat())
                            .fillMaxSize()
                            .background(colorGanancia)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colorCostos))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Costos / Reposición (${String.format(Locale.getDefault(), "%.0f", pctCostos)}%)",
                        fontSize = 11.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colorGanancia))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ganancia Neta (${String.format(Locale.getDefault(), "%.0f", pctGanancia)}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorGanancia
                    )
                }
            }
        }
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
// COMPONENTE 5: TARJETAS KPI TOTALES
// ==========================================
@Composable
fun TarjetasKpiTotales(uiState: ResumenUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaKpiItem(
                titulo = "Ingresos Cobrados",
                montoUsd = uiState.ingresosCobradosUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            TarjetaKpiItem(
                titulo = "Fondo Reposición",
                montoUsd = uiState.fondoReposicionUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaKpiItem(
                titulo = "Ganancia Neta Libre",
                montoUsd = uiState.gananciaNetaUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFFC2185B),
                modifier = Modifier.weight(1f)
            )
            TarjetaKpiItem(
                titulo = "Por Cobrar (Calle)",
                montoUsd = uiState.porCobrarUsd,
                monedaVista = uiState.monedaVista,
                tasaBcv = uiState.tasaBcv,
                color = Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TarjetaKpiItem(
    titulo: String,
    montoUsd: Double,
    monedaVista: String,
    tasaBcv: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val montoMostrar = if (monedaVista == "VES" && tasaBcv > 0) montoUsd * tasaBcv else montoUsd
    val simbolo = if (monedaVista == "VES") "Bs" else "$"

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$simbolo ${String.format(Locale.getDefault(), "%.2f", montoMostrar)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ==========================================
// COMPONENTE 6: CARRUSEL DE SOBRES DE INSUMOS
// ==========================================
@Composable
fun SeccionCarruselSobres(
    fondos: List<FondoInsumoItem>,
    monedaVista: String,
    tasaBcv: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "División de Fondos por Insumos (Sobres)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(fondos) { fondo ->
                val montoMostrar = if (monedaVista == "VES" && tasaBcv > 0) fondo.montoUsd * tasaBcv else fondo.montoUsd
                val simbolo = if (monedaVista == "VES") "Bs" else "$"

                Card(
                    modifier = Modifier.width(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = fondo.icono, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fondo.nombre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "$simbolo ${String.format(Locale.getDefault(), "%.2f", montoMostrar)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LinearProgressIndicator(
                            progress = { (fondo.porcentajeDelTotal / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
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
                    val itemMonto = if (monedaVista == "VES" && tasaBcv > 0) item.montoUsd * tasaBcv else item.montoUsd
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• ${item.concepto}", fontSize = 12.sp)
                        Text(
                            text = "$simbolo ${String.format(Locale.getDefault(), "%.2f", itemMonto)} (${String.format(Locale.getDefault(), "%.1f", item.porcentaje)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTE 8: SECCIÓN BANCARIA Y BILLETERAS
// ==========================================
@Composable
fun SeccionCuentasBancarias(
    cuentas: List<CuentaBancaria>,
    monedaVista: String,
    tasaBcv: Double,
    onAgregarCuentaClick: () -> Unit,
    onEditarSaldoClick: (CuentaBancaria) -> Unit,
    onEliminarCuentaClick: (CuentaBancaria) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏦 Bancos y Billeteras",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = onAgregarCuentaClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nueva", fontSize = 12.sp)
            }
        }

        if (cuentas.isEmpty()) {
            Text(
                text = "No tienes cuentas o bancos registrados. Pulsa '+ Nueva' para agregar uno.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cuentas) { cuenta ->
                    val montoMostrar = if (monedaVista == "VES" && cuenta.moneda == "USD" && tasaBcv > 0) {
                        cuenta.saldoActual * tasaBcv
                    } else cuenta.saldoActual

                    val simbolo = if (monedaVista == "VES" || cuenta.moneda == "VES") "Bs" else "$"

                    Card(
                        modifier = Modifier.width(190.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cuenta.nombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(
                                        onClick = { onEditarSaldoClick(cuenta) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar Saldo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEliminarCuentaClick(cuenta) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${cuenta.tipo} · ${cuenta.moneda}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "$simbolo ${String.format(Locale.getDefault(), "%.2f", montoMostrar)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
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

// ==========================================
// COMPONENTE 10: DIÁLOGOS DE CUENTAS Y COSTOS BASE
// ==========================================
@Composable
fun DialogoAgregarCuentaBancaria(
    onDismiss: () -> Unit,
    onGuardarCuenta: (nombre: String, tipo: String, moneda: String, saldo: Double) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("BANCO") }
    var moneda by remember { mutableStateOf("USD") }
    var saldoStr by remember { mutableStateOf("0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Cuenta o Banco", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (ej: Mercantil, Zelle)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = saldoStr,
                    onValueChange = { saldoStr = it },
                    label = { Text("Saldo Inicial") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Moneda:", fontSize = 12.sp)
                    Row {
                        TextButton(onClick = { moneda = "USD" }) {
                            Text("USD ($)", fontWeight = if (moneda == "USD") FontWeight.Bold else FontWeight.Normal)
                        }
                        TextButton(onClick = { moneda = "VES" }) {
                            Text("VES (Bs)", fontWeight = if (moneda == "VES") FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        val saldo = saldoStr.toDoubleOrNull() ?: 0.0
                        onGuardarCuenta(nombre, tipo, moneda, saldo)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogoAjustarSaldoCuenta(
    cuenta: CuentaBancaria,
    onDismiss: () -> Unit,
    onGuardarSaldo: (nuevoSaldo: Double) -> Unit
) {
    var saldoStr by remember { mutableStateOf(cuenta.saldoActual.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Saldo de ${cuenta.nombre}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ingresa el saldo real disponible en este banco o billetera:", fontSize = 12.sp)
                OutlinedTextField(
                    value = saldoStr,
                    onValueChange = { saldoStr = it },
                    label = { Text("Saldo Real (${cuenta.moneda})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val saldo = saldoStr.toDoubleOrNull() ?: cuenta.saldoActual
                    onGuardarSaldo(saldo)
                }
            ) {
                Text("Guardar Saldo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
