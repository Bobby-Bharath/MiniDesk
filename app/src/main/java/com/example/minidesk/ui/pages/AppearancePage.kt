package com.example.minidesk.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.settings.ColorGraph
import com.example.minidesk.data.settings.HapticMode
import com.example.minidesk.data.settings.InnerIndicator
import com.example.minidesk.data.settings.MiniDeskPreferences
import com.example.minidesk.data.settings.MiniDeskSettingsRepository
import com.example.minidesk.data.settings.ThemePreset
import com.example.minidesk.data.settings.TriggerShape
import kotlin.math.roundToInt

@Composable
fun AppearancePage(
    settingsRepo: MiniDeskSettingsRepository,
    prefs: MiniDeskPreferences,
    modifier: Modifier = Modifier
) {
    val cg = prefs.colorGraph
    var isPreviewOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOOKS & FEEL",
                color = Color(cg.textSecondary),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Reset",
                color = Color(cg.textSecondary),
                fontSize = 12.sp,
                modifier = Modifier.clickable {
                    settingsRepo.updatePreferences {
                        it.copy(
                            globalRadiusDp = 16,
                            restingOpacity = 0.90f,
                            colorGraph = ColorGraph()
                        )
                    }
                }
            )
        }

        // Live Interactive Phone Screen Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
            border = BorderStroke(0.5.dp, Color(cg.border))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(224.dp)
                        .clip(RoundedCornerShape(prefs.globalRadiusDp.dp))
                        .background(Color(cg.bgSolid))
                        .border(1.dp, Color(cg.border), RoundedCornerShape(prefs.globalRadiusDp.dp))
                ) {
                    Text(
                        text = "Tap trigger to preview",
                        color = Color(cg.textSecondary).copy(alpha = 0.40f),
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Simulated Handle Element
                    val simulatedYOffset = (224.dp * prefs.yPositionPercent) - (prefs.handleHeightDp.dp * 0.25f)
                    val handleAlignment = if (prefs.isRightEdge) Alignment.TopEnd else Alignment.TopStart
                    val handleXOffset = if (prefs.isRightEdge) -prefs.edgeGapDp.dp else prefs.edgeGapDp.dp

                    Surface(
                        onClick = { isPreviewOpen = !isPreviewOpen },
                        modifier = Modifier
                            .align(handleAlignment)
                            .offset { IntOffset(x = handleXOffset.value.roundToInt(), y = simulatedYOffset.value.roundToInt()) }
                            .width(
                                if (prefs.triggerShape == TriggerShape.FLOATING_PUCK) prefs.puckDiameterDp.dp
                                else prefs.handleWidthDp.dp
                            )
                            .height(
                                if (prefs.triggerShape == TriggerShape.FLOATING_PUCK) prefs.puckDiameterDp.dp
                                else (prefs.handleHeightDp * 0.5f).dp
                            )
                            .alpha(prefs.restingOpacity),
                        shape = when (prefs.triggerShape) {
                            TriggerShape.FLOATING_PUCK -> CircleShape
                            TriggerShape.EDGE_BAR -> RoundedCornerShape(if (prefs.isRightEdge) 6.dp else 0.dp, if (!prefs.isRightEdge) 6.dp else 0.dp, if (!prefs.isRightEdge) 6.dp else 0.dp, if (prefs.isRightEdge) 6.dp else 0.dp)
                            TriggerShape.FLOATING_PILL -> RoundedCornerShape(prefs.globalRadiusDp.coerceAtMost(28).dp)
                            else -> CircleShape
                        },
                        color = Color(cg.surface),
                        border = BorderStroke(1.dp, Color(cg.border))
                    ) {
                        if (prefs.innerIndicator != InnerIndicator.NONE) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .width(if (prefs.innerIndicator == InnerIndicator.LINE) 2.dp else 4.dp)
                                        .height(if (prefs.innerIndicator == InnerIndicator.LINE) 16.dp else 4.dp)
                                        .background(Color(cg.primarySolid), CircleShape)
                                )
                            }
                        }
                    }

                    // Simulated Expanded Deck
                    if (isPreviewOpen) {
                        Surface(
                            modifier = Modifier
                                .align(if (prefs.isRightEdge) Alignment.CenterEnd else Alignment.CenterStart)
                                .width(176.dp)
                                .fillMaxHeight()
                                .padding(12.dp),
                            shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
                            color = Color(cg.surface),
                            border = BorderStroke(1.dp, Color(cg.border))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Command Deck", color = Color(cg.textPrimary), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text("✕", color = Color(cg.textSecondary), fontSize = 11.sp, modifier = Modifier.clickable { isPreviewOpen = false })
                                    }
                                    SimDeckRow("Volume", "72%", cg)
                                    SimDeckRow("Brightness", "Auto", cg)
                                    Surface(
                                        shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                                        color = Color(cg.primarySolid)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Media", color = Color(cg.primaryText), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            Text("Playing", color = Color(cg.primaryText), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                                Text("Tap outside to close", color = Color(cg.textSecondary), fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${if (prefs.triggerShape == TriggerShape.EDGE_BAR) "Edge Bar" else if (prefs.triggerShape == TriggerShape.FLOATING_PUCK) "Floating Puck" else if (prefs.triggerShape == TriggerShape.SPLIT_BAR) "Split Bar" else if (prefs.triggerShape == TriggerShape.ORGANIC_TEARDROP) "Teardrop Notch" else "Floating Pill"} • ${if (isPreviewOpen) "Expanded" else "Collapsed"}",
                    color = Color(cg.textSecondary),
                    fontSize = 11.sp
                )
            }
        }

        // Trigger Shape Group (5 Shapes)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Trigger Shape", color = Color(cg.textSecondary), fontSize = 12.sp, fontWeight = FontWeight.Medium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShapeCard("Edge Bar", "Flush handle", isSelected = prefs.triggerShape == TriggerShape.EDGE_BAR, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences { it.copy(triggerShape = TriggerShape.EDGE_BAR) }
                }

                ShapeCard("Floating Pill", "Detached bar", isSelected = prefs.triggerShape == TriggerShape.FLOATING_PILL, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences { it.copy(triggerShape = TriggerShape.FLOATING_PILL) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShapeCard("Floating Puck", "Circular tap", isSelected = prefs.triggerShape == TriggerShape.FLOATING_PUCK, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences { it.copy(triggerShape = TriggerShape.FLOATING_PUCK) }
                }

                ShapeCard("Split Bar", "Dual-zone", isSelected = prefs.triggerShape == TriggerShape.SPLIT_BAR, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences { it.copy(triggerShape = TriggerShape.SPLIT_BAR) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShapeCard("Teardrop Notch", "Bézier curve", isSelected = prefs.triggerShape == TriggerShape.ORGANIC_TEARDROP, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences { it.copy(triggerShape = TriggerShape.ORGANIC_TEARDROP) }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Dynamic Per-Shape Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
            border = BorderStroke(0.5.dp, Color(cg.border))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val shapeTitle = when (prefs.triggerShape) {
                    TriggerShape.EDGE_BAR -> "Edge Bar Settings"
                    TriggerShape.FLOATING_PILL -> "Floating Pill Settings"
                    TriggerShape.FLOATING_PUCK -> "Floating Puck Settings"
                    TriggerShape.SPLIT_BAR -> "Split Bar Settings"
                    TriggerShape.ORGANIC_TEARDROP -> "Teardrop Notch Settings"
                }
                Text(shapeTitle, color = Color(cg.textPrimary), fontSize = 13.sp, fontWeight = FontWeight.Medium)

                // Edge Side Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Display Edge", color = Color(cg.textPrimary), fontSize = 13.sp)

                    Surface(
                        shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                        color = Color(cg.secondary),
                        border = BorderStroke(1.dp, Color(cg.border))
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            Surface(
                                onClick = { settingsRepo.updatePreferences { it.copy(isRightEdge = false) } },
                                shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                                color = if (!prefs.isRightEdge) Color(cg.primarySolid) else Color.Transparent,
                                contentColor = if (!prefs.isRightEdge) Color(cg.primaryText) else Color(cg.textSecondary)
                            ) {
                                Text("Left", fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            }

                            Surface(
                                onClick = { settingsRepo.updatePreferences { it.copy(isRightEdge = true) } },
                                shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                                color = if (prefs.isRightEdge) Color(cg.primarySolid) else Color.Transparent,
                                contentColor = if (prefs.isRightEdge) Color(cg.primaryText) else Color(cg.textSecondary)
                            ) {
                                Text("Right", fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                // Edge Gap Slider (Pill, Puck, Split)
                if (prefs.triggerShape != TriggerShape.EDGE_BAR && prefs.triggerShape != TriggerShape.ORGANIC_TEARDROP) {
                    MetricSliderRow(
                        label = "Edge Gap (Clears back-swipe)",
                        valueText = "${prefs.edgeGapDp} dp",
                        value = prefs.edgeGapDp.toFloat(),
                        valueRange = 4f..28f,
                        cg = cg,
                        onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(edgeGapDp = newVal.toInt()) } }
                    )
                }

                // Vertical Position Slider
                MetricSliderRow(
                    label = "Vertical Position",
                    valueText = "${(prefs.yPositionPercent * 100).roundToInt()}%",
                    value = prefs.yPositionPercent,
                    valueRange = 0.15f..0.85f,
                    cg = cg,
                    onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(yPositionPercent = newVal) } }
                )

                // Length & Width Sliders
                if (prefs.triggerShape == TriggerShape.FLOATING_PUCK) {
                    MetricSliderRow(
                        label = "Diameter",
                        valueText = "${prefs.puckDiameterDp} dp",
                        value = prefs.puckDiameterDp.toFloat(),
                        valueRange = 24f..52f,
                        cg = cg,
                        onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(puckDiameterDp = newVal.toInt()) } }
                    )
                } else {
                    MetricSliderRow(
                        label = "Length",
                        valueText = "${prefs.handleHeightDp} dp",
                        value = prefs.handleHeightDp.toFloat(),
                        valueRange = 48f..150f,
                        cg = cg,
                        onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(handleHeightDp = newVal.toInt()) } }
                    )

                    MetricSliderRow(
                        label = "Thickness / Width",
                        valueText = "${prefs.handleWidthDp} dp",
                        value = prefs.handleWidthDp.toFloat(),
                        valueRange = 4f..24f,
                        cg = cg,
                        onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(handleWidthDp = newVal.toInt()) } }
                    )
                }

                // Inner Indicator Selector (for Pill)
                if (prefs.triggerShape == TriggerShape.FLOATING_PILL) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Inner Indicator", color = Color(cg.textPrimary), fontSize = 13.sp)

                        Surface(
                            shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                            color = Color(cg.secondary),
                            border = BorderStroke(1.dp, Color(cg.border))
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                listOf(InnerIndicator.NONE to "None", InnerIndicator.LINE to "Line", InnerIndicator.DOT to "Dot").forEach { (ind, label) ->
                                    val isSelected = prefs.innerIndicator == ind
                                    Surface(
                                        onClick = { settingsRepo.updatePreferences { it.copy(innerIndicator = ind) } },
                                        shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                                        color = if (isSelected) Color(cg.primarySolid) else Color.Transparent,
                                        contentColor = if (isSelected) Color(cg.primaryText) else Color(cg.textSecondary)
                                    ) {
                                        Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Global Roundness & Geometry Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
            border = BorderStroke(0.5.dp, Color(cg.border))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Global Roundness
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Global Roundness", color = Color(cg.textPrimary), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${prefs.globalRadiusDp} dp", color = Color(cg.textSecondary), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text("Uniform corner radius across all cards, buttons, and sidebar", color = Color(cg.textSecondary), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = prefs.globalRadiusDp.toFloat(),
                        onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(globalRadiusDp = newVal.toInt()) } },
                        valueRange = 0f..28f,
                        colors = SliderDefaults.colors(thumbColor = Color(cg.primarySolid), activeTrackColor = Color(cg.primarySolid), inactiveTrackColor = Color(cg.border))
                    )
                }

                Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(cg.border)) {}

                // Resting Opacity
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Resting Opacity", color = Color(cg.textSecondary), fontSize = 12.sp)
                        Text("${(prefs.restingOpacity * 100).roundToInt()}%", color = Color(cg.textPrimary), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = prefs.restingOpacity,
                        onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(restingOpacity = newVal) } },
                        valueRange = 0.15f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(cg.primarySolid), activeTrackColor = Color(cg.primarySolid), inactiveTrackColor = Color(cg.border))
                    )
                }

                Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(cg.border)) {}

                // Haptic Feedback Mode
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Haptic Feedback", color = Color(cg.textPrimary), fontSize = 14.sp)

                    Surface(
                        shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                        color = Color(cg.secondary),
                        border = BorderStroke(1.dp, Color(cg.border))
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            listOf(HapticMode.OFF to "Off", HapticMode.SUBTLE to "Subtle", HapticMode.CRISP to "Crisp").forEach { (mode, label) ->
                                val isSelected = prefs.hapticMode == mode
                                Surface(
                                    onClick = { settingsRepo.updatePreferences { it.copy(hapticMode = mode) } },
                                    shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
                                    color = if (isSelected) Color(cg.primarySolid) else Color.Transparent,
                                    contentColor = if (isSelected) Color(cg.primaryText) else Color(cg.textSecondary)
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Theme Presets Group
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Theme & Palette Presets", color = Color(cg.textSecondary), fontSize = 12.sp, fontWeight = FontWeight.Medium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetButton("OLED", isSelected = prefs.themePreset == ThemePreset.OLED, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences {
                        it.copy(
                            themePreset = ThemePreset.OLED,
                            colorGraph = ColorGraph(bgSolid = 0xFF000000, surface = 0xFF0B0B0B, border = 0xFF1E1E1E, primarySolid = 0xFFFFFFFF, primaryText = 0xFF000000)
                        )
                    }
                }

                PresetButton("Midnight", isSelected = prefs.themePreset == ThemePreset.MIDNIGHT, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences {
                        it.copy(
                            themePreset = ThemePreset.MIDNIGHT,
                            colorGraph = ColorGraph(bgSolid = 0xFF070B14, surface = 0xFF0E1525, border = 0xFF1E293B, primarySolid = 0xFF38BDF8, primaryText = 0xFF04121E)
                        )
                    }
                }

                PresetButton("Ember", isSelected = prefs.themePreset == ThemePreset.EMBER, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences {
                        it.copy(
                            themePreset = ThemePreset.EMBER,
                            colorGraph = ColorGraph(bgSolid = 0xFF100A08, surface = 0xFF1A110E, border = 0xFF2E1F1A, primarySolid = 0xFFF97316, primaryText = 0xFF1A0A02)
                        )
                    }
                }

                PresetButton("Forest", isSelected = prefs.themePreset == ThemePreset.FOREST, cg, prefs, modifier = Modifier.weight(1f)) {
                    settingsRepo.updatePreferences {
                        it.copy(
                            themePreset = ThemePreset.FOREST,
                            colorGraph = ColorGraph(bgSolid = 0xFF060F0B, surface = 0xFF0C1A13, border = 0xFF173024, primarySolid = 0xFF34D399, primaryText = 0xFF041A10)
                        )
                    }
                }
            }
        }

        // Color Graph Tokens Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
            colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
            border = BorderStroke(0.5.dp, Color(cg.border))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("COLOR GRAPH TOKENS", color = Color(cg.textPrimary), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Individual color token values applying to app and sidebar UI", color = Color(cg.textSecondary), fontSize = 11.sp)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorTokenRow("Background", String.format("#%06X", (0xFFFFFF and cg.bgSolid.toInt())), cg.bgSolid, cg) { newColor ->
                        settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(bgSolid = newColor)) }
                    }
                    ColorTokenRow("Surface", String.format("#%06X", (0xFFFFFF and cg.surface.toInt())), cg.surface, cg) { newColor ->
                        settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(surface = newColor)) }
                    }
                    ColorTokenRow("Border", String.format("#%06X", (0xFFFFFF and cg.border.toInt())), cg.border, cg) { newColor ->
                        settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(border = newColor)) }
                    }
                    ColorTokenRow("Primary Accent", String.format("#%06X", (0xFFFFFF and cg.primarySolid.toInt())), cg.primarySolid, cg) { newColor ->
                        settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(primarySolid = newColor)) }
                    }
                    ColorTokenRow("Secondary", String.format("#%06X", (0xFFFFFF and cg.secondary.toInt())), cg.secondary, cg) { newColor ->
                        settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(secondary = newColor)) }
                    }
                    ColorTokenRow("Text Primary", String.format("#%06X", (0xFFFFFF and cg.textPrimary.toInt())), cg.textPrimary, cg) { newColor ->
                        settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(textPrimary = newColor)) }
                    }
                    ColorTokenRow("Text Secondary", String.format("#%06X", (0xFFFFFF and cg.textSecondary.toInt())), cg.textSecondary, cg) { newColor ->
                        settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(textSecondary = newColor)) }
                    }
                }

                Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(cg.border)) {}

                // Background Gradient
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Background Gradient", color = Color(cg.textPrimary), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("App canvas & sidebar backdrop", color = Color(cg.textSecondary), fontSize = 11.sp)
                    }

                    Switch(
                        checked = cg.bgGradEnabled,
                        onCheckedChange = { checked ->
                            settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(bgGradEnabled = checked)) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(cg.primaryText), checkedTrackColor = Color(cg.primarySolid), uncheckedThumbColor = Color(cg.textSecondary), uncheckedTrackColor = Color(cg.secondary))
                    )
                }

                if (cg.bgGradEnabled) {
                    MetricSliderRow(
                        label = "Gradient Angle",
                        valueText = "${cg.bgGradAngle.roundToInt()}°",
                        value = cg.bgGradAngle,
                        valueRange = 0f..360f,
                        cg = cg,
                        onValueChange = { newVal -> settingsRepo.updatePreferences { it.copy(colorGraph = cg.copy(bgGradAngle = newVal)) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorTokenRow(
    label: String,
    hexCode: String,
    colorValue: Long,
    cg: ColorGraph,
    onColorChanged: (Long) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(cg.secondary),
        border = BorderStroke(1.dp, Color(cg.border))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, color = Color(cg.textPrimary), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(hexCode, color = Color(cg.textSecondary), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(colorValue),
                border = BorderStroke(1.dp, Color(cg.border))
            ) {}
        }
    }
}

@Composable
private fun ShapeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    cg: ColorGraph,
    prefs: MiniDeskPreferences,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
        color = if (isSelected) Color(cg.surfaceRaised) else Color(cg.surface),
        border = BorderStroke(1.dp, if (isSelected) Color(cg.primarySolid) else Color(cg.border))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color(cg.textPrimary), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(cg.textSecondary), fontSize = 10.sp)
        }
    }
}

@Composable
private fun PresetButton(
    title: String,
    isSelected: Boolean,
    cg: ColorGraph,
    prefs: MiniDeskPreferences,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape((prefs.globalRadiusDp * 0.625f).dp),
        color = if (isSelected) Color(cg.surfaceRaised) else Color(cg.surface),
        border = BorderStroke(1.dp, if (isSelected) Color(cg.primarySolid) else Color(cg.border))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title, color = Color(cg.textPrimary), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MetricSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    cg: ColorGraph,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color(cg.textSecondary), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text(valueText, color = Color(cg.textPrimary), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(cg.primarySolid),
                activeTrackColor = Color(cg.primarySolid),
                inactiveTrackColor = Color(cg.border)
            )
        )
    }
}

@Composable
private fun SimDeckRow(label: String, valText: String, cg: ColorGraph) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(cg.secondary),
        border = BorderStroke(1.dp, Color(cg.border))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(cg.textSecondary), fontSize = 11.sp)
            Text(valText, color = Color(cg.textPrimary), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
