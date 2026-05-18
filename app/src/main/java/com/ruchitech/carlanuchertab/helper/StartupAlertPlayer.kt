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
        val lastEnqueuedTimestamp = prefs.getLong(KEY_LAST_ENQUEUED_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()

        Log.d(TAG, "scheduleIfNeeded called: bootCount=$bootCount, lastEnqueuedBootCount=$lastEnqueuedBootCount, lastEnqueuedTimestamp=$lastEnqueuedTimestamp, now=$now")

        val timeDiff = now - lastEnqueuedTimestamp
        val throttleDuration = 5 * 60_000L // 5 minutes in milliseconds

        // If boot count is valid (>= 0), check if it's the same boot AND within the throttle period
        if (bootCount >= 0) {
            if (bootCount <= lastEnqueuedBootCount && timeDiff < throttleDuration) {
                val remainingSeconds = (throttleDuration - timeDiff) / 1000
                Log.d(TAG, "Throttling: Same boot ($bootCount) and recently enqueued. Skipping scheduling. Try again in $remainingSeconds seconds.")
                return
            }
            // Update the boot count and timestamp
            prefs.edit()
                .putInt(KEY_LAST_ENQUEUED_BOOT_COUNT, bootCount)
                .putLong(KEY_LAST_ENQUEUED_TIMESTAMP, now)
                .apply()
            Log.d(TAG, "Updating prefs for boot: bootCount=$bootCount, timestamp=$now")
        } else {
            // Fallback for when boot count is unavailable
            if (timeDiff < throttleDuration) {
                val remainingSeconds = (throttleDuration - timeDiff) / 1000
                Log.d(TAG, "Throttling: Boot count unavailable, but enqueued recently. Skipping scheduling. Try again in $remainingSeconds seconds.")
                return
            }
            prefs.edit()
                .putLong(KEY_LAST_ENQUEUED_TIMESTAMP, now)
                .apply()
            Log.d(TAG, "Updating prefs for timestamp fallback: timestamp=$now")
        }

        Log.d(TAG, "Enqueuing StartupAlertWorker with delay of $STARTUP_DELAY_SECONDS seconds.")

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
        Log.i(TAG, "playNow: Attempting to play startup alert audio. Maximizing volume.")
        maximizeVolume(appContext)

        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                try {
                    Log.d(TAG, "playNow: Creating MediaPlayer for alert resource.")
                    val player = MediaPlayer.create(appContext, R.raw.alert)
                    if (player == null) {
                        Log.e(TAG, "playNow: MediaPlayer.create returned null. Alert audio resource R.raw.alert might be missing or corrupted.")
                        continuation.resume(false)
                        return@post
                    }

                    player.setVolume(1f, 1f)
                    player.setOnCompletionListener {
                        Log.i(TAG, "playNow: Alert audio playback completed successfully.")
                        it.release()
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }
                    player.setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "playNow: Alert playback failed: what=$what extra=$extra")
                        mp.release()
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                        true
                    }
                    continuation.invokeOnCancellation {
                        Log.d(TAG, "playNow: Job cancelled, releasing MediaPlayer.")
                        player.release()
                    }
                    Log.d(TAG, "playNow: Starting playback.")
                    player.start()
                } catch (e: Exception) {
                    Log.e(TAG, "playNow: Exception occurred while playing startup alert", e)
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
