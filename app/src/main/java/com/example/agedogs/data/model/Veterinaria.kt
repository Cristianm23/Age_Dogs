package com.example.agedogs.data.model

data class Veterinaria(
    val id: String,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val lat: Double,
    val lon: Double,
    val es24Horas: Boolean,
    val distanciaKm: Double = 0.0,
    val abierta: Boolean = true
)
