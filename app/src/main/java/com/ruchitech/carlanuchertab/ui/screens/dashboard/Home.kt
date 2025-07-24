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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.hilt.navigation.compose.hiltViewModel
import com.idapgroup.snowfall.snowfall
import com.ruchitech.carlanuchertab.ClickedViewPrefs
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem
import com.ruchitech.carlanuchertab.clock.ShowAnalogClock
import com.ruchitech.carlanuchertab.helper.MusicNotificationListener
import com.ruchitech.carlanuchertab.helper.NavItem
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
import com.ruchitech.carlanuchertab.rememberVehicleLocationState
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import com.ruchitech.carlanuchertab.ui.composables.FuelLogDialog
import com.ruchitech.carlanuchertab.ui.composables.HomeBottomIcons
import com.ruchitech.carlanuchertab.ui.composables.ModalWallpaper
import com.ruchitech.carlanuchertab.ui.composables.MusicUi
import com.ruchitech.carlanuchertab.ui.composables.WidgetsDropdownMenu
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.DashboardViewModel
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.FuelLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun AnalogSpeedometer(
    currentSpeed: Int = 30, // Default dummy value (0-220 km/h)
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(300.dp)
            .height(300.dp), contentAlignment = Alignment.TopCenter
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
        ) {
            val center = Offset(size.width / 2, size.height * 0.9f)
            val radius = size.width * 0.45f

            // Draw speedometer arc
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 4f)
            )

            for (i in 0..220 step 20) {
                val angle = 150f + (i.toFloat() / 220f) * 240f
                val tickLength = if (i % 40 == 0) 20f else 10f
                val start = Offset(
                    center.x + radius * 0.9f * cos(angle * (PI / 180f)).toFloat(),
                    center.y + radius * 0.9f * sin(angle * (PI / 180f)).toFloat()
                )
                val end = Offset(
                    center.x + (radius * 0.9f - tickLength) * cos(angle * (PI / 180f)).toFloat(),
                    center.y + (radius * 0.9f - tickLength) * sin(angle * (PI / 180f)).toFloat()
                )

                drawLine(
                    color = Color.White, start = start, end = end, strokeWidth = 2f
                )

                // Add numbers for major ticks
                if (i % 40 == 0) {
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            i.toString(),
                            center.x + (radius * 0.7f) * cos(angle * (PI / 180f)).toFloat() - 10f,
                            center.y + (radius * 0.7f) * sin(angle * (PI / 180f)).toFloat() + 5f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 24f
                                textAlign = android.graphics.Paint.Align.CENTER
                            })
                    }
                }
            }

            // Draw needle
            val needleAngle = 150f + (currentSpeed.toFloat() / 220f) * 240f
            val needleLength = radius * 0.8f
            drawLine(
                color = Color.Red, start = center, end = Offset(
                    center.x + needleLength * cos(needleAngle * (PI / 180f)).toFloat(),
                    center.y + needleLength * sin(needleAngle * (PI / 180f)).toFloat()
                ), strokeWidth = 4f
            )

            // Draw center circle
            drawCircle(
                color = Color.Red, radius = 8f, center = center
            )
        }

        // Display current speed numerically

    }
}

