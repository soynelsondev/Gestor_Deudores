package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DistribucionAbonoDao {
    @Insert
    suspend fun agregarDistribucion(distribucion: DistribucionAbono): Long

    @Insert
    suspend fun agregarDistribuciones(distribuciones: List<DistribucionAbono>)

    @Update
    suspend fun actualizarDistribucion(distribucion: DistribucionAbono)

    @Delete
    suspend fun eliminarDistribucion(distribucion: DistribucionAbono)

    @Query("SELECT * FROM distribucion_abonos WHERE abonoId = :abonoId")
    suspend fun obtenerPorAbono(abonoId: Int): DistribucionAbono?

    @Query("SELECT * FROM distribucion_abonos WHERE fechaMillis BETWEEN :inicioMillis AND :finMillis ORDER BY fechaMillis ASC")
    fun obtenerPorRango(inicioMillis: Long, finMillis: Long): Flow<List<DistribucionAbono>>

    @Query("SELECT * FROM distribucion_abonos WHERE fechaMillis BETWEEN :inicioMillis AND :finMillis ORDER BY fechaMillis ASC")
    suspend fun obtenerPorRangoSync(inicioMillis: Long, finMillis: Long): List<DistribucionAbono>

    @Query("SELECT * FROM distribucion_abonos ORDER BY fechaMillis ASC")
    fun obtenerTodas(): Flow<List<DistribucionAbono>>

    @Query("SELECT * FROM distribucion_abonos ORDER BY fechaMillis ASC")
    suspend fun obtenerTodasSync(): List<DistribucionAbono>
}
