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
    val porcentaje: Float
)

data class DesglosePagoIndividual(
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
    val ultimoAbonoDesglose: DesglosePagoIndividual? = null,
    val pedidosRecibidosCount: Int = 0,
    val pedidosProduccionCount: Int = 0,
    val pedidosListosCount: Int = 0,
    val pedidosEntregadosCount: Int = 0,
    val alertasFinancieras: List<AlertaFinanciera> = emptyList(),
    val topProductos: List<Pair<String, Int>> = emptyList()
)

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
    val tasa: Double
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

    fun actualizarTasaBcv(tasa: Double) {
        _tasaBcv.value = tasa
    }

    fun cambiarPeriodo(nuevoPeriodo: PeriodoFiltro) {
        _periodoSeleccionado.value = nuevoPeriodo
    }

    fun cambiarMonedaVista(nuevaMoneda: String) {
        _monedaVista.value = nuevaMoneda
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

    fun actualizarCuentaBancaria(cuenta: CuentaBancaria) {
        viewModelScope.launch {
            cuentaBancariaDao.actualizarCuenta(cuenta)
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
        _tasaBcv
    ) { periodo, moneda, tasa ->
        FiltrosUiCombinados(periodo, moneda, tasa)
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

        // 4. Estimación de Fondo de Reposición vs Ganancia Neta
        val costoPromedioPorcentaje = if (plantillas.isNotEmpty()) {
            val promedioOp = plantillas.map { it.porcentajeOperativo.toDouble() }.average().toFloat()
            if (promedioOp > 0) (promedioOp / 100f).coerceIn(0.2f, 0.7f) else 0.45f
        } else {
            0.45f // 45% costo estimado por defecto en sublimación
        }

        val fondoReposicionUsd = (ingresosCobradosUsd * costoPromedioPorcentaje).redondear2()
        val gananciaNetaUsd = (ingresosCobradosUsd - fondoReposicionUsd).coerceAtLeast(0.0).redondear2()

        // 5. Construcción del Carrusel de Sobres / Fondos de Insumos
        val totalFondos = if (ingresosCobradosUsd > 0) ingresosCobradosUsd else 1.0

        val fondoTextil = (ingresosCobradosUsd * 0.20).redondear2()
        val fondoCeramica = (ingresosCobradosUsd * 0.15).redondear2()
        val fondoTintasPapel = (ingresosCobradosUsd * 0.10).redondear2()
        val fondoPasajes = (ingresosCobradosUsd * 0.05).redondear2()
        val fondoGanancia = gananciaNetaUsd

        val fondosInsumos = listOf(
            FondoInsumoItem(
                id = "textil",
                nombre = "Insumos Textiles (Franelas / Gorras)",
                montoUsd = fondoTextil,
                porcentajeDelTotal = (fondoTextil / totalFondos * 100).toFloat(),
                icono = "👕",
                colorHex = "#2196F3"
            ),
            FondoInsumoItem(
                id = "ceramica",
                nombre = "Cerámica & Rígidos (Tazas / Mugs)",
                montoUsd = fondoCeramica,
                porcentajeDelTotal = (fondoCeramica / totalFondos * 100).toFloat(),
                icono = "☕",
                colorHex = "#FF9800"
            ),
            FondoInsumoItem(
                id = "tintas",
                nombre = "Tintas, Papel & Prensa",
                montoUsd = fondoTintasPapel,
                porcentajeDelTotal = (fondoTintasPapel / totalFondos * 100).toFloat(),
                icono = "🖨️",
                colorHex = "#9C27B0"
            ),
            FondoInsumoItem(
                id = "pasaje",
                nombre = "Pasajes, Fletes & Envíos",
                montoUsd = fondoPasajes,
                porcentajeDelTotal = (fondoPasajes / totalFondos * 100).toFloat(),
                icono = "🚚",
                colorHex = "#4CAF50"
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

        // 6. Radiografía del Último Pago / Abono Individual
        val ultimoAbono = deudas.filter { it.tipo == "ABONO" }.maxByOrNull { obtenerFechaMillisValida(it) }
        val ultimoAbonoDesglose = if (ultimoAbono != null) {
            val cliente = deudores.find { it.id == ultimoAbono.idDeudor }
            val clienteNombre = if (cliente != null) "${cliente.nombre} ${cliente.apellido}" else "Cliente"
            val montoAbono = Math.abs(ultimoAbono.montoRestante).redondear2()

            val textilAbono = (montoAbono * 0.20).redondear2()
            val ceramicaAbono = (montoAbono * 0.15).redondear2()
            val tintasAbono = (montoAbono * 0.10).redondear2()
            val pasajesAbono = (montoAbono * 0.05).redondear2()
            val gananciaAbono = (montoAbono - (textilAbono + ceramicaAbono + tintasAbono + pasajesAbono)).coerceAtLeast(0.0).redondear2()

            DesglosePagoIndividual(
                clienteNombre = clienteNombre,
                montoTotalAbonoUsd = montoAbono,
                fechaStr = ultimoAbono.fecha,
                desgloseItems = listOf(
                    DesglosePagoItem("Insumo Textil", textilAbono, 20f),
                    DesglosePagoItem("Cerámica/Tazas", ceramicaAbono, 15f),
                    DesglosePagoItem("Tintas & Papel", tintasAbono, 10f),
                    DesglosePagoItem("Pasaje / Logística", pasajesAbono, 5f),
                    DesglosePagoItem("Ganancia Neta", gananciaAbono, 50f)
                )
            )
        } else null

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

        // 8. Alertas de Salud Financiera
        val alertas = mutableListOf<AlertaFinanciera>()

        if (porCobrarUsd > (ingresosCobradosUsd * 0.5) && porCobrarUsd > 20.0) {
            alertas.add(
                AlertaFinanciera(
                    titulo = "Alto saldo en la calle",
                    mensaje = "Tienes $$porCobrarUsd USD por cobrar. Usa el recordatorio de WhatsApp para agilizar los pagos.",
                    nivel = "WARNING"
                )
            )
        }

        if (ingresosCobradosUsd > 0.0 && gananciaNetaUsd / ingresosCobradosUsd < 0.25) {
            alertas.add(
                AlertaFinanciera(
                    titulo = "Margen de ganancia bajo",
                    mensaje = "Tu ganancia neta estimada es inferior al 25%. Revisa los costos de pasaje e insumos en el Cotizador.",
                    nivel = "DANGER"
                )
            )
        } else if (ingresosCobradosUsd > 0.0) {
            alertas.add(
                AlertaFinanciera(
                    titulo = "Salud Financiera Estable",
                    mensaje = "Tu margen de ganancia está saludable. Mantén la separación de fondos al recibir cada abono.",
                    nivel = "INFO"
                )
            )
        }

        ResumenUiState(
            cargando = false,
            periodoSeleccionado = periodo,
            monedaVista = moneda,
            tasaBcv = tasa,
            ingresosCobradosUsd = ingresosCobradosUsd,
            fondoReposicionUsd = fondoReposicionUsd,
            gananciaNetaUsd = gananciaNetaUsd,
            porCobrarUsd = porCobrarUsd,
            fondosInsumos = fondosInsumos,
            cuentasBancarias = cuentas,
            ultimoAbonoDesglose = ultimoAbonoDesglose,
            pedidosRecibidosCount = recCount,
            pedidosProduccionCount = prodCount,
            pedidosListosCount = lisCount,
            pedidosEntregadosCount = entCount,
            alertasFinancieras = alertas,
            topProductos = topProds
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
