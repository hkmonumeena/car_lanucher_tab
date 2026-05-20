package com.ruchitech.carlanuchertab

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ruchitech.carlanuchertab.helper.StartupAlertPlayer
import com.ruchitech.carlanuchertab.ui.screens.apps.AppInfo
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        StartupAlertPlayer.scheduleIfNeeded(this)
        CoroutineScope(Dispatchers.IO).launch {
            loadInstalledApps()
        }
    }

    companion object {
        lateinit var instance: MyApp
            private set

        private var _allApps: List<AppInfo> = emptyList()
        val allApps: List<AppInfo>
            get() = _allApps

        suspend fun loadInstalledApps() {
            _allApps = getInstalledApps(instance)
        }

        private suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val apps = mutableListOf<AppInfo>()

            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolvedActivities = packageManager.queryIntentActivities(launcherIntent, 0)

            for (resolveInfo in resolvedActivities) {
                val activityInfo = resolveInfo.activityInfo
                val appInfo = activityInfo.applicationInfo

                try {
                    val name = resolveInfo.loadLabel(packageManager)
                        ?.toString()
                        ?.takeIf { it.isNotBlank() }
                        ?: appInfo.loadLabel(packageManager).toString()
                    val icon = resolveInfo.loadIcon(packageManager)
                    val packageName = activityInfo.packageName
                    val launchActivityName = activityInfo.name

                    apps.add(
                        AppInfo(
                            name = name,
                            packageName = packageName,
                            launchActivityName = launchActivityName,
                            icon = icon
                        )
                    )
                } catch (e: Exception) {
                    Log.e("AppLoader", "Failed to load app info for: ${appInfo.packageName}", e)
                }
            }

            val sorted = apps
                .distinctBy { it.stableKey }
                .sortedWith(
                    compareBy<AppInfo> { it.name.lowercase() }
                        .thenBy { it.packageName }
                        .thenBy { it.launchActivityName }
                )
            Log.d("AppLoader", "Loaded ${sorted.size} apps")
            sorted
        }

    }
}

