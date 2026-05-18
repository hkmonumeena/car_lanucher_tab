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
    val Accent = Color(0xFF5CE1E6)
    val AccentSoft = Color(0xFF3A9EA3)
    val TextPrimary = Color(0xFFF4F7FA)
    val TextSecondary = Color(0xB8C5D3)
    val GlassTop = Color(0xE6101820)
    val GlassBottom = Color(0xD8141E2A)
    val GlassBorder = Color(0x28FFFFFF)
    val DockTop = Color(0xE0121A24)
    val DockBottom = Color(0xD0182430)
    val Success = Color(0xFF69F0AE)
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
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(HomePalette.GlassTop, HomePalette.GlassBottom)
                )
            )
            .border(1.dp, HomePalette.GlassBorder, RoundedCornerShape(cornerRadius))
    ) {
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
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(HomePalette.DockTop, HomePalette.DockBottom)
                )
            )
            .border(1.dp, HomePalette.GlassBorder, RoundedCornerShape(26.dp))
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
