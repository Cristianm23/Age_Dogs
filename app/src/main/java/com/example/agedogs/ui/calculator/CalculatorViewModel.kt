package com.example.agedogs.ui.calculator

import androidx.lifecycle.ViewModel
import com.example.agedogs.domain.EdadCalculator
import com.example.agedogs.domain.EdadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class CalculatorUiState(
    val ageInput: String = "",
    val resultText: String = "",
    val errorMessage: String? = null
)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onAgeInputChanged(newInput: String) {
        _uiState.update {
            it.copy(
                ageInput = newInput,
                errorMessage = null
            )
        }
    }

    fun calculateAge() {
        when (val result = EdadCalculator.parseAndCalculate(_uiState.value.ageInput)) {
            is EdadResult.Success -> {
                val formatted = String.format(Locale.getDefault(), "%.1f", result.edadHumana)
                _uiState.update {
                    it.copy(
                        resultText = "Edad en años humanos: $formatted años",
                        errorMessage = null
                    )
                }
            }
            is EdadResult.Error -> {
                _uiState.update {
                    it.copy(
                        resultText = "",
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clear() {
        _uiState.update {
            CalculatorUiState()
        }
    }
}
