package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plantilla_atributos",
    foreignKeys = [
        ForeignKey(
            entity = PlantillaCotizacion::class,
            parentColumns = ["id"],
            childColumns = ["plantillaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["plantillaId"])]
)
data class PlantillaAtributo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plantillaId: Int,
    val nombre: String, // Ej: "Talla", "Ubicación", "Color", "Modelo"
    val tipo: String, // "REPARTE_CANTIDAD", "OPCION", "TEXTO"
    val obligatorio: Boolean = false,
    val orden: Int = 0
)

@Entity(
    tableName = "plantilla_atributo_opciones",
    foreignKeys = [
        ForeignKey(
            entity = PlantillaAtributo::class,
            parentColumns = ["id"],
            childColumns = ["atributoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["atributoId"])]
)
data class PlantillaAtributoOpcion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val atributoId: Int,
    val etiqueta: String, // Ej: "S", "M", "L", "XL", "Frente", "Espalda"
    val ajustePrecio: Double = 0.0, // Opcional +1.00 USD
    val orden: Int = 0
)

@Entity(
    tableName = "pedido_item_atributos",
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pedidoId"])]
)
data class PedidoItemAtributo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pedidoId: Int,
    val nombreCampo: String, // Ej: "Talla"
    val valor: String // Ej: "M (x2), L (x3)" o "Frente y Espalda"
)

data class PlantillaAtributoConOpciones(
    val atributo: PlantillaAtributo,
    val opciones: List<PlantillaAtributoOpcion>
)
