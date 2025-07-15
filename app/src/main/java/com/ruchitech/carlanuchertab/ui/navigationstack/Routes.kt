package com.ruchitech.carlanuchertab.ui.navigationstack

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()
    @Serializable
    data object Apps : Screen()
    @Serializable
    data object Settings : Screen()
}