package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CuentaBancariaDao {
    @Insert
    suspend fun agregarCuenta(cuenta: CuentaBancaria): Long

    @Update
    suspend fun actualizarCuenta(cuenta: CuentaBancaria)

    @Delete
    suspend fun eliminarCuenta(cuenta: CuentaBancaria)

    @Query("SELECT * FROM cuentas_bancarias ORDER BY nombre ASC")
    fun obtenerTodasLasCuentas(): Flow<List<CuentaBancaria>>
}
