package com.example.minidesk

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class VolumeController(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val maxVolume: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    var currentVolume by mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        private set

    fun update() {
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun setVolume(volume: Int) {
        val clamped = volume.coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
        currentVolume = clamped
    }

    fun adjustVolume(delta: Int) {
        setVolume(currentVolume + delta)
    }
}

class BrightnessController(private val context: Context) {

    var currentBrightness by mutableIntStateOf(getCurrentBrightnessFromSettings())
        private set

    fun canWriteSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    fun requestWriteSettingsPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun getCurrentBrightnessFromSettings(): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128
        }
    }

    fun update() {
        currentBrightness = getCurrentBrightnessFromSettings()
    }

    fun setBrightness(brightness: Int) {
        if (!canWriteSettings()) {
            requestWriteSettingsPermission()
            return
        }
        val clamped = brightness.coerceIn(0, 255)
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                clamped
            )
            currentBrightness = clamped
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun adjustBrightness(delta: Int) {
        setBrightness(currentBrightness + delta)
    }
}

class FlashlightController(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null

    var isTorchOn by mutableStateOf(false)
        private set

    init {
        try {
            val manager = cameraManager
            if (manager != null) {
                cameraId = manager.cameraIdList.firstOrNull { id ->
                    val characteristics = manager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
                } ?: manager.cameraIdList.firstOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTorch() {
        val id = cameraId ?: return
        val manager = cameraManager ?: return
        try {
            val newState = !isTorchOn
            manager.setTorchMode(id, newState)
            isTorchOn = newState
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class MediaController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var isLiveModeEnabled by mutableStateOf(false)

    fun isNotificationListenerGranted(): Boolean {
        return MiniDeskMediaListenerService.isNotificationListenerGranted(context)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    val liveTrackInfo: MediaTrackInfo
        get() = if (isLiveModeEnabled && isNotificationListenerGranted()) {
            MiniDeskMediaListenerService.liveTrackInfo
        } else {
            MediaTrackInfo()
        }

    fun togglePlayPause() {
        if (isLiveModeEnabled && isNotificationListenerGranted() && MiniDeskMediaListenerService.instance != null) {
            MiniDeskMediaListenerService.instance?.togglePlayPause()
        } else {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        }
    }

    fun nextTrack() {
        if (isLiveModeEnabled && isNotificationListenerGranted() && MiniDeskMediaListenerService.instance != null) {
            MiniDeskMediaListenerService.instance?.nextTrack()
        } else {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT)
            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        }
    }

    fun previousTrack() {
        if (isLiveModeEnabled && isNotificationListenerGranted() && MiniDeskMediaListenerService.instance != null) {
            MiniDeskMediaListenerService.instance?.previousTrack()
        } else {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        }
    }
}

class ScratchpadHelper(context: Context) {
    private val prefs = context.getSharedPreferences("minidesk_scratchpad", Context.MODE_PRIVATE)

    var noteText by mutableStateOf(prefs.getString("note", "") ?: "")
        private set

    fun updateNote(text: String) {
        noteText = text
        prefs.edit().putString("note", text).apply()
    }

    fun clear() {
        updateNote("")
    }
}
