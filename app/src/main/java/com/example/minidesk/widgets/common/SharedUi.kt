package com.example.minidesk.widgets.common

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.deck.DeckColors
import kotlin.math.roundToInt

enum class WidgetSize {
    Compact,
    Medium,
    Large
}

fun calculateWidgetSize(widthDp: Dp, heightDp: Dp): WidgetSize {
    return when {
        widthDp < 180.dp || heightDp < 130.dp -> WidgetSize.Compact
        widthDp < 280.dp || heightDp < 200.dp -> WidgetSize.Medium
        else -> WidgetSize.Large
    }
}

@Composable
fun rememberHaptic(): () -> Unit {
    val view = LocalView.current
    return remember(view) {
        {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        color = DeckColors.Mute,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun IconAction(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    tint: Color = DeckColors.Paper
) {
    val haptic = rememberHaptic()
    Surface(
        onClick = {
            haptic()
            onClick()
        },
        modifier = modifier.size(size),
        shape = RoundedCornerShape(10.dp),
        color = DeckColors.Panel,
        border = BorderStroke(0.5.dp, DeckColors.Hairline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun GrantRow(
    message: String,
    intent: Intent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = rememberHaptic()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DeckColors.Panel,
        border = BorderStroke(0.5.dp, DeckColors.Hairline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                color = DeckColors.Mute,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = {
                    haptic()
                    try {
                        context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = DeckColors.Paper,
                contentColor = DeckColors.Ink
            ) {
                Text(
                    text = "GRANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ToggleTile(
    label: String,
    stateText: String,
    icon: ImageVector,
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHaptic()

    Surface(
        onClick = {
            haptic()
            onClick()
        },
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isOn) DeckColors.Paper else DeckColors.Panel,
        contentColor = if (isOn) DeckColors.Ink else DeckColors.Paper,
        border = BorderStroke(0.5.dp, if (isOn) DeckColors.Paper else DeckColors.Hairline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isOn) DeckColors.Ink else DeckColors.Paper,
                modifier = Modifier.size(16.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    color = if (isOn) DeckColors.Ink else DeckColors.Paper,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stateText,
                    color = if (isOn) DeckColors.Ink.copy(alpha = 0.6f) else DeckColors.Mute,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DeckSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    label: String? = null,
    steps: Int = 100
) {
    val haptic = rememberHaptic()
    val density = LocalDensity.current

    val clampedValue = value.coerceIn(0f, 1f)
    var dragVal by remember(clampedValue) { mutableFloatStateOf(clampedValue) }
    var lastStep by remember { mutableIntStateOf((clampedValue * steps).roundToInt()) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DeckColors.Panel)
            .border(0.5.dp, DeckColors.Hairline, RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val widthPx = size.width.toFloat()
                    if (widthPx > 0) {
                        val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                        dragVal = fraction
                        onValueChange(fraction)
                        haptic()
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val widthPx = size.width.toFloat()
                        if (widthPx > 0) {
                            val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                            dragVal = fraction
                            onValueChange(fraction)
                        }
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val widthPx = size.width.toFloat()
                        if (widthPx > 0) {
                            val fraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                            dragVal = fraction
                            val currentStep = (fraction * steps).roundToInt()
                            if (currentStep != lastStep) {
                                lastStep = currentStep
                                haptic()
                            }
                            onValueChange(fraction)
                        }
                    }
                )
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val fillWidth = widthPx * dragVal

        // Filled track
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { fillWidth.toDp() })
                .background(DeckColors.Paper)
        )

        // Overlay text/icon
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (dragVal > 0.4f) DeckColors.Ink else DeckColors.Paper,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (label != null) {
                    Text(
                        text = label,
                        color = if (dragVal > 0.4f) DeckColors.Ink else DeckColors.Paper,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${(dragVal * 100).roundToInt()}%",
                color = if (dragVal > 0.85f) DeckColors.Ink else DeckColors.Paper,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
