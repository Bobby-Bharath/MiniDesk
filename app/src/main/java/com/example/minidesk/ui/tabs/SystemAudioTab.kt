package com.example.minidesk.ui.tabs

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.minidesk.AudioRoutingManager
import com.example.minidesk.FlashlightController
import com.example.minidesk.VolumeController

@SuppressLint("MissingPermission")
@Composable
fun SystemAudioTab(
    volumeController: VolumeController,
    flashlightController: FlashlightController
) {
    val context = LocalContext.current
    val view = LocalView.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val audioRoutingManager = remember { AudioRoutingManager(context) }

    var ringVolume by remember {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_RING))
    }
    val maxRingVolume = remember {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
    }

    var isAutoRotateOn by remember {
        mutableStateOf(
            try {
                Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
            } catch (e: Exception) { false }
        )
    }

    var ringerMode by remember { mutableIntStateOf(audioManager.ringerMode) }

    LaunchedEffect(Unit) {
        audioRoutingManager.refreshDevices()
        audioRoutingManager.refreshActiveSoundSource()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Active Sound Source App Indicator Card
        val activeApp = audioRoutingManager.activeSoundApp
        if (activeApp != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF333333)))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeApp.icon != null) {
                        Image(
                            bitmap = activeApp.icon.toBitmap(32, 32).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${activeApp.appName} playing audio",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Output Route: ${audioRoutingManager.getActiveDeviceName()}",
                            color = Color(0xFF888888),
                            fontSize = 9.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        Text(
            text = "AUDIO FLOW MATRIX",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sliders Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF222222)))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Media Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Media Stream", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${((volumeController.currentVolume.toFloat() / volumeController.maxVolume.coerceAtLeast(1)) * 100).toInt()}%",
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }

                Slider(
                    value = volumeController.currentVolume.toFloat(),
                    onValueChange = {
                        volumeController.setVolume(it.toInt())
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    },
                    valueRange = 0f..volumeController.maxVolume.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color(0xFF333333)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Ring/Notification Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Ring / Notification", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${((ringVolume.toFloat() / maxRingVolume.coerceAtLeast(1)) * 100).toInt()}%",
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }

                Slider(
                    value = ringVolume.toFloat(),
                    onValueChange = {
                        ringVolume = it.toInt()
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, ringVolume, 0)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    },
                    valueRange = 0f..maxRingVolume.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color(0xFF333333)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Output Device Switcher
        Text(
            text = "OUTPUT ROUTING",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        AudioRoutingChips(audioRoutingManager = audioRoutingManager)

        Spacer(modifier = Modifier.height(14.dp))

        // System Toggles Grid (2x3)
        Text(
            text = "SYSTEM TOGGLES",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleCard(
                    title = "Wi-Fi",
                    subtitle = "Inline Panel",
                    icon = Icons.Default.Wifi,
                    isActive = true,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        val panelIntent = Intent("android.settings.panel.action.INTERNET_CONNECTIVITY").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(panelIntent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                ToggleCard(
                    title = "Bluetooth",
                    subtitle = "Inline Panel",
                    icon = Icons.Default.Bluetooth,
                    isActive = true,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        val btPanel = Intent("android.settings.panel.action.BLUETOOTH").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(btPanel)
                        } catch (e: Exception) {
                            try {
                                @Suppress("DEPRECATION")
                                val adapter = BluetoothAdapter.getDefaultAdapter()
                                if (adapter != null) {
                                    if (adapter.isEnabled) adapter.disable() else adapter.enable()
                                }
                            } catch (ex: Exception) {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleCard(
                    title = "Flashlight",
                    subtitle = if (flashlightController.isTorchOn) "ACTIVE" else "OFF",
                    icon = if (flashlightController.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    isActive = flashlightController.isTorchOn,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        flashlightController.toggleTorch()
                    },
                    modifier = Modifier.weight(1f)
                )
                ToggleCard(
                    title = "Auto-Rotate",
                    subtitle = if (isAutoRotateOn) "ENABLED" else "LOCKED",
                    icon = Icons.Default.ScreenRotation,
                    isActive = isAutoRotateOn,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        if (Settings.System.canWrite(context)) {
                            val newVal = if (isAutoRotateOn) 0 else 1
                            Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, newVal)
                            isAutoRotateOn = newVal == 1
                        } else {
                            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleCard(
                    title = "Location",
                    subtitle = "GPS Settings",
                    icon = Icons.Default.LocationOn,
                    isActive = true,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    },
                    modifier = Modifier.weight(1f)
                )
                ToggleCard(
                    title = "Ringer Mode",
                    subtitle = when (ringerMode) {
                        AudioManager.RINGER_MODE_NORMAL -> "NORMAL"
                        AudioManager.RINGER_MODE_VIBRATE -> "VIBRATE"
                        else -> "SILENT"
                    },
                    icon = if (ringerMode == AudioManager.RINGER_MODE_NORMAL) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                    isActive = ringerMode == AudioManager.RINGER_MODE_NORMAL,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        val nextMode = when (ringerMode) {
                            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                            else -> AudioManager.RINGER_MODE_NORMAL
                        }
                        audioManager.ringerMode = nextMode
                        ringerMode = nextMode
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AudioRoutingChips(audioRoutingManager: AudioRoutingManager) {
    val view = LocalView.current
    val devices = audioRoutingManager.outputDevices

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(devices) { dev ->
            val isSelected = dev.type == audioRoutingManager.activeDeviceType
            val icon = when {
                dev.name.contains("Bluetooth", ignoreCase = true) || dev.name.contains("Buds", ignoreCase = true) -> Icons.Default.Headphones
                dev.name.contains("Earpiece", ignoreCase = true) -> Icons.Default.PhoneAndroid
                else -> Icons.Default.Speaker
            }

            ChipItem(
                label = dev.name,
                icon = icon,
                isSelected = isSelected,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    audioRoutingManager.selectDevice(dev)
                }
            )
        }
    }
}

@Composable
private fun ChipItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color.White else Color(0xFF111111),
        border = BorderStroke(0.5.dp, if (isSelected) Color.White else Color(0xFF333333))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) Color(0xFF181818) else Color(0xFF111111)),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (isActive) Color(0xFF444444) else Color(0xFF222222)))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = if (isActive) Color.White else Color(0xFF888888), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color(0xFF888888), fontSize = 9.sp)
        }
    }
}
