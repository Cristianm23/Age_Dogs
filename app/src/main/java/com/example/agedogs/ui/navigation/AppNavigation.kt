package com.example.agedogs.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.agedogs.ui.ai.AiScreen
import com.example.agedogs.ui.auth.AuthViewModel
import com.example.agedogs.ui.auth.ForgotPasswordScreen
import com.example.agedogs.ui.auth.LoginScreen
import com.example.agedogs.ui.auth.ProfileScreen
import com.example.agedogs.ui.auth.RegisterScreen
import com.example.agedogs.ui.calculator.CalculatorScreen
import com.example.agedogs.ui.chat.ChatDetailScreen
import com.example.agedogs.ui.chat.ChatViewModel
import com.example.agedogs.ui.chat.ConversationsListScreen
import com.example.agedogs.ui.chat.UserDirectoryScreen
import com.example.agedogs.ui.home.HomeScreen
import com.example.agedogs.ui.map.MapScreen
import com.example.agedogs.ui.more.MoreScreen
import com.example.agedogs.ui.pets.PetDetailScreen
import com.example.agedogs.ui.pets.PetFormScreen
import com.example.agedogs.ui.pets.PetsScreen
import com.example.agedogs.ui.pets.PetsViewModel
import com.example.agedogs.ui.store.AdminProductFormScreen
import com.example.agedogs.ui.store.AdminProductsScreen
import com.example.agedogs.ui.store.CartScreen
import com.example.agedogs.ui.store.CartViewModel
import com.example.agedogs.ui.store.MarketplaceViewModel
import com.example.agedogs.ui.store.OrdersScreen
import com.example.agedogs.ui.store.PaymentScreen
import com.example.agedogs.ui.store.PaymentViewModel
import com.example.agedogs.ui.store.ProductDetailScreen
import com.example.agedogs.ui.store.ReceiptScreen
import com.example.agedogs.ui.store.StoreScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    petsViewModel: PetsViewModel = viewModel(),
    marketplaceViewModel: MarketplaceViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()
    val petsState by petsViewModel.uiState.collectAsState()
    val marketplaceState by marketplaceViewModel.uiState.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = if (authState.isLoggedIn) Screen.Home.route else Screen.Login.route

    // Ocultar el FAB de IA si estamos en pantallas que ya poseen su propio FAB (como PetsScreen o AdminProductsScreen)
    val showAiFab = authState.isLoggedIn &&
            bottomNavItems.any { it.route == currentRoute } &&
            currentRoute != Screen.Pets.route &&
            currentRoute != "admin_products"

    Scaffold(
        floatingActionButton = {
            if (showAiFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Ai.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Asistente IA"
                    )
                }
            }
        },
        bottomBar = {
            if (authState.isLoggedIn && bottomNavItems.any { it.route == currentRoute }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth Routes
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        petsViewModel.cargarPerros()
                        marketplaceViewModel.cargarDatos()
                        cartViewModel.cargarDatos()
                        chatViewModel.cargarConversaciones()
                        chatViewModel.cargarDirectorio()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgot = {
                        navController.navigate(Screen.Forgot.route)
                    },
                    viewModel = authViewModel
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        petsViewModel.cargarPerros()
                        marketplaceViewModel.cargarDatos()
                        cartViewModel.cargarDatos()
                        chatViewModel.cargarConversaciones()
                        chatViewModel.cargarDirectorio()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    },
                    viewModel = authViewModel
                )
            }
            composable(Screen.Forgot.route) {
                ForgotPasswordScreen(
                    onBackToLogin = {
                        navController.popBackStack()
                    },
                    viewModel = authViewModel
                )
            }

            // Main App Routes
            composable(Screen.Home.route) {
                val userName = authState.currentUser?.nombre
                    ?: authState.currentUser?.email?.substringBefore("@")
                    ?: "Amigo"
                HomeScreen(
                    onNavigateTo = { targetRoute ->
                        navController.navigate(targetRoute)
                    },
                    userName = userName
                )
            }
            composable(Screen.Pets.route) {
                PetsScreen(
                    onAddPet = {
                        petsViewModel.seleccionarPerro(null)
                        navController.navigate("pet_form")
                    },
                    onSelectPet = { perro ->
                        petsViewModel.seleccionarPerro(perro)
                        navController.navigate("pet_detail")
                    },
                    viewModel = petsViewModel
                )
            }
            composable("pet_detail") {
                val perro = petsState.selectedPerro
                if (perro != null) {
                    PetDetailScreen(
                        perro = perro,
                        onEdit = {
                            navController.navigate("pet_form_edit")
                        },
                        onDelete = {
                            petsViewModel.eliminarPerro(perro.id) {
                                navController.popBackStack()
                            }
                        },
                        onConsultAi = { _ ->
                            navController.navigate(Screen.Ai.route)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            composable("pet_form") {
                PetFormScreen(
                    perroToEdit = null,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    viewModel = petsViewModel
                )
            }
            composable("pet_form_edit") {
                PetFormScreen(
                    perroToEdit = petsState.selectedPerro,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    viewModel = petsViewModel
                )
            }

            composable(Screen.Ai.route) {
                AiScreen(
                    onNavigateToMap = {
                        navController.navigate(Screen.Map.route)
                    }
                )
            }
            composable(Screen.Map.route) {
                MapScreen()
            }
            composable(Screen.Store.route) {
                StoreScreen(
                    onSelectProduct = { par ->
                        marketplaceViewModel.seleccionarProducto(par)
                        navController.navigate("product_detail")
                    },
                    onOpenAdmin = {
                        navController.navigate("admin_products")
                    },
                    onOpenCart = {
                        navController.navigate("cart")
                    },
                    viewModel = marketplaceViewModel,
                    cartViewModel = cartViewModel
                )
            }
            composable("product_detail") {
                val selected = marketplaceState.selectedProducto
                if (selected != null) {
                    ProductDetailScreen(
                        producto = selected.first,
                        inventario = selected.second,
                        onBack = { navController.popBackStack() },
                        cartViewModel = cartViewModel
                    )
                }
            }
            composable("cart") {
                CartScreen(
                    onOrderSuccess = {
                        navController.navigate("payment")
                    },
                    onNavigateToOrders = {
                        navController.navigate("orders")
                    },
                    viewModel = cartViewModel
                )
            }
            composable("payment") {
                val pedido = cartState.pedidoReciente ?: cartState.pedidos.firstOrNull()
                if (pedido != null) {
                    PaymentScreen(
                        pedido = pedido,
                        onPaymentFinished = { _ ->
                            navController.navigate("receipt") {
                                popUpTo("cart") { inclusive = true }
                            }
                        },
                        viewModel = paymentViewModel
                    )
                }
            }
            composable("receipt") {
                val pedido = cartState.pedidoReciente ?: cartState.pedidos.firstOrNull()
                if (pedido != null) {
                    ReceiptScreen(
                        pedido = pedido,
                        onBackToStore = {
                            navController.navigate(Screen.Store.route) {
                                popUpTo(Screen.Store.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("orders") {
                OrdersScreen(
                    viewModel = cartViewModel
                )
            }
            composable("admin_products") {
                AdminProductsScreen(
                    onAddProduct = {
                        marketplaceViewModel.seleccionarProducto(null)
                        navController.navigate("admin_product_form")
                    },
                    onEditProduct = { par ->
                        marketplaceViewModel.seleccionarProducto(par)
                        navController.navigate("admin_product_form_edit")
                    },
                    viewModel = marketplaceViewModel
                )
            }
            composable("admin_product_form") {
                AdminProductFormScreen(
                    productoToEdit = null,
                    inventarioToEdit = null,
                    onSaveSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                    viewModel = marketplaceViewModel
                )
            }
            composable("admin_product_form_edit") {
                val selected = marketplaceState.selectedProducto
                AdminProductFormScreen(
                    productoToEdit = selected?.first,
                    inventarioToEdit = selected?.second,
                    onSaveSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                    viewModel = marketplaceViewModel
                )
            }

            // Chat Routes
            composable("conversations") {
                ConversationsListScreen(
                    onOpenDirectory = {
                        chatViewModel.cargarDirectorio()
                        navController.navigate("user_directory")
                    },
                    onOpenChat = { conv ->
                        chatViewModel.abrirConversacionExistente(conv) {
                            navController.navigate("chat_detail")
                        }
                    },
                    viewModel = chatViewModel
                )
            }
            composable("user_directory") {
                UserDirectoryScreen(
                    onSelectUser = { usuario ->
                        chatViewModel.abrirChatCon(usuario) {
                            navController.navigate("chat_detail")
                        }
                    },
                    viewModel = chatViewModel
                )
            }
            composable("chat_detail") {
                ChatDetailScreen(
                    currentUid = authState.currentUser?.id ?: "",
                    onBack = { navController.popBackStack() },
                    viewModel = chatViewModel
                )
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToConversations = {
                        navController.navigate("conversations") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Calculator.route) {
                CalculatorScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }
        }
    }
}
