package com.example.minidesk.widgets.brightness

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BrightnessController(private val context: Context) {

    var currentBrightness by mutableIntStateOf(getBrightnessFromSettings())
        private set

    var isAutoBrightness by mutableStateOf(getAutoModeFromSettings())
        private set

    private var maxObservedRange = 255

    init {
        val initial = getBrightnessFromSettings()
        if (initial > maxObservedRange) {
            maxObservedRange = initial
        }
        Log.d("MiniDeskDebug", "BrightnessController initialized: current=$initial, observedMax=$maxObservedRange")
    }

    fun canWriteSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    fun getGrantIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
    }

    private fun getBrightnessFromSettings(): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128
        }
    }

    private fun getAutoModeFromSettings(): Boolean {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE
            ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        } catch (e: Exception) {
            false
        }
    }

    fun update() {
        val valFromSystem = getBrightnessFromSettings()
        if (valFromSystem > maxObservedRange) {
            maxObservedRange = valFromSystem
        }
        currentBrightness = valFromSystem
        isAutoBrightness = getAutoModeFromSettings()
    }

    fun setBrightnessFraction(fraction: Float) {
        if (!canWriteSettings()) return

        val clampedPx = (fraction * maxObservedRange).toInt().coerceIn(1, maxObservedRange)
        try {
            // Switch to manual mode first
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                clampedPx
            )
            currentBrightness = clampedPx
            isAutoBrightness = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleAutoBrightness() {
        if (!canWriteSettings()) return

        val targetMode = if (isAutoBrightness) {
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        } else {
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        }

        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                targetMode
            )
            isAutoBrightness = targetMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerObserver(onChanged: () -> Unit): ContentObserver {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                update()
                onChanged()
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
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

    val currentFraction: Float
        get() = (currentBrightness.toFloat() / maxObservedRange.toFloat()).coerceIn(0f, 1f)
}
