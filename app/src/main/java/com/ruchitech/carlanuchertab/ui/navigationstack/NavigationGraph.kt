package com.ruchitech.carlanuchertab.ui.navigationstack

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ruchitech.carlanuchertab.ui.screens.dashboard.HomeScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen()
        }

        composable<Screen.Profile> { backStackEntry ->
         //   val profile = backStackEntry.toRoute<Screen.Profile>()

        }

        composable<Screen.Settings> {

        }
    }
}