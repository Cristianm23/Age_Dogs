package com.example.agedogs.data.model

data class ItemCarrito(
    val productoId: String = "",
    val nombre: String = "",
    val marca: String = "",
    val imagenUrl: String = "",
    val presentacion: String = "",
    val precioUnitario: Int = 0,
    val cantidad: Int = 1
) {
    val subtotal: Int get() = precioUnitario * cantidad
}

/**
 * Entidad 6/11: Carrito de compras del usuario.
 */
data class Carrito(
    val usuarioId: String = "",
    val items: List<ItemCarrito> = emptyList(),
    val direccionEnvio: String = "",
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

data class ConfigEnvio(
    val tarifaFija: Int = 10000, // COP
    val umbralEnvioGratis: Int = 100000 // COP
)
