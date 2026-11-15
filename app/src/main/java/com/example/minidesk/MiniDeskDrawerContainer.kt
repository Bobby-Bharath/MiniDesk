package com.example.minidesk

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.os.SystemClock
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.minidesk.ui.bento.BentoDeckView
import com.example.minidesk.ui.capsule.CapsuleSpineView
import kotlinx.coroutines.launch

private val SmoothCurve = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

@Composable
fun MiniDeskDrawerContainer(
    drawerMode: OverlayState,
    dismissSignal: Int,
    onStateChange: (OverlayState) -> Unit,
    onDismissComplete: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    volumeController: VolumeController,
    brightnessController: BrightnessController,
    flashlightController: FlashlightController,
    mediaController: MediaController,
    scratchpadHelper: ScratchpadHelper,
    clipboardHelper: ClipboardManagerHelper,
    widgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val spineWidthDp = 64
    val gapDp = 8
    val leftMarginDp = 12

    val targetBentoWidthDp = (screenWidthDp - spineWidthDp - gapDp - leftMarginDp).coerceIn(280, 360)
    val totalContainerWidthDp = targetBentoWidthDp + gapDp + spineWidthDp

    val drawerOffset = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }
    var openTimestamp by remember { mutableLongStateOf(0L) }

    val isOverlayOpen = drawerMode != OverlayState.IDLE_NOTCH
    val isDeckExpanded = drawerMode == OverlayState.COMMAND_DECK

    LaunchedEffect(isOverlayOpen) {
        if (isOverlayOpen) {
            if (drawerOffset.value > 0.05f) {
                isDismissing = false
                openTimestamp = SystemClock.uptimeMillis()
                drawerOffset.snapTo(1f)
                drawerOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    fun startDismiss(reason: String) {
        val elapsed = SystemClock.uptimeMillis() - openTimestamp
        if (elapsed < 500L) return
        if (isDismissing) return
        isDismissing = true

        coroutineScope.launch {
            drawerOffset.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
            onDismissComplete()
        }
    }

    LaunchedEffect(dismissSignal) {
        if (dismissSignal > 0 && !isDismissing) {
            startDismiss("DISMISS_SIGNAL_$dismissSignal")
        }
    }

    val bentoWidth by animateDpAsState(
        targetValue = if (isDeckExpanded) targetBentoWidthDp.dp else 0.dp,
        animationSpec = tween(durationMillis = 280, easing = SmoothCurve),
        label = "BentoWidthAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(totalContainerWidthDp.dp)
            .graphicsLayer { translationX = size.width * drawerOffset.value }
            .padding(end = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (!isDeckExpanded) {
            val transparentWidthDp = (totalContainerWidthDp - spineWidthDp).dp
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(transparentWidthDp)
                    .align(Alignment.CenterStart)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { startDismiss("TRANSPARENT_OUTSIDE_TAP") }
            )
        }

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentHeight()
        ) {
            if (isDeckExpanded) {
                Box(
                    modifier = Modifier
                        .width(bentoWidth)
                        .graphicsLayer {
                            clip = true
                            alpha = if (bentoWidth > 15.dp) ((bentoWidth - 15.dp) / (targetBentoWidthDp.dp - 15.dp)).coerceIn(0f, 1f) else 0f
                        }
                ) {
                    BentoDeckView(
                        onRequestDismiss = { startDismiss("HEADER_CLOSE_BUTTON") },
                        onCollapse = { onStateChange(OverlayState.QUICK_STRIP) },
                        volumeController = volumeController,
                        brightnessController = brightnessController,
                        flashlightController = flashlightController,
                        mediaController = mediaController,
                        scratchpadHelper = scratchpadHelper,
                        clipboardHelper = clipboardHelper,
                        widgetHost = widgetHost,
                        appWidgetManager = appWidgetManager,
                        onFocusChanged = onFocusChanged,
                        bentoWidthDp = targetBentoWidthDp.dp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            CapsuleSpineView(
                onExpandBentoDeck = {
                    if (isDeckExpanded) {
                        onStateChange(OverlayState.QUICK_STRIP)
                    } else {
                        onStateChange(OverlayState.COMMAND_DECK)
                    }
                },
                onDockToNotch = { startDismiss("SPINE_DOCK_BUTTON") },
                volumeController = volumeController,
                brightnessController = brightnessController,
                mediaController = mediaController,
                flashlightController = flashlightController,
                modifier = Modifier.width(64.dp)
            )
        }
    }
}
