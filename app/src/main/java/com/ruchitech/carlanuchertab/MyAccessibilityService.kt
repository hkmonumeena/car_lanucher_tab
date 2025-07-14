package com.ruchitech.carlanuchertab

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val myPackage = applicationContext.packageName
            if (it.packageName == myPackage) return  // Ignore own app's events

            Log.d("AccessibilityEvent", "Package: ${it.packageName}, Type: ${it.eventType}")

            if (it.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                val source = it.source
                source?.viewIdResourceName?.let { viewId ->
                    Log.d("ClickedView", "Clicked view ID: $viewId")

                    when (viewId) {
                        "com.other.app:id/button1" -> {
                            Log.d("Action", "Detected button1 click in another app")
                        }
                        // Add more IDs as needed
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w("AccessibilityService", "Service was interrupted")
    }

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        Log.d("AccessibilityService", "Service connected and configured")
    }
}
