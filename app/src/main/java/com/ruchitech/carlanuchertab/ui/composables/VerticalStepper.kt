package com.ruchitech.carlanuchertab.ui.composables

import android.R.attr.contentDescription
import android.R.attr.maxLines
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog

@Composable
fun FuelLogsList(
    fuelLogs: List<FuelLog>,
    onClose: () -> Unit = {},
    onAddNew: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.LightGray.copy(0.85f), shape = RoundedCornerShape(0.dp))
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(                        brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF8B4513).copy(alpha = 0.5f), // Sienna
                        Color.Transparent
                    )
                )
                )
                .padding(horizontal = 10.dp),
        ) {
            Text(
                "Fuel Entries",
                style = TextStyle(fontWeight = FontWeight.W900, fontSize = 18.sp),
                modifier = Modifier.align(Alignment.CenterStart)
            )

            IconButton(
                onClick = {onClose()},
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = Color.Black
                )
            }
        }

        // List Items
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(fuelLogs) { log ->
                VintageFuelLogItem(log)
            }
        }
    }
}

@Composable
fun VintageFuelLogItem(
    log: FuelLog,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp), // Less rounded for retro look
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5E9D5) // Vintage cream paper color
        ),
        border = BorderStroke(1.dp, Color(0xFF8B4513).copy(alpha = 0.3f)) // Vintage border
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5E9D5),
                            Color(0xFFE8D9B5)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(16.dp)
        ) {
            // Retro header with badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Vintage badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFF8B0000), // Dark red
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFFFFD700), // Gold border
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filled_fuel),
                        contentDescription = "Fuel Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFD700) // Gold icon
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "₹${log.rupee}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = Color(0xFF8B0000), // Dark red
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.2f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier
                    )

                    log.fuelPrice?.let { price ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF8B4513).copy(alpha = 0.1f), // Sienna tint
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF8B4513).copy(alpha = 0.3f), // Sienna border
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "@ ₹%.2f/L".format(price),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B4513) // Sienna text
                                )
                            )
                        }
                    }
                    // Vintage sticker-style liters indicator
                    log.liters?.let { liters ->
                        Text(
                            text = "%.1f L".format(liters),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray.copy(0.60F)
                            )
                        )

                    }
                }


            }

            Spacer(modifier = Modifier.height(12.dp))

            // Retro divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF8B4513).copy(alpha = 0.5f), // Sienna
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Information section with retro styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
            ) {
                // Date/Time row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1F)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF556B2F), // Dark olive
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${log.date} • ${log.time}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF556B2F), // Dark olive
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        log.location?.let {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF556B2F), // Dark olive
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = log.location?:"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF556B2F), // Dark olive
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.65F),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                // Price per liter badge at bottom
            }
        }
    }
}
