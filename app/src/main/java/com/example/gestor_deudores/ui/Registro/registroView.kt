@file:kotlin.OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.Registro

import android.R
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestor_deudores.ui.theme.estados
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2


@Composable
fun Principal(viewModel: RegistroDeudorViewModel,onNavegarADeuda: (Int) -> Unit){

    val estado by viewModel.uiState.collectAsState()
    LaunchedEffect(estado.nuevoDeudorId) {
        if (estado.nuevoDeudorId > 0) {
            // Pasamos el ID hacia afuera (hacia el NavHost)
            onNavegarADeuda(estado.nuevoDeudorId)

            // IMPORTANTE: Dile a tu ViewModel que limpie el ID (vuelva a 0)
            // para que no se quede pegado navegando en bucle si la pantalla se recrea.
            viewModel.resetNuevoDeudorId()
        }
    }


    Scaffold (topBar = {toolbar()}){ innerPadding ->
        Column (modifier = Modifier.fillMaxSize().padding(innerPadding) .background(fondo))
        {
            AvatarUsuario()
            datos_personales(viewModel)
            btnAceptar { viewModel.guardarUsuario() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun toolbar(){
    CenterAlignedTopAppBar( // <-- CAMBIADO PARA CENTRAR EL TÍTULO
        title = { Text("NUEVO DEUDOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = estados)
    )

}

@Preview(showBackground = true)
@Composable
fun AvatarUsuario() {
    // Este Box externo hace que el círculo se centre automáticamente en la pantalla
    Box(
        modifier = Modifier.fillMaxWidth() .padding(top = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Este Box es el círculo de fondo
        Box(
            modifier = Modifier
                .size(110.dp) // Tamaño del círculo
                .clip(CircleShape)
                .background(Color(0xFFE0F2F1)), // Aquí cambias el color del fondo circular
            contentAlignment = Alignment.Center
        ) {
            // El vector nativo de Android Studio
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar estándar",
                modifier = Modifier.size(110.dp), // Tamaño del vector interior
                tint = estados // Aquí cambias el color de la silueta (o usas 'estados')
            )
        }
    }
}

@Composable
fun datos_personales(viewModel: RegistroDeudorViewModel){

    val estado by viewModel.uiState.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()
        .padding(16.dp,vertical=25.dp) ,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(fondo)
    ) {
        Column(modifier = Modifier.fillMaxWidth() .padding(12.dp)) {

           // AvatarUsuario()
            Text(
                text = "Datos Personales",
                fontSize =30.sp,
                fontWeight = FontWeight.Bold,
                color = estados,
                modifier = Modifier.padding(vertical = 10.dp)
            )



            textReutilizable(
                textoActual = estado.nombre,
                textoFondo = "Nombre",
                tipoTeclado = KeyboardType.Text,
                alEscribir = { entrada ->
                    if (entrada.all { it.isLetter() || it.isWhitespace() }) {
                        viewModel.onNombreChange(entrada)
                    }
                }
            )

            // 2. Apellido: Igual que el nombre
            textReutilizable(
                textoActual = estado.apellido,
                textoFondo = "Apellido",
                tipoTeclado = KeyboardType.Text,
                alEscribir = { entrada ->
                    if (entrada.all { it.isLetter() || it.isWhitespace() }) {
                        viewModel.onApellidoChange(entrada)
                    }
                }
            )

            // 3. Teléfono: Bloqueo total. Solo pasa si TODOS son números (bloquea puntos y comas)
            textReutilizable(
                textoActual = estado.telefono,
                textoFondo = "Telefono",
                tipoTeclado = KeyboardType.Phone,
                icono = Icons.Default.Phone,
                alEscribir = { entrada ->
                    if (entrada.all { it.isDigit() }) {
                        viewModel.onTelefonoChange(entrada)
                    }
                }
            )

            // 4. Cédula: Solo números
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(0.70f)) {
                    textReutilizable(
                        textoActual = estado.cedula,
                        textoFondo = "Cedula",
                        tipoTeclado = KeyboardType.Number,
                        textoPrefijo = "V-",
                        alEscribir = { entrada ->
                            if (entrada.all { it.isDigit() }) {
                                viewModel.onCedulaChange(entrada)
                            }
                        }
                    )
                }
            }
        }

    }
}


@Composable
fun btnAceptar(
    alPresionar: () -> Unit
){

    Button(onClick = alPresionar, modifier = Modifier.fillMaxWidth()
        .height(70.dp) .padding(horizontal = 35.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = estados))
    {
        Text(
            "CONTINUAR", color = Color.White, fontSize = 25.sp)
    }

}
@Composable
fun textReutilizable(textoActual: String,
                     textoFondo: String,
                     tipoTeclado: KeyboardType = KeyboardType.Text, // Por defecto teclado normal de letras
                     textoPrefijo: String? = null,
                     icono: ImageVector? = null,
                     alEscribir: (String)-> Unit ){

     OutlinedTextField(
        value = textoActual,
        onValueChange= alEscribir,
         placeholder= {
             Text(text = textoFondo, fontSize = 20.sp)
                      }, textStyle = TextStyle(fontSize = 18.sp),
         // --- AQUÍ ESTÁ LA MAGIA DEL TECLADO ---
         keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),

         // --- AQUÍ ESTÁ LA MAGIA DEL PREFIJO (V-) ---
         prefix = if (textoPrefijo != null) {
             { Text(text = textoPrefijo, fontSize = 18.sp, color = Color(0xFF005b52)) }
         } else null,
         // --- MAGIA DEL LOGO (Teléfono) ---
         leadingIcon = if (icono != null) {
             { Icon(imageVector = icono, contentDescription = null, tint = estados) }
         } else null,
         shape = RoundedCornerShape(18.dp) ,
         modifier = Modifier.fillMaxWidth() . padding(horizontal = 7.dp, vertical = 7.dp),
         colors = OutlinedTextFieldDefaults.colors(
             focusedBorderColor = estados,
             unfocusedBorderColor = fondo2,
             unfocusedTextColor = Color.Black,
             unfocusedContainerColor = Color.Transparent,
             focusedContainerColor = Color.Transparent
         )
    )
}
