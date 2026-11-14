package com.example.minidesk.ui.bento.widgets

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.clipboard.ClipboardVaultRepository

@Composable
fun ClipboardVaultWidget(
    repository: ClipboardVaultRepository,
    modifier: Modifier = Modifier
) {
    val clips by repository.clips.collectAsState()
    val view = LocalView.current
    var recentlyCopiedId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.syncSystemClipboard()
    }

    val pinnedClips = remember(clips) { clips.filter { it.isPinned } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF161616), RoundedCornerShape(18.dp))
            .border(0.5.dp, Color(0xFF242424), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                Text(
                    text = "CLIPBOARD VAULT",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
            }
            if (clips.any { !it.isPinned }) {
                Text(
                    text = "Clear",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        repository.clearUnpinned()
                    }
                )
            }
        }

        // 2. Pinned Shelf (Horizontal chips if any items are pinned)
        if (pinnedClips.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pinnedClips.forEach { pinned ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF242424), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0xFF383838), RoundedCornerShape(8.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                repository.copyToClipboard(pinned.text)
                                recentlyCopiedId = pinned.id
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Text(
                                text = pinned.text,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 3. Recent Clips List
        if (clips.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No clips recorded", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(clips, key = { it.id }) { clip ->
                    val isCopied = recentlyCopiedId == clip.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCopied) Color.White else Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                repository.copyToClipboard(clip.text)
                                recentlyCopiedId = clip.id
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCopied) "COPIED TO CLIPBOARD" else clip.text,
                            color = if (isCopied) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontFamily = if (isCopied) FontFamily.SansSerif else FontFamily.Monospace,
                            fontWeight = if (isCopied) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (clip.isPinned) (if (isCopied) Color.Black else Color.White) else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .size(13.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    repository.togglePin(clip.id)
                                }
                        )
                    }
                }
            }
        }

        // 4. Footer Telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${clips.size} items in vault",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Tap to inject",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}
