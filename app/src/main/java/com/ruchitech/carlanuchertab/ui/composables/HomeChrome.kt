package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object HomePalette {
    val Accent = CockpitPalette.Accent
    val AccentSoft = CockpitPalette.AccentSoft
    val TextPrimary = CockpitPalette.TextPrimary
    val TextSecondary = CockpitPalette.TextSecondary
    val GlassTop = CockpitPalette.SurfaceTop
    val GlassBottom = CockpitPalette.SurfaceBottom
    val GlassBorder = CockpitPalette.Border
    val DockTop = Color(0xEE101922)
    val DockBottom = Color(0xE6070D13)
    val Success = CockpitPalette.Success
}

@Composable
fun HomeCinematicOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color(0x99080C12),
                            0.35f to Color(0x33080C12),
                            0.65f to Color(0x44080C12),
                            1f to Color(0xA0080C12)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to Color(0x88000000),
                            0.22f to Color.Transparent,
                            0.78f to Color.Transparent,
                            1f to Color(0x99000000)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.55f to Color.Transparent,
                            1f to Color(0x66080C12)
                        )
                    )
                )
        )
    }
}

@Composable
fun HomeGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    content: @Composable () -> Unit,
) {
    CockpitSurface(modifier = modifier, radius = cornerRadius) {
        content()
    }
}

@Composable
fun HomeDockPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(HomePalette.DockTop, HomePalette.DockBottom)
                )
            )
            .border(1.dp, HomePalette.GlassBorder, RoundedCornerShape(18.dp))
    ) {
        content()
    }
}

@Composable
fun HomeConnectionBadge(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A2E28).copy(alpha = 0.92f))
            .border(1.dp, HomePalette.Success.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "● Connected",
            color = HomePalette.Success,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
