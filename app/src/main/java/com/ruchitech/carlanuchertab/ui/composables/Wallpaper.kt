package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource


@Composable
fun Wallpaper(currentWallpaper: Int, modifier: Modifier) {
    Image(
        painter = painterResource(currentWallpaper),
        contentDescription = null,
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Black.copy(alpha = 1f)),
                    startY = 0f,
                    endY = 500f
                )
            ),
        contentScale = ContentScale.FillWidth
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0f)) // Adjust alpha (0.0-1.0)
    )
}