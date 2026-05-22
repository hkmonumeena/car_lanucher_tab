package com.ruchitech.carlanuchertab.ui.composables

import android.R.attr.contentDescription
import android.R.attr.maxLines
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import com.ruchitech.carlanuchertab.roomdb.data.fuelMonthSummaryNow

@Composable
fun FuelLogsList(
    fuelLogs: List<FuelLog>,
    onClose: () -> Unit = {},
    onAddNew: () -> Unit = {},
    onDelete: (FuelLog) -> Unit = {},
    embeddedInParentScroll: Boolean = false,
) {
    if (embeddedInParentScroll) {
        EmbeddedFuelLedger(
            fuelLogs = fuelLogs,
            onAddNew = onAddNew,
            onDelete = onDelete,
        )
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Premium Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A3D52),
                            Color(0xFF1A2E42)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1f, 0f)
                    )
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "FUEL HISTORY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF5D8BF4)
                    )
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF7D8FA1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Add New Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (fuelLogs.isNotEmpty()) {
                    val (monthCount, monthTotal) = fuelMonthSummaryNow(fuelLogs)
                    Text(
                        text = "This month: $monthCount fills · ₹$monthTotal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Button(
                onClick = onAddNew,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5D8BF4),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD NEW ENTRY", fontWeight = FontWeight.Bold)
                }
            }
            }
        }

        // List Items
        if (fuelLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No fuel entries yet",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF7D8FA1))
                )
            }
        } else {
            if (embeddedInParentScroll) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fuelLogs.forEach { log ->
                        PremiumFuelLogItem(log, onDelete = onDelete)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(fuelLogs, key = { it.id }) { log ->
                        PremiumFuelLogItem(log, onDelete = onDelete)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmbeddedFuelLedger(
    fuelLogs: List<FuelLog>,
    onAddNew: () -> Unit,
    onDelete: (FuelLog) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (fuelLogs.isNotEmpty()) {
            val (monthCount, monthTotal) = fuelMonthSummaryNow(fuelLogs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = 0.045f))
                    .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "MONTH",
                    color = Color(0xFF7D8FA1),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$monthCount fills",
                    color = Color(0xFFE2E8F0),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "₹$monthTotal",
                    color = Color(0xFF5D8BF4),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (fuelLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = 0.035f))
                    .clickable(onClick = onAddNew),
                contentAlignment = Alignment.Center
            ) {
                Text("No fuel logs. Tap to add.", color = Color(0xFF7D8FA1), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        } else {
            fuelLogs.take(8).forEach { log ->
                CompactFuelLogRow(log = log, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun CompactFuelLogRow(
    log: FuelLog,
    onDelete: (FuelLog) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.045f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(7.dp))
            .pointerInput(log.id) {
                detectTapGestures(onLongPress = { onDelete(log) })
            }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF5D8BF4).copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color(0xFF5D8BF4), modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(log.date, color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(log.location ?: log.time, color = Color(0xFF7D8FA1), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        log.liters?.let {
            Text("%.1f L".format(it), color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
        Text("₹${log.rupee}", color = Color(0xFFE2E8F0), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun PremiumFuelLogItem(
    log: FuelLog,
    onDelete: (FuelLog) -> Unit // 👈 callback
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onDelete(log) // 👈 invoke callback
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A3D52),
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column - Date/Time/Location
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = log.date,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D8BF4)
                        )
                    )
                    Text(
                        text = log.time,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                }

                log.location?.let { location ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Middle Column - Fuel Data
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                log.liters?.let { liters ->
                    Text(
                        text = "%.1f L".format(liters),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                log.fuelPrice?.let { price ->
                    Text(
                        text = "₹%.2f/L".format(price),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Right Column - Cost
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹ ${log.rupee}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0),
                    )
                )

            //    Spacer(modifier = Modifier.height(8.dp))

       /*         // Calculate efficiency if possible
                if (log.liters != null && log.liters > 0) {
                    val kmPerLiter = log.rupee.toFloat() / (log.fuelPrice ?: 1f)
                    Text(
                        text = "%.1f km/L".format(kmPerLiter),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when {
                                kmPerLiter > 15 -> Color(0xFF4CAF50) // Green
                                kmPerLiter > 10 -> Color(0xFFFFC107) // Yellow
                                else -> Color(0xFFF44336) // Red
                            }
                        )
                    )
                }*/
            }
        }
    }
}
