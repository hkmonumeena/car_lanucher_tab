package com.ruchitech.carlanuchertab.ui.screens.phonelink

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ruchitech.carlanuchertab.phonelink.PhoneLinkManager
import com.ruchitech.carlanuchertab.ui.composables.CockpitPalette
import com.ruchitech.carlanuchertab.ui.composables.cockpitBackgroundBrush
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhoneLinkViewModel @Inject constructor(
    private val phoneLinkManager: PhoneLinkManager,
) : ViewModel() {
    val state = phoneLinkManager.state

    fun start() = phoneLinkManager.start()
    fun stop() = phoneLinkManager.stop()
    fun resetToken() = phoneLinkManager.resetToken()
    fun pairingPayload(): String = phoneLinkManager.pairingInfo().let {
        "${it.host}:${it.port}|${it.token}"
    }
}

@HiltViewModel
class PhoneLinkRouterViewModel @Inject constructor(
    phoneLinkManager: PhoneLinkManager,
) : ViewModel() {
    val navigationEvents = phoneLinkManager.navigationEvents

    init {
        phoneLinkManager.start()
    }
}

@Composable
fun PhoneLinkScreen(
    onBack: () -> Unit,
    viewModel: PhoneLinkViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cockpitBackgroundBrush())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CockpitPalette.TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Phone Link", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("Direct phone streaming and remote control", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
            }
        }

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LinkPanel(modifier = Modifier.width(260.dp)) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = CockpitPalette.Accent)
                Text(if (state.connected) "CONNECTED" else "WAITING", color = if (state.connected) CockpitPalette.Success else CockpitPalette.TextMuted, fontWeight = FontWeight.ExtraBold)
                Text(state.pairedDeviceName ?: "No phone paired", color = CockpitPalette.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Button(
                    onClick = { if (state.running) viewModel.stop() else viewModel.start() },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitPalette.Accent, contentColor = CockpitPalette.OnAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.running) "STOP LINK" else "START LINK", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = viewModel::resetToken,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = CockpitPalette.TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("NEW TOKEN", fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LinkPanel {
                    Text("PAIRING", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("Host ${state.hostAddress}:${state.port}", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Token ${state.token}", color = CockpitPalette.Accent, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text("QR payload: ${viewModel.pairingPayload()}", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelMedium)
                    Text("If Wi-Fi Direct is unavailable on this stereo firmware, connect phone through manual hotspot/IP fallback and use the same host/token.", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodySmall)
                }

                LinkPanel {
                    Text("NOW STREAMING", color = CockpitPalette.TextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(state.currentTitle ?: "No phone stream", color = CockpitPalette.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(state.currentArtist ?: state.lastMessage, color = CockpitPalette.TextMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun LinkPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1D2428), Color(0xFF101417))))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start,
        content = content
    )
}
