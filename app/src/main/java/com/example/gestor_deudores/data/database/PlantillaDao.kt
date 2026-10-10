package com.example.gestor_deudores.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantillaDao {
    @Insert
    suspend fun agregarPlantilla(plantilla: PlantillaCotizacion)

    @Update
    suspend fun actualizarPlantilla(plantilla: PlantillaCotizacion)

    @Delete
    suspend fun eliminarPlantilla(plantilla: PlantillaCotizacion)

    @Query("SELECT * FROM plantillas_cotizacion ORDER BY nombrePlantilla ASC")
    fun obtenerTodasLasPlantillas(): Flow<List<PlantillaCotizacion>>

    @Query("SELECT * FROM plantillas_cotizacion WHERE id = :id LIMIT 1")
    suspend fun obtenerPlantillaPorId(id: Int): PlantillaCotizacion?

    @Insert
    suspend fun agregarAtributo(atributo: PlantillaAtributo): Long

    @Insert
    suspend fun agregarOpcionesAtributo(opciones: List<PlantillaAtributoOpcion>)

    @Query("SELECT * FROM plantilla_atributos WHERE plantillaId = :plantillaId ORDER BY orden ASC")
    fun obtenerAtributosPorPlantilla(plantillaId: Int): Flow<List<PlantillaAtributo>>

    @Query("SELECT * FROM plantilla_atributo_opciones WHERE atributoId = :atributoId ORDER BY orden ASC")
    fun obtenerOpcionesPorAtributo(atributoId: Int): Flow<List<PlantillaAtributoOpcion>>

    @Query("SELECT * FROM plantilla_atributos WHERE plantillaId = :plantillaId ORDER BY orden ASC")
    suspend fun obtenerAtributosPorPlantillaSync(plantillaId: Int): List<PlantillaAtributo>

    @Query("SELECT * FROM plantilla_atributo_opciones WHERE atributoId = :atributoId ORDER BY orden ASC")
    suspend fun obtenerOpcionesPorAtributoSync(atributoId: Int): List<PlantillaAtributoOpcion>
}
