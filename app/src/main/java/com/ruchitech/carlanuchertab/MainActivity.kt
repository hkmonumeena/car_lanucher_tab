package com.ruchitech.carlanuchertab


import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Surface
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.idapgroup.snowfall.snowfall
import com.ruchitech.carlanuchertab.clock.ShowAnalogClock
import com.ruchitech.carlanuchertab.helper.BottomNavItem
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
import com.ruchitech.carlanuchertab.helper.wallpapers
import com.ruchitech.carlanuchertab.roomdb.action.AppDatabase
import com.ruchitech.carlanuchertab.roomdb.dao.DashboardDao
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


data class WidgetItem(
    val appWidgetId: Int,
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int,
)

data class FuelRecord(
    val id: Int,
    val date: java.time.LocalDate,
    val amount: Double, // in liters
    val cost: Double,   // in currency
    val mileage: Int,    // in km
)


class MainActivity : ComponentActivity() {
    val widgetItems = mutableStateListOf<WidgetItem>()
    var editWidgets by mutableStateOf(false)
    var isSnowfalll by mutableStateOf(false)
    var showFuelLogs by mutableStateOf(false)
    var setWallpaper by mutableStateOf(R.drawable.launcher_bg7)

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost
    private val APPWIDGET_HOST_ID = 1024
    private var currentAppWidgetId = -1
    private lateinit var appDatabase: AppDatabase
    private lateinit var dashboardDao: DashboardDao


    private val pickWidget =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("WidgetFlow", "Widget Picker Result: $result")

            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    ?: return@registerForActivityResult

                Log.d("WidgetFlow", "Picked widget ID: $appWidgetId")

                currentAppWidgetId = appWidgetId
                val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

                if (appWidgetInfo == null) {
                    Log.e("WidgetFlow", "AppWidgetInfo is null for ID: $appWidgetId")
                    return@registerForActivityResult
                }

                Log.d(
                    "WidgetFlow",
                    "Picked widget: ${appWidgetInfo.label} - configure: ${appWidgetInfo.configure}"
                )

