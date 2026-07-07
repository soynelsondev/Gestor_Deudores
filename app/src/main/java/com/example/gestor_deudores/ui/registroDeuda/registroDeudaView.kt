@file:kotlin.OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.registroDeuda

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestor_deudores.ui.Registro.AvatarUsuario
import com.example.gestor_deudores.ui.Registro.RegistroDeudorViewModel
import com.example.gestor_deudores.ui.Registro.btnAceptar
import com.example.gestor_deudores.ui.Registro.datos_personales
import com.example.gestor_deudores.ui.Registro.textReutilizable
import com.example.gestor_deudores.ui.Registro.toolbar
import com.example.gestor_deudores.ui.theme.estados
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun Principal(viewModel: RDeudaViewModel, onNavegarAtras: () -> Unit){
    val estado by viewModel.uiEstado.collectAsState()

    LaunchedEffect(estado.guardadoExitoso) {
        if (estado.guardadoExitoso) {
            viewModel.reiniciarEstadoGuardado()
            onNavegarAtras() // Cerramos la pantalla y volvemos
        }
    }
    Scaffold (topBar = { toolbar2() }){ innerPadding ->
        Column (modifier = Modifier.fillMaxSize().padding(innerPadding) .background(fondo))
        {
            datos_prestamo(viewModel)
            btnDeuda { viewModel.GuardarDeuda() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun toolbar2(){
    CenterAlignedTopAppBar( // <-- CAMBIADO PARA CENTRAR EL TÍTULO
        title = { Text("NUEVO DEUDOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = estados)
    )

}


@Composable
fun datos_prestamo(viewModel: RDeudaViewModel) {

    val estado by viewModel.uiEstado.collectAsState()
    // Variables para controlar el popup del calendario
    var mostrarCalendario by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // SI LA VARIABLE ES TRUE, DIBUJAMOS EL CALENDARIO EN PANTALLA
    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    mostrarCalendario = false
                    // Transformamos los milisegundos a fecha normal (ej. 24/05/2026)
                    val fechaSeleccionada = datePickerState.selectedDateMillis?.let { millis ->
                        val formateador = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formateador.format(Date(millis))
                    } ?: ""

                    viewModel.onFechaChange(fechaSeleccionada)
                }) {
                    Text("Aceptar", color = estados)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp, vertical = 25.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(fondo)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {


            Text(
                text = "DETALLES DEL PRESTAMO",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = estados,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // 1. MONTO INICIAL ($ a la izquierda)
            textReutilizable(
                textoActual = estado.monto,
                textoFondo = "Monto Inicial",
                tipoTeclado = KeyboardType.Decimal, // Teclado numérico con punto decimal
                alEscribir = { entrada ->
                    // Reemplaza puntos por comas y valida que solo exista una coma decimal
                    val procesado = entrada.replace('.', ',')
                    if (procesado.count { it == ',' } <= 1) {
                        viewModel.onMontoChange(procesado)
                    }
                }
            )

            textReutilizable(
                textoActual = estado.tipoDeuda,
                textoFondo = "Tipo de Préstamo",

                alEscribir = { viewModel.onTipoDeudaChange(it) }
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(0.65f)) { // <-- CAJA REDUCIDA PARA EVITAR UNIFORMIDAD
                    textReutilizable(
                        textoActual = estado.fecha,
                        textoFondo = "Fecha",
                        iconoDerecho = Icons.Default.DateRange,
                        readOnly = true, // <-- PROTEGE EL INPUT DE ESCRITURA MANUAL Y LETRAS
                        onIconoDerechoClick = { mostrarCalendario = true },
                        alEscribir = {}
                    )
                }
            }

            // 4. DESCRIPCIÓN
            textReutilizable(
                textoActual = estado.descripcion,
                textoFondo = "Descripción",
                iconoDerecho = Icons.Default.Info,
                alEscribir = { viewModel.onDescripcionChange(it) }
            )

            Text(
                text = "ROL",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = estados,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp, top = 12.dp)
            )

            val opcionesRol = listOf("CLIENTE", "FAMILIAR", "OTRO")

            Row(
                modifier = Modifier
                    .fillMaxWidth() .padding(bottom = 10.dp)
                    .height(45.dp) // Altura del selector
                    .clip(RoundedCornerShape(50))
                    .background(fondo2),
                    verticalAlignment = Alignment.CenterVertically
            ){
                opcionesRol.forEach { opcion ->
                    val seleccionado = estado.rol == opcion

                    Box(
                        modifier = Modifier
                            .weight(1f) // Divide el ancho equitativamente (33% cada uno)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            // Si está seleccionado, se pinta de tu color "estados", si no, transparente
                            .background(if (seleccionado) estados else Color.Transparent)
                            .clickable { viewModel.onRolChange(opcion) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opcion,
                            // Texto blanco si está seleccionado, de lo contrario color oscuro
                            color = if (seleccionado) Color.White else estados,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }


        }

    }

}




@Composable
fun textReutilizable(
    textoActual: String,
    textoFondo: String,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    textoPrefijo: String? = null,
    iconoIzquierdo: ImageVector? = null, // El logo normal de la izquierda
    iconoDerecho: ImageVector? = null,   // NUEVO: El logo de la derecha (Calendario/Notas)
    readOnly: Boolean = false, // <-- NUEVO PARÁMETRO PARA CONTROLAR LA EDICIÓN
    onIconoDerechoClick: (() -> Unit)? = null, // NUEVO: Acción al tocar el icono derecho
    alEscribir: (String) -> Unit
) {
    OutlinedTextField(
        value = textoActual,
        onValueChange = alEscribir,
        readOnly = readOnly, // <-- SE ASIGNA AL OUTLINEDTEXTFIELD
        placeholder = {
            Text(text = textoFondo, fontSize = 18.sp)
        },
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp),
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),

        prefix = if (textoPrefijo != null) {
            { Text(text = textoPrefijo, fontSize = 18.sp, color = Color(0xFF005b52)) }
        } else null,

        // MAGIA DEL LOGO IZQUIERDO (Ej. Signo de Dólar)
        leadingIcon = if (iconoIzquierdo != null) {
            { Icon(imageVector = iconoIzquierdo, contentDescription = null, tint = estados) }
        } else null,

        // MAGIA DEL LOGO DERECHO (Ej. Calendario)
        trailingIcon = if (iconoDerecho != null) {
            {
                // Envolvemos el icono en un IconButton para que reaccione al toque
                IconButton(onClick = { onIconoDerechoClick?.invoke() }) {
                    Icon(imageVector = iconoDerecho, contentDescription = null, tint = estados)
                }
            }
        } else null,

        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp, vertical = 7.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = estados,
            unfocusedBorderColor = fondo2,
            unfocusedTextColor = fondo2,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        )
    )
}

@Composable
fun btnDeuda(
    alPresionar: () -> Unit
) {

    Button(
        onClick = alPresionar, modifier = Modifier.fillMaxWidth()
            .height(70.dp).padding(horizontal = 35.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = estados)
    )
    {
        Text(
            "REGISTRAR DEUDA ", color = Color.White, fontSize = 25.sp
        )
    }
}
