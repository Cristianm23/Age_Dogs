package com.example.agedogs.data.model

/**
 * Entidad 7/11: Detalle de Pedido (Item comprado con snapshot de precio y nombre).
 */
data class DetallePedido(
    val productoId: String = "",
    val nombre: String = "",
    val presentacion: String = "",
    val precioUnitario: Int = 0,
    val cantidad: Int = 0,
    val subtotal: Int = 0
)

/**
 * Entidad 8/11: Pedido realizado por el usuario.
 */
data class Pedido(
    val id: String = "",
    val usuarioId: String = "",
    val items: List<DetallePedido> = emptyList(),
    val subtotal: Int = 0,
    val costoEnvio: Int = 0,
    val total: Int = 0,
    val direccionEnvio: String = "",
    val estado: String = ESTADO_PENDIENTE_PAGO, // PENDIENTE_PAGO, PAGADO, RECHAZADO, CANCELADO
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaExpiracionReserva: Long = System.currentTimeMillis() + (30 * 60 * 1000) // 30 min expiración
) {
    companion object {
        const val ESTADO_PENDIENTE_PAGO = "PENDIENTE_PAGO"
        const val ESTADO_PAGADO = "PAGADO"
        const val ESTADO_RECHAZADO = "RECHAZADO"
        const val ESTADO_CANCELADO = "CANCELADO"
    }
}
