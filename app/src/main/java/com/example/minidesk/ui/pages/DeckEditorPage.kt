package com.example.minidesk.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.minidesk.data.settings.MiniDeskPreferences
import com.example.minidesk.data.settings.MiniDeskSettingsRepository
import com.example.minidesk.data.settings.ToolShape
import com.example.minidesk.data.settings.defaultMiniToolSettings

data class MiniToolSpec(
    val id: String,
    val name: String,
    val desc: String,
    val glyph: String,
    val hasRing: Boolean = false
)

val ALL_MINI_TOOL_SPECS = listOf(
    MiniToolSpec("volume", "Volume", "Media or ring stream dial", "VOL", hasRing = true),
    MiniToolSpec("brightness", "Brightness", "Display level & auto toggle", "LUX", hasRing = true),
    MiniToolSpec("media", "Media Controls", "Play, pause, and skip", "▶"),
    MiniToolSpec("deck_btn", "Command Deck", "Expand to full Bento deck", "⊞"),
    MiniToolSpec("torch", "Torch", "LED flashlight toggle", "☀"),
    MiniToolSpec("timer", "Timer", "Quick countdown & stopwatch", "⏱", hasRing = true),
    MiniToolSpec("eyedropper", "Eyedropper", "One-frame color sampler", "◎"),
    MiniToolSpec("rotate", "Auto-Rotate", "Orientation lock toggle", "↻")
)

val COMMAND_DECK_MODULES = listOf(
    Triple("audio", "Audio Streams & Output", "Per-stream sliders and system output switcher"),
    Triple("connectivity", "Connectivity", "Wi-Fi and Bluetooth panel shortcuts"),
    Triple("scratchpad", "Scratchpad", "Quick notes with auto-save"),
    Triple("clipboard", "Clipboard Vault", "Encrypted clip history and pinned items"),
    Triple("eyedropper_mod", "Screen Studio", "One-frame MediaProjection eyedropper"),
    Triple("search", "App Search", "In-memory app and shortcut launcher")
)

