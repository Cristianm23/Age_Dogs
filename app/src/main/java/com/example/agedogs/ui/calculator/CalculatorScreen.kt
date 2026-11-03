package com.example.agedogs.ui.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agedogs.ui.components.AppCard
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppSecondaryButton
import com.example.agedogs.ui.components.AppTextField
import com.example.agedogs.ui.components.AppTopBar

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(title = "Calculadora Canina")
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Text(
                text = "Calcula la edad real de tu perro en años humanos.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = uiState.ageInput,
                onValueChange = viewModel::onAgeInputChanged,
                label = "Edad del perro (años)",
                isError = uiState.errorMessage != null,
                errorMessage = uiState.errorMessage,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppPrimaryButton(
                text = "Calcular",
                onClick = viewModel::calculateAge
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppSecondaryButton(
                text = "Limpiar",
                onClick = viewModel::clear
            )

            if (uiState.resultText.isNotBlank()) {
                Spacer(modifier = Modifier.height(32.dp))
                AppCard {
                    Text(
                        text = uiState.resultText,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
