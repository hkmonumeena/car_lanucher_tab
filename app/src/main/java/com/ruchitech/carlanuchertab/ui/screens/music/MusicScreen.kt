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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.carlanuchertab.music.AlbumSummary
import com.ruchitech.carlanuchertab.music.GenreSummary
import com.ruchitech.carlanuchertab.music.MusicSettingsEntity
import com.ruchitech.carlanuchertab.music.MusicTrackEntity
import com.ruchitech.carlanuchertab.music.MusicViewModel
import com.ruchitech.carlanuchertab.music.PlaylistTrackWithSong
import com.ruchitech.carlanuchertab.music.PlaylistWithCount
import com.ruchitech.carlanuchertab.ui.composables.MusicAddToPlaylistDialog
import com.ruchitech.carlanuchertab.ui.composables.MusicAlbumArtwork
import com.ruchitech.carlanuchertab.ui.composables.MusicConfirmDialog
import com.ruchitech.carlanuchertab.ui.composables.MusicPlayerPanel
import com.ruchitech.carlanuchertab.ui.composables.MusicPlayerStyle
import com.ruchitech.carlanuchertab.ui.composables.MusicTextInputDialog
import com.ruchitech.carlanuchertab.ui.composables.formatDuration
private object MusicScreenColors {
    val BackgroundTop = Color(0xFF050A10)
    val BackgroundBottom = Color(0xFF121E2C)
    val PanelFill = Color(0xFF141C26)
    val RowFill = Color(0xFF1A2430)
    val Border = Color(0x22FFFFFF)
    val Accent = Color(0xFF5CE1E6)
    val TextPrimary = Color(0xFFF4F7FA)
    val TextSecondary = Color(0xB3F4F7FA)
    val TextMuted = Color(0x80F4F7FA)
}

