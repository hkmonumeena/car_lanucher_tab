package com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard_home

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem
import com.ruchitech.carlanuchertab.helper.VoiceCommandHelper
import com.ruchitech.carlanuchertab.roomdb.action.AppDatabase
import com.ruchitech.carlanuchertab.roomdb.dao.DashboardDao
import com.ruchitech.carlanuchertab.roomdb.data.Dashboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    val widgetItems = mutableStateListOf<WidgetItem>()
    var editWidgets by mutableStateOf(false)
    var isSnowfalll by mutableStateOf(false)
    var showFuelLogs by mutableStateOf(false)
    var setWallpaper by mutableStateOf(R.drawable.launcher_bg7)
    lateinit var appWidgetManager: AppWidgetManager
    lateinit var appWidgetHost: AppWidgetHost
    val APPWIDGET_HOST_ID = 1024
    var currentAppWidgetId = -1
    lateinit var appDatabase: AppDatabase
    lateinit var dashboardDao: DashboardDao
     lateinit var voiceHelper: VoiceCommandHelper

    fun initData(context: Context) {
        viewModelScope.launch(Dispatchers.IO){
         appDatabase = AppDatabase.getDatabase(context)
        dashboardDao = appDatabase.dashboardDao()
        appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetHost = AppWidgetHost(context, APPWIDGET_HOST_ID)
        appWidgetHost.startListening()
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                setWallpaper = dashboard.wallpaperId
                widgetItems.addAll(dashboard.widgets)
                isSnowfalll = dashboard.isSnowfall ?: false
            }

            voiceHelper = VoiceCommandHelper(context) { command ->
                when {
                    "open music" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Open Music")
                    }

                    "fuel log" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Fuel Log")
                    }

                    "weather" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Weather")
                    }

                    "map" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Map")
                    }

                    "add widget" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Add Widget")
                    }
                    "start snow" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Add Widget")
                        isSnowfalll = true
                    }

                    "stop snow" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Add Widget")
                        isSnowfalll = false
                    }

                    else -> {
                        Log.w("VoiceCommand", "Unknown command: $command")

                    }
                }
            }

        }
    }


     fun launchWidgetPicker(launchWidget: (pickIntent: Intent) -> Unit) {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        currentAppWidgetId = appWidgetId

        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        // Required: tell Android you have no custom widgets to offer
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, ArrayList())
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, ArrayList())
        launchWidget(pickIntent)
     //   pickWidget.launch(pickIntent)
    }

    fun showWidget(appWidgetId: Int) {
        Log.e("showWidget", "Showing widget: $appWidgetId")
        viewModelScope.launch(Dispatchers.IO) {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val dashboardData = dashboardDao.getDashboard()
            if (info == null) {
                Log.e("showWidget", "AppWidgetInfo is null for ID: $appWidgetId")
                if (dashboardData != null) {
                    deleteWidget(appWidgetId)
                }
                return@launch
            }

            val widgetItem = WidgetItem(
                appWidgetId = appWidgetId, x = 0f, y = 0f, width = 400, height = 400
            )

            if (dashboardData != null) {
                val updatedWidgets = dashboardData.widgets.toMutableList().apply {
                    add(widgetItem)
                }
                val updatedDashboard = dashboardData.copy(widgets = updatedWidgets)
                dashboardDao.updateDashboard(updatedDashboard)
            } else {
                dashboardDao.insertOrUpdateDashboard(Dashboard(widgets = listOf(widgetItem)))
            }
            widgetItems.add(widgetItem)
        }
        //saveWidgetItems(widgetItems)
    }

    suspend fun deleteWidget(widgetId: Int) {
        val dashboard = dashboardDao.getDashboard()
        if (dashboard != null) {
            val updatedWidgets = dashboard.widgets.filter { it.appWidgetId != widgetId }
            val updatedDashboard = dashboard.copy(widgets = updatedWidgets)
            dashboardDao.updateDashboard(updatedDashboard)
            widgetItems.clear()
            widgetItems.addAll(updatedWidgets)
        }
    }


    fun saveWidgetItems(items: List<WidgetItem>) {
        viewModelScope.launch(Dispatchers.IO){
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                dashboardDao.updateDashboard(dashboard.copy(widgets = items))
            } else {
                dashboardDao.insertOrUpdateDashboard(Dashboard(widgets = items))
            }
        }
    }


}