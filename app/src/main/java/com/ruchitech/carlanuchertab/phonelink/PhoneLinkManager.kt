package com.ruchitech.carlanuchertab.phonelink

import android.content.Context
import android.media.AudioManager
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.ruchitech.carlanuchertab.music.MusicPlayerManager
import com.ruchitech.phonelink.protocol.PairingInfo
import com.ruchitech.phonelink.protocol.PhoneLinkDefaults
import com.ruchitech.phonelink.protocol.PhoneLinkJson
import com.ruchitech.phonelink.protocol.PhoneLinkMessage
import com.ruchitech.phonelink.protocol.PhoneRemoteCommand
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PhoneLinkUiState(
    val running: Boolean = false,
    val connected: Boolean = false,
    val hostAddress: String = "0.0.0.0",
    val port: Int = PhoneLinkDefaults.CONTROL_PORT,
    val token: String = "",
    val pairedDeviceName: String? = null,
    val currentTitle: String? = null,
    val currentArtist: String? = null,
    val lastMessage: String = "Phone Link is idle",
)

@Singleton
class PhoneLinkManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicPlayerManager: MusicPlayerManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: BufferedWriter? = null

    private val _state = MutableStateFlow(PhoneLinkUiState(token = newToken(), hostAddress = localIpAddress()))
    val state: StateFlow<PhoneLinkUiState> = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<PhoneRemoteCommand>(extraBufferCapacity = 4)
    val navigationEvents: SharedFlow<PhoneRemoteCommand> = _navigationEvents.asSharedFlow()

    fun pairingInfo(): PairingInfo = PairingInfo(
        host = _state.value.hostAddress,
        port = _state.value.port,
        token = _state.value.token,
    )

    fun start() {
        if (_state.value.running) return
        scope.launch {
            runCatching {
                serverSocket = ServerSocket(PhoneLinkDefaults.CONTROL_PORT)
                _state.value = _state.value.copy(
                    running = true,
                    hostAddress = localIpAddress(),
                    lastMessage = "Waiting for phone connection"
                )
                while (serverSocket?.isClosed == false) {
                    val socket = serverSocket?.accept() ?: break
                    handleClient(socket)
                }
            }.onFailure { error ->
                Log.e("PhoneLink", "Server failed", error)
                _state.value = _state.value.copy(running = false, connected = false, lastMessage = error.message ?: "Server failed")
            }
        }
        startWifiDirectHostBestEffort()
    }

    fun stop() {
        runCatching { clientSocket?.close() }
        runCatching { serverSocket?.close() }
        clientSocket = null
        serverSocket = null
        writer = null
        _state.value = _state.value.copy(running = false, connected = false, lastMessage = "Phone Link stopped")
    }

    fun resetToken() {
        _state.value = _state.value.copy(token = newToken(), lastMessage = "Pairing token refreshed")
    }

    fun sendPlaybackState() {
        val player = musicPlayerManager.playerState.value
        send(
            PhoneLinkMessage.PlaybackState(
                title = player.currentTrack?.title,
                artist = player.currentTrack?.artist,
                isPlaying = player.isPlaying,
                progressMs = player.progressMs,
                durationMs = player.durationMs,
            )
        )
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        runCatching { clientSocket?.close() }
        clientSocket = socket
        writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
        _state.value = _state.value.copy(connected = false, lastMessage = "Phone connected, waiting for pairing")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        while (!socket.isClosed) {
            val line = reader.readLine() ?: break
            handleMessage(line)
        }
        _state.value = _state.value.copy(connected = false, lastMessage = "Phone disconnected")
    }

    private fun handleMessage(raw: String) {
        runCatching { PhoneLinkJson.decode(raw) }
            .onSuccess { message ->
                when (message) {
                    is PhoneLinkMessage.PairRequest -> handlePairRequest(message)
                    is PhoneLinkMessage.PlayStream -> {
                        musicPlayerManager.playPhoneStream(message.streamUrl, message.metadata)
                        _state.value = _state.value.copy(
                            currentTitle = message.metadata.title,
                            currentArtist = message.metadata.artist,
                            lastMessage = "Playing ${message.metadata.title}"
                        )
                    }
                    is PhoneLinkMessage.RemoteCommand -> handleRemoteCommand(message)
                    else -> Unit
                }
            }
            .onFailure {
                send(PhoneLinkMessage.Error("Invalid message"))
            }
    }

    private fun handlePairRequest(request: PhoneLinkMessage.PairRequest) {
        val accepted = request.token == _state.value.token
        _state.value = _state.value.copy(
            connected = accepted,
            pairedDeviceName = if (accepted) request.deviceName else _state.value.pairedDeviceName,
            lastMessage = if (accepted) "Paired with ${request.deviceName}" else "Rejected invalid token"
        )
        send(PhoneLinkMessage.PairResult(accepted, if (accepted) "Paired" else "Invalid token"))
        if (accepted) sendPlaybackState()
    }

    private fun handleRemoteCommand(message: PhoneLinkMessage.RemoteCommand) {
        when (message.command) {
            PhoneRemoteCommand.PlayPause -> musicPlayerManager.togglePlayback()
            PhoneRemoteCommand.Stop -> musicPlayerManager.clearQueue()
            PhoneRemoteCommand.Next -> musicPlayerManager.skipNext()
            PhoneRemoteCommand.Previous -> musicPlayerManager.skipPrevious()
            PhoneRemoteCommand.SeekTo -> musicPlayerManager.seekTo(message.value)
            PhoneRemoteCommand.VolumeUp -> adjustVolume(AudioManager.ADJUST_RAISE)
            PhoneRemoteCommand.VolumeDown -> adjustVolume(AudioManager.ADJUST_LOWER)
            PhoneRemoteCommand.OpenMusic,
            PhoneRemoteCommand.OpenApps,
            PhoneRemoteCommand.OpenTrips -> _navigationEvents.tryEmit(message.command)
        }
        _state.value = _state.value.copy(lastMessage = "Remote: ${message.command.name}")
        sendPlaybackState()
    }

    private fun adjustVolume(direction: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
    }

    private fun send(message: PhoneLinkMessage) {
        runCatching {
            writer?.apply {
                write(PhoneLinkJson.encode(message))
                newLine()
                flush()
            }
        }
    }

    private fun startWifiDirectHostBestEffort() {
        val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager ?: return
        val channel = manager.initialize(context, context.mainLooper, null) ?: return
        try {
            manager.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    _state.value = _state.value.copy(lastMessage = "Wi-Fi Direct group ready")
                }

                override fun onFailure(reason: Int) {
                    _state.value = _state.value.copy(lastMessage = "Use QR fallback if Wi-Fi Direct fails ($reason)")
                }
            })
        } catch (error: SecurityException) {
            _state.value = _state.value.copy(lastMessage = "Grant nearby Wi-Fi permission or use QR fallback")
        }
    }

    private fun newToken(): String = (100000..999999).random().toString()

    private fun localIpAddress(): String {
        return runCatching {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { !it.isLoopbackAddress }
                ?.hostAddress
        }.getOrNull() ?: "192.168.49.1"
    }
}
