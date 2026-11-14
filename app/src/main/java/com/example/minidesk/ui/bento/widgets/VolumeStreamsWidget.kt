package com.example.minidesk.ui.bento.widgets

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.audio.RealAudioRouteManager
import kotlin.math.roundToInt

@Composable
fun VolumeStreamsWidget(
    audioRouteManager: RealAudioRouteManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    val activeDeviceName by audioRouteManager.activeDeviceName.collectAsState()

    // Hardware max streams
    val maxMedia = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat().coerceAtLeast(1f) }
    val maxRing = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_RING).toFloat().coerceAtLeast(1f) }
    val maxAlarm = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM).toFloat().coerceAtLeast(1f) }

    // Smooth 1-by-1 local UI float states (0..100)
    var mediaPct by remember { mutableFloatStateOf((audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / maxMedia) * 100f) }
    var ringPct by remember { mutableFloatStateOf((audioManager.getStreamVolume(AudioManager.STREAM_RING) / maxRing) * 100f) }
    var alarmPct by remember { mutableFloatStateOf((audioManager.getStreamVolume(AudioManager.STREAM_ALARM) / maxAlarm) * 100f) }

    var lastMediaIdx by remember { mutableIntStateOf(-1) }
    var lastRingIdx by remember { mutableIntStateOf(-1) }
    var lastAlarmIdx by remember { mutableIntStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF161616), RoundedCornerShape(18.dp))
            .border(0.5.dp, Color(0xFF242424), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP HEADER: Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUDIO MATRIX",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
        }

        // 2. ACTION CONTROLS ROW: Route Chip, Mixer Chip, Mute Chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // A. Route Selector Chip (Single Button that triggers SystemUI Dialog)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                    .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        audioRouteManager.openSystemMediaSwitcher()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = if (activeDeviceName.contains("phone", true) || activeDeviceName.contains("speaker", true))
                            Icons.Default.Speaker else Icons.Default.Headphones,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = activeDeviceName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Switch",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
            }

            // B. System Mixer Chip
            Box(
                modifier = Modifier
                    .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                    .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        audioManager.adjustSuggestedStreamVolume(
                            AudioManager.ADJUST_SAME,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.FLAG_SHOW_UI
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text("Mixer", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // C. Mute All Chip
            Box(
                modifier = Modifier
                    .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                    .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        if (mediaPct > 0f) {
                            mediaPct = 0f
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                        } else {
                            mediaPct = 60f
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (0.6f * maxMedia).roundToInt(), 0)
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (mediaPct > 0f) "Mute" else "Unmute",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3. SLIDERS MATRIX: Media, Ring, Alarm (Smooth 1% increment)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AudioStreamSliderRow("Media", mediaPct, onScrub = { newPct ->
                mediaPct = newPct
                val idx = ((newPct / 100f) * maxMedia).roundToInt().coerceIn(0, maxMedia.toInt())
                if (idx != lastMediaIdx) {
                    lastMediaIdx = idx
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, idx, 0)
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            })

            AudioStreamSliderRow("Ring", ringPct, onScrub = { newPct ->
                ringPct = newPct
                val idx = ((newPct / 100f) * maxRing).roundToInt().coerceIn(0, maxRing.toInt())
                if (idx != lastRingIdx) {
                    lastRingIdx = idx
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, idx, 0)
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            })

            AudioStreamSliderRow("Alarm", alarmPct, onScrub = { newPct ->
                alarmPct = newPct
                val idx = ((newPct / 100f) * maxAlarm).roundToInt().coerceIn(0, maxAlarm.toInt())
                if (idx != lastAlarmIdx) {
                    lastAlarmIdx = idx
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, idx, 0)
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            })
        }
    }
}

@Composable
private fun AudioStreamSliderRow(
    label: String,
    percentage: Float,
    onScrub: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(36.dp)
        )

        // Interactive Track
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val pct = ((offset.x / size.width) * 100f).coerceIn(0f, 100f)
                        onScrub(pct)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val pct = ((change.position.x / size.width) * 100f).coerceIn(0f, 100f)
                        onScrub(pct)
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val trackWidth = maxWidth
            // Background Inactive Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF262626), RoundedCornerShape(3.dp))
            )
            // Active Fill Bar
            Box(
                modifier = Modifier
                    .width(trackWidth * (percentage / 100f).coerceIn(0.01f, 1f))
                    .height(6.dp)
                    .background(Color.White, RoundedCornerShape(3.dp))
            )
        }

        Text(
            text = "${percentage.roundToInt()}%",
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp)
        )
    }
}
