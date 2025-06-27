package com.ruchitech.carlanuchertab


import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)
        appWidgetHost.startListening()
// Restore previously added widget
        widgetItems.addAll(loadWidgetItems())
        setContent {
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
                            val index = widgetItems.indexOfFirst { it.appWidgetId == itemToRemove.appWidgetId }
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
        onSizeChanged: (Int, Int) -> Unit
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

    @Composable
    fun LauncherHomeScreen(
        onAddWidget: () -> Unit,
        widgetItems: SnapshotStateList<WidgetItem>,
        appWidgetManager: AppWidgetManager,
        widgetHost: AppWidgetHost,
        onUpdate: () -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            // Background with subtle gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                Color(0xFF121212)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Settings icon in top-right corner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { showMenu = true },
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
            )
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        appWidgetHost.stopListening()
    }
}


