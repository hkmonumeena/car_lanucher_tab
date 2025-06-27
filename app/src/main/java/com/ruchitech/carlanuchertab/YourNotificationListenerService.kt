package com.ruchitech.carlanuchertab

import android.service.notification.NotificationListenerService
import android.util.Log

class YourNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("MediaService", "Notification listener connected")
    }
}
