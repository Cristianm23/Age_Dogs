package com.example.agedogs.data.model

/**
 * Entidad 1/11: Usuario de la plataforma Age Dogs.
 * Almacenada en Firestore en la colección "usuarios".
 */
data class Usuario(
    val id: String = "",
    val email: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val fotoUrl: String = "",
    val rol: String = ROL_CLIENTE, // "cliente", "admin", "veterinario"
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    companion object {
        const val ROL_CLIENTE = "cliente"
        const val ROL_ADMIN = "admin"
        const val ROL_VETERINARIO = "veterinario"
    }
}
