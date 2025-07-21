package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.DashboardViewModel
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.MusicoletNowPlaying
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSlider(
    nowPlaying: MusicoletNowPlaying,
    onPositionChange: (Long) -> Unit,
) {
    var sliderPosition by remember(nowPlaying.position) {
        mutableFloatStateOf(nowPlaying.position.toFloat())
    }
    val interactionSource = remember { MutableInteractionSource() }
    var isSliderDragging by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.8F)
            .padding(horizontal = 0.dp)
    ) {
        Slider(
            value = if (isSliderDragging) sliderPosition else nowPlaying.position.toFloat(),
            onValueChange = { newValue ->
                isSliderDragging = true
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                isSliderDragging = false
                onPositionChange(sliderPosition.toLong())
            },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = SliderDefaults.colors(thumbColor = White),
                    modifier = Modifier.size(10.dp) // Smaller thumb
                )
            },
            track = { sliderPositions ->

                SliderDefaults.Track(
                    colors = SliderDefaults.colors(
                        activeTrackColor = White, inactiveTrackColor = White.copy(alpha = 0.5f)
                    ), sliderState = sliderPositions, modifier = Modifier.height(3.dp) // Thin track
                )
            },
            valueRange = 0f..nowPlaying.duration.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration2(nowPlaying.position), color = White
            )
            Text(
                text = formatDuration2(nowPlaying.duration), color = White
            )
        }
    }
}

// Helper function to format milliseconds to MM:SS
fun formatDuration2(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}


@Composable
fun MusicUi(viewModel: DashboardViewModel) {
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "ZoomTransition")
    val interactionSource = remember { MutableInteractionSource() }
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (nowPlaying?.isPlaying == true) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ZoomScale"
    )

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateNowPlayingInfo()
            delay(1000)
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
        // .border(0.dp, White.copy(alpha = 0.2F), shape = RoundedCornerShape(10.dp)),
        , contentAlignment = Alignment.TopCenter

    ) {
        // Dimmed background artwork
        /* nowPlaying?.artwork?.let { artwork ->
             Image(
                 bitmap = artwork.asImageBitmap(),
                 contentDescription = null,
                 contentScale = ContentScale.Crop,
                 modifier = Modifier.fillMaxSize()
                     .clip(RoundedCornerShape(10.dp)) // This must come BEFORE blur
                     .border(1.dp, White.copy(alpha = 0.2F), shape = RoundedCornerShape(10.dp))
                     .alpha(0.8f) // Adjust opacity here (0.2f = 20% opacity)
                     .blur(radius = 12.dp) // Add blur effect
             )
         }*/
        nowPlaying?.let { music ->
            Column(
                modifier = Modifier
                    .fillMaxSize()/*.height(300.dp)*/.pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            if (dragAmount > 30) {
                                viewModel.previous()
                            } else if (dragAmount < -30) {
                                viewModel.next()
                            }
                            change.consume()
                        }
                    }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Transparent)
                        .weight(1.3F)
                        .padding(horizontal = 100.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = White.copy(alpha = 0.80f)
                        )
                        .background(Color.Transparent)
                        .clickable(onClick = {
                            viewModel.playPause()
                        })
                ) {
                    music.artwork?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Album Art",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        White.copy(alpha = 0.85f), White.copy(alpha = 0.6f)
                                    )
                                )
                            ), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.music),
                            contentDescription = null,
                            tint = White.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1F)
                        .background(color = Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = music.title ?: "Unknown Track",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = White, fontWeight = FontWeight.Bold, shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = music.artist ?: "Unknown Artist",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = White.copy(alpha = 0.8f), shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = music.album ?: "Unknown Album",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = White.copy(alpha = 0.6f)
                        )
                    )
                    PlaybackSlider(
                        nowPlaying = music, onPositionChange = { newPosition ->
                            viewModel.seekTo(newPosition)
                        })
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(60.dp)
                                .shadow(
                                    elevation = 0.dp, shape = CircleShape, ambientColor = White
                                )
                                .border(
                                    width = 2.dp, color = White, shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable { viewModel.previous() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.skip_song),
                                contentDescription = "Previous",
                                tint = White,
                                modifier = Modifier
                                    .size(28.dp)
                                    .rotate(180F)
                            )
                        }

                        Spacer(modifier = Modifier.width(38.dp))

                        // Play/Pause Button
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(80.dp)
                                .border(
                                    width = 2.dp, color = White, shape = CircleShape
                                )
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.2f))
                                .clickable(
                                    interactionSource = interactionSource, indication = ripple(
                                        bounded = true, color = White
                                    )
                                ) { viewModel.playPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!music.isPlaying) {
                                Icon(
                                    painter = painterResource(R.drawable.play),
                                    contentDescription = "Play",
                                    tint = White,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(start = 5.dp)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.pause),
                                    contentDescription = "Pause",
                                    tint = White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(38.dp))
                        // Next Button
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(60.dp)
                                .shadow(
                                    elevation = 0.dp, shape = CircleShape, ambientColor = White
                                )
                                .border(
                                    width = 2.dp, color = White, shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable { viewModel.next() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.skip_song),
                                contentDescription = "Next",
                                tint = White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

            }
        }
    }


}