                if (appWidgetInfo.configure != null) {
                    val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                    configIntent.component = appWidgetInfo.configure
                    configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    Log.d("WidgetFlow", "Launching config activity for widget ID: $appWidgetId")
                    configureWidget.launch(configIntent)
                } else {
                    Log.d("WidgetFlow", "No config required. Directly showing widget.")
                    showWidget(appWidgetId)
                }
            } else {
                Log.w(
                    "WidgetFlow",
                    "Widget picker canceled or failed. ResultCode: ${result.resultCode}"
                )
            }
        }

    private val configureWidget =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("WidgetFlow", "Widget Config Result: $result")

            if (result.resultCode == RESULT_OK) {
                Log.d(
                    "WidgetFlow", "Configuration complete. Showing widget ID: $currentAppWidgetId"
                )
                showWidget(currentAppWidgetId)
            } else {
                Log.w(
                    "WidgetFlow", "Widget configuration canceled. ResultCode: ${result.resultCode}"
                )
                // Optional: Show anyway if configure activity was launched
                val appWidgetInfo = appWidgetManager.getAppWidgetInfo(currentAppWidgetId)
                if (appWidgetInfo?.configure != null) {
                    // Some apps don't return RESULT_OK even when configured
                    Log.w("WidgetFlow", "Trying to show widget anyway.")
                    showWidget(currentAppWidgetId)
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        val db = AppDatabase.getDatabase(this)
        dashboardDao = db.dashboardDao()
        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)
        appWidgetHost.startListening()
        CoroutineScope(Dispatchers.IO).launch {
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                setWallpaper = dashboard.wallpaperId
                widgetItems.addAll(dashboard.widgets)
                isSnowfalll = dashboard.isSnowfall ?: false
            }
        }
        //   widgetItems.addAll(loadWidgetItems())
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherHomeScreen(
                        wallpaper = setWallpaper,
                        onAddWidget = { launchWidgetPicker() },
                        widgetItems = widgetItems,
                        appWidgetManager = appWidgetManager,
                        widgetHost = appWidgetHost,
                        onUpdate = { saveWidgetItems(widgetItems) })
                }
            }

        }
    }

    private fun launchWidgetPicker() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        currentAppWidgetId = appWidgetId

        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        // Required: tell Android you have no custom widgets to offer
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, ArrayList())
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, ArrayList())

        pickWidget.launch(pickIntent)
    }

    private fun showWidget(appWidgetId: Int) {
        Log.e("showWidget", "Showing widget: $appWidgetId")
        CoroutineScope(Dispatchers.IO).launch {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val dashboardData = dashboardDao.getDashboard()
            if (info == null) {
                Log.e("showWidget", "AppWidgetInfo is null for ID: $appWidgetId")
                if (dashboardData != null) {
                    deleteWidget(appWidgetId)
                }
                return@launch
            }

            val widgetItem = WidgetItem(
                appWidgetId = appWidgetId, x = 0f, y = 0f, width = 400, height = 400
            )

            if (dashboardData != null) {
                val updatedWidgets = dashboardData.widgets.toMutableList().apply {
                    add(widgetItem)
                }
                val updatedDashboard = dashboardData.copy(widgets = updatedWidgets)
                dashboardDao.updateDashboard(updatedDashboard)
            } else {
                dashboardDao.insertOrUpdateDashboard(Dashboard(widgets = listOf(widgetItem)))
            }
            widgetItems.add(widgetItem)
        }
        //saveWidgetItems(widgetItems)
    }

    suspend fun deleteWidget(widgetId: Int) {
        val dashboard = dashboardDao.getDashboard()
        if (dashboard != null) {
            val updatedWidgets = dashboard.widgets.filter { it.appWidgetId != widgetId }
            val updatedDashboard = dashboard.copy(widgets = updatedWidgets)
            dashboardDao.updateDashboard(updatedDashboard)
            widgetItems.clear()
            widgetItems.addAll(updatedWidgets)
        }
    }


    fun saveWidgetItems(items: List<WidgetItem>) {
        CoroutineScope(Dispatchers.IO).launch {
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                dashboardDao.updateDashboard(dashboard.copy(widgets = items))
            } else {
                dashboardDao.insertOrUpdateDashboard(Dashboard(widgets = items))
            }
        }
    }


    @Composable
    fun MultiWidgetCanvas(
        widgetItems: SnapshotStateList<WidgetItem>,
        widgetHost: AppWidgetHost,
        appWidgetManager: AppWidgetManager,
        onUpdate: () -> Unit,
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
                        })
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
    ) {
        val context = LocalContext.current
        var offsetX by remember { mutableStateOf(item.x) }
        var offsetY by remember { mutableStateOf(item.y) }
        var widgetWidth by remember { mutableStateOf(item.width) }
        var widgetHeight by remember { mutableStateOf(item.height) }

        Box(modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Log.e("fdknfjdonfld", "DraggableWidget: worked press")
                        onLongPressToRemove(item)
                    })
            }) {
            AndroidView(factory = {
                val info = appWidgetManager.getAppWidgetInfo(item.appWidgetId)
                widgetHost.createView(context, item.appWidgetId, info).apply {
                    setAppWidget(item.appWidgetId, info)
                    layoutParams = FrameLayout.LayoutParams(widgetWidth, widgetHeight)
                }
            }, modifier = Modifier
                .size(widgetWidth.dp, widgetHeight.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        if (editWidgets) {
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            onPositionChanged(offsetX, offsetY)
                        }
                    }
                })
            if (editWidgets) {
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
                                        deleteWidget(item.appWidgetId)
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
                                        if (editWidgets) {
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
    fun FuelLogs(onClose: () -> Unit) {
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
                val logs = dashboardDao.getAllLogs()
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
    ) {
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
                    if (isSnowfalll) Modifier.snowfall(density = 0.020, alpha = 0.5f)
                    else Modifier
                )
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
                                saveWidgetItems(emptyList())
                                //onUpdate()
                            }

                            is WidgetMenuAction.EditWidgets -> {
                                editWidgets = !editWidgets
                            }

                            is WidgetMenuAction.Wallpapers -> {
                                showWallpaperSheet = true
                            }

                            is WidgetMenuAction.Fuel -> {
                                showFuelDialog = true
                            }

                            is WidgetMenuAction.Snowfall -> {
                                isSnowfalll = !isSnowfalll
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dashboard = dashboardDao.getDashboard()
                                    if (dashboard != null) {
                                        dashboardDao.updateDashboard(dashboard.copy(isSnowfall = isSnowfalll))
                                    } else {
                                        dashboardDao.insertOrUpdateDashboard(Dashboard(isSnowfall = isSnowfalll))
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
                            dashboardDao.insertLog(newLog)
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
                                                    val dashboard = dashboardDao.getDashboard()
                                                    if (dashboard != null) {
                                                        dashboardDao.updateDashboard(
                                                            dashboard.copy(wallpaperId = wallpaper)
                                                        )
                                                    } else {
                                                        dashboardDao.insertOrUpdateDashboard(
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
                        is BottomNavItem.Map -> Unit
                        is BottomNavItem.Radio -> Unit
                        is BottomNavItem.Music -> {
                            val packageName = "in.krosbits.musicolet"
                            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                            if (launchIntent != null) {
                                startActivity(launchIntent)
                            } else {
                                Toast.makeText(
                                    this@MainActivity, "App not installed", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        is BottomNavItem.AllApps -> {
                            startActivity(
                                Intent(
                                    this@MainActivity, AppsActivity::class.java
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
                    })
                }

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        appWidgetHost.stopListening()
    }
}


