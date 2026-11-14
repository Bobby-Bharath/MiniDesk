package com.example.minidesk.deck

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/** Full-panel picker: built-in widgets on one tab, installed apps' widgets on the other. */
@Composable
fun AddWidgetSheet(
    builtins: BuiltinRegistry,
    hostManager: WidgetHostManager,
    onPickBuiltin: (BuiltinSpec) -> Unit,
    onPickProvider: (ProviderRow) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableIntStateOf(0) }
    val providers by produceState<List<ProviderRow>>(emptyList()) {
        value = withContext(Dispatchers.Default) { hostManager.providers() }
    }
    val grouped = remember(providers) { providers.groupBy { it.appLabel }.toList() }

    Column(
        modifier
            .background(DeckColors.Ink)
            // Swallow touches so nothing underneath reacts while the sheet is open.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "ADD WIDGET", color = DeckColors.Mute, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier.size(36.dp).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = DeckColors.Paper, modifier = Modifier.size(20.dp))
            }
        }
        Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DeckButton("MINIDESK", filled = tab == 0, modifier = Modifier.weight(1f)) { tab = 0 }
            DeckButton("APPS", filled = tab == 1, modifier = Modifier.weight(1f)) { tab = 1 }
        }

        if (tab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(builtins.all, key = { it.type }) { spec ->
                    PickRow(spec.title, "${spec.defW}×${spec.defH}", preview = null) { onPickBuiltin(spec) }
                }
            }
        } else {
            val ctx = LocalContext.current
            if (providers.isEmpty()) {
                Text("Loading widgets…", color = DeckColors.Mute, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                grouped.forEach { (appLabel, rows) ->
                    item(key = "h:$appLabel") {
                        Text(
                            appLabel.uppercase(), color = DeckColors.Mute, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(rows, key = { it.component.flattenToString() }) { row ->
                        val preview by produceState<ImageBitmap?>(null, row.component) {
                            value = withContext(Dispatchers.Default) { loadPreview(ctx, row.info) }
                        }
                        val span = remember(row.component) { hostManager.sizing(row.info, DeckGrid.cellDp) }
                        PickRow(row.label, "${span.defW}×${span.defH}", preview) { onPickProvider(row) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickRow(title: String, size: String, preview: ImageBitmap?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = DeckColors.Panel,
        contentColor = DeckColors.Paper,
        border = BorderStroke(0.5.dp, DeckColors.Hairline),
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (preview != null) {
                Image(
                    bitmap = preview, contentDescription = null, contentScale = ContentScale.Fit,
                    modifier = Modifier.size(64.dp),
                )
                Box(Modifier.size(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(size, color = DeckColors.Mute, fontSize = 10.sp)
            }
        }
    }
}

/** Widget previews are coloured; that is intentional, third-party widgets are never filtered. */
private fun loadPreview(ctx: Context, info: AppWidgetProviderInfo): ImageBitmap? = try {
    val d = info.loadPreviewImage(ctx, 0) ?: info.loadIcon(ctx, 0)
    val w = max(d.intrinsicWidth, 1)
    val h = max(d.intrinsicHeight, 1)
    val scale = min(1f, 320f / max(w, h))
    d.toBitmap(max((w * scale).toInt(), 1), max((h * scale).toInt(), 1)).asImageBitmap()
} catch (e: Exception) {
    null
}
