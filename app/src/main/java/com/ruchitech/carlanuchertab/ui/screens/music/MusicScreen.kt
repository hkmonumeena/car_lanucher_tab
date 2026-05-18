package com.ruchitech.carlanuchertab.ui.screens.music

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.music.AlbumSummary
import com.ruchitech.carlanuchertab.music.GenreSummary
import com.ruchitech.carlanuchertab.music.MusicSettingsEntity
import com.ruchitech.carlanuchertab.music.MusicTrackEntity
import com.ruchitech.carlanuchertab.music.MusicViewModel
import com.ruchitech.carlanuchertab.music.PlaylistTrackWithSong
import com.ruchitech.carlanuchertab.music.PlaylistWithCount
import com.ruchitech.carlanuchertab.ui.composables.MusicPlayerPanel
import java.io.File

private enum class MusicTab {
    Playlists,
    Albums,
    Songs,
    Genres,
}

@Composable
fun MusicScreen(
    onBack: () -> Unit,
    viewModel: MusicViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(MusicTab.Songs) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedAlbum by remember { mutableStateOf<AlbumSummary?>(null) }
    var selectedGenre by remember { mutableStateOf<GenreSummary?>(null) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistWithCount?>(null) }
    var playlistNameInput by rememberSaveable { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistWithCount?>(null) }
    var trackPendingDelete by remember { mutableStateOf<MusicTrackEntity?>(null) }
    var addToPlaylistTrack by remember { mutableStateOf<MusicTrackEntity?>(null) }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = data.data ?: return@rememberLauncherForActivityResult
        viewModel.onFolderSelected(uri, data.flags)
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF061018), Color(0xFF102033), Color(0xFF162A3B))
                    )
                )
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                        .padding(18.dp)
                ) {
                    MusicHeader(
                        settings = settings,
                        onBack = onBack,
                        onChooseFolder = {
                            folderLauncher.launch(
                                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                                }
                            )
                        },
                        onRescan = viewModel::rescanLibrary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    MusicTabs(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedAlbum = null
                            selectedGenre = null
                            selectedPlaylist = null
                            selectedTab = it
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        settings.folderUri.isNullOrBlank() -> {
                            MusicEmptyBrowser(
                                title = "Choose a folder to build your library",
                                subtitle = "The player will scan this folder recursively and keep it available across Home and Music screens.",
                                actionLabel = "Choose Folder",
                                onAction = {
                                    folderLauncher.launch(
                                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                            addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                                        }
                                    )
                                }
                            )
                        }

                        selectedAlbum != null -> {
                            AlbumDetailView(
                                summary = selectedAlbum!!,
                                tracks = viewModel.albumTracks(
                                    selectedAlbum!!.album,
                                    selectedAlbum!!.artist
                                ).collectAsState().value.filterTrackSearch(searchQuery),
                                onBack = { selectedAlbum = null },
                                onPlayTrack = { track ->
                                    viewModel.playAlbum(selectedAlbum!!.album, selectedAlbum!!.artist, track.uri)
                                },
                                onPlayAll = {
                                    val first = viewModel.albumTracks(
                                        selectedAlbum!!.album,
                                        selectedAlbum!!.artist
                                    ).value.firstOrNull() ?: return@AlbumDetailView
                                    viewModel.playAlbum(selectedAlbum!!.album, selectedAlbum!!.artist, first.uri)
                                },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onDelete = { trackPendingDelete = it }
                            )
                        }

                        selectedGenre != null -> {
                            GenreDetailView(
                                summary = selectedGenre!!,
                                tracks = viewModel.genreTracks(selectedGenre!!.genre).collectAsState().value.filterTrackSearch(searchQuery),
                                onBack = { selectedGenre = null },
                                onPlayTrack = { track ->
                                    viewModel.playGenre(selectedGenre!!.genre, track.uri)
                                },
                                onPlayAll = {
                                    val first = viewModel.genreTracks(selectedGenre!!.genre).value.firstOrNull()
                                        ?: return@GenreDetailView
                                    viewModel.playGenre(selectedGenre!!.genre, first.uri)
                                },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onDelete = { trackPendingDelete = it }
                            )
                        }

                        selectedPlaylist != null -> {
                            PlaylistDetailView(
                                playlist = selectedPlaylist!!,
                                tracks = viewModel.playlistTracks(selectedPlaylist!!.id)
                                    .collectAsState().value.filterPlaylistTrackSearch(searchQuery),
                                onBack = { selectedPlaylist = null },
                                onPlayTrack = { track ->
                                    viewModel.playPlaylist(selectedPlaylist!!.id, track.uri)
                                },
                                onPlayAll = {
                                    val first = viewModel.playlistTracks(selectedPlaylist!!.id).value.firstOrNull()
                                        ?: return@PlaylistDetailView
                                    viewModel.playPlaylist(selectedPlaylist!!.id, first.uri)
                                },
                                onMoveUp = { viewModel.movePlaylistTrackUp(selectedPlaylist!!.id, it.uri) },
                                onMoveDown = { viewModel.movePlaylistTrackDown(selectedPlaylist!!.id, it.uri) },
                                onRemove = { viewModel.removeTrackFromPlaylist(selectedPlaylist!!.id, it.uri) },
                                onDelete = {
                                    trackPendingDelete = it.toMusicTrackEntity()
                                }
                            )
                        }

                        selectedTab == MusicTab.Songs -> {
                            SongListView(
                                tracks = tracks.filterTrackSearch(searchQuery),
                                onPlay = { viewModel.playAllSongs(it.uri) },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onDelete = { trackPendingDelete = it }
                            )
                        }

                        selectedTab == MusicTab.Albums -> {
                            AlbumListView(
                                albums = albums.filterAlbumSearch(searchQuery),
                                onOpen = { selectedAlbum = it }
                            )
                        }

                        selectedTab == MusicTab.Genres -> {
                            GenreListView(
                                genres = genres.filterGenreSearch(searchQuery),
                                onOpen = { selectedGenre = it }
                            )
                        }

                        else -> {
                            PlaylistListView(
                                playlists = playlists.filterPlaylistSearch(searchQuery),
                                onCreate = { showCreatePlaylistDialog = true },
                                onOpen = { selectedPlaylist = it },
                                onRename = {
                                    playlistToRename = it
                                    playlistNameInput = it.name
                                },
                                onDelete = { viewModel.deletePlaylist(it.id) }
                            )
                        }
                    }
                }

                MusicPlayerPanel(
                    modifier = Modifier.weight(0.95f),
                    settings = settings,
                    playerState = playerState,
                    allowDelete = playerState.currentTrack != null,
                    onTogglePlayback = viewModel::togglePlayback,
                    onSeekTo = viewModel::seekTo,
                    onSkipNext = viewModel::skipNext,
                    onSkipPrevious = viewModel::skipPrevious,
                    onDelete = {
                        playerState.currentTrack?.let { trackPendingDelete = it }
                    },
                    onOpenLibrary = {}
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        TextInputDialog(
            title = "Create Playlist",
            initialValue = playlistNameInput,
            confirmLabel = "Create",
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                playlistNameInput = ""
                showCreatePlaylistDialog = false
                viewModel.createPlaylist(name)
            }
        )
    }

    playlistToRename?.let { playlist ->
        TextInputDialog(
            title = "Rename Playlist",
            initialValue = playlistNameInput,
            confirmLabel = "Save",
            onDismiss = {
                playlistToRename = null
                playlistNameInput = ""
            },
            onConfirm = { name ->
                viewModel.renamePlaylist(playlist.id, name)
                playlistToRename = null
                playlistNameInput = ""
            }
        )
    }

    trackPendingDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { trackPendingDelete = null },
            title = { Text("Delete song") },
            text = {
                Text("Delete \"${track.title}\" from storage permanently?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTrack(track.uri)
                    trackPendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { trackPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    addToPlaylistTrack?.let { track ->
        AlertDialog(
            onDismissRequest = { addToPlaylistTrack = null },
            title = { Text("Add to playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("Create a playlist first.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        playlists.forEach { playlist ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .clickable {
                                        viewModel.addTrackToPlaylist(playlist.id, track.uri)
                                        addToPlaylistTrack = null
                                    }
                                    .padding(14.dp)
                            ) {
                                Text(playlist.name, color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (playlists.isEmpty()) {
                    TextButton(onClick = {
                        addToPlaylistTrack = null
                        showCreatePlaylistDialog = true
                    }) {
                        Text("Create Playlist")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { addToPlaylistTrack = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun MusicHeader(
    settings: MusicSettingsEntity,
    onBack: () -> Unit,
    onChooseFolder: () -> Unit,
    onRescan: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Native Music",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = settings.folderName ?: "No folder selected",
                color = Color.White.copy(alpha = 0.70f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Button(onClick = onChooseFolder) { Text("Folder") }
            Button(onClick = onRescan) { Text("Rescan") }
        }
    }
}

@Composable
private fun MusicTabs(
    selectedTab: MusicTab,
    onTabSelected: (MusicTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MusicTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (selected) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f)
                    )
                    .border(1.dp, Color.White.copy(alpha = if (selected) 0.18f else 0.08f), RoundedCornerShape(18.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = tab.name,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun MusicEmptyBrowser(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            subtitle,
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun SongListView(
    tracks: List<MusicTrackEntity>,
    onPlay: (MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    TrackList(
        tracks = tracks,
        onPlay = onPlay,
        onAddToPlaylist = onAddToPlaylist,
        onDelete = onDelete
    )
}

@Composable
private fun AlbumListView(
    albums: List<AlbumSummary>,
    onOpen: (AlbumSummary) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(albums) { album ->
            SummaryRow(
                title = album.album,
                subtitle = "${album.artist} • ${album.songCount} songs",
                artworkPath = album.artworkPath,
                onClick = { onOpen(album) }
            )
        }
    }
}

@Composable
private fun GenreListView(
    genres: List<GenreSummary>,
    onOpen: (GenreSummary) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(genres) { genre ->
            SummaryRow(
                title = genre.genre,
                subtitle = "${genre.songCount} songs",
                artworkPath = genre.artworkPath,
                onClick = { onOpen(genre) }
            )
        }
    }
}

@Composable
private fun PlaylistListView(
    playlists: List<PlaylistWithCount>,
    onCreate: () -> Unit,
    onOpen: (PlaylistWithCount) -> Unit,
    onRename: (PlaylistWithCount) -> Unit,
    onDelete: (PlaylistWithCount) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = onCreate) {
            Text("Create Playlist")
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(playlists) { playlist ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(playlist.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${playlist.songCount} songs",
                            color = Color.White.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onOpen(playlist) }) { Text("Open") }
                            Button(onClick = { onRename(playlist) }) { Text("Rename") }
                            Button(onClick = { onDelete(playlist) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumDetailView(
    summary: AlbumSummary,
    tracks: List<MusicTrackEntity>,
    onBack: () -> Unit,
    onPlayTrack: (MusicTrackEntity) -> Unit,
    onPlayAll: () -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    DetailScaffold(
        title = summary.album,
        subtitle = summary.artist,
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        TrackList(tracks, onPlayTrack, onAddToPlaylist, onDelete)
    }
}

@Composable
private fun GenreDetailView(
    summary: GenreSummary,
    tracks: List<MusicTrackEntity>,
    onBack: () -> Unit,
    onPlayTrack: (MusicTrackEntity) -> Unit,
    onPlayAll: () -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    DetailScaffold(
        title = summary.genre,
        subtitle = "${summary.songCount} songs",
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        TrackList(tracks, onPlayTrack, onAddToPlaylist, onDelete)
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: PlaylistWithCount,
    tracks: List<PlaylistTrackWithSong>,
    onBack: () -> Unit,
    onPlayTrack: (PlaylistTrackWithSong) -> Unit,
    onPlayAll: () -> Unit,
    onMoveUp: (PlaylistTrackWithSong) -> Unit,
    onMoveDown: (PlaylistTrackWithSong) -> Unit,
    onRemove: (PlaylistTrackWithSong) -> Unit,
    onDelete: (PlaylistTrackWithSong) -> Unit,
) {
    DetailScaffold(
        title = playlist.name,
        subtitle = "${playlist.songCount} songs",
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tracks) { track ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(track.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${track.artist} • ${track.album}",
                            color = Color.White.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onPlayTrack(track) }) { Text("Play") }
                            Button(onClick = { onMoveUp(track) }) { Text("Up") }
                            Button(onClick = { onMoveDown(track) }) { Text("Down") }
                            Button(onClick = { onRemove(track) }) { Text("Remove") }
                            Button(onClick = { onDelete(track) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(subtitle, color = Color.White.copy(alpha = 0.68f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBack) { Text("Back") }
                Button(onClick = onPlayAll) { Text("Play All") }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun TrackList(
    tracks: List<MusicTrackEntity>,
    onPlay: (MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tracks) { track ->
            TrackRow(
                track = track,
                onPlay = { onPlay(track) },
                onAddToPlaylist = { onAddToPlaylist(track) },
                onDelete = { onDelete(track) }
            )
        }
    }
}

@Composable
private fun TrackRow(
    track: MusicTrackEntity,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.artworkPath?.let(::File),
                contentDescription = track.title,
                error = painterResource(R.drawable.music),
                placeholder = painterResource(R.drawable.music),
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${track.artist} • ${track.album}",
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${track.genre} • ${track.durationMs / 1000}s",
                    color = Color.White.copy(alpha = 0.48f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPlay) { Text("Play") }
                Button(onClick = onAddToPlaylist) { Text("Playlist") }
                Button(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    title: String,
    subtitle: String,
    artworkPath: String?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = artworkPath?.let(::File),
                contentDescription = title,
                error = painterResource(R.drawable.music),
                placeholder = painterResource(R.drawable.music),
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    initialValue: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun List<MusicTrackEntity>.filterTrackSearch(query: String): List<MusicTrackEntity> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter {
        it.title.contains(normalized, ignoreCase = true) ||
            it.artist.contains(normalized, ignoreCase = true) ||
            it.album.contains(normalized, ignoreCase = true) ||
            it.genre.contains(normalized, ignoreCase = true)
    }
}

private fun List<AlbumSummary>.filterAlbumSearch(query: String): List<AlbumSummary> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter {
        it.album.contains(normalized, ignoreCase = true) ||
            it.artist.contains(normalized, ignoreCase = true)
    }
}

private fun List<GenreSummary>.filterGenreSearch(query: String): List<GenreSummary> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter { it.genre.contains(normalized, ignoreCase = true) }
}

private fun List<PlaylistWithCount>.filterPlaylistSearch(query: String): List<PlaylistWithCount> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter { it.name.contains(normalized, ignoreCase = true) }
}

private fun List<PlaylistTrackWithSong>.filterPlaylistTrackSearch(query: String): List<PlaylistTrackWithSong> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter {
        it.title.contains(normalized, ignoreCase = true) ||
            it.artist.contains(normalized, ignoreCase = true) ||
            it.album.contains(normalized, ignoreCase = true)
    }
}

private fun PlaylistTrackWithSong.toMusicTrackEntity(): MusicTrackEntity {
    return MusicTrackEntity(
        uri = uri,
        displayName = displayName,
        title = title,
        artist = artist,
        album = album,
        genre = genre,
        durationMs = durationMs,
        trackNumber = trackNumber,
        discNumber = discNumber,
        artworkPath = artworkPath,
        mimeType = mimeType,
        fileSizeBytes = fileSizeBytes,
        lastModified = lastModified,
        isAvailable = isAvailable
    )
}
