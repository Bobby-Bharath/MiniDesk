package com.example.minidesk.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.data.settings.MiniDeskPreferences

data class TaskbarNavItem(
    val id: Int,
    val title: String
)

val TASKBAR_NAV_ITEMS = listOf(
    TaskbarNavItem(0, "Looks & Feel"),
    TaskbarNavItem(1, "Deck Editor"),
    TaskbarNavItem(2, "Settings")
)

@Composable
fun FloatingTaskbar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    prefs: MiniDeskPreferences,
    modifier: Modifier = Modifier
) {
    val cg = prefs.colorGraph

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 20.dp),
            shape = CircleShape,
            color = Color(cg.surface).copy(alpha = 0.90f),
            border = BorderStroke(1.dp, Color(cg.border))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TASKBAR_NAV_ITEMS.forEach { item ->
                    val isSelected = item.id == selectedTab

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) Color(cg.primarySolid) else Color.Transparent,
                        label = "taskbarBg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color(cg.primaryText) else Color(cg.textSecondary),
                        label = "taskbarContent"
                    )

                    Surface(
                        onClick = { onTabSelected(item.id) },
                        shape = CircleShape,
                        color = containerColor,
                        contentColor = contentColor
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
