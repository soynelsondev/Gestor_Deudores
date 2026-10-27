package com.example.gestor_deudores.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.database.Deuda
import com.example.gestor_deudores.data.database.DeudaDao
import com.example.gestor_deudores.data.database.Deudor
import com.example.gestor_deudores.data.database.DeudorDao
import com.example.gestor_deudores.data.database.Pedido
import com.example.gestor_deudores.data.database.PedidoConCliente
import com.example.gestor_deudores.data.database.PedidoDao
import com.example.gestor_deudores.data.database.PlantillaCotizacion
import com.example.gestor_deudores.data.database.PlantillaDao
import com.example.gestor_deudores.data.database.DistribucionAbono
import com.example.gestor_deudores.data.database.DistribucionAbonoDao
import com.example.gestor_deudores.data.utils.obtenerFechaMillisValida
import com.example.gestor_deudores.data.utils.redondear2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FiltroEstadoPedido {
    TODOS,
    HOY_SEMANA,
    PENDIENTE_SALDO,
    RECIBIDO,
    EN_PRODUCCION,
    LISTO,
    ENTREGADO,
    CANCELADO
}

data class PedidoBorradorState(
    val deudorId: Int? = null,
    val clienteNombre: String = "",
    val busquedaCliente: String = "",
    val abonoInicialTexto: String = "",
    val frecuenciaSeleccionada: String = "AL_ENTREGAR",
    val numCuotasTexto: String = "1",
    val fechaEntregaMillis: Long? = null,
    val notas: String = "",
    val descuentoTexto: String = "",
    val esDescuentoPorcentaje: Boolean = false,
    val cargoDisenoTexto: String = "",
    val cargoEnvioTexto: String = "",
    val tipoEntrega: String = "RETIRO",
    val direccionEntrega: String = "",
    val esUrgente: Boolean = false,
    val listaArticulosJson: String = ""
)

