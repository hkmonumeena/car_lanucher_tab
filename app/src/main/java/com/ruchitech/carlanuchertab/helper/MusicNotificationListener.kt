package com.ruchitech.carlanuchertab.helper

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class NowPlayingInfo(
    val title: String? = null,
    val artist: String? = null,
    val artwork: Bitmap? = null,
)


class MusicNotificationListener : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var lastPackage: String? = null
    private var lastSongTitle: String? = null
    private var lastBroadcastTime = 0L
    private val debounceInterval = 1500L // 1.5 seconds

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // 🎯 Filter only Musicolet notifications
        if (packageName != "in.krosbits.musicolet") return
        // ⏳ Try to force access even if controller is null
        val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val controllers = mediaSessionManager.getActiveSessions(
            ComponentName(this, MusicNotificationListener::class.java)
        )

        if (controllers.isEmpty()) {
            Log.w("MusicNotification", "🎮 No active media controllers yet!")
            // Try fallback: use notification extras to populate info anyway
            val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
            val artist = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
            val info = NowPlayingInfo(title, artist, null)
            broadcastNowPlaying(info)
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras

        // 🔍 Dump all extras
        Log.e("NotificationExtras", "🎧 Notification from: $packageName")
        for (key in extras.keySet()) {
            val value = extras.get(key)
            Log.e("NotificationExtras", "$key => $value")
        }
        val title = extras.getString(Notification.EXTRA_TITLE)
        val artist = extras.getString(Notification.EXTRA_TEXT)
        val subText = extras.getString(Notification.EXTRA_SUB_TEXT)
        val bigText = extras.getString(Notification.EXTRA_BIG_TEXT)

        Log.d("MusicNotification", """
        🎵 Raw Notification Info:
        - Title: $title
        - Artist/Text: $artist
        - SubText: $subText
        - BigText: $bigText
    """.trimIndent())

        // 🕒 Debounce duplicate songs
        val currentTime = System.currentTimeMillis()
        if (title == lastSongTitle && (currentTime - lastBroadcastTime < debounceInterval)) {
            Log.d("MusicNotification", "⏱ Skipped duplicate update")
            return
        }

        lastSongTitle = title
        lastBroadcastTime = currentTime
        val controller = controllers.firstOrNull { it.packageName == packageName }
        controller?.let {
            val metadata = it.metadata
            val description = metadata?.description
            val songTitle = description?.title?.toString()
            val songArtist = description?.subtitle?.toString()
            val artwork = description?.iconBitmap ?: description?.iconBitmap
            broadcastNowPlaying(
                NowPlayingInfo(songTitle, songArtist, artwork)
            )
        }

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("MusicNotification", "Notification removed: ${sbn.packageName}")
    }

    private fun broadcastNowPlaying(info: NowPlayingInfo) {
        serviceScope.launch {
            val intent = Intent("now_playing_update").apply {
                setPackage(packageName)
                putExtra("title", info.title)
                putExtra("artist", info.artist)
                info.artwork?.let { bitmap ->
                    val file = File(cacheDir, "artwork.jpg")
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                    putExtra("artwork_path", file.absolutePath)
                    //   sendBroadcast(intent)
                    // Scale down the bitmap
                    /*     val scaledBitmap = bitmap.scale(256, 256)
                         val byteStream = ByteArrayOutputStream()
                         scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteStream)
                         putExtra("artwork", byteStream.toByteArray())*/
                }
            }
            sendBroadcast(intent)
        }
    }
}


