package com.ruchitech.carlanuchertab.models

import androidx.compose.ui.graphics.vector.ImageVector
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction

data class MenuItem(
    val text: String,
    val icon: ImageVector,
    val action: WidgetMenuAction
)