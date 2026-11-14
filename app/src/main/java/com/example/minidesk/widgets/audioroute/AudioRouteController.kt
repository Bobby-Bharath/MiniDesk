package com.example.minidesk.widgets.audioroute

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AudioRouteController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var currentRouteName by mutableStateOf("Phone Speaker")
        private set

    var currentRouteType by mutableStateOf("Speaker")
        private set

    fun updateCurrentOutput() {
        try {
            val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

            var bestDevice: AudioDeviceInfo? = null
            // Priority: BLE headset -> Bluetooth A2DP -> USB headset -> Wired headphones -> Wired headset -> Speaker
            for (typePriority in listOf(
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_USB_DEVICE,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET
            )) {
                val found = outputs.firstOrNull { it.type == typePriority }
                if (found != null) {
                    bestDevice = found
                    break
                }
            }

            if (bestDevice != null) {
                val name = bestDevice.productName?.toString()
                currentRouteName = if (!name.isNullOrBlank()) name else when (bestDevice.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> "Bluetooth Audio"
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
                    AudioDeviceInfo.TYPE_USB_HEADSET,
                    AudioDeviceInfo.TYPE_USB_DEVICE -> "USB Audio"
                    else -> "External Device"
                }
                currentRouteType = when (bestDevice.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> "Bluetooth"
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired"
                    AudioDeviceInfo.TYPE_USB_HEADSET,
                    AudioDeviceInfo.TYPE_USB_DEVICE -> "USB"
                    else -> "External"
                }
            } else {
                currentRouteName = "Phone Speaker"
                currentRouteType = "Speaker"
            }
        } catch (e: Exception) {
            currentRouteName = "Phone Speaker"
            currentRouteType = "Speaker"
        }
    }

    fun openVolumePanel(onRequestDismiss: () -> Unit) {
        onRequestDismiss()
        try {
            val intent = Intent("android.settings.panel.action.VOLUME").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun registerDeviceCallback(onChanged: () -> Unit): AudioDeviceCallback {
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                updateCurrentOutput()
                onChanged()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                updateCurrentOutput()
                onChanged()
            }
        }
        try {
            audioManager.registerAudioDeviceCallback(callback, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateCurrentOutput()
        return callback
    }

    fun unregisterDeviceCallback(callback: AudioDeviceCallback) {
        try {
            audioManager.unregisterAudioDeviceCallback(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
