package com.example.minidesk.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.minidesk.deck.WidgetRepository
import com.example.minidesk.service.MiniDeskWidgetHost

class WidgetPickerActivity : Activity() {

    private lateinit var appWidgetHost: MiniDeskWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager
    private var allocatedWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    companion object {
        private const val REQUEST_PICK_APPWIDGET = 2001
        private const val REQUEST_CREATE_APPWIDGET = 2002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetHost = MiniDeskWidgetHost(applicationContext)
        appWidgetManager = AppWidgetManager.getInstance(applicationContext)

        allocatedWidgetId = appWidgetHost.allocateAppWidgetId()

        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allocatedWidgetId)
        }
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_APPWIDGET -> {
                    val widgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allocatedWidgetId) ?: allocatedWidgetId
                    configureOrSaveWidget(widgetId)
                }
                REQUEST_CREATE_APPWIDGET -> {
                    val widgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allocatedWidgetId) ?: allocatedWidgetId
                    saveWidgetToBento(widgetId)
                }
            }
        } else {
            // User cancelled picking or configuration: clean up allocated ID
            if (allocatedWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                appWidgetHost.deleteAppWidgetId(allocatedWidgetId)
            }
            finish()
        }
    }

    private fun configureOrSaveWidget(widgetId: Int) {
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(widgetId)
        if (appWidgetInfo?.configure != null) {
            // Launch widget's setup/config activity
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = appWidgetInfo.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
        } else {
            saveWidgetToBento(widgetId)
        }
    }

    private fun saveWidgetToBento(widgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(widgetId)
        val providerStr = info?.provider?.flattenToString() ?: ""
        WidgetRepository.get(this).addAppWidget(widgetId, providerStr, 2, 2)
        Log.d("MiniDeskWidget", "Saved AppWidget ID $widgetId into Bento Grid")
        finish()
    }
}