@Composable
fun ClickedViewsScreen(context: Context = LocalContext.current) {
    var clickedViews by remember { mutableStateOf(ClickedViewPrefs.getClickedViews(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Clicked Views",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (clickedViews.isEmpty()) {
            Text("No views clicked yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(clickedViews) { view ->
                    Text(
                        text = view,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .background(Color.LightGray)
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                clickedViews = ClickedViewPrefs.getClickedViews(context)
            }) {
                Text("Refresh")
            }

            Button(onClick = {
                ClickedViewPrefs.clearClickedViews(context)
                clickedViews = emptyList()
            }) {
                Text("Clear Views")
            }
        }
    }
}

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
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = remember { AppWidgetHost(context, viewModel.APPWIDGET_HOST_ID) }
    val locationState = rememberVehicleLocationState()
    var deleteDialog by remember { mutableStateOf(false) }
    var itemToDelete: FuelLog? by remember { mutableStateOf(null) }
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
        val mediaSessionManager =
            context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val componentName = ComponentName(context, MusicNotificationListener::class.java)

        // Create the listener as a separate variable so we can properly remove it
        val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            Log.d("MediaSession", "🟢 Session changed: ${controllers?.map { it.packageName }}")
            controllers?.firstOrNull { it.packageName == "in.krosbits.musicolet" }
                ?.let { viewModel.updateMusicoletController(it) }
        }

        // Register the listener with a handler
        mediaSessionManager.addOnActiveSessionsChangedListener(
            sessionListener, componentName, Handler(Looper.getMainLooper()) // Explicit handler
        )

        // Get initial sessions (with try-catch for security exception)
        val currentSessions = try {
            mediaSessionManager.getActiveSessions(componentName)
        } catch (e: SecurityException) {
            Log.w("MediaSession", "Permission denied for getActiveSessions", e)
            emptyList()
        }

        Log.d("MediaSession", "🔵 Current sessions: ${currentSessions.map { it.packageName }}")
        currentSessions.firstOrNull { it.packageName == "in.krosbits.musicolet" }
            ?.let { viewModel.updateMusicoletController(it) }

        onDispose {
            appWidgetHost.stopListening()
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener)
        }
    }
    NowPlayingObserver(viewModel)
    if (uiState.showPairedDevices) {
        Dialog(onDismissRequest = {
            viewModel.hidePairedDevicesModal()
        }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column {
                    // Header with title and close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Devices",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { viewModel.hidePairedDevicesModal() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    Divider()

                    // Devices list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(viewModel.pairedDevices) { device ->
                            Text(
                                text = "${device.name} (${device.address})",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.connect(device)
                                        viewModel.hidePairedDevicesModal()
                                    }
                                    .padding(16.dp)
                            )
                            Divider()
                        }
                    }
                }
            }
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
            // 🎨 Wallpaper background from resource
            if (uiState.wallpaperId != 0) {
                Image(
                    painter = painterResource(id = R.drawable.launcher_bg10),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            if (uiState.addFuelLog) {
                FuelLogDialog(
                    onDismiss = { viewModel.hideAddFuelLogDialog() },
                    onSubmit = { newLog ->
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.dashboardDao.insertLog(newLog)
                        }
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1.5F)
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(Color(0x00000000))
                            .align(alignment = Alignment.BottomCenter),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(alignment = Alignment.CenterEnd)
                                .padding(bottom = 40.dp)
                        ) {
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
                                        val intent =
                                            Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                                putExtra(
                                                    AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId
                                                )
                                            }
                                        pickWidgetLauncher.launch(intent)
                                        return@WidgetsDropdownMenu
                                    }
                                    viewModel.handleMenuAction(action, context)
                                })
                        }
                        HomeBottomIcons(onClick = { bottomNavItem ->
                            when (bottomNavItem) {
                                NavItem.AllApps -> {

                                }

                                NavItem.Fuel -> {
                                    viewModel.showFuelLogsModal()
                                }

                                NavItem.Map -> {}
                                NavItem.Music -> {
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

                                NavItem.Radio -> {
                                    val packageName = "com.tw.radio"
                                    val launchIntent =
                                        context.packageManager.getLaunchIntentForPackage(packageName)
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    } else {
                                        onNavigated(bottomNavItem)
                                        Toast.makeText(
                                            context, "App not installed", Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                NavItem.Settings -> {
                                    viewModel.toggleSettings()
                                }

                                NavItem.Client -> {
                                    onNavigated(bottomNavItem)
                                }

                                NavItem.Server -> {
                                    onNavigated(bottomNavItem)
                                }
                            }
                        })

                    }
                    ShowAnalogClock(
                        modifier = Modifier
                            .align(alignment = Alignment.TopCenter)
                            .wrapContentSize()
                    )


                    if (uiState.serverStarted) {
                        Image(
                            modifier = Modifier.padding(10.dp).size(25.dp),
                            painter = painterResource(R.drawable.connected),
                            contentDescription = null
                        )
                    }

               /*     Text(
                        text = "${(locationState.speed * 3.6f).toInt()} km/h",
                        color = Color.White,
                        fontSize = 32.sp,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 10.dp)
                    )*/
                }

                Box(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxSize()
                ) {
                    MusicUi(viewModel)
                }/*                Box(
                                    modifier = Modifier.fillMaxSize(),
                                ) {

                                    Box(modifier = Modifier.size(260.dp)){
                                        AnalogSpeedometer(currentSpeed = (locationState.speed * 3.6f).toInt())
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "${(locationState.speed * 3.6f).toInt()} km/h",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .align(alignment = Alignment.BottomCenter)
                                            .fillMaxHeight()
                                            .background(Color(0x00000000)) // Semi-transparent black background
                                    ) {

                                    }
                                }*//*Box(modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, White.copy(alpha = 0.2F), shape = RoundedCornerShape(10.dp))
                    .weight(1F)) {

                }*/
            }

            if (uiState.showFuelLogs) {
                Box(
                    modifier = Modifier.align(alignment = Alignment.TopCenter),
                    contentAlignment = Alignment.CenterStart
                ) {
                    FuelLogs(onClose = {
                        viewModel.hideFuelLogsModal()
                    }, onAddNew = {
                        viewModel.addFuelLog()
                    }, viewModel, onDelete = {
                        itemToDelete = it
                        deleteDialog = true
                    })
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

    // 🔄 Track offset locally for immediate drag feedback
    var offsetX by remember(item.appWidgetId) { mutableFloatStateOf(item.x) }
    var offsetY by remember(item.appWidgetId) { mutableFloatStateOf(item.y) }

    // 🧠 Sync offset when item is updated externally (e.g. from ViewModel)
    LaunchedEffect(item.x, item.y) {
        offsetX = item.x
        offsetY = item.y
    }

    // 🔄 Track size locally for resize responsiveness
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
            Log.e("fkgidbidfijgk", "❌ Failed to register music receiver: $e")
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
            Text("🎵 ${info.title ?: "Unknown"}", fontWeight = FontWeight.Bold)
            Text("👤 ${info.artist ?: "Unknown"}")
            Text("💿 ${info.album ?: "Unknown"}")
            Text("⏱ ${info.position / 1000}s / ${info.duration / 1000}s")
            Text("▶️ Playing: ${info.isPlaying}")
        }
    }
}
