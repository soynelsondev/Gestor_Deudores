package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilNegocioDao {
    @Query("SELECT * FROM perfil_negocio WHERE id = 1")
    fun obtenerPerfil(): Flow<PerfilNegocio?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPerfil(perfil: PerfilNegocio)
}
