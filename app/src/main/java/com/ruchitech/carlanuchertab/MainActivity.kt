package com.ruchitech.carlanuchertab


import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ruchitech.carlanuchertab.clock.AnalogClock
import com.ruchitech.carlanuchertab.clock.DigitalClock
import com.ruchitech.carlanuchertab.helper.BottomNavItem
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
import com.ruchitech.carlanuchertab.helper.getActiveMediaMetadata
import com.ruchitech.carlanuchertab.helper.getCurrentDateFormatted
import com.ruchitech.carlanuchertab.ui.composables.HomeBottomIcons
import com.ruchitech.carlanuchertab.ui.composables.WidgetsDropdownMenu
import com.ruchitech.carlanuchertab.ui.theme.nonScaledSp
import kotlinx.datetime.LocalDate
import java.time.format.DateTimeFormatter

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
    val mileage: Int    // in km
)


class MainActivity : ComponentActivity() {
    val widgetItems = mutableStateListOf<WidgetItem>()

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    private val APPWIDGET_HOST_ID = 1024

    private var currentAppWidgetId = -1

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)
        appWidgetHost.startListening()
        widgetItems.addAll(loadWidgetItems())
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherHomeScreen(
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

        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (info == null) {
            Log.e("showWidget", "AppWidgetInfo is null for ID: $appWidgetId")
            return
        }

        widgetItems.add(
            WidgetItem(
                appWidgetId = appWidgetId, x = 0f, y = 0f, width = 400, height = 400
            )
        )
        saveWidgetItems(widgetItems)
    }

    fun saveWidgetItems(items: List<WidgetItem>) {
        val json = Gson().toJson(items)
        getSharedPreferences("widgets", MODE_PRIVATE).edit().putString("widget_items", json).apply()
    }

    fun loadWidgetItems(): List<WidgetItem> {
        val json = getSharedPreferences("widgets", MODE_PRIVATE).getString("widget_items", "[]")
        return Gson().fromJson(json, object : TypeToken<List<WidgetItem>>() {}.type)
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

        Box(modifier = Modifier.offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPressToRemove(item)
                    })
            }) {
            AndroidView(factory = {
                val info = appWidgetManager.getAppWidgetInfo(item.appWidgetId)
                widgetHost.createView(context, item.appWidgetId, info).apply {
                    setAppWidget(item.appWidgetId, info)
                    layoutParams = FrameLayout.LayoutParams(widgetWidth, widgetHeight)
                }
            }, modifier = Modifier.size(widgetWidth.dp, widgetHeight.dp).pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onPositionChanged(offsetX, offsetY)
                    }
                })

            // Bottom-right resize handle
            Box(
                modifier = Modifier.size(24.dp).align(Alignment.BottomEnd)
                    .background(Color.Gray.copy(alpha = 0.6f), shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            widgetWidth = (widgetWidth + dragAmount.x).toInt().coerceAtLeast(100)
                            widgetHeight = (widgetHeight + dragAmount.y).toInt().coerceAtLeast(100)
                            onSizeChanged(widgetWidth, widgetHeight)
                        }
                    })
        }
    }

    fun requestNotificationListenerPermission(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        context.startActivity(intent)
    }


    fun sendMediaButtonEvent(keyCode: Int) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)

        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val flat =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(pkgName)
    }

    @Composable
    fun NowPlayingMiniUI(context: Context) {
        val metadataState = remember { mutableStateOf<MediaMetadata?>(null) }

        // Check for permission
        LaunchedEffect(Unit) {
            if (!isNotificationServiceEnabled(context)) {
                requestNotificationListenerPermission(context)
            } else {
                metadataState.value = getActiveMediaMetadata(context)
            }
        }

        val metadata = metadataState.value

        Column(
            modifier = Modifier.background(Color.Red).padding(16.dp)
        ) {
            Text("Now Playing:", color = Color.White)
            Text(
                "Title: ${metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown"}",
                color = Color.White
            )
            Text(
                "Artist: ${metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown"}",
                color = Color.White
            )
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun FuelHistoryRoad() {
        // Sample data - in a real app this would come from a database
        val fuelRecords = remember {
            listOf(
                FuelRecord(1, java.time.LocalDate.now(), 42.3, 3500.0, 24500),
                FuelRecord(2, java.time.LocalDate.now().minusDays(4), 38.7, 3200.0, 24100),
                FuelRecord(3, java.time.LocalDate.now().minusDays(7), 40.2, 3300.0, 23700),
                FuelRecord(4, java.time.LocalDate.now().minusDays(10), 39.5, 3250.0, 23300),
                FuelRecord(5, java.time.LocalDate.now().minusDays(10), 39.5, 3250.0, 23300),
                FuelRecord(6, java.time.LocalDate.now().minusDays(10), 39.5, 3250.0, 23300),
                FuelRecord(7, java.time.LocalDate.now().minusDays(10), 39.5, 3250.0, 23300),
            )
        }

        // State for the next predicted filling
        val predictedAmount by remember { mutableStateOf(41.0) }
        val predictedCost by remember { mutableStateOf(3400.0) }

        // Road dimensions
        val roadWidth = 100.dp
        val roadColor = Color(0xFF444444)
        val roadLineColor = Color(0xFFFFD700) // gold/yellow for road markings

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(roadWidth)
                .background(roadColor)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Upcoming filling (at the top)
            FuelRoadItem(
                isNext = true,
                date = "Next",
                amount = predictedAmount,
                cost = predictedCost,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Road markings (dashed lines)
            repeat(3) {
                RoadMarking()
            }

            // Past fillings (most recent first)
            fuelRecords.forEach { record ->
                FuelRoadItem(
                    isNext = false,
                    date = record.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                    amount = record.amount,
                    cost = record.cost,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                RoadMarking()
            }

            // Start of the road (bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(roadLineColor)
            )
        }
    }

    @Composable
    fun RoadMarking() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.Transparent)
        ) {
            // Dashed line in the middle
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dashWidth = 8.dp.toPx()
                val dashHeight = 2.dp.toPx()
                val gap = 8.dp.toPx()

                var startX = 0f
                while (startX < size.width) {
                    drawRect(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(startX, size.height / 2 - dashHeight / 2),
                        size = Size(dashWidth, dashHeight)
                    )
                    startX += dashWidth + gap
                }
            }
        }
    }

    @Composable
    fun FuelRoadItem(
        isNext: Boolean,
        date: String,
        amount: Double,
        cost: Double,
        modifier: Modifier = Modifier
    ) {
        val backgroundColor = if (isNext) Color(0xFF4CAF50) else Color(0xFF2196F3)
        val textColor = Color.White

        Box(
            modifier = modifier
                .width(80.dp)  // Fixed width for compactness
                .height(40.dp) // Fixed height
                .background(backgroundColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Left side - Date
                Text(
                    text = date,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Right side - Amount and Cost stacked vertically
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(30.dp)
                ) {
                    Text(
                        text = "${amount.toInt()}L",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "₹${cost.toInt()}",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LauncherHomeScreen(
        onAddWidget: () -> Unit,
        widgetItems: SnapshotStateList<WidgetItem>,
        appWidgetManager: AppWidgetManager,
        widgetHost: AppWidgetHost,
        onUpdate: () -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }
        var showWallpaperSheet by remember { mutableStateOf(false) }
        var currentWallpaper by remember { mutableStateOf(R.drawable.launcher_bg7) }

        // Wallpaper options
        val wallpapers = listOf(
            R.drawable.launcher_bg1,
            R.drawable.launcher_bg2,
            R.drawable.launcher_bg3,
            R.drawable.launcher_bg4,
            R.drawable.launcher_bg5,
            R.drawable.launcher_bg6,
            R.drawable.launcher_bg7,
            R.drawable.launcher_bg8,
        )

        Box(
            modifier = Modifier.fillMaxSize().background(Color.Transparent)
        ) {
            Image(
                painter = painterResource(currentWallpaper),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.FillWidth
            )

            Box(
                modifier = Modifier.align(alignment = Alignment.CenterEnd)
            ) {
                FuelHistoryRoad()
            }
            Box(
                modifier = Modifier.align(alignment = Alignment.TopStart)
                    .padding(start = 50.dp, top = 30.dp)
            ) {
                AnalogClock()
                Box(
                    modifier = Modifier.align(alignment = Alignment.Center).padding(top = 80.dp)
                ) {
                    Text(
                        text = getCurrentDateFormatted(), style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            fontSize = 24.sp.nonScaledSp
                        )
                    )
                }
            }


            Box(
                modifier = Modifier.align(alignment = Alignment.TopEnd)
                    .padding(top = 25.dp, end = 15.dp)
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
                        modifier = Modifier.size(32.dp)
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
                                onUpdate()
                            }
                            is WidgetMenuAction.EditWidgets -> {
                                // Open wallpaper selector
                                showWallpaperSheet = true
                            }
                        }
                    }
                )

                // Wallpaper Selection Bottom Sheet
                if (showWallpaperSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showWallpaperSheet = false },
                        sheetState = rememberModalBottomSheetState(),
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFF2D2D2D)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Select Wallpaper",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(wallpapers) { wallpaper ->
                                    Card(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .aspectRatio(1f)
                                            .clickable {
                                                currentWallpaper = wallpaper
                                                showWallpaperSheet = false
                                            },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(wallpaper),
                                            contentDescription = "Wallpaper",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
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
                modifier = Modifier.align(Alignment.BottomCenter).wrapContentWidth().height(100.dp)
                    .background(Color(0x00000000)) // Semi-transparent black background
            ) {
                HomeBottomIcons(onClick = { bottomNavItem ->
                    when (bottomNavItem) {
                        is BottomNavItem.Map -> Unit
                        is BottomNavItem.Radio -> Unit
                        is BottomNavItem.Music -> Unit
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
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        appWidgetHost.stopListening()
    }
}


