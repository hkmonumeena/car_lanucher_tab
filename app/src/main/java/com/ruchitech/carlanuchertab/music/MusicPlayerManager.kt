package com.ruchitech.carlanuchertab.music

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class MusicPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateState(player)
        }
    }

    @Volatile
    private var playerInternal: ExoPlayer? = null
    private var progressJob: Job? = null
    private var queue: MutableList<MusicTrackEntity> = mutableListOf()

    private val _playerState = MutableStateFlow(MusicPlayerUiState())
    val playerState: StateFlow<MusicPlayerUiState> = _playerState.asStateFlow()

    fun getOrCreatePlayer(): ExoPlayer {
        playerInternal?.let { return it }
        synchronized(this) {
            playerInternal?.let { return it }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                playerInternal = buildPlayer()
            } else {
                val latch = CountDownLatch(1)
                mainHandler.post {
                    playerInternal = buildPlayer()
                    latch.countDown()
                }
                latch.await()
            }
            return playerInternal!!
        }
    }

    fun playTracks(
        tracks: List<MusicTrackEntity>,
        startIndex: Int,
    ) {
        mainHandler.post {
            val player = getOrCreatePlayer()
            queue = tracks.toMutableList()
            ensureServiceRunning()
            player.setMediaItems(
                tracks.map { it.toMediaItem() },
                startIndex.coerceIn(0, (tracks.lastIndex).coerceAtLeast(0)),
                0L
            )
            player.prepare()
            player.playWhenReady = true
            updateState(player)
        }
    }

    fun togglePlayback() {
        mainHandler.post {
            val player = getOrCreatePlayer()
            if (player.isPlaying) {
                player.pause()
            } else {
                ensureServiceRunning()
                player.playWhenReady = true
                player.play()
            }
            updateState(player)
        }
    }

    fun seekTo(positionMs: Long) {
        mainHandler.post {
            val player = getOrCreatePlayer()
            player.seekTo(positionMs.coerceAtLeast(0L))
            updateState(player)
        }
    }

    fun skipNext() {
        mainHandler.post {
            val player = getOrCreatePlayer()
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
                player.playWhenReady = true
            }
            updateState(player)
        }
    }

    fun skipPrevious() {
        mainHandler.post {
            val player = getOrCreatePlayer()
            if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem()
                player.playWhenReady = true
            } else {
                player.seekTo(0L)
            }
            updateState(player)
        }
    }

    fun onTrackDeleted(trackUri: String) {
        mainHandler.post {
            val player = playerInternal ?: return@post
            val deletedIndex = queue.indexOfFirst { it.uri == trackUri }
            if (deletedIndex == -1) return@post

            val shouldKeepPlaying = player.playWhenReady || player.isPlaying
            val wasCurrent = deletedIndex == player.currentMediaItemIndex
            queue.removeAt(deletedIndex)

            if (deletedIndex < player.mediaItemCount) {
                player.removeMediaItem(deletedIndex)
            }

            when {
                queue.isEmpty() -> {
                    player.stop()
                    player.clearMediaItems()
                }

                wasCurrent && deletedIndex >= queue.size -> {
                    player.seekTo(queue.lastIndex, 0L)
                    if (shouldKeepPlaying) {
                        player.playWhenReady = true
                    }
                }

                shouldKeepPlaying -> {
                    player.playWhenReady = true
                }
            }
            updateState(player)
        }
    }

    private fun ensureServiceRunning() {
        val intent = Intent(appContext, MusicPlaybackService::class.java)
        try {
            // Try starting as a regular service first. This works if the app is in the foreground.
            // It avoids the immediate requirement to call startForeground() within 5 seconds,
            // which can cause crashes if the player takes time to prepare/start.
            appContext.startService(intent)
        } catch (e: Exception) {
            // If the app is in the background, startService() might fail on API 26+.
            // We then fallback to startForegroundService().
            // MediaSessionService will call startForeground() once playback starts.
            ContextCompat.startForegroundService(appContext, intent)
        }
    }

    private fun buildPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(appContext)
            .setLooper(Looper.getMainLooper())
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()
        player.addListener(listener)
        startProgressUpdates()
        return player
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (true) {
                playerInternal?.let(::updateState)
                delay(500L)
            }
        }
    }

    private fun updateState(player: Player) {
        val currentIndex = player.currentMediaItemIndex
        val currentTrack = when {
            currentIndex in queue.indices -> queue[currentIndex]
            else -> queue.firstOrNull { it.uri == player.currentMediaItem?.mediaId }
        }
        _playerState.value = MusicPlayerUiState(
            currentTrack = currentTrack,
            currentQueue = queue.toList(),
            currentIndex = currentIndex,
            isPlaying = player.isPlaying,
            progressMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = if (player.duration > 0) player.duration else currentTrack?.durationMs ?: 0L,
            hasNext = player.hasNextMediaItem(),
            hasPrevious = player.hasPreviousMediaItem()
        )
    }

    private fun MusicTrackEntity.toMediaItem(): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)

        artworkPath?.let { path ->
            metadataBuilder.setArtworkUri(Uri.fromFile(File(path)))
        }

        return MediaItem.Builder()
            .setMediaId(uri)
            .setUri(uri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }
}
