package com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.palette.graphics.Palette
import com.idapgroup.snowfall.snowfall
import com.ruchitech.carlanuchertab.AppsActivity
import com.ruchitech.carlanuchertab.GpsActivity
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem
import com.ruchitech.carlanuchertab.clock.ShowAnalogClock
import com.ruchitech.carlanuchertab.helper.NavItem
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
import com.ruchitech.carlanuchertab.helper.isNotificationListenerEnabled
import com.ruchitech.carlanuchertab.helper.wallpapers
import com.ruchitech.carlanuchertab.roomdb.data.Dashboard
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import com.ruchitech.carlanuchertab.ui.composables.FuelLogDialog
import com.ruchitech.carlanuchertab.ui.composables.FuelLogsEntry
import com.ruchitech.carlanuchertab.ui.composables.FuelLogsList
import com.ruchitech.carlanuchertab.ui.composables.HomeBottomIcons
import com.ruchitech.carlanuchertab.ui.composables.Wallpaper
import com.ruchitech.carlanuchertab.ui.composables.WidgetsDropdownMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardHome(viewModel: DashboardViewModel) {
/*
    val context = LocalContext.current as MainActivity

    LauncherHomeScreen(
        wallpaper = viewModel.setWallpaper,
        onAddWidget = {
            */
/*       viewModel.launchWidgetPicker(launchWidget = {
                       context.pickWidget.launch(it)
                   })*//*

        },
        widgetItems = viewModel.widgetItems,
        appWidgetManager = viewModel.appWidgetManager,
        widgetHost = viewModel.appWidgetHost,
        onUpdate = { viewModel.saveWidgetItems(viewModel.widgetItems) },
        viewModel
    )
*/

}

// Non-composable function to extract dominant color
fun extractDominantColor(bitmap: Bitmap): Color? {
    return Palette.from(bitmap).generate().dominantSwatch?.rgb?.let { Color(it) }
}

// Helper extension for luminance check
val Color.luminance: Float
    get() = 0.2126f * red + 0.7152f * green + 0.0722f * blue

