package com.example.agedogs.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.Carrito
import com.example.agedogs.data.model.ConfigEnvio
import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.ItemCarrito
import com.example.agedogs.data.model.Pedido
import com.example.agedogs.data.model.Producto
import com.example.agedogs.data.repository.AuthRepository
import com.example.agedogs.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val carrito: Carrito = Carrito(),
    val configEnvio: ConfigEnvio = ConfigEnvio(),
    val pedidos: List<Pedido> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pedidoReciente: Pedido? = null
) {
    val subtotalGeneral: Int get() = carrito.items.sumOf { it.subtotal }
    val costoEnvio: Int get() = if (subtotalGeneral >= configEnvio.umbralEnvioGratis || subtotalGeneral == 0) 0 else configEnvio.tarifaFija
    val totalGeneral: Int get() = subtotalGeneral + costoEnvio
}

class CartViewModel : ViewModel() {

    private val pedidoRepository = PedidoRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val carrito = pedidoRepository.obtenerCarrito(uid)
            val config = pedidoRepository.obtenerConfigEnvio()
            val pedidosResult = pedidoRepository.obtenerPedidos(uid)

            val pedidos = pedidosResult.getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    carrito = carrito.copy(usuarioId = uid),
                    configEnvio = config,
                    pedidos = pedidos,
                    isLoading = false
                )
            }
        }
    }

    fun agregarAlCarrito(producto: Producto, inventario: Inventario) {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            val carritoActual = _uiState.value.carrito
            val itemsActuales = carritoActual.items.toMutableList()
            val index = itemsActuales.indexOfFirst { it.productoId == producto.id }

            if (index >= 0) {
                val item = itemsActuales[index]
                if (item.cantidad < inventario.cantidadDisponible) {
                    itemsActuales[index] = item.copy(cantidad = item.cantidad + 1)
                }
            } else {
                if (inventario.cantidadDisponible > 0) {
                    itemsActuales.add(
                        ItemCarrito(
                            productoId = producto.id,
                            nombre = producto.nombre,
                            marca = producto.marca,
                            imagenUrl = producto.imagenUrl,
                            presentacion = producto.presentacion,
                            precioUnitario = producto.precio,
                            cantidad = 1
                        )
                    )
                }
            }

            val nuevoCarrito = carritoActual.copy(
                usuarioId = uid,
                items = itemsActuales,
                ultimaActualizacion = System.currentTimeMillis()
            )
            pedidoRepository.guardarCarrito(nuevoCarrito)
            _uiState.update { it.copy(carrito = nuevoCarrito) }
        }
    }

    fun cambiarCantidad(productoId: String, delta: Int) {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            val carritoActual = _uiState.value.carrito
            val itemsActuales = carritoActual.items.toMutableList()
            val index = itemsActuales.indexOfFirst { it.productoId == productoId }
            if (index >= 0) {
                val item = itemsActuales[index]
                val nuevaCantidad = item.cantidad + delta
                if (nuevaCantidad <= 0) {
                    itemsActuales.removeAt(index)
                } else {
                    itemsActuales[index] = item.copy(cantidad = nuevaCantidad)
                }

                val nuevoCarrito = carritoActual.copy(
                    usuarioId = uid,
                    items = itemsActuales,
                    ultimaActualizacion = System.currentTimeMillis()
                )
                pedidoRepository.guardarCarrito(nuevoCarrito)
                _uiState.update { it.copy(carrito = nuevoCarrito) }
            }
        }
    }

    fun eliminarItem(productoId: String) {
        cambiarCantidad(productoId, -999)
    }

    fun vaciarCarrito() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            val nuevoCarrito = Carrito(usuarioId = uid, items = emptyList())
            pedidoRepository.guardarCarrito(nuevoCarrito)
            _uiState.update { it.copy(carrito = nuevoCarrito) }
        }
    }

    fun confirmarPedido(direccionEnvio: String, onSuccess: (Pedido) -> Unit) {
        val uid = authRepository.currentUid ?: return
        if (direccionEnvio.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingresa o selecciona una dirección de envío.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = pedidoRepository.crearPedido(uid, direccionEnvio)
            result.fold(
                onSuccess = { pedido ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pedidoReciente = pedido,
                            successMessage = "¡Pedido creado exitosamente (Pendiente de pago)!"
                        )
                    }
                    cargarDatos()
                    onSuccess(pedido)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al crear pedido: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
