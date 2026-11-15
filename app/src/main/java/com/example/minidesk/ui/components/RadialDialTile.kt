package com.example.minidesk.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.settings.ToolShape
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RadialDialTile(
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    toolShapeType: ToolShape = ToolShape.CIRCLE,
    cornerRadiusDp: Float = 14f,
    showRing: Boolean = true,
    surfaceColor: Color = Color(0xFF1E1E1E),
    borderColor: Color = Color(0xFF282828),
    progressBrush: Brush = SolidColor(Color.White),
    contentColor: Color = Color.White,
    onTap: (() -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var isScrubbing by remember { mutableStateOf(false) }
    var localValue by remember { mutableFloatStateOf(currentValue) }
    var releaseJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(currentValue) {
        if (!isScrubbing) {
            localValue = currentValue
        }
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(shape)
            .background(surfaceColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (onTap != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTap
                    )
                } else Modifier
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        releaseJob?.cancel()
                        isScrubbing = true
                    },
                    onDragEnd = {
                        releaseJob?.cancel()
                        releaseJob = coroutineScope.launch {
                            delay(900L)
                            isScrubbing = false
                        }
                    },
                    onDragCancel = {
                        releaseJob?.cancel()
                        isScrubbing = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        releaseJob?.cancel()
                        isScrubbing = true

                        val sensitivity = 0.5f
                        localValue = (localValue - dragAmount * sensitivity).coerceIn(0f, 100f)
                        onValueChange(localValue)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (showRing) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackWidth = 2.2.dp.toPx()
                val fillWidth = 2.6.dp.toPx()
                val inset = fillWidth / 2f
                val sizePx = size.minDimension - fillWidth

                if (toolShapeType == ToolShape.CIRCLE) {
                    drawCircle(
                        color = borderColor,
                        radius = size.minDimension / 2f - inset,
                        style = Stroke(trackWidth)
                    )
                    val sweepAngle = (localValue.coerceIn(0f, 100f) / 100f) * 360f
                    if (sweepAngle > 0f) {
                        drawArc(
                            brush = progressBrush,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(sizePx, sizePx),
                            style = Stroke(fillWidth, cap = StrokeCap.Round)
                        )
                    }
                } else {
                    // Bottom progress indicator bar inside squircle/global shapes for clean visual alignment
                    val barHeight = 3.dp.toPx()
                    val padH = 8.dp.toPx()
                    val availW = size.width - padH * 2f
                    val y = size.height - 6.dp.toPx()
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(padH, y),
                        size = Size(availW, barHeight),
                        cornerRadius = CornerRadius(barHeight / 2f)
                    )
                    val fillW = availW * (localValue.coerceIn(0f, 100f) / 100f)
                    if (fillW > 0f) {
                        drawRoundRect(
                            brush = progressBrush,
                            topLeft = Offset(padH, y),
                            size = Size(fillW, barHeight),
                            cornerRadius = CornerRadius(barHeight / 2f)
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = isScrubbing,
            transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(140)) },
            label = "DialMorph"
        ) { scrubbing ->
            if (scrubbing) {
                Text(
                    text = "${localValue.roundToInt()}%",
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                icon()
            }
        }
    }
}
