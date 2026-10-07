package com.example.gestor_deudores.data.utils

import com.example.gestor_deudores.data.database.InsumoCotizacion

object CotizadorPresetsUtils {

    data class PresetProducto(
        val titulo: String,
        val icono: String,
        val insumosSugeridos: List<InsumoCotizacion>
    )

    fun obtenerPresets(): List<PresetProducto> {
        return listOf(
            PresetProducto(
                titulo = "Taza Mágica / Cerámica",
                icono = "☕",
                insumosSugeridos = listOf(
                    InsumoCotizacion(nombre = "Taza Blanca 11oz", categoria = "MATERIA_PRIMA", precioLote = 45.0, rendimientoCantidad = 36),
                    InsumoCotizacion(nombre = "Caja / Empaque Individual", categoria = "INSUMO_EXTRA", precioLote = 5.0, rendimientoCantidad = 50),
                    InsumoCotizacion(nombre = "Papel Sublimación (1/2 hoja)", categoria = "INSUMO_EXTRA", precioLote = 10.0, rendimientoCantidad = 200),
                    InsumoCotizacion(nombre = "Cinta Térmica y Tintas", categoria = "INSUMO_EXTRA", precioLote = 15.0, rendimientoCantidad = 300),
                    InsumoCotizacion(nombre = "Pasaje / Delivery Taller", categoria = "SERVICIO", precioLote = 5.0, rendimientoCantidad = 36)
                )
            ),
            PresetProducto(
                titulo = "Franela Stamp / DTF",
                icono = "👕",
                insumosSugeridos = listOf(
                    InsumoCotizacion(nombre = "Franela Algodón 100%", categoria = "MATERIA_PRIMA", precioLote = 48.0, rendimientoCantidad = 12),
                    InsumoCotizacion(nombre = "Impresión Metro DTF", categoria = "SERVICIO", precioLote = 8.5, rendimientoCantidad = 6),
                    InsumoCotizacion(nombre = "Bolsa de Empaque Transparente", categoria = "INSUMO_EXTRA", precioLote = 4.0, rendimientoCantidad = 100),
                    InsumoCotizacion(nombre = "Flete / Transporte", categoria = "SERVICIO", precioLote = 5.0, rendimientoCantidad = 12)
                )
            ),
            PresetProducto(
                titulo = "Franela Sublimación",
                icono = "🎽",
                insumosSugeridos = listOf(
                    InsumoCotizacion(nombre = "Franela Poliéster Sublimable", categoria = "MATERIA_PRIMA", precioLote = 36.0, rendimientoCantidad = 12),
                    InsumoCotizacion(nombre = "Hoja Sublimación A4", categoria = "INSUMO_EXTRA", precioLote = 10.0, rendimientoCantidad = 100),
                    InsumoCotizacion(nombre = "Tintas y Cinta Térmica", categoria = "INSUMO_EXTRA", precioLote = 15.0, rendimientoCantidad = 300),
                    InsumoCotizacion(nombre = "Bolsa Empaque", categoria = "INSUMO_EXTRA", precioLote = 4.0, rendimientoCantidad = 100)
                )
            ),
            PresetProducto(
                titulo = "Gorra Personalizada",
                icono = "🧢",
                insumosSugeridos = listOf(
                    InsumoCotizacion(nombre = "Gorra Trucker Sublimable", categoria = "MATERIA_PRIMA", precioLote = 24.0, rendimientoCantidad = 12),
                    InsumoCotizacion(nombre = "Papel y Tinta Sublimación", categoria = "INSUMO_EXTRA", precioLote = 10.0, rendimientoCantidad = 200),
                    InsumoCotizacion(nombre = "Bolsa de Empaque", categoria = "INSUMO_EXTRA", precioLote = 4.0, rendimientoCantidad = 100)
                )
            ),
            PresetProducto(
                titulo = "Cuadro / Placa MDF",
                icono = "🖼️",
                insumosSugeridos = listOf(
                    InsumoCotizacion(nombre = "Base MDF Sublimable", categoria = "MATERIA_PRIMA", precioLote = 20.0, rendimientoCantidad = 10),
                    InsumoCotizacion(nombre = "Hoja A4 + Tinta + Cinta", categoria = "INSUMO_EXTRA", precioLote = 10.0, rendimientoCantidad = 100),
                    InsumoCotizacion(nombre = "Embalaje / Burbuja", categoria = "INSUMO_EXTRA", precioLote = 5.0, rendimientoCantidad = 50)
                )
            ),
            PresetProducto(
                titulo = "Mousepad / Almohada / Termo",
                icono = "🖱️",
                insumosSugeridos = listOf(
                    InsumoCotizacion(nombre = "Producto Base Sublimable", categoria = "MATERIA_PRIMA", precioLote = 15.0, rendimientoCantidad = 10),
                    InsumoCotizacion(nombre = "Papel + Tinta + Cinta", categoria = "INSUMO_EXTRA", precioLote = 10.0, rendimientoCantidad = 100),
                    InsumoCotizacion(nombre = "Empaque / Bolsa", categoria = "INSUMO_EXTRA", precioLote = 4.0, rendimientoCantidad = 100)
                )
            )
        )
    }
}
