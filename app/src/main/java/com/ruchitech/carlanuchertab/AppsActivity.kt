package com.ruchitech.carlanuchertab

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DraggablePosition(
    val x: Float = 0f,
    val y: Float = 0f
)


class AppsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = resources.getColor(R.color.transparent)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    InstalledAppsScreen()
                }
            }
            /*       val fixedDensity = Density(
                       density = 1f,
                       fontScale = 1f
                   ) // You can also use 2f to simulate 2x density (e.g. mdpi, hdpi)
                   CompositionLocalProvider(LocalDensity provides fixedDensity) {
                       MaterialTheme {
                           Surface(modifier = Modifier.fillMaxSize()) {
                               InstalledAppsScreen()
                           }
                       }
                   }*/
        }
    }
}


@Composable
fun InstalledAppsScreen() {
    val context = LocalContext.current
    val allApps = remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val searchQuery = remember { mutableStateOf("") }

    val filteredApps by remember(searchQuery.value, allApps.value) {
        derivedStateOf {
            if (searchQuery.value.isBlank()) {
                allApps.value
            } else {
                allApps.value.filter {
                    it.name.contains(searchQuery.value, ignoreCase = true)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        allApps.value = getInstalledApps(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.launcher_bg7),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            // 🔍 Search TextField
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                label = { Text("Search apps", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(8.dp)
                    .padding(horizontal = 24.dp)
            )

            if (allApps.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        AppGridItem(app = app)
                    }
                }
            }
        }
    }
}



@Composable
fun AppGridItem(app: AppInfo) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2F)
            .clickable {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                context.startActivity(launchIntent)
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App icon
        if (app.icon != null) {
            Image(
                bitmap = app.icon.toBitmap().asImageBitmap(),
                contentDescription = "${app.name} icon",
                modifier = Modifier.size(56.dp),
            )
        } else {
            Image(
                painter = painterResource(android.R.drawable.sym_def_app_icon),
                contentDescription = "Default app icon",
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App name with ellipsis
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
)

suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val packageManager = context.packageManager
    val apps = mutableListOf<AppInfo>()

    // Get all launcher activities
    val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val launcherPackages = packageManager.queryIntentActivities(launcherIntent, 0)
        .map { it.activityInfo.packageName }
        .toSet()

    // Get all installed applications
    val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    for (appInfo in installedApps) {
        // Include if it's either a launcher app or a non-system app
        if (appInfo.packageName in launcherPackages || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
            apps.add(
                AppInfo(
                    name = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName,
                    icon = appInfo.loadIcon(packageManager)
                )
            )
        }
    }

    // Sort alphabetically
    apps.sortedBy { it.name.lowercase() }
}