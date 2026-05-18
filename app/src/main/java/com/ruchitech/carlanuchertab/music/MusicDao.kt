package com.ruchitech.carlanuchertab.music

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Query("SELECT * FROM music_settings WHERE id = 1")
    fun observeSettings(): Flow<MusicSettingsEntity?>

    @Query("SELECT * FROM music_settings WHERE id = 1")
    suspend fun getSettings(): MusicSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: MusicSettingsEntity)

    @Query("SELECT * FROM music_tracks WHERE isAvailable = 1 ORDER BY title COLLATE NOCASE ASC")
    fun observeTracks(): Flow<List<MusicTrackEntity>>

    @Query("SELECT * FROM music_tracks WHERE isAvailable = 1 ORDER BY title COLLATE NOCASE ASC")
    suspend fun getTracks(): List<MusicTrackEntity>

    @Query(
        """
        SELECT * FROM music_tracks
        WHERE album = :album AND artist = :artist AND isAvailable = 1
        ORDER BY discNumber ASC, trackNumber ASC, title COLLATE NOCASE ASC
        """
    )
    fun observeTracksForAlbum(album: String, artist: String): Flow<List<MusicTrackEntity>>

    @Query(
        """
        SELECT * FROM music_tracks
        WHERE album = :album AND artist = :artist AND isAvailable = 1
        ORDER BY discNumber ASC, trackNumber ASC, title COLLATE NOCASE ASC
        """
    )
    suspend fun getTracksForAlbum(album: String, artist: String): List<MusicTrackEntity>

    @Query(
        """
        SELECT * FROM music_tracks
        WHERE genre = :genre AND isAvailable = 1
        ORDER BY title COLLATE NOCASE ASC
        """
    )
    fun observeTracksForGenre(genre: String): Flow<List<MusicTrackEntity>>

    @Query(
        """
        SELECT * FROM music_tracks
        WHERE genre = :genre AND isAvailable = 1
        ORDER BY title COLLATE NOCASE ASC
        """
    )
    suspend fun getTracksForGenre(genre: String): List<MusicTrackEntity>

    @Query("SELECT * FROM music_tracks WHERE uri = :trackUri LIMIT 1")
    suspend fun getTrack(trackUri: String): MusicTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<MusicTrackEntity>)

    @Query("DELETE FROM music_tracks")
    suspend fun clearTracks()

    @Query("DELETE FROM music_tracks WHERE uri = :trackUri")
    suspend fun deleteTrackEntity(trackUri: String)

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT p.id, p.name, p.createdAt, p.updatedAt, COUNT(pt.trackUri) AS songCount
        FROM playlists p
        LEFT JOIN playlist_tracks pt ON p.id = pt.playlistId
        GROUP BY p.id
        ORDER BY p.name COLLATE NOCASE ASC
        """
    )
    fun observePlaylistsWithCount(): Flow<List<PlaylistWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylist(playlistId: Long): PlaylistEntity?

    @Query(
        """
        SELECT t.*, pt.position AS playlistPosition
        FROM playlist_tracks pt
        INNER JOIN music_tracks t ON t.uri = pt.trackUri
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.position ASC
        """
    )
    fun observePlaylistTracks(playlistId: Long): Flow<List<PlaylistTrackWithSong>>

    @Query(
        """
        SELECT t.*
        FROM playlist_tracks pt
        INNER JOIN music_tracks t ON t.uri = pt.trackUri
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.position ASC
        """
    )
    suspend fun getTracksForPlaylist(playlistId: Long): List<MusicTrackEntity>

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getPlaylistTrackRefs(playlistId: Long): List<PlaylistTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaylistTrack(playlistTrack: PlaylistTrackEntity)

    @Update
    suspend fun updatePlaylistTracks(playlistTracks: List<PlaylistTrackEntity>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackUri = :trackUri")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackUri: String)

    @Query("DELETE FROM playlist_tracks WHERE trackUri = :trackUri")
    suspend fun removeTrackFromAllPlaylists(trackUri: String)

    @Query("DELETE FROM playlist_tracks WHERE trackUri NOT IN (SELECT uri FROM music_tracks)")
    suspend fun deleteOrphanPlaylistTracks()

    @Query(
        "SELECT EXISTS(SELECT 1 FROM playlist_tracks WHERE playlistId = :playlistId AND trackUri = :trackUri)"
    )
    suspend fun hasPlaylistTrack(playlistId: Long, trackUri: String): Boolean

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getPlaylistMaxPosition(playlistId: Long): Int

    @Query(
        """
        SELECT t.* FROM music_tracks t
        INNER JOIN liked_tracks l ON l.trackUri = t.uri
        WHERE t.isAvailable = 1
        ORDER BY l.likedAt DESC
        """
    )
    fun observeLikedTracks(): Flow<List<MusicTrackEntity>>

    @Query(
        """
        SELECT t.* FROM music_tracks t
        INNER JOIN liked_tracks l ON l.trackUri = t.uri
        WHERE t.isAvailable = 1
        ORDER BY l.likedAt DESC
        """
    )
    suspend fun getLikedTracks(): List<MusicTrackEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_tracks WHERE trackUri = :trackUri)")
    suspend fun isTrackLiked(trackUri: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedTrack(likedTrack: LikedTrackEntity)

    @Query("DELETE FROM liked_tracks WHERE trackUri = :trackUri")
    suspend fun removeLikedTrack(trackUri: String)

    @Query("DELETE FROM liked_tracks WHERE trackUri NOT IN (SELECT uri FROM music_tracks)")
    suspend fun deleteOrphanLikedTracks()

    @Transaction
    suspend fun replaceLibrary(tracks: List<MusicTrackEntity>) {
        clearTracks()
        if (tracks.isNotEmpty()) {
            insertTracks(tracks)
        }
        deleteOrphanPlaylistTracks()
        deleteOrphanLikedTracks()
    }

    @Transaction
    suspend fun addTrackToPlaylist(playlistId: Long, trackUri: String) {
        if (hasPlaylistTrack(playlistId, trackUri)) return
        val nextPosition = getPlaylistMaxPosition(playlistId) + 1
        upsertPlaylistTrack(
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackUri = trackUri,
                position = nextPosition
            )
        )
    }

    @Transaction
    suspend fun movePlaylistTrack(
        playlistId: Long,
        trackUri: String,
        direction: Int,
    ) {
        val items = getPlaylistTrackRefs(playlistId).toMutableList()
        val currentIndex = items.indexOfFirst { it.trackUri == trackUri }
        if (currentIndex == -1) return
        val targetIndex = currentIndex + direction
        if (targetIndex !in items.indices) return

        val current = items[currentIndex]
        val target = items[targetIndex]
        items[currentIndex] = current.copy(position = target.position)
        items[targetIndex] = target.copy(position = current.position)
        updatePlaylistTracks(listOf(items[currentIndex], items[targetIndex]))
    }

    @Transaction
    suspend fun deleteTrackFromLibrary(trackUri: String) {
        removeTrackFromAllPlaylists(trackUri)
        removeLikedTrack(trackUri)
        deleteTrackEntity(trackUri)
    }
}
