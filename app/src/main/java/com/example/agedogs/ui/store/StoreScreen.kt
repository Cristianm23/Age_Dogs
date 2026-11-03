package com.example.agedogs.ui.store

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.Producto
import com.example.agedogs.ui.components.AppErrorState
import com.example.agedogs.ui.components.AppLoadingState
import com.example.agedogs.ui.components.AppPrimaryButton
import com.example.agedogs.ui.components.AppTopBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StoreScreen(
    onSelectProduct: (Pair<Producto, Inventario>) -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenCart: () -> Unit,
    viewModel: MarketplaceViewModel,
    cartViewModel: CartViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.verificarYcargar()
        cartViewModel.cargarDatos()
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(
        Locale.Builder().setLanguage("es").setRegion("CO").build()
    ).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Comida para perros",
                actions = {
                    val totalItems = cartState.carrito.items.sumOf { it.cantidad }
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onOpenCart) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        if (totalItems > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Text(text = totalItems.toString())
                            }
                        }
                    }

                    if (uiState.isAdmin) {
                        IconButton(onClick = onOpenAdmin) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Panel Admin",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.buscar(it)
                },
                placeholder = { Text("Buscar por nombre o marca...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.categoriaSeleccionada == null,
                        onClick = { viewModel.seleccionarCategoria(null) },
                        label = { Text("Todos") }
                    )
                }
                items(uiState.categorias) { cat ->
                    FilterChip(
                        selected = uiState.categoriaSeleccionada == cat.nombre,
                        onClick = { viewModel.seleccionarCategoria(cat.nombre) },
                        label = { Text(cat.nombre) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    AppLoadingState(message = "Cargando alimentos para perros...")
                }
                uiState.errorMessage != null -> {
                    AppErrorState(
                        message = uiState.errorMessage ?: "Error desconocido en la tienda",
                        onRetry = { viewModel.verificarYcargar() }
                    )
                }
                uiState.productos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "La tienda está vacía. ¡Siembra los 12 productos de demostración!",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AppPrimaryButton(
                            text = "Sembrar 12 Productos de Demostración",
                            onClick = { viewModel.sembrarDemostracion {} }
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.productosFiltrados) { par ->
                            ProductoGridItem(
                                producto = par.first,
                                inventario = par.second,
                                currencyFormat = currencyFormat,
                                onClick = { onSelectProduct(par) },
                                onAddToCart = {
                                    cartViewModel.agregarAlCarrito(par.first, par.second)
                                    Toast.makeText(context, "${par.first.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoGridItem(
    producto: Producto,
    inventario: Inventario,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val stock = inventario.cantidadDisponible
    val isAvailable = stock > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OFERTA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Por ${producto.marca}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )

            Text(
                text = producto.presentacion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currencyFormat.format(producto.precio),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppPrimaryButton(
                text = if (isAvailable) "Agregar" else "Agotado",
                enabled = isAvailable,
                onClick = onAddToCart,
                modifier = Modifier.height(40.dp)
            )
        }
    }
}
