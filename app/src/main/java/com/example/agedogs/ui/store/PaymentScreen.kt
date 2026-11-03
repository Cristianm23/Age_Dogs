package com.example.agedogs.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agedogs.data.model.Pago
import com.example.agedogs.data.model.Pedido
import com.example.agedogs.ui.components.AppDangerButton
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppSecondaryButton
import com.example.agedogs.ui.components.AppTopBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    pedido: Pedido,
    onPaymentFinished: (Pedido) -> Unit,
    viewModel: PaymentViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var metodoPago by remember { mutableStateOf("CARD") }

    val currencyFormat = NumberFormat.getCurrencyInstance(
        Locale.Builder().setLanguage("es").setRegion("CO").build()
    ).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = { AppTopBar(title = "Pasarela Wompi (Sandbox)") },
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Resumen de Pago Wompi", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(text = "Pedido ID: #${pedido.id}")
                    Text(text = "Total a Pagar: ${currencyFormat.format(pedido.total)} COP")
                    Text(text = "Entorno: Sandbox (Pruebas)")
                }
            }

            Text(
                text = "Simulador de Transacción Wompi (Prueba los estados de pasarela)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            AppPrimaryButton(
                text = "Simular Pago APROBADO (APPROVED)",
                onClick = {
                    viewModel.iniciarPago(pedido, metodoPago) { pago ->
                        viewModel.simularRespuestaWompi(Pago.ESTADO_APPROVED) {
                            onPaymentFinished(viewModel.uiState.value.pedidoActual ?: pedido)
                        }
                    }
                },
                isLoading = uiState.isLoading
            )

            AppSecondaryButton(
                text = "Simular Pago DECLINADO (DECLINED)",
                onClick = {
                    viewModel.iniciarPago(pedido, metodoPago) { pago ->
                        viewModel.simularRespuestaWompi(Pago.ESTADO_DECLINED) {
                            onPaymentFinished(viewModel.uiState.value.pedidoActual ?: pedido)
                        }
                    }
                }
            )

            AppDangerButton(
                text = "Simular Pago con ERROR / VOIDED",
                onClick = {
                    viewModel.iniciarPago(pedido, metodoPago) { pago ->
                        viewModel.simularRespuestaWompi(Pago.ESTADO_ERROR) {
                            onPaymentFinished(viewModel.uiState.value.pedidoActual ?: pedido)
                        }
                    }
                }
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
