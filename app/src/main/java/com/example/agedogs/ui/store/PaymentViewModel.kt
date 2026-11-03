package com.example.agedogs.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agedogs.data.model.Pago
import com.example.agedogs.data.model.Pedido
import com.example.agedogs.data.repository.PagoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isLoading: Boolean = false,
    val pagoActual: Pago? = null,
    val pedidoActual: Pedido? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class PaymentViewModel : ViewModel() {

    private val pagoRepository = PagoRepository()

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun iniciarPago(pedido: Pedido, metodo: String, onSuccess: (Pago) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, pedidoActual = pedido) }
            val result = pagoRepository.iniciarTransaccionPago(pedido, metodo)
            result.fold(
                onSuccess = { pago ->
                    _uiState.update { it.copy(isLoading = false, pagoActual = pago) }
                    onSuccess(pago)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }

    fun simularRespuestaWompi(nuevoEstado: String, onSuccess: () -> Unit) {
        val pago = _uiState.value.pagoActual ?: return
        val pedido = _uiState.value.pedidoActual ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = pagoRepository.simularWebhookWompi(pago.id, pedido.id, nuevoEstado)
            result.fold(
                onSuccess = {
                    val pagoActualizado = pago.copy(estado = nuevoEstado)
                    val pedidoActualizado = pedido.copy(
                        estado = when (nuevoEstado) {
                            Pago.ESTADO_APPROVED -> Pedido.ESTADO_PAGADO
                            Pago.ESTADO_DECLINED, Pago.ESTADO_ERROR, Pago.ESTADO_VOIDED -> Pedido.ESTADO_RECHAZADO
                            else -> Pedido.ESTADO_PENDIENTE_PAGO
                        }
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pagoActual = pagoActualizado,
                            pedidoActual = pedidoActualizado,
                            successMessage = "Estado de pago actualizado: $nuevoEstado"
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
            )
        }
    }
}
