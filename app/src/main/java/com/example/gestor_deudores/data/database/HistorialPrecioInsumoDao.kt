package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialPrecioInsumoDao {
    @Query("SELECT * FROM historial_precios_insumo WHERE insumoId = :insumoId ORDER BY fecha DESC")
    fun obtenerHistorialPorInsumo(insumoId: String): Flow<List<HistorialPrecioInsumo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarHistorial(historial: HistorialPrecioInsumo)
}
