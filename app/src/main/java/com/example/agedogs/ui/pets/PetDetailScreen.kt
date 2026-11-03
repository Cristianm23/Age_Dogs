package com.example.agedogs.ui.pets

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.agedogs.data.model.Perro
import com.example.agedogs.domain.EdadCalculator
import com.example.agedogs.domain.LifeStageCalculator
import com.example.agedogs.ui.components.AppDangerButton
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTopBar
import java.util.Locale

@Composable
fun PetDetailScreen(
    perro: Perro,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConsultAi: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val edadHumana = EdadCalculator.calculate(perro.edadAnios)
    val etapa = LifeStageCalculator.determinarEtapa(perro.edadAnios, perro.pesoKg)
    val recomendaciones = LifeStageCalculator.obtenerRecomendaciones(etapa)

    Scaffold(
        topBar = {
            AppTopBar(
                title = perro.nombre,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Galería de fotos
            if (perro.fotos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(perro.fotos) { fotoUrl ->
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = perro.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Datos generales
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Raza: ${perro.raza}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Sexo: ${perro.sexo}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Peso: ${perro.pesoKg} kg", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Edad canina: ${perro.edadAnios} años", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Edad equivalente en humanos: ${String.format(Locale.getDefault(), "%.1f", edadHumana)} años",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (perro.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Descripción: ${perro.descripcion}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Etapa de vida y recomendaciones
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Etapa de Vida: ${etapa.titulo}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(text = etapa.descripcion, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "🥩 Alimentación: ${recomendaciones.alimentacion}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "💉 Salud: ${recomendaciones.salud}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "🎾 Ejercicio: ${recomendaciones.ejercicio}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "🩺 Chequeos: ${recomendaciones.chequeos}", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Información general; no sustituye la valoración de un médico veterinario.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppPrimaryButton(
                text = "Consultar a la IA sobre ${perro.nombre} 🤖",
                onClick = { onConsultAi(perro.nombre) }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar ${perro.nombre}") },
            text = { Text("¿Estás seguro de que deseas eliminar este perro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
