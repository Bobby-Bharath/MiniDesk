package com.example.minidesk.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class TriggerShape { EDGE_BAR, FLOATING_PILL, FLOATING_PUCK, SPLIT_BAR, ORGANIC_TEARDROP }
enum class InnerIndicator { NONE, LINE, DOT }
enum class ThemePreset { OLED, MIDNIGHT, EMBER, FOREST, PAPER }
enum class ToolShape { CIRCLE, SQUIRCLE, GLOBAL }
enum class HapticMode { OFF, SUBTLE, CRISP }

data class ColorGraph(
    val bgSolid: Long = 0xFF000000,
    val surface: Long = 0xFF0B0B0B,
    val surfaceRaised: Long = 0xFF141414,
    val border: Long = 0xFF1E1E1E,
    val primarySolid: Long = 0xFFFFFFFF,
    val primaryText: Long = 0xFF000000,
    val secondary: Long = 0xFF1A1A1A,
    val textPrimary: Long = 0xFFF5F5F5,
    val textSecondary: Long = 0xFF8A8A8A,
    val bgGradEnabled: Boolean = false,
    val bgGradStart: Long = 0xFF000000,
    val bgGradEnd: Long = 0xFF121212,
    val bgGradAngle: Float = 180f,
    val pGradEnabled: Boolean = false,
    val pGradStart: Long = 0xFFFFFFFF,
    val pGradEnd: Long = 0xFFA3A3A3,
    val pGradAngle: Float = 135f
) {
    fun backgroundBrush(): Brush = if (bgGradEnabled) {
        angledLinearGradient(Color(bgGradStart), Color(bgGradEnd), bgGradAngle)
    } else {
        SolidColor(Color(bgSolid))
    }

    fun surfaceBrush(): Brush = if (bgGradEnabled) {
        angledLinearGradient(Color(bgGradStart), Color(bgGradEnd), bgGradAngle)
    } else {
        SolidColor(Color(surface))
    }

    fun primaryBrush(): Brush = if (pGradEnabled) {
        angledLinearGradient(Color(pGradStart), Color(pGradEnd), pGradAngle)
    } else {
        SolidColor(Color(primarySolid))
    }
}

private fun angledLinearGradient(start: Color, end: Color, angleDeg: Float): Brush {
    val rad = angleDeg * (PI / 180.0)
    val dx = cos(rad).toFloat()
    val dy = sin(rad).toFloat()
    return Brush.linearGradient(
        colors = listOf(start, end),
        start = Offset.Zero,
        end = Offset(dx * 600f, dy * 600f)
    )
}

data class MiniDeskPreferences(
    val isFirstRunComplete: Boolean = true,
    val isServiceEnabled: Boolean = true,
    val triggerShape: TriggerShape = TriggerShape.FLOATING_PILL,
    val themePreset: ThemePreset = ThemePreset.OLED,
    val colorGraph: ColorGraph = ColorGraph(),
    val globalRadiusDp: Int = 16,
    val restingOpacity: Float = 0.90f,
    val hapticMode: HapticMode = HapticMode.SUBTLE,
    val isRightEdge: Boolean = true,
    val yPositionPercent: Float = 0.50f,
    val edgeGapDp: Int = 10,
    val handleWidthDp: Int = 12,
    val handleHeightDp: Int = 76,
    val innerIndicator: InnerIndicator = InnerIndicator.LINE,
    val puckDiameterDp: Int = 36,
    val puckSnapToEdge: Boolean = true,
    val puckSingleTap: String = "Quick Strip",
    val puckDoubleTap: String = "Command Deck",
    val splitStyleFloating: Boolean = true,
    val splitGapDp: Int = 6,
    val splitTopAction: String = "Quick Strip",
    val splitBottomAction: String = "Command Deck",
    val openGesture: String = "Inward Swipe",
    val toolSlotsLimit: Int = 4,
    val toolShape: ToolShape = ToolShape.CIRCLE,
    val showRadialRings: Boolean = true,
    val activeMiniToolIds: List<String> = listOf("volume", "brightness", "media", "deck_btn"),
    val miniToolSettings: Map<String, Map<String, String>> = defaultMiniToolSettings(),
    val commandDeckModuleStates: Map<String, Boolean> = mapOf(
        "audio" to true,
        "connectivity" to true,
        "scratchpad" to true,
        "clipboard" to true,
        "eyedropper_mod" to true,
        "search" to true
    ),
    val useShizuku: Boolean = false,
    val enableFreeformLauncher: Boolean = false,
    val blacklistedPackages: Set<String> = emptySet()
) {
    val notchEdgeRight: Boolean get() = isRightEdge
    val notchYPercentage: Float get() = yPositionPercent
    val notchWidthDp: Int get() = handleWidthDp
    val notchHeightDp: Int get() = handleHeightDp
    val notchStyle: String get() = triggerShape.name
    val handleOpacity: Float get() = restingOpacity
    val xOffsetDp: Int get() = edgeGapDp

    fun resolveToolShape(): Shape = when (toolShape) {
        ToolShape.CIRCLE -> CircleShape
        ToolShape.SQUIRCLE -> RoundedCornerShape(14.dp)
        ToolShape.GLOBAL -> RoundedCornerShape((globalRadiusDp * 0.625f).coerceIn(4f, 23f).dp)
    }

    fun boxCornerShape(): RoundedCornerShape = RoundedCornerShape(globalRadiusDp.dp)
    fun buttonCornerShape(): RoundedCornerShape = RoundedCornerShape((globalRadiusDp * 0.625f).coerceAtLeast(4f).dp)
}

