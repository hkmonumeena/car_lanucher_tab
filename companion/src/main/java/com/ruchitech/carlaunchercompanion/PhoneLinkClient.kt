package com.ruchitech.carlaunchercompanion

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.ruchitech.phonelink.protocol.PhoneLinkDefaults
import com.ruchitech.phonelink.protocol.PhoneLinkJson
import com.ruchitech.phonelink.protocol.PhoneLinkMessage
import com.ruchitech.phonelink.protocol.PhoneRemoteCommand
import com.ruchitech.phonelink.protocol.PhoneSongMetadata
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CompanionUiState(
    val connected: Boolean = false,
    val paired: Boolean = false,
    val message: String = "Not connected",
    val selectedTitle: String? = null,
    val selectedArtist: String? = null,
)

class PhoneLinkClient(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val streamServer = PhoneStreamServer(context)
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var host: String = ""
    private var port: Int = PhoneLinkDefaults.CONTROL_PORT

    private val _state = MutableStateFlow(CompanionUiState())
    val state: StateFlow<CompanionUiState> = _state.asStateFlow()

    fun connect(host: String, port: Int, token: String) {
        this.host = host.trim()
        this.port = port
        scope.launch {
            runCatching {
                val connectedSocket = Socket(this@PhoneLinkClient.host, this@PhoneLinkClient.port)
                socket = connectedSocket
                writer = BufferedWriter(OutputStreamWriter(connectedSocket.getOutputStream()))
                _state.value = _state.value.copy(connected = true, message = "Connected, pairing")
                send(
                    PhoneLinkMessage.PairRequest(
                        token = token.trim(),
                        deviceName = Build.MODEL ?: "Android Phone",
                        deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "phone",
                    )
                )
                listen(connectedSocket)
            }.onFailure {
                _state.value = _state.value.copy(connected = false, paired = false, message = it.message ?: "Connect failed")
            }
        }
    }

    fun playUri(uri: Uri) {
        scope.launch {
            runCatching {
                streamServer.setSource(uri)
                streamServer.start()
                val metadata = readMetadata(uri)
                val url = "http://${localIpAddress()}:${PhoneLinkDefaults.PHONE_STREAM_PORT}/stream"
                send(PhoneLinkMessage.PlayStream(streamUrl = url, metadata = metadata))
                _state.value = _state.value.copy(
                    selectedTitle = metadata.title,
                    selectedArtist = metadata.artist,
                    message = "Streaming ${metadata.title}"
                )
            }.onFailure {
                _state.value = _state.value.copy(message = it.message ?: "Unable to stream selected song")
            }
        }
    }

    fun remote(command: PhoneRemoteCommand, value: Long = 0L) {
        scope.launch {
            send(PhoneLinkMessage.RemoteCommand(command, value))
        }
    }

    private suspend fun listen(socket: Socket) = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        while (!socket.isClosed) {
            val raw = reader.readLine() ?: break
            when (val message = PhoneLinkJson.decode(raw)) {
                is PhoneLinkMessage.PairResult -> {
                    _state.value = _state.value.copy(paired = message.accepted, message = message.message)
                }
                is PhoneLinkMessage.PlaybackState -> {
                    _state.value = _state.value.copy(
                        selectedTitle = message.title ?: _state.value.selectedTitle,
                        selectedArtist = message.artist ?: _state.value.selectedArtist,
                        message = if (message.isPlaying) "Playing on stereo" else "Stereo paused"
                    )
                }
                is PhoneLinkMessage.Error -> _state.value = _state.value.copy(message = message.message)
                else -> Unit
            }
        }
        _state.value = _state.value.copy(connected = false, paired = false, message = "Disconnected")
    }

    private fun send(message: PhoneLinkMessage) {
        writer?.apply {
            write(PhoneLinkJson.encode(message))
            newLine()
            flush()
        }
    }

    private fun readMetadata(uri: Uri): PhoneSongMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() } ?: "Phone Song"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() } ?: "Phone"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() } ?: "Phone Stream"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            PhoneSongMetadata(
                id = uri.toString(),
                title = title,
                artist = artist,
                album = album,
                durationMs = duration,
                mimeType = context.contentResolver.getType(uri),
            )
        } finally {
            retriever.release()
        }
    }

    private fun localIpAddress(): String {
        return runCatching {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { !it.isLoopbackAddress }
                ?.hostAddress
        }.getOrNull() ?: "192.168.49.2"
    }
}
