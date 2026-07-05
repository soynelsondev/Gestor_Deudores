package com.example.gestor_deudores.ui

object rutas {
    const val HOME = "pantalla_Principal"
    const val REGISTRO = "pantalla_registro_deudor"
    private const val REGISTRO_DEUDA_BASE = "pantalla_registro_deuda"

    // 2. LA PLANTILLA (Esta es la que vas a poner dentro del NavHost)
    // Resultado: "pantalla_registro_deuda/{id}"
    const val REGISTRO_DEUDA_TEMPLATE = "$REGISTRO_DEUDA_BASE/{id}"

    // 3. LA FUNCIÓN DE NAVEGACIÓN (Esta es la que usas en el botón al hacer clic)
    // Resultado si le pasas 14: "pantalla_registro_deuda/14"
    fun crearRutaRegistroDeuda(id: Int): String {
        return "$REGISTRO_DEUDA_BASE/$id"
    }
}