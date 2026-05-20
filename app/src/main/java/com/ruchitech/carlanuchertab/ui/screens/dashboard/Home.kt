package com.ruchitech.carlanuchertab.ui.screens.dashboard

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.session.MediaSessionManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.idapgroup.snowfall.snowfall
import com.ruchitech.carlanuchertab.ClickedViewPrefs
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem
import com.ruchitech.carlanuchertab.clock.ShowAnalogClock
import com.ruchitech.carlanuchertab.helper.MusicNotificationListener
import com.ruchitech.carlanuchertab.helper.NavItem
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
import com.ruchitech.carlanuchertab.helper.isNotificationListenerEnabled
import com.ruchitech.carlanuchertab.rememberVehicleLocationState
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import com.ruchitech.carlanuchertab.roomdb.data.FuelQuickFillHints
import com.ruchitech.carlanuchertab.ui.composables.FuelLogDialog
import com.ruchitech.carlanuchertab.ui.composables.HomeBottomIcons
import com.ruchitech.carlanuchertab.ui.composables.HomeCinematicOverlay
import com.ruchitech.carlanuchertab.ui.composables.HomeConnectionBadge
import com.ruchitech.carlanuchertab.ui.composables.HomeDockPanel
import com.ruchitech.carlanuchertab.ui.composables.HomeGlassPanel
import com.ruchitech.carlanuchertab.ui.composables.ModalWallpaper
import com.ruchitech.carlanuchertab.ui.composables.MusicUi
import com.ruchitech.carlanuchertab.ui.composables.WidgetsDropdownMenu
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.DashboardViewModel
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.FuelLogs
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin



@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this entry?") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            })
    }
}


