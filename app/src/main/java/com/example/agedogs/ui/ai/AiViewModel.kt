package com.example.agedogs.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.MensajeIa
import com.example.agedogs.data.repository.AiRepository
import com.example.agedogs.data.repository.AuthRepository
import com.example.agedogs.data.repository.PerrosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiUiState(
    val historial: List<MensajeIa> = emptyList(),
    val inputMensaje: String = "",
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val errorMessage: String? = null,
    val perroContextoId: String? = null,
    val perroContextoNombre: String? = null,
    val webhookUrl: String = "https://hook.eu1.make.com/your-webhook-endpoint"
)

class AiViewModel : ViewModel() {

    private val aiRepository = AiRepository()
    private val authRepository = AuthRepository()
    private val perrosRepository = PerrosRepository()

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cargarHistorial() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = aiRepository.obtenerHistorial(uid)
            result.fold(
                onSuccess = { lista ->
                    _uiState.update { it.copy(historial = lista, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputMensaje = text) }
    }

    fun setPerroContexto(id: String?, nombre: String?) {
        _uiState.update { it.copy(perroContextoId = id, perroContextoNombre = nombre) }
    }

    fun setWebhookUrl(url: String) {
        _uiState.update { it.copy(webhookUrl = url) }
    }

    fun enviarMensaje() {
        val mensaje = _uiState.value.inputMensaje.trim()
        val uid = authRepository.currentUid
        if (mensaje.isBlank() || uid == null) return

        val perroCtx = _uiState.value.perroContextoNombre?.let { "Perro seleccionado: $it" }

        _uiState.update {
            it.copy(
                inputMensaje = "",
                isTyping = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = aiRepository.consultarIa(
                usuarioId = uid,
                mensaje = mensaje,
                perroContexto = perroCtx,
                webhookUrl = _uiState.value.webhookUrl
            )

            result.fold(
                onSuccess = { nuevoMensaje ->
                    _uiState.update {
                        it.copy(
                            historial = it.historial + nuevoMensaje,
                            isTyping = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isTyping = false,
                            errorMessage = "Error al conectar con la IA: ${error.localizedMessage}. Verifica tu conexión o URL del webhook."
                        )
                    }
                }
            )
        }
    }

    fun reintentarUltimoMensaje() {
        // Vuelve a enviar si hay historial fallido o último input
        cargarHistorial()
    }
}
