package com.example.minidesk.deck

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class GridMetrics(val cols: Int = DeckGrid.COLS, val gapDp: Float = DeckGrid.GAP_DP)

/** A widget being dragged: how far the finger has moved since the long press, in pixels. */
private data class DragState(val id: String, val offset: Offset)

/** A widget being resized: how far the corner handle has moved, in pixels. */
private data class ResizeState(val id: String, val delta: Offset)

private const val AUTO_SCROLL_PX = 18f
private val ItemShape = RoundedCornerShape(14.dp)

/**
 * The widget canvas. Needs a bounded height (give it weight(1f) or a fixed height).
 *
 * How it stays reliable, in four rules:
 *  1. [items] is never changed while a gesture is running. The finger only moves [drag] / [resize].
 *  2. The layout shown during a gesture is a pure function of (items, drag, resize) via
 *     [GridEngine.place], so neighbours slide away live and the result on release is exactly what was shown.
 *  3. Pointer handlers are keyed on the widget id only. Keying them on position or size restarts the
 *     gesture every frame, which is what makes drag and resize feel broken.
 *  4. Everything is absolutely positioned inside one scrolling Box. No LazyGrid: lazy layouts cannot
 *     place items freely or resize them.
 */
@Composable
fun WidgetGrid(
    items: List<WidgetEntry>,
    editMode: Boolean,
    limitsOf: (WidgetEntry, Float) -> Limits,
    titleOf: (WidgetEntry) -> String,
    onLayoutChange: (List<Cell>) -> Unit,
    onDelete: (WidgetEntry) -> Unit,
    body: @Composable (WidgetEntry, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    metrics: GridMetrics = GridMetrics(),
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val haptic = LocalHapticFeedback.current
        val cols = metrics.cols
        val gapPx = with(density) { metrics.gapDp.dp.toPx() }
        val cellPx = (constraints.maxWidth - gapPx * (cols - 1)) / cols
        val step = cellPx + gapPx
        val cellDp = with(density) { cellPx.toDp().value }
        val viewportPx = if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() else 0f
        SideEffect { DeckGrid.cellDp = cellDp }

        val engine = remember(cols) { GridEngine(cols) }
        val scroll = rememberScrollState()
        var drag by remember { mutableStateOf<DragState?>(null) }
        var resize by remember { mutableStateOf<ResizeState?>(null) }

        val baseCells = remember(items) { items.map { it.toCell() } }
        val limitsById = remember(items, cellDp) { items.associate { it.id to limitsOf(it, cellDp) } }

        // The one function that turns "what the finger is doing" into "what the grid looks like".
        val compute: (DragState?, ResizeState?) -> List<Cell> = { d, r ->
            when {
                d != null -> baseCells.firstOrNull { it.id == d.id }?.let { b ->
                    engine.place(
                        baseCells,
                        b.copy(
                            col = b.col + GridMath.deltaCells(d.offset.x, step),
                            row = b.row + GridMath.deltaCells(d.offset.y, step),
                        )
                    )
                } ?: baseCells
                r != null -> baseCells.firstOrNull { it.id == r.id }?.let { b ->
                    val lim = limitsById[b.id] ?: Limits.fixed(b.w, b.h)
                    val w = lim.clampW(b.w + GridMath.deltaCells(r.delta.x, step), cols - b.col)
                    val h = lim.clampH(b.h + GridMath.deltaCells(r.delta.y, step))
                    engine.place(baseCells, b.copy(w = w, h = h))
                } ?: baseCells
                else -> baseCells
            }
        }
        val computeLatest by rememberUpdatedState(compute)
        val preview = compute(drag, resize)
        val previewById = preview.associateBy { it.id }
        val baseById = baseCells.associateBy { it.id }

        val onDragStart: (String) -> Unit = { id ->
            if (resize == null && drag == null) {
                drag = DragState(id, Offset.Zero)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
        val onDragBy: (Offset) -> Unit = { delta -> drag = drag?.let { it.copy(offset = it.offset + delta) } }
        val onDragEnd: (Boolean) -> Unit = { commit ->
            val d = drag
            drag = null
            if (commit && d != null) onLayoutChange(computeLatest(d, null))
        }
        val onResizeStart: (String) -> Unit = { id ->
            if (resize == null && drag == null) resize = ResizeState(id, Offset.Zero)
        }
        val onResizeBy: (Offset) -> Unit = { delta -> resize = resize?.let { it.copy(delta = it.delta + delta) } }
        val onResizeEnd: (Boolean) -> Unit = { commit ->
            val r = resize
            resize = null
            if (commit && r != null) onLayoutChange(computeLatest(null, r))
        }

        // Scroll while dragging near the top or bottom edge, and keep the widget under the finger.
        val dragActive = drag != null
        val baseCellsLatest by rememberUpdatedState(baseCells)
        LaunchedEffect(dragActive) {
            if (!dragActive) return@LaunchedEffect
            val edge = with(density) { 56.dp.toPx() }
            while (true) {
                val d = drag ?: break
                val b = baseCellsLatest.firstOrNull { it.id == d.id } ?: break
                if (d.offset != Offset.Zero) {
                    val top = b.row * step + d.offset.y - scroll.value
                    val bottom = top + GridMath.spanPx(b.h, cellPx, gapPx)
                    val dy = when {
                        top < edge -> -AUTO_SCROLL_PX
                        bottom > viewportPx - edge -> AUTO_SCROLL_PX
                        else -> 0f
                    }
                    if (dy != 0f) {
                        val used = scroll.scrollBy(dy)
                        if (used != 0f) drag = drag?.let { it.copy(offset = it.offset + Offset(0f, used)) }
                    }
                }
                delay(16)
            }
        }

        val rows = preview.maxOfOrNull { it.bottom } ?: 0
        val contentPx = if (editMode) max((rows + 2) * step - gapPx, viewportPx) else max(rows * step - gapPx, 0f)
        fun px(v: Float): Dp = with(density) { v.toDp() }

        Box(
            Modifier
                .fillMaxWidth()
                .verticalScroll(scroll, enabled = drag == null && resize == null)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(px(contentPx))
                    .then(
                        if (editMode) Modifier.drawBehind {
                            // Empty cells, so you can see where things can go.
                            val radius = CornerRadius(12.dp.toPx())
                            val stroke = Stroke(width = 1.dp.toPx())
                            val rowCount = ceil(size.height / step).toInt()
                            for (r in 0 until rowCount) for (c in 0 until cols) {
                                drawRoundRect(
                                    color = DeckColors.Hairline,
                                    topLeft = Offset(c * step, r * step),
                                    size = Size(cellPx, cellPx),
                                    cornerRadius = radius,
                                    style = stroke,
                                )
                            }
                        } else Modifier
                    )
            ) {
                // Outline of where the active widget will land if released now.
                val activeId = drag?.id ?: resize?.id
                if (activeId != null) {
                    previewById[activeId]?.let { g ->
                        Box(
                            Modifier
                                .offset { IntOffset((g.col * step).roundToInt(), (g.row * step).roundToInt()) }
                                .size(px(GridMath.spanPx(g.w, cellPx, gapPx)), px(GridMath.spanPx(g.h, cellPx, gapPx)))
                                .border(1.dp, DeckColors.Mute, ItemShape)
                        )
                    }
                }

                items.forEach { e ->
                    key(e.id) {
                        val base = baseById.getValue(e.id)
                        val shown = previewById[e.id] ?: base
                        val d = drag?.takeIf { it.id == e.id }
                        val r = resize?.takeIf { it.id == e.id }
                        GridItem(
                            base = base,
                            shown = shown,
                            dragOffset = d?.offset,
                            resizeDelta = r?.delta,
                            limits = limitsById[e.id] ?: Limits.fixed(base.w, base.h),
                            cols = cols, step = step, cellPx = cellPx, gapPx = gapPx,
                            editMode = editMode,
                            title = titleOf(e),
                            onDragStart = { onDragStart(e.id) },
                            onDragBy = onDragBy,
                            onDragEnd = onDragEnd,
                            onResizeStart = { onResizeStart(e.id) },
                            onResizeBy = onResizeBy,
                            onResizeEnd = onResizeEnd,
                            onDelete = { onDelete(e) },
                            body = { w, h -> body(e, w, h) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridItem(
    base: Cell,
    shown: Cell,
    dragOffset: Offset?,
    resizeDelta: Offset?,
    limits: Limits,
    cols: Int,
    step: Float,
    cellPx: Float,
    gapPx: Float,
    editMode: Boolean,
    title: String,
    onDragStart: () -> Unit,
    onDragBy: (Offset) -> Unit,
    onDragEnd: (Boolean) -> Unit,
    onResizeStart: () -> Unit,
    onResizeBy: (Offset) -> Unit,
    onResizeEnd: (Boolean) -> Unit,
    onDelete: () -> Unit,
    body: @Composable (Float, Float) -> Unit,
) {
    val density = LocalDensity.current
    val isDragging = dragOffset != null
    val isResizing = resizeDelta != null

    // Position: follows the finger while dragging, otherwise glides to its (possibly pushed) cell.
    // The animation keeps chasing the preview cell even while the finger overrides it, so on release
    // the widget is already sitting in its final cell and nothing jumps.
    val targetPos = IntOffset((shown.col * step).roundToInt(), (shown.row * step).roundToInt())
    val animatedPos by animateIntOffsetAsState(targetPos, spring(stiffness = Spring.StiffnessMediumLow), label = "pos")
    val pos = if (dragOffset != null) {
        IntOffset((base.col * step + dragOffset.x).roundToInt(), (base.row * step + dragOffset.y).roundToInt())
    } else animatedPos

    val targetSize = IntSize(
        GridMath.spanPx(shown.w, cellPx, gapPx).roundToInt(),
        GridMath.spanPx(shown.h, cellPx, gapPx).roundToInt(),
    )
    val animatedSize by animateIntSizeAsState(targetSize, spring(stiffness = Spring.StiffnessMediumLow), label = "size")
    val size = if (resizeDelta != null) {
        val maxWCells = max(limits.minW, min(limits.maxW, cols - base.col))
        val w = (GridMath.spanPx(base.w, cellPx, gapPx) + resizeDelta.x)
            .coerceIn(GridMath.spanPx(limits.minW, cellPx, gapPx), GridMath.spanPx(maxWCells, cellPx, gapPx))
        val h = (GridMath.spanPx(base.h, cellPx, gapPx) + resizeDelta.y)
            .coerceIn(GridMath.spanPx(limits.minH, cellPx, gapPx), GridMath.spanPx(limits.maxH, cellPx, gapPx))
        IntSize(w.roundToInt(), h.roundToInt())
    } else animatedSize

    val widthDp = with(density) { size.width.toDp() }
    val heightDp = with(density) { size.height.toDp() }

    Box(
        Modifier
            .offset { pos }
            .size(widthDp, heightDp)
            .zIndex(if (isDragging || isResizing) 10f else 0f)
            .graphicsLayer {
                val s = if (isDragging) 1.03f else 1f
                scaleX = s
                scaleY = s
            }
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(ItemShape)
                .background(DeckColors.Panel)
                .border(0.5.dp, if (editMode) DeckColors.Paper else DeckColors.Hairline, ItemShape)
        ) {
            body(widthDp.value, heightDp.value)
        }
        if (editMode) {
            EditChrome(
                id = base.id,
                title = title,
                spanLabel = "${shown.w}×${shown.h}",
                onDragStart = onDragStart, onDragBy = onDragBy, onDragEnd = onDragEnd,
                onResizeStart = onResizeStart, onResizeBy = onResizeBy, onResizeEnd = onResizeEnd,
                onDelete = onDelete,
            )
        }
    }
}

/** Everything drawn on top of a widget in edit mode: dim layer, name, size, delete badge, resize handle. */
@Composable
private fun BoxScope.EditChrome(
    id: String,
    title: String,
    spanLabel: String,
    onDragStart: () -> Unit,
    onDragBy: (Offset) -> Unit,
    onDragEnd: (Boolean) -> Unit,
    onResizeStart: () -> Unit,
    onResizeBy: (Offset) -> Unit,
    onResizeEnd: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    // Always call the newest callbacks from long-lived pointer handlers (rule 3).
    val dragStart by rememberUpdatedState(onDragStart)
    val dragBy by rememberUpdatedState(onDragBy)
    val dragEnd by rememberUpdatedState(onDragEnd)
    val resizeStart by rememberUpdatedState(onResizeStart)
    val resizeBy by rememberUpdatedState(onResizeBy)
    val resizeEnd by rememberUpdatedState(onResizeEnd)

    // Dim layer. It also swallows touches so the live widget underneath cannot be tapped while editing.
    Box(
        Modifier
            .matchParentSize()
            .clip(ItemShape)
            .background(DeckColors.Ink.copy(alpha = 0.55f))
            .pointerInput(id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { dragStart() },
                    onDrag = { change, amount ->
                        change.consume()
                        dragBy(amount)
                    },
                    onDragEnd = { dragEnd(true) },
                    onDragCancel = { dragEnd(false) },
                )
            }
    )

    Text(
        text = title,
        color = DeckColors.Paper,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 10.dp, top = 12.dp, end = 46.dp),
    )

    Text(
        text = spanLabel,
        color = DeckColors.Ink,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
            .clip(CircleShape)
            .background(DeckColors.Paper)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )

    // Delete badge
    Box(
        Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(DeckColors.Paper)
            .clickable(onClick = onDelete),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.Close, contentDescription = "Remove widget", tint = DeckColors.Ink, modifier = Modifier.size(18.dp))
    }

    // Resize handle
    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .padding(6.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(DeckColors.Ink)
            .border(1.dp, DeckColors.Paper, CircleShape)
            .pointerInput(id) {
                detectDragGestures(
                    onDragStart = { resizeStart() },
                    onDrag = { change, amount ->
                        change.consume()
                        resizeBy(amount)
                    },
                    onDragEnd = { resizeEnd(true) },
                    onDragCancel = { resizeEnd(false) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(16.dp)) {
            val w = 2.dp.toPx()
            drawLine(DeckColors.Paper, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = w)
            drawLine(DeckColors.Paper, Offset(size.width, size.height * 0.5f), Offset(size.width * 0.5f, size.height), strokeWidth = w)
        }
    }
}
