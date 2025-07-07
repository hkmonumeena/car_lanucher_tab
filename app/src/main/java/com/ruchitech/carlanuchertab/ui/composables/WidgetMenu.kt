package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
import com.ruchitech.carlanuchertab.models.MenuItem

@Composable
fun WidgetsDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onMenuAction: (WidgetMenuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        MenuItem(
            text = "Add Widget",
            icon = Icons.Default.Add,
            action = WidgetMenuAction.AddWidget
        ),
        MenuItem(
            text = "Edit Widget",
            icon = Icons.Default.Add,
            action = WidgetMenuAction.EditWidgets
        ),
        MenuItem(
            text = "Remove All Widgets",
            icon = Icons.Default.Delete,
            action = WidgetMenuAction.RemoveAllWidgets
        ),
        MenuItem(
            text = "Wallpapers",
            icon = Icons.Default.Edit,
            action = WidgetMenuAction.Wallpapers
        ) ,
/*        MenuItem(
            text = "Fuels",
            icon = Icons.Default.Edit,
            action = WidgetMenuAction.Fuels
        ) ,*/
        MenuItem(
            text = "Snowfall",
            icon = Icons.Default.Edit,
            action = WidgetMenuAction.Snowfall
        )
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .background(
                color = Color(0xFF1E293B), // Dark blue-gray
                shape = RoundedCornerShape(12.dp) // Rounded corners
            )
            .border(
                width = 1.dp,
                color = Color(0xFF3A5A78).copy(alpha = 0.5f), // Metallic border
                shape = RoundedCornerShape(12.dp)
            )
            .width(IntrinsicSize.Max)
    ) {
        menuItems.forEachIndexed { index, item ->
            // Add divider between items except after last item
            if (index > 0) {
                Divider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color(0xFF3A5A78).copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }

            DropdownMenuItem(
                text = {
                    Text(
                        text = item.text,
                        color = Color(0xFFE2E8F0), // Light gray
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                },
                onClick = {
                    onDismissRequest()
                    onMenuAction(item.action)
                },
                modifier = Modifier
                    .height(48.dp) // Consistent touch target size
                    .background(
                        color =Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = MenuDefaults.itemColors(
                    textColor = Color(0xFFE2E8F0),
                    leadingIconColor = Color(0xFF5D8BF4), // Blue accent
                    trailingIconColor = Color(0xFF94A3B8), // Gray
                    disabledTextColor = Color(0xFF94A3B8).copy(alpha = 0.5f),
                )
            )
        }
    }}