package com.ruchitech.carlanuchertab.helper

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import com.ruchitech.carlanuchertab.YourNotificationListenerService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getActiveMediaMetadata(context: Context): MediaMetadata? {
    val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    val listenerComponent = ComponentName(context, YourNotificationListenerService::class.java)
    val controllers = mediaSessionManager.getActiveSessions(listenerComponent)


    for (controller in controllers) {
        val playbackState = controller.playbackState
        val metadata = controller.metadata
        if (playbackState != null &&
            playbackState.state == PlaybackState.STATE_PLAYING &&
            metadata != null
        ) {
            Log.d("MediaMetadata", "Now playing: ${metadata.description.title}")
            return metadata
        }
    }
    Log.e("glfjgifogkfgf", "getActiveMediaMetadata: ${controllers.size}", )
    return null
}

fun getCurrentDateFormatted(): String {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    return dateFormat.format(Date())
}