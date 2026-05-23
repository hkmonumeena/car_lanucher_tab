package com.ruchitech.phonelink.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PhoneLinkDefaults {
    const val SERVICE_NAME = "CarLauncher-Link"
    const val CONTROL_PORT = 47891
    const val PHONE_STREAM_PORT = 47892
}

@Serializable
data class PairingInfo(
    val host: String,
    val port: Int = PhoneLinkDefaults.CONTROL_PORT,
    val token: String,
    val serviceName: String = PhoneLinkDefaults.SERVICE_NAME,
)

@Serializable
data class PhoneSongMetadata(
    val id: String,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Phone Stream",
    val durationMs: Long = 0L,
    val mimeType: String? = null,
    val artworkBase64: String? = null,
)

@Serializable
sealed class PhoneLinkMessage {
    @Serializable
    @SerialName("pair_request")
    data class PairRequest(
        val token: String,
        val deviceName: String,
        val deviceId: String,
    ) : PhoneLinkMessage()

    @Serializable
    @SerialName("pair_result")
    data class PairResult(
        val accepted: Boolean,
        val message: String,
    ) : PhoneLinkMessage()

    @Serializable
    @SerialName("play_stream")
    data class PlayStream(
        val streamUrl: String,
        val metadata: PhoneSongMetadata,
    ) : PhoneLinkMessage()

    @Serializable
    @SerialName("remote_command")
    data class RemoteCommand(
        val command: PhoneRemoteCommand,
        val value: Long = 0L,
    ) : PhoneLinkMessage()

    @Serializable
    @SerialName("playback_state")
    data class PlaybackState(
        val title: String? = null,
        val artist: String? = null,
        val isPlaying: Boolean = false,
        val progressMs: Long = 0L,
        val durationMs: Long = 0L,
    ) : PhoneLinkMessage()

    @Serializable
    @SerialName("error")
    data class Error(
        val message: String,
    ) : PhoneLinkMessage()
}

@Serializable
enum class PhoneRemoteCommand {
    PlayPause,
    Stop,
    Next,
    Previous,
    SeekTo,
    VolumeUp,
    VolumeDown,
    OpenMusic,
    OpenApps,
    OpenTrips,
}

object PhoneLinkJson {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    fun encode(message: PhoneLinkMessage): String = json.encodeToString(PhoneLinkMessage.serializer(), message)
    fun decode(raw: String): PhoneLinkMessage = json.decodeFromString(PhoneLinkMessage.serializer(), raw)
    fun encodePairing(info: PairingInfo): String = json.encodeToString(PairingInfo.serializer(), info)
}
