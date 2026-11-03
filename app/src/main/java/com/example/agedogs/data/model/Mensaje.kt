package com.example.agedogs.data.model

/**
 * Entidad 11/11: Mensaje dentro de una subcolección de conversación.
 */
data class Mensaje(
    val id: String = "",
    val remitenteId: String = "",
    val texto: String = "",
    val fechaHora: Long = System.currentTimeMillis(),
    val leido: Boolean = false
)
