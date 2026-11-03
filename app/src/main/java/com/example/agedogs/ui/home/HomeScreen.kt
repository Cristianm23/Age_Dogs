package com.example.agedogs.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.agedogs.ui.components.AppDangerButton
import com.example.agedogs.ui.components.AppTopBar

@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    userName: String = "Amigo"
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(title = "Age Dogs")
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
            // Tarjeta Banner de Bienvenida Inmersiva con Nombre de Usuario real y silueta/ilustración sin fondo
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bienvenido de nuevo,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Hola \"$userName\"",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Todo para el bienestar, salud y nutrición de tu mascota.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Ilustración inmersiva con silueta / contorno limpio de mascota
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400",
                        contentDescription = "Mascota Inmersiva",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }

            // Botón de Urgencias en Rojo Ferrari (Terciario M3)
            AppDangerButton(
                text = "🚨 Urgencias Veterinarias (Llamada Rápida)",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:123")
                    }
                    context.startActivity(intent)
                }
            )

            Text(
                text = "Popular Pet's Services & Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            QuickAccessGrid(onNavigateTo = onNavigateTo)
        }
    }
}

@Composable
private fun QuickAccessGrid(onNavigateTo: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickAccessCard(
                title = "Calculadora",
                subtitle = "Edad canina",
                icon = Icons.Default.Calculate,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("calculator") }
            )
            QuickAccessCard(
                title = "Mis Mascotas",
                subtitle = "Perfiles y salud",
                icon = Icons.Default.Pets,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("pets") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickAccessCard(
                title = "Asistente IA",
                subtitle = "Consultas caninas",
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("ai") }
            )
            QuickAccessCard(
                title = "Mapa & Cerca",
                subtitle = "Veterinarias",
                icon = Icons.Default.Map,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("map") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickAccessCard(
                title = "Tienda",
                subtitle = "Alimentos y snacks",
                icon = Icons.Default.Storefront,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("store") }
            )
            QuickAccessCard(
                title = "Más Servicios",
                subtitle = "Perfil y pedidos",
                icon = Icons.Default.MoreHoriz,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTo("more") }
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
