package com.kronnoz.reproductormusica.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kronnoz.reproductormusica.presentation.screens.PantallaPrincipal
import com.kronnoz.reproductormusica.presentation.screens.ListaReproduccion
import com.kronnoz.reproductormusica.presentation.navigation.AppNavigation


@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "principal") {
        composable("principal") { PantallaPrincipal(navController) }
        composable("lista") { ListaReproduccion(navController) }
    }
}
