package com.example.gestor_deudores.data


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "deudores")
    data class  Deudor(
    @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        val nombre: String,
        val apellido: String,
        val cedula: String,
        val telf: String
    )

@Entity(tableName = "Tabla_Deuda",
    foreignKeys = [ForeignKey(entity = Deudor::class,
        parentColumns = ["id"],
        childColumns = ["idDeudor"],
        onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["idDeudor"])]
)
data class Deuda(
    @PrimaryKey(autoGenerate = true)
    var id: Int= 0,
    val idDeudor: Int,
    val montoInicial : Double,
    val montoRestante: Double,
    val tipoDeuda: String,
    val fecha: String,
    val rol: String,
    val descripcion: String,
    val estado : String
)
