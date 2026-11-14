package com.example.minidesk.widgets.audioroute

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minidesk.deck.DeckColors
import com.example.minidesk.widgets.common.SectionLabel
import com.example.minidesk.widgets.common.WidgetSize
import com.example.minidesk.widgets.common.calculateWidgetSize

@Composable
fun AudioRouteWidget(
    controller: AudioRouteController,
    onRequestDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DisposableEffect(controller) {
        val callback = controller.registerDeviceCallback {
            // Callback updates state
        }
        onDispose {
            controller.unregisterDeviceCallback(callback)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widgetSize = calculateWidgetSize(maxWidth, maxHeight)

        val routeIcon = when (controller.currentRouteType) {
            "Bluetooth" -> Icons.Default.Bluetooth
            "USB" -> Icons.Default.Usb
            "Wired" -> Icons.Default.Headphones
            else -> Icons.Default.Speaker
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DeckColors.Panel,
            border = BorderStroke(0.5.dp, DeckColors.Hairline),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                SectionLabel("CURRENT OUTPUT")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = routeIcon,
                        contentDescription = controller.currentRouteType,
                        tint = DeckColors.Paper,
                        modifier = Modifier.size(20.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = controller.currentRouteName,
                            color = DeckColors.Paper,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = controller.currentRouteType,
                            color = DeckColors.Mute,
                            fontSize = 10.sp
                        )
                    }
                }

                if (widgetSize != WidgetSize.Compact) {
                    Surface(
                        onClick = {
                            controller.openVolumePanel(onRequestDismiss)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = DeckColors.Panel,
                        border = BorderStroke(0.5.dp, DeckColors.Paper)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CHANGE OUTPUT",
                                color = DeckColors.Paper,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
