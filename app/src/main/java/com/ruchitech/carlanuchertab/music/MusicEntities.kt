package com.ruchitech.carlanuchertab.music

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "music_settings")
data class MusicSettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val folderUri: String? = null,
    val folderName: String? = null,
    val lastScanTimestamp: Long = 0L,
    val scanStatus: String = MusicScanStatus.IDLE,
    val errorMessage: String? = null,
)

object MusicScanStatus {
    const val IDLE = "IDLE"
    const val SCANNING = "SCANNING"
    const val READY = "READY"
    const val ERROR = "ERROR"
}

@Entity(
    tableName = "music_tracks",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["genre"]),
    ]
)
data class MusicTrackEntity(
    @PrimaryKey
    val uri: String,
    val displayName: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val artworkPath: String? = null,
    val mimeType: String? = null,
    val fileSizeBytes: Long = 0L,
    val lastModified: Long = 0L,
    val isAvailable: Boolean = true,
)

@Entity(
    tableName = "playlists",
    indices = [Index(value = ["name"])]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackUri"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MusicTrackEntity::class,
            parentColumns = ["uri"],
            childColumns = ["trackUri"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["trackUri"]),
    ]
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackUri: String,
    val position: Int,
)

data class AlbumSummary(
    val album: String,
    val artist: String,
    val songCount: Int,
    val artworkPath: String? = null,
)

data class GenreSummary(
    val genre: String,
    val songCount: Int,
    val artworkPath: String? = null,
)

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val songCount: Int,
)

data class PlaylistTrackWithSong(
    val uri: String,
    val displayName: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val artworkPath: String?,
    val mimeType: String?,
    val fileSizeBytes: Long,
    val lastModified: Long,
    val isAvailable: Boolean,
    val playlistPosition: Int,
)

data class MusicPlayerUiState(
    val currentTrack: MusicTrackEntity? = null,
    val currentQueue: List<MusicTrackEntity> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
)
