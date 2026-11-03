package com.example.agedogs.data.model

/**
 * Entidad 4/11: Producto de alimento para perros.
 */
data class Producto(
    val id: String = "",
    val nombre: String = "",
    val marca: String = "",
    val descripcion: String = "",
    val presentacion: String = "", // ej. "Bulto 15 kg", "Lata 350g"
    val precio: Int = 0, // Entero en COP
    val categoriaId: String = "", // Debe pertenecer a Categorias válidas
    val imagenUrl: String = "",
    val activo: Boolean = true
)
