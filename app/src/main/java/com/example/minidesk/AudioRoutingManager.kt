package com.example.minidesk

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class AudioOutputDevice(
    val id: Int,
    val type: Int,
    val name: String,
    val rawDevice: AudioDeviceInfo?
)

data class ActiveSoundApp(
    val appName: String,
    val icon: Drawable? = null
)

class AudioRoutingManager(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var outputDevices by mutableStateOf<List<AudioOutputDevice>>(emptyList())
        private set

    var activeDeviceType by mutableStateOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        private set

    var activeSoundApp by mutableStateOf<ActiveSoundApp?>(null)
        private set

    fun refreshDevices() {
        val list = mutableListOf<AudioOutputDevice>()
        list.add(AudioOutputDevice(-1, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, "Phone Speaker", null))
        try {
            val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in outputs) {
                if (device.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    val name = device.productName?.toString() ?: "Audio Device"
                    list.add(AudioOutputDevice(device.id, device.type, name, device))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        outputDevices = list
    }

    fun refreshActiveSoundSource() {}

    fun selectDevice(targetDevice: AudioOutputDevice) {
        activeDeviceType = targetDevice.type
    }

    fun openSystemOutputSwitcher() {}

    fun getActiveDeviceName(): String {
        return outputDevices.firstOrNull { it.type == activeDeviceType }?.name ?: "Phone Speaker"
    }
}
