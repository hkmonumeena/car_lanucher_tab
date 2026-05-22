package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CockpitPalette {
    val BackgroundTop = Color(0xFF080D12)
    val BackgroundMid = Color(0xFF111922)
    val BackgroundBottom = Color(0xFF05080C)
    val SurfaceTop = Color(0xEE121A22)
    val SurfaceBottom = Color(0xE80A1017)
    val SurfaceRaised = Color(0xFF18222C)
    val SurfacePressed = Color(0xFF223241)
    val Border = Color(0x36D7E2EA)
    val BorderStrong = Color(0x525CE1E6)
    val Accent = Color(0xFF5CE1E6)
    val AccentSoft = Color(0xFF2E7D86)
    val TextPrimary = Color(0xFFF4F7FA)
    val TextSecondary = Color(0xB8D6DEE8)
    val TextMuted = Color(0x80D6DEE8)
    val Danger = Color(0xFFFF8A80)
    val Success = Color(0xFF69F0AE)
    val OnAccent = Color(0xFF061014)
}

object CockpitDimens {
    val PanelRadius = 18.dp
    val ControlRadius = 10.dp
    val CompactRadius = 8.dp
    val PanelPadding = 14.dp
}

fun cockpitBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        CockpitPalette.BackgroundTop,
        CockpitPalette.BackgroundMid,
        CockpitPalette.BackgroundBottom
    )
)

fun cockpitSurfaceBrush(): Brush = Brush.verticalGradient(
    colors = listOf(CockpitPalette.SurfaceTop, CockpitPalette.SurfaceBottom)
)

@Composable
fun CockpitSurface(
    modifier: Modifier = Modifier,
    radius: Dp = CockpitDimens.PanelRadius,
    elevated: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .then(
                if (elevated) {
                    Modifier.shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(radius),
                        ambientColor = Color.Black.copy(alpha = 0.48f),
                        spotColor = Color.Black.copy(alpha = 0.32f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(radius))
            .background(cockpitSurfaceBrush())
            .border(1.dp, CockpitPalette.Border, RoundedCornerShape(radius))
    ) {
        content()
    }
}

@Composable
fun CockpitControlChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CockpitDimens.ControlRadius))
            .background(
                if (selected) CockpitPalette.Accent.copy(alpha = 0.20f)
                else Color.White.copy(alpha = 0.065f)
            )
            .border(
                1.dp,
                if (selected) CockpitPalette.BorderStrong else CockpitPalette.Border,
                RoundedCornerShape(CockpitDimens.ControlRadius)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFFCFFBFF) else CockpitPalette.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun CockpitIconControl(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CockpitDimens.ControlRadius))
            .background(
                if (selected) CockpitPalette.Accent.copy(alpha = 0.18f)
                else Color.White.copy(alpha = 0.07f)
            )
            .border(
                1.dp,
                if (selected) CockpitPalette.BorderStrong else CockpitPalette.Border,
                RoundedCornerShape(CockpitDimens.ControlRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) CockpitPalette.Accent else CockpitPalette.TextPrimary
        )
    }
}

@Composable
fun CockpitSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = CockpitPalette.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
