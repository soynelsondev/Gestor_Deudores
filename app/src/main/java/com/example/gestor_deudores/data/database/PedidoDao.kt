package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Insert
    suspend fun agregarPedido(pedido: Pedido): Long

    @Update
    suspend fun actualizarPedido(pedido: Pedido)

    @Delete
    suspend fun eliminarPedido(pedido: Pedido)

    @Query("SELECT * FROM pedidos WHERE id = :id")
    suspend fun obtenerPedidoPorId(id: Int): Pedido?

    @Query("""
        SELECT p.id, p.deudorId, p.producto, p.cantidad, p.precioUnitarioUsd, p.totalUsd, 
               p.fechaCreacionMillis, p.fechaEntregaMillis, p.estado, p.notas, 
               d.nombre AS clienteNombre, d.apellido AS clienteApellido, d.telf AS clienteTelefono
        FROM pedidos p
        INNER JOIN deudores d ON p.deudorId = d.id
        ORDER BY p.fechaCreacionMillis DESC
    """)
    fun obtenerPedidosConCliente(): Flow<List<PedidoConCliente>>
}
