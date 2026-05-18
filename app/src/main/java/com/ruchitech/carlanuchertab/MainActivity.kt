package com.ruchitech.carlanuchertab


import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.ruchitech.carlanuchertab.helper.enableAccessibilityService
import com.ruchitech.carlanuchertab.ui.navigationstack.NavigationGraph
import com.ruchitech.carlanuchertab.ui.navigationstack.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


data class WidgetItem(
    val appWidgetId: Int,
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int,
)


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val navigationRequests = MutableSharedFlow<Screen>(extraBufferCapacity = 1)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            val navController = rememberNavController()
            LaunchedEffect(Unit) {
                navigationRequests.collect { screen ->
                    when (screen) {
                        Screen.Home -> {
                            navController.navigate(Screen.Home) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }

                        Screen.Apps -> {
                            navController.navigate(Screen.Apps) {
                                launchSingleTop = true
                            }
                        }

                        else -> Unit
                    }
                }
            }

            LaunchedEffect(Unit) {
                ClickedViewBus.clickedViews.collect { viewId ->
                    Log.e("ihbygyigbiyh", "onCreate: $viewId")
                    when (viewId) {
                        "com.android.systemui:id/home" -> {
                            requestNavigation(Screen.Home)
                        }

                        "com.android.systemui:id/apps" -> {
                            requestNavigation(Screen.Apps)
                        }

                        "openAccessibilitySettings" -> {
                            Log.e("gfjgbifbgkfjgfg", "onCreate: $viewId")
                            enableAccessibilityService(this@MainActivity)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLauncherIntent(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun handleLauncherIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_MAIN) return
        val categories = intent.categories ?: return
        if (Intent.CATEGORY_HOME in categories || Intent.CATEGORY_LAUNCHER in categories) {
            requestNavigation(Screen.Home)
        }
    }

    private fun requestNavigation(screen: Screen) {
        if (!navigationRequests.tryEmit(screen)) {
            lifecycleScope.launch {
                navigationRequests.emit(screen)
            }
        }
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


