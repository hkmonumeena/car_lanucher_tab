package com.ruchitech.carlanuchertab.ui.screens.music

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.ruchitech.carlanuchertab.music.AlbumSummary
import com.ruchitech.carlanuchertab.music.GenreSummary
import com.ruchitech.carlanuchertab.music.MusicSettingsEntity
import com.ruchitech.carlanuchertab.music.MusicTrackEntity
import com.ruchitech.carlanuchertab.music.MusicViewModel
import com.ruchitech.carlanuchertab.music.PlaylistTrackWithSong
import com.ruchitech.carlanuchertab.music.PlaylistWithCount
import com.ruchitech.carlanuchertab.ui.composables.CockpitPalette
import com.ruchitech.carlanuchertab.ui.composables.CockpitControlChip
import com.ruchitech.carlanuchertab.ui.composables.CockpitSectionHeader
import com.ruchitech.carlanuchertab.ui.composables.MusicAddToPlaylistDialog
import com.ruchitech.carlanuchertab.ui.composables.MusicAlbumArtwork
import com.ruchitech.carlanuchertab.ui.composables.MusicConfirmDialog
import com.ruchitech.carlanuchertab.ui.composables.MusicPlayerPanel
import com.ruchitech.carlanuchertab.ui.composables.MusicPlayerStyle
import com.ruchitech.carlanuchertab.ui.composables.MusicQueueBottomSheet
import com.ruchitech.carlanuchertab.ui.composables.MusicSoundControlBottomSheet
import com.ruchitech.carlanuchertab.ui.composables.MusicTextInputDialog
import com.ruchitech.carlanuchertab.ui.composables.formatDuration
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class SongMetadataDraft(
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val yearText: String,
    val artworkUri: Uri? = null,
)

private object MusicScreenColors {
    val BackgroundTop = CockpitPalette.BackgroundTop
    val BackgroundBottom = CockpitPalette.BackgroundBottom
    val BackgroundMid = CockpitPalette.BackgroundMid
    val PremiumSteel = CockpitPalette.SurfaceRaised
    val PremiumCharcoal = CockpitPalette.BackgroundTop
    val PremiumAccentBand = Color(0xFF243646)
    val PanelFill = CockpitPalette.SurfaceTop
    val RowFill = CockpitPalette.SurfaceRaised
    val Border = CockpitPalette.Border
    val Accent = CockpitPalette.Accent
    val TextPrimary = CockpitPalette.TextPrimary
    val TextSecondary = CockpitPalette.TextSecondary
    val TextMuted = CockpitPalette.TextMuted
}

private enum class MusicTab {
    Playlists,
    Albums,
    Songs,
    Smart,
    Genres,
    Liked,
}

private enum class MusicLibraryViewMode {
    List,
    Grid,
}

private fun createMusicFolderPickerIntent(): Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
    }
}

private fun requiredAudioReadPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun Context.hasAudioReadPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        requiredAudioReadPermission()
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

