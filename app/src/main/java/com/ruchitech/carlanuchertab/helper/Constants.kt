package com.ruchitech.carlanuchertab.helper

import com.ruchitech.carlanuchertab.R


sealed class BottomNavItem(
    val iconRes: Int,
    val label: String
) {
    object Map : BottomNavItem(R.drawable.map, "Map")
    object Radio : BottomNavItem(R.drawable.radio, "Radio")
    object Music : BottomNavItem(R.drawable.music, "Music")
    object AllApps : BottomNavItem(R.drawable.apps, "All Apps")

    companion object {
        val allItems = listOf(Map, Radio, Music, AllApps)
    }
}

sealed class WidgetMenuAction {
    object AddWidget : WidgetMenuAction()
    object RemoveAllWidgets : WidgetMenuAction()
    object EditWidgets : WidgetMenuAction()
    object Wallpapers : WidgetMenuAction()
    object Fuel : WidgetMenuAction()
    object Snowfall : WidgetMenuAction()
    object Fuels : WidgetMenuAction()

    companion object {
        val allActions = listOf(AddWidget, RemoveAllWidgets, EditWidgets)
    }
}

val bottomNavItems = BottomNavItem.allItems

val wallpapers = listOf(
    R.drawable.launcher_bg1,
    R.drawable.launcher_bg3,
    R.drawable.launcher_bg4,
    R.drawable.launcher_bg5,
    R.drawable.launcher_bg6,
    R.drawable.launcher_bg7,
    R.drawable.launcher_bg8,
)



