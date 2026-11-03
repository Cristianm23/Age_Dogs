package com.example.agedogs.data.repository

import com.example.agedogs.data.model.Carrito
import com.example.agedogs.data.model.ConfigEnvio
import com.example.agedogs.data.model.DetallePedido
import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.ItemCarrito
import com.example.agedogs.data.model.Pedido
import com.example.agedogs.data.model.Producto
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PedidoRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
) {

    suspend fun obtenerConfigEnvio(): ConfigEnvio {
        return try {
            val doc = firestore.collection("configuracion").document("envio").get().await()
            if (doc.exists()) {
                val tarifa = doc.getLong("tarifaFija")?.toInt() ?: 10000
                val umbral = doc.getLong("umbralEnvioGratis")?.toInt() ?: 100000
                ConfigEnvio(tarifa, umbral)
            } else {
                val defaultConfig = ConfigEnvio(10000, 100000)
                firestore.collection("configuracion").document("envio").set(defaultConfig).await()
                defaultConfig
            }
        } catch (e: Exception) {
            ConfigEnvio(10000, 100000)
        }
    }

    suspend fun obtenerCarrito(usuarioId: String): Carrito {
        return try {
            val doc = firestore.collection("carritos").document(usuarioId).get().await()
            doc.toObject(Carrito::class.java) ?: Carrito(usuarioId = usuarioId)
        } catch (e: Exception) {
            Carrito(usuarioId = usuarioId)
        }
    }

    suspend fun guardarCarrito(carrito: Carrito): Result<Unit> {
        return try {
            firestore.collection("carritos").document(carrito.usuarioId).set(carrito).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearPedido(usuarioId: String, direccionEnvio: String): Result<Pedido> {
        return try {
            val carritoRef = firestore.collection("carritos").document(usuarioId)
            val configEnvio = obtenerConfigEnvio()

            val resultado = firestore.runTransaction { transaction ->
                val carritoSnapshot = transaction.get(carritoRef)
                val carrito = carritoSnapshot.toObject(Carrito::class.java)
                val items = carrito?.items ?: emptyList()

                if (items.isEmpty()) {
                    throw Exception("El carrito está vacío")
                }

                var subtotalGeneral = 0
                val detalles = mutableListOf<DetallePedido>()

                for (item in items) {
                    val prodRef = firestore.collection("productos").document(item.productoId)
                    val invRef = firestore.collection("inventario").document(item.productoId)

                    val prodSnap = transaction.get(prodRef)
                    val invSnap = transaction.get(invRef)

                    val productoServidor = prodSnap.toObject(Producto::class.java)
                        ?: throw Exception("El producto ${item.nombre} ya no existe")
                    val inventarioServidor = invSnap.toObject(Inventario::class.java)
                        ?: throw Exception("Inventario no disponible para ${item.nombre}")

                    if (inventarioServidor.cantidadDisponible < item.cantidad) {
                        throw Exception("Stock insuficiente para ${productoServidor.nombre}. Disponible: ${inventarioServidor.cantidadDisponible}")
                    }

                    // REGLA CLAVE: Servidor fija el precio real, ignorando el precio del cliente
                    val precioReal = productoServidor.precio
                    val subtotalItem = precioReal * item.cantidad
                    subtotalGeneral += subtotalItem

                    detalles.add(
                        DetallePedido(
                            productoId = productoServidor.id,
                            nombre = productoServidor.nombre,
                            presentacion = productoServidor.presentacion,
                            precioUnitario = precioReal,
                            cantidad = item.cantidad,
                            subtotal = subtotalItem
                        )
                    )

                    // Descontar stock (reserva atómica)
                    val nuevoStock = inventarioServidor.cantidadDisponible - item.cantidad
                    transaction.update(invRef, "cantidadDisponible", nuevoStock)
                }

                val costoEnvio = if (subtotalGeneral >= configEnvio.umbralEnvioGratis) 0 else configEnvio.tarifaFija
                val totalGeneral = subtotalGeneral + costoEnvio

                val pedidoRef = firestore.collection("pedidos").document()
                val pedido = Pedido(
                    id = pedidoRef.id,
                    usuarioId = usuarioId,
                    items = detalles,
                    subtotal = subtotalGeneral,
                    costoEnvio = costoEnvio,
                    total = totalGeneral,
                    direccionEnvio = direccionEnvio,
                    estado = Pedido.ESTADO_PENDIENTE_PAGO
                )

                transaction.set(pedidoRef, pedido)
                // Vaciar carrito
                transaction.set(carritoRef, Carrito(usuarioId = usuarioId, items = emptyList()))

                pedido
            }.await()

            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPedidos(usuarioId: String): Result<List<Pedido>> {
        return try {
            val snapshot = firestore.collection("pedidos")
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val pedidos = snapshot.documents.mapNotNull { it.toObject(Pedido::class.java) }
            Result.success(pedidos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
