package com.ruchitech.carlanuchertab


import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ruchitech.carlanuchertab.helper.NowPlayingInfo
import com.ruchitech.carlanuchertab.helper.VoiceCommandHelper
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard_home.DashboardHome
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard_home.DashboardViewModel


data class WidgetItem(
    val appWidgetId: Int,
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int,
)


class MainActivity : ComponentActivity() {

    val viewModel: DashboardViewModel by viewModels()

    val configureWidget =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("WidgetFlow", "Widget Config Result: $result")

            if (result.resultCode == RESULT_OK) {
                Log.d(
                    "WidgetFlow",
                    "Configuration complete. Showing widget ID: ${viewModel.currentAppWidgetId}"
                )
                viewModel.showWidget(viewModel.currentAppWidgetId)
            } else {
                Log.w(
                    "WidgetFlow", "Widget configuration canceled. ResultCode: ${result.resultCode}"
                )
                // Optional: Show anyway if configure activity was launched
                val appWidgetInfo =
                    viewModel.appWidgetManager.getAppWidgetInfo(viewModel.currentAppWidgetId)
                if (appWidgetInfo?.configure != null) {
                    // Some apps don't return RESULT_OK even when configured
                    Log.w("WidgetFlow", "Trying to show widget anyway.")
                    viewModel.showWidget(viewModel.currentAppWidgetId)
                }
            }
        }

    val pickWidget =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("WidgetFlow", "Widget Picker Result: $result")

            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    ?: return@registerForActivityResult

                Log.d("WidgetFlow", "Picked widget ID: $appWidgetId")

                viewModel.currentAppWidgetId = appWidgetId
                val appWidgetInfo = viewModel.appWidgetManager.getAppWidgetInfo(appWidgetId)

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
                    viewModel.showWidget(appWidgetId)
                }
            } else {
                Log.w(
                    "WidgetFlow",
                    "Widget picker canceled or failed. ResultCode: ${result.resultCode}"
                )
            }
        }

    private val nowPlayingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { intent: Intent ->
                val title = intent.getStringExtra("title")
                val artist = intent.getStringExtra("artist")
                val artworkPath = intent.getStringExtra("artwork_path")
                val artwork = artworkPath?.let { path ->
                    try {
                        BitmapFactory.decodeFile(path)
                    } catch (e: Exception) {
                        Log.e("NowPlayingReceiver", "❌ Failed to decode artwork from file: $e")
                        null
                    }
                }

                Log.d("MusicNotification", "From Broadcast 🎵 Song Title: $title, 👤 Artist: $artist")
                viewModel.updateNowPlaying(
                    NowPlayingInfo(title, artist, artwork)
                )
            }
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        viewModel.initData(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                nowPlayingReceiver,
                IntentFilter("now_playing_update"),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(nowPlayingReceiver, IntentFilter("now_playing_update"))
        }


        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardHome(viewModel)
                }
            }

        }
    }

    override fun onDestroy() {
        viewModel.appWidgetHost.stopListening()
        unregisterReceiver(nowPlayingReceiver)
        super.onDestroy()
    }
}


