package com.example.gestor_deudores.data.database

import com.example.gestor_deudores.data.utils.TipoGanancia
import java.util.UUID

/**
 * REPRESENTA UNA ESCALA DE PRECIO POR CANTIDAD (DETAL, MAYOR, GRAN MAYORISTA).
 * Permite cambiar automáticamente la ganancia cuando el cliente compra en volumen.
 */
data class EscalaPrecio(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "Mayor", // "Detal", "Mayor", "Gran Mayor"
    val desdeCantidad: Int = 6,
    val tipoGanancia: TipoGanancia = TipoGanancia.SOBRE_COSTO,
    val gananciaValor: Double = 30.0 // 30% o $3.00
)
