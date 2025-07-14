package com.ruchitech.carlanuchertab.ui.navigationstack

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()
    @Serializable
    data class Profile(val userId: String) : Screen()
    @Serializable
    data object Settings : Screen()
}