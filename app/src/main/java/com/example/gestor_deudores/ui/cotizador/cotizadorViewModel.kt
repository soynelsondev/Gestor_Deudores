package com.example.gestor_deudores.ui.cotizador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.database.PlantillaCotizacion
import com.example.gestor_deudores.data.database.PlantillaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

// Estado completo del Formulario de Edición/Creación de Plantilla
data class CotizadorUiState(
    val idActual: Int = 0,
    val nombrePlantilla: String = "",
    
    // 1. Materiales (Pieza Base)
    val precioPaquetePieza: String = "",
    val monedaPaquetePieza: String = "USD", // "USD" o "VES"
    val cantidadPaquetePieza: String = "1", // Por defecto 1 para evitar división por cero
    
    // 2. Materiales (Empaque)
    val precioPaqueteEmpaque: String = "",
    val monedaPaqueteEmpaque: String = "USD",
    val cantidadPaqueteEmpaque: String = "1",
    
    // 2.5. Materiales (Papel de Sublimación Opcional)
    val precioPaquetePapel: String = "",
    val monedaPaquetePapel: String = "USD",
    val cantidadPaquetePapel: String = "100", // Generalmente es una resma de 100

    // 3. Servicios Directos (DTF)
    val precioTotalDtf: String = "",
    val monedaDtf: String = "USD",
    val rendimientoDtf: String = "1", // Cuántas piezas salen de ese metro/impresión
    
    // 4. Servicios Extras
    val costoTransporte: String = "",
    val monedaTransporte: String = "USD",
    val rendimientoTransporte: String = "1", // <-- NUEVO: ¿Para cuántas piezas te sirvió el pasaje/flete?
    
    val costoDiseno: String = "",
    val monedaDiseno: String = "USD",
    val rendimientoDiseno: String = "1", // <-- NUEVO: ¿Para cuántas piezas te sirvió el diseño?

    // 5. Operatividad y Ganancia (Barras deslizables)
    val porcentajeOperativo: Float = 10f, // 10% por defecto para luz/desgaste
    val porcentajeGanancia: Float = 40f,  // 40% por defecto de margen DETAL
    val porcentajeGananciaMayor: Float = 25f, // 25% por defecto de margen al MAYOR

    // ==========================================
    // RESULTADOS MATEMÁTICOS (Calculados en vivo)
    // ==========================================
    val costoTotalProduccionUsd: Double = 0.0,
    
    // Resultados al Detal
    val precioSugeridoUsd: Double = 0.0,
    val gananciaNetaUsd: Double = 0.0,
    
    // Resultados al Mayor
    val precioSugeridoMayorUsd: Double = 0.0,
    val gananciaNetaMayorUsd: Double = 0.0
)

class CotizadorViewModel(private val plantillaDao: PlantillaDao) : ViewModel() {

    private val _uiState = MutableStateFlow(CotizadorUiState())
    val uiState: StateFlow<CotizadorUiState> = _uiState.asStateFlow()

    // El catálogo completo de plantillas guardadas, conectado a la base de datos
    val listaPlantillas: StateFlow<List<PlantillaCotizacion>> = plantillaDao.obtenerTodasLasPlantillas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Variable reactiva para saber la tasa de cambio actual que haya introducido el usuario
    private var tasaBcvActual: Double = 1.0

    fun actualizarTasaBcv(tasa: Double) {
        if (tasa > 0) {
            tasaBcvActual = tasa
            calcularResultados() // Recalcula los USD en pantalla si cambia la tasa
        }
    }

    // --- FUNCIONES PARA ACTUALIZAR CADA CAMPO DESDE EL FORMULARIO ---

    fun onNombrePlantillaChange(valor: String) {
        _uiState.update { it.copy(nombrePlantilla = valor) }
    }

    fun onPrecioPaquetePiezaChange(valor: String) {
        _uiState.update { it.copy(precioPaquetePieza = valor) }
        calcularResultados()
    }

