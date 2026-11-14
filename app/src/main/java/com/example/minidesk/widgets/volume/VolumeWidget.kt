package com.example.minidesk.widgets.volume

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.deck.DeckColors
import com.example.minidesk.widgets.common.DeckSlider
import com.example.minidesk.widgets.common.IconAction
import com.example.minidesk.widgets.common.SectionLabel
import com.example.minidesk.widgets.common.WidgetSize
import com.example.minidesk.widgets.common.calculateWidgetSize
import kotlin.math.roundToInt

fun triggerAppSpecificVolume(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    // 1. Trigger native system volume slider with UI (reveals Vivo multi-app volume expander)
    try {
        audioManager.adjustSuggestedStreamVolume(
            AudioManager.ADJUST_SAME,
            AudioManager.STREAM_MUSIC,
            AudioManager.FLAG_SHOW_UI
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 2. Attempt direct launch of Vivo / OEM Multi-App Volume Settings
    val appVolIntents = listOf(
        Intent("com.vivo.settings.APP_VOLUME_SETTINGS"),
        Intent("android.settings.APP_VOLUME_SETTINGS"),
        Intent("com.android.settings.sound.APP_VOLUME_SETTINGS")
    )
    for (intent in appVolIntents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            break
        } catch (e: Exception) { /* fall through to next */ }
    }
}

@Composable
fun VolumeWidget(
    controller: VolumeController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(controller) {
        val observer = controller.registerObserver {
            // Observer handles state updates
        }
        onDispose {
            controller.unregisterObserver(observer)
        }
    }

    val maxMedia = controller.maxMedia
    val maxRing = controller.maxRing
    val maxAlarm = controller.maxAlarm

    val initialMediaPct = if (maxMedia > 0) controller.mediaVol.toFloat() / maxMedia else 0f
    val initialRingPct = if (maxRing > 0) controller.ringVol.toFloat() / maxRing else 0f
    val initialAlarmPct = if (maxAlarm > 0) controller.alarmVol.toFloat() / maxAlarm else 0f

    var mediaPercent by remember { mutableFloatStateOf(initialMediaPct) }
    var ringPercent by remember { mutableFloatStateOf(initialRingPct) }
    var alarmPercent by remember { mutableFloatStateOf(initialAlarmPct) }

    var lastMediaIndex by remember { mutableIntStateOf(controller.mediaVol) }
    var lastRingIndex by remember { mutableIntStateOf(controller.ringVol) }
    var lastAlarmIndex by remember { mutableIntStateOf(controller.alarmVol) }

    // Sync from hardware volume button presses
    LaunchedEffect(controller.mediaVol) {
        val externalPct = if (maxMedia > 0) controller.mediaVol.toFloat() / maxMedia else 0f
        if ((mediaPercent * maxMedia).roundToInt() != controller.mediaVol) {
            mediaPercent = externalPct
            lastMediaIndex = controller.mediaVol
        }
    }

    LaunchedEffect(controller.ringVol) {
        val externalPct = if (maxRing > 0) controller.ringVol.toFloat() / maxRing else 0f
        if ((ringPercent * maxRing).roundToInt() != controller.ringVol) {
            ringPercent = externalPct
            lastRingIndex = controller.ringVol
        }
    }

    LaunchedEffect(controller.alarmVol) {
        val externalPct = if (maxAlarm > 0) controller.alarmVol.toFloat() / maxAlarm else 0f
        if ((alarmPercent * maxAlarm).roundToInt() != controller.alarmVol) {
            alarmPercent = externalPct
            lastAlarmIndex = controller.alarmVol
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val size = calculateWidgetSize(maxWidth, maxHeight)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DeckColors.Panel,
            border = BorderStroke(0.5.dp, DeckColors.Hairline),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("VOLUME")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // App Specific Volume Pill
                        Surface(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                triggerAppSpecificVolume(context)
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = DeckColors.Panel,
                            border = BorderStroke(0.5.dp, DeckColors.Hairline)
                        ) {
                            Text(
                                text = "App Vol",
                                color = DeckColors.Paper,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        if (size == WidgetSize.Large) {
                            Text(
                                text = if (controller.mediaVol == 0) "MUTED" else "ACTIVE",
                                color = DeckColors.Mute,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                when (size) {
                    WidgetSize.Compact -> {
                        DeckSlider(
                            value = mediaPercent,
                            onValueChange = { frac ->
                                mediaPercent = frac
                                val targetIdx = (frac * maxMedia).roundToInt().coerceIn(0, maxMedia)
                                if (targetIdx != lastMediaIndex) {
                                    lastMediaIndex = targetIdx
                                    controller.setMediaFraction(frac)
                                }
                            },
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = "Media"
                        )
                    }
                    WidgetSize.Medium -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DeckSlider(
                                value = mediaPercent,
                                onValueChange = { frac ->
                                    mediaPercent = frac
                                    val targetIdx = (frac * maxMedia).roundToInt().coerceIn(0, maxMedia)
                                    if (targetIdx != lastMediaIndex) {
                                        lastMediaIndex = targetIdx
                                        controller.setMediaFraction(frac)
                                    }
                                },
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                label = "Media"
                            )
                            DeckSlider(
                                value = ringPercent,
                                onValueChange = { frac ->
                                    ringPercent = frac
                                    val targetIdx = (frac * maxRing).roundToInt().coerceIn(0, maxRing)
                                    if (targetIdx != lastRingIndex) {
                                        lastRingIndex = targetIdx
                                        controller.setRingFraction(frac)
                                    }
                                },
                                icon = Icons.Default.Notifications,
                                label = "Ring"
                            )
                            DeckSlider(
                                value = alarmPercent,
                                onValueChange = { frac ->
                                    alarmPercent = frac
                                    val targetIdx = (frac * maxAlarm).roundToInt().coerceIn(0, maxAlarm)
                                    if (targetIdx != lastAlarmIndex) {
                                        lastAlarmIndex = targetIdx
                                        controller.setAlarmFraction(frac)
                                    }
                                },
                                icon = Icons.Default.Alarm,
                                label = "Alarm"
                            )
                        }
                    }
                    WidgetSize.Large -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DeckSlider(
                                    value = mediaPercent,
                                    onValueChange = { frac ->
                                        mediaPercent = frac
                                        val targetIdx = (frac * maxMedia).roundToInt().coerceIn(0, maxMedia)
                                        if (targetIdx != lastMediaIndex) {
                                            lastMediaIndex = targetIdx
                                            controller.setMediaFraction(frac)
                                        }
                                    },
                                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                                    label = "Media",
                                    modifier = Modifier.weight(1f)
                                )
                                IconAction(
                                    icon = Icons.AutoMirrored.Filled.VolumeMute,
                                    contentDescription = "Mute Media",
                                    onClick = { controller.toggleMuteMedia() }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DeckSlider(
                                    value = ringPercent,
                                    onValueChange = { frac ->
                                        ringPercent = frac
                                        val targetIdx = (frac * maxRing).roundToInt().coerceIn(0, maxRing)
                                        if (targetIdx != lastRingIndex) {
                                            lastRingIndex = targetIdx
                                            controller.setRingFraction(frac)
                                        }
                                    },
                                    icon = Icons.Default.Notifications,
                                    label = "Ring",
                                    modifier = Modifier.weight(1f)
                                )
                                IconAction(
                                    icon = Icons.AutoMirrored.Filled.VolumeMute,
                                    contentDescription = "Mute Ring",
                                    onClick = { controller.setRingFraction(0f) }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DeckSlider(
                                    value = alarmPercent,
                                    onValueChange = { frac ->
                                        alarmPercent = frac
                                        val targetIdx = (frac * maxAlarm).roundToInt().coerceIn(0, maxAlarm)
                                        if (targetIdx != lastAlarmIndex) {
                                            lastAlarmIndex = targetIdx
                                            controller.setAlarmFraction(frac)
                                        }
                                    },
                                    icon = Icons.Default.Alarm,
                                    label = "Alarm",
                                    modifier = Modifier.weight(1f)
                                )
                                IconAction(
                                    icon = Icons.AutoMirrored.Filled.VolumeMute,
                                    contentDescription = "Mute Alarm",
                                    onClick = { controller.setAlarmFraction(0f) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
