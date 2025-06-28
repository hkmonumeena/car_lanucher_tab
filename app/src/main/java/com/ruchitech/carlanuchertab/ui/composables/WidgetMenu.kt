package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            text = "Remove All Widgets",
            icon = Icons.Default.Delete,
            action = WidgetMenuAction.RemoveAllWidgets
        ),
        MenuItem(
            text = "Wallpapers",
            icon = Icons.Default.Edit,
            action = WidgetMenuAction.EditWidgets
        )
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(Color(0xFF2D2D2D))
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.text,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                onClick = {
                    onDismissRequest()
                    onMenuAction(item.action)
                }/*,
                leadingIcon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }*/
            )
        }
    }
}