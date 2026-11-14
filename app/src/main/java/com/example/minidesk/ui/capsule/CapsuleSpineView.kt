package com.example.minidesk.ui.capsule

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.BrightnessController
import com.example.minidesk.MediaController
import com.example.minidesk.VolumeController
import com.example.minidesk.ui.components.RadialDialTile
import kotlin.math.roundToInt

@Composable
fun CapsuleSpineView(
    onExpandBentoDeck: () -> Unit,
    onDockToNotch: () -> Unit,
    volumeController: VolumeController,
    brightnessController: BrightnessController,
    mediaController: MediaController,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val liveTrackInfo = mediaController.liveTrackInfo

    val maxVol = volumeController.maxVolume
    val initialVolPercent = if (maxVol > 0) {
        (volumeController.currentVolume.toFloat() / maxVol.toFloat() * 100f).coerceIn(0f, 100f)
    } else 0f

    var smoothVolumePercent by remember { mutableFloatStateOf(initialVolPercent) }
    var lastAppliedVolIndex by remember { mutableIntStateOf(volumeController.currentVolume) }

    // Keep smoothVolumePercent in sync with external hardware volume button changes
    LaunchedEffect(volumeController.currentVolume) {
        val externalPercent = if (maxVol > 0) (volumeController.currentVolume.toFloat() / maxVol.toFloat() * 100f) else 0f
        val currentCalculatedIndex = ((smoothVolumePercent / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
        if (volumeController.currentVolume != currentCalculatedIndex) {
            smoothVolumePercent = externalPercent
            lastAppliedVolIndex = volumeController.currentVolume
        }
    }

    Surface(
        modifier = modifier.width(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF101010),
        border = BorderStroke(1.dp, Color(0xFF242424))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. 'M' Master Logo Button (46dp x 46dp)
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onExpandBentoDeck()
                },
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "M",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // 2. Volume Radial Scrubber Dial (0..100%)
            RadialDialTile(
                currentValue = smoothVolumePercent,
                onValueChange = { rawSmoothPercent ->
                    // 1. UPDATE THE UI STATE DIRECTLY WITH THE SMOOTH FLOAT (0..100)
                    smoothVolumePercent = rawSmoothPercent

                    // 2. Only update Android system audio when crossing an index threshold
                    val targetIndex = ((rawSmoothPercent / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
                    if (targetIndex != lastAppliedVolIndex) {
                        lastAppliedVolIndex = targetIndex
                        volumeController.setVolume(targetIndex)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // 3. Brightness Radial Scrubber Dial (0..100%)
            val currentBriPercent = (brightnessController.currentBrightness.toFloat() / 255f * 100f).coerceIn(0f, 100f)

            RadialDialTile(
                currentValue = currentBriPercent,
                onValueChange = { newPercent ->
                    val targetBri = (newPercent / 100f * 255f).toInt()
                    brightnessController.setBrightness(targetBri)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Brightness",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // 4. Media Play / Pause Button (46dp x 44dp squircle)
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    mediaController.togglePlayPause()
                },
                modifier = Modifier.size(width = 46.dp, height = 44.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E1E1E),
                border = BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (liveTrackInfo.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 5. Bento Grid Toggle Button (46dp x 44dp squircle)
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onExpandBentoDeck()
                },
                modifier = Modifier.size(width = 46.dp, height = 44.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E1E1E),
                border = BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Bento Grid Deck",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 6. Dock to Notch Button (32dp x 32dp circle)
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onDockToNotch()
                },
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = Color(0xFF1E1E1E),
                border = BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Dock to Notch",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
