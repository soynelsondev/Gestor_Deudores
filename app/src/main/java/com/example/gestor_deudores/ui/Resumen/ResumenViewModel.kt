package com.example.gestor_deudores.ui.Resumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.database.CuentaBancaria
import com.example.gestor_deudores.data.database.CuentaBancariaDao
import com.example.gestor_deudores.data.database.Deuda
import com.example.gestor_deudores.data.database.DeudaDao
import com.example.gestor_deudores.data.database.Deudor
import com.example.gestor_deudores.data.database.DeudorDao
import com.example.gestor_deudores.data.database.PedidoConCliente
import com.example.gestor_deudores.data.database.PedidoDao
import com.example.gestor_deudores.data.database.PlantillaCotizacion
import com.example.gestor_deudores.data.database.PlantillaDao
import com.example.gestor_deudores.data.utils.obtenerFechaMillisValida
import com.example.gestor_deudores.data.utils.redondear2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

enum class PeriodoFiltro {
    ESTA_SEMANA,
    ESTE_MES,
    MES_ANTERIOR,
    ULTIMOS_3_MESES,
    TODO_EL_HISTORICO
}

data class FondoInsumoItem(
    val id: String,
    val nombre: String,
    val montoUsd: Double,
    val porcentajeDelTotal: Float,
    val icono: String,
    val colorHex: String
)

data class DesglosePagoItem(
    val concepto: String,
    val montoUsd: Double,
    val porcentaje: Float,
    val subItems: List<DesglosePagoItem> = emptyList() // NUEVO: Para el menú desplegable (Acordeón)
)

data class DesglosePagoIndividual(
    val idDeuda: Int, // -1 para Consolidado Total
    val clienteNombre: String,
    val montoTotalAbonoUsd: Double,
    val fechaStr: String,
    val desgloseItems: List<DesglosePagoItem>
)

data class AlertaFinanciera(
    val titulo: String,
    val mensaje: String,
    val nivel: String // "INFO", "WARNING", "DANGER"
)

data class ConfiguracionCostos(
    val pctTextil: Float = 32.5f,  // Ej: $3.90 en $12
    val pctPasaje: Float = 8.3f,   // Ej: $1.00 en $12
    val pctInsumos: Float = 15.0f, // Ej: $1.80 en $12
    val pctGanancia: Float = 44.2f  // Ej: $5.30 en $12
)

data class ResumenUiState(
    val cargando: Boolean = false,
    val periodoSeleccionado: PeriodoFiltro = PeriodoFiltro.ESTE_MES,
    val monedaVista: String = "USD", // "USD" o "VES"
    val tasaBcv: Double = 0.0,
    val ingresosCobradosUsd: Double = 0.0,
    val fondoReposicionUsd: Double = 0.0,
    val gananciaNetaUsd: Double = 0.0,
    val porCobrarUsd: Double = 0.0,
    val fondosInsumos: List<FondoInsumoItem> = emptyList(),
    val cuentasBancarias: List<CuentaBancaria> = emptyList(),
    val listaAbonosDesglose: List<DesglosePagoIndividual> = emptyList(),
    val abonoSeleccionadoIndex: Int = 0,
    val configuracionCostos: ConfiguracionCostos = ConfiguracionCostos(),
    val pedidosRecibidosCount: Int = 0,
    val pedidosProduccionCount: Int = 0,
    val pedidosListosCount: Int = 0,
    val pedidosEntregadosCount: Int = 0,
    val alertasFinancieras: List<AlertaFinanciera> = emptyList(),
    val topProductos: List<Pair<String, Int>> = emptyList(),
    
    // Métricas del Mes Anterior (para el Cuadro Contable del PDF)
    val ingresosCobradosMesAnterior: Double = 0.0,
    val gananciaNetaMesAnterior: Double = 0.0,
    val costoPasajesUsd: Double = 0.0,
    val costoInsumosUsd: Double = 0.0,
    val costoPasajesMesAnterior: Double = 0.0,
    val costoInsumosMesAnterior: Double = 0.0
) {
    val abonoSeleccionado: DesglosePagoIndividual?
        get() = if (listaAbonosDesglose.isNotEmpty() && abonoSeleccionadoIndex in listaAbonosDesglose.indices) {
            listaAbonosDesglose[abonoSeleccionadoIndex]
        } else null
}

