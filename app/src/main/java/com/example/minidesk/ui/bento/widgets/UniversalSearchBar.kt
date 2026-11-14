package com.example.minidesk.ui.bento.widgets

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.minidesk.data.search.AppSearchRepository
import com.example.minidesk.data.search.SearchResultItem
import com.example.minidesk.data.settings.MiniDeskSettingsRepository

@Composable
fun rememberDrawablePainter(drawable: Drawable?): BitmapPainter? {
    return remember(drawable) {
        drawable?.toBitmap(48, 48)?.asImageBitmap()?.let {
            BitmapPainter(it)
        }
    }
}

@Composable
fun UniversalSearchBar(
    onFocusChanged: (Boolean) -> Unit,
    onActionComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    val settingsRepo = remember { MiniDeskSettingsRepository.get(context) }
    val prefs by settingsRepo.preferences.collectAsState()
    val isFreeformEnabled = prefs.enableFreeformLauncher

    LaunchedEffect(Unit) {
        AppSearchRepository.indexInstalledApps(context)
    }

    val results = remember(query) { AppSearchRepository.query(query) }

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Search Bar Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161616), RoundedCornerShape(14.dp))
                .border(0.5.dp, if (isFocused) Color.White else Color(0xFF262626), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (isFocused) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty() && !isFocused) {
                    Text(
                        text = "Search apps, settings, web...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            isFocused = it.isFocused
                            onFocusChanged(it.isFocused)
                        }
                )
            }

            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { query = "" }
                )
            }
        }

        // 2. Results Dropdown Shelf
        AnimatedVisibility(
            visible = query.isNotBlank() && results.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .padding(top = 6.dp)
                    .background(Color(0xFF121212), RoundedCornerShape(14.dp))
                    .border(0.5.dp, Color(0xFF242424), RoundedCornerShape(14.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(results, key = { it.id }) { item ->
                    SearchResultRow(
                        item = item,
                        isFreeformEnabled = isFreeformEnabled,
                        onLaunch = {
                            when (item) {
                                is SearchResultItem.AppItem -> {
                                    AppSearchRepository.launchApp(context, item.packageName, inFreeform = false)
                                }
                                is SearchResultItem.SettingsItem -> {
                                    AppSearchRepository.launchSettings(context, item.intentAction)
                                }
                                is SearchResultItem.WebQueryItem -> {
                                    AppSearchRepository.launchWeb(context, item.query)
                                }
                            }
                            query = ""
                            onActionComplete()
                        },
                        onLaunchFreeform = {
                            if (item is SearchResultItem.AppItem) {
                                AppSearchRepository.launchApp(context, item.packageName, inFreeform = true)
                                query = ""
                                onActionComplete()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: SearchResultItem,
    isFreeformEnabled: Boolean,
    onLaunch: () -> Unit,
    onLaunchFreeform: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF181818), RoundedCornerShape(10.dp))
            .clickable { onLaunch() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon
            when (item) {
                is SearchResultItem.AppItem -> {
                    val painter = rememberDrawablePainter(item.icon)
                    if (painter != null) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF242424), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.title.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is SearchResultItem.SettingsItem -> {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF242424), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                is SearchResultItem.WebQueryItem -> {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF242424), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Title + Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Freeform Floating Window Button (only for Apps when explicitly enabled)
        if (item is SearchResultItem.AppItem && isFreeformEnabled) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF222222), RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(6.dp))
                    .clickable { onLaunchFreeform() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Float", tint = Color.White, modifier = Modifier.size(11.dp))
                    Text("Float", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
