package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoCuentaDao {
    @Insert
    suspend fun agregarMovimiento(movimiento: MovimientoCuenta): Long

    @Insert
    suspend fun agregarMovimientos(movimientos: List<MovimientoCuenta>)

    @Update
    suspend fun actualizarMovimiento(movimiento: MovimientoCuenta)

    @Delete
    suspend fun eliminarMovimiento(movimiento: MovimientoCuenta)

    @Query("SELECT * FROM movimientos_cuenta WHERE cuentaId = :cuentaId ORDER BY fechaMillis DESC")
    fun obtenerMovimientosPorCuenta(cuentaId: Int): Flow<List<MovimientoCuenta>>

    @Query("SELECT * FROM movimientos_cuenta WHERE cuentaId = :cuentaId ORDER BY fechaMillis DESC")
    suspend fun obtenerMovimientosPorCuentaSync(cuentaId: Int): List<MovimientoCuenta>

    @Query("SELECT SUM(monto) FROM movimientos_cuenta WHERE cuentaId = :cuentaId")
    fun obtenerSumaMovimientosCuenta(cuentaId: Int): Flow<Double?>

    @Query("SELECT SUM(monto) FROM movimientos_cuenta WHERE cuentaId = :cuentaId")
    suspend fun obtenerSumaMovimientosCuentaSync(cuentaId: Int): Double?

    @Query("SELECT * FROM movimientos_cuenta ORDER BY fechaMillis DESC")
    fun obtenerTodosLosMovimientos(): Flow<List<MovimientoCuenta>>

    @Query("SELECT * FROM movimientos_cuenta ORDER BY fechaMillis DESC")
    suspend fun obtenerTodosLosMovimientosSync(): List<MovimientoCuenta>
}
