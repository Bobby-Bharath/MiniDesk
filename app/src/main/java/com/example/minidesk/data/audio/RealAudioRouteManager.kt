package com.example.minidesk.data.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioOutputDevice(
    val id: Int,
    val name: String,
    val isBluetooth: Boolean
)

class RealAudioRouteManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

    private val _availableDevices = MutableStateFlow<List<AudioOutputDevice>>(emptyList())
    val availableDevices: StateFlow<List<AudioOutputDevice>> = _availableDevices.asStateFlow()

    private val _activeDeviceName = MutableStateFlow("This phone")
    val activeDeviceName: StateFlow<String> = _activeDeviceName.asStateFlow()

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) { scanDevices() }
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) { scanDevices() }
    }

    init {
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
        scanDevices()
    }

    fun scanDevices() {
        try {
            val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val list = mutableListOf<AudioOutputDevice>()

            // Bluetooth Media Headset (A2DP / BLE)
            val bt = outputs.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER
            }
            if (bt != null) {
                val raw = bt.productName?.toString()?.trim()
                val cleanName = if (raw.isNullOrBlank() || raw == Build.MODEL || raw.contains("V2218", ignoreCase = true)) "Bluetooth Audio" else raw
                list.add(AudioOutputDevice(bt.id, cleanName, isBluetooth = true))
            }

            // Built-in Speaker
            list.add(AudioOutputDevice(100, "This phone", isBluetooth = false))
            _availableDevices.value = list

            // Detect Active Device
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
                val activeMedia = audioManager.getAudioDevicesForAttributes(attrs)
                val activeBt = activeMedia.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || it.type == AudioDeviceInfo.TYPE_BLE_HEADSET }
                _activeDeviceName.value = if (activeBt && bt != null) list.first().name else "This phone"
            } else {
                _activeDeviceName.value = if (bt != null) list.first().name else "This phone"
            }
        } catch (e: Exception) {
            Log.e("MiniDeskAudio", "Error scanning audio", e)
        }
    }

    /**
     * Triggers Android SystemUI's native MediaOutputDialog via EXACTLY ONE Broadcast.
     */
    fun openSystemMediaSwitcher() {
        Log.d("MiniDeskAudio", ">>> Dispatching single SystemUI MediaOutputDialog broadcast")
        val targetPkg = getActiveMediaPackage()
        val intent = if (targetPkg != null) {
            Intent("com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG").apply {
                setPackage("com.android.systemui")
                putExtra("package_name", targetPkg)
            }
        } else {
            Intent("com.android.systemui.action.LAUNCH_SYSTEM_MEDIA_OUTPUT_DIALOG").apply {
                setPackage("com.android.systemui")
            }
        }
        try {
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e("MiniDeskAudio", "Error sending SystemUI broadcast", e)
        }
    }

    private fun getActiveMediaPackage(): String? {
        return try {
            // If there is an active session, use its package name
            val sessions = mediaSessionManager?.getActiveSessions(null)
            sessions?.firstOrNull()?.packageName
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun release() {
        audioManager.unregisterAudioDeviceCallback(deviceCallback)
    }
}
