package com.ruchitech.carlanuchertab


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.ruchitech.carlanuchertab.helper.isNotificationListenerEnabled
import com.ruchitech.carlanuchertab.helper.openNotificationAccessSettings
import com.ruchitech.carlanuchertab.ui.navigationstack.NavigationGraph
import com.ruchitech.carlanuchertab.ui.navigationstack.Screen
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        if (!isNotificationListenerEnabled(this)) {
            openNotificationAccessSettings(this)
            return
        }

        setContent {
            val navController = rememberNavController()
            LaunchedEffect(Unit) {
                ClickedViewBus.clickedViews.collect { viewId ->
                    Log.e("ihbygyigbiyh", "onCreate: $viewId")
                    when (viewId) {
                        "com.android.systemui:id/home" -> {
                            navController.navigate(Screen.Home) {
                                popUpTo(0) { inclusive = true } // Clears the entire back stack
                                launchSingleTop =
                                    true          // Prevents multiple copies of the destination
                            }
                        }

                        "com.android.systemui:id/apps" -> {
                            navController.navigate(Screen.Apps) {
                                launchSingleTop =
                                    true          // Prevents multiple copies of the destination
                            }
                        }

                    }
                }
            }

            MaterialTheme {
                calculateWindowSizeClass(this)
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationGraph(navController)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // For Android 11+ (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        // For older versions
        else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }

    fun getNavigationBarHeight(): Int {
        val resources = resources
        val resourceId = resources.getIdentifier(
            "navigation_bar_height",
            "dimen",
            "android"
        )
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}


