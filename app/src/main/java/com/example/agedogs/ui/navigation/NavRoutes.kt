package com.example.agedogs.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Pets : Screen("pets", "Mascotas", Icons.Default.Pets)
    object Ai : Screen("ai", "IA", Icons.Default.AutoAwesome)
    object Map : Screen("map", "Mapa", Icons.Default.Map)
    object Store : Screen("store", "Tienda", Icons.Default.Storefront)
    object More : Screen("more", "Más", Icons.Default.MoreHoriz)

    object Calculator : Screen("calculator", "Calculadora", Icons.Default.Pets)
    object Login : Screen("login", "Login", Icons.Default.Pets)
    object Register : Screen("register", "Registro", Icons.Default.Pets)
    object Forgot : Screen("forgot", "Recuperar", Icons.Default.Pets)
    object Profile : Screen("profile", "Perfil", Icons.Default.Pets)
}

// 5 elementos limpios en la barra inferior (IA se movió a botón flotante)
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Pets,
    Screen.Map,
    Screen.Store,
    Screen.More
)
