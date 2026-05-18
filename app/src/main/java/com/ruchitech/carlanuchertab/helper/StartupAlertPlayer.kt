package com.ruchitech.carlanuchertab.helper

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ruchitech.carlanuchertab.R
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object StartupAlertPlayer {
    private const val PREFS_NAME = "startup_alert_prefs"
    private const val KEY_LAST_ENQUEUED_BOOT_COUNT = "last_enqueued_boot_count"
    private const val KEY_LAST_ENQUEUED_TIMESTAMP = "last_enqueued_timestamp"
    private const val UNIQUE_WORK_NAME = "startup_alert_work"
    private const val STARTUP_DELAY_SECONDS = 10L
    private const val TAG = "StartupAlertPlayer"

    @Synchronized
    fun scheduleIfNeeded(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val bootCount = readBootCount(appContext)
        val lastEnqueuedBootCount = prefs.getInt(KEY_LAST_ENQUEUED_BOOT_COUNT, -1)

        if (bootCount >= 0 && bootCount <= lastEnqueuedBootCount) {
            return
        }

        if (bootCount < 0) {
            val now = System.currentTimeMillis()
            val lastEnqueuedTimestamp = prefs.getLong(KEY_LAST_ENQUEUED_TIMESTAMP, 0L)
            if (now - lastEnqueuedTimestamp < 60_000L) {
                return
            }
            prefs.edit().putLong(KEY_LAST_ENQUEUED_TIMESTAMP, now).apply()
        } else {
            prefs.edit()
                .putInt(KEY_LAST_ENQUEUED_BOOT_COUNT, bootCount)
                .putLong(KEY_LAST_ENQUEUED_TIMESTAMP, System.currentTimeMillis())
                .apply()
        }

        val request = OneTimeWorkRequestBuilder<StartupAlertWorker>()
            .setInitialDelay(STARTUP_DELAY_SECONDS, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    suspend fun playNow(context: Context): Boolean {
        val appContext = context.applicationContext
        maximizeVolume(appContext)

        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                try {
                    val player = MediaPlayer.create(appContext, R.raw.alert)
                    if (player == null) {
                        continuation.resume(false)
                        return@post
                    }

                    player.setVolume(1f, 1f)
                    player.setOnCompletionListener {
                        it.release()
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }
                    player.setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "Alert playback failed: what=$what extra=$extra")
                        mp.release()
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                        true
                    }
                    continuation.invokeOnCancellation {
                        player.release()
                    }
                    player.start()
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to play startup alert", e)
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            }
        }
    }

    private fun maximizeVolume(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVolume, 0)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to maximize audio volume", e)
        }
    }

    private fun readBootCount(context: Context): Int {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT)
        } catch (e: Exception) {
            Log.w(TAG, "BOOT_COUNT unavailable", e)
            -1
        }
    }
}
