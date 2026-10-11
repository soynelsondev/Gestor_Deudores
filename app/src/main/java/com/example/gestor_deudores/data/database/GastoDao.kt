package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class GastoConCategoria(
    val id: Int,
    val categoriaId: Int,
    val categoriaNombre: String,
    val categoriaTipo: String,
    val sobreAsociado: String?,
    val descripcion: String,
    val montoUsd: Double,
    val moneda: String,
    val montoOriginal: Double,
    val tasa: Double,
    val cuentaId: Int?,
    val proveedor: String?,
    val fechaMillis: Long
)

@Dao
interface GastoDao {
    // CATEGORÍAS
    @Insert
    suspend fun agregarCategoria(categoria: CategoriaGasto): Long

    @Query("SELECT * FROM categoria_gastos WHERE activa = 1 ORDER BY nombre ASC")
    fun obtenerCategoriasActivas(): Flow<List<CategoriaGasto>>

    @Query("SELECT * FROM categoria_gastos WHERE activa = 1 ORDER BY nombre ASC")
    suspend fun obtenerCategoriasActivasSync(): List<CategoriaGasto>

    // GASTOS
    @Insert
    suspend fun agregarGasto(gasto: Gasto): Long

    @Delete
    suspend fun eliminarGasto(gasto: Gasto)

    @Query("""
        SELECT g.id, g.categoriaId, c.nombre AS categoriaNombre, c.tipo AS categoriaTipo, c.sobreAsociado,
               g.descripcion, g.montoUsd, g.moneda, g.montoOriginal, g.tasa, g.cuentaId, g.proveedor, g.fechaMillis
        FROM gastos g
        INNER JOIN categoria_gastos c ON g.categoriaId = c.id
        WHERE g.fechaMillis BETWEEN :inicioMillis AND :finMillis
        ORDER BY g.fechaMillis DESC
    """)
    fun obtenerGastosPorRango(inicioMillis: Long, finMillis: Long): Flow<List<GastoConCategoria>>

    @Query("""
        SELECT g.id, g.categoriaId, c.nombre AS categoriaNombre, c.tipo AS categoriaTipo, c.sobreAsociado,
               g.descripcion, g.montoUsd, g.moneda, g.montoOriginal, g.tasa, g.cuentaId, g.proveedor, g.fechaMillis
        FROM gastos g
        INNER JOIN categoria_gastos c ON g.categoriaId = c.id
        ORDER BY g.fechaMillis DESC
    """)
    fun obtenerTodosLosGastos(): Flow<List<GastoConCategoria>>

    // RETIROS DE DUENO
    @Insert
    suspend fun agregarRetiro(retiro: Retiro): Long

    @Delete
    suspend fun eliminarRetiro(retiro: Retiro)

    @Query("SELECT * FROM retiros WHERE fechaMillis BETWEEN :inicioMillis AND :finMillis ORDER BY fechaMillis DESC")
    fun obtenerRetirosPorRango(inicioMillis: Long, finMillis: Long): Flow<List<Retiro>>

    @Query("SELECT * FROM retiros ORDER BY fechaMillis DESC")
    fun obtenerTodosLosRetiros(): Flow<List<Retiro>>
}
