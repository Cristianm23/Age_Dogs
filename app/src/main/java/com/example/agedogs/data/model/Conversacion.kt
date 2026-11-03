package com.example.agedogs.data.model

/**
 * Entidad 10/11: Conversación entre dos usuarios.
 */
data class Conversacion(
    val id: String = "",
    val participantes: List<String> = emptyList(), // Lista de exactamente 2 UIDs
    val ultimoMensaje: String = "",
    val ultimoMensajeFecha: Long = System.currentTimeMillis(),
    val fechaCreacion: Long = System.currentTimeMillis()
)
