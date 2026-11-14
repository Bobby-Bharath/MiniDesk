package com.example.minidesk.widgets.volume

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class VolumeController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val maxMedia: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val maxRing: Int
        get() = try { audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) } catch (e: Exception) { 15 }
    val maxAlarm: Int
        get() = try { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) } catch (e: Exception) { 15 }

    var mediaVol by mutableIntStateOf(getStreamVol(AudioManager.STREAM_MUSIC))
        private set
    var ringVol by mutableIntStateOf(getStreamVol(AudioManager.STREAM_RING))
        private set
    var alarmVol by mutableIntStateOf(getStreamVol(AudioManager.STREAM_ALARM))
        private set

    private var savedMediaVol = mediaVol

    private fun getStreamVol(stream: Int): Int {
        return try {
            audioManager.getStreamVolume(stream)
        } catch (e: Exception) {
            0
        }
    }

    fun updateAll() {
        mediaVol = getStreamVol(AudioManager.STREAM_MUSIC)
        ringVol = getStreamVol(AudioManager.STREAM_RING)
        alarmVol = getStreamVol(AudioManager.STREAM_ALARM)
    }

    fun setMediaFraction(fraction: Float) {
        val max = maxMedia
        val index = (fraction * max).toInt().coerceIn(0, max)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
            mediaVol = index
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setRingFraction(fraction: Float) {
        val max = maxRing
        val index = (fraction * max).toInt().coerceIn(0, max)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_RING, index, 0)
            ringVol = index
        } catch (e: SecurityException) {
            // Fallback for Do Not Disturb policy
            try {
                if (index == 0) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAlarmFraction(fraction: Float) {
        val max = maxAlarm
        val index = (fraction * max).toInt().coerceIn(0, max)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, index, 0)
            alarmVol = index
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleMuteMedia() {
        if (mediaVol > 0) {
            savedMediaVol = mediaVol
            setMediaFraction(0f)
        } else {
            val restore = if (savedMediaVol > 0) savedMediaVol.toFloat() / maxMedia else 0.5f
            setMediaFraction(restore)
        }
    }

    fun registerObserver(onChanged: () -> Unit): ContentObserver {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                updateAll()
                onChanged()
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return observer
    }

    fun unregisterObserver(observer: ContentObserver) {
        try {
            context.contentResolver.unregisterContentObserver(observer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
