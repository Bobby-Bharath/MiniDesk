package com.example.minidesk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.settings.MiniDeskPreferences

@Composable
fun MainTopHeader(
    prefs: MiniDeskPreferences,
    modifier: Modifier = Modifier
) {
    val cg = prefs.colorGraph

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(cg.bgSolid).copy(alpha = 0.88f),
        border = BorderStroke(0.5.dp, Color(cg.border))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Double-Bar Logo Icon
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(prefs.globalRadiusDp.coerceAtMost(10).dp),
                color = Color(cg.surfaceRaised),
                border = BorderStroke(1.dp, Color(cg.border))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(10.dp)
                            .background(Color(cg.textSecondary), RoundedCornerShape(1.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(8.dp)
                            .background(Color(cg.primarySolid), RoundedCornerShape(1.dp))
                    )
                }
            }

            // Center: Title
            Text(
                text = "MiniDesk",
                color = Color(cg.textPrimary),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp
            )

            // Right: Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (prefs.isServiceEnabled) Color(0xFF22C55E) else Color(0xFF525252),
                            CircleShape
                        )
                )
                Text(
                    text = if (prefs.isServiceEnabled) "Active" else "Off",
                    color = Color(cg.textSecondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
