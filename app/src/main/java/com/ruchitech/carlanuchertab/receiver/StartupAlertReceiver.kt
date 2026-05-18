package com.ruchitech.carlanuchertab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ruchitech.carlanuchertab.helper.StartupAlertPlayer

class StartupAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        StartupAlertPlayer.scheduleIfNeeded(context.applicationContext)
    }
}