@Composable
fun NowPlayingWidget(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
   // val nowPlaying by viewModel.nowPlaying
/*    val isPlaying by viewModel.isPlaying
    val position by viewModel.playbackPosition
    val duration by viewModel.playbackDuration*/

    // State for slider while dragging
    var sliderPosition by remember { mutableStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
/*
    LaunchedEffect(position, duration) {
        if (!isUserSeeking && duration > 0L) {
            sliderPosition = position.toFloat() / duration
        }
    }*/
    val interactionSource = remember { MutableInteractionSource() }
    val infiniteTransition = rememberInfiniteTransition(label = "ZoomTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (true) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ZoomScale"
    )
/*    val dominantColor by remember(nowPlaying.artwork) {
        mutableStateOf(
            nowPlaying.artwork?.let { artwork ->
                extractDominantColor(artwork.asImageBitmap().asAndroidBitmap())
                    ?.let { color ->
                        // Convert to linear RGB space for accurate luminance calculation
                        val linearRed = if (color.red <= 0.04045f) {
                            color.red / 12.92f
                        } else {
                            ((color.red + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
                        }

                        val linearGreen = if (color.green <= 0.04045f) {
                            color.green / 12.92f
                        } else {
                            ((color.green + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
                        }

                        val linearBlue = if (color.blue <= 0.04045f) {
                            color.blue / 12.92f
                        } else {
                            ((color.blue + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
                        }

                        // Calculate perceptual luminance (CIE 1931 standard)
                        val luminance =
                            0.2126f * linearRed + 0.7152f * linearGreen + 0.0722f * linearBlue

                        // Adjust color based on luminance and saturation
                        if (luminance > 0.7f) {
                            // For bright colors, reduce brightness but maintain saturation
                            val hsv = FloatArray(3)
                            android.graphics.Color.RGBToHSV(
                                (color.red * 255).toInt(),
                                (color.green * 255).toInt(),
                                (color.blue * 255).toInt(),
                                hsv
                            )
                            hsv[2] = hsv[2] * 0.7f // Reduce value (brightness)
                            val adjustedColor = android.graphics.Color.HSVToColor(hsv)
                            Color(adjustedColor).copy(alpha = 0.8f)
                        } else if (luminance < 0.2f) {
                            // For very dark colors, lighten them slightly
                            val hsv = FloatArray(3)
                            android.graphics.Color.RGBToHSV(
                                (color.red * 255).toInt(),
                                (color.green * 255).toInt(),
                                (color.blue * 255).toInt(),
                                hsv
                            )
                            hsv[2] = hsv[2].coerceAtLeast(0.3f) // Ensure minimum brightness
                            val adjustedColor = android.graphics.Color.HSVToColor(hsv)
                            Color(adjustedColor).copy(alpha = 0.8f)
                        } else {
                            color.copy(alpha = 0.8f)
                        }
                    }
            } ?: Color(0xFF5D8BF4) // Fallback color
        )
    }
    if (nowPlaying.title == null && nowPlaying.artist == null) {
        return
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 10.dp)
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        dominantColor.copy(alpha = 0.80f),
                        dominantColor.copy(alpha = 0.40f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = dominantColor.copy(alpha = 0.20f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Glossy overlay effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.7f, size.height * 0.5f),
                    radius = size.width * 0.6f
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (dragAmount > 30) {
                        Log.e("ifudgfhjkl", "NowPlayingWidget: Previous")
                        viewModel.skipToPrevious()
                    } else if (dragAmount < -30) {
                        Log.e("ifudgfhjkl", "NowPlayingWidget: Next")
                        viewModel.skipToNext()
                    }
                    change.consume()
                }
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Artwork with reflection effect
            Box(
                modifier = modifier
                    .size(268.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = dominantColor.copy(alpha = 0.80f)
                    )
                    .background(Color.Black)
            ) {
                nowPlaying.artwork?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    dominantColor.copy(alpha = 0.85f),
                                    dominantColor.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.music),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        *//*           .background(
                                       color = Color(0x801E293B),
                                       shape = RoundedCornerShape(12.dp)
                                   )
                                   .border(
                                       width = 1.dp,
                                       color = dominantColor.copy(alpha = 0.2f),
                                       shape = RoundedCornerShape(12.dp)
                                   )*//*
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = nowPlaying.title ?: "Unknown Track",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 4f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = nowPlaying.artist ?: "Unknown Artist",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Unknown Album",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                if (duration > 0L) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        // White slider

                        ThinSlider(
                            value = sliderPosition.toFloat(),
                            onValueChange = { newValue ->
                                Log.e("fdklhfkdhnfld", "NowPlayingWidget: $newValue")
                                isUserSeeking = true
                                sliderPosition = newValue
                            },
                            valueRange = 0f..duration.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            thumbColor = Color.White,
                            trackColor = Color.White,
                            onValueChangeFinished = {
                                val seekMs =
                                    (sliderPosition * duration).toLong().coerceIn(0L, duration)
                                viewModel.seekTo(seekMs)
                                isUserSeeking = false
                            }
                        )


                        // Time labels below
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Current position (left)
                            Text(
                                text = formatTime(position),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Duration (right)
                            Text(
                                text = formatTime(duration),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }


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
                            .size(56.dp)
                            .shadow(
                                elevation = 0.dp,
                                shape = CircleShape,
                                ambientColor = dominantColor
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { viewModel.skipToPrevious() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_song),
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(180F)
                        )
                    }

                    Spacer(modifier = Modifier.width(30.dp))

                    // Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(dominantColor.copy(alpha = 0.2f))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    bounded = true,
                                    color = Color.White
                                )
                            ) { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isPlaying) {
                            Icon(
                                painter = painterResource(R.drawable.play),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(start = 5.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.pause),
                                contentDescription = "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }


                    }

                    Spacer(modifier = Modifier.width(30.dp))

                    // Next Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(
                                elevation = 0.dp,
                                shape = CircleShape,
                                ambientColor = dominantColor
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { viewModel.skipToNext() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_song),
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }


            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Controls with dynamic color accents
    }*/
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

// Helper for extracting dominant color
@Composable
fun rememberDominantColorState(): DominantColorState {
    val context = LocalContext.current
    return remember { DominantColorState(context) }
}

class DominantColorState(context: Context) {
    var color by mutableStateOf(Color.Transparent)

    fun updateColors(bitmap: ImageBitmap, colorTransform: (Color) -> Color = { it }) {
        val palette = Palette.from(bitmap.asAndroidBitmap()).generate()
        val dominantSwatch = palette.dominantSwatch
        color = dominantSwatch?.rgb?.let { Color(it) }?.let(colorTransform) ?: Color.Transparent
    }
}

fun Modifier.basicMarquee(
    iterations: Int = Int.MAX_VALUE,
    animationDuration: Int = 3000,
) = composed {
    var textWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }
    val scroll by remember { derivedStateOf { textWidth > containerWidth } }
    val offsetX by animateFloatAsState(
        targetValue = if (scroll) -textWidth.toFloat() else 0f, animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "marqueeAnimation"
    )

    Modifier
        .drawWithContent {
            // First draw the original content
            this.drawContent()

            if (scroll) {
                // Then draw the repeating content if needed
                translate(left = textWidth.toFloat() + 32.dp.toPx()) {
                    this@drawWithContent.drawContent()
                }
            }
        }
        .onSizeChanged { containerWidth = it.width }
        .graphicsLayer {
            if (scroll) {
                translationX = offsetX
            }
        }
        .onPlaced { layoutCoordinates ->
            textWidth = layoutCoordinates.size.width
        }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherHomeScreen(
    wallpaper: Int = R.drawable.launcher_bg7,
    onAddWidget: () -> Unit,
    widgetItems: SnapshotStateList<WidgetItem>,
    appWidgetManager: AppWidgetManager,
    widgetHost: AppWidgetHost,
    onUpdate: () -> Unit,
    viewModel: DashboardViewModel,
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showWallpaperSheet by remember { mutableStateOf(false) }
    var showFuelLogs by remember { mutableStateOf(false) }
    var showFuelDialog by remember { mutableStateOf(false) }
    var currentWallpaper by remember { mutableIntStateOf(wallpaper) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                    startY = 0f,
                    endY = 500f
                )
            )
            .then(
                if (true) Modifier.snowfall(density = 0.020, alpha = 0.5f)
                else Modifier
            )
    ) {
        Wallpaper(currentWallpaper, Modifier.align(Alignment.Center))
        FuelLogsEntry(modifier = Modifier.align(alignment = Alignment.BottomEnd), onTap = {
            showFuelLogs = false
            showFuelDialog = true
        }, onLongPress = {
            showFuelDialog = false
            showFuelLogs = true
        })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .align(alignment = Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.TopStart)
            ) {
                ShowAnalogClock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3F)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                ) {
                    NowPlayingWidget(viewModel)
                }
            }
        }




        /*        Box(modifier = Modifier.align(alignment = Alignment.BottomStart)) {
                    IconButton(onClick = { viewModel.voiceHelper.startListening() }) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Voice Command",
                            tint = Color.Yellow,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }*/

        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomStart)
                .padding(top = 0.dp, end = 15.dp)
        ) {
            IconButton(
                onClick = {
                    if (!isNotificationListenerEnabled(context)) {
                        val intent =
                            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        context.startActivity(intent)
                        return@IconButton
                    }
                    showMenu = true

                }, modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }

            WidgetsDropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                onMenuAction = { action ->
                    when (action) {
                        is WidgetMenuAction.AddWidget -> onAddWidget()
                        is WidgetMenuAction.RemoveAllWidgets -> {
                            widgetItems.clear()
                            viewModel.saveWidgetItems(emptyList())
                            //onUpdate()
                        }

                        is WidgetMenuAction.EditWidgets -> {
                            // viewModel.editWidgets = !viewModel.editWidgets
                        }

                        is WidgetMenuAction.Wallpapers -> {
                            showWallpaperSheet = true
                        }

                        is WidgetMenuAction.Fuel -> {
                            showFuelDialog = true
                        }

                        is WidgetMenuAction.Snowfall -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                val dashboard = viewModel.dashboardDao.getDashboard()
                                if (dashboard != null) {
                                    viewModel.dashboardDao.updateDashboard(dashboard.copy(isSnowfall = true))
                                } else {
                                    viewModel.dashboardDao.insertOrUpdateDashboard(
                                        Dashboard(
                                            isSnowfall = true
                                        )
                                    )
                                }
                            }
                        }

                        WidgetMenuAction.Fuels -> {

                        }
                    }
                })

            if (showFuelDialog) {
                FuelLogDialog(onDismiss = { showFuelDialog = false }, onSubmit = { newLog ->
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.dashboardDao.insertLog(newLog)
                        showFuelDialog = false
                    }
                })
            }

            if (showWallpaperSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showWallpaperSheet = false },
                    sheetState = rememberModalBottomSheetState(),
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF1E1E1E),  // Darker background for better contrast
                    /// windowInsets = WindowInsets(0)  // Remove system insets for full control
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Select Wallpaper",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White, fontWeight = FontWeight.SemiBold
                                )
                            )
                            IconButton(
                                onClick = { showWallpaperSheet = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Wallpaper Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(wallpapers) { wallpaper ->
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)  // Better aspect ratio for wallpapers
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2D2D2D))
                                        .clickable {
                                            currentWallpaper = wallpaper
                                            showWallpaperSheet = false
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val dashboard =
                                                    viewModel.dashboardDao.getDashboard()
                                                if (dashboard != null) {
                                                    viewModel.dashboardDao.updateDashboard(
                                                        dashboard.copy(wallpaperId = wallpaper)
                                                    )
                                                } else {
                                                    viewModel.dashboardDao.insertOrUpdateDashboard(
                                                        Dashboard(wallpaperId = wallpaper)
                                                    )
                                                }
                                            }
                                        }) {
                                    Image(
                                        painter = painterResource(wallpaper),
                                        contentDescription = "Wallpaper",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp)  // Small padding for visual breathing room
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }

        // Full-screen widget canvas
        MultiWidgetCanvas(
            widgetItems = widgetItems,
            widgetHost = widgetHost,
            appWidgetManager = appWidgetManager,
            onUpdate = onUpdate,
            viewModel
            // modifier = Modifier.padding(bottom = 80.dp) // Add padding to avoid overlap with bottom bar
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .wrapContentWidth()
                .height(100.dp)
                .background(Color(0x00000000)) // Semi-transparent black background
        ) {
            HomeBottomIcons(onClick = { bottomNavItem ->
                when (bottomNavItem) {
                    is NavItem.Map -> {
                        context.startActivity(
                            Intent(
                                context, GpsActivity::class.java
                            )
                        )
                    }

                    is NavItem.Radio -> {
                        val packageName = "com.tw.radio"
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            Toast.makeText(
                                context, "App not installed", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is NavItem.Music -> {
                        val packageName = "in.krosbits.musicolet"
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            Toast.makeText(
                                context, "App not installed", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is NavItem.Fuel -> {
                        showFuelDialog = true
                    }

                    is NavItem.AllApps -> {
                        context.startActivity(
                            Intent(
                                context, AppsActivity::class.java
                            )
                        )
                    }

                    NavItem.Settings -> TODO()
                }

            })
        }

        if (showFuelLogs) {
            Box(
                modifier = Modifier.align(alignment = Alignment.TopCenter),
                contentAlignment = Alignment.CenterStart
            ) {
                FuelLogs(onClose = {
                    showFuelLogs = false
                }, onAddNew = {
                    showFuelLogs = false
                    showFuelDialog = true
                }, viewModel)
            }

        }
    }
}

@Composable
private fun MultiWidgetCanvas(
    widgetItems: SnapshotStateList<WidgetItem>,
    widgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    onUpdate: () -> Unit,
    viewModel1: DashboardViewModel,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        for (item in widgetItems) {
            key(item.appWidgetId) {
                DraggableWidget(
                    item = item,
                    widgetHost = widgetHost,
                    appWidgetManager = appWidgetManager,
                    onPositionChanged = { newX, newY ->
                        val index = widgetItems.indexOfFirst { it.appWidgetId == item.appWidgetId }
                        if (index != -1) {
                            widgetItems[index] = item.copy(x = newX, y = newY)
                            onUpdate()
                        }
                    },
                    onSizeChanged = { newWidth, newHeight ->
                        val index = widgetItems.indexOfFirst { it.appWidgetId == item.appWidgetId }
                        if (index != -1) {
                            widgetItems[index] = item.copy(width = newWidth, height = newHeight)
                            onUpdate()
                        }
                    },
                    onLongPressToRemove = { itemToRemove ->
                        val index =
                            widgetItems.indexOfFirst { it.appWidgetId == itemToRemove.appWidgetId }
                        if (index != -1) {
                            widgetHost.deleteAppWidgetId(itemToRemove.appWidgetId)
                            widgetItems.removeAt(index)
                            onUpdate()
                        }
                    },
                    viewModel = viewModel1
                )
            }
        }
    }
}

@Composable
private fun DraggableWidget(
    item: WidgetItem,
    widgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    onPositionChanged: (Float, Float) -> Unit,
    onLongPressToRemove: (WidgetItem) -> Unit,
    onSizeChanged: (Int, Int) -> Unit,
    viewModel: DashboardViewModel,
) {
    val context = LocalContext.current
    var offsetX by remember { mutableStateOf(item.x) }
    var offsetY by remember { mutableStateOf(item.y) }
    var widgetWidth by remember { mutableStateOf(item.width) }
    var widgetHeight by remember { mutableStateOf(item.height) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Log.e("fdknfjdonfld", "DraggableWidget: worked press")
                        onLongPressToRemove(item)
                    })
            }) {
        AndroidView(
            factory = {
                val info = appWidgetManager.getAppWidgetInfo(item.appWidgetId)
                widgetHost.createView(context, item.appWidgetId, info).apply {
                    setAppWidget(item.appWidgetId, info)
                    layoutParams = FrameLayout.LayoutParams(widgetWidth, widgetHeight)
                }
            }, modifier = Modifier
                .size(widgetWidth.dp, widgetHeight.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        if (true) {
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            onPositionChanged(offsetX, offsetY)
                        }
                    }
                })
        if (true) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp)
            ) {
                Row {
                    // ❌ Remove Icon (Top End) - Styled
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove widget",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            // .align(Alignment.TopEnd)
                            .background(Color(0xFFD32F2F), shape = CircleShape) // deep red
                            .clickable {
                                CoroutineScope(Dispatchers.IO).launch {
                                    viewModel.deleteWidget(item.appWidgetId)
                                }
                            }
                            .padding(4.dp)
                            .shadow(4.dp, shape = CircleShape))

                    Spacer(modifier = Modifier.width(20.dp))

                    // ⬍ Resize Handle (Bottom End) - Styled Box
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            //  .align(Alignment.BottomEnd)
                            .background(Color(0xFF424242), shape = CircleShape) // dark gray
                            .shadow(4.dp, shape = CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    if (true) {
                                        change.consume()
                                        widgetWidth =
                                            (widgetWidth + dragAmount.x).toInt().coerceAtLeast(100)
                                        widgetHeight =
                                            (widgetHeight + dragAmount.y).toInt().coerceAtLeast(100)
                                        onSizeChanged(widgetWidth, widgetHeight)
                                    }
                                }
                            }) {
                        Icon(
                            imageVector = Icons.Default.Menu, // cross arrows icon
                            contentDescription = "Resize",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                        )
                    }
                }
            }
        }


    }
}

@Composable
fun FuelLogs(onClose: () -> Unit, onAddNew: () -> Unit, viewModel: DashboardViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        // Background dimming
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onClose() })

        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 16.dp, pressedElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E), contentColor = Color.White
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val isLoading = remember { mutableStateOf(true) }
                val fuelLogs = remember { mutableStateListOf<FuelLog>() }

                LaunchedEffect(Unit) {
                    val logs = viewModel.dashboardDao.getAllLogs()
                    fuelLogs.addAll(logs)
                    isLoading.value = false
                }

                if (isLoading.value) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF5D8BF4),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    FuelLogsList(fuelLogs = fuelLogs, onClose = onClose, onAddNew = onAddNew)
                }
            }
        }
    }
}
