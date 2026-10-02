package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeudorDao {
    @Insert
    suspend fun agregarDeudor(deudor: Deudor): Long

    @Update
    suspend fun actualizarDeudor(deudor: Deudor)

    @Query("SELECT * From deudores ORDER BY nombre ASC")
    fun obtenerDeudores(): Flow<List<Deudor>>

    @Query("SELECT * FROM deudores WHERE nombre LIKE '%' || :busqueda || '%' OR apellido LIKE '%' || :busqueda || '%'")
    fun buscarDeudores(busqueda: String): Flow<List<Deudor>>

    @Query("SELECT * FROM deudores WHERE id = :id")
    suspend fun obtenerDeudorPorId(id: Int): Deudor?

    @Delete
    suspend fun eliminarDeudor(deudor: Deudor)
}

@Dao
interface DeudaDao {
    @Insert
    suspend fun agregarDeuda(deuda: Deuda)

    @Update
    suspend fun actualizarDeuda(deuda: Deuda)

    @Delete
    suspend fun eliminarDeuda(deuda: Deuda)

    @Query("SELECT * FROM Tabla_Deuda WHERE id = :idDeuda")
    suspend fun obtenerDeudaPorId(idDeuda: Int): Deuda?

    @Query("DELETE FROM Tabla_Deuda WHERE idDeudor = :idDeudor")
    suspend fun eliminarDeudasDeUsuario(idDeudor: Int)

    @Query("SELECT * FROM Tabla_Deuda")
    fun obtenerTodasLasDeudas(): Flow<List<Deuda>>

    @Query("SELECT * FROM Tabla_Deuda WHERE idDeudor = :idDelDeudor")
    fun obtenerDeudasPorDeudor(idDelDeudor: Int): Flow<List<Deuda>>

    @Query("SELECT SUM(montoRestante) FROM tabla_deuda WHERE idDeudor = :idDelDeudor AND estado != 'Cancelado'")
    fun obtenerSumaDeudasPorDeudor(idDelDeudor: Int): Flow<Double?>

    @Query("SELECT DISTINCT tipoDeuda FROM Tabla_Deuda WHERE tipoDeuda != '' ORDER BY tipoDeuda ASC")
    fun obtenerTiposDeudaUnicos(): Flow<List<String>>
}
