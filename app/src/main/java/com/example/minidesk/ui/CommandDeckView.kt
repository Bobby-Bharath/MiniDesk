package com.example.minidesk.ui

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.AudioRoutingManager
import com.example.minidesk.ClipboardManagerHelper
import com.example.minidesk.FlashlightController
import com.example.minidesk.MediaController
import com.example.minidesk.ScratchpadHelper
import com.example.minidesk.VolumeController
import com.example.minidesk.ui.tabs.ClipboardTab
import com.example.minidesk.ui.tabs.SearchTab
import com.example.minidesk.ui.tabs.ToolsTab

enum class CommandDeckTab {
    TELEMETRY_SYSTEM,
    PRODUCTIVITY_HUB,
    CLIPBOARD_VAULT,
    UNIVERSAL_SEARCH
}

@Composable
fun CommandDeckView(
    onRequestDismiss: () -> Unit,
    volumeController: VolumeController,
    flashlightController: FlashlightController,
    mediaController: MediaController,
    scratchpadHelper: ScratchpadHelper,
    clipboardHelper: ClipboardManagerHelper,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    val view = LocalView.current
    var activeTab by remember { mutableStateOf(CommandDeckTab.TELEMETRY_SYSTEM) }
    val configuration = LocalConfiguration.current
    val maxHeightCap = (configuration.screenHeightDp.dp * 0.75f)

    Surface(
        modifier = Modifier
            .width(340.dp)
            .heightIn(max = maxHeightCap),
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        color = Color(0xFF000000),
        border = BorderStroke(0.5.dp, Color(0xFF222222))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Canvas (~284dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF000000))
            ) {
                Crossfade(
                    targetState = activeTab,
                    label = "CommandDeckTabCrossfade"
                ) { tab ->
                    when (tab) {
                        CommandDeckTab.TELEMETRY_SYSTEM -> {
                            TelemetrySystemCanvas(
                                volumeController = volumeController,
                                flashlightController = flashlightController,
                                mediaController = mediaController
                            )
                        }
                        CommandDeckTab.PRODUCTIVITY_HUB -> {
                            ToolsTab(
                                scratchpadHelper = scratchpadHelper,
                                onCopyToClipboard = { text ->
                                    clipboardHelper.copyToClipboard(text)
                                    clipboardHelper.addClip(text, false)
                                },
                                onFocusChanged = onFocusChanged
                            )
                        }
                        CommandDeckTab.CLIPBOARD_VAULT -> {
                            ClipboardTab(clipboardHelper = clipboardHelper)
                        }
                        CommandDeckTab.UNIVERSAL_SEARCH -> {
                            SearchTab(
                                onRequestDismiss = onRequestDismiss,
                                onFocusChanged = onFocusChanged
                            )
                        }
                    }
                }
            }

            // Right Thumb Rail (~56dp wide, closest to right edge)
            Surface(
                modifier = Modifier.width(56.dp),
                color = Color(0xFF0A0A0A),
                border = BorderStroke(0.5.dp, Color(0xFF222222))
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThumbRailButton(
                            icon = Icons.Default.GraphicEq,
                            contentDescription = "Telemetry & System",
                            isSelected = activeTab == CommandDeckTab.TELEMETRY_SYSTEM,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                activeTab = CommandDeckTab.TELEMETRY_SYSTEM
                            }
                        )

                        ThumbRailButton(
                            icon = Icons.Default.Timer,
                            contentDescription = "Productivity Hub",
                            isSelected = activeTab == CommandDeckTab.PRODUCTIVITY_HUB,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                activeTab = CommandDeckTab.PRODUCTIVITY_HUB
                            }
                        )

                        ThumbRailButton(
                            icon = Icons.Default.ContentPaste,
                            contentDescription = "Clipboard Vault",
                            isSelected = activeTab == CommandDeckTab.CLIPBOARD_VAULT,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                activeTab = CommandDeckTab.CLIPBOARD_VAULT
                            }
                        )

                        ThumbRailButton(
                            icon = Icons.Default.Search,
                            contentDescription = "Universal Search",
                            isSelected = activeTab == CommandDeckTab.UNIVERSAL_SEARCH,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                activeTab = CommandDeckTab.UNIVERSAL_SEARCH
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Square Close Button at Bottom of Rail
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onRequestDismiss()
                        },
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(0.5.dp, Color(0xFF262626))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThumbRailButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White else Color(0xFF141414),
        border = BorderStroke(0.5.dp, if (isSelected) Color.White else Color(0xFF262626))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) Color.Black else Color(0xFF888888),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Tab 1: Real Telemetry & System Canvas
 * Contains ONLY:
 * 1) Real Audio Output & Media Card
 * 2) Real System Toggles (2x2 Grid)
 */
