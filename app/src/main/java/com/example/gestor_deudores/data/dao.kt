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
    suspend fun agregarDeudor(deudor: Deudor): Long

    @Update
    suspend fun actualizarDeudor(deudor: Deudor)

    @Query("SELECT * From deudores ORDER BY nombre ASC")
    fun obtenerDeudores(): Flow<List<Deudor>>

    // FUNCIONALIDAD FASE 1: Buscador de perfil de deudor
    // Busca coincidencias tanto en el nombre como en el apellido
    @Query("SELECT * FROM deudores WHERE nombre LIKE '%' || :busqueda || '%' OR apellido LIKE '%' || :busqueda || '%'")
    fun buscarDeudores(busqueda: String): Flow<List<Deudor>>

    @Query("SELECT * FROM deudores WHERE id = :id") // Asegúrate de que "deudor" sea el nombre real de tu tabla
    suspend fun obtenerDeudorPorId(id: Int): Deudor?

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

    // --- ¡AQUÍ VA LA NUEVA FUNCIÓN! ---
    @Query("DELETE FROM Tabla_Deuda WHERE idDeudor = :idDeudor")
    suspend fun eliminarDeudasDeUsuario(idDeudor: Int)

    // Obtener TODAS las deudas (por si quieres un historial general)
    @Query("SELECT * FROM Tabla_Deuda")
    fun obtenerTodasLasDeudas(): Flow<List<Deuda>>

    // LA MÁS IMPORTANTE: Obtener las deudas de UNA sola persona
    @Query("SELECT * FROM Tabla_Deuda WHERE idDeudor = :idDelDeudor")
    fun obtenerDeudasPorDeudor(idDelDeudor: Int): Flow<List<Deuda>>

    // FUNCIONALIDAD FASE 2: Suma de deudas
    // Calcula automáticamente cuánto te debe en total una persona (sumando solo lo que no está cancelado)
    @Query("SELECT SUM(montoRestante) FROM tabla_deuda WHERE idDeudor = :idDelDeudor AND estado != 'Cancelado'")
    fun obtenerSumaDeudasPorDeudor(idDelDeudor: Int): Flow<Double?>
}