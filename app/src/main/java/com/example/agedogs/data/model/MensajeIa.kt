package com.example.agedogs.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MensajeIa(
    val id: String = "",
    val usuarioId: String = "",
    val pregunta: String = "",
    val respuesta: String = "",
    val urgencia: Boolean = false,
    val motivo: String? = null,
    val fecha: Long = System.currentTimeMillis()
)

@Serializable
data class AiRequest(
    val mensaje: String,
    val perroContexto: String? = null,
    val sistemaPrompt: String
)

@Serializable
data class AiResponse(
    val respuesta: String,
    val urgencia: Boolean,
    val motivo: String? = null
)
