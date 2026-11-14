package com.example.minidesk.deck

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Invisible activity that runs the system's widget-adding handshake, because binding permission
 * and widget configuration screens can only be started from an Activity, never from the overlay.
 * It opens, shows the system dialog if one is needed, adds the widget to the Deck and closes.
 */
class WidgetPickerActivity : Activity() {
    private lateinit var hostManager: WidgetHostManager
    private lateinit var provider: ComponentName
    private var appWidgetId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostManager = WidgetHostManager.get(this)

        val flat = intent.getStringExtra(EXTRA_PROVIDER)
        val parsed = flat?.let { ComponentName.unflattenFromString(it) }
        if (parsed == null) { finish(); return }
        provider = parsed

        if (savedInstanceState != null) {
            // Recreated while a system dialog was on screen: the result will still be delivered.
            appWidgetId = savedInstanceState.getInt(STATE_ID, -1)
            if (appWidgetId != -1) return
        }

        appWidgetId = hostManager.allocateId()
        if (hostManager.bindIfAllowed(appWidgetId, provider)) {
            afterBind()
        } else {
            // First time for this provider: the system asks the user for permission.
            val bind = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
            @Suppress("DEPRECATION")
            startActivityForResult(bind, REQ_BIND)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_ID, appWidgetId)
    }

    private fun afterBind() {
        val info = hostManager.info(appWidgetId)
        if (info == null) { cancel(); return }
        if (info.configure != null) {
            try {
                hostManager.host.startAppWidgetConfigureActivityForResult(this, appWidgetId, 0, REQ_CONFIG, null)
            } catch (e: Exception) {
                cancel()
            }
        } else {
            finishWithWidget()
        }
    }

    @Deprecated("Activity result API is not available on plain Activity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_BIND -> if (resultCode == RESULT_OK) afterBind() else cancel()
            REQ_CONFIG -> if (resultCode == RESULT_OK) finishWithWidget() else cancel()
            else -> cancel()
        }
    }

    private fun finishWithWidget() {
        val info = hostManager.info(appWidgetId)
        if (info == null) { cancel(); return }
        val sizing = hostManager.sizing(info, DeckGrid.cellDp)
        WidgetRepository.get(this).addAppWidget(appWidgetId, provider.flattenToString(), sizing.defW, sizing.defH)
        finish()
    }

    /** Releases the allocated id so cancelled adds do not leak widget slots. */
    private fun cancel() {
        if (appWidgetId != -1) hostManager.deleteId(appWidgetId)
        finish()
    }

    companion object {
        private const val EXTRA_PROVIDER = "provider"
        private const val STATE_ID = "appWidgetId"
        private const val REQ_BIND = 1
        private const val REQ_CONFIG = 2

        fun start(context: Context, provider: ComponentName) {
            context.startActivity(
                Intent(context, WidgetPickerActivity::class.java)
                    .putExtra(EXTRA_PROVIDER, provider.flattenToString())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
