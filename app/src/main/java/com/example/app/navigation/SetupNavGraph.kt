package com.example.app

import RecordScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.data.DataHandlerInterface
import com.example.app.navigation.Screen

@Composable
fun SetupNavGraph(navController: NavHostController, connected: Boolean, dataHandler: DataHandlerInterface, viewModel: MainViewModel) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(route = Screen.Splash.route) {
            AnimatedSplashScreen(navController, AppStrings.version)
        }
        composable(route = Screen.Home.route) {
            MainScreen(connected = connected, dataHandler = dataHandler, viewModel = viewModel, navController)
        }
        composable(route = Screen.Record.route) {
            RecordScreen(viewModel = viewModel, navController)
        }
    }
}