package com.example.agedogs.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.Usuario
import com.example.agedogs.data.repository.AuthRepository
import com.example.agedogs.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: Usuario? = null,
    val savedEmail: String = "",
    val rememberMe: Boolean = false,
    val isLoggedIn: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferencesManager = PreferencesManager(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkInitialSession()
    }

    private fun checkInitialSession() {
        viewModelScope.launch {
            val remember = preferencesManager.rememberMeFlow.first()
            val savedEmail = preferencesManager.savedEmailFlow.first()
            val currentUid = authRepository.currentUid

            if (!remember && currentUid != null) {
                // Si "Recordarme" no estaba marcado, cerramos sesión al abrir
                authRepository.logout()
                _uiState.update { it.copy(isLoggedIn = false, savedEmail = savedEmail, rememberMe = false) }
            } else if (currentUid != null) {
                val user = authRepository.getCurrentUserData()
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = true,
                        savedEmail = savedEmail,
                        rememberMe = remember
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        savedEmail = savedEmail,
                        rememberMe = remember
                    )
                }
            }
        }
    }

    fun login(email: String, pass: String, rememberMe: Boolean, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            preferencesManager.saveRememberMeData(email, rememberMe)

            val result = authRepository.login(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isLoggedIn = true,
                            rememberMe = rememberMe
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    val friendlyMessage = translateAuthError(error.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = friendlyMessage) }
                }
            )
        }
    }

    fun register(
        nombre: String,
        email: String,
        pass: String,
        confirmPass: String,
        telefono: String,
        direccion: String,
        onSuccess: () -> Unit
    ) {
        if (nombre.isBlank() || email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa los campos obligatorios.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "El formato del correo electrónico no es válido.") }
            return
        }
        if (pass.length < 8) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres.") }
            return
        }
        if (pass != confirmPass) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(nombre, email, pass, telefono, direccion)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isLoggedIn = true,
                            successMessage = "¡Registro exitoso!"
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    val friendlyMessage = translateAuthError(error.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = friendlyMessage) }
                }
            )
        }
    }

    fun recoverPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Ingresa un correo electrónico válido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Correo de recuperación enviado. Revisa tu bandeja."
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = translateAuthError(error.message)) }
                }
            )
        }
    }

    fun updateProfile(nombre: String, telefono: String, direccion: String, fotoUrl: String) {
        val current = _uiState.value.currentUser ?: return
        val updated = current.copy(
            nombre = nombre,
            telefono = telefono,
            direccion = direccion,
            fotoUrl = fotoUrl
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.updateProfile(updated)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = updated,
                            successMessage = "Perfil actualizado correctamente."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun changePassword(oldPass: String, newPass: String, confirmNewPass: String, onSuccess: () -> Unit) {
        if (oldPass.isBlank() || newPass.isBlank() || confirmNewPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Todos los campos de contraseña son obligatorios.") }
            return
        }
        if (newPass.length < 8) {
            _uiState.update { it.copy(errorMessage = "La nueva contraseña debe tener al menos 8 caracteres.") }
            return
        }
        if (newPass != confirmNewPass) {
            _uiState.update { it.copy(errorMessage = "Las nuevas contraseñas no coinciden.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.changePassword(oldPass, newPass)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Contraseña actualizada con éxito."
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cambiar contraseña: ${translateAuthError(error.message)}") }
                }
            )
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                AuthUiState(savedEmail = it.savedEmail, rememberMe = it.rememberMe, isLoggedIn = false)
            }
            onSuccess()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun translateAuthError(raw: String?): String {
        return when {
            raw.isNullOrBlank() -> "Ha ocurrido un error desconocido."
            raw.contains("no user record", true) || raw.contains("user-not-found", true) -> "No existe una cuenta registrada con este correo."
            raw.contains("password", true) && (raw.contains("invalid", true) || raw.contains("wrong", true)) -> "Contraseña incorrecta."
            raw.contains("email-already-in-use", true) -> "Este correo ya está registrado."
            raw.contains("network", true) -> "Sin conexión a internet. Verifica tu red."
            raw.contains("badly formatted", true) -> "El formato del correo es inválido."
            else -> raw
        }
    }
}
