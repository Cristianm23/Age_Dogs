package com.example.agedogs.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.agedogs.ui.components.AppDangerButton
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTextField
import com.example.agedogs.ui.components.AppTopBar

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.currentUser

    var nombre by remember(user) { mutableStateOf(user?.nombre ?: "") }
    var telefono by remember(user) { mutableStateOf(user?.telefono ?: "") }
    var direccion by remember(user) { mutableStateOf(user?.direccion ?: "") }
    var fotoUrl by remember(user) { mutableStateOf(user?.fotoUrl ?: "") }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Perfil de Usuario",
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Información Personal",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Correo: ${user?.email ?: ""}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Rol: ${user?.rol?.uppercase() ?: "CLIENTE"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            AppTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre completo"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = "Teléfono"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = "Dirección"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = fotoUrl,
                onValueChange = { fotoUrl = it },
                label = "URL de foto de perfil"
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.successMessage ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppPrimaryButton(
                text = "Guardar Cambios",
                onClick = {
                    viewModel.updateProfile(nombre, telefono, direccion, fotoUrl)
                },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppPrimaryButton(
                text = "Cambiar Contraseña",
                onClick = {
                    viewModel.clearMessages()
                    oldPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    showChangePasswordDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppDangerButton(
                text = "Cerrar Sesión",
                onClick = { showLogoutDialog = true }
            )
        }
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    AppTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = "Contraseña actual",
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "Nueva contraseña (mín. 8)",
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = "Confirmar nueva contraseña",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.changePassword(oldPassword, newPassword, confirmNewPassword) {
                            showChangePasswordDialog = false
                        }
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión en Age Dogs?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    }
                ) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
