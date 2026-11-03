package com.example.agedogs.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTopBar

@Composable
fun MoreScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToConversations: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { AppTopBar(title = "Más Servicios") },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            AppPrimaryButton(
                text = "Ver y Editar Perfil de Usuario",
                onClick = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppPrimaryButton(
                text = "Mensajes y Chats 💬",
                onClick = onNavigateToConversations
            )
        }
    }
}
