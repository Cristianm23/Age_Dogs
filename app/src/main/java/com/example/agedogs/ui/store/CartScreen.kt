package com.example.agedogs.ui.store

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.agedogs.data.model.ItemCarrito
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppSecondaryButton
import com.example.agedogs.ui.components.AppTextField
import com.example.agedogs.ui.components.AppTopBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartScreen(
    onOrderSuccess: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: CartViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(
        Locale.Builder().setLanguage("es").setRegion("CO").build()
    ).apply {
        maximumFractionDigits = 0
    }

    var direccionEnvio by remember { mutableStateOf("") }

    Scaffold(
        topBar = { AppTopBar(title = "Carrito de Compras") },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.carrito.items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Tu carrito está vacío 🛒", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    AppPrimaryButton(
                        text = "Ver Historial de Pedidos",
                        onClick = onNavigateToOrders
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.carrito.items) { item ->
                        CartItemRow(
                            item = item,
                            currencyFormat = currencyFormat,
                            onIncrement = { viewModel.cambiarCantidad(item.productoId, 1) },
                            onDecrement = { viewModel.cambiarCantidad(item.productoId, -1) },
                            onDelete = { viewModel.eliminarItem(item.productoId) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = direccionEnvio,
                    onValueChange = { direccionEnvio = it },
                    label = "Dirección de entrega *"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Subtotal:")
                            Text(text = currencyFormat.format(uiState.subtotalGeneral))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Costo de envío:")
                            Text(text = if (uiState.costoEnvio == 0) "¡GRATIS!" else currencyFormat.format(uiState.costoEnvio))
                        }
                        if (uiState.subtotalGeneral < uiState.configEnvio.umbralEnvioGratis) {
                            Text(
                                text = "Faltan ${currencyFormat.format(uiState.configEnvio.umbralEnvioGratis - uiState.subtotalGeneral)} para envío gratis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total a pagar:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = currencyFormat.format(uiState.totalGeneral), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AppSecondaryButton(
                        text = "Vaciar",
                        onClick = { viewModel.vaciarCarrito() },
                        modifier = Modifier.weight(1f)
                    )
                    AppPrimaryButton(
                        text = "Confirmar Pedido",
                        onClick = {
                            viewModel.confirmarPedido(direccionEnvio) { pedido ->
                                Toast.makeText(context, "Pedido creado: ${pedido.id}", Toast.LENGTH_LONG).show()
                                onOrderSuccess()
                            }
                        },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: ItemCarrito,
    currencyFormat: NumberFormat,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${item.presentacion} • ${currencyFormat.format(item.precioUnitario)} c/u", style = MaterialTheme.typography.bodySmall)
                Text(text = "Subtotal: ${currencyFormat.format(item.subtotal)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos")
                }
                Text(text = "${item.cantidad}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onIncrement) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Más")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
