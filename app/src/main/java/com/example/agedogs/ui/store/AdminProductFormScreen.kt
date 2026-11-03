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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.agedogs.data.model.Categoria
import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.Producto
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTextField
import com.example.agedogs.ui.components.AppTopBar

@Composable
fun AdminProductFormScreen(
    productoToEdit: Producto? = null,
    inventarioToEdit: Inventario? = null,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var nombre by remember { mutableStateOf(productoToEdit?.nombre ?: "") }
    var marca by remember { mutableStateOf(productoToEdit?.marca ?: "") }
    var descripcion by remember { mutableStateOf(productoToEdit?.descripcion ?: "") }
    var presentacion by remember { mutableStateOf(productoToEdit?.presentacion ?: "") }
    var precioInput by remember { mutableStateOf(productoToEdit?.precio?.toString() ?: "") }
    var imagenUrl by remember { mutableStateOf(productoToEdit?.imagenUrl ?: "") }
    var stockInput by remember { mutableStateOf(inventarioToEdit?.cantidadDisponible?.toString() ?: "10") }

    var categoriaSeleccionada by remember { mutableStateOf(productoToEdit?.categoriaId ?: Categoria.CATEGORIAS_VALIDAS[0]) }

    Scaffold(
        topBar = {
            AppTopBar(title = if (productoToEdit == null) "Nuevo Alimento Canino" else "Editar Alimento")
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
                label = "Nombre del producto *"
            )

            AppTextField(
                value = marca,
                onValueChange = { marca = it },
                label = "Marca *"
            )

            AppTextField(
                value = presentacion,
                onValueChange = { presentacion = it },
                label = "Presentación (ej. Bulto 15 kg, Lata 350g) *"
            )

            AppTextField(
                value = precioInput,
                onValueChange = { precioInput = it },
                label = "Precio en COP (entero) *"
            )

            AppTextField(
                value = stockInput,
                onValueChange = { stockInput = it },
                label = "Stock inicial (cantidad disponible) *"
            )

            AppTextField(
                value = imagenUrl,
                onValueChange = { imagenUrl = it },
                label = "URL de imagen *"
            )

            AppTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = "Descripción *"
            )

            Column {
                Text(text = "Categoría permitida (Alimento canino) *", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Categoria.CATEGORIAS_VALIDAS.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (categoriaSeleccionada == cat),
                                onClick = { categoriaSeleccionada = cat }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (categoriaSeleccionada == cat),
                            onClick = { categoriaSeleccionada = cat }
                        )
                        Text(text = cat, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppPrimaryButton(
                text = "Guardar Producto",
                onClick = {
                    val precio = precioInput.toIntOrNull() ?: 0
                    val stock = stockInput.toIntOrNull() ?: 0

                    if (nombre.isBlank() || marca.isBlank() || precio <= 0) {
                        Toast.makeText(context, "Por favor completa los campos obligatorios correctamente.", Toast.LENGTH_SHORT).show()
                        return@AppPrimaryButton
                    }

                    val producto = Producto(
                        id = productoToEdit?.id ?: "",
                        nombre = nombre,
                        marca = marca,
                        descripcion = descripcion,
                        presentacion = presentacion,
                        precio = precio,
                        categoriaId = categoriaSeleccionada,
                        imagenUrl = imagenUrl,
                        activo = true
                    )

                    viewModel.guardarProducto(producto, stock, onSaveSuccess)
                },
                isLoading = uiState.isLoading
            )
        }
    }
}
