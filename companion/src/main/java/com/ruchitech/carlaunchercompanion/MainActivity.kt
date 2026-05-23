package com.ruchitech.carlaunchercompanion

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ruchitech.phonelink.protocol.PhoneLinkDefaults
import com.ruchitech.phonelink.protocol.PhoneRemoteCommand

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    CompanionApp(client = remember { PhoneLinkClient(applicationContext) })
                }
            }
        }
    }
}

@Composable
private fun CompanionApp(client: PhoneLinkClient) {
    val state by client.state.collectAsState()
    var host by rememberSaveable { mutableStateOf("192.168.49.1") }
    var port by rememberSaveable { mutableStateOf(PhoneLinkDefaults.CONTROL_PORT.toString()) }
    var token by rememberSaveable { mutableStateOf("") }
    var seekValue by rememberSaveable { mutableStateOf("0") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(client::playUri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111417), Color(0xFF050607))))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Car Link", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(state.message, color = if (state.paired) Color(0xFF55D88A) else Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)

        Panel {
            OutlinedTextField(host, { host = it }, label = { Text("Stereo host/IP") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(port, { port = it.filter(Char::isDigit) }, label = { Text("Port") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(token, { token = it.filter(Char::isDigit) }, label = { Text("Token") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            PrimaryButton("Connect") {
                client.connect(host, port.toIntOrNull() ?: PhoneLinkDefaults.CONTROL_PORT, token)
            }
        }

        Panel {
            Text(state.selectedTitle ?: "No song selected", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(state.selectedArtist ?: "Pick a phone song to stream", color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
            PrimaryButton("Pick & Stream Song", icon = { Icon(Icons.Default.UploadFile, null) }) {
                picker.launch("audio/*")
            }
        }

        Panel {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                IconRemoteButton(Modifier.weight(1f), PhoneRemoteCommand.Previous, client) { Icon(Icons.Default.SkipPrevious, null) }
                IconRemoteButton(Modifier.weight(1f), PhoneRemoteCommand.PlayPause, client) { Icon(Icons.Default.PlayArrow, null) }
                IconRemoteButton(Modifier.weight(1f), PhoneRemoteCommand.Stop, client) { Icon(Icons.Default.Pause, null) }
                IconRemoteButton(Modifier.weight(1f), PhoneRemoteCommand.Next, client) { Icon(Icons.Default.SkipNext, null) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                IconRemoteButton(Modifier.weight(1f), PhoneRemoteCommand.VolumeDown, client) { Icon(Icons.Default.VolumeDown, null) }
                IconRemoteButton(Modifier.weight(1f), PhoneRemoteCommand.VolumeUp, client) { Icon(Icons.Default.VolumeUp, null) }
                OutlinedTextField(seekValue, { seekValue = it.filter(Char::isDigit) }, label = { Text("Seek ms") }, modifier = Modifier.weight(1.4f), singleLine = true)
                PrimaryButton("Seek", modifier = Modifier.weight(1f)) {
                    client.remote(PhoneRemoteCommand.SeekTo, seekValue.toLongOrNull() ?: 0L)
                }
            }
        }

        Panel {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PrimaryButton("Music", Modifier.weight(1f), icon = { Icon(Icons.Default.MusicNote, null) }) { client.remote(PhoneRemoteCommand.OpenMusic) }
                PrimaryButton("Apps", Modifier.weight(1f), icon = { Icon(Icons.Default.Apps, null) }) { client.remote(PhoneRemoteCommand.OpenApps) }
                PrimaryButton("Trips", Modifier.weight(1f)) { client.remote(PhoneRemoteCommand.OpenTrips) }
            }
        }
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun PrimaryButton(
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D8BF4), contentColor = Color.White)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            icon?.invoke()
            Text(label, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IconRemoteButton(
    modifier: Modifier,
    command: PhoneRemoteCommand,
    client: PhoneLinkClient,
    icon: @Composable () -> Unit,
) {
    Button(
        onClick = { client.remote(command) },
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White)
    ) {
        icon()
    }
}
