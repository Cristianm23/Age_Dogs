package com.example.agedogs.data.model

/**
 * Entidad 9/11: Registro de pago de transacción (Wompi Sandbox).
 * Nunca almacena números de tarjeta ni datos sensibles.
 */
data class Pago(
    val id: String = "",
    val pedidoId: String = "",
    val proveedor: String = "Wompi",
    val referencia: String = "",
    val idTransaccion: String = "",
    val estado: String = ESTADO_PENDIENTE, // PENDING, APPROVED, DECLINED, ERROR, VOIDED
    val montoCentavos: Long = 0L,
    val moneda: String = "COP",
    val metodo: String = "CARD", // CARD, PSE, NEQUI
    val fecha: Long = System.currentTimeMillis(),
    val respuestaResumen: String = ""
) {
    companion object {
        const val ESTADO_PENDIENTE = "PENDIENTE"
        const val ESTADO_APPROVED = "APPROVED"
        const val ESTADO_DECLINED = "DECLINED"
        const val ESTADO_ERROR = "ERROR"
        const val ESTADO_VOIDED = "VOIDED"
    }
}
