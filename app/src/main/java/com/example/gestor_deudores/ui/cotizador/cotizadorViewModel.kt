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

// Estado de Resultados calculados para una plantilla del catálogo
data class ResultadoPlantilla(
    val costoTotalProduccionUsd: Double,
    val precioUsd: Double,
    val gananciaUsd: Double
)

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

    // 4.5. Insumos Extras (Imanes, resina, etc)
    val costoExtra: String = "",
    val monedaExtra: String = "USD",
    val rendimientoExtra: String = "1",

    // 5. Operatividad y Ganancia (Barras deslizables)
    val porcentajeOperativo: Float = 10f, // 10% por defecto para luz/desgaste
    val porcentajeGanancia: Float = 40f,  // 40% por defecto de margen libre
    
    // 6. Configuración Venta al Mayor
    val esPlantillaMayor: Boolean = false,
    val minimoUnidadesMayor: String = "6",

    // 7. LISTA DINÁMICA DE INSUMOS Y SERVICIOS (Lego Style)
    val listaInsumos: List<com.example.gestor_deudores.data.database.InsumoCotizacion> = emptyList(),

    // ==========================================
    // RESULTADOS MATEMÁTICOS (Calculados en vivo)
    // ==========================================
    val costoTotalProduccionUsd: Double = 0.0,
    
    // Resultados
    val precioSugeridoUsd: Double = 0.0,
    val gananciaNetaUsd: Double = 0.0
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
    
    fun onCostoExtraChange(valor: String) {
        _uiState.update { it.copy(costoExtra = valor) }
        calcularResultados()
    }

    fun onMonedaExtraChange(moneda: String) {
        _uiState.update { it.copy(monedaExtra = moneda) }
        calcularResultados()
    }
    
    fun onRendimientoExtraChange(valor: String) {
        _uiState.update { it.copy(rendimientoExtra = valor) }
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

    fun onEsPlantillaMayorChange(valor: Boolean) {
        _uiState.update { it.copy(esPlantillaMayor = valor) }
    }

    fun onMinimoUnidadesMayorChange(valor: String) {
        if (valor.all { it.isDigit() }) {
            _uiState.update { it.copy(minimoUnidadesMayor = valor) }
        }
    }

    // --- ACCIONES SOBRE INSUMOS DINÁMICOS ---
    fun agregarInsumo(nuevoInsumo: com.example.gestor_deudores.data.database.InsumoCotizacion) {
        _uiState.update { actual ->
            val listaActualizada = actual.listaInsumos + nuevoInsumo
            actual.copy(listaInsumos = listaActualizada)
        }
        calcularResultados()
    }

    fun eliminarInsumo(idInsumo: String) {
        _uiState.update { actual ->
            val listaActualizada = actual.listaInsumos.filter { it.id != idInsumo }
            actual.copy(listaInsumos = listaActualizada)
        }
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
                costoExtra = plantilla.costoExtra.toString(),
                monedaExtra = plantilla.monedaExtra,
                rendimientoExtra = plantilla.rendimientoExtra.toString(),
                porcentajeOperativo = plantilla.porcentajeOperativo,
                porcentajeGanancia = plantilla.porcentajeGanancia,
                esPlantillaMayor = plantilla.esPlantillaMayor,
                minimoUnidadesMayor = plantilla.minimoUnidadesMayor.toString(),
                listaInsumos = try {
                    if (plantilla.costosAdicionalesJson.isNotBlank() && plantilla.costosAdicionalesJson != "[]") {
                        val type = object : com.google.gson.reflect.TypeToken<List<com.example.gestor_deudores.data.database.InsumoCotizacion>>() {}.type
                        com.google.gson.Gson().fromJson(plantilla.costosAdicionalesJson, type) ?: emptyList()
                    } else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
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
            costoExtra = parseD(estado.costoExtra),
            monedaExtra = estado.monedaExtra,
            rendimientoExtra = parseCant(estado.rendimientoExtra),
            porcentajeOperativo = estado.porcentajeOperativo,
            porcentajeGanancia = estado.porcentajeGanancia,
            esPlantillaMayor = estado.esPlantillaMayor,
            minimoUnidadesMayor = parseCant(estado.minimoUnidadesMayor),
            costosAdicionalesJson = com.google.gson.Gson().toJson(estado.listaInsumos)
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
    // CÁLCULO DE RESULTADOS PARA UNA PLANTILLA GUARDADA
    // ==========================================
    fun calcularResultadosPlantilla(plantilla: PlantillaCotizacion): ResultadoPlantilla {
        val costos = com.example.gestor_deudores.data.utils.calcularCostosPlantilla(plantilla, tasaBcvActual)

        return ResultadoPlantilla(
            costoTotalProduccionUsd = costos.costoTotalProduccionUsd,
            precioUsd = costos.precioSugeridoUsd,
            gananciaUsd = costos.gananciaUsd
        )
    }

    // ==========================================
    // EL CEREBRO MATEMÁTICO EN VIVO (Formulario)
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

        val totalExtra = aUsd(parseD(estado.costoExtra), estado.monedaExtra)
        val extraPorPieza = totalExtra / parseCant(estado.rendimientoExtra)

        val costoEstatico = costoUnitarioPieza + costoUnitarioEmpaque + costoUnitarioPapel + dtfPorPieza + transportePorPieza + disenoPorPieza + extraPorPieza
        val costoDinamico = estado.listaInsumos.sumOf { it.calcularCostoUnitarioUsd(tasaActiva) }
        val subtotalMateriales = costoEstatico + costoDinamico

        val costoOperativo = subtotalMateriales * (estado.porcentajeOperativo / 100.0)
        val costoTotalProduccion = subtotalMateriales + costoOperativo

        var precioVentaSug = 0.0
        var ganancia = 0.0
        if (costoTotalProduccion > 0.0) {
            val margenDecimal = estado.porcentajeGanancia / 100.0
            precioVentaSug = costoTotalProduccion / (1.0 - margenDecimal)
            ganancia = precioVentaSug - costoTotalProduccion
        }

        fun redondear2(valor: Double): Double = 
            BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toDouble()

        _uiState.update {
            it.copy(
                costoTotalProduccionUsd = redondear2(costoTotalProduccion),
                precioSugeridoUsd = redondear2(precioVentaSug),
                gananciaNetaUsd = redondear2(ganancia)
            )
        }
    }
}
