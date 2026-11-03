package com.example.agedogs.ui.store

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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agedogs.data.model.Pedido
import com.example.agedogs.ui.components.AppEmptyState
import com.example.agedogs.ui.components.AppTopBar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: CartViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(
        Locale.Builder().setLanguage("es").setRegion("CO").build()
    ).apply {
        maximumFractionDigits = 0
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = { AppTopBar(title = "Historial de Pedidos") },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.pedidos.isEmpty()) {
                AppEmptyState(
                    message = "Aún no tienes pedidos realizados.",
                    icon = Icons.Default.ReceiptLong
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.pedidos) { pedido ->
                        PedidoCard(
                            pedido = pedido,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PedidoCard(
    pedido: Pedido,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pedido #${pedido.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = pedido.estado,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = when (pedido.estado) {
                        Pedido.ESTADO_PAGADO -> MaterialTheme.colorScheme.primary
                        Pedido.ESTADO_PENDIENTE_PAGO -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Fecha: ${dateFormat.format(Date(pedido.fechaCreacion))}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Dirección: ${pedido.direccionEnvio}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Ítems (${pedido.items.size}):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            for (detalle in pedido.items) {
                Text(
                    text = "• ${detallesTexto(detalle, currencyFormat)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Envío: ${currencyFormat.format(pedido.costoEnvio)}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Total: ${currencyFormat.format(pedido.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun detallesTexto(detalle: com.example.agedogs.data.model.DetallePedido, currencyFormat: NumberFormat): String {
    return "${detalle.cantidad}x ${detalle.nombre} (${detalle.presentacion}) - ${currencyFormat.format(detalle.subtotal)}"
}
