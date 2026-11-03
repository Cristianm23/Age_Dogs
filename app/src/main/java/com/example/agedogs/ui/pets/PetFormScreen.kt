package com.example.agedogs.ui.pets

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.agedogs.data.model.Perro
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppSecondaryButton
import com.example.agedogs.ui.components.AppTextField
import com.example.agedogs.ui.components.AppTopBar
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PetFormScreen(
    perroToEdit: Perro? = null,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: PetsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf(perroToEdit?.nombre ?: "") }
    var raza by remember { mutableStateOf(perroToEdit?.raza ?: "Mestizo") }
    var edadInput by remember { mutableStateOf(perroToEdit?.edadAnios?.toString() ?: "") }
    var pesoInput by remember { mutableStateOf(perroToEdit?.pesoKg?.toString() ?: "") }
    var sexo by remember { mutableStateOf(perroToEdit?.sexo ?: "Macho") }
    var descripcion by remember { mutableStateOf(perroToEdit?.descripcion ?: "") }
    var fotos by remember { mutableStateOf(perroToEdit?.fotos ?: emptyList()) }

    var isUploading by remember { mutableStateOf(false) }

    // Uri temporal para la cámara
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val uri = tempCameraUri!!
            if (fotos.size >= 5) {
                Toast.makeText(context, "Ya has alcanzado el límite de 5 fotos.", Toast.LENGTH_SHORT).show()
            } else {
                isUploading = true
                coroutineScope.launch {
                    val result = viewModel.subirFoto(uri)
                    result.onSuccess { url ->
                        fotos = (fotos + url).take(5)
                    }
                    isUploading = false
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "camera_dog_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para tomar fotos.", Toast.LENGTH_LONG).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remainingSlots = 5 - fotos.size
            if (uris.size > remainingSlots) {
                Toast.makeText(context, "Máximo 5 fotos permitidas. Solo se agregaron las primeras $remainingSlots.", Toast.LENGTH_LONG).show()
            }
            val urisToProcess = uris.take(remainingSlots)
            if (urisToProcess.isNotEmpty()) {
                isUploading = true
                coroutineScope.launch {
                    val newUrls = mutableListOf<String>()
                    for (uri in urisToProcess) {
                        val result = viewModel.subirFoto(uri)
                        result.onSuccess { url -> newUrls.add(url) }
                    }
                    fotos = (fotos + newUrls).take(5)
                    isUploading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = if (perroToEdit == null) "Registrar Perro" else "Editar Perro")
        },
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
            AppTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre del perro *"
            )

            AppTextField(
                value = raza,
                onValueChange = { raza = it },
                label = "Raza (ej. Labrador, Mestizo)"
            )

            AppTextField(
                value = edadInput,
                onValueChange = { edadInput = it },
                label = "Edad en años (ej. 2.5)"
            )

            AppTextField(
                value = pesoInput,
                onValueChange = { pesoInput = it },
                label = "Peso en kg (ej. 14.5)"
            )

            Column {
                Text(text = "Sexo:", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = sexo == "Macho", onClick = { sexo = "Macho" })
                    Text(text = "Macho")
                    Spacer(modifier = Modifier.padding(8.dp))
                    RadioButton(selected = sexo == "Hembra", onClick = { sexo = "Hembra" })
                    Text(text = "Hembra")
                }
            }

            AppTextField(
                value = descripcion,
                onValueChange = { if (it.length <= 300) descripcion = it },
                label = "Descripción (máx. 300 caracteres)"
            )
            Text(
                text = "${descripcion.length}/300 caracteres",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Fotos y botones para Cámara / Galería (Máximo 5)
            Column {
                Text(text = "Fotos (${fotos.size}/5):", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fotos) { fotoUrl ->
                        Box(modifier = Modifier.size(90.dp)) {
                            AsyncImage(
                                model = fotoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = { fotos = fotos - fotoUrl },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar foto",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (fotos.size < 5) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (fotos.size >= 5) {
                                            Toast.makeText(context, "Ya has alcanzado el límite de 5 fotos.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Galería",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (fotos.size >= 5) {
                                            Toast.makeText(context, "Ya has alcanzado el límite de 5 fotos.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Cámara",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppPrimaryButton(
                text = "Guardar Perro",
                onClick = {
                    val edad = edadInput.toDoubleOrNull() ?: 0.0
                    val peso = pesoInput.toDoubleOrNull() ?: 10.0
                    viewModel.guardarPerro(
                        id = perroToEdit?.id ?: "",
                        nombre = nombre,
                        raza = raza,
                        edadAnios = edad,
                        pesoKg = peso,
                        sexo = sexo,
                        descripcion = descripcion,
                        fotos = fotos,
                        onSuccess = onSaveSuccess
                    )
                },
                isLoading = uiState.isLoading
            )
        }
    }
}
