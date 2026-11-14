package com.example.minidesk

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import com.example.minidesk.data.settings.InnerIndicator
import com.example.minidesk.data.settings.MiniDeskSettingsRepository
import com.example.minidesk.data.settings.TriggerShape
import com.example.minidesk.ui.CommandDeckView
import com.example.minidesk.ui.QuickStripView
import kotlin.math.abs

enum class OverlayState {
    IDLE_NOTCH,
    QUICK_STRIP,
    COMMAND_DECK
}

/**
 * Dynamic 120Hz Zero-Allocation Handle/Notch Composable observing Settings Repository
 */
@Composable
fun MiniDeskNotch(
    modifier: Modifier = Modifier,
    onStateChange: (OverlayState) -> Unit
) {
    val context = LocalContext.current
    val viewConfig = LocalViewConfiguration.current
    val settingsRepo = remember { MiniDeskSettingsRepository.get(context) }
    val prefs by settingsRepo.preferences.collectAsState()
    val cg = prefs.colorGraph

    val widthDp = if (prefs.triggerShape == TriggerShape.FLOATING_PUCK) prefs.puckDiameterDp.dp else prefs.handleWidthDp.dp
    val heightDp = if (prefs.triggerShape == TriggerShape.FLOATING_PUCK) prefs.puckDiameterDp.dp else prefs.handleHeightDp.dp

    val handleRadiusDp = prefs.globalRadiusDp.coerceAtMost(28).dp

    if (prefs.triggerShape == TriggerShape.ORGANIC_TEARDROP) {
        Box(
            modifier = modifier
                .size(width = widthDp.coerceAtLeast(20.dp), height = heightDp.coerceAtLeast(60.dp))
                .alpha(prefs.restingOpacity)
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    val isRight = prefs.isRightEdge
                    val path = Path().apply {
                        if (isRight) {
                            moveTo(w, 0f)
                            cubicTo(w, h * 0.15f, 0f, h * 0.28f, 0f, h * 0.5f)
                            cubicTo(0f, h * 0.72f, w, h * 0.85f, w, h)
                            lineTo(w, 0f)
                            close()
                        } else {
                            moveTo(0f, 0f)
                            cubicTo(0f, h * 0.15f, w, h * 0.28f, w, h * 0.5f)
                            cubicTo(w, h * 0.72f, 0f, h * 0.85f, 0f, h)
                            lineTo(0f, 0f)
                            close()
                        }
                    }
                    onDrawBehind {
                        drawPath(path, color = Color(cg.surface))
                        drawPath(path, color = Color(cg.border), style = Stroke(width = 1.dp.toPx()))
                    }
                }
                .systemGestureExclusion()
                .clickable { onStateChange(OverlayState.QUICK_STRIP) }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var dragX = 0f
                        var hasDragged = false
                        val touchSlop = viewConfig.touchSlop

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                if (!hasDragged || abs(dragX) < touchSlop) {
                                    onStateChange(OverlayState.QUICK_STRIP)
                                } else if (dragX < -touchSlop) {
                                    onStateChange(OverlayState.COMMAND_DECK)
                                }
                                break
                            }

                            val dragDelta = change.position.x - down.position.x
                            dragX = dragDelta
                            if (abs(dragX) > touchSlop) {
                                hasDragged = true
                                if (dragX < -touchSlop * 1.5f) {
                                    onStateChange(OverlayState.COMMAND_DECK)
                                    change.consume()
                                    break
                                }
                            }
                        }
                    }
                }
        )
        return
    }

    val handleShape = when (prefs.triggerShape) {
        TriggerShape.FLOATING_PUCK -> CircleShape
        TriggerShape.EDGE_BAR -> RoundedCornerShape(
            topStart = if (!prefs.isRightEdge) handleRadiusDp else 0.dp,
            bottomStart = if (!prefs.isRightEdge) handleRadiusDp else 0.dp,
            topEnd = if (prefs.isRightEdge) handleRadiusDp else 0.dp,
            bottomEnd = if (prefs.isRightEdge) handleRadiusDp else 0.dp
        )
        TriggerShape.SPLIT_BAR -> RoundedCornerShape(handleRadiusDp)
        else -> RoundedCornerShape(handleRadiusDp)
    }

    if (prefs.triggerShape == TriggerShape.SPLIT_BAR) {
        Column(
            modifier = modifier
                .size(width = widthDp, height = heightDp)
                .alpha(prefs.restingOpacity)
                .clickable { onStateChange(OverlayState.QUICK_STRIP) },
            verticalArrangement = Arrangement.spacedBy(prefs.splitGapDp.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = handleShape,
                color = Color(cg.primarySolid),
                border = BorderStroke(1.dp, Color(cg.border))
            ) {}
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = handleShape,
                color = Color(cg.surface),
                border = BorderStroke(1.dp, Color(cg.border))
            ) {}
        }
        return
    }

    Surface(
        onClick = { onStateChange(OverlayState.QUICK_STRIP) },
        modifier = modifier
            .size(width = widthDp, height = heightDp)
            .alpha(prefs.restingOpacity)
            .systemGestureExclusion()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dragX = 0f
                    var hasDragged = false
                    val touchSlop = viewConfig.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            if (!hasDragged || abs(dragX) < touchSlop) {
                                onStateChange(OverlayState.QUICK_STRIP)
                            } else if (dragX < -touchSlop) {
                                onStateChange(OverlayState.COMMAND_DECK)
                            }
                            break
                        }

                        val dragDelta = change.position.x - down.position.x
                        dragX = dragDelta
                        if (abs(dragX) > touchSlop) {
                            hasDragged = true
                            if (dragX < -touchSlop * 1.5f) {
                                onStateChange(OverlayState.COMMAND_DECK)
                                change.consume()
                                break
                            }
                        }
                    }
                }
            },
        shape = handleShape,
        color = Color(cg.surface),
        border = BorderStroke(1.dp, Color(cg.border))
    ) {
        if (prefs.innerIndicator != InnerIndicator.NONE) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .width(if (prefs.innerIndicator == InnerIndicator.LINE) 2.dp else 4.dp)
                        .height(if (prefs.innerIndicator == InnerIndicator.LINE) 16.dp else 4.dp)
                        .background(Color(cg.primarySolid), CircleShape)
                )
            }
        }
    }
}

