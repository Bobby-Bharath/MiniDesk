package com.example.minidesk.ui.capsule

import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.BrightnessController
import com.example.minidesk.FlashlightController
import com.example.minidesk.MediaController
import com.example.minidesk.VolumeController
import com.example.minidesk.data.settings.HapticMode
import com.example.minidesk.data.settings.MiniDeskSettingsRepository
import com.example.minidesk.data.settings.ToolShape
import com.example.minidesk.ui.components.RadialDialTile
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CapsuleSpineView(
    onExpandBentoDeck: () -> Unit,
    onDockToNotch: () -> Unit,
    volumeController: VolumeController,
    brightnessController: BrightnessController,
    mediaController: MediaController,
    flashlightController: FlashlightController? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val settingsRepo = remember { MiniDeskSettingsRepository.get(context) }
    val prefs by settingsRepo.preferences.collectAsState()

    val cg = prefs.colorGraph
    val itemShape = prefs.resolveToolShape()
    val globalRadius = prefs.globalRadiusDp.toFloat()
    val spineShape = when (prefs.toolShape) {
        ToolShape.CIRCLE -> RoundedCornerShape(32.dp)
        ToolShape.SQUIRCLE -> RoundedCornerShape(22.dp)
        ToolShape.GLOBAL -> RoundedCornerShape((globalRadius + 6f).coerceIn(8f, 32f).dp)
    }

    fun triggerHaptic() {
        when (prefs.hapticMode) {
            HapticMode.OFF -> Unit
            HapticMode.SUBTLE -> view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            HapticMode.CRISP -> view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    val liveTrackInfo = mediaController.liveTrackInfo
    val maxVol = volumeController.maxVolume.coerceAtLeast(1)
    val initialVolPercent = (volumeController.currentVolume.toFloat() / maxVol.toFloat() * 100f).coerceIn(0f, 100f)

    var smoothVolumePercent by remember { mutableFloatStateOf(initialVolPercent) }
    var lastAppliedVolIndex by remember { mutableIntStateOf(volumeController.currentVolume) }

    LaunchedEffect(volumeController.currentVolume) {
        val externalPercent = (volumeController.currentVolume.toFloat() / maxVol.toFloat() * 100f).coerceIn(0f, 100f)
        val currentCalculatedIndex = ((smoothVolumePercent / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
        if (volumeController.currentVolume != currentCalculatedIndex) {
            smoothVolumePercent = externalPercent
            lastAppliedVolIndex = volumeController.currentVolume
        }
    }

    val visibleTools = remember(prefs.activeMiniToolIds, prefs.toolSlotsLimit) {
        prefs.activeMiniToolIds.take(prefs.toolSlotsLimit)
    }

    Box(
        modifier = modifier
            .width(64.dp)
            .clip(spineShape)
            .background(cg.backgroundBrush())
            .border(1.dp, Color(cg.border), spineShape)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Master 'M' Header Button (Uniform shape + Primary brush)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(itemShape)
                    .background(cg.primaryBrush())
                    .clickable {
                        triggerHaptic()
                        onExpandBentoDeck()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = Color(cg.primaryText),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. Dynamic Active MiniBar Tool Slots (Uniform shape + Per-Tool Settings)
            visibleTools.forEach { toolId ->
                val toolCfg = prefs.miniToolSettings[toolId].orEmpty()

                when (toolId) {
                    "volume" -> {
                        val tapToMute = toolCfg["Tap to Mute"] == "true"
                        RadialDialTile(
                            currentValue = smoothVolumePercent,
                            onValueChange = { rawSmoothPercent ->
                                smoothVolumePercent = rawSmoothPercent
                                val targetIndex = ((rawSmoothPercent / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
                                if (targetIndex != lastAppliedVolIndex) {
                                    lastAppliedVolIndex = targetIndex
                                    volumeController.setVolume(targetIndex)
                                    triggerHaptic()
                                }
                            },
                            shape = itemShape,
                            toolShapeType = prefs.toolShape,
                            cornerRadiusDp = globalRadius * 0.625f,
                            showRing = prefs.showRadialRings,
                            surfaceColor = Color(cg.secondary),
                            borderColor = Color(cg.border),
                            progressBrush = cg.primaryBrush(),
                            contentColor = Color(cg.textPrimary),
                            onTap = if (tapToMute) {
                                {
                                    triggerHaptic()
                                    val nextVol = if (volumeController.currentVolume > 0) 0 else (maxVol / 2)
                                    volumeController.setVolume(nextVol)
                                    smoothVolumePercent = (nextVol.toFloat() / maxVol * 100f)
                                }
                            } else null,
                            icon = {
                                Icon(
                                    imageVector = if (volumeController.currentVolume == 0) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = Color(cg.textPrimary),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    "brightness" -> {
                        val currentBriPercent = (brightnessController.currentBrightness.toFloat() / 255f * 100f).coerceIn(0f, 100f)
                        RadialDialTile(
                            currentValue = currentBriPercent,
                            onValueChange = { newPercent ->
                                val targetBri = (newPercent / 100f * 255f).toInt()
                                brightnessController.setBrightness(targetBri)
                            },
                            shape = itemShape,
                            toolShapeType = prefs.toolShape,
                            cornerRadiusDp = globalRadius * 0.625f,
                            showRing = prefs.showRadialRings,
                            surfaceColor = Color(cg.secondary),
                            borderColor = Color(cg.border),
                            progressBrush = cg.primaryBrush(),
                            contentColor = Color(cg.textPrimary),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Brightness",
                                    tint = Color(cg.textPrimary),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    "media" -> {
                        val swipeSkip = toolCfg["Swipe to Skip"] != "false"
                        var dragAccum by remember { mutableFloatStateOf(0f) }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(itemShape)
                                .background(if (liveTrackInfo.isPlaying) cg.primaryBrush() else SolidColor(Color(cg.secondary)))
                                .border(1.dp, Color(cg.border), itemShape)
                                .clickable {
                                    triggerHaptic()
                                    mediaController.togglePlayPause()
                                }
                                .then(
                                    if (swipeSkip) {
                                        Modifier.pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = { dragAccum = 0f },
                                                onHorizontalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAccum += dragAmount
                                                    if (abs(dragAccum) > 36f) {
                                                        triggerHaptic()
                                                        if (dragAccum > 0) mediaController.nextTrack() else mediaController.previousTrack()
                                                        dragAccum = 0f
                                                    }
                                                }
                                            )
                                        }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (liveTrackInfo.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = if (liveTrackInfo.isPlaying) Color(cg.primaryText) else Color(cg.textPrimary),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    "deck_btn" -> {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(itemShape)
                                .background(Color(cg.secondary))
                                .border(1.dp, Color(cg.border), itemShape)
                                .clickable {
                                    triggerHaptic()
                                    onExpandBentoDeck()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Expand Deck",
                                tint = Color(cg.textPrimary),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    "torch" -> {
                        val isOn = flashlightController?.isTorchOn == true
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(itemShape)
                                .background(if (isOn) cg.primaryBrush() else SolidColor(Color(cg.secondary)))
                                .border(1.dp, Color(cg.border), itemShape)
                                .clickable {
                                    triggerHaptic()
                                    flashlightController?.toggleTorch()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Torch",
                                tint = if (isOn) Color(cg.primaryText) else Color(cg.textPrimary),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    "timer" -> {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(itemShape)
                                .background(Color(cg.secondary))
                                .border(1.dp, Color(cg.border), itemShape)
                                .clickable {
                                    triggerHaptic()
                                    onExpandBentoDeck()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = Color(cg.textPrimary),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    "eyedropper" -> {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(itemShape)
                                .background(Color(cg.secondary))
                                .border(1.dp, Color(cg.border), itemShape)
                                .clickable {
                                    triggerHaptic()
                                    onExpandBentoDeck()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Colorize,
                                contentDescription = "Eyedropper",
                                tint = Color(cg.textPrimary),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    "rotate" -> {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(itemShape)
                                .background(Color(cg.secondary))
                                .border(1.dp, Color(cg.border), itemShape)
                                .clickable {
                                    triggerHaptic()
                                    if (Settings.System.canWrite(context)) {
                                        val cur = Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
                                        val next = if (cur == 1) 0 else 1
                                        Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, next)
                                        if (toolCfg["Toast on Toggle"] != "false") {
                                            Toast.makeText(context, if (next == 1) "Auto-Rotate On" else "Rotation Locked", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        brightnessController.requestWriteSettingsPermission()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Auto-Rotate",
                                tint = Color(cg.textPrimary),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 3. Dock to Notch Button (Matches uniform itemShape at 32dp)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(itemShape)
                    .background(Color(cg.surface))
                    .border(1.dp, Color(cg.border), itemShape)
                    .clickable {
                        triggerHaptic()
                        onDockToNotch()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Dock to Notch",
                    tint = Color(cg.textSecondary),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
