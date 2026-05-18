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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import java.io.File

enum class MusicPlayerStyle {
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

    MusicPlayerPanel(
        modifier = modifier,
        style = MusicPlayerStyle.Compact,
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
        onAddToPlaylist = { showAddToPlaylistDialog = true }
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
) {
    val cardShape = RoundedCornerShape(if (style == MusicPlayerStyle.Compact) 22.dp else 24.dp)
    val cardPadding = if (style == MusicPlayerStyle.Compact) 14.dp else 18.dp

    Box(
        modifier = modifier
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
    ) {
        when {
            settings.folderUri.isNullOrBlank() -> {
                EmptyMusicState(
                    style = style,
                    title = "Add your music",
                    subtitle = "Choose a folder to scan songs, albums, and playlists for this launcher.",
                    actionLabel = "Open library",
                    onAction = onOpenLibrary
                )
            }

            settings.scanStatus == MusicScanStatus.SCANNING -> {
                EmptyMusicState(
                    style = style,
                    title = "Scanning library",
                    subtitle = "Indexing tracks from your selected folder…",
                    actionLabel = "Open library",
                    onAction = onOpenLibrary
                )
            }

            playerState.currentTrack == null -> {
                EmptyMusicState(
                    style = style,
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
            contentScale = ContentScale.Fit,
            placeholder = androidx.compose.ui.res.painterResource(R.drawable.music),
            error = androidx.compose.ui.res.painterResource(R.drawable.music),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
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
    dragModifier: Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(dragModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NOW PLAYING",
                color = MusicPalette.Accent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.weight(1f)
            )
            PlayerTopActions(
                isLiked = isLiked,
                showDelete = false,
                onAddToPlaylist = onAddToPlaylist,
                onToggleLike = onToggleLike,
                onDelete = {},
                onOpenLibrary = onOpenLibrary,
                showLibrary = true
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val artSize = minOf(maxWidth, maxHeight).coerceAtMost(maxWidth)
            MusicAlbumArtwork(
                artworkPath = track.artworkPath,
                title = track.title,
                modifier = Modifier
                    .size(artSize)
                    .align(Alignment.Center),
                cornerRadius = 18.dp,
                pulseWhenPlaying = true,
                isPlaying = playerState.isPlaying
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TrackMetadata(
            title = track.title,
            artist = track.artist,
            album = track.album,
            genre = track.genre,
            titleStyle = MaterialTheme.typography.titleLarge,
            compact = false,
            modifier = Modifier.fillMaxWidth(),
            centered = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        PlaybackSlider(
            progressMs = playerState.progressMs,
            durationMs = playerState.durationMs,
            onPositionChange = onSeekTo,
            compact = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        PlayerControlsRow(
            isPlaying = playerState.isPlaying,
            onPrevious = onSkipPrevious,
            onTogglePlayback = onTogglePlayback,
            onNext = onSkipNext,
            playButtonSize = 68.dp,
            sideButtonSize = 46.dp,
            playIconSize = 34.dp,
            sideIconSize = 26.dp
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
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
    dragModifier: Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(dragModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerTopActions(
                isLiked = isLiked,
                showDelete = allowDelete,
                onAddToPlaylist = onAddToPlaylist,
                onToggleLike = onToggleLike,
                onDelete = onDelete,
                onOpenLibrary = {},
                showLibrary = false
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val artSize = minOf(maxWidth, maxHeight).coerceAtMost(280.dp)
            MusicAlbumArtwork(
                artworkPath = track.artworkPath,
                title = track.title,
                modifier = Modifier
                    .size(artSize)
                    .align(Alignment.Center),
                cornerRadius = 20.dp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TrackMetadata(
            title = track.title,
            artist = track.artist,
            album = track.album,
            genre = track.genre,
            titleStyle = MaterialTheme.typography.headlineSmall,
            compact = false,
            modifier = Modifier.fillMaxWidth(),
            centered = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        PlaybackSlider(
            progressMs = playerState.progressMs,
            durationMs = playerState.durationMs,
            onPositionChange = onSeekTo,
            compact = false
        )

        Spacer(modifier = Modifier.height(14.dp))

        PlayerControlsRow(
            isPlaying = playerState.isPlaying,
            onPrevious = onSkipPrevious,
            onTogglePlayback = onTogglePlayback,
            onNext = onSkipNext,
            playButtonSize = 62.dp,
            sideButtonSize = 48.dp,
            playIconSize = 32.dp,
            sideIconSize = 26.dp
        )
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
    title: String,
    artist: String,
    album: String,
    genre: String,
    titleStyle: androidx.compose.ui.text.TextStyle,
    compact: Boolean,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = title,
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
            text = artist,
            color = MusicPalette.TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        if (!compact) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = album,
                color = MusicPalette.TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = genre,
                color = MusicPalette.TextMuted.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
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

        Spacer(modifier = Modifier.width(24.dp))

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

        Spacer(modifier = Modifier.width(24.dp))

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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(progressMs),
                color = MusicPalette.TextMuted,
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
            )
            Text(
                text = formatDuration(durationMs),
                color = MusicPalette.TextMuted,
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
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