private data class DatosDbCombinados(
    val deudas: List<Deuda>,
    val deudores: List<Deudor>,
    val pedidos: List<PedidoConCliente>,
    val plantillas: List<PlantillaCotizacion>,
    val cuentas: List<CuentaBancaria>
)

private data class FiltrosUiCombinados(
    val periodo: PeriodoFiltro,
    val moneda: String,
    val tasa: Double,
    val abonoIdx: Int,
    val configCostos: ConfiguracionCostos
)

class ResumenViewModel(
    private val deudaDao: DeudaDao,
    private val deudorDao: DeudorDao,
    private val pedidoDao: PedidoDao,
    private val plantillaDao: PlantillaDao,
    private val cuentaBancariaDao: CuentaBancariaDao
) : ViewModel() {

    private val _periodoSeleccionado = MutableStateFlow(PeriodoFiltro.ESTE_MES)
    val periodoSeleccionado = _periodoSeleccionado.asStateFlow()

    private val _monedaVista = MutableStateFlow("USD")
    val monedaVista = _monedaVista.asStateFlow()

    private val _tasaBcv = MutableStateFlow(0.0)
    val tasaBcv = _tasaBcv.asStateFlow()

    private val _abonoSeleccionadoIndex = MutableStateFlow(0)
    val abonoSeleccionadoIndex = _abonoSeleccionadoIndex.asStateFlow()

    private val _configuracionCostos = MutableStateFlow(ConfiguracionCostos())
    val configuracionCostos = _configuracionCostos.asStateFlow()

    fun actualizarTasaBcv(tasa: Double) {
        _tasaBcv.value = tasa
    }

    fun cambiarPeriodo(nuevoPeriodo: PeriodoFiltro) {
        _periodoSeleccionado.value = nuevoPeriodo
        _abonoSeleccionadoIndex.value = 0
    }

    fun cambiarMonedaVista(nuevaMoneda: String) {
        _monedaVista.value = nuevaMoneda
    }

    fun seleccionarAbono(index: Int) {
        _abonoSeleccionadoIndex.value = index
    }

    fun guardarConfiguracionCostos(pctTextil: Float, pctPasaje: Float, pctInsumos: Float, pctGanancia: Float) {
        _configuracionCostos.value = ConfiguracionCostos(
            pctTextil = pctTextil.coerceIn(1f, 90f),
            pctPasaje = pctPasaje.coerceIn(0f, 50f),
            pctInsumos = pctInsumos.coerceIn(0f, 50f),
            pctGanancia = pctGanancia.coerceIn(1f, 90f)
        )
    }

    // --- ACCIONES DE GESTIÓN DE CUENTAS BANCARIAS / BILLETERAS ---
    fun agregarCuentaBancaria(nombre: String, tipo: String, moneda: String, saldoInicial: Double) {
        viewModelScope.launch {
            val nuevaCuenta = CuentaBancaria(
                nombre = nombre.trim(),
                tipo = tipo,
                moneda = moneda,
                saldoActual = saldoInicial.redondear2()
            )
            cuentaBancariaDao.agregarCuenta(nuevaCuenta)
        }
    }

    fun actualizarSaldoCuenta(cuenta: CuentaBancaria, nuevoSaldo: Double) {
        viewModelScope.launch {
            val cuentaActualizada = cuenta.copy(saldoActual = nuevoSaldo.redondear2())
            cuentaBancariaDao.actualizarCuenta(cuentaActualizada)
        }
    }

    fun eliminarCuentaBancaria(cuenta: CuentaBancaria) {
        viewModelScope.launch {
            cuentaBancariaDao.eliminarCuenta(cuenta)
        }
    }

    // Combined Flow 1: Base de datos
    private val datosDbFlow = combine(
        deudaDao.obtenerTodasLasDeudas(),
        deudorDao.obtenerDeudores(),
        pedidoDao.obtenerPedidosConCliente(),
        plantillaDao.obtenerTodasLasPlantillas(),
        cuentaBancariaDao.obtenerTodasLasCuentas()
    ) { deudas, deudores, pedidos, plantillas, cuentas ->
        DatosDbCombinados(deudas, deudores, pedidos, plantillas, cuentas)
    }

    // Combined Flow 2: Filtros de UI
    private val filtrosUiFlow = combine(
        _periodoSeleccionado,
        _monedaVista,
        _tasaBcv,
        _abonoSeleccionadoIndex,
        _configuracionCostos
    ) { periodo, moneda, tasa, abonoIdx, config ->
        FiltrosUiCombinados(periodo, moneda, tasa, abonoIdx, config)
    }

    // Combined Flow Final: Cerebro de cálculo completo
    val uiState: StateFlow<ResumenUiState> = combine(
        datosDbFlow,
        filtrosUiFlow
    ) { dbData, filtros ->
        val deudas = dbData.deudas
        val deudores = dbData.deudores
        val pedidos = dbData.pedidos
        val plantillas = dbData.plantillas
        val cuentas = dbData.cuentas

        val periodo = filtros.periodo
        val moneda = filtros.moneda
        val tasa = filtros.tasa
        val abonoIdx = filtros.abonoIdx
        val config = filtros.configCostos

        val rangoMillis = calcularRangoMillis(periodo)
        val inicioMillis = rangoMillis.first
        val finMillis = rangoMillis.second

        // 1. Filtrado de deudas / abonos por fecha de período
        val deudasEnPeriodo = deudas.filter { deuda ->
            val fechaValida = obtenerFechaMillisValida(deuda)
            fechaValida in inicioMillis..finMillis
        }

        // 2. Cálculo de Ingresos Cobrados (Suma de abonos reales)
        val abonosEnPeriodo = deudasEnPeriodo.filter { it.tipo == "ABONO" }
        val ingresosCobradosUsd = abonosEnPeriodo.sumOf { Math.abs(it.montoRestante) }.redondear2()

        // 3. Cálculo de Cuentas por Cobrar Pendientes en la calle
        val cargosActivos = deudas.filter { it.tipo == "CARGO" && it.estado != "Cancelado" }
        val totalCargosUsd = cargosActivos.sumOf { it.montoInicial }.redondear2()
        val totalAbonosHistoricosUsd = deudas.filter { it.tipo == "ABONO" }.sumOf { Math.abs(it.montoRestante) }.redondear2()
        val porCobrarUsd = (totalCargosUsd - totalAbonosHistoricosUsd).coerceAtLeast(0.0).redondear2()

        // 4. Estimación de Fondo de Reposición vs Ganancia Neta usando la FOTOGRAFÍA del Pedido
        // Buscamos los pedidos vinculados a los abonos del período para extraer sus costos exactos
        var fondoReposicionTextilUsd = 0.0
        var fondoReposicionPasajesUsd = 0.0
        var fondoReposicionInsumosUsd = 0.0

        for (abono in abonosEnPeriodo) {
            val montoAbono = Math.abs(abono.montoRestante)
            val cargoOriginal = cargosActivos.find { it.idDeudor == abono.idDeudor && it.tipoDeuda != "Abono / Pago Parcial" }
            
            if (cargoOriginal != null) {
                // Buscamos si existe un pedido fotográfico asociado a este cargo
                // (Para esto nos guiaremos por el monto, fecha o cliente, idealmente en la BD habría un pedidoId en Deuda)
                // Como aproximación, buscamos el pedido del cliente que coincida en monto o fecha cercana
                val pedidoFotografia = pedidos.find { it.deudorId == abono.idDeudor && it.totalUsd == cargoOriginal.montoInicial }
                
                if (pedidoFotografia != null && (pedidoFotografia.costoPiezaBaseUsd > 0 || pedidoFotografia.costoInsumosUsd > 0)) {
                    // Usar Fotografía Histórica del Pedido
                    val proporcionAbono = if (pedidoFotografia.totalUsd > 0) montoAbono / pedidoFotografia.totalUsd else 1.0
                    fondoReposicionTextilUsd += (pedidoFotografia.costoPiezaBaseUsd * proporcionAbono)
                    fondoReposicionPasajesUsd += (pedidoFotografia.costoPasajeUsd * proporcionAbono)
                    fondoReposicionInsumosUsd += (pedidoFotografia.costoInsumosUsd * proporcionAbono)
                } else {
                    // Si es un pedido viejo sin fotografía o registro suelto, usamos el config base
                    fondoReposicionTextilUsd += (montoAbono * (config.pctTextil / 100.0))
                    fondoReposicionPasajesUsd += (montoAbono * (config.pctPasaje / 100.0))
                    fondoReposicionInsumosUsd += (montoAbono * (config.pctInsumos / 100.0))
                }
            } else {
                fondoReposicionTextilUsd += (montoAbono * (config.pctTextil / 100.0))
                fondoReposicionPasajesUsd += (montoAbono * (config.pctPasaje / 100.0))
                fondoReposicionInsumosUsd += (montoAbono * (config.pctInsumos / 100.0))
            }
        }

        val fondoReposicionTotalUsd = (fondoReposicionTextilUsd + fondoReposicionPasajesUsd + fondoReposicionInsumosUsd).redondear2()
        val gananciaNetaUsd = (ingresosCobradosUsd - fondoReposicionTotalUsd).coerceAtLeast(0.0).redondear2()

        // 5. Construcción del Carrusel de Sobres / Fondos de Insumos
        val totalFondos = if (ingresosCobradosUsd > 0) ingresosCobradosUsd else 1.0

        val fondoTextil = fondoReposicionTextilUsd.redondear2()
        val fondoPasajes = fondoReposicionPasajesUsd.redondear2()
        val fondoTintasPapel = fondoReposicionInsumosUsd.redondear2()
        val fondoGanancia = gananciaNetaUsd

        val fondosInsumos = listOf(
            FondoInsumoItem(
                id = "textil",
                nombre = "Material Base (Sublimación, Rígidos, etc.)",
                montoUsd = fondoTextil,
                porcentajeDelTotal = (fondoTextil / totalFondos * 100).toFloat(),
                icono = "📦",
                colorHex = "#2196F3"
            ),
            FondoInsumoItem(
                id = "pasaje",
                nombre = "Pasajes & Transporte Logístico",
                montoUsd = fondoPasajes,
                porcentajeDelTotal = (fondoPasajes / totalFondos * 100).toFloat(),
                icono = "🚚",
                colorHex = "#4CAF50"
            ),
            FondoInsumoItem(
                id = "tintas",
                nombre = "Tintas, Papel & Insumos",
                montoUsd = fondoTintasPapel,
                porcentajeDelTotal = (fondoTintasPapel / totalFondos * 100).toFloat(),
                icono = "🖨️",
                colorHex = "#9C27B0"
            ),
            FondoInsumoItem(
                id = "ganancia",
                nombre = "Ganancia Neta Libre",
                montoUsd = fondoGanancia,
                porcentajeDelTotal = (fondoGanancia / totalFondos * 100).toFloat(),
                icono = "💎",
                colorHex = "#E91E63"
            )
        )

        // 6. Radiografía de Abonos Recibidos (Opción 0: CONSOLIDADO TOTAL DE TODOS LOS CLIENTES)
        val listaAbonosDesglose = mutableListOf<DesglosePagoIndividual>()

        // Opción 0: Consolidado de todos los abonos del período
        if (ingresosCobradosUsd > 0) {
            
            // Creamos los sub-ítems para el consolidado estimando sobre el total de insumos
            val subItemsConsolidado = mutableListOf<DesglosePagoItem>()
            if (fondoTintasPapel > 0) {
                subItemsConsolidado.add(DesglosePagoItem("Papel, Tintas y Empaque", (fondoTintasPapel * 0.40).redondear2(), 40f))
                subItemsConsolidado.add(DesglosePagoItem("Impresión (DTF)", (fondoTintasPapel * 0.30).redondear2(), 30f))
                subItemsConsolidado.add(DesglosePagoItem("Diseño & Extras", (fondoTintasPapel * 0.15).redondear2(), 15f))
                subItemsConsolidado.add(DesglosePagoItem("Operatividad (Luz)", (fondoTintasPapel * 0.15).redondear2(), 15f))
            }

            listaAbonosDesglose.add(
                DesglosePagoIndividual(
                    idDeuda = -1,
                    clienteNombre = "📊 TODOS LOS CLIENTES (${abonosEnPeriodo.size} pagos)",
                    montoTotalAbonoUsd = ingresosCobradosUsd,
                    fechaStr = "Consolidado Período",
                    desgloseItems = listOf(
                        DesglosePagoItem("Material Base (Sublimación/Rígidos)", fondoTextil, (fondoTextil / totalFondos * 100).toFloat()),
                        DesglosePagoItem("Pasajes & Transporte", fondoPasajes, (fondoPasajes / totalFondos * 100).toFloat()),
                        DesglosePagoItem("Costos Adicionales de Producción", fondoTintasPapel, (fondoTintasPapel / totalFondos * 100).toFloat(), subItemsConsolidado),
                        DesglosePagoItem("Ganancia Neta Libre", fondoGanancia, (fondoGanancia / totalFondos * 100).toFloat())
                    )
                )
            )
        }

        // Siguientes Opciones: Abonos individuales por cliente
        val abonosOrdenados = abonosEnPeriodo.sortedByDescending { obtenerFechaMillisValida(it) }.take(15)
        for (abono in abonosOrdenados) {
            val cliente = deudores.find { it.id == abono.idDeudor }
            val clienteNombre = if (cliente != null) "${cliente.nombre} ${cliente.apellido}" else "Cliente"
            val montoAbono = Math.abs(abono.montoRestante).redondear2()

            val cargoOriginal = cargosActivos.find { it.idDeudor == abono.idDeudor && it.tipoDeuda != "Abono / Pago Parcial" }
            val pedidoFotografia = pedidos.find { it.deudorId == abono.idDeudor && it.totalUsd == cargoOriginal?.montoInicial }

            val pTextil: Double
            val pPasaje: Double
            val pInsumo: Double
            val pGanancia: Double
            var nombreInsumoReal = "Insumo Base / Textil"
            val subItemsExtras = mutableListOf<DesglosePagoItem>()

            if (pedidoFotografia != null && (pedidoFotografia.costoPiezaBaseUsd > 0 || pedidoFotografia.costoInsumosUsd > 0)) {
                val proporcion = if (pedidoFotografia.totalUsd > 0) montoAbono / pedidoFotografia.totalUsd else 1.0
                
                // Si la plantilla base existe hoy, desglosamos visualmente
                val plantillaOrigen = plantillas.find { it.nombrePlantilla == pedidoFotografia.nombrePiezaBase }
                
                if (plantillaOrigen != null) {
                    val mCosto = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(plantillaOrigen)
                    val escala = (if(pedidoFotografia.cantidad>0) pedidoFotografia.cantidad else 1) * proporcion
                    
                    val pasajeV = (mCosto.costoPasajeUsd * escala).redondear2()
                    val insumoV = (mCosto.costoInsumosUsd * escala).redondear2()
                    
                    pPasaje = pasajeV
                    pInsumo = insumoV
                    
                    // Llenamos los subItems con una estimación lógica de la plantilla actual
                    if (insumoV > 0) {
                        subItemsExtras.add(DesglosePagoItem("Papel, Tintas y Empaque", (insumoV * 0.40).redondear2(), 40f))
                        subItemsExtras.add(DesglosePagoItem("Impresión (DTF)", (insumoV * 0.30).redondear2(), 30f))
                        subItemsExtras.add(DesglosePagoItem("Diseño & Extras", (insumoV * 0.15).redondear2(), 15f))
                        subItemsExtras.add(DesglosePagoItem("Operatividad (Luz)", (insumoV * 0.15).redondear2(), 15f))
                    }
                } else {
                    pPasaje = 0.0
                    pInsumo = 0.0
                }

                pTextil = (pedidoFotografia.costoPiezaBaseUsd * proporcion).redondear2()
                pGanancia = (montoAbono - (pTextil + pPasaje + pInsumo)).coerceAtLeast(0.0).redondear2()
                nombreInsumoReal = "Pieza Base: ${pedidoFotografia.nombrePiezaBase}"
            } else {
                pTextil = (montoAbono * (config.pctTextil / 100.0)).redondear2()
                pPasaje = (montoAbono * (config.pctPasaje / 100.0)).redondear2()
                pInsumo = (montoAbono * (config.pctInsumos / 100.0)).redondear2()
                pGanancia = (montoAbono - (pTextil + pPasaje + pInsumo)).coerceAtLeast(0.0).redondear2()
            }

            listaAbonosDesglose.add(
                DesglosePagoIndividual(
                    idDeuda = abono.id,
                    clienteNombre = clienteNombre,
                    montoTotalAbonoUsd = montoAbono,
                    fechaStr = abono.fecha,
                    desgloseItems = listOf(
                        DesglosePagoItem(nombreInsumoReal, pTextil, (pTextil / montoAbono * 100).toFloat()),
                        DesglosePagoItem("Pasajes & Transporte", pPasaje, (pPasaje / montoAbono * 100).toFloat()),
                        DesglosePagoItem("Costos Adicionales de Producción", pInsumo, (pInsumo / montoAbono * 100).toFloat(), subItemsExtras),
                        DesglosePagoItem("Ganancia Neta Libre", pGanancia, (pGanancia / montoAbono * 100).toFloat())
                    )
                )
            )
        }

        // 7. Estadísticas de Estado de Pedidos
        val pedidosEnPeriodo = pedidos.filter { it.fechaCreacionMillis in inicioMillis..finMillis }
        val recCount = pedidosEnPeriodo.count { it.estado.uppercase() == "RECIBIDO" }
        val prodCount = pedidosEnPeriodo.count { it.estado.uppercase() == "EN_PRODUCCION" }
        val lisCount = pedidosEnPeriodo.count { it.estado.uppercase() == "LISTO" }
        val entCount = pedidosEnPeriodo.count { it.estado.uppercase() == "ENTREGADO" }

        // Top Productos
        val topProds = pedidosEnPeriodo.groupBy { it.producto }
            .mapValues { entry -> entry.value.sumOf { it.cantidad } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        // Análisis de Clientes (Auditoría para el Asesor y el PDF)
        // Agrupamos la deuda actual de cada cliente en la calle
        val deudaPorCliente = cargosActivos.groupBy { it.idDeudor }
            .mapValues { entry -> entry.value.sumOf { it.montoInicial } - deudas.filter { it.idDeudor == entry.key && it.tipo == "ABONO" }.sumOf { Math.abs(it.montoRestante) } }
            .filterValues { it > 0.0 }
            
        val clienteMasDeudorEntry = deudaPorCliente.maxByOrNull { it.value }
        val clienteMasDeudor = deudores.find { it.id == clienteMasDeudorEntry?.key }
        
        // Agrupamos el pago (abonos) recibido por cliente en este período
        val pagosPorCliente = abonosEnPeriodo.groupBy { it.idDeudor }
            .mapValues { entry -> entry.value.sumOf { Math.abs(it.montoRestante) } }
            
        val clienteVipEntry = pagosPorCliente.maxByOrNull { it.value }
        val clienteVip = deudores.find { it.id == clienteVipEntry?.key }

        // Cálculo de Métricas del Mes Anterior (para Comparativa Intermensual)
        val (inicioMesAntMillis, finMesAntMillis) = calcularRangoMillis(PeriodoFiltro.MES_ANTERIOR)
        val abonosMesAnt = deudas.filter { 
            it.tipo == "ABONO" && obtenerFechaMillisValida(it) in inicioMesAntMillis..finMesAntMillis 
        }
        val ingresosMesAntUsd = abonosMesAnt.sumOf { Math.abs(it.montoRestante) }
        val pctGananciaActual = if (ingresosCobradosUsd > 0) (gananciaNetaUsd / ingresosCobradosUsd) else 0.40
        val gananciaMesAntUsd = ingresosMesAntUsd * pctGananciaActual
        val pasajesMesAntUsd = ingresosMesAntUsd * (config.pctPasaje / 100.0)
        val insumosMesAntUsd = ingresosMesAntUsd * ((config.pctTextil + config.pctInsumos) / 100.0)

        // 8. Alertas de Salud Financiera (Asesor Zubli Inteligente - Números Puros)
        val alertas = com.example.gestor_deudores.data.utils.AsesorZubliUtils.generarConsejos(
            ingresosCobradosUsd = ingresosCobradosUsd,
            gananciaNetaUsd = gananciaNetaUsd,
            porCobrarUsd = porCobrarUsd,
            costoPasajesUsd = fondoPasajes,
            costoInsumosUsd = fondoTintasPapel,
            topProductos = topProds,
            clienteMasDeudorNombre = clienteMasDeudor?.let { "${it.nombre} ${it.apellido}" },
            clienteMasDeudorMonto = clienteMasDeudorEntry?.value ?: 0.0,
            clienteVipNombre = clienteVip?.let { "${it.nombre} ${it.apellido}" },
            clienteVipMonto = clienteVipEntry?.value ?: 0.0,
            gananciaNetaMesAnterior = gananciaMesAntUsd,
            ingresosCobradosMesAnterior = ingresosMesAntUsd,
            costoPasajesMesAnterior = pasajesMesAntUsd,
            costoInsumosMesAnterior = insumosMesAntUsd
        )

        ResumenUiState(
            cargando = false,
            periodoSeleccionado = periodo,
            monedaVista = moneda,
            tasaBcv = tasa,
            ingresosCobradosUsd = ingresosCobradosUsd,
            fondoReposicionUsd = fondoReposicionTotalUsd,
            gananciaNetaUsd = gananciaNetaUsd,
            porCobrarUsd = porCobrarUsd,
            fondosInsumos = fondosInsumos,
            cuentasBancarias = cuentas,
            listaAbonosDesglose = listaAbonosDesglose,
            abonoSeleccionadoIndex = abonoIdx.coerceIn(0, (listaAbonosDesglose.size - 1).coerceAtLeast(0)),
            configuracionCostos = config,
            pedidosRecibidosCount = recCount,
            pedidosProduccionCount = prodCount,
            pedidosListosCount = lisCount,
            pedidosEntregadosCount = entCount,
            alertasFinancieras = alertas,
            topProductos = topProds,
            ingresosCobradosMesAnterior = ingresosMesAntUsd,
            gananciaNetaMesAnterior = gananciaMesAntUsd,
            costoPasajesUsd = fondoPasajes,
            costoInsumosUsd = fondoTintasPapel,
            costoPasajesMesAnterior = pasajesMesAntUsd,
            costoInsumosMesAnterior = insumosMesAntUsd
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResumenUiState(cargando = true)
    )

    private fun calcularRangoMillis(periodo: PeriodoFiltro): Pair<Long, Long> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val finMillis = System.currentTimeMillis()

        return when (periodo) {
            PeriodoFiltro.ESTA_SEMANA -> {
                // Ir al lunes de esta semana
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, finMillis)
            }
            PeriodoFiltro.ESTE_MES -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, finMillis)
            }
            PeriodoFiltro.MES_ANTERIOR -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val inicioAnterior = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                Pair(inicioAnterior, cal.timeInMillis)
            }
            PeriodoFiltro.ULTIMOS_3_MESES -> {
                cal.add(Calendar.MONTH, -3)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, finMillis)
            }
            PeriodoFiltro.TODO_EL_HISTORICO -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