private enum class MusicTab {
    Playlists,
    Albums,
    Songs,
    Genres,
    Liked,
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
    val likedTracks by viewModel.likedTracks.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val likedTrackUris = remember(likedTracks) { likedTracks.map { it.uri }.toSet() }
    val currentTrack = playerState.currentTrack
    val isCurrentTrackLiked = currentTrack != null && currentTrack.uri in likedTrackUris

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
                    Brush.verticalGradient(
                        colors = listOf(
                            MusicScreenColors.BackgroundTop,
                            MusicScreenColors.BackgroundBottom
                        )
                    )
                )
                .padding(padding)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MusicScreenColors.PanelFill)
                        .border(1.dp, MusicScreenColors.Border, RoundedCornerShape(24.dp))
                        .padding(16.dp)
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
                        label = { Text("Search library", color = MusicScreenColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MusicScreenColors.TextPrimary,
                            unfocusedTextColor = MusicScreenColors.TextPrimary,
                            focusedBorderColor = MusicScreenColors.Accent,
                            unfocusedBorderColor = MusicScreenColors.Border,
                            cursorColor = MusicScreenColors.Accent,
                            focusedLabelColor = MusicScreenColors.Accent,
                            unfocusedLabelColor = MusicScreenColors.TextMuted
                        )
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
                                likedTrackUris = likedTrackUris,
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onToggleLike = { viewModel.toggleLike(it.uri) },
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
                                likedTrackUris = likedTrackUris,
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onToggleLike = { viewModel.toggleLike(it.uri) },
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
                                likedTrackUris = likedTrackUris,
                                onPlay = { viewModel.playAllSongs(it.uri) },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onToggleLike = { viewModel.toggleLike(it.uri) },
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

                        selectedTab == MusicTab.Liked -> {
                            LikedSongsView(
                                tracks = likedTracks.filterTrackSearch(searchQuery),
                                likedTrackUris = likedTrackUris,
                                onPlay = { viewModel.playLikedSongs(it.uri) },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onToggleLike = { viewModel.toggleLike(it.uri) },
                                onDelete = { trackPendingDelete = it }
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
                    style = MusicPlayerStyle.Expanded,
                    settings = settings,
                    playerState = playerState,
                    allowDelete = playerState.currentTrack != null,
                    isCurrentTrackLiked = isCurrentTrackLiked,
                    onTogglePlayback = viewModel::togglePlayback,
                    onSeekTo = viewModel::seekTo,
                    onSkipNext = viewModel::skipNext,
                    onSkipPrevious = viewModel::skipPrevious,
                    onDelete = {
                        playerState.currentTrack?.let { trackPendingDelete = it }
                    },
                    onOpenLibrary = {},
                    onToggleLike = viewModel::toggleLikeCurrentTrack,
                    onAddToPlaylist = {
                        playerState.currentTrack?.let { addToPlaylistTrack = it }
                    }
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        MusicTextInputDialog(
            title = "New playlist",
            initialValue = playlistNameInput,
            fieldLabel = "Name",
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
        MusicTextInputDialog(
            title = "Rename",
            subtitle = playlist.name,
            initialValue = playlistNameInput,
            fieldLabel = "Name",
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
        MusicConfirmDialog(
            title = "Delete song",
            message = "Delete \"${track.title}\" from storage permanently?",
            confirmLabel = "Delete",
            destructive = true,
            onDismiss = { trackPendingDelete = null },
            onConfirm = {
                viewModel.deleteTrack(track.uri)
                trackPendingDelete = null
            }
        )
    }

    addToPlaylistTrack?.let { track ->
        MusicAddToPlaylistDialog(
            trackTitle = track.title,
            playlists = playlists,
            onDismiss = { addToPlaylistTrack = null },
            onSelectPlaylist = { playlist ->
                viewModel.addTrackToPlaylist(playlist.id, track.uri)
                addToPlaylistTrack = null
            },
            onCreatePlaylist = {
                addToPlaylistTrack = null
                showCreatePlaylistDialog = true
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MusicScreenColors.TextPrimary
                )
            }
            Column {
                Text(
                    text = "Music Library",
                    color = MusicScreenColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = settings.folderName ?: "No folder selected",
                    color = MusicScreenColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onChooseFolder) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Choose folder",
                    tint = MusicScreenColors.Accent
                )
            }
            IconButton(onClick = onRescan) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rescan",
                    tint = MusicScreenColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MusicTabs(
    selectedTab: MusicTab,
    onTabSelected: (MusicTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MusicTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (selected) MusicScreenColors.Accent.copy(alpha = 0.18f)
                        else Color.White.copy(alpha = 0.05f)
                    )
                    .border(
                        1.dp,
                        if (selected) MusicScreenColors.Accent.copy(alpha = 0.5f) else MusicScreenColors.Border,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    text = tab.name,
                    color = if (selected) MusicScreenColors.Accent else MusicScreenColors.TextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
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
        Text(
            title,
            color = MusicScreenColors.TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitle,
            color = MusicScreenColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onAction) {
            Text(actionLabel, color = MusicScreenColors.Accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SongListView(
    tracks: List<MusicTrackEntity>,
    likedTrackUris: Set<String>,
    onPlay: (MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    TrackList(
        tracks = tracks,
        likedTrackUris = likedTrackUris,
        onPlay = onPlay,
        onAddToPlaylist = onAddToPlaylist,
        onToggleLike = onToggleLike,
        onDelete = onDelete
    )
}

@Composable
private fun LikedSongsView(
    tracks: List<MusicTrackEntity>,
    likedTrackUris: Set<String>,
    onPlay: (MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    if (tracks.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = MusicScreenColors.Accent,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "No liked songs yet",
                color = MusicScreenColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Tap the heart on a playing track to save it here.",
                color = MusicScreenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    } else {
        TrackList(
            tracks = tracks,
            likedTrackUris = likedTrackUris,
            onPlay = onPlay,
            onAddToPlaylist = onAddToPlaylist,
            onToggleLike = onToggleLike,
            onDelete = onDelete
        )
    }
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
        TextButton(onClick = onCreate) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MusicScreenColors.Accent)
            Spacer(modifier = Modifier.width(6.dp))
            Text("New playlist", color = MusicScreenColors.Accent, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(playlists) { playlist ->
                LibraryListCard(onClick = { onOpen(playlist) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                playlist.name,
                                color = MusicScreenColors.TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${playlist.songCount} songs",
                                color = MusicScreenColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { onRename(playlist) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = MusicScreenColors.TextSecondary
                            )
                        }
                        IconButton(onClick = { onDelete(playlist) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF8A80)
                            )
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
    likedTrackUris: Set<String>,
    onBack: () -> Unit,
    onPlayTrack: (MusicTrackEntity) -> Unit,
    onPlayAll: () -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    DetailScaffold(
        title = summary.album,
        subtitle = summary.artist,
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        TrackList(
            tracks = tracks,
            likedTrackUris = likedTrackUris,
            onPlay = onPlayTrack,
            onAddToPlaylist = onAddToPlaylist,
            onToggleLike = onToggleLike,
            onDelete = onDelete
        )
    }
}

@Composable
private fun GenreDetailView(
    summary: GenreSummary,
    tracks: List<MusicTrackEntity>,
    likedTrackUris: Set<String>,
    onBack: () -> Unit,
    onPlayTrack: (MusicTrackEntity) -> Unit,
    onPlayAll: () -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
) {
    DetailScaffold(
        title = summary.genre,
        subtitle = "${summary.songCount} songs",
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        TrackList(
            tracks = tracks,
            likedTrackUris = likedTrackUris,
            onPlay = onPlayTrack,
            onAddToPlaylist = onAddToPlaylist,
            onToggleLike = onToggleLike,
            onDelete = onDelete
        )
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
                LibraryListCard(onClick = { onPlayTrack(track) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MusicAlbumArtwork(
                            artworkPath = track.artworkPath,
                            title = track.title,
                            modifier = Modifier.size(52.dp),
                            cornerRadius = 10.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                track.title,
                                color = MusicScreenColors.TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${track.artist} • ${track.album}",
                                color = MusicScreenColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onMoveUp(track) }) {
                            Text("↑", color = MusicScreenColors.TextSecondary)
                        }
                        IconButton(onClick = { onMoveDown(track) }) {
                            Text("↓", color = MusicScreenColors.TextSecondary)
                        }
                        IconButton(onClick = { onRemove(track) }) {
                            Text("✕", color = Color(0xFFFF8A80))
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MusicScreenColors.TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = MusicScreenColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(subtitle, color = MusicScreenColors.TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onPlayAll) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play all",
                    tint = MusicScreenColors.Accent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun TrackList(
    tracks: List<MusicTrackEntity>,
    likedTrackUris: Set<String>,
    onPlay: (MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
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
                isLiked = track.uri in likedTrackUris,
                onPlay = { onPlay(track) },
                onAddToPlaylist = { onAddToPlaylist(track) },
                onToggleLike = { onToggleLike(track) },
                onDelete = { onDelete(track) }
            )
        }
    }
}

@Composable
private fun TrackRow(
    track: MusicTrackEntity,
    isLiked: Boolean,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleLike: () -> Unit,
    onDelete: () -> Unit,
) {
    LibraryListCard(onClick = onPlay) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MusicAlbumArtwork(
                artworkPath = track.artworkPath,
                title = track.title,
                modifier = Modifier.size(56.dp),
                cornerRadius = 12.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = MusicScreenColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${track.artist} • ${track.album}",
                    color = MusicScreenColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatDuration(track.durationMs),
                    color = MusicScreenColors.TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = onAddToPlaylist) {
                Icon(
                    Icons.Default.PlaylistAdd,
                    contentDescription = "Add to playlist",
                    tint = MusicScreenColors.TextSecondary
                )
            }
            IconButton(onClick = onToggleLike) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) MusicScreenColors.Accent else MusicScreenColors.TextSecondary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF8A80)
                )
            }
        }
    }
}

@Composable
private fun LibraryListCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MusicScreenColors.RowFill)
            .border(1.dp, MusicScreenColors.Border, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        content()
    }
}

@Composable
private fun SummaryRow(
    title: String,
    subtitle: String,
    artworkPath: String?,
    onClick: () -> Unit,
) {
    LibraryListCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicAlbumArtwork(
                artworkPath = artworkPath,
                title = title,
                modifier = Modifier.size(56.dp),
                cornerRadius = 12.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = MusicScreenColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    color = MusicScreenColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
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
