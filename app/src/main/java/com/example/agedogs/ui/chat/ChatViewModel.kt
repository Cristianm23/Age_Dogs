package com.example.agedogs.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.Conversacion
import com.example.agedogs.data.model.Mensaje
import com.example.agedogs.data.model.Usuario
import com.example.agedogs.data.repository.AuthRepository
import com.example.agedogs.data.repository.ChatRepository
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatUiState(
    val conversaciones: List<Conversacion> = emptyList(),
    val directorioUsuarios: List<Usuario> = emptyList(),
    val mensajesActuales: List<Mensaje> = emptyList(),
    val conversacionActiva: Conversacion? = null,
    val otroUsuarioActivo: Usuario? = null,
    val inputMensaje: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val authRepository = AuthRepository()
    private val firestore = FirebaseProvider.firestore

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var mensajesListener: ListenerRegistration? = null
    private var convsListener: ListenerRegistration? = null

    init {
        cargarConversaciones()
        cargarDirectorio()
    }

    fun cargarConversaciones() {
        val uid = authRepository.currentUid ?: return
        convsListener?.remove()

        convsListener = firestore.collection("conversaciones")
            .whereArrayContains("participantes", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { it.toObject(Conversacion::class.java) } ?: emptyList()
                _uiState.update { it.copy(conversaciones = lista.sortedByDescending { conv -> conv.ultimoMensajeFecha }) }
            }
    }

    fun cargarDirectorio() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            val result = chatRepository.obtenerDirectorioUsuarios(uid)
            result.fold(
                onSuccess = { usuarios ->
                    _uiState.update { it.copy(directorioUsuarios = usuarios) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun abrirChatCon(otroUsuario: Usuario, onReady: () -> Unit) {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = chatRepository.obtenerOcrearConversacion(uid, otroUsuario.id)
            result.fold(
                onSuccess = { conv ->
                    _uiState.update {
                        it.copy(
                            conversacionActiva = conv,
                            otroUsuarioActivo = otroUsuario,
                            isLoading = false
                        )
                    }
                    escucharMensajes(conv.id)
                    onReady()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun abrirConversacionExistente(conv: Conversacion, onReady: () -> Unit) {
        val uid = authRepository.currentUid ?: return
        val otroUid = conv.participantes.firstOrNull { it != uid } ?: ""

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val doc = firestore.collection("usuarios").document(otroUid).get().await()
            val nombre = doc.getString("nombre") ?: "Usuario"
            val foto = doc.getString("fotoUrl") ?: ""
            val otroUsuario = Usuario(id = otroUid, nombre = nombre, fotoUrl = foto, email = "", telefono = "")

            _uiState.update {
                it.copy(
                    conversacionActiva = conv,
                    otroUsuarioActivo = otroUsuario,
                    isLoading = false
                )
            }
            escucharMensajes(conv.id)
            onReady()
        }
    }

    private fun escucharMensajes(conversacionId: String) {
        mensajesListener?.remove()
        mensajesListener = firestore.collection("conversaciones").document(conversacionId)
            .collection("mensajes")
            .orderBy("fechaHora", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val mensajes = snapshot?.documents?.mapNotNull { it.toObject(Mensaje::class.java) } ?: emptyList()
                _uiState.update { it.copy(mensajesActuales = mensajes) }
            }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputMensaje = text) }
    }

    fun enviarMensaje() {
        val texto = _uiState.value.inputMensaje.trim()
        val conv = _uiState.value.conversacionActiva
        val uid = authRepository.currentUid
        if (texto.isBlank() || conv == null || uid == null) return

        _uiState.update { it.copy(inputMensaje = "") }

        viewModelScope.launch {
            chatRepository.enviarMensaje(conv.id, uid, texto)
        }
    }

    fun bloquearUsuarioActivo(onSuccess: () -> Unit) {
        val uid = authRepository.currentUid ?: return
        val otroUid = _uiState.value.otroUsuarioActivo?.id ?: return

        viewModelScope.launch {
            val result = chatRepository.bloquearUsuario(uid, otroUid)
            result.fold(
                onSuccess = {
                    cargarDirectorio()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        mensajesListener?.remove()
        convsListener?.remove()
    }
}
