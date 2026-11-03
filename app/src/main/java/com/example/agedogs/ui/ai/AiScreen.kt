package com.example.agedogs.ui.ai

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agedogs.data.model.MensajeIa
import com.example.agedogs.ui.components.AppDangerButton
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTextField
import com.example.agedogs.ui.components.AppTopBar

@Composable
fun AiScreen(
    onNavigateToMap: () -> Unit,
    viewModel: AiViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showWebhookDialog by remember { mutableStateOf(false) }
    var webhookInput by remember { mutableStateOf(uiState.webhookUrl) }

    val tieneUrgenciaActiva = uiState.historial.any { it.urgencia }

    LaunchedEffect(uiState.historial.size) {
        if (uiState.historial.isNotEmpty()) {
            listState.animateScrollToItem(uiState.historial.size - 1)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Asistente Canino IA",
                actions = {
                    // Botón para configurar la URL del Webhook (soluciona el 404 si no se ha configurado Make)
                    IconButton(onClick = {
                        webhookInput = uiState.webhookUrl
                        showWebhookDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurar Webhook",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // Botón de Urgencias
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:123")
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.LocalPhone,
                            contentDescription = "Urgencias",
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
            if (tieneUrgenciaActiva) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🚨 Posible urgencia: acude a un veterinario de inmediato",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppPrimaryButton(
                                text = "Buscar veterinarias",
                                onClick = onNavigateToMap,
                                modifier = Modifier.weight(1f)
                            )
                            AppDangerButton(
                                text = "Llamar",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:123")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.historial) { mensaje ->
                    ChatBubble(mensaje = mensaje)
                }

                if (uiState.isTyping) {
                    item {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Asistente escribiendo...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.cargarHistorial() }) {
                        Text("Reintentar")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Text(
                    text = "ℹ️ La IA no sustituye la valoración de un médico veterinario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.inputMensaje,
                        onValueChange = viewModel::onInputChanged,
                        placeholder = { Text("Escribe tu consulta canina...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.enviarMensaje() },
                        enabled = uiState.inputMensaje.isNotBlank() && !uiState.isTyping
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

    // Diálogo para configurar la URL del Webhook
    if (showWebhookDialog) {
        AlertDialog(
            onDismissRequest = { showWebhookDialog = false },
            title = { Text("Configurar Webhook de IA") },
            text = {
                Column {
                    Text(
                        text = "Ingresa la URL HTTPS de tu backend o webhook (ej. Make / Gemini proxy):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppTextField(
                        value = webhookInput,
                        onValueChange = { webhookInput = it },
                        label = "URL del Webhook"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setWebhookUrl(webhookInput)
                        showWebhookDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebhookDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ChatBubble(mensaje: MensajeIa) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
            ) {
                Text(
                    text = mensaje.pregunta,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 48.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (mensaje.urgencia) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = mensaje.respuesta,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (mensaje.urgencia) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (mensaje.urgencia && !mensaje.motivo.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Motivo: ${mensaje.motivo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
