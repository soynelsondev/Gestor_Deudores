package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CierreCajaDao {
    @Insert
    suspend fun agregarCierre(cierre: CierreCaja): Long

    @Delete
    suspend fun eliminarCierre(cierre: CierreCaja)

    @Query("SELECT * FROM cierres_caja WHERE cuentaId = :cuentaId ORDER BY fechaMillis DESC")
    fun obtenerCierresPorCuenta(cuentaId: Int): Flow<List<CierreCaja>>

    @Query("SELECT * FROM cierres_caja ORDER BY fechaMillis DESC")
    fun obtenerTodosLosCierres(): Flow<List<CierreCaja>>

    @Query("SELECT * FROM cierres_caja ORDER BY fechaMillis DESC")
    suspend fun obtenerTodosLosCierresSync(): List<CierreCaja>
}
