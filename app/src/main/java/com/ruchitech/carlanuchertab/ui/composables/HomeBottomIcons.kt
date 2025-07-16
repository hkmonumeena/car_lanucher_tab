package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ruchitech.carlanuchertab.helper.NavItem
import com.ruchitech.carlanuchertab.helper.navItems

@Composable
fun HomeBottomIcons(onClick: (navItem: NavItem) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp),

        ) {
        navItems.forEach { item ->
            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .width(80.dp)
                    .padding(vertical = 5.dp)
                    .horizontalScroll(rememberScrollState())
                    .clickable {
                        onClick(item)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp) // Size of the circle
                        .background(
                            color = if (item is NavItem.AllApps) Color(0xFF2C2C2E) else Color.White,
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = Color(0xFFB7B8BB),
                            shape = CircleShape
                        )
                        .padding(4.dp), // Padding inside the circle
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        colorFilter = if (item is NavItem.AllApps) ColorFilter.tint(Color.White) else ColorFilter.tint(
                            Color.Black
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    color = Color.White,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }


}