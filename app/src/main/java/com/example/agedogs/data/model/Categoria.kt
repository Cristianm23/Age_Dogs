package com.example.agedogs.data.model

/**
 * Entidad 3/11: Categorías permitidas (Únicamente alimentos para perros).
 */
data class Categoria(
    val id: String = "",
    val nombre: String = "" // "Concentrado", "Alimento húmedo", "Snacks comestibles caninos"
) {
    companion object {
        val CATEGORIAS_VALIDAS = listOf("Concentrado", "Alimento húmedo", "Snacks comestibles caninos")
    }
}
