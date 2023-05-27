package com.example.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.Screen

@Composable
fun SetupNavGraph(navController: NavHostController, connected: Boolean, dataHandler: DataHandlerInterface, viewModel: MainViewModel) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(route = Screen.Splash.route) {
            AnimatedSplashScreen(navController, AppStrings.version)
        }
        composable(route = Screen.Home.route) {
            MainScreen(connected = connected, dataHandler = dataHandler, viewModel = viewModel)
        }
    }
}