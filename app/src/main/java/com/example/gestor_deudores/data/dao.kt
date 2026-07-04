package com.example.gestor_deudores.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface DeudorDao{
    @Insert
    suspend fun agregarDeudor(deudor: Deudor)

    @Update
    suspend fun actualizarDeudor(deudor: Deudor)

    @Query("SELECT * From deudores ORDER BY nombre ASC")
    fun obtenerDeudores(): Flow<List<Deudor>>

    @Delete
    suspend fun eliminarDeudor(deudor: Deudor)

}

@Dao
interface DeudaDao {
    @Insert
    suspend fun agregarDeuda(deuda: Deuda)

    @Update
    suspend fun actualizarDeuda(deuda: Deuda) // Perfecto para registrar pagos parciales

    @Delete
    suspend fun eliminarDeuda(deuda: Deuda)

    // Obtener TODAS las deudas (por si quieres un historial general)
    @Query("SELECT * FROM Tabla_Deuda")
    fun obtenerTodasLasDeudas(): Flow<List<Deuda>>

    // LA MÁS IMPORTANTE: Obtener las deudas de UNA sola persona
    @Query("SELECT * FROM Tabla_Deuda WHERE idDeudor = :idDelDeudor")
    fun obtenerDeudasPorDeudor(idDelDeudor: Int): Flow<List<Deuda>>
}