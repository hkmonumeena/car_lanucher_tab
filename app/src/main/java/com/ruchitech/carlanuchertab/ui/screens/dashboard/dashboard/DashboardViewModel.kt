package com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruchitech.carlanuchertab.WidgetItem
import com.ruchitech.carlanuchertab.helper.NowPlayingInfo
import com.ruchitech.carlanuchertab.helper.VoiceCommandHelper
import com.ruchitech.carlanuchertab.helper.WidgetMenuAction
import com.ruchitech.carlanuchertab.helper.enableAccessibilityService
import com.ruchitech.carlanuchertab.helper.isAccessibilityEnabled
import com.ruchitech.carlanuchertab.roomdb.dao.DashboardDao
import com.ruchitech.carlanuchertab.roomdb.data.Dashboard
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard.state.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MusicoletNowPlaying(
    val title: String?,
    val artist: String?,
    val album: String?,
    val artwork: Bitmap?,
    val duration: Long,
    val position: Long,
    val isPlaying: Boolean,
)


@HiltViewModel
class DashboardViewModel @Inject constructor(
    val dashboardDao: DashboardDao,
) : ViewModel() {
    val widgetItems = mutableStateListOf<WidgetItem>()
    val APPWIDGET_HOST_ID = 1024
    var currentAppWidgetId = -1
    lateinit var voiceHelper: VoiceCommandHelper

    private val _nowPlaying = MutableStateFlow<MusicoletNowPlaying?>(null)
    val nowPlaying: StateFlow<MusicoletNowPlaying?> = _nowPlaying

    private var lastSkipTime = 0L
    private val DEBOUNCE_INTERVAL_MS = 800L // 800ms between swipes

    private val _musicoletController = MutableStateFlow<MediaController?>(null)
    val musicoletController: StateFlow<MediaController?> = _musicoletController

    private val _uiState = mutableStateOf(DashboardUiState())
    val uiState: State<DashboardUiState> = _uiState

    init {
        initData()
    }

    fun updateMusicoletController(controllers: MediaController?) {
        Log.e("gfdhiguhdf9ghdfiogh", "fdjgbguifdgbkufdn: ${controllers?.metadata}")
        _musicoletController.value = controllers
        updateNowPlayingInfo()
    }

    fun updateNowPlayingInfo() {
        musicoletController.value?.let { controller ->
            Log.e("fdjgbguifdgbkufdn", "updateNowPlayingInfo: ${controller}")
            val metadata = controller.metadata
            val state = controller.playbackState
            val info = MusicoletNowPlaying(
                title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
                album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM),
                artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART),
                duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
                position = state?.position ?: 0L,
                isPlaying = state?.state == PlaybackState.STATE_PLAYING
            )



            _nowPlaying.value = info
        }
    }

    fun seekTo(position: Long) {
        _musicoletController.value?.let { controller ->
            val transportControls = controller.transportControls
            transportControls.seekTo(position)

            // Optional: Update your UI state immediately
            val currentNowPlaying = _nowPlaying.value
            currentNowPlaying?.let {
                _nowPlaying.value = it.copy(position = position)
            }
        } ?: run {
            Log.w("NowPlayingVM", "No media controller available for seeking")
        }
    }



    fun playPause() {
        _musicoletController.value?.let {
            val isPlaying = it.playbackState?.state == PlaybackState.STATE_PLAYING
            if (isPlaying) it.transportControls.pause()
            else it.transportControls.play()
        }
    }

    fun next(){
        val now = System.currentTimeMillis()
        if (now - lastSkipTime < DEBOUNCE_INTERVAL_MS) {
            Log.d("MediaControl", "⏭ Skipped due to debounce")
            return
        }

        lastSkipTime = now
        _musicoletController.value?.transportControls?.skipToNext()
    }
    fun previous() {
        val now = System.currentTimeMillis()
        if (now - lastSkipTime < DEBOUNCE_INTERVAL_MS) {
            Log.d("MediaControl", "⏮ Skipped due to debounce")
            return
        }

        lastSkipTime = now
        _musicoletController.value?.transportControls?.skipToPrevious()
    }


    private fun initData() {
        viewModelScope.launch(Dispatchers.IO) {
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                withContext(Dispatchers.Main) {
                    _uiState.value = DashboardUiState(
                        widgetItems = dashboard.widgets,
                        wallpaperId = dashboard.wallpaperId,
                        isSnowfall = dashboard.isSnowfall
                    )
                }
            }
        }
    }
    fun initData1(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                widgetItems.addAll(dashboard.widgets)
                //    isSnowfalll = dashboard.isSnowfall ?: false
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
                        // isSnowfalll = true
                    }

                    "stop snow" in command -> {
                        Log.d("VoiceCommand", "Command recognized: Add Widget")

                    }

                    else -> {
                        Log.w("VoiceCommand", "Unknown command: $command")

                    }
                }
            }


        }
    }

    fun showWidget(appWidgetId: Int, context: Context) {
        Log.d("showWidget", "📦 Request to show widget: $appWidgetId")

        viewModelScope.launch(Dispatchers.IO) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val dashboardData = dashboardDao.getDashboard()

            if (info == null) {
                Log.e("showWidget", "❌ AppWidgetInfo is null for ID: $appWidgetId")
                if (dashboardData != null) {
                    deleteWidget(appWidgetId)
                }
                return@launch
            }

            val widgetItem = WidgetItem(
                appWidgetId = appWidgetId,
                x = 0f,
                y = 0f,
                width = 400,
                height = 400
            )

            // 🧠 Step 1: Update DB
            if (dashboardData != null) {
                val updatedWidgets = dashboardData.widgets
                    .filterNot { it.appWidgetId == appWidgetId } + widgetItem
                val updatedDashboard = dashboardData.copy(widgets = updatedWidgets)
                dashboardDao.updateDashboard(updatedDashboard)
            } else {
                dashboardDao.insertOrUpdateDashboard(Dashboard(widgets = listOf(widgetItem)))
            }

            // 🧠 Step 2: Update Compose state
            withContext(Dispatchers.Main) {
                val current = _uiState.value
                _uiState.value = current.copy(
                    widgetItems = current.widgetItems
                        .filterNot { it.appWidgetId == appWidgetId } + widgetItem
                )
                Log.d("showWidget", "✅ Widget shown and stored: $widgetItem")
            }
        }
    }

    fun updateWidgetItem(updatedItem: WidgetItem) {
        _uiState.value = _uiState.value.copy(
            widgetItems = _uiState.value.widgetItems.map {
                if (it.appWidgetId == updatedItem.appWidgetId) updatedItem else it
            }
        )
        saveWidgetItems(_uiState.value.widgetItems)
    }

    fun deleteWidget(appWidgetId: Int) {
        _uiState.value = _uiState.value.copy(
            widgetItems = _uiState.value.widgetItems.filterNot { it.appWidgetId == appWidgetId }
        )
        saveWidgetItems(_uiState.value.widgetItems)
    }

    fun clearWidgets() {
        widgetItems.clear()
        _uiState.value = _uiState.value.copy(widgetItems = widgetItems)
        saveWidgetItems(widgetItems)
    }

    fun saveWidgetItems(items: List<WidgetItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                dashboardDao.updateDashboard(dashboard.copy(widgets = items))
            } else {
                dashboardDao.insertOrUpdateDashboard(Dashboard(widgets = items))
            }
        }
    }

    fun setWallpaper(wallpaperId: Int) {
        _uiState.value = _uiState.value.copy(wallpaperId = wallpaperId)
        saveWidgetItems(_uiState.value.widgetItems)
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditMode = !_uiState.value.isEditMode)
    }

    fun toggleSettings() {
        _uiState.value = _uiState.value.copy(showSettings = !_uiState.value.showSettings)
    }

    fun hideWallpaperModal() {
        val current = _uiState.value
        _uiState.value = current.copy(showWallpaper = false)
    }

    fun hideFuelLogsModal() {
        val current = _uiState.value
        _uiState.value = current.copy(showFuelLogs = false)
    }

    fun showFuelLogsModal() {
        val current = _uiState.value
        _uiState.value = current.copy(showFuelLogs = true, addFuelLog =  false)
    }

    fun addFuelLog() {
        val current = _uiState.value
        _uiState.value = current.copy(addFuelLog = true, showFuelLogs = false)
    }

    fun hideAddFuelLogDialog() {
        val current = _uiState.value
        _uiState.value = current.copy(addFuelLog = false)
    }

    fun handleMenuAction(action: WidgetMenuAction,context:Context) {
        when (action) {
            WidgetMenuAction.AddWidget -> TODO()
            WidgetMenuAction.EditWidgets -> toggleEditMode()
            WidgetMenuAction.Fuel -> TODO()
            WidgetMenuAction.Fuels -> TODO()
            WidgetMenuAction.RemoveAllWidgets -> clearWidgets()
            WidgetMenuAction.Snowfall -> {
                if (!isAccessibilityEnabled(context)){
                    enableAccessibilityService(context)
                }else{
                    _uiState.value = _uiState.value.copy(isSnowfall = !_uiState.value.isSnowfall)
                }
            }

            WidgetMenuAction.Wallpapers -> {
                _uiState.value = _uiState.value.copy(
                    showWallpaper = !_uiState.value.showWallpaper
                )
            }
        }
    }

}