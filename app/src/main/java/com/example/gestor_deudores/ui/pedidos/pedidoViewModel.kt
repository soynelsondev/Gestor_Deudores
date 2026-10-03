package com.example.gestor_deudores.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.database.DeudaDao
import com.example.gestor_deudores.data.database.Deudor
import com.example.gestor_deudores.data.database.DeudorDao
import com.example.gestor_deudores.data.database.Pedido
import com.example.gestor_deudores.data.database.PedidoConCliente
import com.example.gestor_deudores.data.database.PedidoDao
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

class PedidoViewModel(
    private val pedidoDao: PedidoDao,
    private val deudorDao: DeudorDao,
    private val deudaDao: DeudaDao
) : ViewModel() {

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda.asStateFlow()

    private val _filtroEstado = MutableStateFlow(FiltroEstadoPedido.TODOS)
    val filtroEstado: StateFlow<FiltroEstadoPedido> = _filtroEstado.asStateFlow()

    // Lista de clientes disponibles para el selector del formulario
    val listaClientes: StateFlow<List<Deudor>> = deudorDao.obtenerDeudores()
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
                notas = pedidoConCliente.notas
            )
            pedidoDao.actualizarPedido(pedidoEditado)
        }
    }

    // --- GUARDADO TRANSACCIONAL DEL PEDIDO COMPLETO ---
    fun crearNuevoPedido(
        deudorId: Int,
        producto: String,
        cantidad: Int,
        precioUnitarioUsd: Double,
        abonoInicialUsd: Double,
        numCuotas: Int,
        frecuencia: String,
        fechaEntregaMillis: Long,
        notas: String,
        onExito: () -> Unit
    ) {
        val totalUsd = cantidad * precioUnitarioUsd
        val nuevoPedido = Pedido(
            deudorId = deudorId,
            producto = producto,
            cantidad = cantidad,
            precioUnitarioUsd = precioUnitarioUsd,
            totalUsd = totalUsd,
            fechaCreacionMillis = System.currentTimeMillis(),
            fechaEntregaMillis = fechaEntregaMillis,
            estado = "RECIBIDO",
            notas = notas
        )

        viewModelScope.launch {
            pedidoDao.guardarPedidoCompleto(
                pedido = nuevoPedido,
                deudaDao = deudaDao,
                abonoInicial = abonoInicialUsd,
                numCuotas = numCuotas,
                frecuencia = frecuencia
            )
            onExito()
        }
    }
}
