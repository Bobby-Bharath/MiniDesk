package com.example.minidesk.deck

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val UNDO_MS = 4000L

/**
 * The whole widget Deck: the grid, the edit bar, the add sheet and the undo bar.
 * This is the only composable your Command Deck needs to call. Pass [editMode] from your Edit/Done button.
 */
@Composable
fun WidgetDeck(
    repo: WidgetRepository,
    hostManager: WidgetHostManager,
    builtins: BuiltinRegistry,
    editMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val items by repo.items.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    // A removed widget stays recoverable for a few seconds; its system id is only freed afterwards.
    var pending by remember { mutableStateOf<Pair<WidgetEntry, String>?>(null) }

    fun freeId(e: WidgetEntry) { if (e.isApp) hostManager.deleteId(e.appWidgetId) }

    LaunchedEffect(Unit) { repo.seedIfFirstRun(builtins.firstRunDefaults) }
    LaunchedEffect(editMode) {
        if (!editMode) { showAdd = false; confirmReset = false }
    }
    LaunchedEffect(pending) {
        val p = pending ?: return@LaunchedEffect
        delay(UNDO_MS)
        freeId(p.first)
        pending = null
    }
    LaunchedEffect(confirmReset) {
        if (confirmReset) { delay(3000); confirmReset = false }
    }
    DisposableEffect(Unit) {
        onDispose { pending?.let { freeId(it.first) } }
    }

    val titles = remember(items) {
        items.associate { e ->
            e.id to if (e.isApp) {
                hostManager.info(e.appWidgetId)?.loadLabel(ctx.packageManager) ?: "Widget"
            } else {
                builtins[e.builtinType]?.title ?: "Unavailable"
            }
        }
    }

    Box(modifier) {
        Column(Modifier.fillMaxSize()) {
            WidgetGrid(
                items = items,
                editMode = editMode,
                limitsOf = { e, cellDp ->
                    if (e.isApp) {
                        hostManager.limitsFor(e.appWidgetId, cellDp) ?: Limits.fixed(e.w, e.h)
                    } else {
                        builtins[e.builtinType]?.limits ?: Limits.fixed(e.w, e.h)
                    }
                },
                titleOf = { e -> titles[e.id] ?: "Widget" },
                onLayoutChange = { cells -> repo.applyLayout(cells) },
                onDelete = { e ->
                    pending?.let { freeId(it.first) }
                    repo.remove(e.id)
                    pending = e to (titles[e.id] ?: "Widget")
                },
                body = { e, w, h ->
                    if (e.isApp) {
                        AppWidgetContent(e.appWidgetId, hostManager, w, h)
                    } else {
                        val spec = builtins[e.builtinType]
                        if (spec != null) spec.content() else UnavailableBody()
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )

            if (editMode) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DeckButton("+ ADD WIDGET", filled = true, modifier = Modifier.weight(1f)) { showAdd = true }
                    DeckButton(if (confirmReset) "TAP AGAIN" else "RESET", filled = false) {
                        if (confirmReset) {
                            items.filter { it.isApp }.forEach { hostManager.deleteId(it.appWidgetId) }
                            repo.resetToDefaults(builtins.firstRunDefaults)
                            confirmReset = false
                        } else {
                            confirmReset = true
                        }
                    }
                }
            }
        }

        if (showAdd) {
            AddWidgetSheet(
                builtins = builtins,
                hostManager = hostManager,
                onPickBuiltin = { spec ->
                    repo.addBuiltin(spec.type, spec.defW, spec.defH)
                    showAdd = false
                },
                onPickProvider = { row ->
                    showAdd = false
                    WidgetPickerActivity.start(ctx, row.component)
                },
                onClose = { showAdd = false },
                modifier = Modifier.fillMaxSize(),
            )
        }

        pending?.let { (entry, title) ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (editMode) 56.dp else 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = DeckColors.Panel,
                border = BorderStroke(0.5.dp, DeckColors.Paper),
            ) {
                Row(
                    Modifier.padding(start = 14.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$title removed", color = DeckColors.Paper, fontSize = 12.sp)
                    DeckButton("UNDO", filled = false, modifier = Modifier.padding(6.dp)) {
                        repo.restore(entry)
                        pending = null
                    }
                }
            }
        }
    }
}

@Composable
private fun UnavailableBody() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Widget unavailable", color = DeckColors.Mute, fontSize = 11.sp)
    }
}

@Composable
internal fun DeckButton(
    text: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (filled) DeckColors.Paper else DeckColors.Panel,
        contentColor = if (filled) DeckColors.Ink else DeckColors.Paper,
        border = BorderStroke(0.5.dp, if (filled) DeckColors.Paper else DeckColors.Hairline),
    ) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