class PedidoViewModel(
    private val pedidoDao: PedidoDao,
    private val deudorDao: DeudorDao,
    private val deudaDao: DeudaDao,
    private val plantillaDao: PlantillaDao,
    private val distribucionAbonoDao: DistribucionAbonoDao
) : ViewModel() {

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda.asStateFlow()

    private val _filtroEstado = MutableStateFlow(FiltroEstadoPedido.TODOS)
    val filtroEstado: StateFlow<FiltroEstadoPedido> = _filtroEstado.asStateFlow()

    private val _borradorActual = MutableStateFlow<PedidoBorradorState?>(null)
    val borradorActual: StateFlow<PedidoBorradorState?> = _borradorActual.asStateFlow()

    fun guardarBorrador(contexto: android.content.Context, borrador: PedidoBorradorState) {
        _borradorActual.value = borrador
        try {
            val prefs = contexto.getSharedPreferences("BorradoresZubli", android.content.Context.MODE_PRIVATE)
            val json = com.google.gson.Gson().toJson(borrador)
            prefs.edit().putString("ultimo_borrador_pedido", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cargarBorradorPersistido(contexto: android.content.Context) {
        try {
            val prefs = contexto.getSharedPreferences("BorradoresZubli", android.content.Context.MODE_PRIVATE)
            val json = prefs.getString("ultimo_borrador_pedido", null)
            if (!json.isNullOrBlank()) {
                val borrador = com.google.gson.Gson().fromJson(json, PedidoBorradorState::class.java)
                _borradorActual.value = borrador
            }
        } catch (e: Exception) {
            _borradorActual.value = null
        }
    }

    fun descartarBorrador(contexto: android.content.Context? = null) {
        _borradorActual.value = null
        if (contexto != null) {
            try {
                val prefs = contexto.getSharedPreferences("BorradoresZubli", android.content.Context.MODE_PRIVATE)
                prefs.edit().remove("ultimo_borrador_pedido").apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun obtenerImagenesPorPedido(pedidoId: Int): Flow<List<com.example.gestor_deudores.data.database.PedidoImagen>> {
        return pedidoDao.obtenerImagenesPorPedido(pedidoId)
    }

    fun cambiarTipoImagen(imagenId: Int, nuevoTipo: String) {
        viewModelScope.launch {
            pedidoDao.actualizarTipoImagen(imagenId, nuevoTipo)
        }
    }

    fun eliminarImagen(imagen: com.example.gestor_deudores.data.database.PedidoImagen) {
        viewModelScope.launch {
            pedidoDao.eliminarImagenPedido(imagen)
        }
    }

    fun guardarNuevaFotoPedido(context: android.content.Context, pedidoId: Int, uriStr: String, tipoImagen: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val resultadoImg = com.example.gestor_deudores.data.utils.ImagenUtils.procesarYGuardarImagenPedido(
                context = context,
                pedidoId = pedidoId,
                uriRawString = uriStr
            )
            if (resultadoImg != null) {
                val imagenRecord = com.example.gestor_deudores.data.database.PedidoImagen(
                    pedidoId = pedidoId,
                    ruta = resultadoImg.rutaOriginalComprimida,
                    rutaMiniatura = resultadoImg.rutaMiniatura,
                    tipo = tipoImagen
                )
                pedidoDao.agregarImagenPedido(imagenRecord)
            }
        }
    }

    // Lista de clientes disponibles para el selector del formulario
    val listaClientes: StateFlow<List<Deudor>> = deudorDao.obtenerDeudores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Todas las plantillas del cotizador disponibles
    val listaPlantillas: StateFlow<List<PlantillaCotizacion>> = plantillaDao.obtenerTodasLasPlantillas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sugerenciasProductos: StateFlow<List<String>> = deudaDao.obtenerTiposDeudaUnicos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun obtenerDeudasPorDeudor(deudorId: Int): Flow<List<Deuda>> {
        return deudaDao.obtenerDeudasPorDeudor(deudorId)
    }

    suspend fun obtenerAtributosConOpciones(plantillaId: Int): List<com.example.gestor_deudores.data.database.PlantillaAtributoConOpciones> {
        val atributos = plantillaDao.obtenerAtributosPorPlantillaSync(plantillaId)
        return atributos.map { atr ->
            val opciones = plantillaDao.obtenerOpcionesPorAtributoSync(atr.id)
            com.example.gestor_deudores.data.database.PlantillaAtributoConOpciones(atr, opciones)
        }
    }

    val pedidosPorEntregarHoy: StateFlow<Int> = pedidoDao.obtenerPedidosConCliente()
        .map { lista ->
            val calHoy = java.util.Calendar.getInstance()
            lista.count { item ->
                if (item.estado == "ENTREGADO" || item.estado == "CANCELADO" || item.fechaEntregaMillis <= 0) false
                else {
                    val calEnt = java.util.Calendar.getInstance().apply { timeInMillis = item.fechaEntregaMillis }
                    calHoy.get(java.util.Calendar.YEAR) == calEnt.get(java.util.Calendar.YEAR) &&
                    calHoy.get(java.util.Calendar.DAY_OF_YEAR) == calEnt.get(java.util.Calendar.DAY_OF_YEAR)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val listaPedidos: StateFlow<List<PedidoConCliente>> = combine(
        pedidoDao.obtenerPedidosConCliente(),
        _textoBusqueda,
        _filtroEstado
    ) { pedidos, texto, estadoFiltro ->
        val hoyMillis = System.currentTimeMillis()

        pedidos.filter { item ->
            val coincideEstado = when (estadoFiltro) {
                FiltroEstadoPedido.TODOS -> item.estado != "CANCELADO" && !item.esBorrador
                FiltroEstadoPedido.HOY_SEMANA -> {
                    val calHoy = java.util.Calendar.getInstance()
                    val calEnt = java.util.Calendar.getInstance().apply { timeInMillis = item.fechaEntregaMillis }
                    item.estado != "CANCELADO" && !item.esBorrador &&
                    calHoy.get(java.util.Calendar.YEAR) == calEnt.get(java.util.Calendar.YEAR) &&
                    calHoy.get(java.util.Calendar.WEEK_OF_YEAR) == calEnt.get(java.util.Calendar.WEEK_OF_YEAR)
                }
                FiltroEstadoPedido.PENDIENTE_SALDO -> item.estado != "ENTREGADO" && item.estado != "CANCELADO" && !item.esBorrador
                FiltroEstadoPedido.RECIBIDO -> item.estado == "RECIBIDO" && !item.esBorrador
                FiltroEstadoPedido.EN_PRODUCCION -> item.estado == "EN_PRODUCCION" && !item.esBorrador
                FiltroEstadoPedido.LISTO -> item.estado == "LISTO" && !item.esBorrador
                FiltroEstadoPedido.ENTREGADO -> item.estado == "ENTREGADO" && !item.esBorrador
                FiltroEstadoPedido.CANCELADO -> item.estado == "CANCELADO"
            }

            val coincideTexto = if (texto.isBlank()) true else {
                item.producto.contains(texto, ignoreCase = true) ||
                item.clienteNombre.contains(texto, ignoreCase = true) ||
                item.clienteApellido.contains(texto, ignoreCase = true)
            }

            coincideEstado && coincideTexto
        }.sortedWith(
            compareBy<PedidoConCliente> {
                val esVencido = it.estado != "ENTREGADO" && it.estado != "CANCELADO" && it.fechaEntregaMillis > 0 && it.fechaEntregaMillis < hoyMillis
                if (esVencido) 0 else 1
            }.thenBy {
                if (it.fechaEntregaMillis > 0) it.fechaEntregaMillis else Long.MAX_VALUE
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun eliminarPedidoDefinitivamente(pedidoConCliente: PedidoConCliente, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            val pedidoObj = pedidoDao.obtenerPedidoPorId(pedidoConCliente.id)
            if (pedidoObj != null) {
                val deudasCliente = deudaDao.obtenerDeudasPorDeudorSync(pedidoConCliente.deudorId)
                val cargoAsociado = deudasCliente.find {
                    it.tipo == "CARGO" && (it.descripcion.contains("Pedido #${pedidoConCliente.id}") || it.tipoDeuda.contains("Pedido #${pedidoConCliente.id}"))
                }
                if (cargoAsociado != null) {
                    deudaDao.eliminarDeuda(cargoAsociado)
                }
                pedidoDao.eliminarPedido(pedidoObj)
            }
            onExito()
        }
    }

    fun cambiarEstadoDiseno(pedidoId: Int, nuevoEstadoDiseno: String) {
        viewModelScope.launch {
            pedidoDao.actualizarEstadoDiseno(pedidoId, nuevoEstadoDiseno)
        }
    }

    fun cancelarPedidoConDecisionAbono(
        pedidoConCliente: PedidoConCliente,
        devolverAbono: Boolean,
        notaCancelacion: String,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            val pedidoEditado = Pedido(
                id = pedidoConCliente.id,
                deudorId = pedidoConCliente.deudorId,
                producto = pedidoConCliente.producto,
                cantidad = pedidoConCliente.cantidad,
                precioUnitarioUsd = pedidoConCliente.precioUnitarioUsd,
                totalUsd = pedidoConCliente.totalUsd,
                fechaCreacionMillis = pedidoConCliente.fechaCreacionMillis,
                fechaEntregaMillis = pedidoConCliente.fechaEntregaMillis,
                estado = "CANCELADO",
                notas = pedidoConCliente.notas + "\n--- CANCELADO: $notaCancelacion",
                costoPiezaBaseUsd = pedidoConCliente.costoPiezaBaseUsd,
                costoPasajeUsd = pedidoConCliente.costoPasajeUsd,
                costoInsumosUsd = pedidoConCliente.costoInsumosUsd,
                nombrePiezaBase = pedidoConCliente.nombrePiezaBase,
                costoEnvio = pedidoConCliente.costoEnvio,
                descuento = pedidoConCliente.descuento,
                costoDiseno = pedidoConCliente.costoDiseno,
                tipoEntrega = pedidoConCliente.tipoEntrega,
                direccionEntrega = pedidoConCliente.direccionEntrega,
                urgente = pedidoConCliente.urgente,
                estadoDiseno = pedidoConCliente.estadoDiseno,
                esBorrador = pedidoConCliente.esBorrador
            )
            pedidoDao.actualizarPedido(pedidoEditado)

            // Cancelamos también el CARGO en Tabla_Deuda para que la deuda desaparezca de 'Mis Cobros'
            val deudasCliente = deudaDao.obtenerDeudasPorDeudorSync(pedidoConCliente.deudorId)
            val cargoAsociado = deudasCliente.find {
                it.tipo == "CARGO" && (it.descripcion.contains("Pedido #${pedidoConCliente.id}") || it.tipoDeuda.contains("Pedido #${pedidoConCliente.id}") || (it.montoInicial == pedidoConCliente.totalUsd && it.fechaMillis == pedidoConCliente.fechaCreacionMillis))
            }
            if (cargoAsociado != null) {
                deudaDao.actualizarDeuda(cargoAsociado.copy(estado = "Cancelado"))
            }

            if (devolverAbono) {
                val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val fechaStr = formatoFecha.format(java.util.Date())
                val reembolsoDeuda = Deuda(
                    idDeudor = pedidoConCliente.deudorId,
                    montoInicial = 0.0,
                    montoRestante = 0.0,
                    tipoDeuda = "Devolución Abono",
                    fecha = fechaStr,
                    fechaMillis = System.currentTimeMillis(),
                    tipo = "ABONO",
                    numCuotas = 1,
                    frecuencia = "MENSUAL",
                    rol = "PAGO",
                    descripcion = "Devolución de abono por cancelación de Pedido #${pedidoConCliente.id}",
                    estado = "Cancelado"
                )
                deudaDao.agregarDeuda(reembolsoDeuda)
            }

            onExito()
        }
    }

    fun onTextoBusquedaChange(nuevoTexto: String) {
        _textoBusqueda.value = nuevoTexto
    }

    fun onFiltroEstadoChange(nuevoEstado: FiltroEstadoPedido) {
        _filtroEstado.value = nuevoEstado
    }

    fun cambiarEstadoPedido(pedidoConCliente: PedidoConCliente, nuevoEstado: String) {
        viewModelScope.launch {
            val pedidoEditado = Pedido(
                id = pedidoConCliente.id,
                deudorId = pedidoConCliente.deudorId,
                producto = pedidoConCliente.producto,
                cantidad = pedidoConCliente.cantidad,
                precioUnitarioUsd = pedidoConCliente.precioUnitarioUsd,
                totalUsd = pedidoConCliente.totalUsd,
                fechaCreacionMillis = pedidoConCliente.fechaCreacionMillis,
                fechaEntregaMillis = pedidoConCliente.fechaEntregaMillis,
                estado = nuevoEstado,
                notas = pedidoConCliente.notas,
                costoPiezaBaseUsd = pedidoConCliente.costoPiezaBaseUsd,
                costoPasajeUsd = pedidoConCliente.costoPasajeUsd,
                costoInsumosUsd = pedidoConCliente.costoInsumosUsd,
                nombrePiezaBase = pedidoConCliente.nombrePiezaBase
            )
            pedidoDao.actualizarPedido(pedidoEditado)
        }
    }

    // --- GUARDADO TRANSACCIONAL DEL PEDIDO COMPLETO (SOLUCIÓN ELEGANTE) ---
    fun crearNuevoPedido(
        context: android.content.Context,
        deudorId: Int,
        producto: String,
        cantidad: Int,
        precioUnitarioUsd: Double,
        abonoInicialUsd: Double,
        numCuotas: Int,
        frecuencia: String,
        fechaEntregaMillis: Long,
        notas: String,
        costoPiezaBaseUnitario: Double = 0.0,
        nombrePiezaBase: String = "Insumo Base",
        costoPasajeUnitario: Double = 0.0,
        costoInsumosUnitario: Double = 0.0,
        descuento: Double = 0.0,
        costoDiseno: Double = 0.0,
        costoEnvio: Double = 0.0,
        totalCalculadoUsd: Double? = null,
        listaArticulos: List<ArticuloPedido> = emptyList(),
        onExito: () -> Unit
    ) {
        val totalUsd = totalCalculadoUsd ?: (cantidad * precioUnitarioUsd)
        
        // Fotografía de costos: Multiplicamos el costo unitario por la cantidad pedida
        val costoPiezaBaseTotal = costoPiezaBaseUnitario * cantidad
        val costoPasajeTotal = costoPasajeUnitario * cantidad
        val costoInsumosTotal = costoInsumosUnitario * cantidad
        
        val nuevoPedido = Pedido(
            deudorId = deudorId,
            producto = producto,
            cantidad = cantidad,
            precioUnitarioUsd = precioUnitarioUsd,
            totalUsd = totalUsd,
            fechaCreacionMillis = System.currentTimeMillis(),
            fechaEntregaMillis = fechaEntregaMillis,
            estado = "RECIBIDO",
            notas = notas,
            costoPiezaBaseUsd = costoPiezaBaseTotal,
            costoPasajeUsd = costoPasajeTotal,
            costoInsumosUsd = costoInsumosTotal,
            nombrePiezaBase = nombrePiezaBase,
            descuento = descuento,
            costoDiseno = costoDiseno,
            costoEnvio = costoEnvio
        )

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val pedidoId = pedidoDao.guardarPedidoCompleto(
                pedido = nuevoPedido,
                deudaDao = deudaDao,
                distribucionAbonoDao = distribucionAbonoDao,
                abonoInicial = abonoInicialUsd,
                numCuotas = numCuotas,
                frecuencia = frecuencia
            )

            // PROCESAMIENTO Y COMPRESIÓN DE FOTOS DE DISEÑO (SECCIÓN 6.2)
            listaArticulos.forEach { art ->
                art.fotosUris.forEach { uriStr ->
                    val resultadoImg = com.example.gestor_deudores.data.utils.ImagenUtils.procesarYGuardarImagenPedido(
                        context = context,
                        pedidoId = pedidoId.toInt(),
                        uriRawString = uriStr
                    )
                    if (resultadoImg != null) {
                        val imagenRecord = com.example.gestor_deudores.data.database.PedidoImagen(
                            pedidoId = pedidoId.toInt(),
                            itemId = art.id,
                            ruta = resultadoImg.rutaOriginalComprimida,
                            rutaMiniatura = resultadoImg.rutaMiniatura,
                            tipo = "DISENO"
                        )
                        pedidoDao.agregarImagenPedido(imagenRecord)
                    }
                }
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onExito()
            }
        }
    }
}
