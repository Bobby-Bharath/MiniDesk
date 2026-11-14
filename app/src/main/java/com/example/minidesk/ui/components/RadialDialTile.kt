package com.example.minidesk.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RadialDialTile(
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var isScrubbing by remember { mutableStateOf(false) }
    var localValue by remember { mutableFloatStateOf(currentValue) }
    var releaseJob by remember { mutableStateOf<Job?>(null) }

    // Synchronize from external currentValue ONLY when user is NOT dragging
    LaunchedEffect(currentValue) {
        if (!isScrubbing) {
            localValue = currentValue
        }
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF282828), CircleShape)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        releaseJob?.cancel()
                        isScrubbing = true
                    },
                    onDragEnd = {
                        releaseJob?.cancel()
                        releaseJob = coroutineScope.launch {
                            delay(1000L)
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackWidth = 2.5.dp.toPx()
            val fillWidth = 3.dp.toPx()
            val inset = fillWidth / 2f
            val sizePx = size.minDimension - fillWidth

            // Background track circle
            drawCircle(
                color = Color(0xFF282828),
                radius = size.minDimension / 2f - inset,
                style = Stroke(trackWidth)
            )

            // Filled progress arc
            val sweepAngle = (localValue.coerceIn(0f, 100f) / 100f) * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(sizePx, sizePx),
                    style = Stroke(fillWidth, cap = StrokeCap.Round)
                )
            }
        }

        AnimatedContent(
            targetState = isScrubbing,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
            label = "DialMorph"
        ) { scrubbing ->
            if (scrubbing) {
                Text(
                    text = "${localValue.roundToInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                icon()
            }
        }
    }
}
