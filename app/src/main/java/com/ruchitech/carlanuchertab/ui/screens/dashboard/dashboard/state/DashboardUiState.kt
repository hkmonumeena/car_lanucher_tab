package com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.state

import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem

data class DashboardUiState(
    val widgetItems: List<WidgetItem> = emptyList(),
    val wallpaperId: Int = 0,
    val isSnowfall: Boolean = false,
    val isEditMode: Boolean = false,
    val showSettings: Boolean = false,
    val showWallpaper: Boolean = false,
    val showFuelLogs: Boolean = false,
    val addFuelLog: Boolean = false
)
