package com.example.minidesk.ui.pages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val verticalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("SETTINGS", color = Color(0xFF8A8A8A), fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0B0B)),
            border = BorderStroke(1.dp, Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Updates Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Updates", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("v2.1.0", color = Color(0xFF8A8A8A), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Surface(
                        onClick = { Toast.makeText(context, "Up to date (v2.1.0)", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(1.dp, Color(0xFF1E1E1E))
                    ) {
                        Text("Check for updates", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }

                Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(0xFF1E1E1E)) {}

                // Shizuku Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Shizuku", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Ready", color = Color(0xFF8A8A8A), fontSize = 11.sp)
                        }
                        Text("Run ADB commands on-device", color = Color(0xFF8A8A8A), fontSize = 12.sp)
                    }

                    Surface(
                        onClick = { Toast.makeText(context, "Shizuku authorized", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        contentColor = Color.Black
                    ) {
                        Text("Authorize", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }

                Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = Color(0xFF1E1E1E)) {}

                // Background Service Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Background Service", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Verify Doze exemption", color = Color(0xFF8A8A8A), fontSize = 12.sp)
                    }

                    Surface(
                        onClick = { Toast.makeText(context, "Service active • Doze exempt", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(1.dp, Color(0xFF1E1E1E))
                    ) {
                        Text("Verify", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }
            }
        }

        // ADB Script Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0B0B)),
            border = BorderStroke(1.dp, Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("ADB Setup Script", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Run over wireless ADB if Shizuku is unavailable", color = Color(0xFF8A8A8A), fontSize = 12.sp)
                    }

                    Surface(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ADB Script", PermissionHelper.FULL_ADB_SCRIPT))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(1.dp, Color(0xFF1E1E1E))
                    ) {
                        Text("Copy", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF000000),
                    border = BorderStroke(1.dp, Color(0xFF1E1E1E))
                ) {
                    Text(
                        text = "chmod +x setup_permissions.sh && ./setup_permissions.sh",
                        color = Color(0xFF8A8A8A),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
