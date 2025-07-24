package com.ruchitech.carlanuchertab.helper

import com.ruchitech.carlanuchertab.R


sealed class NavItem(
    val iconRes: Int,
    val label: String,
) {
    object Map : NavItem(R.drawable.map, "Map")
    object Radio : NavItem(R.drawable.radio, "Radio")
    object Music : NavItem(R.drawable.music, "Music")
    object Fuel : NavItem(R.drawable.fuel_add, "Fuel")
    object Settings : NavItem(R.drawable.my_settings, "Settings")
    object AllApps : NavItem(R.drawable.apps, "All Apps")
    object Client : NavItem(R.drawable.apps, "Client")
    object Server : NavItem(R.drawable.apps, "Server")

    companion object {
        val allItems = listOf(Radio, Music, Fuel, Settings, Client, Server)
    }
}

sealed class WidgetMenuAction {
    object AddWidget : WidgetMenuAction()
    object RemoveAllWidgets : WidgetMenuAction()
    object EditWidgets : WidgetMenuAction()
    object Wallpapers : WidgetMenuAction()
    object Fuel : WidgetMenuAction()
    object Snowfall : WidgetMenuAction()
    object PairedDevices : WidgetMenuAction()
    object StartStopServer : WidgetMenuAction()
    object Fuels : WidgetMenuAction()

    companion object {
        val allActions = listOf(AddWidget, RemoveAllWidgets, EditWidgets)
    }
}

val navItems = NavItem.allItems

val wallpapers = listOf(
    R.drawable.launcher_bg1,
    R.drawable.launcher_bg2,
    R.drawable.launcher_bg3,
    R.drawable.launcher_bg4,
    R.drawable.launcher_bg5,
    R.drawable.launcher_bg6,
    R.drawable.launcher_bg7,
    R.drawable.launcher_bg8,
    R.drawable.launcher_bg9,
    R.drawable.launcher_bg10,
)



