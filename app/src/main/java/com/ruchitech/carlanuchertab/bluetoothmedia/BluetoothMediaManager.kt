package com.ruchitech.carlanuchertab.bluetoothmedia

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.ruchitech.carlanuchertab.helper.MusicNotificationListener
import com.ruchitech.carlanuchertab.helper.isNotificationListenerEnabled
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PairedBluetoothDevice(
    val name: String,
    val address: String,
    val bonded: Boolean,
    val connected: Boolean,
)

data class BluetoothMediaUiState(
    val bluetoothEnabled: Boolean = false,
    val discovering: Boolean = false,
    val connecting: Boolean = false,
    val connectedDeviceName: String? = null,
    val connectedDeviceAddress: String? = null,
    val mruDeviceName: String? = null,
    val mruDeviceAddress: String? = null,
    val devices: List<PairedBluetoothDevice> = emptyList(),
    val notificationAccessEnabled: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val artwork: Bitmap? = null,
    val durationMs: Long = 0L,
    val progressMs: Long = 0L,
    val playing: Boolean = false,
    val status: String = "Bluetooth media idle",
)

@Singleton
class BluetoothMediaManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs = appContext.getSharedPreferences("paired_device_media", Context.MODE_PRIVATE)
    private val bluetoothAdapter: BluetoothAdapter? =
        (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val mediaSessionManager =
        appContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    private var a2dpProxy: BluetoothA2dp? = null
    private var activeController: MediaController? = null
    private var receiverRegistered = false
    private val discoveredDevices = linkedMapOf<String, PairedBluetoothDevice>()

    private val _state = MutableStateFlow(
        BluetoothMediaUiState(
            bluetoothEnabled = bluetoothAdapter?.isEnabled == true,
            mruDeviceName = prefs.getString(KEY_MRU_NAME, null),
            mruDeviceAddress = prefs.getString(KEY_MRU_ADDRESS, null),
            notificationAccessEnabled = isNotificationListenerEnabled(appContext),
        )
    )
    val state: StateFlow<BluetoothMediaUiState> = _state.asStateFlow()

    init {
        registerReceivers()
        bindA2dpProfile()
        startProgressTicker()
        refreshDevices()
        refreshMedia()
        reconnectMostRecent()
    }

    @SuppressLint("MissingPermission")
    fun refreshDevices() {
        val adapter = bluetoothAdapter
        val bonded = adapter?.bondedDevices.orEmpty()
        val connectedAddresses = connectedA2dpDevices().map { it.address }.toSet()
        val bondedDevices = bonded
            .map {
                PairedBluetoothDevice(
                    name = it.safeName(),
                    address = it.address,
                    bonded = it.bondState == BluetoothDevice.BOND_BONDED,
                    connected = it.address in connectedAddresses,
                )
            }
            .sortedWith(compareByDescending<PairedBluetoothDevice> { it.connected }.thenBy { it.name.lowercase() })
        val bondedAddresses = bondedDevices.map { it.address }.toSet()
        val nearbyDevices = discoveredDevices.values
            .filterNot { it.address in bondedAddresses }
            .sortedBy { it.name.lowercase() }
        val devices = bondedDevices + nearbyDevices
        val connected = devices.firstOrNull { it.connected }
        if (connected != null) saveMru(connected.name, connected.address)
        _state.value = _state.value.copy(
            bluetoothEnabled = adapter?.isEnabled == true,
            devices = devices,
            connectedDeviceName = connected?.name,
            connectedDeviceAddress = connected?.address,
            notificationAccessEnabled = isNotificationListenerEnabled(appContext),
            connecting = false,
            status = when {
                adapter?.isEnabled != true -> "Bluetooth is off"
                connected != null -> "Connected to ${connected.name}"
                devices.isEmpty() -> "No paired devices"
                else -> "Waiting for system Bluetooth connection"
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (!hasBluetoothPermission()) {
            _state.value = _state.value.copy(status = "Bluetooth permission required")
            return
        }
        runCatching {
            if (adapter.isDiscovering) adapter.cancelDiscovery()
            discoveredDevices.clear()
            adapter.startDiscovery()
            _state.value = _state.value.copy(discovering = true, status = "Searching for nearby phones")
        }.onFailure {
            _state.value = _state.value.copy(status = it.message ?: "Discovery failed")
        }
    }

    @SuppressLint("MissingPermission")
    fun pairOrReconnect(device: PairedBluetoothDevice) {
        val remote = bluetoothAdapter?.getRemoteDevice(device.address) ?: return
        _state.value = _state.value.copy(connecting = true, status = "Connecting ${device.name}")
        saveMru(device.name, device.address)
        if (remote.bondState != BluetoothDevice.BOND_BONDED) {
            runCatching { remote.createBond() }
                .onFailure { _state.value = _state.value.copy(connecting = false, status = it.message ?: "Pairing failed") }
            return
        }
        reconnectDevice(remote)
    }

    fun reconnectMostRecent() {
        val address = prefs.getString(KEY_MRU_ADDRESS, null) ?: return
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return
        reconnectDevice(device)
    }

    fun forgetMostRecent() {
        prefs.edit().remove(KEY_MRU_ADDRESS).remove(KEY_MRU_NAME).apply()
        _state.value = _state.value.copy(
            mruDeviceName = null,
            mruDeviceAddress = null,
            status = "Forgot saved device preference"
        )
        refreshDevices()
    }

    fun openBluetoothSettings() {
        appContext.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun openNotificationAccessSettings() {
        appContext.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun playPause() {
        activeController()?.transportControls?.let { controls ->
            if (_state.value.playing) controls.pause() else controls.play()
        } ?: updateStatus("No active media session")
    }

    fun next() {
        activeController()?.transportControls?.skipToNext() ?: updateStatus("No active media session")
    }

    fun previous() {
        activeController()?.transportControls?.skipToPrevious() ?: updateStatus("No active media session")
    }

    fun refreshMedia() {
        val controller = activeController()
        val metadata = controller?.metadata
        val playback = controller?.playbackState
        _state.value = _state.value.copy(
            notificationAccessEnabled = isNotificationListenerEnabled(appContext),
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
            album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM),
            artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART),
            durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L,
            progressMs = playback?.estimatedPosition()?.coerceAtLeast(0L) ?: 0L,
            playing = playback?.state == PlaybackState.STATE_PLAYING,
        )
    }

    private fun reconnectDevice(device: BluetoothDevice) {
        if (!hasBluetoothPermission()) {
            _state.value = _state.value.copy(connecting = false, status = "Bluetooth permission required")
            return
        }
        scope.launch {
            bindA2dpProfile()
            delay(500)
            runCatching {
                val proxy = a2dpProxy
                if (proxy != null) {
                    val connect = proxy.javaClass.getMethod("connect", BluetoothDevice::class.java)
                    connect.invoke(proxy, device)
                    _state.value = _state.value.copy(status = "Requested system reconnect to ${device.safeName()}")
                } else {
                    _state.value = _state.value.copy(status = "Waiting for system Bluetooth reconnect")
                }
            }.onFailure {
                _state.value = _state.value.copy(status = "Waiting for system Bluetooth reconnect")
            }
            delay(1200)
            refreshDevices()
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectedA2dpDevices(): List<BluetoothDevice> {
        return runCatching { a2dpProxy?.connectedDevices.orEmpty() }.getOrDefault(emptyList())
    }

    private fun activeController(): MediaController? {
        if (!isNotificationListenerEnabled(appContext)) return null
        val component = ComponentName(appContext, MusicNotificationListener::class.java)
        val controllers = runCatching { mediaSessionManager.getActiveSessions(component) }.getOrDefault(emptyList())
        activeController = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull()
        return activeController
    }

    private fun registerReceivers() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        ContextCompat.registerReceiver(appContext, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        receiverRegistered = true
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> _state.value = _state.value.copy(discovering = true)
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> _state.value = _state.value.copy(discovering = false)
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_BOND_STATE_CHANGED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                BluetoothAdapter.ACTION_STATE_CHANGED -> refreshDevices()
                BluetoothDevice.ACTION_FOUND -> {
                    intent.bluetoothDevice()?.let { device ->
                        discoveredDevices[device.address] = PairedBluetoothDevice(
                            name = device.safeName(),
                            address = device.address,
                            bonded = device.bondState == BluetoothDevice.BOND_BONDED,
                            connected = false,
                        )
                    }
                    refreshDevices()
                }
            }
        }
    }

    private fun bindA2dpProfile() {
        val adapter = bluetoothAdapter ?: return
        runCatching {
            adapter.getProfileProxy(appContext, profileListener, BluetoothProfile.A2DP)
        }
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProxy = proxy as? BluetoothA2dp
                refreshDevices()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProxy = null
                refreshDevices()
            }
        }
    }

    private fun startProgressTicker() {
        scope.launch {
            while (true) {
                refreshMedia()
                delay(1000)
            }
        }
    }

    private fun saveMru(name: String, address: String) {
        prefs.edit().putString(KEY_MRU_NAME, name).putString(KEY_MRU_ADDRESS, address).apply()
        _state.value = _state.value.copy(mruDeviceName = name, mruDeviceAddress = address)
    }

    private fun updateStatus(status: String) {
        _state.value = _state.value.copy(status = status)
    }

    private fun PlaybackState.estimatedPosition(): Long {
        if (state != PlaybackState.STATE_PLAYING) return position
        val elapsed = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        return position + (elapsed * playbackSpeed).toLong()
    }

    private fun BluetoothDevice.safeName(): String = runCatching { name }.getOrNull()?.takeIf { it.isNotBlank() } ?: address

    private fun hasBluetoothPermission(): Boolean {
        return Build.VERSION.SDK_INT < 31 ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    @Suppress("DEPRECATION")
    private fun Intent.bluetoothDevice(): BluetoothDevice? {
        return if (Build.VERSION.SDK_INT >= 33) {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
    }

    companion object {
        private const val KEY_MRU_NAME = "mru_name"
        private const val KEY_MRU_ADDRESS = "mru_address"
    }
}