fun defaultMiniToolSettings(): Map<String, Map<String, String>> = mapOf(
    "volume" to mapOf("Stream" to "Media", "Step Size" to "5%", "Tap to Mute" to "false"),
    "brightness" to mapOf("Mode" to "Manual", "Step Size" to "10%", "Double Tap Auto" to "true"),
    "media" to mapOf("Swipe to Skip" to "true", "Target App" to "Active Session"),
    "deck_btn" to mapOf("Default Tab" to "Bento Grid", "Remember Last Tab" to "true"),
    "torch" to mapOf("Auto-Off" to "Never", "Double Tap SOS" to "false"),
    "timer" to mapOf("Default Preset" to "5 min", "Exact Alarm" to "true"),
    "eyedropper" to mapOf("Format" to "HEX", "Auto-Copy" to "true"),
    "rotate" to mapOf("Toast on Toggle" to "true")
)

class MiniDeskSettingsRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("minidesk_settings", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<MiniDeskPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): MiniDeskPreferences {
        val firstRun = prefs.getBoolean("is_first_run_complete", true)
        val serviceEnabled = prefs.getBoolean("is_service_enabled", true)
        val triggerShapeStr = prefs.getString("trigger_shape", TriggerShape.FLOATING_PILL.name) ?: TriggerShape.FLOATING_PILL.name
        val presetStr = prefs.getString("theme_preset", ThemePreset.OLED.name) ?: ThemePreset.OLED.name
        val globalRadius = prefs.getInt("global_radius_dp", 16)
        val opacity = prefs.getFloat("resting_opacity", 0.90f)
        val hapticStr = prefs.getString("haptic_mode", HapticMode.SUBTLE.name) ?: HapticMode.SUBTLE.name
        val isRight = prefs.getBoolean("notch_edge_right", true)
        val yPct = prefs.getFloat("notch_y_pct", 0.50f)
        val edgeGap = prefs.getInt("edge_gap_dp", 10)
        val width = prefs.getInt("notch_width_dp", 12)
        val height = prefs.getInt("notch_height_dp", 76)
        val indicatorStr = prefs.getString("inner_indicator", InnerIndicator.LINE.name) ?: InnerIndicator.LINE.name
        val puckDia = prefs.getInt("puck_diameter_dp", 36)
        val puckSnap = prefs.getBoolean("puck_snap_to_edge", true)
        val puckSingle = prefs.getString("puck_single_tap", "Quick Strip") ?: "Quick Strip"
        val puckDouble = prefs.getString("puck_double_tap", "Command Deck") ?: "Command Deck"
        val splitFloat = prefs.getBoolean("split_style_floating", true)
        val splitGap = prefs.getInt("split_gap_dp", 6)
        val splitTop = prefs.getString("split_top_action", "Quick Strip") ?: "Quick Strip"
        val splitBot = prefs.getString("split_bottom_action", "Command Deck") ?: "Command Deck"
        val openGestureStr = prefs.getString("open_gesture", "Inward Swipe") ?: "Inward Swipe"
        val slotsLimit = prefs.getInt("tool_slots_limit", 4)
        val toolShapeStr = prefs.getString("tool_shape", ToolShape.CIRCLE.name) ?: ToolShape.CIRCLE.name
        val radialRings = prefs.getBoolean("show_radial_rings", true)
        val shizuku = prefs.getBoolean("use_shizuku", false)
        val freeform = prefs.getBoolean("enable_freeform_launcher", false)

