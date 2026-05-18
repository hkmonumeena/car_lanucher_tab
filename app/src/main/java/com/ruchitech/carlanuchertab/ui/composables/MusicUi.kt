package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.music.MusicPlayerUiState
import com.ruchitech.carlanuchertab.music.MusicScanStatus
import com.ruchitech.carlanuchertab.music.MusicSettingsEntity
import com.ruchitech.carlanuchertab.music.MusicViewModel
import java.io.File

@Composable
fun MusicUi(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = hiltViewModel(),
    onOpenLibrary: () -> Unit = {},
) {
    val settings by viewModel.settings.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    MusicPlayerPanel(
        modifier = modifier,
        settings = settings,
        playerState = playerState,
        allowDelete = false,
        onTogglePlayback = viewModel::togglePlayback,
        onSeekTo = viewModel::seekTo,
        onSkipNext = viewModel::skipNext,
        onSkipPrevious = viewModel::skipPrevious,
        onDelete = {},
        onOpenLibrary = onOpenLibrary
    )
}

@Composable
fun MusicPlayerPanel(
    modifier: Modifier = Modifier,
    settings: MusicSettingsEntity,
    playerState: MusicPlayerUiState,
    allowDelete: Boolean,
    onTogglePlayback: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDelete: () -> Unit,
    onOpenLibrary: () -> Unit,
) {
    val currentTrack = playerState.currentTrack
    val premiumBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF0E1620), Color(0xFF182635), Color(0xFF223347))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(premiumBrush)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        when {
            settings.folderUri.isNullOrBlank() -> {
                EmptyMusicState(
                    title = "Choose your music folder",
                    subtitle = "Pick one folder and this launcher will scan songs, albums, genres, and playlists.",
                    actionLabel = "Open Music",
                    onAction = onOpenLibrary
                )
            }

            settings.scanStatus == MusicScanStatus.SCANNING -> {
                EmptyMusicState(
                    title = "Scanning music",
                    subtitle = "Building your local library from the selected folder.",
                    actionLabel = "Open Music",
                    onAction = onOpenLibrary
                )
            }

            currentTrack == null -> {
                EmptyMusicState(
                    title = "Ready to play",
                    subtitle = settings.errorMessage ?: "Open the music screen and start any song to show it here.",
                    actionLabel = "Open Music",
                    onAction = onOpenLibrary
                )
            }

            else -> {
                var dragDistance by remember(currentTrack.uri) { mutableFloatStateOf(0f) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentTrack.uri) {
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
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    if (allowDelete) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = onDelete) {
                                Text("Delete Song")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    AsyncImage(
                        model = currentTrack.artworkPath?.let(::File),
                        contentDescription = currentTrack.title,
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.music),
                        placeholder = painterResource(R.drawable.music),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentTrack.title,
                        color = White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentTrack.artist,
                        color = White.copy(alpha = 0.80f),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTrack.album,
                        color = White.copy(alpha = 0.58f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    PlaybackSlider(
                        progressMs = playerState.progressMs,
                        durationMs = playerState.durationMs,
                        onPositionChange = onSeekTo
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayerActionChip(label = "Prev", onClick = onSkipPrevious)
                        Spacer(modifier = Modifier.width(18.dp))
                        PlayerActionChip(
                            label = if (playerState.isPlaying) "Pause" else "Play",
                            emphasized = true,
                            onClick = onTogglePlayback
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        PlayerActionChip(label = "Next", onClick = onSkipNext)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMusicState(
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
            text = title,
            color = White,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = subtitle,
            color = White.copy(alpha = 0.74f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun PlayerActionChip(
    label: String,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (emphasized) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f)
            )
            .border(1.dp, White.copy(alpha = if (emphasized) 0.30f else 0.12f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PlaybackSlider(
    progressMs: Long,
    durationMs: Long,
    onPositionChange: (Long) -> Unit,
) {
    var sliderPosition by remember(progressMs, durationMs) {
        mutableFloatStateOf(progressMs.coerceAtMost(durationMs).toFloat())
    }
    var isDragging by remember { mutableStateOf(false) }

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
            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDuration(progressMs), color = White.copy(alpha = 0.72f))
            Text(formatDuration(durationMs), color = White.copy(alpha = 0.72f))
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0L) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
