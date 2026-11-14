package com.example.minidesk.ui.bento

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.SystemClock
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.BrightnessController
import com.example.minidesk.ClipboardManagerHelper
import com.example.minidesk.FlashlightController
import com.example.minidesk.MediaController
import com.example.minidesk.ScratchpadHelper
import com.example.minidesk.VolumeController
import com.example.minidesk.data.audio.RealAudioRouteManager
import com.example.minidesk.data.clipboard.ClipboardVaultRepository
import com.example.minidesk.deck.BuiltinRegistry
import com.example.minidesk.deck.BuiltinSpec
import com.example.minidesk.deck.Limits
import com.example.minidesk.deck.WidgetDeck
import com.example.minidesk.deck.WidgetHostManager
import com.example.minidesk.deck.WidgetRepository
import com.example.minidesk.ui.bento.widgets.ClipboardVaultWidget
import com.example.minidesk.ui.bento.widgets.UniversalSearchBar
import com.example.minidesk.ui.bento.widgets.VolumeStreamsWidget
import com.example.minidesk.widgets.brightness.BrightnessWidget
import com.example.minidesk.widgets.common.WidgetHub
import com.example.minidesk.widgets.toggles.QuickTogglesWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BentoDeckView(
    onRequestDismiss: () -> Unit,
    onCollapse: () -> Unit = {},
    volumeController: VolumeController,
    brightnessController: BrightnessController,
    flashlightController: FlashlightController,
    mediaController: MediaController,
    scratchpadHelper: ScratchpadHelper,
    clipboardHelper: ClipboardManagerHelper,
    widgetHost: AppWidgetHost? = null,
    appWidgetManager: AppWidgetManager? = null,
    onFocusChanged: (Boolean) -> Unit = {},
    bentoWidthDp: Dp = 300.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val maxHeightCap = (configuration.screenHeightDp.dp * 0.80f)

    var isEditing by remember { mutableStateOf(false) }

    val hub = remember { WidgetHub.get(context) }
    val realAudioRouteManager = remember { RealAudioRouteManager(context) }
    val clipboardVaultRepo = remember { ClipboardVaultRepository.get(context) }

    DisposableEffect(Unit) {
        onDispose {
            realAudioRouteManager.release()
        }
    }

    val builtins = remember {
        BuiltinRegistry(
            listOf(
                BuiltinSpec("volume", "Volume Matrix", 3, 2, Limits(2, 1, 4, 4), onFirstRun = true) {
                    VolumeStreamsWidget(realAudioRouteManager)
                },
                BuiltinSpec("brightness", "Brightness", 2, 1, Limits(2, 1, 4, 2), onFirstRun = false) {
                    BrightnessWidget(hub.brightnessController)
                },
                BuiltinSpec("toggles", "Quick Toggles", 2, 2, Limits(2, 2, 4, 3), onFirstRun = false) {
                    QuickTogglesWidget(hub.quickTogglesController, onRequestDismiss)
                },
                BuiltinSpec("scratch", "Scratchpad", 4, 2, Limits(2, 2, 4, 6), onFirstRun = true) {
                    OledScratchpadWidget(scratchpadHelper, clipboardHelper, onFocusChanged)
                },
                BuiltinSpec("clipboard", "Clipboard Vault", 4, 2, Limits(2, 2, 4, 6), onFirstRun = true) {
                    ClipboardVaultWidget(clipboardVaultRepo)
                },
                BuiltinSpec("sysflow", "System Quick Flow", 4, 2, Limits(2, 1, 4, 4), onFirstRun = false) {
                    SystemQuickFlowWidget(flashlightController)
                },
                BuiltinSpec("stopwatch", "Stopwatch", 2, 2, Limits(1, 1, 4, 4), onFirstRun = true) {
                    PrecisionStopwatchWidget()
                },
                BuiltinSpec("calculator", "Calculator", 2, 2, Limits(1, 1, 4, 4), onFirstRun = false) {
                    CalculatorWidget()
                },
                BuiltinSpec("pomodoro", "Pomodoro", 2, 2, Limits(1, 1, 4, 4), onFirstRun = false) {
                    PomodoroWidget()
                }
            )
        )
    }

    Surface(
        modifier = modifier
            .width(bentoWidthDp)
            .heightIn(max = maxHeightCap),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF101010),
        border = BorderStroke(1.dp, Color(0xFF242424))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Bento Header
            BentoHeader(
                isEditing = isEditing,
                onToggleEdit = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    isEditing = !isEditing
                },
                onCollapse = onCollapse,
                onRequestDismiss = onRequestDismiss
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Universal Search Bar
            UniversalSearchBar(
                onFocusChanged = { hasFocus ->
                    onFocusChanged(hasFocus)
                },
                onActionComplete = {
                    onRequestDismiss()
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Main Widget Deck replacing old grid
            WidgetDeck(
                repo = WidgetRepository.get(context),
                hostManager = WidgetHostManager.get(context),
                builtins = builtins,
                editMode = isEditing,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BentoHeader(
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    onCollapse: () -> Unit,
    onRequestDismiss: () -> Unit
) {
    val view = LocalView.current
    var currentTimeString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            currentTimeString = formatter.format(Date())
            delay(1000L)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // M Logo Chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "M",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = "MiniDesk",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            if (currentTimeString.isNotEmpty()) {
                Text(
                    text = currentTimeString,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Edit Mode Toggle Button
            Surface(
                onClick = onToggleEdit,
                shape = RoundedCornerShape(8.dp),
                color = if (isEditing) Color.White else Color(0xFF181818),
                border = BorderStroke(1.dp, if (isEditing) Color.White else Color(0xFF282828))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Bento",
                        tint = if (isEditing) Color.Black else Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isEditing) "Done" else "Edit",
                        color = if (isEditing) Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Collapse to Spine Button
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onCollapse()
                },
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF181818),
                border = BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Collapse to Spine",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Close Button
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onRequestDismiss()
                },
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF181818),
                border = BorderStroke(1.dp, Color(0xFF282828))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Widget 3: System Quick Flow Banner
 */
@Composable
private fun SystemQuickFlowWidget(flashlightController: FlashlightController) {
    val context = LocalContext.current
    val view = LocalView.current

    var isWifiOn by remember { mutableStateOf(false) }
    var isBtOn by remember { mutableStateOf(false) }
    var isAutoRotateOn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val wifi = try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiManager?.isWifiEnabled == true
            } catch (e: Exception) { false }

            val bt = try {
                @Suppress("DEPRECATION")
                BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
            } catch (e: Exception) { false }

            val rotate = try {
                Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
            } catch (e: Exception) { false }

            withContext(Dispatchers.Main) {
                isWifiOn = wifi
                isBtOn = bt
                isAutoRotateOn = rotate
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color(0xFF242424))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "SYSTEM QUICK FLOW",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FlowPillButton(
                        label = "Wi-Fi",
                        icon = Icons.Default.Wifi,
                        isActive = isWifiOn,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            try { context.startActivity(Intent("android.settings.panel.action.INTERNET_CONNECTIVITY").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) }
                            catch (e: Exception) { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FlowPillButton(
                        label = "BT",
                        icon = Icons.Default.Bluetooth,
                        isActive = isBtOn,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            try { context.startActivity(Intent("android.settings.panel.action.BLUETOOTH").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) }
                            catch (e: Exception) { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FlowPillButton(
                        label = "Torch",
                        icon = Icons.Default.FlashOn,
                        isActive = flashlightController.isTorchOn,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            flashlightController.toggleTorch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FlowPillButton(
                        label = "DND",
                        icon = Icons.Default.DoNotDisturb,
                        isActive = false,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FlowPillButton(
                        label = "Hotspot",
                        icon = Icons.Default.SettingsCell,
                        isActive = false,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            context.startActivity(Intent("android.settings.TETHER_SETTINGS").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FlowPillButton(
                        label = "Rotate",
                        icon = Icons.Default.ScreenRotation,
                        isActive = isAutoRotateOn,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            if (Settings.System.canWrite(context)) {
                                val newVal = if (isAutoRotateOn) 0 else 1
                                try {
                                    Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, newVal)
                                    isAutoRotateOn = newVal == 1
                                } catch (e: Exception) { e.printStackTrace() }
                            } else {
                                context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowPillButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) Color.White else Color(0xFF181818),
        border = BorderStroke(1.dp, if (isActive) Color.White else Color(0xFF282828))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label,
                color = if (isActive) Color.Black else Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Widget 4: OLED Quick Notes Scratchpad
 */
@Composable
private fun OledScratchpadWidget(
    scratchpadHelper: ScratchpadHelper,
    clipboardHelper: ClipboardManagerHelper,
    onFocusChanged: (Boolean) -> Unit
) {
    val view = LocalView.current
    var copiedFeedback by remember { mutableStateOf(false) }

    val words = remember(scratchpadHelper.noteText) {
        if (scratchpadHelper.noteText.isBlank()) 0 else scratchpadHelper.noteText.trim().split("\\s+".toRegex()).size
    }

    LaunchedEffect(copiedFeedback) {
        if (copiedFeedback) {
            delay(1500L)
            copiedFeedback = false
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color(0xFF242424))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OLED SCRATCHPAD",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            clipboardHelper.copyToClipboard(scratchpadHelper.noteText)
                            clipboardHelper.addClip(scratchpadHelper.noteText, false)
                            copiedFeedback = true
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF181818),
                        border = BorderStroke(1.dp, Color(0xFF282828))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = if (copiedFeedback) "Copied!" else "Copy", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            scratchpadHelper.clear()
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF181818),
                        border = BorderStroke(1.dp, Color(0xFF282828))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Clear", color = Color(0xFF888888), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            BasicTextField(
                value = scratchpadHelper.noteText,
                onValueChange = { scratchpadHelper.updateNote(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .onFocusChanged { focusState ->
                        onFocusChanged(focusState.isFocused)
                    },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default
                ),
                cursorBrush = SolidColor(Color.White),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (scratchpadHelper.noteText.isEmpty()) {
                            Text(text = "Zero-latency note or scratchpad...", color = Color(0xFF555555), fontSize = 11.sp)
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$words words • ${scratchpadHelper.noteText.length} chars",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Widget 5: Precision Stopwatch
 */
@Composable
private fun PrecisionStopwatchWidget() {
    val view = LocalView.current

    var stopwatchStartTime by remember { mutableLongStateOf(0L) }
    var stopwatchElapsedMillis by remember { mutableLongStateOf(0L) }
    var isStopwatchRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isStopwatchRunning) {
        while (isStopwatchRunning) {
            stopwatchElapsedMillis = SystemClock.elapsedRealtime() - stopwatchStartTime
            delay(50L)
        }
    }

    val mins = (stopwatchElapsedMillis / 60000)
    val secs = (stopwatchElapsedMillis % 60000) / 1000
    val tenths = (stopwatchElapsedMillis % 1000) / 100

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color(0xFF242424))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "STOPWATCH", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format(Locale.US, "%02d:%02d.%d", mins, secs, tenths),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        if (!isStopwatchRunning) {
                            stopwatchStartTime = SystemClock.elapsedRealtime() - stopwatchElapsedMillis
                            isStopwatchRunning = true
                        } else {
                            isStopwatchRunning = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                    }
                }

                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        isStopwatchRunning = false
                        stopwatchElapsedMillis = 0L
                    },
                    modifier = Modifier.weight(1f).height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF181818),
                    border = BorderStroke(1.dp, Color(0xFF282828))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

/**
 * Widget 6: Calculator
 */
@Composable
private fun CalculatorWidget() {
    val view = LocalView.current
    var expr by remember { mutableStateOf("0") }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color(0xFF242424))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text(text = "Calc", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = expr, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("C", "7", "8", "9", "+").forEach { key ->
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            if (key == "C") expr = "0" else {
                                expr = if (expr == "0") key else expr + key
                            }
                        },
                        modifier = Modifier.weight(1f).height(26.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF181818),
                        border = BorderStroke(1.dp, Color(0xFF282828))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = key, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Widget 7: Focus Pomodoro
 */
@Composable
private fun PomodoroWidget() {
    val view = LocalView.current
    var pomoSecondsLeft by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && pomoSecondsLeft > 0) {
            delay(1000L)
            pomoSecondsLeft--
            if (pomoSecondsLeft == 0) {
                isRunning = false
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    val m = pomoSecondsLeft / 60
    val s = pomoSecondsLeft % 60

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color(0xFF242424))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text(text = "Pomo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = String.format(Locale.US, "%02d:%02d", m, s),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        isRunning = !isRunning
                    },
                    modifier = Modifier.weight(1f).height(26.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = if (isRunning) "Pause" else "Focus", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        isRunning = false
                        pomoSecondsLeft = 25 * 60
                    },
                    modifier = Modifier.weight(1f).height(26.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF181818),
                    border = BorderStroke(1.dp, Color(0xFF282828))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "Reset", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
