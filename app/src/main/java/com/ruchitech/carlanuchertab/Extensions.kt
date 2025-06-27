package com.ruchitech.carlanuchertab

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.util.Log

fun getActiveMediaMetadata(context: Context): MediaMetadata? {
    val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    val listenerComponent = ComponentName(context, YourNotificationListenerService::class.java)
    val controllers = mediaSessionManager.getActiveSessions(listenerComponent)


    for (controller in controllers) {
        val playbackState = controller.playbackState
        val metadata = controller.metadata
        if (playbackState != null &&
            playbackState.state == android.media.session.PlaybackState.STATE_PLAYING &&
            metadata != null
        ) {
            Log.d("MediaMetadata", "Now playing: ${metadata.description.title}")
            return metadata
        }
    }
    Log.e("glfjgifogkfgf", "getActiveMediaMetadata: ${controllers.size}", )
    return null
}

