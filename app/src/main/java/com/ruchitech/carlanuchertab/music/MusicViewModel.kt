package com.ruchitech.carlanuchertab.music

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerManager: MusicPlayerManager,
) : ViewModel() {

    val settings = repository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MusicSettingsEntity()
    )

    val tracks = repository.tracksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val albums = repository.albumsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val genres = repository.genresFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val playlists = repository.playlistsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val likedTracks = repository.likedTracksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val recentlyAddedTracks = repository.recentlyAddedTracksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val recentlyPlayedTracks = repository.recentlyPlayedTracksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val mostPlayedTracks = repository.mostPlayedTracksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val playerState: StateFlow<MusicPlayerUiState> = playerManager.playerState

    private var libraryResumeAttempted: Boolean = false

    init {
        viewModelScope.launch {
            combine(
                repository.tracksFlow,
                repository.settingsFlow,
            ) { tracks, settings ->
                Triple(
                    tracks,
                    settings.scanStatus,
                    !settings.folderUri.isNullOrBlank(),
                )
            }.collect { (tracks, scanStatus, folderConfigured) ->
                if (libraryResumeAttempted) return@collect
                if (!folderConfigured || scanStatus != MusicScanStatus.READY) return@collect
                if (tracks.isEmpty()) return@collect
                libraryResumeAttempted = true
                playerManager.tryResumeFromLibrary(tracks)
            }
        }
    }

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    private val playlistTrackFlows = mutableMapOf<Long, StateFlow<List<PlaylistTrackWithSong>>>()
    private val albumTrackFlows = mutableMapOf<String, StateFlow<List<MusicTrackEntity>>>()
    private val genreTrackFlows = mutableMapOf<String, StateFlow<List<MusicTrackEntity>>>()

    fun playlistTracks(playlistId: Long): StateFlow<List<PlaylistTrackWithSong>> {
        return playlistTrackFlows.getOrPut(playlistId) {
            repository.observePlaylistTracks(playlistId).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )
        }
    }

    fun albumTracks(album: String): StateFlow<List<MusicTrackEntity>> {
        val key = album.trim().lowercase()
        return albumTrackFlows.getOrPut(key) {
            repository.observeAlbumTracks(album).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )
        }
    }

    fun genreTracks(genre: String): StateFlow<List<MusicTrackEntity>> {
        return genreTrackFlows.getOrPut(genre) {
            repository.observeGenreTracks(genre).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )
        }
    }

    fun onFolderSelected(uri: Uri, permissionFlags: Int) {
        viewModelScope.launch {
            runCatching {
                repository.setMusicFolder(uri, permissionFlags)
            }.onFailure {
                _messages.emit(it.message ?: "Unable to open the selected folder.")
            }
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            runCatching {
                repository.rescanLibrary()
            }.onFailure {
                _messages.emit(it.message ?: "Music scan failed.")
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                _messages.emit("Playlist name cannot be empty.")
                return@launch
            }
            repository.createPlaylist(trimmed)
        }
    }

    fun renamePlaylist(playlistId: Long, name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                _messages.emit("Playlist name cannot be empty.")
                return@launch
            }
            repository.renamePlaylist(playlistId, trimmed)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackUri: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackUri)
            _messages.emit("Added to playlist.")
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackUri: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackUri)
        }
    }

    fun movePlaylistTrackUp(playlistId: Long, trackUri: String) {
        viewModelScope.launch {
            repository.movePlaylistTrack(playlistId, trackUri, -1)
        }
    }

    fun movePlaylistTrackDown(playlistId: Long, trackUri: String) {
        viewModelScope.launch {
            repository.movePlaylistTrack(playlistId, trackUri, 1)
        }
    }

    fun playAllSongs(startTrackUri: String) {
        viewModelScope.launch {
            val allTracks = repository.getAllTracks()
            playTrackFromList(allTracks, startTrackUri)
        }
    }

    fun playAlbum(album: String, startTrackUri: String) {
        viewModelScope.launch {
            val albumTracks = repository.getAlbumTracks(album)
            playTrackFromList(albumTracks, startTrackUri)
        }
    }

    fun playGenre(genre: String, startTrackUri: String) {
        viewModelScope.launch {
            val genreTracks = repository.getGenreTracks(genre)
            playTrackFromList(genreTracks, startTrackUri)
        }
    }

    fun playPlaylist(playlistId: Long, startTrackUri: String) {
        viewModelScope.launch {
            val playlistTracks = repository.getPlaylistTracks(playlistId)
            playTrackFromList(playlistTracks, startTrackUri)
        }
    }

    fun playLikedSongs(startTrackUri: String) {
        viewModelScope.launch {
            val liked = repository.getLikedTracks()
            playTrackFromList(liked, startTrackUri)
        }
    }

    fun playRecentlyAdded(startTrackUri: String) {
        viewModelScope.launch {
            playTrackFromList(recentlyAddedTracks.value, startTrackUri)
        }
    }

    fun playRecentlyPlayed(startTrackUri: String) {
        viewModelScope.launch {
            playTrackFromList(recentlyPlayedTracks.value, startTrackUri)
        }
    }

    fun playMostPlayed(startTrackUri: String) {
        viewModelScope.launch {
            playTrackFromList(mostPlayedTracks.value, startTrackUri)
        }
    }

    fun toggleLike(trackUri: String) {
        viewModelScope.launch {
            val liked = repository.isTrackLiked(trackUri)
            repository.setTrackLiked(trackUri, !liked)
            _messages.emit(if (liked) "Removed from liked songs." else "Added to liked songs.")
        }
    }

    fun toggleLikeCurrentTrack() {
        val trackUri = playerState.value.currentTrack?.uri ?: return
        toggleLike(trackUri)
    }

    fun togglePlayback() {
        playerManager.togglePlayback()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun skipNext() {
        playerManager.skipNext()
    }

    fun skipPrevious() {
        playerManager.skipPrevious()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerManager.cycleRepeatMode()
    }

    fun setCrossfadeEnabled(enabled: Boolean) {
        playerManager.setCrossfadeMs(if (enabled) 400 else 0)
    }

    fun moveQueueItem(from: Int, to: Int) {
        playerManager.moveQueueItem(from, to)
    }

    fun removeQueueItem(index: Int) {
        playerManager.removeQueueItem(index)
    }

    fun clearQueue() {
        playerManager.clearQueue()
    }

    fun playQueueIndex(index: Int) {
        playerManager.playQueueIndex(index)
    }

    fun deleteTrack(trackUri: String) {
        viewModelScope.launch {
            val result = repository.deleteTrack(trackUri)
            result.onFailure {
                _messages.emit(it.message ?: "Unable to delete the selected song.")
            }
        }
    }

    fun deleteCurrentTrack() {
        val trackUri = playerState.value.currentTrack?.uri ?: return
        deleteTrack(trackUri)
    }

    private suspend fun playTrackFromList(
        tracks: List<MusicTrackEntity>,
        startTrackUri: String,
    ) {
        if (tracks.isEmpty()) {
            _messages.emit("No songs available in this section.")
            return
        }
        val startIndex = tracks.indexOfFirst { it.uri == startTrackUri }.coerceAtLeast(0)
        playerManager.playTracks(tracks, startIndex)
    }
}