    fun onMonedaPaquetePiezaChange(moneda: String) {
        _uiState.update { it.copy(monedaPaquetePieza = moneda) }
        calcularResultados()
    }

    fun onCantidadPaquetePiezaChange(valor: String) {
        _uiState.update { it.copy(cantidadPaquetePieza = valor) }
        calcularResultados()
    }

    fun onPrecioPaqueteEmpaqueChange(valor: String) {
        _uiState.update { it.copy(precioPaqueteEmpaque = valor) }
        calcularResultados()
    }

    fun onMonedaPaqueteEmpaqueChange(moneda: String) {
        _uiState.update { it.copy(monedaPaqueteEmpaque = moneda) }
        calcularResultados()
    }

    fun onCantidadPaqueteEmpaqueChange(valor: String) {
        _uiState.update { it.copy(cantidadPaqueteEmpaque = valor) }
        calcularResultados()
    }
    
    fun onPrecioPaquetePapelChange(valor: String) {
        _uiState.update { it.copy(precioPaquetePapel = valor) }
        calcularResultados()
    }

    fun onMonedaPaquetePapelChange(moneda: String) {
        _uiState.update { it.copy(monedaPaquetePapel = moneda) }
        calcularResultados()
    }

    fun onCantidadPaquetePapelChange(valor: String) {
        _uiState.update { it.copy(cantidadPaquetePapel = valor) }
        calcularResultados()
    }

    fun onPrecioTotalDtfChange(valor: String) {
        _uiState.update { it.copy(precioTotalDtf = valor) }
        calcularResultados()
    }

    fun onMonedaDtfChange(moneda: String) {
        _uiState.update { it.copy(monedaDtf = moneda) }
        calcularResultados()
    }
    
    fun onRendimientoDtfChange(valor: String) {
        _uiState.update { it.copy(rendimientoDtf = valor) }
        calcularResultados()
    }

    fun onCostoTransporteChange(valor: String) {
        _uiState.update { it.copy(costoTransporte = valor) }
        calcularResultados()
    }

    fun onMonedaTransporteChange(moneda: String) {
        _uiState.update { it.copy(monedaTransporte = moneda) }
        calcularResultados()
    }
    
    fun onRendimientoTransporteChange(valor: String) {
        _uiState.update { it.copy(rendimientoTransporte = valor) }
        calcularResultados()
    }

    fun onCostoDisenoChange(valor: String) {
        _uiState.update { it.copy(costoDiseno = valor) }
        calcularResultados()
    }

    fun onMonedaDisenoChange(moneda: String) {
        _uiState.update { it.copy(monedaDiseno = moneda) }
        calcularResultados()
    }
    
    fun onRendimientoDisenoChange(valor: String) {
        _uiState.update { it.copy(rendimientoDiseno = valor) }
        calcularResultados()
    }

    fun onPorcentajeOperativoChange(valor: Float) {
        _uiState.update { it.copy(porcentajeOperativo = valor) }
        calcularResultados()
    }

    fun onPorcentajeGananciaChange(valor: Float) {
        val margenSeguro = if (valor >= 100f) 99.9f else valor
        _uiState.update { it.copy(porcentajeGanancia = margenSeguro) }
        calcularResultados()
    }

    fun onPorcentajeGananciaMayorChange(valor: Float) {
        val margenSeguro = if (valor >= 100f) 99.9f else valor
        _uiState.update { it.copy(porcentajeGananciaMayor = margenSeguro) }
        calcularResultados()
    }

    // ==========================================
    // CARGAR Y LIMPIAR PLANTILLAS
    // ==========================================

    fun limpiarFormulario() {
        _uiState.value = CotizadorUiState()
    }

