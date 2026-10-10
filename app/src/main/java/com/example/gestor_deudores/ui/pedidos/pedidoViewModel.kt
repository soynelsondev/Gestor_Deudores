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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FiltroEstadoPedido {
    TODOS,
    RECIBIDO,
    EN_PRODUCCION,
    LISTO,
    ENTREGADO
}

data class PedidoBorradorState(
    val deudorId: Int? = null,
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
    val esUrgente: Boolean = false
)

class PedidoViewModel(
    private val pedidoDao: PedidoDao,
    private val deudorDao: DeudorDao,
    private val deudaDao: DeudaDao,
    private val plantillaDao: PlantillaDao
) : ViewModel() {

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda.asStateFlow()

    private val _filtroEstado = MutableStateFlow(FiltroEstadoPedido.TODOS)
    val filtroEstado: StateFlow<FiltroEstadoPedido> = _filtroEstado.asStateFlow()

    private val _borradorActual = MutableStateFlow<PedidoBorradorState?>(null)
    val borradorActual: StateFlow<PedidoBorradorState?> = _borradorActual.asStateFlow()

    fun guardarBorrador(borrador: PedidoBorradorState) {
        _borradorActual.value = borrador
    }

    fun descartarBorrador() {
        _borradorActual.value = null
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

    val listaPedidos: StateFlow<List<PedidoConCliente>> = combine(
        pedidoDao.obtenerPedidosConCliente(),
        _textoBusqueda,
        _filtroEstado
    ) { pedidos, texto, estadoFiltro ->
        pedidos.filter { item ->
            val coincideEstado = when (estadoFiltro) {
                FiltroEstadoPedido.TODOS -> true
                FiltroEstadoPedido.RECIBIDO -> item.estado == "RECIBIDO"
                FiltroEstadoPedido.EN_PRODUCCION -> item.estado == "EN_PRODUCCION"
                FiltroEstadoPedido.LISTO -> item.estado == "LISTO"
                FiltroEstadoPedido.ENTREGADO -> item.estado == "ENTREGADO"
            }

            val coincideTexto = if (texto.isBlank()) true else {
                item.producto.contains(texto, ignoreCase = true) ||
                item.clienteNombre.contains(texto, ignoreCase = true) ||
                item.clienteApellido.contains(texto, ignoreCase = true)
            }

            coincideEstado && coincideTexto
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