private fun Context.canOpenDocumentTreePicker(): Boolean {
    return createMusicFolderPickerIntent().resolveActivity(packageManager) != null
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
    val recentlyAdded by viewModel.recentlyAddedTracks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedTracks.collectAsState()
    val mostPlayed by viewModel.mostPlayedTracks.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val soundControlState by viewModel.soundControlState.collectAsState()
    val likedTrackUris = remember(likedTracks) { likedTracks.map { it.uri }.toSet() }
    val currentTrack = playerState.currentTrack
    val isCurrentTrackLiked = currentTrack != null && currentTrack.uri in likedTrackUris
    var dynamicBgTop by remember { mutableStateOf(MusicScreenColors.BackgroundTop) }
    var dynamicBgMid by remember { mutableStateOf(Color(0xFF0D1622)) }
    var dynamicBgBottom by remember { mutableStateOf(MusicScreenColors.BackgroundBottom) }

    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val canOpenDocumentTreePicker = remember(context) { context.canOpenDocumentTreePicker() }
    val useCompactLibraryChrome = configuration.screenWidthDp > configuration.screenHeightDp
    var selectedTab by rememberSaveable { mutableStateOf(MusicTab.Songs) }
    var libraryViewMode by rememberSaveable { mutableStateOf(MusicLibraryViewMode.List) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedAlbum by remember { mutableStateOf<AlbumSummary?>(null) }
    var selectedGenre by remember { mutableStateOf<GenreSummary?>(null) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistWithCount?>(null) }
    var playlistNameInput by rememberSaveable { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistWithCount?>(null) }
    var trackPendingDelete by remember { mutableStateOf<MusicTrackEntity?>(null) }
    var addToPlaylistTrack by remember { mutableStateOf<MusicTrackEntity?>(null) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSoundControlSheet by remember { mutableStateOf(false) }
    var editingTrack by remember { mutableStateOf<MusicTrackEntity?>(null) }
    var metadataDraft by remember { mutableStateOf<SongMetadataDraft?>(null) }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = data.data ?: return@rememberLauncherForActivityResult
        viewModel.onFolderSelected(uri, data.flags)
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.useDeviceLibrary()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Allow audio access to scan the device music library."
                )
            }
        }
    }

    val artworkPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        metadataDraft = metadataDraft?.copy(artworkUri = uri)
    }

    val openMusicLibrarySource: () -> Unit = {
        if (canOpenDocumentTreePicker) {
            try {
                folderLauncher.launch(createMusicFolderPickerIntent())
            } catch (_: ActivityNotFoundException) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Folder picker is unavailable on this device. Scanning the device music library instead."
                    )
                }
                if (context.hasAudioReadPermission()) {
                    viewModel.useDeviceLibrary()
                } else {
                    audioPermissionLauncher.launch(requiredAudioReadPermission())
                }
            }
        } else if (context.hasAudioReadPermission()) {
            viewModel.useDeviceLibrary()
        } else {
            audioPermissionLauncher.launch(requiredAudioReadPermission())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    fun cinematicDim(color: Int, brightnessScale: Float, saturationScale: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hsv[1] = (hsv[1] * saturationScale).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * brightnessScale).coerceIn(0f, 1f)
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    fun blendColors(a: Color, b: Color, ratio: Float): Color {
        val t = ratio.coerceIn(0f, 1f)
        return Color(
            red = a.red * (1f - t) + b.red * t,
            green = a.green * (1f - t) + b.green * t,
            blue = a.blue * (1f - t) + b.blue * t,
            alpha = a.alpha * (1f - t) + b.alpha * t
        )
    }

    LaunchedEffect(currentTrack?.artworkPath) {
        val path = currentTrack?.artworkPath
        if (path.isNullOrBlank()) {
            dynamicBgTop = MusicScreenColors.BackgroundTop
            dynamicBgMid = MusicScreenColors.BackgroundMid
            dynamicBgBottom = MusicScreenColors.BackgroundBottom
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(File(path))
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@runCatching
                    val palette = Palette.from(bitmap).generate()
                    val dominant = palette.getDominantColor(MusicScreenColors.BackgroundTop.toArgb())
                    val darkVibrant = palette.getDarkVibrantColor(dominant)
                    val tintedTop = cinematicDim(darkVibrant, brightnessScale = 0.35f, saturationScale = 0.46f)
                    val tintedMid = cinematicDim(dominant, brightnessScale = 0.30f, saturationScale = 0.40f)
                    val tintedBottom = cinematicDim(dominant, brightnessScale = 0.20f, saturationScale = 0.30f)

                    dynamicBgTop = blendColors(MusicScreenColors.PremiumCharcoal, tintedTop, 0.46f)
                    dynamicBgMid = blendColors(MusicScreenColors.PremiumAccentBand, tintedMid, 0.52f)
                    dynamicBgBottom = blendColors(MusicScreenColors.BackgroundBottom, tintedBottom, 0.42f)
                }
            }.onFailure {
                dynamicBgTop = MusicScreenColors.BackgroundTop
                dynamicBgMid = MusicScreenColors.BackgroundMid
                dynamicBgBottom = MusicScreenColors.BackgroundBottom
            }
        }
    }

    val animatedBgTop by animateColorAsState(dynamicBgTop, label = "musicScreenBgTop")
    val animatedBgMid by animateColorAsState(dynamicBgMid, label = "musicScreenBgMid")
    val animatedBgBottom by animateColorAsState(dynamicBgBottom, label = "musicScreenBgBottom")

    val filteredTracks = remember(tracks, searchQuery) { tracks.filterTrackSearch(searchQuery) }
    val filteredAlbums = remember(albums, searchQuery) { albums.filterAlbumSearch(searchQuery) }
    val filteredGenres = remember(genres, searchQuery) { genres.filterGenreSearch(searchQuery) }
    val filteredPlaylists = remember(playlists, searchQuery) { playlists.filterPlaylistSearch(searchQuery) }
    val filteredLiked = remember(likedTracks, searchQuery) { likedTracks.filterTrackSearch(searchQuery) }
    val smartTrackCount = remember(recentlyAdded, recentlyPlayed, mostPlayed, searchQuery) {
        (recentlyAdded.filterTrackSearch(searchQuery) +
            recentlyPlayed.filterTrackSearch(searchQuery) +
            mostPlayed.filterTrackSearch(searchQuery))
            .distinctBy { it.uri }
            .size
    }
    val libraryCountLabel = when (selectedTab) {
        MusicTab.Songs -> "${filteredTracks.size} songs"
        MusicTab.Albums -> "${filteredAlbums.size} albums"
        MusicTab.Genres -> "${filteredGenres.size} genres"
        MusicTab.Liked -> "${filteredLiked.size} liked"
        MusicTab.Playlists -> "${filteredPlaylists.size} playlists"
        MusicTab.Smart -> "$smartTrackCount songs"
    }
    val showLibraryToolbar = !settings.folderUri.isNullOrBlank() &&
        selectedAlbum == null && selectedGenre == null && selectedPlaylist == null
    val librarySetupTitle = if (canOpenDocumentTreePicker) {
        "Choose a folder to build your library"
    } else {
        "Scan the device music library"
    }
    val librarySetupSubtitle = if (canOpenDocumentTreePicker) {
        "The player will scan this folder recursively and keep it available across Home and Music screens."
    } else {
        "This stereo does not provide a folder picker, so the player will import songs from the system music library instead."
    }
    val librarySetupActionLabel = if (canOpenDocumentTreePicker) {
        "Choose Folder"
    } else {
        "Scan Device Library"
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
                            animatedBgTop,
                            animatedBgMid,
                            animatedBgBottom
                        )
                    )
                )
                .padding(padding)
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.22f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.30f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                animatedBgMid.copy(alpha = 0.18f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.24f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                animatedBgTop.copy(alpha = 0.10f),
                                animatedBgMid.copy(alpha = 0.24f),
                                animatedBgBottom.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )
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
                        onChooseFolder = openMusicLibrarySource,
                        onRescan = viewModel::rescanLibrary
                    )

                    Spacer(modifier = Modifier.height(if (useCompactLibraryChrome) 8.dp else 12.dp))

                    if (useCompactLibraryChrome && showLibraryToolbar) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MusicSearchField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                compact = true,
                                placeholder = "Search $libraryCountLabel"
                            )
                            MusicLibraryToolbar(
                                countLabel = libraryCountLabel,
                                viewMode = libraryViewMode,
                                onViewModeChange = { libraryViewMode = it },
                                compact = true,
                            )
                        }
                    } else {
                        MusicSearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            compact = useCompactLibraryChrome,
                            placeholder = "Search $libraryCountLabel"
                        )
                    }

                    Spacer(modifier = Modifier.height(if (useCompactLibraryChrome) 8.dp else 12.dp))

                    MusicTabs(
                        selectedTab = selectedTab,
                        compact = useCompactLibraryChrome,
                        onTabSelected = {
                            selectedAlbum = null
                            selectedGenre = null
                            selectedPlaylist = null
                            selectedTab = it
                        }
                    )
                    Spacer(modifier = Modifier.height(if (useCompactLibraryChrome) 8.dp else 12.dp))

                    if (showLibraryToolbar && !useCompactLibraryChrome) {
                        MusicLibraryToolbar(
                            countLabel = libraryCountLabel,
                            viewMode = libraryViewMode,
                            onViewModeChange = { libraryViewMode = it },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    when {
                        settings.folderUri.isNullOrBlank() -> {
                            MusicEmptyBrowser(
                                title = librarySetupTitle,
                                subtitle = librarySetupSubtitle,
                                actionLabel = librarySetupActionLabel,
                                onAction = openMusicLibrarySource
                            )
                        }

                        selectedAlbum != null -> {
                            AlbumDetailView(
                                summary = selectedAlbum!!,
                                tracks = viewModel.albumTracks(selectedAlbum!!.album)
                                    .collectAsState().value.filterTrackSearch(searchQuery),
                                viewMode = libraryViewMode,
                                onViewModeChange = { libraryViewMode = it },
                                onBack = { selectedAlbum = null },
                                onPlayTrack = { track ->
                                    viewModel.playAlbum(selectedAlbum!!.album, track.uri)
                                },
                                onPlayAll = {
                                    val first = viewModel.albumTracks(selectedAlbum!!.album).value.firstOrNull()
                                        ?: return@AlbumDetailView
                                    viewModel.playAlbum(selectedAlbum!!.album, first.uri)
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
                                viewMode = libraryViewMode,
                                onViewModeChange = { libraryViewMode = it },
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
                                viewMode = libraryViewMode,
                                onViewModeChange = { libraryViewMode = it },
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
                                tracks = filteredTracks,
                                viewMode = libraryViewMode,
                                likedTrackUris = likedTrackUris,
                                onPlay = { viewModel.playAllSongs(it.uri) },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onToggleLike = { viewModel.toggleLike(it.uri) },
                                onDelete = { trackPendingDelete = it },
                                onEditMetadata = { track ->
                                    editingTrack = track
                                    metadataDraft = SongMetadataDraft(
                                        title = track.title,
                                        artist = track.artist,
                                        album = track.album,
                                        genre = track.genre,
                                        yearText = if (track.year > 0) track.year.toString() else "",
                                    )
                                }
                            )
                        }

                        selectedTab == MusicTab.Smart -> {
                            SmartHubView(
                                allTracks = filteredTracks,
                                recentlyAdded = recentlyAdded,
                                recentlyPlayed = recentlyPlayed,
                                mostPlayed = mostPlayed,
                                likedTracks = filteredLiked,
                                genres = filteredGenres,
                                currentTrack = currentTrack,
                                viewMode = libraryViewMode,
                                likedTrackUris = likedTrackUris,
                                searchQuery = searchQuery,
                                onResume = viewModel::togglePlayback,
                                onPlayAll = { viewModel.playAllSongs(it.uri) },
                                onPlayRecent = { viewModel.playRecentlyAdded(it.uri) },
                                onPlayPlayed = { viewModel.playRecentlyPlayed(it.uri) },
                                onPlayMost = { viewModel.playMostPlayed(it.uri) },
                                onPlayLiked = { viewModel.playLikedSongs(it.uri) },
                                onPlayGenre = { genre, track -> viewModel.playGenre(genre, track.uri) },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onToggleLike = { viewModel.toggleLike(it.uri) },
                                onDelete = { trackPendingDelete = it },
                                onOpenQueue = { showQueueSheet = true }
                            )
                        }

                        selectedTab == MusicTab.Albums -> {
                            AlbumListView(
                                albums = filteredAlbums,
                                viewMode = libraryViewMode,
                                onOpen = { selectedAlbum = it }
                            )
                        }

                        selectedTab == MusicTab.Genres -> {
                            GenreListView(
                                genres = filteredGenres,
                                viewMode = libraryViewMode,
                                onOpen = { selectedGenre = it }
                            )
                        }

                        selectedTab == MusicTab.Liked -> {
                            LikedSongsView(
                                tracks = filteredLiked,
                                viewMode = libraryViewMode,
                                likedTrackUris = likedTrackUris,
                                onPlay = { viewModel.playLikedSongs(it.uri) },
                                onAddToPlaylist = { addToPlaylistTrack = it },
                                onToggleLike = { viewModel.toggleLike(it.uri) },
                                onDelete = { trackPendingDelete = it }
                            )
                        }

                        else -> {
                            PlaylistListView(
                                playlists = filteredPlaylists,
                                viewMode = libraryViewMode,
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
                    },
                    onToggleShuffle = viewModel::toggleShuffle,
                    onCycleRepeat = viewModel::cycleRepeatMode,
                    onOpenQueue = { showQueueSheet = true },
                    onOpenSoundControl = { showSoundControlSheet = true },
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

    if (editingTrack != null && metadataDraft != null) {
        EditSongMetadataScreen(
            track = editingTrack!!,
            draft = metadataDraft!!,
            onUpdateDraft = { metadataDraft = it },
            onPickArtwork = { artworkPickerLauncher.launch("image/*") },
            onDismiss = {
                editingTrack = null
                metadataDraft = null
            },
            onSave = {
                val track = editingTrack ?: return@EditSongMetadataScreen
                val draft = metadataDraft ?: return@EditSongMetadataScreen
                viewModel.updateTrackMetadata(
                    trackUri = track.uri,
                    title = draft.title,
                    artist = draft.artist,
                    album = draft.album,
                    genre = draft.genre,
                    year = draft.yearText.toIntOrNull() ?: 0,
                    artworkUri = draft.artworkUri
                )
                editingTrack = null
                metadataDraft = null
            }
        )
    }

    MusicQueueBottomSheet(
        visible = showQueueSheet,
        onDismiss = { showQueueSheet = false },
        playerState = playerState,
        onCrossfadeChange = viewModel::setCrossfadeEnabled,
        onMoveQueueItem = viewModel::moveQueueItem,
        onRemoveQueueItem = viewModel::removeQueueItem,
        onClearQueue = {
            viewModel.clearQueue()
            showQueueSheet = false
        },
        onPlayQueueIndex = viewModel::playQueueIndex,
    )

    MusicSoundControlBottomSheet(
        visible = showSoundControlSheet,
        onDismiss = { showSoundControlSheet = false },
        state = soundControlState,
        onPreset = viewModel::setSoundPreset,
        onBassChange = viewModel::setBassLevel,
        onTrebleChange = viewModel::setTrebleLevel,
        onLoudnessChange = viewModel::setLoudnessLevel,
        onSoundZoneChange = viewModel::setSoundZone,
        onReset = viewModel::resetSoundControl,
    )
}

@Composable
private fun SmartHubView(
    allTracks: List<MusicTrackEntity>,
    recentlyAdded: List<MusicTrackEntity>,
    recentlyPlayed: List<MusicTrackEntity>,
    mostPlayed: List<MusicTrackEntity>,
    likedTracks: List<MusicTrackEntity>,
    genres: List<GenreSummary>,
    currentTrack: MusicTrackEntity?,
    viewMode: MusicLibraryViewMode,
    likedTrackUris: Set<String>,
    searchQuery: String,
    onResume: () -> Unit,
    onPlayAll: (MusicTrackEntity) -> Unit,
    onPlayRecent: (MusicTrackEntity) -> Unit,
    onPlayPlayed: (MusicTrackEntity) -> Unit,
    onPlayMost: (MusicTrackEntity) -> Unit,
    onPlayLiked: (MusicTrackEntity) -> Unit,
    onPlayGenre: (String, MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
    onOpenQueue: () -> Unit,
) {
    val ra = recentlyAdded.filterTrackSearch(searchQuery)
    val rp = recentlyPlayed.filterTrackSearch(searchQuery)
    val mp = mostPlayed.filterTrackSearch(searchQuery)
    val liked = likedTracks.filterTrackSearch(searchQuery)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ForYouHero(
                currentTrack = currentTrack,
                fallbackTrack = rp.firstOrNull() ?: ra.firstOrNull() ?: allTracks.firstOrNull(),
                onResume = onResume,
                onPlayFallback = { track -> onPlayAll(track) },
                onOpenQueue = onOpenQueue
            )
        }
        item {
            TopGenresRow(
                genres = genres.take(8),
                allTracks = allTracks,
                onPlayGenre = onPlayGenre
            )
        }
        item {
            SmartHorizontalSection(
                title = "Recently added",
                emptyMessage = "No tracks in the library yet.",
                tracks = ra,
                likedTrackUris = likedTrackUris,
                onPlay = onPlayRecent,
                onPlayAll = { ra.firstOrNull()?.let(onPlayRecent) }
            )
        }
        item {
            SmartHorizontalSection(
                title = "Recently played",
                emptyMessage = "Play songs to build this list.",
                tracks = rp,
                likedTrackUris = likedTrackUris,
                onPlay = onPlayPlayed,
                onPlayAll = { rp.firstOrNull()?.let(onPlayPlayed) }
            )
        }
        item {
            SmartHorizontalSection(
                title = "Most played",
                emptyMessage = "Counts appear after you listen with the new player.",
                tracks = mp,
                likedTrackUris = likedTrackUris,
                onPlay = onPlayMost,
                onPlayAll = { mp.firstOrNull()?.let(onPlayMost) }
            )
        }
        item {
            SmartHorizontalSection(
                title = "Liked",
                emptyMessage = "Tap the heart on songs you want here.",
                tracks = liked,
                likedTrackUris = likedTrackUris,
                onPlay = onPlayLiked,
                onPlayAll = { liked.firstOrNull()?.let(onPlayLiked) }
            )
        }
    }
}

@Composable
private fun ForYouHero(
    currentTrack: MusicTrackEntity?,
    fallbackTrack: MusicTrackEntity?,
    onResume: () -> Unit,
    onPlayFallback: (MusicTrackEntity) -> Unit,
    onOpenQueue: () -> Unit,
) {
    val heroTrack = currentTrack ?: fallbackTrack
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        CockpitPalette.Accent.copy(alpha = 0.18f),
                        CockpitPalette.SurfaceRaised.copy(alpha = 0.94f),
                        CockpitPalette.SurfaceBottom
                    )
                )
            )
            .border(1.dp, CockpitPalette.BorderStrong, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        if (heroTrack == null) {
            MusicEmptyState(
                title = "Build your music zone",
                message = "Choose a library and your daily mixes will appear here."
            )
            return@Box
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicAlbumArtwork(
                artworkPath = heroTrack.artworkPath,
                title = heroTrack.title,
                modifier = Modifier.size(86.dp),
                cornerRadius = 14.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentTrack != null) "CONTINUE LISTENING" else "START YOUR DRIVE",
                    color = CockpitPalette.Accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = heroTrack.title,
                    color = MusicScreenColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${heroTrack.artist} • ${heroTrack.album}",
                    color = MusicScreenColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CockpitControlChip(
                        label = if (currentTrack != null) "Resume" else "Play",
                        selected = true,
                        onClick = {
                            if (currentTrack != null) onResume() else onPlayFallback(heroTrack)
                        }
                    )
                    CockpitControlChip(
                        label = "Queue",
                        selected = false,
                        onClick = onOpenQueue
                    )
                }
            }
        }
    }
}

