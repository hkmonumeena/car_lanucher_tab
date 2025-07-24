package com.ruchitech.carlanuchertab.ui.btservices

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothConnectionManager @Inject constructor() {

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    val incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val connectionState = MutableStateFlow(false)

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun connect(device: BluetoothDevice, uuid: UUID) {
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()
            socket?.connect()

            input = socket?.inputStream
            output = socket?.outputStream
            connectionState.value = true

            startListening()
        } catch (e: IOException) {
            connectionState.value = false
            Log.e("BT_Manager", "Connection failed: ${e.message}")
        }
    }

    fun send(command: String) {
        try {
            output?.write(command.toByteArray())
        } catch (e: IOException) {
            Log.e("BT_Manager", "Send failed: ${e.message}")
        }
    }

    private fun startListening() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val buffer = ByteArray(1024)
                while (true) {
                    val bytes = input?.read(buffer) ?: break
                    val message = String(buffer, 0, bytes)
                    incomingMessages.emit(message)
                }
            } catch (e: IOException) {
                Log.e("BT_Manager", "Read error: ${e.message}")
                connectionState.value = false
            }
        }
    }

    fun disconnect() {
        socket?.close()
        socket = null
        connectionState.value = false
    }
}
