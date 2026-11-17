package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoCostoLineaDao {
    @Insert
    suspend fun agregarLinea(linea: PedidoCostoLinea): Long

    @Insert
    suspend fun agregarLineas(lineas: List<PedidoCostoLinea>)

    @Update
    suspend fun actualizarLinea(linea: PedidoCostoLinea)

    @Delete
    suspend fun eliminarLinea(linea: PedidoCostoLinea)

    @Query("SELECT * FROM pedido_costo_lineas WHERE pedidoId = :pedidoId ORDER BY id ASC")
    suspend fun obtenerPorPedido(pedidoId: Int): List<PedidoCostoLinea>

    @Query("SELECT * FROM pedido_costo_lineas WHERE pedidoId IN (:pedidoIds) ORDER BY id ASC")
    suspend fun obtenerPorPedidosSync(pedidoIds: List<Int>): List<PedidoCostoLinea>

    @Query("SELECT * FROM pedido_costo_lineas ORDER BY id ASC")
    fun obtenerTodas(): Flow<List<PedidoCostoLinea>>
}
