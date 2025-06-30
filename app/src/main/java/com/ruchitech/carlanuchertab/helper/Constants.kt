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

    companion object {
        val allActions = listOf(AddWidget, RemoveAllWidgets, EditWidgets)
    }
}

val bottomNavItems = BottomNavItem.allItems



