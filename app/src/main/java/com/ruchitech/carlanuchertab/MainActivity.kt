package com.ruchitech.carlanuchertab


import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.ruchitech.carlanuchertab.ui.navigationstack.NavigationGraph
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
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                calculateWindowSizeClass(this)
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationGraph(navController)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


