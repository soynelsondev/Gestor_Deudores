@file:kotlin.OptIn(ExperimentalMaterial3Api::class)
package com.example.gestor_deudores.ui.home

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestor_deudores.data.Deuda
import com.example.gestor_deudores.data.Deudor
import com.example.gestor_deudores.ui.Registro.AvatarUsuario
import com.example.gestor_deudores.ui.Registro.btnAceptar
import com.example.gestor_deudores.ui.Registro.datos_personales
import com.example.gestor_deudores.ui.Registro.toolbar
import com.example.gestor_deudores.ui.theme.componentes
import com.example.gestor_deudores.ui.theme.estados
import com.example.gestor_deudores.ui.theme.fondo
import com.example.gestor_deudores.ui.theme.fondo2
import com.example.gestor_deudores.ui.theme.fondo_claro


@Composable
fun homePrincipal(viewModel: HomeViewModel){
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
    val pestañaActual by viewModel.pestañaActual.collectAsState()
    val listaDeudores by viewModel.deudorFiltrados.collectAsState()

    Scaffold (topBar = {toolbar()}){ innerPadding ->

        Column (modifier = Modifier.fillMaxSize().padding(innerPadding) .background(fondo))
        {
            BuscadorDeudores(
                textoActual = textoBusqueda,
                onTextoCambiado = { nuevoTexto -> viewModel.actualizarBuscador(nuevoTexto) }
            )
            SelectorPestañas(
                pestañaActual = pestañaActual,
                onPestañaSeleccionada = { nuevaPestaña -> viewModel.cambiarPestaña(nuevaPestaña) }
            )
// 5. La lista que dibuja las tarjetas automáticamente
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Iteramos sobre la lista de "paquetes" que nos mandó el ViewModel
                items(listaDeudores) { paquete ->

                    // Le entregamos a la tarjeta exactamente lo que pide sacándolo del paquete
                    carDeudores(
                        deudor = paquete.deudor,
                        deuda = paquete.deuda,
                        montoRestante = paquete.montoRestante
                    )

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun toolbar(){
    CenterAlignedTopAppBar( // <-- CAMBIADO PARA CENTRAR EL TÍTULO
        title = { Text("NUEVO DEUDOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = fondo2)
    )

}


@Composable
fun SelectorPestañas(
    pestañaActual: Pestaña, // La pestaña que el ViewModel dice que está activa
    onPestañaSeleccionada: (Pestaña) -> Unit // La función para avisarle al ViewModel que tocamos otra
) {



    val colorTextoInactivo = Color.Gray // Gris para texto inactivo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(fondo2, shape = RoundedCornerShape(50))
            .padding(4.dp), // Pequeño espacio interno para que el botón no toque los bordes
        verticalAlignment = Alignment.CenterVertically
    ) {

        // --- BOTÓN PENDIENTES ---
        Box(
            modifier = Modifier
                .weight(1f) // Magia: Esto hace que ocupe exactamente el 50% del ancho
                .clip(RoundedCornerShape(50))
                .background(
                    // Condición: Si está seleccionada, pintamos el fondo, si no, transparente
                    if (pestañaActual == Pestaña.PENDIENTES) fondo_claro else Color.Transparent
                )
                .clickable { onPestañaSeleccionada(Pestaña.PENDIENTES) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PENDIENTES",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (pestañaActual == Pestaña.PENDIENTES) componentes else colorTextoInactivo
            )
        }

        // --- BOTÓN HISTORIAL ---
        Box(
            modifier = Modifier
                .weight(1f) // Magia: El otro 50% del ancho
                .clip(RoundedCornerShape(50))
                .background(
                    // Condición: Si está seleccionada, pintamos el fondo, si no, transparente
                    if (pestañaActual == Pestaña.HISTORIAL) componentes else Color.Transparent
                )
                .clickable { onPestañaSeleccionada(Pestaña.HISTORIAL) }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HISTORIAL",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (pestañaActual == Pestaña.HISTORIAL) estados else colorTextoInactivo
            )
        }
    }
}


@Composable
fun BuscadorDeudores(
    textoActual: String, // El texto que se va a mostrar
    onTextoCambiado: (String) -> Unit // La función que avisa que el usuario escribió algo
) {
    TextField(
        value = textoActual,
        onValueChange = { nuevoTexto -> onTextoCambiado(nuevoTexto) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(text = "Buscar deudor...", color = Color.Gray) },

        // El icono de la lupita a la izquierda
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Icono de buscar",
                tint = Color.Gray
            )
        },

        // (Opcional) Un botón de "X" a la derecha para borrar rápido si hay texto
        trailingIcon = {
            if (textoActual.isNotEmpty()) {
                IconButton(onClick = { onTextoCambiado("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Borrar texto",
                        tint = Color.Gray
                    )
                }
            }
        },

        // Bordes bien redondeados
        shape = RoundedCornerShape(50),

        // Colores personalizados (Aquí puedes usar 'fondo2' o tus variables de color)
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fondo2, // Ejemplo de un color de tu paleta
            unfocusedContainerColor = fondo2,
            // Esto quita la línea fea de abajo del TextField
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}


@Composable
fun carDeudores(deudor: Deudor,deuda: Deuda,montoRestante: Double){

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // --- PARTE SUPERIOR: Avatar, Nombre y Rol ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                //avatar
                avatar(deudor)

                Spacer(modifier = Modifier.width(12.dp))
                // 2. Nombre y etiqueta de rol
                Column {
                    Text(
                        text = "${deudor.nombre} ${deudor.apellido}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    // La etiqueta redondita de "CLIENTE"
                    Box(
                        modifier = Modifier
                            .background(fondo2, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = deuda.rol.uppercase(),
                            fontSize = 10.sp,
                            color = estados,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PARTE INFERIOR: Detalles de la deuda ---
            Text(text = "Monto Restante:", fontSize = 12.sp, color = Color.Gray)
            // Colocamos la variable del monto matemático
            Text(
                text = "${montoRestante}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = componentes
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Colocamos el resto de los datos dinámicos concatenando textos
            Text(text = "Tipo: ${deuda.tipoDeuda}", fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Fecha: ${deuda.fecha}", fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Teléfono: ${deudor.telf}", fontSize = 14.sp, color = Color.DarkGray)

            // Validamos que si no hay descripción, no se vea feo
            if (deuda.descripcion.isNotBlank()) {
                Text(text = "Descripción: ${deuda.descripcion}", fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}




@Composable
fun avatar(deudor: Deudor){
    // 1. El Avatar (Círculo con iniciales)
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(estados, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val iniciales = "${deudor.nombre.firstOrNull()?.uppercase() ?: ""}${deudor.apellido.firstOrNull()?.uppercase() ?: ""}"
        Text(text = iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}