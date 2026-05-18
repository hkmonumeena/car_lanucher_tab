package com.ruchitech.carlanuchertab.music

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.isNullOrBlank
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDao: MusicDao,
    private val playerManager: MusicPlayerManager,
) {
    val settingsFlow: Flow<MusicSettingsEntity> =
        musicDao.observeSettings().map { it ?: MusicSettingsEntity() }

    val tracksFlow: Flow<List<MusicTrackEntity>> = musicDao.observeTracks()

    val albumsFlow: Flow<List<AlbumSummary>> =
        tracksFlow.map { tracks ->
            tracks.groupBy { it.album to it.artist }
                .map { (key, groupedTracks) ->
                    AlbumSummary(
                        album = key.first,
                        artist = key.second,
                        songCount = groupedTracks.size,
                        artworkPath = groupedTracks.firstNotNullOfOrNull { it.artworkPath }
                    )
                }
                .sortedBy { it.album.lowercase() }
        }

    val genresFlow: Flow<List<GenreSummary>> =
        tracksFlow.map { tracks ->
            tracks.groupBy { it.genre }
                .map { (genre, groupedTracks) ->
                    GenreSummary(
                        genre = genre,
                        songCount = groupedTracks.size,
                        artworkPath = groupedTracks.firstNotNullOfOrNull { it.artworkPath }
                    )
                }
                .sortedBy { it.genre.lowercase() }
        }

    val playlistsFlow: Flow<List<PlaylistWithCount>> = musicDao.observePlaylistsWithCount()

    val likedTracksFlow: Flow<List<MusicTrackEntity>> = musicDao.observeLikedTracks()

    fun observeAlbumTracks(album: String, artist: String): Flow<List<MusicTrackEntity>> {
        return musicDao.observeTracksForAlbum(album, artist)
    }

    fun observeGenreTracks(genre: String): Flow<List<MusicTrackEntity>> {
        return musicDao.observeTracksForGenre(genre)
    }

    fun observePlaylistTracks(playlistId: Long): Flow<List<PlaylistTrackWithSong>> {
        return musicDao.observePlaylistTracks(playlistId)
    }

    suspend fun getSettings(): MusicSettingsEntity = musicDao.getSettings() ?: MusicSettingsEntity()

    suspend fun setMusicFolder(uri: Uri, permissionFlags: Int) = withContext(Dispatchers.IO) {
        val readWriteFlags = permissionFlags and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        if (readWriteFlags != 0) {
            context.contentResolver.takePersistableUriPermission(uri, readWriteFlags)
        }

        val root = DocumentFile.fromTreeUri(context, uri)
        musicDao.upsertSettings(
            getSettings().copy(
                folderUri = uri.toString(),
                folderName = root?.name ?: "Music Folder",
                scanStatus = MusicScanStatus.IDLE,
                errorMessage = null
            )
        )
        rescanLibrary()
    }

    suspend fun rescanLibrary() = withContext(Dispatchers.IO) {
        val settings = getSettings()
        val folderUri = settings.folderUri
        if (folderUri.isNullOrBlank()) {
            musicDao.upsertSettings(
                settings.copy(
                    scanStatus = MusicScanStatus.ERROR,
                    errorMessage = "Choose a music folder to scan."
                )
            )
            return@withContext
        }

        musicDao.upsertSettings(
            settings.copy(
                scanStatus = MusicScanStatus.SCANNING,
                errorMessage = null
            )
        )

        val root = DocumentFile.fromTreeUri(context, Uri.parse(folderUri))
        if (root == null || !root.exists()) {
            musicDao.upsertSettings(
                settings.copy(
                    scanStatus = MusicScanStatus.ERROR,
                    errorMessage = "The selected music folder is no longer available."
                )
            )
            return@withContext
        }

        val tracks = mutableListOf<MusicTrackEntity>()
        scanFolder(root, tracks)
        musicDao.replaceLibrary(tracks)
        musicDao.upsertSettings(
            settings.copy(
                folderUri = folderUri,
                folderName = root.name ?: settings.folderName,
                lastScanTimestamp = System.currentTimeMillis(),
                scanStatus = MusicScanStatus.READY,
                errorMessage = if (tracks.isEmpty()) {
                    "No supported audio files were found in the selected folder."
                } else {
                    null
                }
            )
        )
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        musicDao.insertPlaylist(
            PlaylistEntity(
                name = name.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun renamePlaylist(playlistId: Long, name: String) = withContext(Dispatchers.IO) {
        val playlist = musicDao.getPlaylist(playlistId) ?: return@withContext
        musicDao.updatePlaylist(
            playlist.copy(
                name = name.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        val playlist = musicDao.getPlaylist(playlistId) ?: return@withContext
        musicDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackUri: String) = withContext(Dispatchers.IO) {
        musicDao.addTrackToPlaylist(playlistId, trackUri)
        touchPlaylist(playlistId)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackUri: String) = withContext(Dispatchers.IO) {
        musicDao.removeTrackFromPlaylist(playlistId, trackUri)
        touchPlaylist(playlistId)
    }

    suspend fun movePlaylistTrack(
        playlistId: Long,
        trackUri: String,
        direction: Int,
    ) = withContext(Dispatchers.IO) {
        musicDao.movePlaylistTrack(playlistId, trackUri, direction)
        touchPlaylist(playlistId)
    }

    suspend fun getAllTracks(): List<MusicTrackEntity> = withContext(Dispatchers.IO) {
        musicDao.getTracks()
    }

    suspend fun getAlbumTracks(album: String, artist: String): List<MusicTrackEntity> =
        withContext(Dispatchers.IO) {
            musicDao.getTracksForAlbum(album, artist)
        }

    suspend fun getGenreTracks(genre: String): List<MusicTrackEntity> =
        withContext(Dispatchers.IO) {
            musicDao.getTracksForGenre(genre)
        }

    suspend fun getPlaylistTracks(playlistId: Long): List<MusicTrackEntity> =
        withContext(Dispatchers.IO) {
            musicDao.getTracksForPlaylist(playlistId)
        }

    suspend fun getLikedTracks(): List<MusicTrackEntity> = withContext(Dispatchers.IO) {
        musicDao.getLikedTracks()
    }

    suspend fun isTrackLiked(trackUri: String): Boolean = withContext(Dispatchers.IO) {
        musicDao.isTrackLiked(trackUri)
    }

    suspend fun setTrackLiked(trackUri: String, liked: Boolean) = withContext(Dispatchers.IO) {
        if (liked) {
            musicDao.insertLikedTrack(LikedTrackEntity(trackUri = trackUri))
        } else {
            musicDao.removeLikedTrack(trackUri)
        }
    }

    suspend fun deleteTrack(trackUri: String): Result<Unit> = withContext(Dispatchers.IO) {
        val track = musicDao.getTrack(trackUri)
            ?: return@withContext Result.failure(IllegalStateException("Track no longer exists."))

        val document = DocumentFile.fromSingleUri(context, Uri.parse(trackUri))
            ?: return@withContext Result.failure(IllegalStateException("Unable to access song file."))

        val deleted = document.delete()
        if (!deleted) {
            return@withContext Result.failure(
                IllegalStateException("The song could not be deleted from storage.")
            )
        }

        track.artworkPath?.let { artworkPath ->
            File(artworkPath).delete()
        }
        musicDao.deleteTrackFromLibrary(trackUri)
        playerManager.onTrackDeleted(trackUri)
        Result.success(Unit)
    }

    private suspend fun touchPlaylist(playlistId: Long) {
        val playlist = musicDao.getPlaylist(playlistId) ?: return
        musicDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
    }

    private fun scanFolder(folder: DocumentFile, tracks: MutableList<MusicTrackEntity>) {
        folder.listFiles().forEach { child ->
            when {
                child.isDirectory -> scanFolder(child, tracks)
                child.isFile && child.name.isSupportedAudioFile(child.type) -> {
                    buildTrack(child)?.let(tracks::add)
                }
            }
        }
    }

    private fun buildTrack(file: DocumentFile): MusicTrackEntity? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, file.uri)
            val rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val rawGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val rawDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val rawTrack = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)

            val embeddedPicture = retriever.embeddedPicture
            MusicTrackEntity(
                uri = file.uri.toString(),
                displayName = file.name?.substringBeforeLast('.') ?: "Unknown Track",
                title = rawTitle.normalizedOr(file.name?.substringBeforeLast('.') ?: "Unknown Track"),
                artist = rawArtist.normalizedOr("Unknown Artist"),
                album = rawAlbum.normalizedOr("Unknown Album"),
                genre = rawGenre.normalizedOr("Unknown Genre"),
                durationMs = rawDuration?.toLongOrNull() ?: 0L,
                trackNumber = parseNumericMetadata(rawTrack),
                discNumber = readDiscNumber(retriever),
                artworkPath = cacheArtwork(file.uri.toString(), embeddedPicture),
                mimeType = file.type,
                fileSizeBytes = file.length(),
                lastModified = file.lastModified(),
                isAvailable = true
            )
        } catch (_: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun cacheArtwork(trackUri: String, imageBytes: ByteArray?): String? {
        if (imageBytes == null || imageBytes.isEmpty()) return null
        val artworkDirectory = File(context.cacheDir, "music_artwork").apply { mkdirs() }
        val artworkFile = File(artworkDirectory, "${trackUri.hashCode().toUInt().toString(16)}.jpg")
        FileOutputStream(artworkFile).use { stream ->
            stream.write(imageBytes)
        }
        return artworkFile.absolutePath
    }

    private fun parseNumericMetadata(value: String?): Int {
        if (value.isNullOrBlank()) return 0
        return value.substringBefore('/').trim().toIntOrNull() ?: 0
    }

    private fun readDiscNumber(retriever: MediaMetadataRetriever): Int {
        return try {
            val field = MediaMetadataRetriever::class.java.getField("METADATA_KEY_DISC_NUMBER")
            val key = field.getInt(null)
            parseNumericMetadata(retriever.extractMetadata(key))
        } catch (_: Exception) {
            0
        }
    }

    private fun String?.normalizedOr(fallback: String): String {
        return if (this.isNullOrBlank()) fallback else this.trim()
    }

    private fun String?.isSupportedAudioFile(mimeType: String?): Boolean {
        if (!mimeType.isNullOrBlank() && mimeType.startsWith("audio/")) return true
        val normalized = this?.substringAfterLast('.', "")?.lowercase().orEmpty()
        return normalized in setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "opus", "amr")
    }
}
