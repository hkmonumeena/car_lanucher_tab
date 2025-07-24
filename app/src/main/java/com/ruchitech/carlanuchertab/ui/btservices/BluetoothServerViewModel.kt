package com.ruchitech.carlanuchertab.ui.btservices

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchitech.carlanuchertab.ClickedViewBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class BluetoothServerViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val bluetoothAdapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()
    private val serverUUID: UUID =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    private var serverSocket: BluetoothServerSocket? = null

    fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    "MyBTServer", serverUUID
                )
                val socket = serverSocket?.accept()

                socket?.let {
                    val input = it.inputStream
                    val output = it.outputStream

                    val buffer = ByteArray(1024)
                    while (true) {
                        val bytes = input.read(buffer)
                        val received = String(buffer, 0, bytes)
                        Log.d("BluetoothServer", "Received: $received")
                        ClickedViewBus.emit("openAccessibilitySettings")

                        val response = "Echo: $received"
                        output.write(response.toByteArray())
                    }
                }
            } catch (e: IOException) {
                Log.e("BluetoothServer", "Error: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        serverSocket?.close()
    }
}
