package com.example.minidesk.ui.tabs

import android.media.RingtoneManager
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.ScratchpadHelper
import kotlinx.coroutines.delay

@Composable
fun ToolsTab(
    scratchpadHelper: ScratchpadHelper,
    onCopyToClipboard: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isTimerMode by remember { mutableStateOf(true) }

    // Freeze-Proof Timer State using SystemClock.elapsedRealtime()
    var targetEndTime by remember { mutableLongStateOf(0L) }
    var remainingMillis by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Freeze-Proof Stopwatch State
    var stopwatchStartTime by remember { mutableLongStateOf(0L) }
    var stopwatchElapsedMillis by remember { mutableLongStateOf(0L) }
    var isStopwatchRunning by remember { mutableStateOf(false) }
    val lapList = remember { mutableStateListOf<Long>() }

    // Coroutine Freeze-Proof Timer Loop
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            val now = SystemClock.elapsedRealtime()
            val remaining = (targetEndTime - now).coerceAtLeast(0L)
            remainingMillis = remaining

            if (remaining == 0L) {
                isTimerRunning = false
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                try {
                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(context, notification)
                    r.play()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                break
            }
            delay(100)
        }
    }

    // Coroutine Freeze-Proof Stopwatch Loop
    LaunchedEffect(isStopwatchRunning) {
        while (isStopwatchRunning) {
            stopwatchElapsedMillis = SystemClock.elapsedRealtime() - stopwatchStartTime
            delay(50)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Tab Switcher for Timer / Stopwatch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { isTimerMode = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isTimerMode) Color.White else Color(0xFF111111),
                    contentColor = if (isTimerMode) Color.Black else Color.White
                ),
                border = BorderStroke(0.5.dp, Color(0xFF333333))
            ) {
                Text(text = "TIMER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = { isTimerMode = false },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (!isTimerMode) Color.White else Color(0xFF111111),
                    contentColor = if (!isTimerMode) Color.Black else Color.White
                ),
                border = BorderStroke(0.5.dp, Color(0xFF333333))
            ) {
                Text(text = "STOPWATCH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timer or Stopwatch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF262626)))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isTimerMode) {
                    val totalSecs = (remainingMillis / 1000).toInt()
                    val minutes = totalSecs / 60
                    val seconds = totalSecs % 60
                    val tenths = ((remainingMillis % 1000) / 100).toInt()

                    Text(
                        text = String.format("%02d:%02d.%d", minutes, seconds, tenths),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PresetButton(label = "+1m", onClick = {
                            val newDuration = remainingMillis + 60000L
                            remainingMillis = newDuration
                            targetEndTime = SystemClock.elapsedRealtime() + newDuration
                        })
                        PresetButton(label = "+5m", onClick = {
                            val newDuration = remainingMillis + 300000L
                            remainingMillis = newDuration
                            targetEndTime = SystemClock.elapsedRealtime() + newDuration
                        })
                        PresetButton(label = "+15m", onClick = {
                            val newDuration = remainingMillis + 900000L
                            remainingMillis = newDuration
                            targetEndTime = SystemClock.elapsedRealtime() + newDuration
                        })
                        PresetButton(label = "Reset", onClick = {
                            isTimerRunning = false
                            remainingMillis = 0L
                            targetEndTime = 0L
                        })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Inverted Solid White Play/Pause Pill Button
                        Surface(
                            onClick = {
                                if (remainingMillis > 0) {
                                    if (!isTimerRunning) {
                                        targetEndTime = SystemClock.elapsedRealtime() + remainingMillis
                                        isTimerRunning = true
                                    } else {
                                        isTimerRunning = false
                                    }
                                }
                            },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Start/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Dark Reset Button
                        Surface(
                            onClick = {
                                isTimerRunning = false
                                remainingMillis = 0L
                                targetEndTime = 0L
                            },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color(0xFF1A1A1A),
                            border = BorderStroke(0.5.dp, Color(0xFF262626))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    val mins = (stopwatchElapsedMillis / 60000)
                    val secs = (stopwatchElapsedMillis % 60000) / 1000
                    val tenths = (stopwatchElapsedMillis % 1000) / 100

                    Text(
                        text = String.format("%02d:%02d.%d", mins, secs, tenths),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            onClick = {
                                if (!isStopwatchRunning) {
                                    stopwatchStartTime = SystemClock.elapsedRealtime() - stopwatchElapsedMillis
                                    isStopwatchRunning = true
                                } else {
                                    isStopwatchRunning = false
                                }
                            },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                if (isStopwatchRunning) lapList.add(0, stopwatchElapsedMillis)
                            },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color(0xFF1A1A1A),
                            border = BorderStroke(0.5.dp, Color(0xFF262626))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Flag, contentDescription = "Lap", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            onClick = {
                                isStopwatchRunning = false
                                stopwatchElapsedMillis = 0L
                                lapList.clear()
                            },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color(0xFF1A1A1A),
                            border = BorderStroke(0.5.dp, Color(0xFF262626))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (lapList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            lapList.forEachIndexed { idx, lap ->
                                val lMins = (lap / 60000)
                                val lSecs = (lap % 60000) / 1000
                                val lMs = (lap % 1000) / 10
                                Text(
                                    text = "Lap ${lapList.size - idx}: ${String.format("%02d:%02d.%02d", lMins, lSecs, lMs)}",
                                    color = Color(0xFF888888),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auto-Saving Scratchpad Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUICK SCRATCHPAD",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onCopyToClipboard(scratchpadHelper.noteText)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A1A1A),
                    border = BorderStroke(0.5.dp, Color(0xFF222222))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Copy All", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        scratchpadHelper.clear()
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A1A1A),
                    border = BorderStroke(0.5.dp, Color(0xFF222222))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Clear", color = Color(0xFF888888), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF222222)))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                BasicTextField(
                    value = scratchpadHelper.noteText,
                    onValueChange = { scratchpadHelper.updateNote(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .onFocusChanged { focusState ->
                            onFocusChanged(focusState.isFocused)
                        },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Default
                    ),
                    cursorBrush = SolidColor(Color.White),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (scratchpadHelper.noteText.isEmpty()) {
                                Text(
                                    text = "Quick note or scratchpad...",
                                    color = Color(0xFF555555),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1A1A1A),
        border = BorderStroke(0.5.dp, Color(0xFF222222))
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
