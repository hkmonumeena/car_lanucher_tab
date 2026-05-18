package com.ruchitech.carlanuchertab.music

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class MusicPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDao: MusicDao,
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var prefsMirror: MusicPlaybackPrefsEntity = MusicPlaybackPrefsEntity()

    private var lastProgressPersistElapsed: Long = 0L
    private var fadeVolumeJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                onMediaItemTransitioned(player)
            }
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED) && !player.isPlaying) {
                persistPlaybackPrefsSnapshot(player)
            }
            updateState(player)
        }
    }

    @Volatile
    private var playerInternal: ExoPlayer? = null
    private var progressJob: Job? = null
    private var queue: MutableList<MusicTrackEntity> = mutableListOf()

    private val _playerState = MutableStateFlow(MusicPlayerUiState())
    val playerState: StateFlow<MusicPlayerUiState> = _playerState.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            val loaded = musicDao.getPlaybackPrefs() ?: MusicPlaybackPrefsEntity()
            withContext(Dispatchers.Main) {
                prefsMirror = loaded
                updateState(playerInternal)
            }
        }
    }

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
        startPositionMs: Long = 0L,
        playWhenReady: Boolean = true,
    ) {
        mainHandler.post {
            playTracksOnMain(tracks, startIndex, startPositionMs, playWhenReady)
        }
    }

    private fun playTracksOnMain(
        tracks: List<MusicTrackEntity>,
        startIndex: Int,
        startPositionMs: Long,
        playWhenReady: Boolean,
    ) {
        val player = getOrCreatePlayer()
        if (tracks.isEmpty()) {
            clearQueueOnMain()
            return
        }
        fadeVolumeJob?.cancel()
        fadeVolumeJob = null
        player.volume = 1f

        queue = tracks.toMutableList()
        ensureServiceRunning()
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val safeStart = startPositionMs.coerceAtLeast(0L)
        player.setMediaItems(
            tracks.map { it.toMediaItem() },
            safeIndex,
            safeStart,
        )
        player.shuffleModeEnabled = prefsMirror.shuffleEnabled
        player.repeatMode = prefsMirror.repeatMode
        player.prepare()
        player.playWhenReady = playWhenReady
        if (playWhenReady) {
            player.play()
        }
        updateState(player)
    }

    fun tryResumeFromLibrary(allTracks: List<MusicTrackEntity>) {
        if (allTracks.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            val loaded = musicDao.getPlaybackPrefs() ?: MusicPlaybackPrefsEntity()
            withContext(Dispatchers.Main) {
                prefsMirror = loaded
                val player = playerInternal
                if (player != null && player.mediaItemCount > 0) return@withContext
                val uri = loaded.lastTrackUri ?: return@withContext
                val idx = allTracks.indexOfFirst { it.uri == uri }
                if (idx < 0) return@withContext
                val pos = loaded.lastPositionMs.coerceAtLeast(0L)
                playTracksOnMain(allTracks, idx, pos, playWhenReady = false)
            }
        }
    }

    fun togglePlayback() {
        mainHandler.post {
            val player = getOrCreatePlayer()
            if (player.isPlaying) {
                player.pause()
                persistPlaybackPrefsSnapshot(player)
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

    fun toggleShuffle() {
        mainHandler.post {
            val player = getOrCreatePlayer()
            val next = !player.shuffleModeEnabled
            player.shuffleModeEnabled = next
            prefsMirror = prefsMirror.copy(shuffleEnabled = next)
            persistPlaybackPrefsAsync()
            updateState(player)
        }
    }

    fun cycleRepeatMode() {
        mainHandler.post {
            val player = getOrCreatePlayer()
            val next = when (player.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            player.repeatMode = next
            prefsMirror = prefsMirror.copy(repeatMode = next)
            persistPlaybackPrefsAsync()
            updateState(player)
        }
    }

    fun setCrossfadeMs(ms: Int) {
        mainHandler.post {
            prefsMirror = prefsMirror.copy(crossfadeMs = ms.coerceIn(0, 3000))
            persistPlaybackPrefsAsync()
            val player = playerInternal
            if (player != null) {
                updateState(player)
            } else {
                updateState(null)
            }
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        mainHandler.post {
            val player = getOrCreatePlayer()
            if (from !in queue.indices || to !in queue.indices || from == to) return@post
            val item = queue.removeAt(from)
            queue.add(to, item)
            player.moveMediaItem(from, to)
            updateState(player)
        }
    }

    fun removeQueueItem(index: Int) {
        mainHandler.post {
            val player = getOrCreatePlayer()
            if (index !in queue.indices) return@post

            val shouldKeepPlaying = player.playWhenReady || player.isPlaying
            val wasCurrent = index == player.currentMediaItemIndex
            queue.removeAt(index)

            if (index < player.mediaItemCount) {
                player.removeMediaItem(index)
            }

            when {
                queue.isEmpty() -> {
                    player.stop()
                    player.clearMediaItems()
                }

                wasCurrent && index >= queue.size -> {
                    player.seekTo(queue.lastIndex.coerceAtLeast(0), 0L)
                    if (shouldKeepPlaying) {
                        player.playWhenReady = true
                    }
                }

                shouldKeepPlaying -> {
                    player.playWhenReady = true
                }
            }
            persistPlaybackPrefsSnapshot(player)
            updateState(player)
        }
    }

    fun clearQueue() {
        mainHandler.post {
            clearQueueOnMain()
        }
    }

    private fun clearQueueOnMain() {
        fadeVolumeJob?.cancel()
        fadeVolumeJob = null
        queue.clear()
        val player = playerInternal
        if (player != null) {
            player.stop()
            player.clearMediaItems()
            player.volume = 1f
            updateState(player)
        } else {
            updateState(null)
        }
    }

    fun playQueueIndex(index: Int) {
        mainHandler.post {
            val player = getOrCreatePlayer()
            if (index !in queue.indices) return@post
            fadeVolumeJob?.cancel()
            player.volume = 1f
            player.seekTo(index, 0L)
            ensureServiceRunning()
            player.playWhenReady = true
            player.play()
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
            persistPlaybackPrefsSnapshot(player)
            updateState(player)
        }
    }

    private fun onMediaItemTransitioned(player: Player) {
        val uri = player.currentMediaItem?.mediaId ?: return
        scope.launch(Dispatchers.IO) {
            runCatching { musicDao.recordTrackPlayed(uri) }
        }
        if (prefsMirror.crossfadeMs > 0 && player.isPlaying) {
            startCrossfadeIn(player as ExoPlayer)
        } else {
            fadeVolumeJob?.cancel()
            player.volume = 1f
        }
    }

    private fun startCrossfadeIn(player: ExoPlayer) {
        fadeVolumeJob?.cancel()
        val totalMs = prefsMirror.crossfadeMs.coerceIn(1, 3000)
        fadeVolumeJob = scope.launch {
            player.volume = 0f
            val steps = 24
            val stepDelayMs = (totalMs.toLong() / steps).coerceAtLeast(1L)
            repeat(steps) { i ->
                if (!isActive) return@launch
                player.volume = (i + 1) / steps.toFloat()
                delay(stepDelayMs)
            }
            player.volume = 1f
        }
    }

    private fun persistPlaybackPrefsSnapshot(player: Player) {
        val idx = player.currentMediaItemIndex
        val track = when {
            idx in queue.indices -> queue[idx]
            else -> queue.firstOrNull { it.uri == player.currentMediaItem?.mediaId }
        }
        prefsMirror = MusicPlaybackPrefsEntity(
            id = 1,
            lastTrackUri = track?.uri,
            lastPositionMs = player.currentPosition.coerceAtLeast(0L),
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode,
            crossfadeMs = prefsMirror.crossfadeMs,
        )
        persistPlaybackPrefsAsync()
    }

    private fun maybePersistProgressWhilePlaying(player: Player) {
        if (!player.isPlaying) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastProgressPersistElapsed < 2000L) return
        lastProgressPersistElapsed = now
        persistPlaybackPrefsSnapshot(player)
    }

    private fun persistPlaybackPrefsAsync() {
        val snapshot = prefsMirror
        scope.launch(Dispatchers.IO) {
            runCatching { musicDao.upsertPlaybackPrefs(snapshot) }
        }
    }

    private fun ensureServiceRunning() {
        val intent = Intent(appContext, MusicPlaybackService::class.java)
        try {
            appContext.startService(intent)
        } catch (e: Exception) {
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
        player.shuffleModeEnabled = prefsMirror.shuffleEnabled
        player.repeatMode = prefsMirror.repeatMode
        player.addListener(listener)
        startProgressUpdates()
        return player
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (true) {
                playerInternal?.let { player ->
                    updateState(player)
                    maybePersistProgressWhilePlaying(player)
                } ?: updateState(null)
                delay(500L)
            }
        }
    }

    private fun updateState(player: Player?) {
        val p = player ?: playerInternal
        if (p == null) {
            _playerState.value = MusicPlayerUiState(
                shuffleEnabled = prefsMirror.shuffleEnabled,
                repeatMode = prefsMirror.repeatMode,
                crossfadeMs = prefsMirror.crossfadeMs,
            )
            return
        }

        val currentIndex = p.currentMediaItemIndex
        val currentTrack = when {
            currentIndex in queue.indices -> queue[currentIndex]
            else -> queue.firstOrNull { it.uri == p.currentMediaItem?.mediaId }
        }
        _playerState.value = MusicPlayerUiState(
            currentTrack = currentTrack,
            currentQueue = queue.toList(),
            currentIndex = currentIndex,
            isPlaying = p.isPlaying,
            progressMs = p.currentPosition.coerceAtLeast(0L),
            durationMs = if (p.duration > 0) p.duration else currentTrack?.durationMs ?: 0L,
            hasNext = p.hasNextMediaItem(),
            hasPrevious = p.hasPreviousMediaItem(),
            shuffleEnabled = p.shuffleModeEnabled,
            repeatMode = p.repeatMode,
            crossfadeMs = prefsMirror.crossfadeMs,
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
