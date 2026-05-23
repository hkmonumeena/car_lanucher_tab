package com.ruchitech.carlaunchercompanion

import android.content.Context
import android.net.Uri
import com.ruchitech.phonelink.protocol.PhoneLinkDefaults
import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PhoneStreamServer(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var sourceUri: Uri? = null

    fun setSource(uri: Uri) {
        sourceUri = uri
    }

    fun start() {
        if (serverSocket?.isClosed == false) return
        scope.launch {
            runCatching {
                serverSocket = ServerSocket(PhoneLinkDefaults.PHONE_STREAM_PORT)
                while (serverSocket?.isClosed == false) {
                    serverSocket?.accept()?.let { socket ->
                        launch { serve(socket) }
                    }
                }
            }
        }
    }

    private fun serve(socket: Socket) {
        socket.use { client ->
            val uri = sourceUri ?: return
            val type = context.contentResolver.getType(uri) ?: "audio/mpeg"
            val output = client.getOutputStream()
            val headers = "HTTP/1.1 200 OK\r\nContent-Type: $type\r\nConnection: close\r\n\r\n"
            output.write(headers.toByteArray())
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.copyTo(output)
            }
            output.flush()
        }
    }
}
