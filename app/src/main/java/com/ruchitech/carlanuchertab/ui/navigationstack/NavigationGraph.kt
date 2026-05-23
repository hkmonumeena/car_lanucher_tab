package com.ruchitech.carlanuchertab.ui.navigationstack

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.carlanuchertab.helper.NavItem
import com.ruchitech.phonelink.protocol.PhoneRemoteCommand
import com.ruchitech.carlanuchertab.ui.btservices.BluetoothClientScreen
import com.ruchitech.carlanuchertab.ui.btservices.BluetoothServerScreen
import com.ruchitech.carlanuchertab.ui.screens.apps.AppUi
import com.ruchitech.carlanuchertab.ui.screens.dashboard.HomeScreen
import com.ruchitech.carlanuchertab.ui.screens.music.MusicScreen
import com.ruchitech.carlanuchertab.ui.screens.paireddevice.PairedDeviceScreen
import com.ruchitech.carlanuchertab.ui.screens.paireddevice.PairedDeviceRouterViewModel
import com.ruchitech.carlanuchertab.ui.screens.phonelink.PhoneLinkScreen
import com.ruchitech.carlanuchertab.ui.screens.phonelink.PhoneLinkRouterViewModel
import com.ruchitech.carlanuchertab.ui.screens.trips.TripsScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    val phoneLinkRouter: PhoneLinkRouterViewModel = hiltViewModel()
    val pairedDeviceRouter: PairedDeviceRouterViewModel = hiltViewModel()
    LaunchedEffect(phoneLinkRouter) {
        phoneLinkRouter.navigationEvents.collect { command ->
            when (command) {
                PhoneRemoteCommand.OpenMusic -> navController.navigate(Screen.Music) { launchSingleTop = true }
                PhoneRemoteCommand.OpenApps -> navController.navigate(Screen.Apps) { launchSingleTop = true }
                PhoneRemoteCommand.OpenTrips -> navController.navigate(Screen.Trips) { launchSingleTop = true }
                else -> Unit
            }
        }
    }
    LaunchedEffect(pairedDeviceRouter) {
        pairedDeviceRouter.warmUp()
    }

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

                    is NavItem.Fuel -> {
                        navController.navigate(Screen.Trips) {
                            launchSingleTop = true
                        }
                    }

                    is NavItem.PhoneLink -> {
                        navController.navigate(Screen.PhoneLink) {
                            launchSingleTop = true
                        }
                    }

                    is NavItem.PairedDevice -> {
                        navController.navigate(Screen.PairedDevice) {
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

        composable<Screen.Trips> {
            TripsScreen(onBack = {
                navController.popBackStack()
            })
        }

        composable<Screen.PhoneLink> {
            PhoneLinkScreen(onBack = {
                navController.popBackStack()
            })
        }

        composable<Screen.PairedDevice> {
            PairedDeviceScreen(onBack = {
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