@Composable
fun HomeScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigated: (bottomNavItem: NavItem) -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val fuelLogs by viewModel.fuelLogs.collectAsState()
    val fuelQuickFillHints = remember(fuelLogs) {
        val last = fuelLogs.firstOrNull()
        FuelQuickFillHints(
            lastPricePerLiter = last?.fuelPrice,
            lastLiters = last?.liters,
        )
    }
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = remember { AppWidgetHost(context, viewModel.APPWIDGET_HOST_ID) }
    val locationState = rememberVehicleLocationState()
    var deleteDialog by remember { mutableStateOf(false) }
    var itemToDelete: FuelLog? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    //val kmhSpeed = speed * 3.6f // Convert m/s to km/h
    //var currentSpeed by remember { mutableStateOf(locationState.speed * 3.6f) } // Default dummy value
    val configureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.showWidget(viewModel.currentAppWidgetId, context)
        } else {
            val info = appWidgetManager.getAppWidgetInfo(viewModel.currentAppWidgetId)
            if (info?.configure != null) {
                viewModel.showWidget(viewModel.currentAppWidgetId, context)
            } else {
                appWidgetHost.deleteAppWidgetId(viewModel.currentAppWidgetId)
            }
        }
    }



    val pickWidgetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
            viewModel.currentAppWidgetId = appWidgetId
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            if (info?.configure != null) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = info.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                configureLauncher.launch(intent)
            } else {
                viewModel.showWidget(appWidgetId, context)
            }
        } else {
            if (viewModel.currentAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                appWidgetHost.deleteAppWidgetId(viewModel.currentAppWidgetId)
            }
        }
    }

    DisposableEffect(Unit) {
        appWidgetHost.startListening()
        onDispose {
            appWidgetHost.stopListening()
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .then(
                    if (uiState.isSnowfall) Modifier.snowfall(density = 0.040, alpha = 0.5f)
                    else Modifier
                )
        ) {
            // Ã°Å¸Å½Â¨ Wallpaper background from resource
            Image(
                painter = painterResource(id = uiState.wallpaperId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            HomeCinematicOverlay()

            if (uiState.addFuelLog) {
                FuelLogDialog(
                    onDismiss = { viewModel.hideAddFuelLogDialog() },
                    quickFillHints = fuelQuickFillHints,
                    onSubmit = { newLog ->
                        viewModel.insertFuelLog(newLog)
                    })
            }


            /*            Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleSettings() }) {
                                Image(
                                    painter = painterResource(R.drawable.ic_settings), contentDescription = null
                                )
                            }
                            WidgetsDropdownMenu(
                                expanded = uiState.showSettings,
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(alignment = Alignment.BottomEnd),
                                onDismissRequest = {
                                    viewModel.toggleSettings()
                                },
                                onMenuAction = { action ->
                                    if (action is WidgetMenuAction.AddWidget) {
                                        val appWidgetId = appWidgetHost.allocateAppWidgetId()
                                        viewModel.currentAppWidgetId = appWidgetId
                                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                        }
                                        pickWidgetLauncher.launch(intent)
                                        return@WidgetsDropdownMenu
                                    }
                                    viewModel.handleMenuAction(action,context)
                                })
                        }*/

            if (uiState.showWallpaper) {
                ModalWallpaper(
                    onDismiss = { viewModel.hideWallpaperModal() },
                    onWallpaperSet = { wallpaperId: Int ->
                        viewModel.setWallpaper(wallpaperId)
                    })
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.22f)
                        .fillMaxSize()
                ) {
                    if (uiState.serverStarted) {
                        HomeConnectionBadge(
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    HomeGlassPanel(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ShowAnalogClock(
                                modifier = Modifier.wrapContentSize(),
                                compact = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HomeDockPanel(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            HomeBottomIcons(
                                onClick = { bottomNavItem ->
                                    when (bottomNavItem) {
                                        NavItem.AllApps -> onNavigated(bottomNavItem)
                                        NavItem.Fuel -> viewModel.showFuelLogsModal()
                                        NavItem.Map -> {}
                                        NavItem.Music -> onNavigated(bottomNavItem)
                                        NavItem.Radio -> {
                                            val packageName = "com.tw.radio"
                                            val launchIntent =
                                                context.packageManager.getLaunchIntentForPackage(packageName)
                                            if (launchIntent != null) {
                                                context.startActivity(launchIntent)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "App not installed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        NavItem.Settings -> viewModel.toggleSettings()
                                    }
                                }
                            )
                            WidgetsDropdownMenu(
                                expanded = uiState.showSettings,
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.TopEnd),
                                onDismissRequest = { viewModel.toggleSettings() },
                                onMenuAction = { action ->
                                    if (action is WidgetMenuAction.AddWidget) {
                                        val appWidgetId = appWidgetHost.allocateAppWidgetId()
                                        viewModel.currentAppWidgetId = appWidgetId
                                        val intent =
                                            Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                                putExtra(
                                                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                                                    appWidgetId
                                                )
                                            }
                                        pickWidgetLauncher.launch(intent)
                                        return@WidgetsDropdownMenu
                                    }
                                    viewModel.handleMenuAction(action, context)
                                }
                            )
                        }
                    }
                }

                HomeGlassPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    MusicUi(
                        modifier = Modifier.fillMaxSize(),
                        onOpenLibrary = { onNavigated(NavItem.Music) }
                    )
                }
            }

            if (uiState.showFuelLogs) {
                Box(
                    modifier = Modifier.align(alignment = Alignment.TopCenter),
                    contentAlignment = Alignment.CenterStart
                ) {
                    FuelLogs(
                        fuelLogs = fuelLogs,
                        onClose = {
                            viewModel.hideFuelLogsModal()
                        },
                        onAddNew = {
                            viewModel.addFuelLog()
                        },
                        onDelete = {
                            itemToDelete = it
                            deleteDialog = true
                        },
                    )
                }

                DeleteConfirmationDialog(showDialog = deleteDialog, onConfirm = {
                    deleteDialog = false
                    itemToDelete?.let {
                        viewModel.deleteFuelLog(it)
                        itemToDelete = null
                    }
                }, onDismiss = {
                    deleteDialog = false
                })
            }

            MultiWidgetCanvas(
                widgetItems = uiState.widgetItems,
                widgetHost = appWidgetHost,
                appWidgetManager = appWidgetManager,
                isEditWidget = uiState.isEditMode,
                onUpdateWidget = { viewModel.updateWidgetItem(it) },
                onDeleteWidget = { viewModel.deleteWidget(it.appWidgetId) })
        }
    }
}


@Composable
private fun MultiWidgetCanvas(
    widgetItems: List<WidgetItem>,
    widgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    isEditWidget: Boolean,
    onUpdateWidget: (WidgetItem) -> Unit,
    onDeleteWidget: (WidgetItem) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        for (item in widgetItems) {
            key(item.appWidgetId) {
                DraggableWidget(
                    item = item,
                    widgetHost = widgetHost,
                    appWidgetManager = appWidgetManager,
                    onPositionChanged = { newX, newY ->
                        onUpdateWidget(item.copy(x = newX, y = newY))
                    },
                    onSizeChanged = { newWidth, newHeight ->
                        onUpdateWidget(item.copy(width = newWidth, height = newHeight))
                    },
                    onLongPressToRemove = {
                        widgetHost.deleteAppWidgetId(it.appWidgetId)
                        onDeleteWidget(it)
                    },
                    isEditWidget = isEditWidget,
                    onDeleteWidget = { id ->
                        widgetHost.deleteAppWidgetId(id)
                        onDeleteWidget(item)
                    })
            }
        }
    }
}

