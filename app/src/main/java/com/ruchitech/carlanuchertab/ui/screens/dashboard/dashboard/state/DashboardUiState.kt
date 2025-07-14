package com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.state

import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem

data class DashboardUiState(
    val widgetItems: List<WidgetItem> = emptyList(),
    val wallpaperId: Int = R.drawable.launcher_bg7,
    val isSnowfall: Boolean = false,
    val isEditMode: Boolean = false,
    val showSettings: Boolean = false,
    val showWallpaper: Boolean = false
)
