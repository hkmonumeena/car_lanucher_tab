package com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard_home

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.WidgetItem
import com.ruchitech.carlanuchertab.helper.MusicNotificationListener
import com.ruchitech.carlanuchertab.helper.NowPlayingInfo
import com.ruchitech.carlanuchertab.helper.VoiceCommandHelper
import com.ruchitech.carlanuchertab.roomdb.action.AppDatabase
import com.ruchitech.carlanuchertab.roomdb.dao.DashboardDao
import com.ruchitech.carlanuchertab.roomdb.data.Dashboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    lateinit var mediaSessionManager: MediaSessionManager
    lateinit var componentName: ComponentName
    private val _nowPlaying = mutableStateOf(NowPlayingInfo())
    val nowPlaying: State<NowPlayingInfo> = _nowPlaying

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    private var lastSkipTime = 0L
    private val DEBOUNCE_INTERVAL_MS = 800L // 800ms between swipes
    private val _playbackPosition = mutableStateOf(0L)
    val playbackPosition: State<Long> = _playbackPosition
    private val _playbackDuration = mutableStateOf(0L)
    val playbackDuration: State<Long> = _playbackDuration
    fun updateNowPlaying(info: NowPlayingInfo) {
        _nowPlaying.value = info
        startPlaybackMonitor()
    }


    fun togglePlayPause() {
        getMusicoletController()?.let { controller ->
            val playbackState = controller.playbackState?.state
            val now = SystemClock.elapsedRealtime()
            val position =
                controller.playbackState!!.position + ((now - controller.playbackState!!.lastPositionUpdateTime) * controller.playbackState!!.playbackSpeed).toLong()
            Log.d("gldfjhgjfghfiugh", "Current song position: $position ms")
            if (playbackState == PlaybackState.STATE_PLAYING) {
                controller.transportControls.pause()
                updatePlaybackState()
                Log.d("MediaControl", "⏸ Paused")
            } else {
                controller.transportControls.play()
                updatePlaybackState()
                Log.d("MediaControl", "▶️ Playing")
            }
        }
    }

    fun skipToPrevious() {
        val now = System.currentTimeMillis()
        if (now - lastSkipTime < DEBOUNCE_INTERVAL_MS) {
            Log.d("MediaControl", "⏮ Skipped due to debounce")
            return
        }

        lastSkipTime = now
        getMusicoletController()?.transportControls?.skipToPrevious()
        Log.d("MediaControl", "⏮ Previous triggered")
    }

    fun skipToNext() {
        val now = System.currentTimeMillis()
        if (now - lastSkipTime < DEBOUNCE_INTERVAL_MS) {
            Log.d("MediaControl", "⏭ Skipped due to debounce")
            return
        }

        lastSkipTime = now
        getMusicoletController()?.transportControls?.skipToNext()
        Log.d("MediaControl", "⏭ Next triggered")
    }

    fun updatePlaybackPosition(position: Long) {
        _playbackPosition.value = position
    }

    fun updatePlaybackDuration(duration: Long) {
        _playbackDuration.value = duration
    }

    fun seekTo(position: Long) {
        val controller = getMusicoletController() ?: return
        val max = controller.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: return
        val safePos = position.coerceIn(0, max)
        Log.d("MediaControl", "SeekTo requested: $safePos ms (clamped)")
        controller.transportControls.seekTo(safePos)
    }


    fun startPlaybackMonitor() {
        viewModelScope.launch {
            val controller = getMusicoletController() ?: return@launch

            while (true) {
                val state = controller.playbackState ?: break
                if (state.state == PlaybackState.STATE_PLAYING) {
                    val now = SystemClock.elapsedRealtime()
                    val pos =
                        state.position + ((now - state.lastPositionUpdateTime) * state.playbackSpeed).toLong()
                    updatePlaybackPosition(pos)
                } else {
                    // Just use the last known position (don't calculate with time)
                    updatePlaybackPosition(state.position)
                }

                // Always update duration (it doesn't change much, but just in case)
                val duration =
                    controller.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
                updatePlaybackDuration(duration)
                updatePlaybackStateOldObj(state.state)

                delay(500L)
            }
        }
    }


    private fun getMusicoletController(): MediaController? {
        val controllers = mediaSessionManager.getActiveSessions(componentName)

        return controllers.firstOrNull { it.packageName == "in.krosbits.musicolet" }
    }

    fun updatePlaybackState() {
        val controller = getMusicoletController()
        val isNowPlaying = controller?.playbackState?.state == PlaybackState.STATE_PLAYING
        _isPlaying.value = isNowPlaying
    }

    fun updatePlaybackStateOldObj(state: Int) {
        val isNowPlaying = state == PlaybackState.STATE_PLAYING
        _isPlaying.value = isNowPlaying
    }

    fun initData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaSessionManager =
                context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            componentName = ComponentName(context, MusicNotificationListener::class.java)
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
        viewModelScope.launch(Dispatchers.IO) {
            val dashboard = dashboardDao.getDashboard()
            if (dashboard != null) {
                dashboardDao.updateDashboard(dashboard.copy(widgets = items))
            } else {
                dashboardDao.insertOrUpdateDashboard(Dashboard(widgets = items))
            }
        }
    }


}