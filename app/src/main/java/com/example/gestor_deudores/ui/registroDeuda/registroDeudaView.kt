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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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


@Composable
fun Principal(viewModel: RDeudaViewModel){
    Scaffold (topBar = { toolbar() }){ innerPadding ->
        Column (modifier = Modifier.fillMaxSize().padding(innerPadding) .background(fondo))
        {
            datos_prestamo(viewModel)
            btnDeuda { viewModel.GuardarDeuda() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun toolbar(){
    TopAppBar(
        title = {Text("Nuevo Deudor", color = Color.White)},
        colors = TopAppBarDefaults.topAppBarColors(fondo)
    )

}


@Composable
fun datos_prestamo(viewModel: RDeudaViewModel) {

    val estado by viewModel.uiEstado.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth().height(450.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(fondo)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {


            Text(
                text = "DETALLES DEL PRESTAMO",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = estados
            )

            // 1. MONTO INICIAL ($ a la izquierda)
            textReutilizable(
                textoActual = estado.monto,
                textoFondo = "Monto Inicial",
                tipoTeclado = KeyboardType.Decimal, // Teclado numérico con punto decimal
                alEscribir = { viewModel.onMontoChange(it) }
            )

            textReutilizable(
                textoActual = estado.tipoDeuda,
                textoFondo = "Tipo de Préstamo",
                iconoDerecho = Icons.Default.ArrowDropDown,
                alEscribir = { viewModel.onTipoDeudaChange(it) }
            )

            textReutilizable(
                textoActual = estado.fecha,
                textoFondo = "Fecha",
                iconoDerecho = Icons.Default.DateRange,
                onIconoDerechoClick = {
                    // Lógica para el DatePickerDialog
                    println("Clic en el calendario")
                },
                alEscribir = { viewModel.onFechaChange(it) }
            )

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
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            val opcionesRol = listOf("CLIENTE", "FAMILIAR", "OTRO")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                            fontSize = 12.sp,
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
    onIconoDerechoClick: (() -> Unit)? = null, // NUEVO: Acción al tocar el icono derecho
    alEscribir: (String) -> Unit
) {
    OutlinedTextField(
        value = textoActual,
        onValueChange = alEscribir,
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
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = estados,
            unfocusedTextColor = estados,
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
