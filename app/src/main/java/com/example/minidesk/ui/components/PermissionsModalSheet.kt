package com.example.minidesk.ui.components

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.settings.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsModalSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val hasOverlay = PermissionHelper.hasOverlayPermission(context)
    val hasWriteSettings = PermissionHelper.hasWriteSettings(context)
    val hasSecureSettings = PermissionHelper.hasSecureSettings(context)
    val hasNotificationListener = PermissionHelper.hasNotificationListener(context)
    val hasBatteryExemption = PermissionHelper.hasBatteryExemption(context)

    val hasAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        am?.canScheduleExactAlarms() ?: true
    } else true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B0B0B),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Permissions",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Enable or disable system capabilities",
                        color = Color(0xFF8A8A8A),
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "✕",
                    color = Color(0xFF8A8A8A),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onDismiss() }.padding(4.dp)
                )
            }

            Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(0xFF1E1E1E)) {}

            // Permission Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                PermToggleRow(
                    title = "Display over other apps",
                    subtitle = "Required for edge bar and deck overlay",
                    isChecked = hasOverlay,
                    onToggle = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )

                PermToggleRow(
                    title = "Modify system settings",
                    subtitle = "Adjust brightness and auto-rotate",
                    isChecked = hasWriteSettings,
                    onToggle = {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )

                PermToggleRow(
                    title = "Write secure settings",
                    subtitle = "Location and protected toggles",
                    isChecked = hasSecureSettings,
                    onToggle = {}
                )

                PermToggleRow(
                    title = "Notification listener",
                    subtitle = "Read active media session",
                    isChecked = hasNotificationListener,
                    onToggle = {
                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )

                PermToggleRow(
                    title = "Unrestricted battery",
                    subtitle = "Prevent background termination",
                    isChecked = hasBatteryExemption,
                    onToggle = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )

                PermToggleRow(
                    title = "Exact alarms",
                    subtitle = "Fire timers while asleep",
                    isChecked = hasAlarmPermission,
                    onToggle = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }
                )

                PermToggleRow(
                    title = "Restricted settings access",
                    subtitle = "Allow sideloaded grants on Android 13+",
                    isChecked = true,
                    onToggle = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )

                PermToggleRow(
                    title = "Camera (Torch)",
                    subtitle = "Control LED flashlight directly",
                    isChecked = true,
                    onToggle = {}
                )
            }

            Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(0xFF1E1E1E)) {}

            // Done Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    contentColor = Color.Black
                ) {
                    Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun PermToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp)
                Text(subtitle, color = Color(0xFF8A8A8A), fontSize = 12.sp)
            }

            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Color.White, checkmarkColor = Color.Black, uncheckedColor = Color(0xFF3F3F46))
            )
        }

        Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(0xFF1E1E1E)) {}
    }
}
