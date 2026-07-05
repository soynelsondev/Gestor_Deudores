@file:kotlin.OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.Registro

import android.R
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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


@Composable
fun Principal(viewModel: RegistroDeudorViewModel){

    Scaffold (topBar = {toolbar()}){ innerPadding ->
        Column (modifier = Modifier.fillMaxSize().padding(innerPadding) .background(fondo))
        {
            datos_personales(viewModel)
            btnAceptar { viewModel.guardarUsuario() }
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
fun datos_personales(viewModel: RegistroDeudorViewModel){

    val estado by viewModel.uiState.collectAsState()

    Card(modifier = Modifier.fillMaxWidth() .height(450.dp)
        .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(fondo)
    ) {
        Column(modifier = Modifier.fillMaxSize() .padding(12.dp)) {
            Text(
                text = "Datos Personales",
                fontSize =35.sp,
                fontWeight = FontWeight.Bold,
                color = estados
            )

           textReutilizable(estado.nombre, textoFondo = "Nombre", KeyboardType.Text, alEscribir = { viewModel.onNombreChange(it)})
            textReutilizable(estado.apellido, textoFondo = "Apellido", KeyboardType.Text, alEscribir = { viewModel.onApellidoChange(it)})
            textReutilizable(estado.telefono, textoFondo = "Telefono", KeyboardType.Phone,"", Icons.Default.Phone,alEscribir = {viewModel.onTelefonoChange(it)})
            textReutilizable(estado.cedula, textoFondo = "Cedula", KeyboardType.Number,"V-", alEscribir = {viewModel.onCedulaChange(it)})
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
            "CONTINUAR ", color = Color.White, fontSize = 25.sp)
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
             focusedBorderColor = Color.Gray,
             unfocusedBorderColor = estados,
             unfocusedTextColor = estados,
             unfocusedContainerColor = Color.Transparent,
             focusedContainerColor = Color.Transparent
         )
    )
}
