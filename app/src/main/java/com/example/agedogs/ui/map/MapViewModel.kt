package com.example.agedogs.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.Veterinaria
import com.example.agedogs.data.repository.VeterinariaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val veterinarias: List<Veterinaria> = emptyList(),
    val filteredVeterinarias: List<Veterinaria> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val soloUrgencias: Boolean = false,
    val currentLat: Double = 4.6097, // Por defecto Bogotá, Colombia
    val currentLon: Double = -74.0817,
    val ubicacionObtenida: Boolean = false
)

class MapViewModel : ViewModel() {

    private val repository = VeterinariaRepository()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun actualizarUbicacion(lat: Double, lon: Double) {
        _uiState.update { it.copy(currentLat = lat, currentLon = lon, ubicacionObtenida = true) }
        buscarCercanas(lat, lon)
    }

    fun buscarCercanas(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.buscarVeterinariasCercanas(lat, lon)
            result.fold(
                onSuccess = { lista ->
                    _uiState.update {
                        it.copy(
                            veterinarias = lista,
                            filteredVeterinarias = aplicarFiltros(lista, it.soloUrgencias, it.searchQuery),
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al obtener veterinarias: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun buscarPorDireccion(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, searchQuery = query) }
            val geoResult = repository.buscarPorDireccionOGps(query)
            geoResult.fold(
                onSuccess = { (lat, lon) ->
                    actualizarUbicacion(lat, lon)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "No se encontró la dirección"
                        )
                    }
                }
            )
        }
    }

    fun toggleSoloUrgencias(soloUrgencias: Boolean) {
        _uiState.update { state ->
            state.copy(
                soloUrgencias = soloUrgencias,
                filteredVeterinarias = aplicarFiltros(state.veterinarias, soloUrgencias, state.searchQuery)
            )
        }
    }

    private fun aplicarFiltros(lista: List<Veterinaria>, soloUrgencias: Boolean, query: String): List<Veterinaria> {
        return lista.filter { vet ->
            val matchUrgencia = if (soloUrgencias) vet.es24Horas else true
            val matchQuery = if (query.isBlank()) true else vet.nombre.contains(query, true) || vet.direccion.contains(query, true)
            matchUrgencia && matchQuery
        }
    }
}
