package com.ruchitech.carlanuchertab.ui.btservices

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.system.Os.socket
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BluetoothClientViewModel @Inject constructor(
    application: Application,
    private val bluetoothManager: BluetoothConnectionManager,
) : AndroidViewModel(application) {

    private val bluetoothAdapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()
    private val serverUUID: UUID =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    private val _pairedDevices = mutableStateListOf<BluetoothDevice>()
    val pairedDevices: List<BluetoothDevice> = _pairedDevices
    private var socket: BluetoothSocket? = null
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun loadPairedDevices() {
        val devices = bluetoothAdapter?.bondedDevices
        devices?.forEach {
            _pairedDevices.add(it)
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                 socket = device?.createRfcommSocketToServiceRecord(serverUUID)
                bluetoothAdapter?.cancelDiscovery()
                socket?.connect()

                val output = socket?.outputStream
                val input = socket?.inputStream

                output?.write("Hello from Mobile".toByteArray())

                val buffer = ByteArray(1024)
                val bytes = input?.read(buffer)
                bytes?.let {
                    val response = String(buffer, 0, bytes)
                    Log.d("BluetoothClient", "Response: $response")
                }

            } catch (e: IOException) {
                Log.e("BluetoothClient", "Connection failed: ${e.message}")
            }
        }
    }

    fun sendCommand(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                socket?.outputStream?.write(command.toByteArray())
                Log.d("BluetoothClient", "Sent command: $command")
            } catch (e: IOException) {
                Log.e("BluetoothClient", "Error sending command: ${e.message}")
            }
        }
    }

}
