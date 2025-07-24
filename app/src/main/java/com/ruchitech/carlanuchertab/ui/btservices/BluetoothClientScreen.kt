package com.ruchitech.carlanuchertab.ui.btservices

import android.Manifest
import android.R.attr.onClick
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BluetoothClientScreen(viewModel: BluetoothClientViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val devices = viewModel.pairedDevices

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                100
            )
        }
        viewModel.loadPairedDevices()
    }

    LazyColumn {
        item {
            Row(){
                Button(onClick = {
                    viewModel.sendCommand("openAccessibilitySettings")
                }) {
                    Text(text = "Open Accessebility Settings")
                }

                Button(onClick = {
                    viewModel.sendCommand("play/pause")
                }) {
                    Text(text = "music")
                }

                Button(onClick = {
                    viewModel.sendCommand("openFuelLog")
                }) {
                    Text(text = "Fuel")
                }
            }
        }
        items(devices) { device ->
            Text(
                text = "${device.name} (${device.address})",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.connectToDevice(device)
                    }
                    .padding(16.dp)
            )
        }
    }
}
