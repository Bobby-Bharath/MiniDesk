package com.example.minidesk.deck

import android.appwidget.AppWidgetHostView
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** Shows a third-party widget. Third-party widgets keep their own colours; only built-ins are monochrome. */
@Composable
fun AppWidgetContent(
    appWidgetId: Int,
    hostManager: WidgetHostManager,
    widthDp: Float,
    heightDp: Float,
    modifier: Modifier = Modifier,
) {
    val available = remember(appWidgetId) { hostManager.info(appWidgetId) != null }
    if (!available) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Widget unavailable", color = DeckColors.Mute, fontSize = 11.sp)
        }
        return
    }
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx -> hostManager.createView(ctx, appWidgetId) ?: View(ctx) },
        update = { v ->
            // Tells the widget how much room it really has so it can pick the right layout.
            (v as? AppWidgetHostView)?.updateAppWidgetSize(
                Bundle(), widthDp.toInt(), heightDp.toInt(), widthDp.toInt(), heightDp.toInt()
            )
        },
    )
}