        val bgSolid = prefs.getLong("cg_bg_solid", 0xFF000000)
        val surface = prefs.getLong("cg_surface", 0xFF0B0B0B)
        val surfaceRaised = prefs.getLong("cg_surface_raised", 0xFF141414)
        val border = prefs.getLong("cg_border", 0xFF1E1E1E)
        val primarySolid = prefs.getLong("cg_primary_solid", 0xFFFFFFFF)
        val primaryText = prefs.getLong("cg_primary_text", 0xFF000000)
        val secondary = prefs.getLong("cg_secondary", 0xFF1A1A1A)
        val textPrimary = prefs.getLong("cg_text_primary", 0xFFF5F5F5)
        val textSecondary = prefs.getLong("cg_text_secondary", 0xFF8A8A8A)
        val bgGrad = prefs.getBoolean("cg_bg_grad", false)
        val bgStart = prefs.getLong("cg_bg_start", 0xFF000000)
        val bgEnd = prefs.getLong("cg_bg_end", 0xFF121212)
        val bgAngle = prefs.getFloat("cg_bg_angle", 180f)
        val pGrad = prefs.getBoolean("cg_p_grad", false)
        val pStart = prefs.getLong("cg_p_start", 0xFFFFFFFF)
        val pEnd = prefs.getLong("cg_p_end", 0xFFA3A3A3)
        val pAngle = prefs.getFloat("cg_p_angle", 135f)

        val cg = ColorGraph(
            bgSolid = bgSolid, surface = surface, surfaceRaised = surfaceRaised, border = border,
            primarySolid = primarySolid, primaryText = primaryText, secondary = secondary,
            textPrimary = textPrimary, textSecondary = textSecondary, bgGradEnabled = bgGrad,
            bgGradStart = bgStart, bgGradEnd = bgEnd, bgGradAngle = bgAngle,
            pGradEnabled = pGrad, pGradStart = pStart, pGradEnd = pEnd, pGradAngle = pAngle
        )

