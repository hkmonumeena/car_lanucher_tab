package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.music.MusicPlayerUiState
import com.ruchitech.carlanuchertab.music.MusicScanStatus
import com.ruchitech.carlanuchertab.music.MusicSettingsEntity
import com.ruchitech.carlanuchertab.music.MusicTrackEntity
import com.ruchitech.carlanuchertab.music.MusicViewModel
import androidx.media3.common.Player
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class MusicPlayerStyle {
    /** Full card — home screen inside [HomeGlassPanel]. */
    HomeGlass,
    Compact,
    Expanded,
}

private object MusicPalette {
    val CardTop = Color(0xFF101820)
    val CardBottom = Color(0xFF1A2836)
    val ArtBackdrop = Color(0xFF0A0F14)
    val Accent = Color(0xFF5CE1E6)
    val AccentDim = Color(0xFF3A9EA3)
    val TextPrimary = Color(0xFFF4F7FA)
    val TextSecondary = Color(0xB3F4F7FA)
    val TextMuted = Color(0x80F4F7FA)
    val GlassBorder = Color(0x24FFFFFF)
    val ControlFill = Color(0x18FFFFFF)
    val ControlFillActive = Color(0xFF2A3D52)
}

@Composable
fun MusicUi(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = hiltViewModel(),
    onOpenLibrary: () -> Unit = {},
) {
    val settings by viewModel.settings.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val likedTracks by viewModel.likedTracks.collectAsState()
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    val currentTrack = playerState.currentTrack
    val isCurrentTrackLiked = currentTrack != null && likedTracks.any { it.uri == currentTrack.uri }
    var showQueueSheet by remember { mutableStateOf(false) }

    MusicPlayerPanel(
        modifier = modifier,
        style = MusicPlayerStyle.HomeGlass,
        settings = settings,
        playerState = playerState,
        allowDelete = false,
        isCurrentTrackLiked = isCurrentTrackLiked,
        onTogglePlayback = viewModel::togglePlayback,
        onSeekTo = viewModel::seekTo,
        onSkipNext = viewModel::skipNext,
        onSkipPrevious = viewModel::skipPrevious,
        onDelete = {},
        onOpenLibrary = onOpenLibrary,
        onToggleLike = viewModel::toggleLikeCurrentTrack,
        onAddToPlaylist = { showAddToPlaylistDialog = true },
        onToggleShuffle = viewModel::toggleShuffle,
        onCycleRepeat = viewModel::cycleRepeatMode,
        onOpenQueue = { showQueueSheet = true },
    )

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

    if (showAddToPlaylistDialog && currentTrack != null) {
        MusicAddToPlaylistDialog(
            trackTitle = currentTrack.title,
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onSelectPlaylist = { playlist ->
                viewModel.addTrackToPlaylist(playlist.id, currentTrack.uri)
                showAddToPlaylistDialog = false
            },
            onCreatePlaylist = {
                showAddToPlaylistDialog = false
                showCreatePlaylistDialog = true
            }
        )
    }

    if (showCreatePlaylistDialog) {
        MusicTextInputDialog(
            title = "New playlist",
            initialValue = "",
            fieldLabel = "Name",
            confirmLabel = "Create",
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                showCreatePlaylistDialog = false
                viewModel.createPlaylist(name)
            }
        )
    }
}

