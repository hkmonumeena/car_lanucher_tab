package com.ruchitech.carlanuchertab.music

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.isNullOrBlank
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import androidx.core.graphics.createBitmap

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDao: MusicDao,
    private val playerManager: MusicPlayerManager,
) {
    private companion object {
        const val DEVICE_LIBRARY_URI = "music-library://device"
        const val DEVICE_LIBRARY_NAME = "Device Library"
    }

    val settingsFlow: Flow<MusicSettingsEntity> =
        musicDao.observeSettings().map { it ?: MusicSettingsEntity() }

    val tracksFlow: Flow<List<MusicTrackEntity>> = musicDao.observeTracks()

    val albumsFlow: Flow<List<AlbumSummary>> =
        tracksFlow.map { tracks -> tracks.toAlbumSummaries() }

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

    val recentlyAddedTracksFlow: Flow<List<MusicTrackEntity>> =
        musicDao.observeRecentlyAddedTracks()

    val recentlyPlayedTracksFlow: Flow<List<MusicTrackEntity>> =
        musicDao.observeRecentlyPlayedTracks()

    val mostPlayedTracksFlow: Flow<List<MusicTrackEntity>> =
        musicDao.observeMostPlayedTracks()

    fun observeAlbumTracks(album: String): Flow<List<MusicTrackEntity>> {
        return musicDao.observeTracksForAlbum(album)
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

    suspend fun setDeviceLibrary() = withContext(Dispatchers.IO) {
        if (!hasAudioReadPermission()) {
            throw SecurityException("Allow audio access to scan the device music library.")
        }

        musicDao.upsertSettings(
            getSettings().copy(
                folderUri = DEVICE_LIBRARY_URI,
                folderName = DEVICE_LIBRARY_NAME,
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

        if (folderUri == DEVICE_LIBRARY_URI) {
            val tracks = try {
                scanDeviceLibrary()
            } catch (error: SecurityException) {
                musicDao.upsertSettings(
                    settings.copy(
                        scanStatus = MusicScanStatus.ERROR,
                        errorMessage = error.message
                            ?: "Allow audio access to scan the device music library."
                    )
                )
                return@withContext
            }

            musicDao.replaceLibrary(tracks)
            musicDao.upsertSettings(
                settings.copy(
                    folderUri = folderUri,
                    folderName = DEVICE_LIBRARY_NAME,
                    lastScanTimestamp = System.currentTimeMillis(),
                    scanStatus = MusicScanStatus.READY,
                    errorMessage = if (tracks.isEmpty()) {
                        "No songs were found in the device music library."
                    } else {
                        null
                    }
                )
            )
            return@withContext
        }

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

    suspend fun getAlbumTracks(album: String): List<MusicTrackEntity> =
        withContext(Dispatchers.IO) {
            musicDao.getTracksForAlbum(album)
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

    suspend fun updateTrackMetadata(trackUri: String, metadata: TrackMetadataUpdate): Result<Unit> =
        withContext(Dispatchers.IO) {
            val track = musicDao.getTrack(trackUri)
                ?: return@withContext Result.failure(
                    IllegalStateException("Track no longer exists.")
                )

            val normalizedTitle = metadata.title.trim().ifBlank { track.displayName }
            val normalizedArtist = metadata.artist.trim().ifBlank { "Unknown Artist" }
            val normalizedAlbum = metadata.album.trim().ifBlank { "Unknown Album" }
            val normalizedGenre = metadata.genre.trim().ifBlank { "Unknown Genre" }
            val normalizedYear = metadata.year.coerceIn(0, 9999)
            val targetArtworkPath = metadata.artworkPath ?: track.artworkPath

            writeEmbeddedTags(
                trackUri = trackUri,
                sourceExtension = resolveAudioExtension(track),
                title = normalizedTitle,
                artist = normalizedArtist,
                album = normalizedAlbum,
                genre = normalizedGenre,
                year = normalizedYear,
                artworkPath = targetArtworkPath
            ).getOrElse { error ->
                return@withContext Result.failure(
                    IllegalStateException(
                        "Unable to write embedded audio tags. ${error.message ?: "Check file permissions and format support."}"
                    )
                )
            }

            val updatedTrack = track.copy(
                title = normalizedTitle,
                artist = normalizedArtist,
                album = normalizedAlbum,
                genre = normalizedGenre,
                year = normalizedYear,
                artworkPath = targetArtworkPath
            )
            musicDao.updateTrack(updatedTrack)
            if (!metadata.artworkPath.isNullOrBlank() && metadata.artworkPath != track.artworkPath) {
                track.artworkPath?.let { previousPath ->
                    File(previousPath).takeIf { it.exists() }?.delete()
                }
            }

            if (trackUri.startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                runCatching {
                    context.contentResolver.update(
                        Uri.parse(trackUri),
                        ContentValues().apply {
                            put(MediaStore.Audio.Media.TITLE, normalizedTitle)
                            put(MediaStore.Audio.Media.ARTIST, normalizedArtist)
                            put(MediaStore.Audio.Media.ALBUM, normalizedAlbum)
                            put(MediaStore.Audio.Media.YEAR, normalizedYear)
                        },
                        null,
                        null
                    )
                    Unit
                }
            }

            Result.success(Unit)
        }

    private fun writeEmbeddedTags(
        trackUri: String,
        sourceExtension: String,
        title: String,
        artist: String,
        album: String,
        genre: String,
        year: Int,
        artworkPath: String?,
    ): Result<Unit> {
        val sourceUri = Uri.parse(trackUri)
        val tempDir = File(context.cacheDir, "music_tag_edit").apply { mkdirs() }
        val safeExt = sourceExtension.lowercase().ifBlank { "mp3" }
        val sourceFile = File(tempDir, "${UUID.randomUUID()}_source.$safeExt")
        val outputFile = File(tempDir, "${UUID.randomUUID()}_output.$safeExt")

        return runCatching {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(sourceFile).use { output -> input.copyTo(output) }
            } ?: throw IllegalStateException("Unable to read source audio file.")

            sourceFile.copyTo(outputFile, overwrite = true)
            val audioFile = AudioFileIO.read(outputFile)
            val tag = audioFile.tagOrCreateAndSetDefault
            tag.setField(FieldKey.TITLE, title)
            tag.setField(FieldKey.ARTIST, artist)
            tag.setField(FieldKey.ALBUM, album)
            tag.setField(FieldKey.GENRE, genre)
            if (year > 0) {
                tag.setField(FieldKey.YEAR, year.toString())
            } else {
                tag.deleteField(FieldKey.YEAR)
            }
            tag.deleteArtworkField()
            if (!artworkPath.isNullOrBlank()) {
                val artFile = File(artworkPath)
                if (artFile.exists()) {
                    tag.setField(ArtworkFactory.createArtworkFromFile(artFile))
                }
            }
            AudioFileIO.write(audioFile)

            val writeMode = "rwt"
            val outputStream = context.contentResolver.openOutputStream(sourceUri, writeMode)
                ?: context.contentResolver.openOutputStream(sourceUri, "wt")
                ?: throw IllegalStateException("Unable to open destination for writing.")
            outputStream.use { output ->
                outputFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            Unit
        }.also {
            sourceFile.delete()
            outputFile.delete()
        }
    }

    private fun resolveAudioExtension(track: MusicTrackEntity): String {
        val fromDisplay = track.displayName
            .substringAfterLast('.', "")
            .trim()
            .lowercase()
        if (fromDisplay.isNotBlank()) return fromDisplay

        val fromMime = track.mimeType
            ?.substringAfter('/', "")
            ?.substringBefore('+')
            ?.trim()
            ?.lowercase()
            .orEmpty()
        val normalizedFromMime = when (fromMime) {
            "mpeg", "mpg", "mpga" -> "mp3"
            "x-flac" -> "flac"
            "x-wav" -> "wav"
            "x-m4a" -> "m4a"
            else -> fromMime
        }
        return normalizedFromMime.ifBlank { "mp3" }
    }

    private suspend fun touchPlaylist(playlistId: Long) {
        val playlist = musicDao.getPlaylist(playlistId) ?: return
        musicDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
    }

    private fun scanDeviceLibrary(): List<MusicTrackEntity> {
        if (!hasAudioReadPermission()) {
            throw SecurityException("Allow audio access to scan the device music library.")
        }

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )
        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%')"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        return context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val modifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            buildList {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val displayName = cursor.getString(displayNameIndex)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Track $id"
                    val fallbackTitle = displayName.substringBeforeLast('.').ifBlank { displayName }

                    add(
                        MusicTrackEntity(
                            uri = uri.toString(),
                            displayName = displayName,
                            title = cursor.getString(titleIndex).normalizedOr(fallbackTitle),
                            artist = cursor.getString(artistIndex).normalizedOr("Unknown Artist"),
                            album = cursor.getString(albumIndex).normalizedOr("Unknown Album"),
                            genre = "Unknown Genre",
                            year = cursor.getInt(yearIndex).coerceAtLeast(0),
                            durationMs = cursor.getLong(durationIndex),
                            trackNumber = cursor.getInt(trackIndex).coerceAtLeast(0),
                            discNumber = 0,
                            artworkPath = cacheArtwork(uri.toString(), extractEmbeddedArtwork(uri)),
                            mimeType = cursor.getString(mimeTypeIndex),
                            fileSizeBytes = cursor.getLong(sizeIndex),
                            lastModified = cursor.getLong(modifiedIndex) * 1_000L,
                            isAvailable = true
                        )
                    )
                }
            }
        }.orEmpty()
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
            val rawYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)

            val embeddedPicture = retriever.embeddedPicture
            MusicTrackEntity(
                uri = file.uri.toString(),
                displayName = file.name?.substringBeforeLast('.') ?: "Unknown Track",
                title = rawTitle.normalizedOr(file.name?.substringBeforeLast('.') ?: "Unknown Track"),
                artist = rawArtist.normalizedOr("Unknown Artist"),
                album = rawAlbum.normalizedOr("Unknown Album"),
                genre = rawGenre.normalizedOr("Unknown Genre"),
                year = parseNumericMetadata(rawYear).coerceAtLeast(0),
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

    private fun extractEmbeddedArtwork(uri: Uri): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture
        } catch (_: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun cacheArtwork(trackUri: String, imageBytes: ByteArray?): String? {
        if (imageBytes == null || imageBytes.isEmpty()) return null
        val scaledImageBytes = scaleArtworkBytes(imageBytes) ?: return null
        val artworkDirectory = File(context.cacheDir, "music_artwork").apply { mkdirs() }
        val artworkFile = File(artworkDirectory, "${trackUri.hashCode().toUInt().toString(16)}.jpg")
        FileOutputStream(artworkFile).use { stream ->
            stream.write(scaledImageBytes)
        }
        return artworkFile.absolutePath
    }

    fun cacheCustomArtwork(trackUri: String, sourceUri: Uri): String? {
        return runCatching {
            val bytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() } ?: return null
            cacheArtwork(trackUri, bytes)
        }.getOrNull()
    }

    private fun scaleArtworkBytes(
        bytes: ByteArray,
        targetWidth: Int = 600,
        targetHeight: Int = 600,
    ): ByteArray? {
        val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return null

        val output = createBitmap(targetWidth, targetHeight)
        val canvas = Canvas(output)

        canvas.drawBitmap(
            source,
            Rect(0, 0, width, height),
            Rect(0, 0, targetWidth, targetHeight),
            null
        )

        return try {
            java.io.ByteArrayOutputStream().use { stream ->
                output.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.toByteArray()
            }
        } finally {
            source.recycle()
            output.recycle()
        }
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

    private fun hasAudioReadPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun String?.normalizedOr(fallback: String): String {
        val normalized = this?.trim().orEmpty()
        return if (normalized.isBlank() || normalized.equals("<unknown>", ignoreCase = true)) {
            fallback
        } else {
            normalized
        }
    }

    private fun String?.isSupportedAudioFile(mimeType: String?): Boolean {
        if (!mimeType.isNullOrBlank() && mimeType.startsWith("audio/")) return true
        val normalized = this?.substringAfterLast('.', "")?.lowercase().orEmpty()
        return normalized in setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "opus", "amr")
    }
}

private fun albumGroupingKey(album: String): String = album.trim().lowercase()

private fun List<MusicTrackEntity>.toAlbumSummaries(): List<AlbumSummary> {
    return groupBy { albumGroupingKey(it.album) }
        .map { (_, groupedTracks) ->
            val artists = groupedTracks
                .map { it.artist.trim() }
                .distinctBy { it.lowercase() }
            val displayAlbum = groupedTracks
                .groupBy { it.album.trim() }
                .maxByOrNull { it.value.size }
                ?.key
                ?: groupedTracks.first().album.trim()
            AlbumSummary(
                album = displayAlbum,
                artist = when {
                    artists.size == 1 -> artists.first()
                    else -> "Various Artists"
                },
                songCount = groupedTracks.size,
                artworkPath = groupedTracks.firstNotNullOfOrNull { it.artworkPath },
            )
        }
        .sortedBy { it.album.lowercase() }
}
