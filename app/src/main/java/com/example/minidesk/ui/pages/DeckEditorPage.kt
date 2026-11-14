package com.example.minidesk.ui.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.settings.ColorGraph
import com.example.minidesk.data.settings.MiniDeskPreferences
import com.example.minidesk.data.settings.MiniDeskSettingsRepository
import com.example.minidesk.data.settings.ToolShape
import com.example.minidesk.deck.WidgetRepository

@Composable
fun DeckEditorPage(
    settingsRepo: MiniDeskSettingsRepository,
    prefs: MiniDeskPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = MiniDeck, 1 = Command Deck
    val cg = prefs.colorGraph

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-Navbar + Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(cg.surface),
                border = BorderStroke(1.dp, Color(cg.border))
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    Surface(
                        onClick = { activeSubTab = 0 },
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeSubTab == 0) Color(cg.primarySolid) else Color.Transparent,
                        contentColor = if (activeSubTab == 0) Color(cg.primaryText) else Color(cg.textSecondary)
                    ) {
                        Text("MiniDeck", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }

                    Surface(
                        onClick = { activeSubTab = 1 },
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeSubTab == 1) Color(cg.primarySolid) else Color.Transparent,
                        contentColor = if (activeSubTab == 1) Color(cg.primaryText) else Color(cg.textSecondary)
                    ) {
                        Text("Command Deck", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            Surface(
                onClick = { Toast.makeText(context, "Deck layout saved", Toast.LENGTH_SHORT).show() },
                shape = RoundedCornerShape(8.dp),
                color = Color(cg.primarySolid),
                contentColor = Color(cg.primaryText)
            ) {
                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }

        if (activeSubTab == 0) {
            // SUBPAGE A: MINIDECK MANAGER

            // Live MiniBar Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
                border = BorderStroke(0.5.dp, Color(cg.border))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("MiniBar Preview", color = Color(cg.textSecondary), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = CircleShape,
                        color = Color(cg.bgSolid),
                        border = BorderStroke(1.dp, Color(cg.border))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top M Badge
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = when (prefs.toolShape) {
                                    ToolShape.CIRCLE -> CircleShape
                                    ToolShape.SQUIRCLE -> RoundedCornerShape(10.dp)
                                    ToolShape.GLOBAL -> RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp)
                                },
                                color = Color(cg.primarySolid)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("M", color = Color(cg.primaryText), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Dynamic Tool Slots
                            listOf("VOL", "LUX", "▶", "⊞").take(prefs.toolSlotsLimit).forEach { toolIcon ->
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = when (prefs.toolShape) {
                                        ToolShape.CIRCLE -> CircleShape
                                        ToolShape.SQUIRCLE -> RoundedCornerShape(10.dp)
                                        ToolShape.GLOBAL -> RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp)
                                    },
                                    color = Color(cg.surface),
                                    border = BorderStroke(1.dp, if (prefs.showRadialRings && (toolIcon == "VOL" || toolIcon == "LUX")) Color(cg.primarySolid) else Color(cg.border))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(toolIcon, color = Color(cg.textPrimary), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            // Chevron Collapse
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = Color(cg.secondary)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("›", color = Color(cg.textSecondary), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Slot Capacity & Uniformity Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
                border = BorderStroke(0.5.dp, Color(cg.border))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Tool Slots Counter (-/+)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tool Slots", color = Color(cg.textPrimary), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Maximum visible tools in MiniBar", color = Color(cg.textSecondary), fontSize = 12.sp)
                        }

                        Surface(
                            shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                            color = Color(cg.secondary),
                            border = BorderStroke(1.dp, Color(cg.border))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("-", color = Color(cg.textPrimary), fontSize = 14.sp, modifier = Modifier.clickable { if (prefs.toolSlotsLimit > 2) settingsRepo.updatePreferences { it.copy(toolSlotsLimit = it.toolSlotsLimit - 1) } }.padding(horizontal = 10.dp, vertical = 4.dp))
                                Text("${prefs.toolSlotsLimit}", color = Color(cg.textPrimary), fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 6.dp))
                                Text("+", color = Color(cg.textPrimary), fontSize = 14.sp, modifier = Modifier.clickable { if (prefs.toolSlotsLimit < 8) settingsRepo.updatePreferences { it.copy(toolSlotsLimit = it.toolSlotsLimit + 1) } }.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(cg.border)) {}

                    // Uniform Tool Shape
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Uniform Tool Shape", color = Color(cg.textPrimary), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Keep every dial and button consistent", color = Color(cg.textSecondary), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(ToolShape.CIRCLE to "Circle", ToolShape.SQUIRCLE to "Squircle", ToolShape.GLOBAL to "Global").forEach { (shape, label) ->
                                val isSelected = prefs.toolShape == shape
                                Surface(
                                    onClick = { settingsRepo.updatePreferences { it.copy(toolShape = shape) } },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                                    color = if (isSelected) Color(cg.primarySolid) else Color(cg.secondary),
                                    contentColor = if (isSelected) Color(cg.primaryText) else Color(cg.textSecondary),
                                    border = BorderStroke(1.dp, Color(cg.border))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(cg.border)) {}

                    // Radial Rings Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Radial Progress Rings", color = Color(cg.textPrimary), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Show arc around Volume & Brightness", color = Color(cg.textSecondary), fontSize = 12.sp)
                        }

                        Switch(
                            checked = prefs.showRadialRings,
                            onCheckedChange = { checked -> settingsRepo.updatePreferences { it.copy(showRadialRings = checked) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(cg.primaryText),
                                checkedTrackColor = Color(cg.primarySolid),
                                uncheckedThumbColor = Color(cg.textSecondary),
                                uncheckedTrackColor = Color(cg.secondary)
                            )
                        )
                    }
                }
            }

            // Tools Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
                border = BorderStroke(0.5.dp, Color(cg.border))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ACTIVE TOOLS (${prefs.activeMiniToolIds.size}/${prefs.toolSlotsLimit})", color = Color(cg.textSecondary), fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    ToolItemRow("VOL", "Volume Stream", "Media or ring stream dial", true, cg)
                    ToolItemRow("LUX", "Brightness Level", "Display level & auto toggle", true, cg)
                    ToolItemRow("▶", "Media Controls", "Play, pause, and skip track", true, cg)
                    ToolItemRow("⊞", "Command Deck", "Expand to full 360dp deck", true, cg)
                }
            }
        } else {
            // SUBPAGE B: COMMAND DECK MANAGER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
                border = BorderStroke(0.5.dp, Color(cg.border))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("COMMAND DECK MODULES (360DP)", color = Color(cg.textSecondary), fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    ToolItemRow("AUD", "Audio Streams & Switcher", "Per-stream sliders & system output switcher", true, cg)
                    ToolItemRow("NET", "Connectivity Panel", "Wi-Fi & Bluetooth panel shortcuts", true, cg)
                    ToolItemRow("NOTE", "Scratchpad", "Quick notes with auto-save", true, cg)
                    ToolItemRow("CLIP", "Clipboard Vault", "Encrypted clip history & pinned items", true, cg)
                    ToolItemRow("LOUPE", "Screen Studio", "One-frame MediaProjection eyedropper", true, cg)
                    ToolItemRow("SEARCH", "App Search", "In-memory app & settings launcher", true, cg)
                }
            }
        }
    }
}

@Composable
private fun ToolItemRow(iconText: String, title: String, subtitle: String, isEnabled: Boolean, cg: ColorGraph) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(cg.surfaceRaised),
        border = BorderStroke(0.5.dp, Color(cg.border))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(cg.secondary),
                    border = BorderStroke(1.dp, Color(cg.border))
                ) {
                    Text(iconText, color = Color(cg.textPrimary), fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                }

                Column {
                    Text(title, color = Color(cg.textPrimary), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(subtitle, color = Color(cg.textSecondary), fontSize = 11.sp)
                }
            }

            Checkbox(
                checked = isEnabled,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors(checkedColor = Color(cg.primarySolid), checkmarkColor = Color(cg.primaryText), uncheckedColor = Color(cg.border))
            )
        }
    }
}