@Composable
fun MusicPlayerPanel(
    modifier: Modifier = Modifier,
    style: MusicPlayerStyle = MusicPlayerStyle.Expanded,
    settings: MusicSettingsEntity,
    playerState: MusicPlayerUiState,
    allowDelete: Boolean,
    isCurrentTrackLiked: Boolean = false,
    onTogglePlayback: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDelete: () -> Unit,
    onOpenLibrary: () -> Unit,
    onToggleLike: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
) {
    val isHomeGlass = style == MusicPlayerStyle.HomeGlass
    val isCompact = style == MusicPlayerStyle.Compact || isHomeGlass
    val cardShape = RoundedCornerShape(if (isCompact) 22.dp else 24.dp)
    val cardPadding = when {
        isHomeGlass -> 14.dp
        style == MusicPlayerStyle.Compact -> 14.dp
        else -> 18.dp
    }

    val surfaceModifier = if (isHomeGlass) {
        modifier.fillMaxSize().padding(cardPadding)
    } else {
        modifier
            .fillMaxSize()
            .shadow(
                elevation = if (style == MusicPlayerStyle.Compact) 10.dp else 14.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(MusicPalette.CardTop, MusicPalette.CardBottom)
                )
            )
            .border(1.dp, MusicPalette.GlassBorder, cardShape)
            .padding(cardPadding)
    }

    Box(modifier = surfaceModifier) {
        when {
            settings.folderUri.isNullOrBlank() -> {
                EmptyMusicState(
                    style = if (isHomeGlass) MusicPlayerStyle.Compact else style,
                    title = "Add your music",
                    subtitle = "Choose a folder to scan songs, albums, and playlists for this launcher.",
                    actionLabel = "Open library",
                    onAction = onOpenLibrary
                )
            }

            settings.scanStatus == MusicScanStatus.SCANNING -> {
                EmptyMusicState(
                    style = if (isHomeGlass) MusicPlayerStyle.Compact else style,
                    title = "Scanning library",
                    subtitle = "Indexing tracks from your selected folder…",
                    actionLabel = "Open library",
                    onAction = onOpenLibrary
                )
            }

            playerState.currentTrack == null -> {
                EmptyMusicState(
                    style = if (isHomeGlass) MusicPlayerStyle.Compact else style,
                    title = "Nothing playing",
                    subtitle = settings.errorMessage ?: "Pick a song from the library to start playback.",
                    actionLabel = "Browse music",
                    onAction = onOpenLibrary
                )
            }

            else -> {
                val track = playerState.currentTrack!!
                var dragDistance by remember(track.uri) { mutableFloatStateOf(0f) }

                when (style) {
                    MusicPlayerStyle.HomeGlass,
                    MusicPlayerStyle.Compact -> CompactNowPlaying(
                        track = track,
                        playerState = playerState,
                        isLiked = isCurrentTrackLiked,
                        onOpenLibrary = onOpenLibrary,
                        onTogglePlayback = onTogglePlayback,
                        onSeekTo = onSeekTo,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onToggleLike = onToggleLike,
                        onAddToPlaylist = onAddToPlaylist,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat,
                        onOpenQueue = onOpenQueue,
                        dragModifier = Modifier.pointerInput(track.uri) {
                            detectHorizontalDragGestures(
                                onDragEnd = { dragDistance = 0f },
                                onDragCancel = { dragDistance = 0f }
                            ) { change, dragAmount ->
                                dragDistance += dragAmount
                                when {
                                    dragDistance >= 120f -> {
                                        onSkipPrevious()
                                        dragDistance = 0f
                                    }

                                    dragDistance <= -120f -> {
                                        onSkipNext()
                                        dragDistance = 0f
                                    }
                                }
                                change.consume()
                            }
                        }
                    )

                    MusicPlayerStyle.Expanded -> ExpandedNowPlaying(
                        track = track,
                        playerState = playerState,
                        allowDelete = allowDelete,
                        isLiked = isCurrentTrackLiked,
                        onDelete = onDelete,
                        onTogglePlayback = onTogglePlayback,
                        onSeekTo = onSeekTo,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious,
                        onToggleLike = onToggleLike,
                        onAddToPlaylist = onAddToPlaylist,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat,
                        onOpenQueue = onOpenQueue,
                        dragModifier = Modifier.pointerInput(track.uri) {
                            detectHorizontalDragGestures(
                                onDragEnd = { dragDistance = 0f },
                                onDragCancel = { dragDistance = 0f }
                            ) { change, dragAmount ->
                                dragDistance += dragAmount
                                when {
                                    dragDistance >= 120f -> {
                                        onSkipPrevious()
                                        dragDistance = 0f
                                    }

                                    dragDistance <= -120f -> {
                                        onSkipNext()
                                        dragDistance = 0f
                                    }
                                }
                                change.consume()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MusicAlbumArtwork(
    artworkPath: String?,
    title: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    pulseWhenPlaying: Boolean = false,
    isPlaying: Boolean = false,
) {
    val hasArtwork = !artworkPath.isNullOrBlank()
    val pulseScale = if (pulseWhenPlaying && isPlaying) {
        val transition = rememberInfiniteTransition(label = "albumPulse")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.035f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "albumScale"
        ).value
    } else {
        1f
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MusicPalette.ArtBackdrop)
            .border(1.dp, MusicPalette.GlassBorder, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = artworkPath?.let(::File),
            contentDescription = title,
            contentScale = if (hasArtwork) ContentScale.Crop else ContentScale.Fit,
            placeholder = androidx.compose.ui.res.painterResource(R.drawable.music),
            error = androidx.compose.ui.res.painterResource(R.drawable.music),
            modifier = Modifier
                .fillMaxSize()
                .padding(if (hasArtwork) 0.dp else 10.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        )
    }
}

@Composable
private fun CompactNowPlaying(
    track: MusicTrackEntity,
    playerState: MusicPlayerUiState,
    isLiked: Boolean,
    onOpenLibrary: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleLike: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
    dragModifier: Modifier,
) {
    HorizontalNowPlayingLayout(
        track = track,
        playerState = playerState,
        compact = true,
        isLiked = isLiked,
        showDelete = false,
        showLibrary = true,
        headerText = "NOW PLAYING",
        onOpenLibrary = onOpenLibrary,
        onDelete = {},
        onTogglePlayback = onTogglePlayback,
        onSeekTo = onSeekTo,
        onSkipNext = onSkipNext,
        onSkipPrevious = onSkipPrevious,
        onToggleLike = onToggleLike,
        onAddToPlaylist = onAddToPlaylist,
        onToggleShuffle = onToggleShuffle,
        onCycleRepeat = onCycleRepeat,
        onOpenQueue = onOpenQueue,
        dragModifier = dragModifier,
    )
}

@Composable
private fun ExpandedNowPlaying(
    track: MusicTrackEntity,
    playerState: MusicPlayerUiState,
    allowDelete: Boolean,
    isLiked: Boolean,
    onDelete: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleLike: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
    dragModifier: Modifier,
) {
    HorizontalNowPlayingLayout(
        track = track,
        playerState = playerState,
        compact = false,
        isLiked = isLiked,
        showDelete = allowDelete,
        showLibrary = false,
        headerText = null,
        onOpenLibrary = {},
        onDelete = onDelete,
        onTogglePlayback = onTogglePlayback,
        onSeekTo = onSeekTo,
        onSkipNext = onSkipNext,
        onSkipPrevious = onSkipPrevious,
        onToggleLike = onToggleLike,
        onAddToPlaylist = onAddToPlaylist,
        onToggleShuffle = onToggleShuffle,
        onCycleRepeat = onCycleRepeat,
        onOpenQueue = onOpenQueue,
        dragModifier = dragModifier,
    )
}

@Composable
private fun HorizontalNowPlayingLayout(
    track: MusicTrackEntity,
    playerState: MusicPlayerUiState,
    compact: Boolean,
    isLiked: Boolean,
    showDelete: Boolean,
    showLibrary: Boolean,
    headerText: String?,
    onOpenLibrary: () -> Unit,
    onDelete: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleLike: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
    dragModifier: Modifier,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(dragModifier)
    ) {
        val contentGap = if (compact) 14.dp else 18.dp
        val sectionGap = if (compact) 10.dp else 14.dp
        val artSize = if (compact) {
            minOf(maxHeight * 0.48f, maxWidth * 0.34f).coerceIn(112.dp, 154.dp)
        } else {
            minOf(maxHeight * 0.56f, maxWidth * 0.38f).coerceIn(176.dp, 256.dp)
        }
        val titleStyle = if (compact) {
            MaterialTheme.typography.titleLarge
        } else {
            MaterialTheme.typography.headlineSmall
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(sectionGap)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (headerText != null) {
                    Text(
                        text = headerText,
                        color = MusicPalette.Accent,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                PlayerTopActions(
                    isLiked = isLiked,
                    showDelete = showDelete,
                    showLibrary = showLibrary,
                    onAddToPlaylist = onAddToPlaylist,
                    onToggleLike = onToggleLike,
                    onDelete = onDelete,
                    onOpenLibrary = onOpenLibrary,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(contentGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicAlbumArtwork(
                    artworkPath = track.artworkPath,
                    title = track.title,
                    modifier = Modifier.size(artSize),
                    cornerRadius = if (compact) 18.dp else 22.dp,
                    pulseWhenPlaying = compact,
                    isPlaying = playerState.isPlaying
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
                ) {
                    TrackMetadata(
                        trackUri = track.uri,
                        title = track.title,
                        artist = track.artist,
                        album = track.album,
                        genre = track.genre,
                        titleStyle = titleStyle,
                        compact = compact,
                        modifier = Modifier.fillMaxWidth(),
                        centered = false
                    )
                }
            }

            PlaybackSlider(
                progressMs = playerState.progressMs,
                durationMs = playerState.durationMs,
                onPositionChange = onSeekTo,
                compact = compact,
                shuffleEnabled = playerState.shuffleEnabled,
                repeatMode = playerState.repeatMode,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                onOpenQueue = onOpenQueue,
            )

            PlayerControlsRow(
                isPlaying = playerState.isPlaying,
                onPrevious = onSkipPrevious,
                onTogglePlayback = onTogglePlayback,
                onNext = onSkipNext,
                playButtonSize = if (compact) 56.dp else 62.dp,
                sideButtonSize = if (compact) 40.dp else 48.dp,
                playIconSize = if (compact) 30.dp else 32.dp,
                sideIconSize = if (compact) 22.dp else 26.dp,
                buttonSpacing = if (compact) 14.dp else 24.dp
            )
        }
    }
}

@Composable
private fun InlinePlaybackTransport(
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val iconSize = if (compact) 18.dp else 20.dp
    val btnSize = if (compact) 32.dp else 36.dp
    Row(
        modifier = modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleShuffle, modifier = Modifier.size(btnSize)) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleEnabled) MusicPalette.Accent else MusicPalette.TextSecondary,
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(onClick = onCycleRepeat, modifier = Modifier.size(btnSize)) {
            val active = repeatMode != Player.REPEAT_MODE_OFF
            Icon(
                imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) {
                    Icons.Default.RepeatOne
                } else {
                    Icons.Default.Repeat
                },
                contentDescription = "Repeat",
                tint = if (active) MusicPalette.Accent else MusicPalette.TextSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(onClick = onOpenQueue, modifier = Modifier.size(btnSize)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Queue",
                tint = MusicPalette.TextSecondary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicQueueBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    playerState: MusicPlayerUiState,
    onCrossfadeChange: (Boolean) -> Unit,
    onMoveQueueItem: (from: Int, to: Int) -> Unit,
    onRemoveQueueItem: (index: Int) -> Unit,
    onClearQueue: () -> Unit,
    onPlayQueueIndex: (index: Int) -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val queue = playerState.currentQueue
    val cur = playerState.currentIndex

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MusicPalette.CardBottom,
        contentColor = MusicPalette.TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Queue",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicPalette.TextPrimary
                )
                TextButton(
                    onClick = onClearQueue,
                    enabled = queue.isNotEmpty()
                ) {
                    Text("Clear", color = MusicPalette.Accent)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Crossfade", color = MusicPalette.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = playerState.crossfadeMs > 0,
                    onCheckedChange = onCrossfadeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MusicPalette.Accent,
                        checkedTrackColor = MusicPalette.Accent.copy(alpha = 0.35f)
                    )
                )
            }
            HorizontalDivider(color = MusicPalette.GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
            if (queue.isEmpty()) {
                Text(
                    "No songs in the queue.",
                    color = MusicPalette.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (cur in queue.indices) {
                        item {
                            Text(
                                "Now playing",
                                color = MusicPalette.Accent,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        item {
                            QueueSheetTrackRow(
                                track = queue[cur],
                                index = cur,
                                isCurrent = true,
                                queueSize = queue.size,
                                onPlay = { onPlayQueueIndex(cur) },
                                onMoveUp = { onMoveQueueItem(cur, cur - 1) },
                                onMoveDown = { onMoveQueueItem(cur, cur + 1) },
                                onRemove = { onRemoveQueueItem(cur) },
                            )
                        }
                    }
                    val upNextStart = cur + 1
                    if (upNextStart < queue.size) {
                        item {
                            Text(
                                "Up next",
                                color = MusicPalette.Accent,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(
                            count = queue.size - upNextStart,
                            key = { i -> queue[upNextStart + i].uri }
                        ) { i ->
                            val idx = upNextStart + i
                            QueueSheetTrackRow(
                                track = queue[idx],
                                index = idx,
                                isCurrent = false,
                                queueSize = queue.size,
                                onPlay = { onPlayQueueIndex(idx) },
                                onMoveUp = { onMoveQueueItem(idx, idx - 1) },
                                onMoveDown = { onMoveQueueItem(idx, idx + 1) },
                                onRemove = { onRemoveQueueItem(idx) },
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QueueSheetTrackRow(
    track: MusicTrackEntity,
    index: Int,
    isCurrent: Boolean,
    queueSize: Int,
    onPlay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val bg = if (isCurrent) MusicPalette.ControlFillActive else MusicPalette.ControlFill
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, MusicPalette.GlassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onPlay)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = MusicPalette.TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artist,
                color = MusicPalette.TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onMoveUp,
            enabled = index > 0,
            modifier = Modifier.size(36.dp)
        ) {
            Text("↑", color = if (index > 0) MusicPalette.TextSecondary else MusicPalette.TextMuted)
        }
        IconButton(
            onClick = onMoveDown,
            enabled = index < queueSize - 1,
            modifier = Modifier.size(36.dp)
        ) {
            Text("↓", color = if (index < queueSize - 1) MusicPalette.TextSecondary else MusicPalette.TextMuted)
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Text("✕", color = Color(0xFFFF8A80))
        }
    }
}

@Composable
private fun PlayerTopActions(
    isLiked: Boolean,
    showDelete: Boolean,
    showLibrary: Boolean,
    onAddToPlaylist: () -> Unit,
    onToggleLike: () -> Unit,
    onDelete: () -> Unit,
    onOpenLibrary: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAddToPlaylist,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistAdd,
                contentDescription = "Add to playlist",
                tint = MusicPalette.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(
            onClick = onToggleLike,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked) MusicPalette.Accent else MusicPalette.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        if (showDelete) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete song",
                    tint = Color(0xFFFF8A80),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        if (showLibrary) {
            IconButton(
                onClick = onOpenLibrary,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Open library",
                    tint = MusicPalette.TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TrackMetadata(
    trackUri: String,
    title: String,
    artist: String,
    album: String,
    genre: String,
    titleStyle: androidx.compose.ui.text.TextStyle,
    compact: Boolean,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    val snapshot = TrackMetaSnapshot(
        uri = trackUri,
        title = title,
        artist = artist,
        album = album,
        genre = genre,
    )
    val metaEnter = fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
        slideInVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { full -> full / 12 }
    val metaExit = fadeOut(animationSpec = tween(280)) +
        slideOutVertically(animationSpec = tween(280)) { full -> -full / 12 }

    AnimatedContent(
        targetState = snapshot,
        transitionSpec = { metaEnter.togetherWith(metaExit) },
        modifier = modifier.fillMaxWidth(),
        label = "trackChange",
    ) { meta ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                text = meta.title,
                color = MusicPalette.TextPrimary,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (compact) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))
            Text(
                text = meta.artist,
                color = MusicPalette.TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            if (!compact) {
                Spacer(modifier = Modifier.height(6.dp))
                CyclingAlbumGenre(
                    album = meta.album,
                    genre = meta.genre,
                    trackUri = meta.uri,
                    centered = centered,
                )
            }
        }
    }
}

private data class TrackMetaSnapshot(
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
)

@Composable
private fun CyclingAlbumGenre(
    album: String,
    genre: String,
    trackUri: String,
    centered: Boolean,
) {
    var phase by remember(trackUri) { mutableIntStateOf(0) }
    LaunchedEffect(trackUri) {
        while (isActive) {
            delay(3200)
            phase = 1 - phase
        }
    }
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start
    val cycleEnter = fadeIn(animationSpec = tween(420, easing = FastOutSlowInEasing)) +
        slideInVertically(animationSpec = tween(420, easing = FastOutSlowInEasing)) { h -> (h * 0.1f).toInt().coerceAtLeast(1) }
    val cycleExit = fadeOut(animationSpec = tween(300)) +
        slideOutVertically(animationSpec = tween(300)) { h -> (-h * 0.1f).toInt().coerceAtMost(-1) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 26.dp),
        contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart
    ) {
        AnimatedContent(
            targetState = phase,
            transitionSpec = { cycleEnter.togetherWith(cycleExit) },
            label = "albumGenre",
        ) { p ->
            Text(
                text = if (p == 0) album else genre,
                color = MusicPalette.TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlayerControlsRow(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit,
    playButtonSize: Dp,
    sideButtonSize: Dp,
    playIconSize: Dp = 30.dp,
    sideIconSize: Dp = 26.dp,
    buttonSpacing: Dp = 24.dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MusicControlButton(
            onClick = onPrevious,
            size = sideButtonSize
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = MusicPalette.TextPrimary,
                modifier = Modifier.size(sideIconSize)
            )
        }

        Spacer(modifier = Modifier.width(buttonSpacing))

        MusicControlButton(
            onClick = onTogglePlayback,
            size = playButtonSize,
            primary = true
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MusicPalette.TextPrimary,
                modifier = Modifier.size(playIconSize)
            )
        }

        Spacer(modifier = Modifier.width(buttonSpacing))

        MusicControlButton(
            onClick = onNext,
            size = sideButtonSize
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = MusicPalette.TextPrimary,
                modifier = Modifier.size(sideIconSize)
            )
        }
    }
}

@Composable
private fun MusicControlButton(
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(
                if (primary) {
                    Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(MusicPalette.AccentDim, MusicPalette.Accent)
                        )
                    )
                } else {
                    Modifier
                        .background(MusicPalette.ControlFill)
                        .border(1.dp, MusicPalette.GlassBorder, shape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun EmptyMusicState(
    style: MusicPlayerStyle,
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
        Box(
            modifier = Modifier
                .size(if (style == MusicPlayerStyle.Compact) 72.dp else 96.dp)
                .clip(CircleShape)
                .background(MusicPalette.ControlFill)
                .border(1.dp, MusicPalette.GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = null,
                tint = MusicPalette.Accent,
                modifier = Modifier.size(if (style == MusicPlayerStyle.Compact) 36.dp else 44.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = MusicPalette.TextPrimary,
            style = if (style == MusicPlayerStyle.Compact) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = MusicPalette.TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onAction) {
            Text(actionLabel, color = MusicPalette.Accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PlaybackSlider(
    progressMs: Long,
    durationMs: Long,
    onPositionChange: (Long) -> Unit,
    compact: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
) {
    var sliderPosition by remember(progressMs, durationMs) {
        mutableFloatStateOf(progressMs.coerceAtMost(durationMs).toFloat())
    }
    var isDragging by remember { mutableStateOf(false) }

    val sliderColors = SliderDefaults.colors(
        thumbColor = MusicPalette.Accent,
        activeTrackColor = MusicPalette.Accent,
        inactiveTrackColor = Color.White.copy(alpha = 0.14f)
    )
    val timeStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = if (isDragging) sliderPosition else progressMs.coerceAtMost(durationMs).toFloat(),
            onValueChange = {
                isDragging = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isDragging = false
                onPositionChange(sliderPosition.toLong())
            },
            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = sliderColors
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(progressMs),
                color = MusicPalette.TextMuted,
                style = timeStyle,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            InlinePlaybackTransport(
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                onOpenQueue = onOpenQueue,
                compact = compact,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            Text(
                text = formatDuration(durationMs),
                color = MusicPalette.TextMuted,
                style = timeStyle,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0L) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
