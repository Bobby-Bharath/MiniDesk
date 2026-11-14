package com.example.minidesk.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val label: String,
    val packageName: String,
    val className: String,
    val icon: Drawable?
)

object AppRepository {
    private var cachedApps: List<AppInfo>? = null

    suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        cachedApps?.let { return@withContext it }

        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveList = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val apps = resolveList.mapNotNull { info ->
            try {
                AppInfo(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    className = info.activityInfo.name,
                    icon = info.loadIcon(pm)
                )
            } catch (e: Exception) {
                null
            }
        }.distinctBy { "${it.packageName}_${it.className}" }
            .sortedBy { it.label.lowercase() }

        cachedApps = apps
        apps
    }

    fun invalidateCache() {
        cachedApps = null
    }
}