@Composable
private fun TopGenresRow(
    genres: List<GenreSummary>,
    allTracks: List<MusicTrackEntity>,
    onPlayGenre: (String, MusicTrackEntity) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CockpitSectionHeader(title = "Top Genres")
        if (genres.isEmpty()) {
            SmartSectionEmpty("Genres appear after your library is scanned.")
            return
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(genres, key = { it.genre }) { genre ->
                Box(
                    modifier = Modifier
                        .width(132.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CockpitPalette.SurfaceRaised.copy(alpha = 0.88f))
                        .border(1.dp, CockpitPalette.Border, RoundedCornerShape(14.dp))
                        .clickable {
                            allTracks.firstOrNull { it.genre == genre.genre }?.let { track ->
                                onPlayGenre(genre.genre, track)
                            }
                        }
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = genre.genre,
                            color = MusicScreenColors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${genre.songCount} songs",
                            color = MusicScreenColors.TextMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartHorizontalSection(
    title: String,
    emptyMessage: String,
    tracks: List<MusicTrackEntity>,
    likedTrackUris: Set<String>,
    onPlay: (MusicTrackEntity) -> Unit,
    onPlayAll: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SmartSectionHeader(title)
            }
            if (tracks.isNotEmpty()) {
                TextButton(onClick = onPlayAll) {
                    Text("Play all", color = MusicScreenColors.Accent)
                }
            }
        }
        if (tracks.isEmpty()) {
            SmartSectionEmpty(emptyMessage)
            return
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tracks, key = { "${title}_${it.uri}" }) { track ->
                Box(modifier = Modifier.width(152.dp)) {
                    SmartSongCard(
                        track = track,
                        isLiked = track.uri in likedTrackUris,
                        onClick = { onPlay(track) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartSongCard(
    track: MusicTrackEntity,
    isLiked: Boolean,
    onClick: () -> Unit,
) {
    TrackGridItem(
        track = track,
        isLiked = isLiked,
        onClick = onClick
    )
}

@Composable
private fun MusicEmptyState(
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = MusicScreenColors.TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = message,
            color = MusicScreenColors.TextMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SmartSectionHeader(title: String) {
    Text(
        title,
        color = MusicScreenColors.Accent,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SmartSectionEmpty(message: String) {
    Text(
        message,
        color = MusicScreenColors.TextMuted,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(vertical = 6.dp)
    )
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = settings.folderName ?: "No library selected",
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
                    contentDescription = "Choose music library",
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
private fun MusicSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    placeholder: String = "Search library",
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = if (compact) {
            null
        } else {
            { Text(placeholder, color = MusicScreenColors.TextMuted) }
        },
        placeholder = if (compact) {
            { Text(placeholder, color = MusicScreenColors.TextMuted) }
        } else {
            null
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(if (compact) 12.dp else 14.dp),
        textStyle = if (compact) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge
        },
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
}

@Composable
private fun MusicTabs(
    selectedTab: MusicTab,
    compact: Boolean = false,
    onTabSelected: (MusicTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
    ) {
        listOf(
            MusicTab.Smart,
            MusicTab.Songs,
            MusicTab.Liked,
            MusicTab.Genres,
            MusicTab.Playlists,
            MusicTab.Albums
        ).forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(if (compact) 18.dp else 20.dp))
                    .background(
                        if (selected) MusicScreenColors.Accent.copy(alpha = 0.18f)
                        else Color.White.copy(alpha = 0.05f)
                    )
                    .border(
                        1.dp,
                        if (selected) MusicScreenColors.Accent.copy(alpha = 0.5f) else MusicScreenColors.Border,
                        RoundedCornerShape(if (compact) 18.dp else 20.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(
                        horizontal = if (compact) 14.dp else 18.dp,
                        vertical = if (compact) 7.dp else 9.dp
                    )
            ) {
                Text(
                    text = tab.displayLabel(),
                    color = if (selected) MusicScreenColors.Accent else MusicScreenColors.TextSecondary,
                    style = if (compact) {
                        MaterialTheme.typography.labelMedium
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

private fun MusicTab.displayLabel(): String {
    return when (this) {
        MusicTab.Smart -> "For You"
        else -> name
    }
}

@Composable
private fun MusicLibraryToolbar(
    countLabel: String,
    viewMode: MusicLibraryViewMode,
    onViewModeChange: (MusicLibraryViewMode) -> Unit,
    compact: Boolean = false,
) {
    Row(
        modifier = if (compact) Modifier else Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (compact) {
            Arrangement.spacedBy(8.dp)
        } else {
            Arrangement.SpaceBetween
        },
    ) {
       /* Text(
            text = countLabel,
            color = MusicScreenColors.TextSecondary,
            style = if (compact) {
                MaterialTheme.typography.labelLarge
            } else {
                MaterialTheme.typography.titleSmall
            },
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )*/
        Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)) {
            ViewModeToggleButton(
                selected = viewMode == MusicLibraryViewMode.List,
                icon = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "List view",
                compact = compact,
                onClick = { onViewModeChange(MusicLibraryViewMode.List) },
            )
            ViewModeToggleButton(
                selected = viewMode == MusicLibraryViewMode.Grid,
                icon = Icons.Default.GridView,
                contentDescription = "Grid view",
                compact = compact,
                onClick = { onViewModeChange(MusicLibraryViewMode.Grid) },
            )
        }
    }
}

@Composable
private fun ViewModeToggleButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(if (compact) 32.dp else 36.dp)
            .clip(RoundedCornerShape(if (compact) 8.dp else 10.dp))
            .background(
                if (selected) MusicScreenColors.Accent.copy(alpha = 0.18f)
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (selected) MusicScreenColors.Accent.copy(alpha = 0.45f) else Color.Transparent,
                shape = RoundedCornerShape(if (compact) 8.dp else 10.dp),
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) MusicScreenColors.Accent else MusicScreenColors.TextMuted,
            modifier = Modifier.size(if (compact) 18.dp else 20.dp),
        )
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
    viewMode: MusicLibraryViewMode,
    likedTrackUris: Set<String>,
    onPlay: (MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
    onEditMetadata: (MusicTrackEntity) -> Unit,
) {
    TrackCollectionView(
        tracks = tracks,
        viewMode = viewMode,
        likedTrackUris = likedTrackUris,
        onPlay = onPlay,
        onAddToPlaylist = onAddToPlaylist,
        onToggleLike = onToggleLike,
        onDelete = onDelete,
        onEditMetadata = onEditMetadata,
    )
}

@Composable
private fun LikedSongsView(
    tracks: List<MusicTrackEntity>,
    viewMode: MusicLibraryViewMode,
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
        TrackCollectionView(
            tracks = tracks,
            viewMode = viewMode,
            likedTrackUris = likedTrackUris,
            onPlay = onPlay,
            onAddToPlaylist = onAddToPlaylist,
            onToggleLike = onToggleLike,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun AlbumListView(
    albums: List<AlbumSummary>,
    viewMode: MusicLibraryViewMode,
    onOpen: (AlbumSummary) -> Unit,
) {
    if (viewMode == MusicLibraryViewMode.Grid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(132.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(albums, key = { "${it.artist}_${it.album}".lowercase() }) { album ->
                SummaryGridItem(
                    title = album.album,
                    subtitle = "${album.artist} • ${album.songCount} songs",
                    artworkPath = album.artworkPath,
                    onClick = { onOpen(album) },
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(albums, key = { "${it.artist}_${it.album}".lowercase() }) { album ->
                SummaryRow(
                    title = album.album,
                    subtitle = "${album.artist} • ${album.songCount} songs",
                    artworkPath = album.artworkPath,
                    onClick = { onOpen(album) }
                )
            }
        }
    }
}

@Composable
private fun GenreListView(
    genres: List<GenreSummary>,
    viewMode: MusicLibraryViewMode,
    onOpen: (GenreSummary) -> Unit,
) {
    if (viewMode == MusicLibraryViewMode.Grid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(132.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(genres, key = { it.genre.lowercase() }) { genre ->
                SummaryGridItem(
                    title = genre.genre,
                    subtitle = "${genre.songCount} songs",
                    artworkPath = genre.artworkPath,
                    onClick = { onOpen(genre) },
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(genres, key = { it.genre.lowercase() }) { genre ->
                SummaryRow(
                    title = genre.genre,
                    subtitle = "${genre.songCount} songs",
                    artworkPath = genre.artworkPath,
                    onClick = { onOpen(genre) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistListView(
    playlists: List<PlaylistWithCount>,
    viewMode: MusicLibraryViewMode,
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
        if (viewMode == MusicLibraryViewMode.Grid) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(132.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistGridItem(
                        playlist = playlist,
                        onOpen = { onOpen(playlist) },
                        onRename = { onRename(playlist) },
                        onDelete = { onDelete(playlist) },
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
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
}

@Composable
private fun AlbumDetailView(
    summary: AlbumSummary,
    tracks: List<MusicTrackEntity>,
    viewMode: MusicLibraryViewMode,
    onViewModeChange: (MusicLibraryViewMode) -> Unit,
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
        countLabel = "${tracks.size} songs",
        viewMode = viewMode,
        onViewModeChange = onViewModeChange,
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        TrackCollectionView(
            tracks = tracks,
            viewMode = viewMode,
            likedTrackUris = likedTrackUris,
            onPlay = onPlayTrack,
            onAddToPlaylist = onAddToPlaylist,
            onToggleLike = onToggleLike,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun GenreDetailView(
    summary: GenreSummary,
    tracks: List<MusicTrackEntity>,
    viewMode: MusicLibraryViewMode,
    onViewModeChange: (MusicLibraryViewMode) -> Unit,
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
        countLabel = "${tracks.size} songs",
        viewMode = viewMode,
        onViewModeChange = onViewModeChange,
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        TrackCollectionView(
            tracks = tracks,
            viewMode = viewMode,
            likedTrackUris = likedTrackUris,
            onPlay = onPlayTrack,
            onAddToPlaylist = onAddToPlaylist,
            onToggleLike = onToggleLike,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: PlaylistWithCount,
    tracks: List<PlaylistTrackWithSong>,
    viewMode: MusicLibraryViewMode,
    onViewModeChange: (MusicLibraryViewMode) -> Unit,
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
        countLabel = "${tracks.size} songs",
        viewMode = viewMode,
        onViewModeChange = onViewModeChange,
        onBack = onBack,
        onPlayAll = onPlayAll
    ) {
        if (viewMode == MusicLibraryViewMode.Grid) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(148.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // playlistPosition + uri ensures uniqueness even if the same song is in the playlist twice
                items(tracks, key = { "${it.playlistPosition}_${it.uri}" }) { track ->
                    TrackGridItem(
                        track = track.toMusicTrackEntity(),
                        isLiked = false,
                        onClick = { onPlayTrack(track) },
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // playlistPosition + uri ensures uniqueness even if the same song is in the playlist twice
                items(tracks, key = { "${it.playlistPosition}_${it.uri}" }) { track ->
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
}

@Composable
private fun DetailScaffold(
    title: String,
    subtitle: String,
    countLabel: String,
    viewMode: MusicLibraryViewMode,
    onViewModeChange: (MusicLibraryViewMode) -> Unit,
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
        Spacer(modifier = Modifier.height(8.dp))
        MusicLibraryToolbar(
            countLabel = countLabel,
            viewMode = viewMode,
            onViewModeChange = onViewModeChange,
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun TrackCollectionView(
    tracks: List<MusicTrackEntity>,
    viewMode: MusicLibraryViewMode,
    likedTrackUris: Set<String>,
    onPlay: (MusicTrackEntity) -> Unit,
    onAddToPlaylist: (MusicTrackEntity) -> Unit,
    onToggleLike: (MusicTrackEntity) -> Unit,
    onDelete: (MusicTrackEntity) -> Unit,
    onEditMetadata: (MusicTrackEntity) -> Unit = {},
) {
    if (viewMode == MusicLibraryViewMode.Grid) {
        TrackGrid(
            tracks = tracks,
            likedTrackUris = likedTrackUris,
            onPlay = onPlay,
        )
    } else {
        TrackList(
            tracks = tracks,
            likedTrackUris = likedTrackUris,
            onPlay = onPlay,
            onAddToPlaylist = onAddToPlaylist,
            onToggleLike = onToggleLike,
            onDelete = onDelete,
            onEditMetadata = onEditMetadata,
        )
    }
}

@Composable
private fun TrackGrid(
    tracks: List<MusicTrackEntity>,
    likedTrackUris: Set<String>,
    onPlay: (MusicTrackEntity) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(148.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(tracks, key = { it.uri }) { track ->
            TrackGridItem(
                track = track,
                isLiked = track.uri in likedTrackUris,
                onClick = { onPlay(track) },
            )
        }
    }
}

@Composable
private fun TrackGridItem(
    track: MusicTrackEntity,
    isLiked: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MusicScreenColors.RowFill)
            .border(1.dp, MusicScreenColors.Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box {
            MusicAlbumArtwork(
                artworkPath = track.artworkPath,
                title = track.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                cornerRadius = 10.dp,
            )
            if (isLiked) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MusicScreenColors.Accent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            track.title,
            color = MusicScreenColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            track.artist,
            color = MusicScreenColors.TextMuted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SummaryGridItem(
    title: String,
    subtitle: String,
    artworkPath: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MusicScreenColors.RowFill)
            .border(1.dp, MusicScreenColors.Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        MusicAlbumArtwork(
            artworkPath = artworkPath,
            title = title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            cornerRadius = 10.dp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            title,
            color = MusicScreenColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            subtitle,
            color = MusicScreenColors.TextMuted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlaylistGridItem(
    playlist: PlaylistWithCount,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MusicScreenColors.RowFill)
            .border(1.dp, MusicScreenColors.Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MusicScreenColors.Accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.PlaylistAdd,
                contentDescription = null,
                tint = MusicScreenColors.Accent,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            playlist.name,
            color = MusicScreenColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${playlist.songCount} songs",
            color = MusicScreenColors.TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Rename",
                    tint = MusicScreenColors.TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF8A80),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
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
    onEditMetadata: (MusicTrackEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tracks, key = { it.uri }) { track ->
            TrackRow(
                track = track,
                isLiked = track.uri in likedTrackUris,
                onPlay = { onPlay(track) },
                onAddToPlaylist = { onAddToPlaylist(track) },
                onToggleLike = { onToggleLike(track) },
                onDelete = { onDelete(track) },
                onEditMetadata = { onEditMetadata(track) },
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
    onEditMetadata: () -> Unit = {},
) {
    LibraryListCard(onClick = onPlay, onLongClick = onEditMetadata) {
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
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MusicScreenColors.RowFill)
            .border(1.dp, MusicScreenColors.Border, RoundedCornerShape(16.dp))
            .then(
                when {
                    onClick != null && onLongClick != null -> Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    onClick != null -> Modifier.clickable(onClick = onClick)
                    else -> Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        content()
    }
}

@Composable
private fun EditSongMetadataScreen(
    track: MusicTrackEntity,
    draft: SongMetadataDraft,
    onUpdateDraft: (SongMetadataDraft) -> Unit,
    onPickArtwork: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MusicScreenColors.BackgroundTop,
                        MusicScreenColors.BackgroundBottom
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MusicScreenColors.TextSecondary)
            }
            Button(onClick = onSave) {
                Text("Save")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(20.dp))
                .background(MusicScreenColors.PanelFill)
                .border(1.dp, MusicScreenColors.Border, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Edit Song Details",
                color = MusicScreenColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Long-press from Songs list to customize metadata.",
                color = MusicScreenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MusicAlbumArtwork(
                    artworkPath = draft.artworkUri?.toString() ?: track.artworkPath,
                    title = draft.title,
                    modifier = Modifier.size(92.dp),
                    cornerRadius = 14.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(onClick = onPickArtwork) {
                    Text("Change album art", color = MusicScreenColors.Accent)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            SongMetaField("Title", draft.title) { onUpdateDraft(draft.copy(title = it)) }
            SongMetaField("Artist", draft.artist) { onUpdateDraft(draft.copy(artist = it)) }
            SongMetaField("Album", draft.album) { onUpdateDraft(draft.copy(album = it)) }
            SongMetaField("Genre", draft.genre) { onUpdateDraft(draft.copy(genre = it)) }
            SongMetaField("Year", draft.yearText) { onUpdateDraft(draft.copy(yearText = it)) }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SongMetaField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
        year = year,
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
