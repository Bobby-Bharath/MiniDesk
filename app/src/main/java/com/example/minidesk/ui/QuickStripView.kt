package com.example.minidesk.ui

import androidx.compose.runtime.Composable
import com.example.minidesk.BrightnessController
import com.example.minidesk.FlashlightController
import com.example.minidesk.MediaController
import com.example.minidesk.OverlayState
import com.example.minidesk.VolumeController
import com.example.minidesk.ui.capsule.CapsuleSpineView

@Composable
fun QuickStripView(
    onStateChange: (OverlayState) -> Unit,
    onRequestDismiss: () -> Unit,
    volumeController: VolumeController,
    brightnessController: BrightnessController,
    flashlightController: FlashlightController,
    mediaController: MediaController
) {
    CapsuleSpineView(
        onExpandBentoDeck = { onStateChange(OverlayState.COMMAND_DECK) },
        onDockToNotch = onRequestDismiss,
        volumeController = volumeController,
        brightnessController = brightnessController,
        mediaController = mediaController,
        flashlightController = flashlightController
    )
}
