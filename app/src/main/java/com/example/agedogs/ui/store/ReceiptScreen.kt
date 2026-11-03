package com.example.agedogs.ui.store

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agedogs.data.model.Pedido
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTopBar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptScreen(
    pedido: Pedido,
    onBackToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(
        Locale.Builder().setLanguage("es").setRegion("CO").build()
    ).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = { AppTopBar(title = "Comprobante de Pago") },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🐾 Age Dogs — Comprobante",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = "Número de Pedido: #${pedido.id}")
                    Text(text = "Fecha: ${dateFormat.format(Date(pedido.fechaCreacion))}")
                    Text(text = "Estado: ${pedido.estado}", fontWeight = FontWeight.Bold, color = if (pedido.estado == Pedido.ESTADO_PAGADO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Text(text = "Dirección de Envío: ${pedido.direccionEnvio}")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(text = "Detalle de Ítems:", fontWeight = FontWeight.Bold)
                    for (detalle in pedido.items) {
                        Text(text = "• ${detalle.cantidad}x ${detalle.nombre} (${detalle.presentacion}) - ${currencyFormat.format(detalle.subtotal)}")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Costo de Envío:")
                        Text(text = currencyFormat.format(pedido.costoEnvio))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Pagado:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = currencyFormat.format(pedido.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppPrimaryButton(
                text = "Compartir Comprobante 📤",
                onClick = {
                    val shareText = """
                        🐾 Comprobante de Compra - Age Dogs 🐾
                        Pedido: #${pedido.id}
                        Total: ${currencyFormat.format(pedido.total)}
                        Estado: ${pedido.estado}
                        ¡Gracias por preferirnos!
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Comprobante Age Dogs #${pedido.id}")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir comprobante"))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppPrimaryButton(
                text = "Volver a la Tienda",
                onClick = onBackToStore
            )
        }
    }
}
