package com.example.minidesk.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.BrightnessController
import com.example.minidesk.FlashlightController
import com.example.minidesk.MediaController
import com.example.minidesk.OverlayState
import com.example.minidesk.VolumeController
import kotlin.math.abs

@Composable
fun QuickStripView(
    onStateChange: (OverlayState) -> Unit,
    onRequestDismiss: () -> Unit,
    volumeController: VolumeController,
    brightnessController: BrightnessController,
    flashlightController: FlashlightController,
    mediaController: MediaController
) {
    val view = LocalView.current
    val liveInfo = mediaController.liveTrackInfo

    LaunchedEffect(Unit) {
        volumeController.update()
        brightnessController.update()
    }

    // Slim 70dp Container
    Column(
        modifier = Modifier
            .width(70.dp)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Slot 1: Volume Tile
        val volPct = (volumeController.currentVolume.toFloat() / volumeController.maxVolume.coerceAtLeast(1)).coerceIn(0f, 1f)
        var volDragAccum by remember { mutableFloatStateOf(0f) }
        var lastVol by remember { mutableIntStateOf(volumeController.currentVolume) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        volDragAccum += dragAmount
                        val step = 10f
                        if (abs(volDragAccum) >= step) {
                            val delta = if (volDragAccum > 0) 1 else -1
                            val newVol = (volumeController.currentVolume + delta).coerceIn(0, volumeController.maxVolume)
                            if (newVol != lastVol) {
                                volumeController.setVolume(newVol)
                                lastVol = newVol
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                            volDragAccum %= step
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF262626)))
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (volumeController.currentVolume == 0) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Vol",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${(volPct * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262626))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(volPct)
                            .height(4.dp)
                            .background(Color.White)
                    )
                }
            }
        }

        // Slot 2: Brightness Tile
        val brightPct = (brightnessController.currentBrightness.toFloat() / 255f).coerceIn(0f, 1f)
        var brightDragAccum by remember { mutableFloatStateOf(0f) }
        var lastBright by remember { mutableIntStateOf(brightnessController.currentBrightness) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        brightDragAccum += dragAmount
                        val step = 10f
                        if (abs(brightDragAccum) >= step) {
                            val delta = if (brightDragAccum > 0) 15 else -15
                            val newBright = (brightnessController.currentBrightness + delta).coerceIn(0, 255)
                            if (newBright != lastBright) {
                                brightnessController.setBrightness(newBright)
                                lastBright = newBright
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                            brightDragAccum %= step
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF262626)))
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Bright",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${(brightPct * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262626))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(brightPct)
                            .height(4.dp)
                            .background(Color.White)
                    )
                }
            }
        }

        // Slot 3: Split Dual Tile (Flashlight + Media)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Flashlight button
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    flashlightController.toggleTorch()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (flashlightController.isTorchOn) Color.White else Color(0xFF1A1A1A),
                border = BorderStroke(0.5.dp, Color(0xFF262626))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (flashlightController.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch",
                        tint = if (flashlightController.isTorchOn) Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Media Play/Pause button
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    mediaController.togglePlayPause()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1A1A1A),
                border = BorderStroke(0.5.dp, Color(0xFF262626))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (mediaController.isLiveModeEnabled && liveInfo.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Slot 4: Expand to Command Deck
        Surface(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onStateChange(OverlayState.COMMAND_DECK)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1A1A1A),
            border = BorderStroke(0.5.dp, Color(0xFF262626))
        ) {
            Box(
                modifier = Modifier.padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Deck <",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
