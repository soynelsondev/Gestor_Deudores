package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Dao
abstract class PedidoDao {
    @Insert
    abstract suspend fun agregarPedido(pedido: Pedido): Long

    @Update
    abstract suspend fun actualizarPedido(pedido: Pedido)

    @Delete
    abstract suspend fun eliminarPedido(pedido: Pedido)

    @Query("SELECT * FROM pedidos WHERE id = :id")
    abstract suspend fun obtenerPedidoPorId(id: Int): Pedido?

    @Query("""
        SELECT p.id, p.deudorId, p.producto, p.cantidad, p.precioUnitarioUsd, p.totalUsd, 
               p.fechaCreacionMillis, p.fechaEntregaMillis, p.estado, p.notas,
               p.costoPiezaBaseUsd, p.costoPasajeUsd, p.costoInsumosUsd, p.nombrePiezaBase,
               p.costoEnvio, p.descuento, p.costoDiseno, p.tipoEntrega, p.direccionEntrega,
               p.urgente, p.estadoDiseno, p.esBorrador,
               d.nombre AS clienteNombre, d.apellido AS clienteApellido, d.telf AS clienteTelefono
        FROM pedidos p
        INNER JOIN deudores d ON p.deudorId = d.id
        ORDER BY p.fechaCreacionMillis DESC
    """)
    abstract fun obtenerPedidosConCliente(): Flow<List<PedidoConCliente>>

    // Guardado Atómico: Pedido + Deuda + Abono Inicial en una sola transacción
    @Transaction
    open suspend fun guardarPedidoCompleto(
        pedido: Pedido,
        deudaDao: DeudaDao,
        abonoInicial: Double,
        numCuotas: Int,
        frecuencia: String
    ) {
        val pedidoId = agregarPedido(pedido)
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaCreacionStr = formatoFecha.format(Date(pedido.fechaCreacionMillis))

        val infoCuotasText = if (frecuencia == "AL_ENTREGAR") {
            " | Cancela saldo restante al retirar/entregar"
        } else {
            " | Paga en $numCuotas cuotas (${frecuencia.lowercase()})"
        }

        // 1. Insertamos el Cargo en la cuenta del cliente
        val cargoDeuda = Deuda(
            idDeudor = pedido.deudorId,
            montoInicial = pedido.totalUsd,
            montoRestante = pedido.totalUsd,
            tipoDeuda = pedido.producto,
            fecha = fechaCreacionStr,
            fechaMillis = pedido.fechaCreacionMillis,
            tipo = "CARGO",
            numCuotas = numCuotas,
            frecuencia = frecuencia,
            rol = "CLIENTE",
            descripcion = "Pedido #${pedidoId}: ${pedido.producto} (x${pedido.cantidad})$infoCuotasText",
            estado = "Pendiente"
        )
        deudaDao.agregarDeuda(cargoDeuda)

        // 2. Si dejó abono inicial, insertamos el recibo
        if (abonoInicial > 0.0) {
            val abonoDeuda = Deuda(
                idDeudor = pedido.deudorId,
                montoInicial = 0.0,
                montoRestante = -abonoInicial,
                tipoDeuda = "Abono Inicial",
                fecha = fechaCreacionStr,
                fechaMillis = pedido.fechaCreacionMillis,
                tipo = "ABONO",
                numCuotas = 1,
                frecuencia = "MENSUAL",
                rol = "PAGO",
                descripcion = "Adelanto entregado al encargar Pedido #${pedidoId}",
                estado = "Activo"
            )
            deudaDao.agregarDeuda(abonoDeuda)
        }
    }
}
