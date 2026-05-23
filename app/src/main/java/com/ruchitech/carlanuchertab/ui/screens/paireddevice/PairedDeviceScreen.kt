package com.ruchitech.carlanuchertab.ui.screens.paireddevice

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ruchitech.carlanuchertab.bluetoothmedia.BluetoothMediaManager
import com.ruchitech.carlanuchertab.bluetoothmedia.BluetoothMediaUiState
import com.ruchitech.carlanuchertab.bluetoothmedia.PairedBluetoothDevice
import com.ruchitech.carlanuchertab.ui.composables.CockpitPalette
import com.ruchitech.carlanuchertab.ui.composables.cockpitBackgroundBrush
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PairedDeviceViewModel @Inject constructor(
    private val bluetoothMediaManager: BluetoothMediaManager,
) : ViewModel() {
    val state = bluetoothMediaManager.state

    fun refresh() = bluetoothMediaManager.refreshDevices()
    fun scan() = bluetoothMediaManager.startDiscovery()
    fun pairOrReconnect(device: PairedBluetoothDevice) = bluetoothMediaManager.pairOrReconnect(device)
    fun reconnect() = bluetoothMediaManager.reconnectMostRecent()
    fun forget() = bluetoothMediaManager.forgetMostRecent()
    fun openBluetoothSettings() = bluetoothMediaManager.openBluetoothSettings()
    fun openNotificationSettings() = bluetoothMediaManager.openNotificationAccessSettings()
    fun playPause() = bluetoothMediaManager.playPause()
    fun next() = bluetoothMediaManager.next()
    fun previous() = bluetoothMediaManager.previous()
}

@HiltViewModel
class PairedDeviceRouterViewModel @Inject constructor(
    private val bluetoothMediaManager: BluetoothMediaManager,
) : ViewModel() {
    init {
        bluetoothMediaManager.refreshDevices()
        bluetoothMediaManager.reconnectMostRecent()
    }

    fun warmUp() = Unit
}

@Composable
fun PairedDeviceScreen(
    onBack: () -> Unit,
    viewModel: PairedDeviceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { viewModel.refresh() }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(requiredBluetoothPermissions())
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cockpitBackgroundBrush())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CockpitPalette.TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Paired Device", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("Bluetooth audio, metadata and steering-style controls", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
            }
            StatusPill(if (state.bluetoothEnabled) "BLUETOOTH ON" else "BLUETOOTH OFF", state.bluetoothEnabled)
        }

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ConnectionPanel(
                state = state,
                onScan = viewModel::scan,
                onReconnect = viewModel::reconnect,
                onForget = viewModel::forget,
                onBluetoothSettings = viewModel::openBluetoothSettings,
                onDeviceClick = viewModel::pairOrReconnect,
                modifier = Modifier
                    .width(330.dp)
                    .fillMaxHeight()
            )
            MediaPanel(
                state = state,
                onNotificationSettings = viewModel::openNotificationSettings,
                onPrevious = viewModel::previous,
                onPlayPause = viewModel::playPause,
                onNext = viewModel::next,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ConnectionPanel(
    state: BluetoothMediaUiState,
    onScan: () -> Unit,
    onReconnect: () -> Unit,
    onForget: () -> Unit,
    onBluetoothSettings: () -> Unit,
    onDeviceClick: (PairedBluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Panel(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (state.connectedDeviceName != null) CockpitPalette.Accent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = if (state.connectedDeviceName != null) CockpitPalette.Accent else CockpitPalette.TextMuted)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(if (state.connectedDeviceName != null) "CONNECTED" else if (state.connecting) "CONNECTING" else "READY", color = if (state.connectedDeviceName != null) CockpitPalette.Success else CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                Text(state.connectedDeviceName ?: state.mruDeviceName ?: "No phone connected", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.status, color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CompactButton("SCAN", onScan, modifier = Modifier.weight(1f), enabled = state.bluetoothEnabled)
            CompactButton("RECONNECT", onReconnect, modifier = Modifier.weight(1f), enabled = state.bluetoothEnabled && state.mruDeviceAddress != null)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            GhostButton("BT SETTINGS", onBluetoothSettings, modifier = Modifier.weight(1f))
            GhostButton("FORGET", onForget, modifier = Modifier.weight(1f), enabled = state.mruDeviceAddress != null || state.connectedDeviceAddress != null)
        }

        Text("DEVICES", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
            items(state.devices, key = { it.address }) { device ->
                DeviceRow(device = device, onClick = { onDeviceClick(device) })
            }
            if (state.devices.isEmpty()) {
                item {
                    EmptyHint(if (state.discovering) "Searching nearby phones..." else "Tap Scan or pair from system Bluetooth.")
                }
            }
        }
    }
}

@Composable
private fun MediaPanel(
    state: BluetoothMediaUiState,
    onNotificationSettings: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Panel(modifier = modifier) {
        if (!state.notificationAccessEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CockpitPalette.Danger.copy(alpha = 0.12f))
                    .border(1.dp, CockpitPalette.Danger.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Enable notification access for song metadata and controls.", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onNotificationSettings) { Text("ENABLE", color = CockpitPalette.Accent, fontWeight = FontWeight.ExtraBold) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Artwork(state)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("BT AUDIO", color = CockpitPalette.Accent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                        StatusPill(if (state.playing) "PLAYING" else "STANDBY", state.playing)
                    }
                    Text(
                        state.title ?: "Open media on phone",
                        color = CockpitPalette.TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        state.artist ?: "YouTube, Spotify and music apps route through Bluetooth",
                        color = CockpitPalette.TextSecondary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        state.album ?: "Metadata appears when the source app exposes it",
                        color = CockpitPalette.TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                CompactMediaConsole(
                    state = state,
                    onPrevious = onPrevious,
                    onPlayPause = onPlayPause,
                    onNext = onNext
                )
            }
        }
    }
}

@Composable
private fun Artwork(state: BluetoothMediaUiState) {
    Box(
        modifier = Modifier
            .size(260.dp)
            .shadow(18.dp, RoundedCornerShape(24.dp), ambientColor = CockpitPalette.Accent.copy(alpha = 0.18f), spotColor = Color.Black.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        CockpitPalette.Accent.copy(alpha = 0.26f),
                        CockpitPalette.SurfaceRaised,
                        CockpitPalette.BackgroundBottom
                    )
                )
            )
            .border(1.dp, CockpitPalette.BorderStrong, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = state.artwork
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Album artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = CockpitPalette.Accent, modifier = Modifier.size(82.dp))
                Text("BLUETOOTH", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun CompactMediaConsole(
    state: BluetoothMediaUiState,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.075f), Color.White.copy(alpha = 0.035f))))
            .border(1.dp, CockpitPalette.Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        ProgressBlock(state)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaButton(Icons.Default.SkipPrevious, "Previous", onPrevious)
            Spacer(modifier = Modifier.width(14.dp))
            MediaButton(if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", onPlayPause, large = true)
            Spacer(modifier = Modifier.width(14.dp))
            MediaButton(Icons.Default.SkipNext, "Next", onNext)
        }
    }
}

