package com.example.minidesk.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.settings.MiniDeskPreferences
import com.example.minidesk.data.settings.PermissionHelper

@Composable
fun HeroSection(
    prefs: MiniDeskPreferences,
    onToggleService: (Boolean) -> Unit,
    onOpenPermissionsSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val grantedCount = countPermissions(context)
    val cg = prefs.colorGraph

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(prefs.globalRadiusDp.dp),
        colors = CardDefaults.cardColors(containerColor = Color(cg.surface)),
        border = BorderStroke(0.5.dp, Color(cg.border))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Sidebar Master Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sidebar",
                        color = Color(cg.textPrimary),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Show control trigger over other apps",
                        color = Color(cg.textSecondary),
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = prefs.isServiceEnabled,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(cg.primaryText),
                        checkedTrackColor = Color(cg.primarySolid),
                        uncheckedThumbColor = Color(cg.textSecondary),
                        uncheckedTrackColor = Color(cg.secondary)
                    )
                )
            }

            // Divider
            Surface(
                modifier = Modifier.fillMaxWidth().height(0.5.dp),
                color = Color(cg.border)
            ) {}

            // Row 2: Permissions Gateway Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPermissionsSheet() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Permissions",
                        color = Color(cg.textPrimary),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Overlay, system settings, notifications, and battery",
                        color = Color(cg.textSecondary),
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$grantedCount/8",
                        color = Color(cg.textSecondary),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(cg.textSecondary),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun countPermissions(context: Context): Int {
    var count = 0
    if (PermissionHelper.hasOverlayPermission(context)) count++
    if (PermissionHelper.hasWriteSettings(context)) count++
    if (PermissionHelper.hasSecureSettings(context)) count++
    if (PermissionHelper.hasNotificationListener(context)) count++
    if (PermissionHelper.hasBatteryExemption(context)) count++
    return count
}
