package com.example.agedogs.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.Categoria
import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.Producto
import com.example.agedogs.data.repository.AuthRepository
import com.example.agedogs.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarketplaceUiState(
    val productos: List<Pair<Producto, Inventario>> = emptyList(),
    val productosFiltrados: List<Pair<Producto, Inventario>> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val categoriaSeleccionada: String? = null, // null = Todos
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedProducto: Pair<Producto, Inventario>? = null,
    val isAdmin: Boolean = false
)

class MarketplaceViewModel : ViewModel() {

    private val repository = MarketplaceRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    fun verificarYcargar() {
        verificarRolAdmin()
        cargarDatos()
    }

    private fun verificarRolAdmin() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData()
            val isAdmin = user?.rol == "admin"
            _uiState.update { it.copy(isAdmin = isAdmin) }
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.obtenerCategorias()

            // OPTIMIZACIÓN: Carga instantánea (< 0.2s). Solo si está vacío siembra una vez.
            val prodResult = repository.obtenerProductos()
            var listaProductos = prodResult.getOrDefault(emptyList())

            if (listaProductos.isEmpty()) {
                repository.sembrarDatosDemostracion()
                val retryResult = repository.obtenerProductos()
                listaProductos = retryResult.getOrDefault(emptyList())
            }

            _uiState.update {
                it.copy(
                    productos = listaProductos,
                    productosFiltrados = filtrarLista(listaProductos, it.categoriaSeleccionada, it.searchQuery),
                    isLoading = false
                )
            }
        }
    }

    fun seleccionarCategoria(categoriaNombre: String?) {
        _uiState.update { state ->
            state.copy(
                categoriaSeleccionada = categoriaNombre,
                productosFiltrados = filtrarLista(state.productos, categoriaNombre, state.searchQuery)
            )
        }
    }

    fun buscar(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                productosFiltrados = filtrarLista(state.productos, state.categoriaSeleccionada, query)
            )
        }
    }

    fun seleccionarProducto(productoPar: Pair<Producto, Inventario>?) {
        _uiState.update { it.copy(selectedProducto = productoPar) }
    }

    fun guardarProducto(producto: Producto, stockInicial: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.guardarProducto(producto, stockInicial)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Producto guardado con éxito") }
                    cargarDatos()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun actualizarInventario(productoId: String, nuevaCantidad: Int) {
        viewModelScope.launch {
            val result = repository.actualizarInventario(productoId, nuevaCantidad)
            result.fold(
                onSuccess = { cargarDatos() },
                onFailure = { error -> _uiState.update { it.copy(errorMessage = error.localizedMessage) } }
            )
        }
    }

    fun eliminarProducto(productoId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.eliminarProducto(productoId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Producto eliminado") }
                    cargarDatos()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun sembrarDemostracion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.sembrarDatosDemostracion()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "¡12 productos de demostración sembrados!") }
                    cargarDatos()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun filtrarLista(lista: List<Pair<Producto, Inventario>>, categoria: String?, query: String): List<Pair<Producto, Inventario>> {
        return lista.filter { (prod, _) ->
            val matchCat = if (categoria == null) true else prod.categoriaId.equals(categoria, true)
            val matchQuery = if (query.isBlank()) true else prod.nombre.contains(query, true) || prod.marca.contains(query, true)
            matchCat && matchQuery
        }
    }
}