@Composable
fun MiniDeskScreen(
    currentState: OverlayState,
    dismissSignal: Int,
    onStateChange: (OverlayState) -> Unit,
    onDismissFinished: () -> Unit,
    volumeController: VolumeController,
    brightnessController: BrightnessController,
    flashlightController: FlashlightController,
    mediaController: MediaController,
    scratchpadHelper: ScratchpadHelper,
    clipboardHelper: ClipboardManagerHelper
) {
    val slideOffsetFraction = remember { Animatable(0f) }
    var isDismissing by remember { mutableStateOf(false) }

    // React to external dismiss signal (e.g. outside touch)
    LaunchedEffect(dismissSignal) {
        if (dismissSignal > 0 && currentState != OverlayState.IDLE_NOTCH && !isDismissing) {
            isDismissing = true
            slideOffsetFraction.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            onDismissFinished()
            slideOffsetFraction.snapTo(0f)
            isDismissing = false
        }
    }

    // Handle opening slide inward
    LaunchedEffect(currentState) {
        if (currentState != OverlayState.IDLE_NOTCH && !isDismissing) {
            slideOffsetFraction.snapTo(1f)
            slideOffsetFraction.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val requestDismiss = {
        if (currentState != OverlayState.IDLE_NOTCH && !isDismissing) {
            isDismissing = true
        }
    }

    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            slideOffsetFraction.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            onDismissFinished()
            slideOffsetFraction.snapTo(0f)
            isDismissing = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (currentState == OverlayState.IDLE_NOTCH && !isDismissing) {
            MiniDeskNotch(onStateChange = onStateChange)
        } else {
            // Expanded Panel (Quick Strip or Command Deck)
            val panelWidth = when (currentState) {
                OverlayState.QUICK_STRIP -> 70.dp
                OverlayState.COMMAND_DECK -> 340.dp
                else -> 70.dp
            }

            val density = LocalDensity.current
            val offsetX = with(density) { (slideOffsetFraction.value * panelWidth.toPx()).dp }
            val panelShape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)

            Surface(
                modifier = Modifier
                    .offset(x = offsetX)
                    .width(panelWidth)
                    .padding(vertical = 12.dp)
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFF262626),
                        shape = panelShape
                    )
                    .clip(panelShape),
                color = Color(0xFF000000),
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    when (currentState) {
                        OverlayState.QUICK_STRIP -> {
                            QuickStripView(
                                onStateChange = onStateChange,
                                onRequestDismiss = requestDismiss,
                                volumeController = volumeController,
                                brightnessController = brightnessController,
                                flashlightController = flashlightController,
                                mediaController = mediaController
                            )
                        }
                        OverlayState.COMMAND_DECK -> {
                            CommandDeckView(
                                onRequestDismiss = requestDismiss,
                                volumeController = volumeController,
                                flashlightController = flashlightController,
                                mediaController = mediaController,
                                scratchpadHelper = scratchpadHelper,
                                clipboardHelper = clipboardHelper
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
