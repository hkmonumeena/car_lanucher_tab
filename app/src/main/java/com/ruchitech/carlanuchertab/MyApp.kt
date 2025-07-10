package com.ruchitech.carlanuchertab

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
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
                    val name = appInfo.loadLabel(packageManager).toString()
                    val icon = appInfo.loadIcon(packageManager)
                    val packageName = appInfo.packageName

                    apps.add(AppInfo(name, packageName, icon))
                } catch (e: Exception) {
                    Log.e("AppLoader", "Failed to load app info for: ${appInfo.packageName}", e)
                }
            }

            val sorted = apps.sortedBy { it.name.lowercase() }
            Log.d("AppLoader", "Loaded ${sorted.size} apps")
            sorted
        }

    }
}

