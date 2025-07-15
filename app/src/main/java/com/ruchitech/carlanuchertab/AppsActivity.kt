package com.ruchitech.carlanuchertab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ruchitech.carlanuchertab.MyApp.Companion.loadInstalledApps
import com.ruchitech.carlanuchertab.ui.screens.apps.InstalledAppsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class DraggablePosition(
    val x: Float = 0f,
    val y: Float = 0f,
)

@AndroidEntryPoint
class AppsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            MaterialTheme {
                Scaffold { _padding ->
                    InstalledAppsScreen(modifier = Modifier.padding(_padding))
                }
            }
        }
    }

    override fun onDestroy() {
        CoroutineScope(Dispatchers.IO).launch {
            loadInstalledApps()
        }
        super.onDestroy()
    }
}




