package com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard_home

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.idapgroup.snowfall.snowfall
import com.ruchitech.carlanuchertab.AppsActivity
import com.ruchitech.carlanuchertab.GpsActivity
import com.ruchitech.carlanuchertab.MainActivity
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem
import com.ruchitech.carlanuchertab.clock.ShowAnalogClock
import com.ruchitech.carlanuchertab.helper.BottomNavItem
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
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
    val context = LocalContext.current as MainActivity

    LauncherHomeScreen(
        wallpaper = viewModel.setWallpaper,
        onAddWidget = {
            viewModel.launchWidgetPicker(launchWidget = {
                context.pickWidget.launch(it)
            })
        },
        widgetItems = viewModel.widgetItems,
        appWidgetManager = viewModel.appWidgetManager,
        widgetHost = viewModel.appWidgetHost,
        onUpdate = { viewModel.saveWidgetItems(viewModel.widgetItems) },
        viewModel
    )

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
     /*       .then(
                if (viewModel.isSnowfalll) Modifier.snowfall(density = 0.020, alpha = 0.5f)
                else Modifier
            )*/
    ) {
        Wallpaper(currentWallpaper, Modifier.align(Alignment.Center))
        FuelLogsEntry(modifier = Modifier.align(alignment = Alignment.CenterEnd), onTap = {
            showFuelLogs = false
            showFuelDialog = true
        }, onLongPress = {
            showFuelDialog = false
            showFuelLogs = true
        })

        ShowAnalogClock(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .padding(start = 50.dp, top = 30.dp)
        )

        Box(modifier = Modifier.align(alignment = Alignment.BottomStart)){
            IconButton(onClick = { viewModel.voiceHelper.startListening() }) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Voice Command",
                    tint = Color.Yellow,
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(top = 0.dp, end = 15.dp)
        ) {
            IconButton(
                onClick = {
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
                            viewModel.editWidgets = !viewModel.editWidgets
                        }

                        is WidgetMenuAction.Wallpapers -> {
                            showWallpaperSheet = true
                        }

                        is WidgetMenuAction.Fuel -> {
                            showFuelDialog = true
                        }

                        is WidgetMenuAction.Snowfall -> {
                            viewModel.isSnowfalll = !viewModel.isSnowfalll
                            CoroutineScope(Dispatchers.IO).launch {
                                val dashboard = viewModel.dashboardDao.getDashboard()
                                if (dashboard != null) {
                                    viewModel.dashboardDao.updateDashboard(dashboard.copy(isSnowfall = viewModel.isSnowfalll))
                                } else {
                                    viewModel.dashboardDao.insertOrUpdateDashboard(
                                        Dashboard(
                                            isSnowfall = viewModel.isSnowfalll
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
                    is BottomNavItem.Map -> {
                        context.startActivity(
                            Intent(
                                context, GpsActivity::class.java
                            )
                        )
                    }
                    is BottomNavItem.Radio -> Unit
                    is BottomNavItem.Music -> {
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

                    is BottomNavItem.AllApps -> {
                        context.startActivity(
                            Intent(
                                context, AppsActivity::class.java
                            )
                        )
                    }
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
                }, viewModel)
            }

        }
    }
}

@Composable
fun MultiWidgetCanvas(
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
                        val index =
                            widgetItems.indexOfFirst { it.appWidgetId == item.appWidgetId }
                        if (index != -1) {
                            widgetItems[index] = item.copy(x = newX, y = newY)
                            onUpdate()
                        }
                    },
                    onSizeChanged = { newWidth, newHeight ->
                        val index =
                            widgetItems.indexOfFirst { it.appWidgetId == item.appWidgetId }
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
                    }, viewModel = viewModel1
                )
            }
        }
    }
}

@Composable
fun DraggableWidget(
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
                        if (viewModel.editWidgets) {
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            onPositionChanged(offsetX, offsetY)
                        }
                    }
                })
        if (viewModel.editWidgets) {
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
                                    if (viewModel.editWidgets) {
                                        change.consume()
                                        widgetWidth = (widgetWidth + dragAmount.x).toInt()
                                            .coerceAtLeast(100)
                                        widgetHeight = (widgetHeight + dragAmount.y).toInt()
                                            .coerceAtLeast(100)
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
fun FuelLogs(onClose: () -> Unit, viewModel: DashboardViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8F)
            .fillMaxHeight(0.8F)
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
    ) {
        val isLoading = remember { mutableStateOf(true) }
        val fuelLogs = remember { mutableStateListOf<FuelLog>() }
        LaunchedEffect(Unit) {
            val logs = viewModel.dashboardDao.getAllLogs()
            fuelLogs.addAll(logs)
            isLoading.value = false
        }

        if (isLoading.value) {
            CircularProgressIndicator()
        } else {
            FuelLogsList(fuelLogs = fuelLogs, onClose = onClose)
        }
    }
}

