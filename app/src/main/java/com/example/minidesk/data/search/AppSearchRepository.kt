package com.example.minidesk.data.search

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// In OriginOS 5 & 6 (Android 15/16), WINDOWING_MODE_VIVO_FREEFORM is 106 (prior versions used 100 or 5)
private const val WINDOWING_MODE_VIVO_FREEFORM = 106
private const val WINDOWING_MODE_VIVO_LEGACY = 100
private const val WINDOWING_MODE_AOSP_FREEFORM = 5

sealed class SearchResultItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String

    data class AppItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val packageName: String,
        val icon: Drawable?
    ) : SearchResultItem()

    data class SettingsItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val intentAction: String
    ) : SearchResultItem()

    data class WebQueryItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val query: String
    ) : SearchResultItem()
}

object AppSearchRepository {
    private var cachedApps: List<SearchResultItem.AppItem> = emptyList()
    private var isIndexed = false

    private val commonSettingsShortcuts = listOf(
        SearchResultItem.SettingsItem("set_dev", "Developer Options", "System > Developer", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
        SearchResultItem.SettingsItem("set_battery", "Battery Settings", "Device Power & Battery", Intent.ACTION_POWER_USAGE_SUMMARY),
        SearchResultItem.SettingsItem("set_display", "Display Settings", "Screen & Refresh Rate", Settings.ACTION_DISPLAY_SETTINGS),
        SearchResultItem.SettingsItem("set_apps", "Installed Applications", "Manage Apps & Storage", Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
        SearchResultItem.SettingsItem("set_sound", "Sound & Vibration", "Audio Settings", Settings.ACTION_SOUND_SETTINGS),
        SearchResultItem.SettingsItem("set_bt", "Bluetooth", "Connected Devices", Settings.ACTION_BLUETOOTH_SETTINGS)
    )

    suspend fun indexInstalledApps(context: Context) = withContext(Dispatchers.IO) {
        if (isIndexed) return@withContext
        try {
            val pm = context.packageManager
            val launchIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(launchIntent, 0)
            val apps = resolveInfos.mapNotNull { resolve ->
                try {
                    val label = resolve.loadLabel(pm).toString()
                    val pkg = resolve.activityInfo.packageName
                    val icon = resolve.loadIcon(pm)
                    SearchResultItem.AppItem(
                        id = pkg,
                        title = label,
                        subtitle = pkg,
                        packageName = pkg,
                        icon = icon
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.title.lowercase() }

            cachedApps = apps
            isIndexed = true
            Log.d("MiniDeskSearch", "Indexed ${apps.size} installed apps")
        } catch (e: Exception) {
            Log.e("MiniDeskSearch", "Failed to index apps", e)
        }
    }

    fun query(queryString: String): List<SearchResultItem> {
        val q = queryString.trim().lowercase()
        if (q.isBlank()) return emptyList()

        val matchedApps = cachedApps.filter {
            it.title.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
        val matchedSettings = commonSettingsShortcuts.filter {
            it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q)
        }

        val results = mutableListOf<SearchResultItem>()
        results.addAll(matchedApps)
        results.addAll(matchedSettings)

        // Web fallback item
        results.add(
            SearchResultItem.WebQueryItem(
                id = "web_google_$q",
                title = "Search Google for \"$q\"",
                subtitle = "Open default browser",
                query = q
            )
        )
        return results
    }

    fun launchApp(context: Context, packageName: String, inFreeform: Boolean) {
        try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } ?: return

            if (!inFreeform) {
                context.startActivity(launchIntent)
                return
            }

            // =========================================================================
            // NATURAL METHOD: Vivo OriginOS Native Small Window (No ADB / Shizuku)
            // =========================================================================
            launchIntent.apply {
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                // Vivo OriginOS Multi-Window Intent Extras
                putExtra("vivo.intent.extra.MULTIWINDOW_MODE", WINDOWING_MODE_VIVO_FREEFORM)
                putExtra("vivo.intent.extra.START_FROM_SMART_MULTI_WINDOW", true)
                putExtra("com.vivo.smartmultiwindow.extra.FREEFORM", true)
                putExtra("start_from_mini_launcher", true)
            }

            val options = ActivityOptions.makeBasic()

            // 1. Calculate Vivo Small Window Bounds (Natural 9:16 Aspect Ratio)
            val dm = context.resources.displayMetrics
            val screenW = dm.widthPixels
            val screenH = dm.heightPixels

            // Vivo default small window: width ~ 65% of screen, height ~ 55% of screen, positioned near center-right
            val windowW = (screenW * 0.65f).toInt()
            val windowH = (screenH * 0.55f).toInt()
            val left = (screenW - windowW - 40).coerceAtLeast(20)
            val top = (screenH - windowH) / 3
            val right = left + windowW
            val bottom = top + windowH
            val bounds = Rect(left, top, right, bottom)
            options.setLaunchBounds(bounds)

            // 2. Set OriginOS Windowing Mode (106) via reflection on ActivityOptions
            var modeSetSuccess = false
            try {
                val setLaunchWindowingModeMethod = ActivityOptions::class.java.getMethod(
                    "setLaunchWindowingMode",
                    Int::class.javaPrimitiveType
                )
                // Attempt OriginOS 6 mode 106 first
                setLaunchWindowingModeMethod.invoke(options, WINDOWING_MODE_VIVO_FREEFORM)
                modeSetSuccess = true
                Log.d("MiniDeskSearch", "Applied Vivo Native Small Window Mode (106)")
            } catch (e: Exception) {
                Log.w("MiniDeskSearch", "Failed to set mode 106, trying legacy 100 / 5", e)
                try {
                    val setLaunchWindowingModeMethod = ActivityOptions::class.java.getMethod(
                        "setLaunchWindowingMode",
                        Int::class.javaPrimitiveType
                    )
                    setLaunchWindowingModeMethod.invoke(options, WINDOWING_MODE_VIVO_LEGACY)
                    modeSetSuccess = true
                } catch (e2: Exception) {
                    // Fallback to mode 5
                    try {
                        val m = ActivityOptions::class.java.getMethod("setLaunchWindowingMode", Int::class.javaPrimitiveType)
                        m.invoke(options, WINDOWING_MODE_AOSP_FREEFORM)
                    } catch (_: Exception) {}
                }
            }

            // 3. Attempt natural launch through Android context
            try {
                context.startActivity(launchIntent, options.toBundle())
                Log.d("MiniDeskSearch", "Natural launch executed for $packageName")
            } catch (e: Exception) {
                Log.w("MiniDeskSearch", "Natural startActivity failed, attempting fallback...", e)
            }

            // =========================================================================
            // FALLBACK: If natural launch is dropped, execute with windowingMode 106 via shell
            // =========================================================================
            val component = launchIntent.component?.flattenToShortString()
            if (component != null) {
                try {
                    // Notice: We pass 106 to am start so even shell launch uses Vivo's native decoration!
                    val cmd = "am start -n $component --windowingMode $WINDOWING_MODE_VIVO_FREEFORM"
                    Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                } catch (_: Exception) {}
            }

        } catch (e: Exception) {
            Log.e("MiniDeskSearch", "Complete launch failure for $packageName", e)
        }
    }

    fun launchSettings(context: Context, action: String) {
        try {
            val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MiniDeskSearch", "Failed to launch settings action: $action", e)
        }
    }

    fun launchWeb(context: Context, query: String) {
        try {
            val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
            val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MiniDeskSearch", "Failed to launch web query", e)
        }
    }
}
