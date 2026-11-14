package com.example.minidesk.ui.bento.engine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.model.BentoPlacement
import kotlin.math.roundToInt

@Composable
fun GridSnappingOverlay(
    isVisible: Boolean,
    columns: Int,
    totalRows: Int,
    cellWidth: Dp,
    cellHeight: Dp,
    gap: Dp,
    padding: Dp,
    activeActor: BentoPlacement?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150))
    ) {
        val density = LocalDensity.current
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }
        val gapPx = with(density) { gap.toPx() }
        val paddingPx = with(density) { padding.toPx() }

        Box(modifier = modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                val cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())

                // 1. Draw dashed guideline cells for every possible slot
                for (r in 0 until totalRows) {
                    for (c in 0 until columns) {
                        val x = paddingPx + c * (cellWidthPx + gapPx)
                        val y = paddingPx + r * (cellHeightPx + gapPx)

                        drawRoundRect(
                            color = Color(0xFF262626),
                            topLeft = Offset(x, y),
                            size = Size(cellWidthPx, cellHeightPx),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
                        )
                    }
                }

                // 2. Draw live highlighted target snap zone if actor is actively being transformed
                if (activeActor != null) {
                    val targetCol = activeActor.x.roundToInt().coerceIn(0, columns - 1)
                    val targetRow = activeActor.y.roundToInt().coerceAtLeast(0)
                    val targetSpanW = activeActor.w.roundToInt().coerceIn(1, columns - targetCol)
                    val targetSpanH = activeActor.h.roundToInt().coerceAtLeast(1)

                    val highlightX = paddingPx + targetCol * (cellWidthPx + gapPx)
                    val highlightY = paddingPx + targetRow * (cellHeightPx + gapPx)
                    val highlightW = targetSpanW * cellWidthPx + (targetSpanW - 1) * gapPx
                    val highlightH = targetSpanH * cellHeightPx + (targetSpanH - 1) * gapPx

                    // Solid translucent fill
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.08f),
                        topLeft = Offset(highlightX, highlightY),
                        size = Size(highlightW, highlightH),
                        cornerRadius = cornerRadius
                    )

                    // Solid white boundary outline
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(highlightX, highlightY),
                        size = Size(highlightW, highlightH),
                        cornerRadius = cornerRadius,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // 3. Render floating "Snapped" badge at top-left of target cell
            if (activeActor != null) {
                val targetCol = activeActor.x.roundToInt().coerceIn(0, columns - 1)
                val targetRow = activeActor.y.roundToInt().coerceAtLeast(0)
                val targetSpanW = activeActor.w.roundToInt().coerceIn(1, columns - targetCol)
                val targetSpanH = activeActor.h.roundToInt().coerceAtLeast(1)

                val badgeOffsetX = padding + (cellWidth + gap) * targetCol + 8.dp
                val badgeOffsetY = padding + (cellHeight + gap) * targetRow + 8.dp

                Box(
                    modifier = Modifier
                        .offset(x = badgeOffsetX, y = badgeOffsetY)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${targetSpanW} × ${targetSpanH} Snapped",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
