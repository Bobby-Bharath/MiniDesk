package com.example.minidesk.widgets.toggles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minidesk.deck.DeckColors
import com.example.minidesk.widgets.common.GrantRow
import com.example.minidesk.widgets.common.SectionLabel
import com.example.minidesk.widgets.common.ToggleTile
import com.example.minidesk.widgets.common.WidgetSize
import com.example.minidesk.widgets.common.calculateWidgetSize

@Composable
fun QuickTogglesWidget(
    controller: QuickTogglesController,
    onRequestDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DisposableEffect(controller) {
        val (torch, observer, receiver) = controller.registerListeners {
            // Listeners update state
        }
        onDispose {
            controller.unregisterListeners(torch, observer, receiver)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val size = calculateWidgetSize(maxWidth, maxHeight)

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
                SectionLabel("QUICK TOGGLES")

                if (!controller.canWriteSettings()) {
                    GrantRow(
                        message = "Modify settings required",
                        intent = controller.getWriteSettingsIntent()
                    )
                } else if (size == WidgetSize.Large && !controller.isDndPolicyGranted()) {
                    GrantRow(
                        message = "DND policy required",
                        intent = controller.getDndPolicyIntent()
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Row 1: Torch & Rotate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ToggleTile(
                                label = "Torch",
                                stateText = if (controller.isTorchOn) "ON" else "OFF",
                                icon = Icons.Default.FlashOn,
                                isOn = controller.isTorchOn,
                                onClick = { controller.toggleTorch() },
                                modifier = Modifier.weight(1f)
                            )

                            ToggleTile(
                                label = "Rotate",
                                stateText = if (controller.isAutoRotateOn) "AUTO" else "LOCKED",
                                icon = Icons.Default.ScreenRotation,
                                isOn = controller.isAutoRotateOn,
                                onClick = { controller.toggleAutoRotate() },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Wi-Fi & BT
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ToggleTile(
                                label = "Wi-Fi",
                                stateText = if (controller.isWifiOn) "ON (OPEN)" else "OFF (OPEN)",
                                icon = Icons.Default.Wifi,
                                isOn = controller.isWifiOn,
                                onClick = { controller.openWifiPanel(onRequestDismiss) },
                                modifier = Modifier.weight(1f)
                            )

                            ToggleTile(
                                label = "BT",
                                stateText = if (controller.isBtOn) "ON (OPEN)" else "OFF (OPEN)",
                                icon = Icons.Default.Bluetooth,
                                isOn = controller.isBtOn,
                                onClick = { controller.openBluetoothSettings(onRequestDismiss) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3 (Large size): Location & DND
                        if (size == WidgetSize.Large) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ToggleTile(
                                    label = "Location",
                                    stateText = if (controller.isLocationOn) "ON (OPEN)" else "OFF (OPEN)",
                                    icon = Icons.Default.LocationOn,
                                    isOn = controller.isLocationOn,
                                    onClick = { controller.openLocationSettings(onRequestDismiss) },
                                    modifier = Modifier.weight(1f)
                                )

                                ToggleTile(
                                    label = "DND",
                                    stateText = if (controller.isDndOn) "ON" else "OFF",
                                    icon = Icons.Default.DoNotDisturb,
                                    isOn = controller.isDndOn,
                                    onClick = { controller.toggleDnd() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
