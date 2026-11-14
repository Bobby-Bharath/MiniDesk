package com.example.minidesk.widgets.common

import android.content.Context
import com.example.minidesk.widgets.audioroute.AudioRouteController
import com.example.minidesk.widgets.brightness.BrightnessController
import com.example.minidesk.widgets.toggles.QuickTogglesController
import com.example.minidesk.widgets.volume.VolumeController

class WidgetHub private constructor(context: Context) {
    val appContext: Context = context.applicationContext

    val volumeController = VolumeController(appContext)
    val brightnessController = BrightnessController(appContext)
    val audioRouteController = AudioRouteController(appContext)
    val quickTogglesController = QuickTogglesController(appContext)

    companion object {
        @Volatile
        private var instance: WidgetHub? = null

        fun get(context: Context): WidgetHub {
            return instance ?: synchronized(this) {
                instance ?: WidgetHub(context.applicationContext).also { instance = it }
            }
        }
    }
}
