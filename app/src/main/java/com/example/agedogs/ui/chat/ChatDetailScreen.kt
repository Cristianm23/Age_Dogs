package com.example.agedogs.ui.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.agedogs.data.model.Mensaje
import com.example.agedogs.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    currentUid: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val otroUsuario = uiState.otroUsuarioActivo
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showBlockDialog by remember { mutableStateOf(false) }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    LaunchedEffect(uiState.mensajesActuales.size) {
        if (uiState.mensajesActuales.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajesActuales.size - 1)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = otroUsuario?.nombre ?: "Chat",
                actions = {
                    IconButton(onClick = { showBlockDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Bloquear usuario",
                            tint = MaterialTheme.colorScheme.errorContainer
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
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.mensajesActuales) { mensaje ->
                    val esMio = mensaje.remitenteId == currentUid
                    ChatBubbleItem(mensaje = mensaje, esMio = esMio, timeFormat = timeFormat)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.inputMensaje,
                        onValueChange = viewModel::onInputChanged,
                        placeholder = { Text("Escribe un mensaje...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.enviarMensaje() },
                        enabled = uiState.inputMensaje.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (uiState.inputMensaje.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Bloquear Usuario") },
            text = { Text("¿Deseas bloquear a ${otroUsuario?.nombre ?: "este usuario"}? Ya no aparecerá en tu directorio ni podrá enviarte mensajes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBlockDialog = false
                        viewModel.bloquearUsuarioActivo {
                            Toast.makeText(context, "Usuario bloqueado", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                ) {
                    Text("Bloquear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ChatBubbleItem(
    mensaje: Mensaje,
    esMio: Boolean,
    timeFormat: SimpleDateFormat
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (esMio) 48.dp else 0.dp,
                end = if (esMio) 0.dp else 48.dp
            ),
        contentAlignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (esMio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esMio) 16.dp else 2.dp,
                bottomEnd = if (esMio) 2.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = mensaje.texto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (esMio) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = timeFormat.format(Date(mensaje.fechaHora)),
                    style = MaterialTheme.typography.bodySmall,
                    color = (if (esMio) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.6f)
                )
            }
        }
    }
}
