package com.example.agedogs.ui.pets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.Perro
import com.example.agedogs.data.repository.AuthRepository
import com.example.agedogs.data.repository.PerrosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetsUiState(
    val perros: List<Perro> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedPerro: Perro? = null
)

class PetsViewModel : ViewModel() {

    private val perrosRepository = PerrosRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    init {
        cargarPerros()
    }

    fun cargarPerros() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = perrosRepository.obtenerPerros(uid)
            result.fold(
                onSuccess = { lista ->
                    _uiState.update { it.copy(perros = lista, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun seleccionarPerro(perro: Perro?) {
        _uiState.update { it.copy(selectedPerro = perro) }
    }

    fun guardarPerro(
        id: String,
        nombre: String,
        raza: String,
        edadAnios: Double,
        pesoKg: Double,
        sexo: String,
        descripcion: String,
        fotos: List<String>,
        onSuccess: () -> Unit
    ) {
        val uid = authRepository.currentUid
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Usuario no autenticado") }
            return
        }
        if (nombre.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre del perro es obligatorio") }
            return
        }
        if (fotos.size > 5) {
            _uiState.update { it.copy(errorMessage = "Máximo 5 fotos permitidas por perro") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val perro = Perro(
                id = id,
                usuarioId = uid,
                nombre = nombre,
                raza = raza,
                edadAnios = edadAnios,
                pesoKg = pesoKg,
                sexo = sexo,
                descripcion = descripcion,
                fotos = fotos
            )
            val result = perrosRepository.guardarPerro(perro)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Perro guardado con éxito") }
                    cargarPerros()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun eliminarPerro(perroId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = perrosRepository.eliminarPerro(perroId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Perro eliminado") }
                    cargarPerros()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    suspend fun subirFoto(uri: Uri): Result<String> {
        val uid = authRepository.currentUid ?: return Result.failure(Exception("No autenticado"))
        return perrosRepository.subirImagen(uri, uid)
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
