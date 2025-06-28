package com.ruchitech.carlanuchertab


import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources.Theme
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
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
import com.ruchitech.carlanuchertab.ui.theme.nonScaledSp

data class WidgetItem(
    val appWidgetId: Int,
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int,
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
                val appWidgetId =
                    data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
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
                    "WidgetFlow",
                    "Configuration complete. Showing widget ID: $currentAppWidgetId"
                )
                showWidget(currentAppWidgetId)
            } else {
                Log.w(
                    "WidgetFlow",
                    "Widget configuration canceled. ResultCode: ${result.resultCode}"
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
        window.statusBarColor =resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)
        appWidgetHost.startListening()
        widgetItems.addAll(loadWidgetItems())
        setContent {
                 val fixedDensity = Density(
                     density = 0f,
                     fontScale = 0f
                 ) // You can also use 2f to simulate 2x density (e.g. mdpi, hdpi)
                 CompositionLocalProvider(LocalDensity provides fixedDensity) {
                     MaterialTheme {
                         Surface(modifier = Modifier.fillMaxSize()) {
                             LauncherHomeScreen(
                                 onAddWidget = { launchWidgetPicker() },
                                 widgetItems = widgetItems,
                                 appWidgetManager = appWidgetManager,
                                 widgetHost = appWidgetHost,
                                 onUpdate = { saveWidgetItems(widgetItems) }
                             )
                         }
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
                appWidgetId = appWidgetId,
                x = 0f,
                y = 0f,
                width = 400,
                height = 400
            )
        )
        saveWidgetItems(widgetItems)
    }

    fun saveWidgetItems(items: List<WidgetItem>) {
        val json = Gson().toJson(items)
        getSharedPreferences("widgets", MODE_PRIVATE).edit()
            .putString("widget_items", json).apply()
    }

    fun loadWidgetItems(): List<WidgetItem> {
        val json = getSharedPreferences("widgets", MODE_PRIVATE)
            .getString("widget_items", "[]")
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
                        }
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
                            onLongPressToRemove(item)
                        }
                    )
                }
        ) {
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
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            onPositionChanged(offsetX, offsetY)
                        }
                    }
            )

            // Bottom-right resize handle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.Gray.copy(alpha = 0.6f), shape = CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            widgetWidth = (widgetWidth + dragAmount.x).toInt().coerceAtLeast(100)
                            widgetHeight = (widgetHeight + dragAmount.y).toInt().coerceAtLeast(100)
                            onSizeChanged(widgetWidth, widgetHeight)
                        }
                    }
            )
        }
    }

    fun requestNotificationListenerPermission(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        context.startActivity(intent)
    }


    fun sendMediaButtonEvent(keyCode: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

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
            modifier = Modifier
                .background(Color.Red)
                .padding(16.dp)
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


    @Composable
    fun LauncherHomeScreen(
        onAddWidget: () -> Unit,
        widgetItems: SnapshotStateList<WidgetItem>,
        appWidgetManager: AppWidgetManager,
        widgetHost: AppWidgetHost,
        onUpdate: () -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }
        val bottomIcons = listOf(
            Pair(R.drawable.map, "Map"),
            Pair(R.drawable.radio, "Radio"),
            Pair(R.drawable.music, "Music"),
            Pair(R.drawable.apps, "All apps")
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Image(
                painter = painterResource(R.drawable.launcher_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )

            Box(modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .padding(top = 25.dp)) {
                //AnalogClock()
                DigitalClock()
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(start = 50.dp, top = 30.dp)
            ) {
                AnalogClock()
                Box(modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .padding(top = 80.dp)) {
                    Text(
                        text = "27 Jun",
                        style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Default,
                            fontSize = 24.sp.nonScaledSp
                        )
                    )
                }
            }


            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(top = 25.dp, end = 15.dp)
            ) {
                IconButton(
                    onClick = {
                        showMenu = true
            /*            val intent =
                            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        startActivity(intent) // S*/

                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF2D2D2D))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add Widget",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        onClick = {
                            showMenu = false
                            onAddWidget()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Remove All Widgets",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        onClick = {
                            showMenu = false
                            widgetItems.clear()
                            saveWidgetItems(emptyList())
                            onUpdate()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Edit Widgets",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        onClick = {
                            showMenu = false
                            // Implement edit functionality here
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )
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

            // NowPlayingMiniUI(context = LocalContext.current)

            // Bottom icon row
            //0x99000000
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .wrapContentWidth()
                    .height(100.dp)
                    .background(Color(0x00000000)) // Semi-transparent black background
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = CenterVertically
                ) {
                    bottomIcons.forEach { (icon, title) ->
                        /*  IconButton(onClick = {
                              sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                          }) {
                              Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause")
                          }*/

                        Column(
                            horizontalAlignment = CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .width(80.dp)
                                .clickable {
                                    // Handle icon click
                                    when (title) {
                                        "Settings" -> showMenu = true
                                        "All apps" -> startActivity(
                                            Intent(
                                                this@MainActivity,
                                                AppsActivity::class.java
                                            )
                                        )
                                        // Add other cases as needed
                                    }
                                }
                        ) {
                            // Icon with filled white circle and grey border
                            Box(
                                modifier = Modifier
                                    .size(60.dp) // Size of the circle
                                    .background(
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = Color.Red.copy(0.2F),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp), // Padding inside the circle
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = icon),
                                    contentDescription = title,
                                    /// colorFilter = ColorFilter.tint(Color.Black),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        appWidgetHost.stopListening()
    }
}


