package com.ruchitech.carlanuchertab

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object ClickedViewPrefs {
    private const val PREF_NAME = "clicked_views_pref"
    private const val KEY_CLICKED_VIEWS = "clicked_views"

    fun addClickedView(context: Context, viewInfo: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_CLICKED_VIEWS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(viewInfo)
        prefs.edit().putStringSet(KEY_CLICKED_VIEWS, currentSet).apply()
    }

    fun getClickedViews(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_CLICKED_VIEWS, emptySet())?.toList() ?: emptyList()
    }

    fun clearClickedViews(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CLICKED_VIEWS).apply()
    }
}

object ClickedViewBus {
    private val _clickedViews = MutableSharedFlow<String>()
    val clickedViews = _clickedViews.asSharedFlow()

    suspend fun emit(viewId: String) {
        _clickedViews.emit(viewId)
    }
}


class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val myPackage = applicationContext.packageName
            if (it.packageName == myPackage) return // Ignore own app's events

            if (it.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                val source = it.source

                // Fallback for null source or missing view ID
                val viewId = source?.viewIdResourceName
                    ?: it.className?.toString()
                    ?: it.text?.joinToString(" ")?.takeIf { it.isNotBlank() }
                    ?: "unknown_click"

                Log.d("ClickedView", "Clicked View ID: $viewId")

                // Store + emit clicked view ID
                ClickedViewPrefs.addClickedView(this, viewId)
                CoroutineScope(Dispatchers.Default).launch {
                    ClickedViewBus.emit(viewId)
                }
            }
        }
    }



    override fun onInterrupt() {
        Log.w("AccessibilityService", "Service was interrupted")
    }

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        Log.d("AccessibilityService", "Service connected and configured")
    }


    private fun broadcastClickedView(context: Context, viewId: String) {
        val intent = Intent("${context.packageName}.CLICKED_VIEW")
        intent.putExtra("viewId", viewId)
        context.sendBroadcast(intent)
    }


}
