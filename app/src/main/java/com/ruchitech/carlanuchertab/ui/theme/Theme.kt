package com.ruchitech.carlanuchertab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CockpitPrimary,
    onPrimary = CockpitOnPrimary,
    secondary = CockpitSecondary,
    tertiary = CockpitTertiary,
    background = CockpitBackground,
    surface = CockpitSurface,
    onBackground = CockpitOnSurface,
    onSurface = CockpitOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = CockpitPrimary,
    onPrimary = CockpitOnPrimary,
    secondary = CockpitSecondary,
    tertiary = CockpitTertiary,
    background = CockpitBackground,
    surface = CockpitSurface,
    onBackground = CockpitOnSurface,
    onSurface = CockpitOnSurface,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CarLanucherTabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme || !dynamicColor) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
