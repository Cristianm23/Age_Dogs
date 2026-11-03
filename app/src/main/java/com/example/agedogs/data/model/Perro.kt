package com.example.agedogs.data.model

/**
 * Entidad 2/11: Perro registrado por el usuario en la colección "perros".
 */
data class Perro(
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val raza: String = "Mestizo",
    val edadAnios: Double = 0.0,
    val pesoKg: Double = 10.0,
    val sexo: String = "Macho", // "Macho" o "Hembra"
    val descripcion: String = "", // Máx. 300 caracteres
    val fotos: List<String> = emptyList(), // Máximo 5 URLs
    val fechaRegistro: Long = System.currentTimeMillis()
)
