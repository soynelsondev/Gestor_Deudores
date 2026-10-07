package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InsumoBibliotecaDao {
    @Query("SELECT * FROM biblioteca_insumos ORDER BY nombre ASC")
    fun obtenerTodosLosInsumos(): Flow<List<InsumoBiblioteca>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarInsumo(insumo: InsumoBiblioteca)

    @Delete
    suspend fun eliminarInsumo(insumo: InsumoBiblioteca)
}
