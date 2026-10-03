package com.example.gestor_deudores.ui.cotizador

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode

// Estado completo de la pantalla del Cotizador
data class CotizadorUiState(
    // 1. Materiales (Pieza Base)
    val precioPaquetePieza: String = "",
    val monedaPaquetePieza: String = "USD", // "USD" o "VES"
    val cantidadPaquetePieza: String = "1",
    
    // 2. Materiales (Empaque)
    val precioPaqueteEmpaque: String = "",
    val monedaPaqueteEmpaque: String = "USD",
    val cantidadPaqueteEmpaque: String = "1",

    // 3. Servicios Directos
    val costoDtf: String = "",
    val monedaDtf: String = "USD",
    val costoTransporte: String = "",
    val monedaTransporte: String = "USD",
    val costoDiseno: String = "",
    val monedaDiseno: String = "USD",

    // 4. Operatividad y Ganancia (Barras deslizables)
    val porcentajeOperativo: Float = 10f, // 10% por defecto para luz/desgaste
    val porcentajeGanancia: Float = 40f,  // 40% por defecto de margen DETAL
    val porcentajeGananciaMayor: Float = 25f, // 25% por defecto de margen al MAYOR

    // ==========================================
    // RESULTADOS MATEMÁTICOS (Calculados automáticamente)
    // ==========================================
    val costoTotalProduccionUsd: Double = 0.0,
    
    // Resultados al Detal
    val precioSugeridoUsd: Double = 0.0,
    val gananciaNetaUsd: Double = 0.0,
    
    // Resultados al Mayor (Ej. a partir de 6 piezas)
    val precioSugeridoMayorUsd: Double = 0.0,
    val gananciaNetaMayorUsd: Double = 0.0
)

class CotizadorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CotizadorUiState())
    val uiState: StateFlow<CotizadorUiState> = _uiState.asStateFlow()

    // --- FUNCIONES PARA ACTUALIZAR CADA CAMPO DESDE LA PANTALLA ---

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

    fun onCostoDtfChange(valor: String) {
        _uiState.update { it.copy(costoDtf = valor) }
        calcularResultados()
    }

    fun onMonedaDtfChange(moneda: String) {
        _uiState.update { it.copy(monedaDtf = moneda) }
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

    fun onCostoDisenoChange(valor: String) {
        _uiState.update { it.copy(costoDiseno = valor) }
        calcularResultados()
    }

    fun onMonedaDisenoChange(moneda: String) {
        _uiState.update { it.copy(monedaDiseno = moneda) }
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

    fun limpiarCotizador() {
        _uiState.value = CotizadorUiState()
    }

    // ==========================================
    // EL CEREBRO MATEMÁTICO
    // ==========================================
    // Ahora recibe la tasa del BCV actual para hacer conversiones en vivo
    fun calcularResultados(tasaBCV: Double = 1.0) {
        val estado = _uiState.value
        val tasaActiva = if (tasaBCV > 0.0) tasaBCV else 1.0

        // Función auxiliar para convertir String a Double seguro
        fun parseD(texto: String): Double = texto.replace(",", ".").toDoubleOrNull() ?: 0.0
        fun parseCant(texto: String): Double {
            val num = texto.toDoubleOrNull() ?: 1.0
            return if (num > 0) num else 1.0 // Nunca dividir por 0
        }

        // Función auxiliar para homogeneizar TODO a Dólares
        // Si el usuario ingresó el costo en Bolívares (VES), lo dividimos entre la tasa para saber su valor en USD
        fun aUsd(valor: Double, moneda: String): Double {
            return if (moneda == "VES") valor / tasaActiva else valor
        }

        // 1. Calculamos el costo unitario de los materiales en USD
        val costoPiezaBruto = parseD(estado.precioPaquetePieza) / parseCant(estado.cantidadPaquetePieza)
        val costoUnitarioPieza = aUsd(costoPiezaBruto, estado.monedaPaquetePieza)

        val costoEmpaqueBruto = parseD(estado.precioPaqueteEmpaque) / parseCant(estado.cantidadPaqueteEmpaque)
        val costoUnitarioEmpaque = aUsd(costoEmpaqueBruto, estado.monedaPaqueteEmpaque)

        // 2. Sumamos los servicios logísticos en USD
        val dtf = aUsd(parseD(estado.costoDtf), estado.monedaDtf)
        val transporte = aUsd(parseD(estado.costoTransporte), estado.monedaTransporte)
        val diseno = aUsd(parseD(estado.costoDiseno), estado.monedaDiseno)

        // 3. Subtotal de materia prima pura
        val subtotalMateriales = costoUnitarioPieza + costoUnitarioEmpaque + dtf + transporte + diseno

        // 4. Calculamos la operatividad (Luz, tinta, desgaste)
        // Ejemplo: Si el subtotal es $10 y operatividad es 10%, sumamos $1 por luz/tinta.
        val costoOperativo = subtotalMateriales * (estado.porcentajeOperativo / 100.0)

        // ESTE ES TU COSTO REAL FINAL POR PIEZA
        val costoTotalProduccion = subtotalMateriales + costoOperativo

        // 5. Fórmula Financiera de Rentabilidad Real: Precio = Costo / (1 - Margen)
        var precioVentaSug = 0.0
        var ganancia = 0.0
        
        var precioVentaMayorSug = 0.0
        var gananciaMayor = 0.0

        if (costoTotalProduccion > 0.0) {
            // Cálculo al Detal
            val margenDecimal = estado.porcentajeGanancia / 100.0
            precioVentaSug = costoTotalProduccion / (1.0 - margenDecimal)
            ganancia = precioVentaSug - costoTotalProduccion
            
            // Cálculo al Mayor
            val margenMayorDecimal = estado.porcentajeGananciaMayor / 100.0
            precioVentaMayorSug = costoTotalProduccion / (1.0 - margenMayorDecimal)
            gananciaMayor = precioVentaMayorSug - costoTotalProduccion
        }

        // 6. Redondeamos todo a 2 decimales para la pantalla
        fun redondear2(valor: Double): Double = 
            BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toDouble()

        // 7. Actualizamos el Estado de la Interfaz
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