@Composable
fun DeckEditorPage(
    prefs: MiniDeskPreferences,
    settingsRepo: MiniDeskSettingsRepository
) {
    val cg = prefs.colorGraph
    val boxShape = prefs.boxCornerShape()
    val btnShape = prefs.buttonCornerShape()
    val itemShape = prefs.resolveToolShape()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = MiniDeck, 1 = Command Deck
    var editingToolId by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        // Secondary Navbar: MiniDeck | Command Deck
        Row(
            modifier = Modifier
                .clip(boxShape)
                .background(Color(cg.surface))
                .border(1.dp, Color(cg.border), boxShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("MiniDeck", "Command Deck").forEachIndexed { idx, label ->
                val active = selectedSubTab == idx
                Box(
                    modifier = Modifier
                        .clip(btnShape)
                        .background(if (active) cg.primaryBrush() else SolidColor(Color.Transparent))
                        .clickable { selectedSubTab = idx }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (active) Color(cg.primaryText) else Color(cg.textSecondary),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (selectedSubTab == 0) {
            // ================= MINIDECK MANAGER =================

            // 1. Live Uniform MiniBar Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(boxShape)
                    .background(Color(cg.surface))
                    .border(1.dp, Color(cg.border), boxShape)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("MiniBar Live Preview", color = Color(cg.textSecondary), fontSize = 11.sp)

                    val spineShape = when (prefs.toolShape) {
                        ToolShape.CIRCLE -> RoundedCornerShape(32.dp)
                        ToolShape.SQUIRCLE -> RoundedCornerShape(22.dp)
                        ToolShape.GLOBAL -> RoundedCornerShape((prefs.globalRadiusDp + 6).coerceIn(8, 32).dp)
                    }

                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .clip(spineShape)
                            .background(cg.backgroundBrush())
                            .border(1.dp, Color(cg.border), spineShape)
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Top M Badge
                        Box(
                            modifier = Modifier.size(38.dp).clip(itemShape).background(cg.primaryBrush()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("M", color = Color(cg.primaryText), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Active Tool Slots
                        prefs.activeMiniToolIds.take(prefs.toolSlotsLimit).forEach { id ->
                            val spec = ALL_MINI_TOOL_SPECS.firstOrNull { it.id == id } ?: return@forEach
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(itemShape)
                                    .background(Color(cg.secondary))
                                    .border(
                                        width = if (spec.hasRing && prefs.showRadialRings) 1.5.dp else 1.dp,
                                        color = if (spec.hasRing && prefs.showRadialRings) Color(cg.primarySolid) else Color(cg.border),
                                        shape = itemShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(spec.glyph, color = Color(cg.textPrimary), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Dock Chevron
                        Box(
                            modifier = Modifier.size(26.dp).clip(itemShape).background(Color(cg.surface)).border(1.dp, Color(cg.border), itemShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("›", color = Color(cg.textSecondary), fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Slot Capacity & Uniform Shape Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(boxShape)
                    .background(Color(cg.surface))
                    .border(1.dp, Color(cg.border), boxShape)
            ) {
                // Tool Slots Stepper
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tool Slots", color = Color(cg.textPrimary), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Maximum visible tools in MiniBar", color = Color(cg.textSecondary), fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier
                            .clip(btnShape)
                            .background(Color(cg.secondary))
                            .border(1.dp, Color(cg.border), btnShape)
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp).clickable {
                                if (prefs.toolSlotsLimit > 2) {
                                    val newLimit = prefs.toolSlotsLimit - 1
                                    settingsRepo.updatePreferences {
                                        it.copy(
                                            toolSlotsLimit = newLimit,
                                            activeMiniToolIds = it.activeMiniToolIds.take(newLimit)
                                        )
                                    }
                                }
                            },
                            contentAlignment = Alignment.Center
                        ) { Text("−", color = Color(cg.textPrimary), fontSize = 14.sp) }

                        Text(
                            text = "${prefs.toolSlotsLimit}",
                            color = Color(cg.textPrimary),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )

                        Box(
                            modifier = Modifier.size(28.dp).clickable {
                                if (prefs.toolSlotsLimit < 8) {
                                    settingsRepo.updatePreferences { it.copy(toolSlotsLimit = it.toolSlotsLimit + 1) }
                                }
                            },
                            contentAlignment = Alignment.Center
                        ) { Text("+", color = Color(cg.textPrimary), fontSize = 14.sp) }
                    }
                }

                HorizontalDivider(color = Color(cg.border), thickness = 1.dp)

                // Uniform Tool Shape Selector
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text("Uniform Tool Shape", color = Color(cg.textPrimary), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Keep every dial and button consistent", color = Color(cg.textSecondary), fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ToolShape.CIRCLE to "Circle",
                            ToolShape.SQUIRCLE to "Squircle",
                            ToolShape.GLOBAL to "Global"
                        ).forEach { (shapeEnum, label) ->
                            val selected = prefs.toolShape == shapeEnum
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(btnShape)
                                    .background(if (selected) cg.primaryBrush() else SolidColor(Color(cg.secondary)))
                                    .border(1.dp, Color(cg.border), btnShape)
                                    .clickable { settingsRepo.updatePreferences { it.copy(toolShape = shapeEnum) } }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) Color(cg.primaryText) else Color(cg.textSecondary),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(cg.border), thickness = 1.dp)

                // Radial Rings Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Radial Progress Rings", color = Color(cg.textPrimary), fontSize = 14.sp)
                        Text("Show level progress on Volume & Brightness", color = Color(cg.textSecondary), fontSize = 12.sp)
                    }
                    Switch(
                        checked = prefs.showRadialRings,
                        onCheckedChange = { enabled -> settingsRepo.updatePreferences { it.copy(showRadialRings = enabled) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(cg.primaryText),
                            checkedTrackColor = Color(cg.primarySolid),
                            uncheckedThumbColor = Color(cg.textSecondary),
                            uncheckedTrackColor = Color(cg.secondary)
                        )
                    )
                }
            }

            // 3. Active MiniBar Tools (Reorder, Configure Settings, Remove)
            Text(
                text = "Active Tools (${prefs.activeMiniToolIds.size}/${prefs.toolSlotsLimit})",
                color = Color(cg.textSecondary),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(boxShape)
                    .background(Color(cg.surface))
                    .border(1.dp, Color(cg.border), boxShape)
            ) {
                if (prefs.activeMiniToolIds.isEmpty()) {
                    Box(modifier = Modifier.padding(18.dp)) {
                        Text("No active tools. Add tools below.", color = Color(cg.textSecondary), fontSize = 12.sp)
                    }
                } else {
                    prefs.activeMiniToolIds.forEachIndexed { idx, id ->
                        val spec = ALL_MINI_TOOL_SPECS.firstOrNull { it.id == id } ?: return@forEachIndexed
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(itemShape)
                                        .background(Color(cg.secondary))
                                        .border(1.dp, Color(cg.border), itemShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(spec.glyph, color = Color(cg.textPrimary), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                Column {
                                    Text(spec.name, color = Color(cg.textPrimary), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(spec.desc, color = Color(cg.textSecondary), fontSize = 11.sp)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Move Up
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(btnShape)
                                        .background(Color(cg.secondary))
                                        .border(1.dp, Color(cg.border), btnShape)
                                        .clickable(enabled = idx > 0) {
                                            val list = prefs.activeMiniToolIds.toMutableList()
                                            val tmp = list[idx - 1]; list[idx - 1] = list[idx]; list[idx] = tmp
                                            settingsRepo.updatePreferences { it.copy(activeMiniToolIds = list) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("↑", color = if (idx > 0) Color(cg.textPrimary) else Color(cg.textSecondary).copy(alpha = 0.3f), fontSize = 12.sp)
                                }

                                // Move Down
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(btnShape)
                                        .background(Color(cg.secondary))
                                        .border(1.dp, Color(cg.border), btnShape)
                                        .clickable(enabled = idx < prefs.activeMiniToolIds.lastIndex) {
                                            val list = prefs.activeMiniToolIds.toMutableList()
                                            val tmp = list[idx + 1]; list[idx + 1] = list[idx]; list[idx] = tmp
                                            settingsRepo.updatePreferences { it.copy(activeMiniToolIds = list) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("↓", color = if (idx < prefs.activeMiniToolIds.lastIndex) Color(cg.textPrimary) else Color(cg.textSecondary).copy(alpha = 0.3f), fontSize = 12.sp)
                                }

                                // Per-Tool Settings Button
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(btnShape)
                                        .background(Color(cg.secondary))
                                        .border(1.dp, Color(cg.border), btnShape)
                                        .clickable { editingToolId = spec.id }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Settings", color = Color(cg.textPrimary), fontSize = 11.sp)
                                }

                                // Remove Tool
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(btnShape)
                                        .background(Color(cg.secondary))
                                        .border(1.dp, Color(cg.border), btnShape)
                                        .clickable {
                                            val list = prefs.activeMiniToolIds.toMutableList()
                                            list.removeAt(idx)
                                            settingsRepo.updatePreferences { it.copy(activeMiniToolIds = list) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", color = Color(cg.textSecondary), fontSize = 11.sp)
                                }
                            }
                        }
                        if (idx < prefs.activeMiniToolIds.lastIndex) {
                            HorizontalDivider(color = Color(cg.border), thickness = 1.dp)
                        }
                    }
                }
            }

            // 4. Available Tools to Add
            val inactiveSpecs = ALL_MINI_TOOL_SPECS.filter { it.id !in prefs.activeMiniToolIds }
            if (inactiveSpecs.isNotEmpty()) {
                Text("Available Tools", color = Color(cg.textSecondary), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(boxShape)
                        .background(Color(cg.surface))
                        .border(1.dp, Color(cg.border), boxShape)
                ) {
                    val canAdd = prefs.activeMiniToolIds.size < prefs.toolSlotsLimit
                    inactiveSpecs.forEachIndexed { idx, spec ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(itemShape)
                                        .background(Color(cg.bgSolid))
                                        .border(1.dp, Color(cg.border), itemShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(spec.glyph, color = Color(cg.textSecondary), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                Column {
                                    Text(spec.name, color = Color(cg.textPrimary), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(spec.desc, color = Color(cg.textSecondary), fontSize = 11.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(btnShape)
                                    .background(if (canAdd) cg.primaryBrush() else SolidColor(Color(cg.secondary)))
                                    .clickable(enabled = canAdd) {
                                        settingsRepo.updatePreferences {
                                            it.copy(activeMiniToolIds = it.activeMiniToolIds + spec.id)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (canAdd) "+ Add" else "Full",
                                    color = if (canAdd) Color(cg.primaryText) else Color(cg.textSecondary),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (idx < inactiveSpecs.lastIndex) {
                            HorizontalDivider(color = Color(cg.border), thickness = 1.dp)
                        }
                    }
                }
            }
        } else {
            // ================= COMMAND DECK MANAGER =================
            Text("Command Deck Modules", color = Color(cg.textSecondary), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(boxShape)
                    .background(Color(cg.surface))
                    .border(1.dp, Color(cg.border), boxShape)
            ) {
                COMMAND_DECK_MODULES.forEachIndexed { idx, (modId, title, desc) ->
                    val enabled = prefs.commandDeckModuleStates[modId] ?: true
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = Color(cg.textPrimary), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(desc, color = Color(cg.textSecondary), fontSize = 12.sp)
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { checked ->
                                val nextMap = prefs.commandDeckModuleStates.toMutableMap()
                                nextMap[modId] = checked
                                settingsRepo.updatePreferences { it.copy(commandDeckModuleStates = nextMap) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(cg.primaryText),
                                checkedTrackColor = Color(cg.primarySolid),
                                uncheckedThumbColor = Color(cg.textSecondary),
                                uncheckedTrackColor = Color(cg.secondary)
                            )
                        )
                    }
                    if (idx < COMMAND_DECK_MODULES.lastIndex) {
                        HorizontalDivider(color = Color(cg.border), thickness = 1.dp)
                    }
                }
            }
        }
    }

    // Per-Tool Settings Modal
    editingToolId?.let { toolId ->
        val spec = ALL_MINI_TOOL_SPECS.firstOrNull { it.id == toolId }
        val currentMap = prefs.miniToolSettings[toolId] ?: defaultMiniToolSettings()[toolId].orEmpty()

        Dialog(onDismissRequest = { editingToolId = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(boxShape)
                    .background(Color(cg.surface))
                    .border(1.dp, Color(cg.border), boxShape)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${spec?.name ?: "Tool"} Settings", color = Color(cg.textPrimary), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(spec?.desc ?: "", color = Color(cg.textSecondary), fontSize = 11.sp)
                    }
                    Text(
                        text = "✕",
                        color = Color(cg.textSecondary),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { editingToolId = null }.padding(4.dp)
                    )
                }
                HorizontalDivider(color = Color(cg.border), thickness = 1.dp)

                currentMap.entries.forEachIndexed { idx, (k, v) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(k, color = Color(cg.textPrimary), fontSize = 13.sp)

                        if (v == "true" || v == "false") {
                            Switch(
                                checked = v == "true",
                                onCheckedChange = { checked ->
                                    val updatedOuter = prefs.miniToolSettings.toMutableMap()
                                    val updatedInner = currentMap.toMutableMap()
                                    updatedInner[k] = checked.toString()
                                    updatedOuter[toolId] = updatedInner
                                    settingsRepo.updatePreferences { it.copy(miniToolSettings = updatedOuter) }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(cg.primaryText),
                                    checkedTrackColor = Color(cg.primarySolid)
                                )
                            )
                        } else {
                            var textVal by remember(v) { mutableStateOf(v) }
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .clip(btnShape)
                                    .background(Color(cg.secondary))
                                    .border(1.dp, Color(cg.border), btnShape)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                BasicTextField(
                                    value = textVal,
                                    onValueChange = { newText ->
                                        textVal = newText
                                        val updatedOuter = prefs.miniToolSettings.toMutableMap()
                                        val updatedInner = currentMap.toMutableMap()
                                        updatedInner[k] = newText
                                        updatedOuter[toolId] = updatedInner
                                        settingsRepo.updatePreferences { it.copy(miniToolSettings = updatedOuter) }
                                    },
                                    textStyle = TextStyle(color = Color(cg.textPrimary), fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                    cursorBrush = SolidColor(Color(cg.primarySolid))
                                )
                            }
                        }
                    }
                    if (idx < currentMap.size - 1) {
                        HorizontalDivider(color = Color(cg.border), thickness = 1.dp)
                    }
                }

                HorizontalDivider(color = Color(cg.border), thickness = 1.dp)
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .clip(btnShape)
                            .background(cg.primaryBrush())
                            .clickable { editingToolId = null }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text("Done", color = Color(cg.primaryText), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
