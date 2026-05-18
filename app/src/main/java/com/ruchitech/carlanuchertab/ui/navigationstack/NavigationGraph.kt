package com.ruchitech.carlanuchertab.ui.navigationstack

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ruchitech.carlanuchertab.helper.NavItem
import com.ruchitech.carlanuchertab.ui.btservices.BluetoothClientScreen
import com.ruchitech.carlanuchertab.ui.btservices.BluetoothServerScreen
import com.ruchitech.carlanuchertab.ui.screens.apps.AppUi
import com.ruchitech.carlanuchertab.ui.screens.dashboard.HomeScreen
import com.ruchitech.carlanuchertab.ui.screens.music.MusicScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController, startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(onNavigated = {
                when (it) {
            /*        is NavItem.Client -> {
                        navController.navigate(Screen.BluetoothClientScreen)
                    }

                    is NavItem.Server -> {
                        navController.navigate(Screen.BluetoothServerScreen)
                    }*/
                    is NavItem.AllApps -> {
                        navController.navigate(Screen.Apps) {
                            launchSingleTop =
                                true          // Prevents multiple copies of the destination
                        }
                    }

                    is NavItem.Music -> {
                        navController.navigate(Screen.Music) {
                            launchSingleTop = true
                        }
                    }

                    else -> {
                        //navController.navigate(Screen.Apps)
                    }
                }
            })
        }

        composable<Screen.Apps> { backStackEntry ->
            //   val profile = backStackEntry.toRoute<Screen.Profile>()
            AppUi(onBack = {
                navController.popBackStack()
            })
        }

        composable<Screen.Music> {
            MusicScreen(onBack = {
                navController.popBackStack()
            })
        }

        composable<Screen.Settings> {
        }
        composable<Screen.BluetoothClientScreen> {
            BluetoothClientScreen()
        }

        composable<Screen.BluetoothServerScreen> {
            BluetoothServerScreen()
        }

    }
}