@Composable
private fun TelemetrySystemCanvas(
    volumeController: VolumeController,
    flashlightController: FlashlightController,
    mediaController: MediaController
) {
    val context = LocalContext.current
    val view = LocalView.current
    val liveInfo = mediaController.liveTrackInfo
    val audioRoutingManager = remember { AudioRoutingManager(context) }

    LaunchedEffect(Unit) {
        audioRoutingManager.refreshDevices()
        audioRoutingManager.refreshActiveSoundSource()
    }

    // Defensive Hardware State Queries wrapped in safe try/catch(SecurityException) blocks
    val isWifiEnabled = remember(context) {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.isWifiEnabled == true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    val wifiSsid = remember(context, isWifiEnabled) {
        if (isWifiEnabled) {
            try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                @Suppress("DEPRECATION")
                val info = wifiManager?.connectionInfo
                val ssid = info?.ssid?.replace("\"", "")
                if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") ssid else "Wi-Fi Active"
            } catch (e: SecurityException) {
                "Wi-Fi Active"
            } catch (e: Exception) {
                "Wi-Fi Active"
            }
        } else "Disconnected"
    }

    val isBtEnabled = remember(context) {
        try {
            @Suppress("DEPRECATION", "MissingPermission")
            val adapter = BluetoothAdapter.getDefaultAdapter()
            adapter?.isEnabled == true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
    val btSubtitle = if (isBtEnabled) "Bluetooth 5.3" else "Standby"

    val isGpsEnabled = remember(context) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
    val locationSubtitle = if (isGpsEnabled) "GPS Active" else "Location Off"

    var isAutoRotateOn by remember {
        mutableStateOf(
            try {
                Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
            } catch (e: SecurityException) {
                false
            } catch (e: Exception) {
                false
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TELEMETRY & SYSTEM",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )

            PillBadge(label = "OLED")
        }

        // Real Audio Output & Media Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF262626)))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIO OUTPUT",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    PillBadge(label = "🎧 ${audioRoutingManager.getActiveDeviceName()}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                val hasTrack = liveInfo.title.isNotEmpty()
                Text(
                    text = if (hasTrack) liveInfo.title else "No Active Playback",
                    color = if (hasTrack) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (hasTrack && liveInfo.artist.isNotEmpty()) liveInfo.artist else if (hasTrack) "Media Session" else "Tap to play media",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Transport Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        mediaController.previousTrack()
                    }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            mediaController.togglePlayPause()
                        },
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (liveInfo.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        mediaController.nextTrack()
                    }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                    }
                }
            }
        }

        // Real System Quick Actions (2x2 Grid)
        Text(
            text = "SYSTEM TOGGLES",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionGridCard(
                    title = "Wi-Fi",
                    subtitle = wifiSsid,
                    icon = Icons.Default.Wifi,
                    hasDot = isWifiEnabled,
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

                ActionGridCard(
                    title = "Bluetooth",
                    subtitle = btSubtitle,
                    icon = Icons.Default.Bluetooth,
                    hasDot = isBtEnabled,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        val btPanel = Intent("android.settings.panel.action.BLUETOOTH").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(btPanel)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionGridCard(
                    title = "Location",
                    subtitle = locationSubtitle,
                    icon = Icons.Default.LocationOn,
                    hasDot = isGpsEnabled,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    },
                    modifier = Modifier.weight(1f)
                )

                ActionGridCard(
                    title = "Auto-Rotate",
                    subtitle = if (isAutoRotateOn) "Gyro Unlocked" else "Locked",
                    icon = Icons.Default.ScreenRotation,
                    hasDot = isAutoRotateOn,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        if (Settings.System.canWrite(context)) {
                            val newVal = if (isAutoRotateOn) 0 else 1
                            try {
                                Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, newVal)
                                isAutoRotateOn = newVal == 1
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    hasDot: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF262626)))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(16.dp))
                if (hasDot) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A1A))
                            .border(0.5.dp, Color(0xFF262626), CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PillBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1A1A),
        border = BorderStroke(0.5.dp, Color(0xFF262626))
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
