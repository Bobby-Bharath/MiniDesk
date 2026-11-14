package com.example.minidesk.ui.bento.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

@Composable
fun NativeAppWidgetTile(
    appWidgetId: Int,
    widgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    cellWidth: Dp,
    cellHeight: Dp,
    spanW: Float,
    spanH: Float,
    gap: Dp,
    modifier: Modifier = Modifier
) {
    val widgetInfo = remember(appWidgetId) {
        try {
            appWidgetManager.getAppWidgetInfo(appWidgetId)
        } catch (e: Exception) {
            null
        }
    }

    if (widgetInfo == null) {
        // Fallback if target app was uninstalled
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF161616), RoundedCornerShape(18.dp))
                .border(0.5.dp, Color(0xFF242424), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Widget Unavailable", color = Color.Gray, fontSize = 11.sp)
        }
        return
    }

    // Calculate exact target bounding box in dp
    val widthDp = ((spanW * cellWidth.value) + ((spanW - 1) * gap.value)).roundToInt().coerceAtLeast(40)
    val heightDp = ((spanH * cellHeight.value) + ((spanH - 1) * gap.value)).roundToInt().coerceAtLeast(40)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101010), RoundedCornerShape(18.dp))
            .border(0.5.dp, Color(0xFF242424), RoundedCornerShape(18.dp))
            .padding(6.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                try {
                    widgetHost.createView(ctx, appWidgetId, widgetInfo).apply {
                        setAppWidget(appWidgetId, widgetInfo)
                        // Inject size bundle to stop third-party widgets from cropping
                        val options = Bundle().apply {
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
                        }
                        appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
                        updateAppWidgetSize(options, widthDp, heightDp, widthDp, heightDp)
                    }
                } catch (e: Exception) {
                    AppWidgetHostView(ctx)
                }
            },
            update = { hostView ->
                val options = Bundle().apply {
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
                }
                hostView.updateAppWidgetSize(options, widthDp, heightDp, widthDp, heightDp)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