@Composable
private fun DraggableWidget(
    item: WidgetItem,
    widgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    isEditWidget: Boolean,
    onPositionChanged: (Float, Float) -> Unit,
    onSizeChanged: (Int, Int) -> Unit,
    onLongPressToRemove: (WidgetItem) -> Unit,
    onDeleteWidget: (Int) -> Unit,
) {
    val context = LocalContext.current

    // Ã°Å¸â€â€ž Track offset locally for immediate drag feedback
    var offsetX by remember(item.appWidgetId) { mutableFloatStateOf(item.x) }
    var offsetY by remember(item.appWidgetId) { mutableFloatStateOf(item.y) }

    // Ã°Å¸Â§Â  Sync offset when item is updated externally (e.g. from ViewModel)
    LaunchedEffect(item.x, item.y) {
        offsetX = item.x
        offsetY = item.y
    }

    // Ã°Å¸â€â€ž Track size locally for resize responsiveness
    var widgetWidth by remember(item.appWidgetId) { mutableIntStateOf(item.width) }
    var widgetHeight by remember(item.appWidgetId) { mutableIntStateOf(item.height) }

    LaunchedEffect(item.width, item.height) {
        widgetWidth = item.width
        widgetHeight = item.height
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .size(widgetWidth.dp, widgetHeight.dp)
            .pointerInput(isEditWidget) {
                detectTapGestures(
                    onLongPress = {
                        if (isEditWidget) {
                            onLongPressToRemove(item)
                        }
                    })
            }) {
        AndroidView(
            factory = {
                val info = appWidgetManager.getAppWidgetInfo(item.appWidgetId)
                widgetHost.createView(context, item.appWidgetId, info).apply {
                    setAppWidget(item.appWidgetId, info)
                    layoutParams = FrameLayout.LayoutParams(widgetWidth, widgetHeight)
                }
            },
            modifier = Modifier
                .size(widgetWidth.dp, widgetHeight.dp)
                .pointerInput(isEditWidget) {
                    if (isEditWidget) {
                        detectDragGestures(onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }, onDragEnd = {
                            onPositionChanged(offsetX, offsetY)
                        })
                    }
                })

        if (isEditWidget) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove widget",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFD32F2F), shape = CircleShape)
                            .clickable {
                                onDeleteWidget(item.appWidgetId)
                            }
                            .padding(4.dp)
                            .shadow(4.dp, shape = CircleShape))

                    Spacer(modifier = Modifier.width(20.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF424242), shape = CircleShape)
                            .shadow(4.dp, shape = CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    if (isEditWidget) {
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
                            imageVector = Icons.Default.Menu,
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
fun NowPlayingObserver(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.updateNowPlayingInfo()
            }
        }

        try {
            val filter = IntentFilter("now_playing_update")
            registerReceiver(
                context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED

            )

        } catch (e: Exception) {
            Log.e("fkgidbidfijgk", "Ã¢ÂÅ’ Failed to register music receiver: $e")
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

@Composable
fun NowPlayingInfo(viewModel: DashboardViewModel = hiltViewModel()) {
    val nowPlaying by viewModel.nowPlaying.collectAsState()

    nowPlaying?.let { info ->
        Column(modifier = Modifier.padding(16.dp)) {
            info.artwork?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
            }
            Text("Ã°Å¸Å½Âµ ${info.title ?: "Unknown"}", fontWeight = FontWeight.Bold)
            Text("Ã°Å¸â€˜Â¤ ${info.artist ?: "Unknown"}")
            Text("Ã°Å¸â€™Â¿ ${info.album ?: "Unknown"}")
            Text("Ã¢ÂÂ± ${info.position / 1000}s / ${info.duration / 1000}s")
            Text("Ã¢â€“Â¶Ã¯Â¸Â Playing: ${info.isPlaying}")
        }
    }
}