        val blacklistJson = prefs.getString("blacklisted_packages", "[]") ?: "[]"
        val blacklistSet = mutableSetOf<String>()
        try {
            val arr = JSONArray(blacklistJson)
            for (i in 0 until arr.length()) blacklistSet.add(arr.getString(i))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val activeToolsJson = prefs.getString("active_mini_tools", "[\"volume\",\"brightness\",\"media\",\"deck_btn\"]") ?: "[]"
        val activeToolsList = mutableListOf<String>()
        try {
            val arr = JSONArray(activeToolsJson)
            for (i in 0 until arr.length()) activeToolsList.add(arr.getString(i))
        } catch (e: Exception) {
            activeToolsList.addAll(listOf("volume", "brightness", "media", "deck_btn"))
        }

        val toolSettingsMap = defaultMiniToolSettings().mapValues { it.value.toMutableMap() }.toMutableMap()
        val toolSettingsJson = prefs.getString("mini_tool_settings", "{}") ?: "{}"
        try {
            val rootObj = JSONObject(toolSettingsJson)
            val toolKeys = rootObj.keys()
            while (toolKeys.hasNext()) {
                val tId = toolKeys.next()
                val subObj = rootObj.getJSONObject(tId)
                val subMap = toolSettingsMap.getOrPut(tId) { mutableMapOf() }
                val settingKeys = subObj.keys()
                while (settingKeys.hasNext()) {
                    val sKey = settingKeys.next()
                    subMap[sKey] = subObj.getString(sKey)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cmdDeckModulesJson = prefs.getString("cmd_deck_modules", "{}") ?: "{}"
        val cmdDeckMap = mutableMapOf(
            "audio" to true, "connectivity" to true, "scratchpad" to true,
            "clipboard" to true, "eyedropper_mod" to true, "search" to true
        )
        try {
            val obj = JSONObject(cmdDeckModulesJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                cmdDeckMap[k] = obj.getBoolean(k)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val triggerShape = try { TriggerShape.valueOf(triggerShapeStr) } catch (e: Exception) { TriggerShape.FLOATING_PILL }
        val themePreset = try { ThemePreset.valueOf(presetStr) } catch (e: Exception) { ThemePreset.OLED }
        val indicator = try { InnerIndicator.valueOf(indicatorStr) } catch (e: Exception) { InnerIndicator.LINE }
        val toolShape = try { ToolShape.valueOf(toolShapeStr) } catch (e: Exception) { ToolShape.CIRCLE }
        val haptic = try { HapticMode.valueOf(hapticStr) } catch (e: Exception) { HapticMode.SUBTLE }

        return MiniDeskPreferences(
            isFirstRunComplete = firstRun,
            isServiceEnabled = serviceEnabled,
            triggerShape = triggerShape,
            themePreset = themePreset,
            colorGraph = cg,
            globalRadiusDp = globalRadius,
            restingOpacity = opacity,
            hapticMode = haptic,
            isRightEdge = isRight,
            yPositionPercent = yPct,
            edgeGapDp = edgeGap,
            handleWidthDp = width,
            handleHeightDp = height,
            innerIndicator = indicator,
            puckDiameterDp = puckDia,
            puckSnapToEdge = puckSnap,
            puckSingleTap = puckSingle,
            puckDoubleTap = puckDouble,
            splitStyleFloating = splitFloat,
            splitGapDp = splitGap,
            splitTopAction = splitTop,
            splitBottomAction = splitBot,
            openGesture = openGestureStr,
            toolSlotsLimit = slotsLimit,
            toolShape = toolShape,
            showRadialRings = radialRings,
            activeMiniToolIds = activeToolsList,
            miniToolSettings = toolSettingsMap,
            commandDeckModuleStates = cmdDeckMap,
            useShizuku = shizuku,
            enableFreeformLauncher = freeform,
            blacklistedPackages = blacklistSet
        )
    }

    @Synchronized
    fun updatePreferences(update: (MiniDeskPreferences) -> MiniDeskPreferences) {
        val newPrefs = update(_preferences.value)
        _preferences.value = newPrefs

        val arrBlacklist = JSONArray()
        newPrefs.blacklistedPackages.forEach { arrBlacklist.put(it) }

        val arrTools = JSONArray()
        newPrefs.activeMiniToolIds.forEach { arrTools.put(it) }

        val objToolSettings = JSONObject()
        newPrefs.miniToolSettings.forEach { (toolId, map) ->
            val sub = JSONObject()
            map.forEach { (k, v) -> sub.put(k, v) }
            objToolSettings.put(toolId, sub)
        }

        val objCmdDeck = JSONObject()
        newPrefs.commandDeckModuleStates.forEach { (k, v) -> objCmdDeck.put(k, v) }

        val cg = newPrefs.colorGraph

        prefs.edit()
            .putBoolean("is_first_run_complete", newPrefs.isFirstRunComplete)
            .putBoolean("is_service_enabled", newPrefs.isServiceEnabled)
            .putString("trigger_shape", newPrefs.triggerShape.name)
            .putString("theme_preset", newPrefs.themePreset.name)
            .putInt("global_radius_dp", newPrefs.globalRadiusDp)
            .putFloat("resting_opacity", newPrefs.restingOpacity)
            .putString("haptic_mode", newPrefs.hapticMode.name)
            .putBoolean("notch_edge_right", newPrefs.isRightEdge)
            .putFloat("notch_y_pct", newPrefs.yPositionPercent)
            .putInt("edge_gap_dp", newPrefs.edgeGapDp)
            .putInt("notch_width_dp", newPrefs.handleWidthDp)
            .putInt("notch_height_dp", newPrefs.handleHeightDp)
            .putString("inner_indicator", newPrefs.innerIndicator.name)
            .putInt("puck_diameter_dp", newPrefs.puckDiameterDp)
            .putBoolean("puck_snap_to_edge", newPrefs.puckSnapToEdge)
            .putString("puck_single_tap", newPrefs.puckSingleTap)
            .putString("puck_double_tap", newPrefs.puckDoubleTap)
            .putBoolean("split_style_floating", newPrefs.splitStyleFloating)
            .putInt("split_gap_dp", newPrefs.splitGapDp)
            .putString("split_top_action", newPrefs.splitTopAction)
            .putString("split_bottom_action", newPrefs.splitBottomAction)
            .putString("open_gesture", newPrefs.openGesture)
            .putInt("tool_slots_limit", newPrefs.toolSlotsLimit)
            .putString("tool_shape", newPrefs.toolShape.name)
            .putBoolean("show_radial_rings", newPrefs.showRadialRings)
            .putString("active_mini_tools", arrTools.toString())
            .putString("mini_tool_settings", objToolSettings.toString())
            .putString("cmd_deck_modules", objCmdDeck.toString())
            .putBoolean("use_shizuku", newPrefs.useShizuku)
            .putBoolean("enable_freeform_launcher", newPrefs.enableFreeformLauncher)
            .putString("blacklisted_packages", arrBlacklist.toString())
            .putLong("cg_bg_solid", cg.bgSolid)
            .putLong("cg_surface", cg.surface)
            .putLong("cg_surface_raised", cg.surfaceRaised)
            .putLong("cg_border", cg.border)
            .putLong("cg_primary_solid", cg.primarySolid)
            .putLong("cg_primary_text", cg.primaryText)
            .putLong("cg_secondary", cg.secondary)
            .putLong("cg_text_primary", cg.textPrimary)
            .putLong("cg_text_secondary", cg.textSecondary)
            .putBoolean("cg_bg_grad", cg.bgGradEnabled)
            .putLong("cg_bg_start", cg.bgGradStart)
            .putLong("cg_bg_end", cg.bgGradEnd)
            .putFloat("cg_bg_angle", cg.bgGradAngle)
            .putBoolean("cg_p_grad", cg.pGradEnabled)
            .putLong("cg_p_start", cg.pGradStart)
            .putLong("cg_p_end", cg.pGradEnd)
            .putFloat("cg_p_angle", cg.pGradAngle)
            .apply()
    }

    companion object {
        @Volatile
        private var instance: MiniDeskSettingsRepository? = null

        fun get(context: Context): MiniDeskSettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: MiniDeskSettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