@Composable
private fun ProgressBlock(state: BluetoothMediaUiState) {
    val progress = if (state.durationMs > 0) {
        (state.progressMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = CockpitPalette.Accent,
            trackColor = Color.White.copy(alpha = 0.10f)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(state.progressMs), color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(if (state.durationMs > 0) formatMs(state.durationMs) else "--:--", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DeviceRow(device: PairedBluetoothDevice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(if (device.connected) CockpitPalette.Accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.055f))
            .border(1.dp, if (device.connected) CockpitPalette.BorderStrong else CockpitPalette.Border, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(if (device.bonded) Icons.Default.Bluetooth else Icons.Default.BluetoothSearching, contentDescription = null, tint = if (device.connected) CockpitPalette.Accent else CockpitPalette.TextSecondary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(device.name, color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (device.connected) "Connected" else if (device.bonded) "Paired" else "Tap to pair", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(CockpitPalette.SurfaceTop, CockpitPalette.SurfaceBottom)))
            .border(1.dp, CockpitPalette.Border, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun CompactButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(9.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CockpitPalette.Accent, contentColor = CockpitPalette.OnAccent)
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(9.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.07f), contentColor = CockpitPalette.TextPrimary)
    ) {
        Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MediaButton(icon: ImageVector, label: String, onClick: () -> Unit, large: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (large) 66.dp else 48.dp)
            .clip(CircleShape)
            .background(
                if (large) Brush.radialGradient(listOf(Color(0xFFBFFAFF), CockpitPalette.Accent))
                else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.055f)))
            )
            .border(1.dp, if (large) CockpitPalette.Accent.copy(alpha = 0.75f) else CockpitPalette.Border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = if (large) CockpitPalette.OnAccent else CockpitPalette.TextPrimary, modifier = Modifier.size(if (large) 34.dp else 24.dp))
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) CockpitPalette.Success.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.07f))
            .border(1.dp, if (active) CockpitPalette.Success.copy(alpha = 0.34f) else CockpitPalette.Border, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = if (active) CockpitPalette.Success else CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White.copy(alpha = 0.045f))
            .border(1.dp, CockpitPalette.Border, RoundedCornerShape(9.dp))
            .padding(12.dp)
    ) {
        Text(text, color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

private fun requiredBluetoothPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= 31) {
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

private fun formatMs(value: Long): String {
    val totalSeconds = (value / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = (totalSeconds % 60).toInt()
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
