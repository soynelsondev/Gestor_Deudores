package com.example.gestor_deudores.data.api

import retrofit2.http.GET

interface Apitasas {
    // Buscamos solo el dólar oficial (BCV)
    @GET("v1/dolares/oficial")
    suspend fun obtenerDolarOficial(): MonedaOficial
}
