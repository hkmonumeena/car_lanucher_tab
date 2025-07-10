package com.ruchitech.carlanuchertab.ui.screens.dashboard

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.carlanuchertab.ui.screens.dashboard.dashboard_home.DashboardViewModel

@Composable
fun WidgetPickerScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val appWidgetHost = remember { AppWidgetHost(context, viewModel.APPWIDGET_HOST_ID) }
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val configureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.showWidget(viewModel.currentAppWidgetId,context)
        } else {
            val appWidgetInfo =appWidgetManager
                .getAppWidgetInfo(viewModel.currentAppWidgetId)

            if (appWidgetInfo?.configure != null) {
                viewModel.showWidget(viewModel.currentAppWidgetId,context)
            }
        }
    }

    val pickWidgetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: return@rememberLauncherForActivityResult
            viewModel.currentAppWidgetId = appWidgetId

            val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

            if (appWidgetInfo?.configure != null) {
                val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = appWidgetInfo.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                configureLauncher.launch(configIntent)
            } else {
                viewModel.showWidget(appWidgetId,context)
            }
        }
    }

    LaunchedEffect(Unit) {
        appWidgetHost.startListening()
    }



    Scaffold {_ ->
        Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            Button(onClick = {
                val appWidgetId = appWidgetHost.allocateAppWidgetId()
                val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                pickWidgetLauncher.launch(pickIntent)
            }) {
                Text("Pick Widget")
            }
            viewModel.widgetItems.forEach { item ->
                AndroidView(
                    modifier = Modifier
                        .absoluteOffset(x = item.x.dp, y = item.y.dp)
                        .size(item.width.dp, item.height.dp),
                    factory = {
                        val hostView = appWidgetHost.createView(context, item.appWidgetId, appWidgetManager.getAppWidgetInfo(item.appWidgetId))
                        hostView.appWidgetId = item.appWidgetId
                        hostView
                    }
                )
            }
        }
        }

    }

}
