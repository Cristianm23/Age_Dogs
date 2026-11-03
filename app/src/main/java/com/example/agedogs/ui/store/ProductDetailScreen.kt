package com.example.agedogs.ui.store

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.Producto
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTopBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailScreen(
    producto: Producto,
    inventario: Inventario,
    onBack: () -> Unit,
    cartViewModel: CartViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFullImageDialog by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(
        Locale.Builder().setLanguage("es").setRegion("CO").build()
    ).apply {
        maximumFractionDigits = 0
    }

    val stock = inventario.cantidadDisponible
    val isAvailable = stock > 0

    Scaffold(
        topBar = { AppTopBar(title = producto.nombre) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Imagen del producto con opción de expandir a pantalla completa al pulsarla
            AsyncImage(
                model = producto.imagenUrl,
                contentDescription = producto.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFullImageDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = producto.marca.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Categoría: ${producto.categoriaId}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Presentación: ${producto.presentacion}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Precio: ${currencyFormat.format(producto.precio)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (isAvailable) "Stock disponible: $stock unidades" else "Agotado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Descripción del producto",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppPrimaryButton(
                text = if (isAvailable) "Agregar al Carrito 🛒" else "Producto Agotado",
                enabled = isAvailable,
                onClick = {
                    cartViewModel.agregarAlCarrito(producto, inventario)
                    Toast.makeText(context, "${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Diálogo para desplegar la imagen a pantalla completa
    if (showFullImageDialog) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