    fun cargarPlantilla(plantilla: PlantillaCotizacion) {
        _uiState.update {
            it.copy(
                idActual = plantilla.id,
                nombrePlantilla = plantilla.nombrePlantilla,
                precioPaquetePieza = plantilla.precioPaquetePieza.toString(),
                monedaPaquetePieza = plantilla.monedaPaquetePieza,
                cantidadPaquetePieza = plantilla.cantidadPaquetePieza.toString(),
                precioPaqueteEmpaque = plantilla.precioPaqueteEmpaque.toString(),
                monedaPaqueteEmpaque = plantilla.monedaPaqueteEmpaque,
                cantidadPaqueteEmpaque = plantilla.cantidadPaqueteEmpaque.toString(),
                precioPaquetePapel = plantilla.precioPaquetePapel.toString(),
                monedaPaquetePapel = plantilla.monedaPaquetePapel,
                cantidadPaquetePapel = plantilla.cantidadPaquetePapel.toString(),
                precioTotalDtf = plantilla.precioTotalDtf.toString(),
                monedaDtf = plantilla.monedaDtf,
                rendimientoDtf = plantilla.rendimientoDtf.toString(),
                costoTransporte = plantilla.costoTransporte.toString(),
                monedaTransporte = plantilla.monedaTransporte,
                rendimientoTransporte = plantilla.rendimientoTransporte.toString(),
                costoDiseno = plantilla.costoDiseno.toString(),
                monedaDiseno = plantilla.monedaDiseno,
                rendimientoDiseno = plantilla.rendimientoDiseno.toString(),
                porcentajeOperativo = plantilla.porcentajeOperativo,
                porcentajeGanancia = plantilla.porcentajeGananciaDetal,
                porcentajeGananciaMayor = plantilla.porcentajeGananciaMayor
            )
        }
        calcularResultados() // Para que refresque los USD en vivo con la tasa actual
    }

    // ==========================================
    // GUARDADO EN LA BASE DE DATOS
    // ==========================================

    fun guardarPlantilla(onExito: () -> Unit) {
        val estado = _uiState.value
        
        // Validación básica
        if (estado.nombrePlantilla.isBlank()) return

        fun parseD(texto: String): Double = texto.replace(",", ".").toDoubleOrNull() ?: 0.0
        fun parseCant(texto: String): Int {
            val num = texto.toIntOrNull() ?: 1
            return if (num > 0) num else 1
        }

        val nuevaPlantilla = PlantillaCotizacion(
            id = estado.idActual, // Si es 0 se crea nueva, si es > 0 se edita
            nombrePlantilla = estado.nombrePlantilla,
            precioPaquetePieza = parseD(estado.precioPaquetePieza),
            monedaPaquetePieza = estado.monedaPaquetePieza,
            cantidadPaquetePieza = parseCant(estado.cantidadPaquetePieza),
            precioPaqueteEmpaque = parseD(estado.precioPaqueteEmpaque),
            monedaPaqueteEmpaque = estado.monedaPaqueteEmpaque,
            cantidadPaqueteEmpaque = parseCant(estado.cantidadPaqueteEmpaque),
            precioPaquetePapel = parseD(estado.precioPaquetePapel),
            monedaPaquetePapel = estado.monedaPaquetePapel,
            cantidadPaquetePapel = parseCant(estado.cantidadPaquetePapel),
            precioTotalDtf = parseD(estado.precioTotalDtf),
            monedaDtf = estado.monedaDtf,
            rendimientoDtf = parseCant(estado.rendimientoDtf),
            costoTransporte = parseD(estado.costoTransporte),
            monedaTransporte = estado.monedaTransporte,
            rendimientoTransporte = parseCant(estado.rendimientoTransporte),
            costoDiseno = parseD(estado.costoDiseno),
            monedaDiseno = estado.monedaDiseno,
            rendimientoDiseno = parseCant(estado.rendimientoDiseno),
            porcentajeOperativo = estado.porcentajeOperativo,
            porcentajeGananciaDetal = estado.porcentajeGanancia,
            porcentajeGananciaMayor = estado.porcentajeGananciaMayor
        )

        viewModelScope.launch {
            if (estado.idActual > 0) {
                plantillaDao.actualizarPlantilla(nuevaPlantilla)
            } else {
                plantillaDao.agregarPlantilla(nuevaPlantilla)
            }
            onExito()
        }
    }

