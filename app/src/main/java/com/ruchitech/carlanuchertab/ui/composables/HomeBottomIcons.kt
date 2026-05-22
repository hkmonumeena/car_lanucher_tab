package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ruchitech.carlanuchertab.helper.NavItem
import com.ruchitech.carlanuchertab.helper.navItems

@Composable
fun HomeBottomIcons(
    modifier: Modifier = Modifier,
    onClick: (navItem: NavItem) -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 108.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                HomeDockItem(
                    item = item,
                    onClick = { onClick(item) }
                )
            }
        }
    }
}

@Composable
private fun HomeDockItem(
    item: NavItem,
    onClick: () -> Unit,
) {
    val isAllApps = item is NavItem.AllApps
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(14.dp))
                .then(
                    if (isAllApps) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(
                                    CockpitPalette.AccentSoft,
                                    CockpitPalette.Accent
                                )
                            )
                        )
                    } else {
                        Modifier
                            .background(Color.White.copy(alpha = 0.075f))
                            .border(1.dp, CockpitPalette.Border, RoundedCornerShape(14.dp))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.label,
                colorFilter = ColorFilter.tint(
                    if (isAllApps) CockpitPalette.OnAccent else CockpitPalette.TextPrimary
                ),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.label,
            color = CockpitPalette.TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
