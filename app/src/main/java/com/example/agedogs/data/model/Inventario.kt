package com.example.agedogs.data.model

/**
 * Entidad 5/11: Inventario de stock para cada producto.
 */
data class Inventario(
    val productoId: String = "",
    val cantidadDisponible: Int = 0,
    val stockMinimo: Int = 5,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)
