package com.example.gestor_deudores.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestor_deudores.data.DeudaDao
import com.example.gestor_deudores.data.DeudorDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class Pestaña{
    PENDIENTES,
    HISTORIAL
}


class HomeViewModel(private val dao: DeudorDao,private val dao2: DeudaDao) : ViewModel() {



    val listaDeudores = dao.obtenerDeudores().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue= emptyList()
    )

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda = _textoBusqueda.asStateFlow()
    private val _pestañaActual = MutableStateFlow(Pestaña.PENDIENTES)
    val pestañaActual = _pestañaActual.asStateFlow()

    fun actualizarBuscador(nuevoTexto: String){
        _textoBusqueda.value   = nuevoTexto
    }

    // buscadore de deudores
    val deudorFiltrados = combine(
        listaDeudores,
        _textoBusqueda,
        _pestañaActual,
        dao2.obtenerTodasLasDeudas()
    ){
        listaDeDeudores, texto, pestaña,deuda ->

        val listaPorPestaña = listaDeDeudores.filter {
            deudor ->
            val deudasDeEstaPersona = deuda.filter { it.idDeudor == deudor.id  }

           val deudaTotal = deudasDeEstaPersona.sumOf { it.montoRestante }

            if (pestaña == Pestaña.PENDIENTES){
                deudaTotal >0.0
            }else   {
                deudaTotal <=0.0
            }

        }

        if(texto.isBlank()){
            listaPorPestaña
        }else{

                listaPorPestaña.filter { deudor ->
                    deudor.nombre.contains(texto, ignoreCase = true) ||
                            deudor.apellido.contains(texto, ignoreCase = true)
            }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    fun cambiarPestaña(nuevaPestaña: Pestaña){
        _pestañaActual.value = nuevaPestaña
    }





}

