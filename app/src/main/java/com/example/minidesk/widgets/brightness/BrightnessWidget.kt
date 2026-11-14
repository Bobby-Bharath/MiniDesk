package com.example.minidesk.widgets.brightness

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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minidesk.deck.DeckColors
import com.example.minidesk.widgets.common.DeckSlider
import com.example.minidesk.widgets.common.GrantRow
import com.example.minidesk.widgets.common.IconAction
import com.example.minidesk.widgets.common.SectionLabel
import com.example.minidesk.widgets.common.WidgetSize
import com.example.minidesk.widgets.common.calculateWidgetSize

@Composable
fun BrightnessWidget(
    controller: BrightnessController,
    modifier: Modifier = Modifier
) {
    DisposableEffect(controller) {
        val observer = controller.registerObserver {
            // Observer handles update
        }
        onDispose {
            controller.unregisterObserver(observer)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widgetSize = calculateWidgetSize(maxWidth, maxHeight)

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
                SectionLabel("BRIGHTNESS ${if (widgetSize == WidgetSize.Large) "CONTROL" else ""}")

                if (!controller.canWriteSettings()) {
                    GrantRow(
                        message = "Modify settings required",
                        intent = controller.getGrantIntent()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeckSlider(
                            value = controller.currentFraction,
                            onValueChange = { controller.setBrightnessFraction(it) },
                            icon = Icons.Default.WbSunny,
                            label = if (controller.isAutoBrightness) "Auto" else "Manual",
                            modifier = Modifier.weight(1f)
                        )

                        IconAction(
                            icon = Icons.Default.BrightnessAuto,
                            contentDescription = "Toggle Auto Brightness",
                            onClick = { controller.toggleAutoBrightness() },
                            tint = if (controller.isAutoBrightness) DeckColors.Paper else DeckColors.Mute
                        )
                    }
                }
            }
        }
    }
}