    fun eliminarPlantilla(plantilla: PlantillaCotizacion) {
        viewModelScope.launch {
            plantillaDao.eliminarPlantilla(plantilla)
        }
    }

    // ==========================================
    // EL CEREBRO MATEMÁTICO EN VIVO
    // ==========================================
    private fun calcularResultados() {
        val estado = _uiState.value
        val tasaActiva = if (tasaBcvActual > 0.0) tasaBcvActual else 1.0

        fun parseD(texto: String): Double = texto.replace(",", ".").toDoubleOrNull() ?: 0.0
        fun parseCant(texto: String): Double {
            val num = texto.toDoubleOrNull() ?: 1.0
            return if (num > 0) num else 1.0
        }

        fun aUsd(valor: Double, moneda: String): Double {
            return if (moneda == "VES") valor / tasaActiva else valor
        }

        val costoPiezaBruto = parseD(estado.precioPaquetePieza) / parseCant(estado.cantidadPaquetePieza)
        val costoUnitarioPieza = aUsd(costoPiezaBruto, estado.monedaPaquetePieza)

        val costoEmpaqueBruto = parseD(estado.precioPaqueteEmpaque) / parseCant(estado.cantidadPaqueteEmpaque)
        val costoUnitarioEmpaque = aUsd(costoEmpaqueBruto, estado.monedaPaqueteEmpaque)
        
        val costoPapelBruto = parseD(estado.precioPaquetePapel) / parseCant(estado.cantidadPaquetePapel)
        val costoUnitarioPapel = aUsd(costoPapelBruto, estado.monedaPaquetePapel)

        // 2. Sumamos los servicios logísticos en USD
        val precioTotalDtf = aUsd(parseD(estado.precioTotalDtf), estado.monedaDtf)
        val dtfPorPieza = precioTotalDtf / parseCant(estado.rendimientoDtf)
        
        val totalTransporte = aUsd(parseD(estado.costoTransporte), estado.monedaTransporte)
        val transportePorPieza = totalTransporte / parseCant(estado.rendimientoTransporte)
        
        val totalDiseno = aUsd(parseD(estado.costoDiseno), estado.monedaDiseno)
        val disenoPorPieza = totalDiseno / parseCant(estado.rendimientoDiseno)

        val subtotalMateriales = costoUnitarioPieza + costoUnitarioEmpaque + costoUnitarioPapel + dtfPorPieza + transportePorPieza + disenoPorPieza
        val costoOperativo = subtotalMateriales * (estado.porcentajeOperativo / 100.0)
        val costoTotalProduccion = subtotalMateriales + costoOperativo

        var precioVentaSug = 0.0
        var ganancia = 0.0
        var precioVentaMayorSug = 0.0
        var gananciaMayor = 0.0

        if (costoTotalProduccion > 0.0) {
            val margenDecimal = estado.porcentajeGanancia / 100.0
            precioVentaSug = costoTotalProduccion / (1.0 - margenDecimal)
            ganancia = precioVentaSug - costoTotalProduccion
            
            val margenMayorDecimal = estado.porcentajeGananciaMayor / 100.0
            precioVentaMayorSug = costoTotalProduccion / (1.0 - margenMayorDecimal)
            gananciaMayor = precioVentaMayorSug - costoTotalProduccion
        }

        fun redondear2(valor: Double): Double = 
            BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toDouble()

        _uiState.update {
            it.copy(
                costoTotalProduccionUsd = redondear2(costoTotalProduccion),
                precioSugeridoUsd = redondear2(precioVentaSug),
                gananciaNetaUsd = redondear2(ganancia),
                precioSugeridoMayorUsd = redondear2(precioVentaMayorSug),
                gananciaNetaMayorUsd = redondear2(gananciaMayor)
            )
        }
    }
}
