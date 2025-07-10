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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ruchitech.carlanuchertab.helper.NowPlayingInfo
import com.ruchitech.carlanuchertab.ui.screens.dashboard.WidgetPickerScreen
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard_home.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint


data class WidgetItem(
    val appWidgetId: Int,
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int,
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    lateinit var viewModel: DashboardViewModel

    val configureWidget =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("WidgetFlow", "Widget Config Result: $result")

            if (result.resultCode == RESULT_OK) {
                Log.d(
                    "WidgetFlow",
                    "Configuration complete. Showing widget ID: ${viewModel.currentAppWidgetId}"
                )
                viewModel.showWidget(viewModel.currentAppWidgetId, this)
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
                    viewModel.showWidget(viewModel.currentAppWidgetId, this)
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
                    viewModel.showWidget(appWidgetId, this)
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


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        registerMusicReceiver()
        //  viewModel.initData(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    //  DashboardHome(viewModel)
                    WidgetPickerScreen()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun registerMusicReceiver() {
        try {
            val filter = IntentFilter("now_playing_update")
            registerReceiver(
                nowPlayingReceiver,
                filter,
                RECEIVER_NOT_EXPORTED
            )

        } catch (e: Exception) {
            Log.e("MusicNotification", "❌ Failed to register music receiver: $e")
        }
    }


    override fun onDestroy() {
        viewModel.appWidgetHost.stopListening()
        unregisterReceiver(nowPlayingReceiver)
        super.onDestroy()
    }
}


