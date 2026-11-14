package com.example.minidesk.data.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PermissionHelper {

    const val FULL_ADB_SCRIPT = """
adb shell appops set com.example.minidesk SYSTEM_ALERT_WINDOW allow
adb shell appops set com.example.minidesk WRITE_SETTINGS allow
adb shell pm grant com.example.minidesk android.permission.WRITE_SECURE_SETTINGS
adb shell pm grant com.example.minidesk android.permission.BLUETOOTH_CONNECT
adb shell dumpsys deviceidle whitelist +com.example.minidesk
adb shell settings put global hidden_api_policy 1
"""

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun hasSecureSettings(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBatteryExemption(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun hasNotificationListener(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(context.packageName)
    }
}
