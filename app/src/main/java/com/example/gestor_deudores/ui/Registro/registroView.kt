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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
            // Quitamos el btnAceptar de aquí porque necesitamos el estado interno de datos_personales
            datos_personales(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun toolbar(titulo: String = "NUEVO DEUDOR"){
    CenterAlignedTopAppBar( // <-- CAMBIADO PARA CENTRAR EL TÍTULO
        title = { Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
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



            // Variables compartidas para la cédula (deben estar al inicio de datos_personales)
            var tipoCedula by remember { mutableStateOf("V-") }

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
                textoFondo = "Telefono (Opcional)",
                tipoTeclado = KeyboardType.Phone,
                icono = Icons.Default.Phone,
                alEscribir = { entrada ->
                    if (entrada.all { it.isDigit() }) {
                        viewModel.onTelefonoChange(entrada)
                    }
                }
            )

            // Si escribe un teléfono, que sea válido
            if (estado.telefono.isNotEmpty() && estado.telefono.length !in 10..11) {
                Text(
                    text = "El teléfono debe tener 10 u 11 dígitos",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                )
            }

            // 4. Cédula: Selector V/E/J + Solo números
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                
                // Selector V/E/J
                var expandido by remember { mutableStateOf(false) }
                val tipos = listOf("V-", "E-", "J-")

                Box {
                    TextButton(onClick = { expandido = true }) {
                        Text(text = tipoCedula, fontSize = 18.sp, color = estados, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        tipos.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    tipoCedula = tipo
                                    expandido = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = estado.cedula,
                        onValueChange = { entrada ->
                            if (entrada.all { it.isDigit() }) {
                                viewModel.onCedulaChange(entrada)
                            }
                        },
                        placeholder = { Text("Cedula", fontSize = 20.sp) },
                        textStyle = TextStyle(fontSize = 18.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = estados,
                            unfocusedBorderColor = fondo2,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            btnAceptar(
                tipoCedula = tipoCedula,
                cedulaActual = estado.cedula,
                onCedulaModificada = { viewModel.onCedulaChange(it) },
                alPresionar = { viewModel.guardarUsuario() }
            )
        }

    }
}


@Composable
fun btnAceptar(
    tipoCedula: String,
    cedulaActual: String,
    onCedulaModificada: (String) -> Unit,
    alPresionar: () -> Unit
){

    Button(onClick = { 
        if (cedulaActual.isNotEmpty() && !cedulaActual.startsWith("V-") && !cedulaActual.startsWith("E-") && !cedulaActual.startsWith("J-")) {
            onCedulaModificada("$tipoCedula$cedulaActual")
        }
        alPresionar() 
    }, modifier = Modifier.fillMaxWidth()
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
