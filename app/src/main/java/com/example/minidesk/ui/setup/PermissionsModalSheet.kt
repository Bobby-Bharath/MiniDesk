package com.example.minidesk.ui.setup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
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
    val hasNotificationListener = PermissionHelper.hasNotificationListener(context)
    val hasBatteryExemption = PermissionHelper.hasBatteryExemption(context)
    val hasSecureSettings = PermissionHelper.hasSecureSettings(context)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF101010),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SYSTEM PERMISSIONS DIAGNOSTIC",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Grant hardware & overlay permissions for full system features",
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 1. Overlay Drawing
            SheetPermissionRow(
                title = "System Overlay Window",
                subtitle = "Draw floating notch and Bento Deck on screen",
                isGranted = hasOverlay,
                onGrant = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )

            // 2. Write System Settings
            SheetPermissionRow(
                title = "Modify System Settings",
                subtitle = "Adjust brightness slider & auto-rotate toggle",
                isGranted = hasWriteSettings,
                onGrant = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )

            // 3. Notification & Media Listener
            SheetPermissionRow(
                title = "Notification & Media Listener",
                subtitle = "Read active media player track & album artwork",
                isGranted = hasNotificationListener,
                onGrant = {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )

            // 4. Battery Optimization Whitelist
            SheetPermissionRow(
                title = "Battery Optimization Whitelist",
                subtitle = "Keep background overlay service running smoothly",
                isGranted = hasBatteryExemption,
                onGrant = {
                    val intent = Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )

            // 5. WRITE_SECURE_SETTINGS ADB & Shizuku
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                border = BorderStroke(0.5.dp, Color(0xFF262626))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WRITE_SECURE_SETTINGS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Advanced system toggles & hidden API access", color = Color.White.copy(alpha = 0.60f), fontSize = 10.sp)
                        }

                        SheetStatusBadge(isGranted = hasSecureSettings)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ADB Script", PermissionHelper.FULL_ADB_SCRIPT))
                            Toast.makeText(context, "ADB Setup Script copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1C1C1C),
                        border = BorderStroke(0.5.dp, Color(0xFF262626))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPY ADB SETUP COMMANDS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SheetPermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(0.5.dp, Color(0xFF262626))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.60f), fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                SheetStatusBadge(isGranted = true)
            } else {
                Surface(
                    onClick = onGrant,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    contentColor = Color.Black
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grant", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(11.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetStatusBadge(isGranted: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1C1C1C),
        border = BorderStroke(0.5.dp, if (isGranted) Color.White else Color(0xFF262626))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                contentDescription = null,
                tint = if (isGranted) Color.White else Color.White.copy(alpha = 0.60f),
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGranted) "ACTIVE" else "MISSING",
                color = if (isGranted) Color.White else Color.White.copy(alpha = 0.60f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
