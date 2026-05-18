package com.ruchitech.carlanuchertab.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.provider.Settings
import com.ruchitech.carlanuchertab.MyAccessibilityService
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getActiveMediaMetadata(context: Context): MediaMetadata? {
    val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    /*
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
        Log.e("glfjgifogkfgf", "getActiveMediaMetadata: ${controllers.size}", )*/

    return null
}

fun getCurrentDateFormatted(): String {
    val dateFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
    return dateFormat.format(Date())
}


fun createFuelLogEntry(
    rupeeInput: Int?,
    fuelPriceInput: Float?,
    litersInput: Float?,
    location: String? = null,
    loggedAtEpochMs: Long = System.currentTimeMillis(),
): FuelLog? {
    if (fuelPriceInput != null && fuelPriceInput <= 0f) return null
    if (litersInput != null && litersInput <= 0f) return null

    var rupee = rupeeInput
    var liters = litersInput
    var price = fuelPriceInput

    fun roundTwo(v: Float): Float = kotlin.math.round(v * 100f) / 100f

    when {
        // Case 1: Calculate liters
        rupee != null && price != null && liters == null -> {
            liters = rupee / price
        }

        // Case 2: Calculate rupees
        liters != null && price != null && rupee == null -> {
            rupee = kotlin.math.round(liters * price).toInt()
        }

        // Case 3: Calculate price
        liters != null && rupee != null && price == null -> {
            price = rupee / liters
        }

        rupee == null -> return null
    }

    if (price != null && price <= 0f) return null
    if (liters != null && liters <= 0f) return null

    liters = liters?.let(::roundTwo)
    price = price?.let(::roundTwo)

    return FuelLog(
        rupee = rupee ?: 0,
        liters = liters,
        fuelPrice = price,
        location = location,
        loggedAtEpochMs = loggedAtEpochMs,
    )
}


fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false
    val expectedComponent =
        ComponentName(context, MusicNotificationListener::class.java).flattenToString()

    return enabledListeners.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
}

fun openNotificationAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

fun isAccessibilityEnabled(context: Context): Boolean {
    val accessibilityEnabled = Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED,
        0
    ) == 1

    if (accessibilityEnabled) {
        val services = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val expectedComponent =
            ComponentName(context, MyAccessibilityService::class.java).flattenToString()
        return services.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
    }
    return false
}

fun enableAccessibilityService(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

