package com.example.agedogs.data.repository

import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.Pago
import com.example.agedogs.data.model.Pedido
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PagoRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
) {

    suspend fun iniciarTransaccionPago(pedido: Pedido, metodo: String): Result<Pago> {
        return try {
            val referencia = "AGEDOGS-${pedido.id}-${System.currentTimeMillis()}"
            val montoCentavos = (pedido.total * 100).toLong()

            val pago = Pago(
                id = "",
                pedidoId = pedido.id,
                proveedor = "Wompi",
                referencia = referencia,
                idTransaccion = "TRX-${System.currentTimeMillis()}",
                estado = Pago.ESTADO_PENDIENTE,
                montoCentavos = montoCentavos,
                moneda = "COP",
                metodo = metodo,
                respuestaResumen = "Transacción creada en Sandbox Wompi"
            )

            val docRef = firestore.collection("pagos").document()
            val pagoToSave = pago.copy(id = docRef.id)
            docRef.set(pagoToSave).await()

            Result.success(pagoToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun simularWebhookWompi(pagoId: String, pedidoId: String, nuevoEstadoPago: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val pagoRef = firestore.collection("pagos").document(pagoId)
                val pedidoRef = firestore.collection("pedidos").document(pedidoId)

                transaction.update(pagoRef, "estado", nuevoEstadoPago)

                val estadoPedido = when (nuevoEstadoPago) {
                    Pago.ESTADO_APPROVED -> Pedido.ESTADO_PAGADO
                    Pago.ESTADO_DECLINED, Pago.ESTADO_ERROR, Pago.ESTADO_VOIDED -> Pedido.ESTADO_RECHAZADO
                    else -> Pedido.ESTADO_PENDIENTE_PAGO
                }

                transaction.update(pedidoRef, "estado", estadoPedido)

                // Si es rechazado, liberar stock de inventario
                if (estadoPedido == Pedido.ESTADO_RECHAZADO) {
                    val pedidoSnap = transaction.get(pedidoRef)
                    val pedido = pedidoSnap.toObject(Pedido::class.java)
                    pedido?.items?.forEach { item ->
                        val invRef = firestore.collection("inventario").document(item.productoId)
                        val invSnap = transaction.get(invRef)
                        val inv = invSnap.toObject(Inventario::class.java)
                        if (inv != null) {
                            val stockRestaurado = inv.cantidadDisponible + item.cantidad
                            transaction.update(invRef, "cantidadDisponible", stockRestaurado)
                        }
                    }
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
