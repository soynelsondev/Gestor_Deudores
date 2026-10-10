package com.example.gestor_deudores.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedidos",
    foreignKeys = [
        ForeignKey(
            entity = Deudor::class,
            parentColumns = ["id"],
            childColumns = ["deudorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deudorId"])]
)
data class Pedido(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val deudorId: Int,
    val producto: String,
    val cantidad: Int,
    val precioUnitarioUsd: Double,
    val totalUsd: Double,
    val fechaCreacionMillis: Long = System.currentTimeMillis(),
    val fechaEntregaMillis: Long = 0L,
    val estado: String = "RECIBIDO",
    val notas: String = "",
    // Costos reales guardados en el momento de la venta
    val costoPiezaBaseUsd: Double = 0.0, // Antes era costoTextilUsd
    val costoPasajeUsd: Double = 0.0,
    val costoInsumosUsd: Double = 0.0,
    val nombrePiezaBase: String = "Insumo Base", // Ej: "Camisa", "Taza Mágica", "Cuadro"
    val costoEnvio: Double = 0.0,
    val descuento: Double = 0.0,
    val costoDiseno: Double = 0.0,
    val tipoEntrega: String = "RETIRO",
    val direccionEntrega: String? = null,
    val urgente: Boolean = false,
    val estadoDiseno: String = "PENDIENTE",
    val esBorrador: Boolean = false
)

data class PedidoConCliente(
    val id: Int,
    val deudorId: Int,
    val producto: String,
    val cantidad: Int,
    val precioUnitarioUsd: Double,
    val totalUsd: Double,
    val fechaCreacionMillis: Long,
    val fechaEntregaMillis: Long,
    val estado: String,
    val notas: String,
    val costoPiezaBaseUsd: Double = 0.0,
    val costoPasajeUsd: Double = 0.0,
    val costoInsumosUsd: Double = 0.0,
    val nombrePiezaBase: String = "Insumo Base",
    val costoEnvio: Double = 0.0,
    val descuento: Double = 0.0,
    val costoDiseno: Double = 0.0,
    val tipoEntrega: String = "RETIRO",
    val direccionEntrega: String? = null,
    val urgente: Boolean = false,
    val estadoDiseno: String = "PENDIENTE",
    val esBorrador: Boolean = false,
    val clienteNombre: String,
    val clienteApellido: String,
    val clienteTelefono: String
)
