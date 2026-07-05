package com.example.gestor_deudores.ui.Registro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestor_deudores.ui.theme.estado
import com.example.gestor_deudores.ui.theme.fondo


@Composable
fun Principal(){

}


@Preview(showSystemUi = true)
@Composable
fun datos_personales(){

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
                color = estado
            )

            textReutilizable("","Nombre",{})
            textReutilizable("","Apellido",{})
            textReutilizable("","Telefono",{})
            textReutilizable("","Cedula",{})



        }

    }
}


@Composable
fun textReutilizable(textoActual: String,
                     textoFondo: String,
                     alEscribir: (String)-> Unit ){

     OutlinedTextField(
        value = textoActual,
        onValueChange= alEscribir,
         placeholder= {
             Text(text = textoFondo, fontSize = 20.sp)
                      }, textStyle = TextStyle(fontSize = 18.sp),
         shape = RoundedCornerShape(18.dp) ,
         modifier = Modifier.fillMaxWidth() . padding(horizontal = 7.dp, vertical = 7.dp),
         colors = OutlinedTextFieldDefaults.colors(
             focusedBorderColor = estado,
             unfocusedTextColor = estado,
             unfocusedContainerColor = Color.Transparent,
             focusedContainerColor = Color.Transparent
         )
    )
}
