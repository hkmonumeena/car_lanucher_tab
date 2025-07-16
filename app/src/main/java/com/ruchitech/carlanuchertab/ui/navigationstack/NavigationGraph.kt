package com.ruchitech.carlanuchertab.ui.navigationstack

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ruchitech.carlanuchertab.ui.screens.apps.AppUi
import com.ruchitech.carlanuchertab.ui.screens.dashboard.HomeScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(onNavigated = {
                navController.navigate(Screen.Apps)
            })
        }

        composable<Screen.Apps> { backStackEntry ->
            //   val profile = backStackEntry.toRoute<Screen.Profile>()
            AppUi(onBack = {
                navController.popBackStack()
            })
        }

        composable<Screen.Settings> {

        }
    }
}