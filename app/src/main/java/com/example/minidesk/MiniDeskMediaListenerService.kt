package com.example.minidesk

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class MediaTrackInfo(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val artwork: Bitmap? = null
)

class MiniDeskMediaListenerService : NotificationListenerService() {

    companion object {
        var instance: MiniDeskMediaListenerService? = null
            private set

        var liveTrackInfo by mutableStateOf(MediaTrackInfo())
            private set

        fun isNotificationListenerGranted(context: Context): Boolean {
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return flat?.contains(context.packageName) == true
        }
    }

    private var mediaSessionManager: MediaSessionManager? = null
    private var activeMediaController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateMediaInfo()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateMediaInfo()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupMediaSessionManager()
    }

    override fun onDestroy() {
        activeMediaController?.unregisterCallback(controllerCallback)
        activeMediaController = null
        instance = null
        super.onDestroy()
    }

    fun setupMediaSessionManager() {
        try {
            mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val component = ComponentName(this, MiniDeskMediaListenerService::class.java)
            val sessions = mediaSessionManager?.getActiveSessions(component)
            attachToActiveSession(sessions)

            mediaSessionManager?.addOnActiveSessionsChangedListener({ newSessions ->
                attachToActiveSession(newSessions)
            }, component)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun attachToActiveSession(controllers: List<MediaController>?) {
        activeMediaController?.unregisterCallback(controllerCallback)
        val active = controllers?.firstOrNull()
        activeMediaController = active
        active?.registerCallback(controllerCallback)
        updateMediaInfo()
    }

    private fun updateMediaInfo() {
        val controller = activeMediaController
        if (controller == null) {
            if (liveTrackInfo != MediaTrackInfo()) {
                liveTrackInfo = MediaTrackInfo()
            }
            return
        }

        val metadata = controller.metadata
        val state = controller.playbackState

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Unknown Track"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: "Unknown Artist"
        val isPlaying = state?.state == PlaybackState.STATE_PLAYING
        val artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val newInfo = MediaTrackInfo(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            artwork = artwork
        )

        // Throttle updates: Only emit if track title, artist, playing status, or artwork changed
        if (liveTrackInfo.title != newInfo.title ||
            liveTrackInfo.artist != newInfo.artist ||
            liveTrackInfo.isPlaying != newInfo.isPlaying ||
            liveTrackInfo.artwork != newInfo.artwork
        ) {
            liveTrackInfo = newInfo
        }
    }

    fun togglePlayPause() {
        val controller = activeMediaController
        val state = controller?.playbackState?.state
        if (controller != null && state != null) {
            if (state == PlaybackState.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        }
    }

    fun nextTrack() {
        activeMediaController?.transportControls?.skipToNext()
    }

    fun previousTrack() {
        activeMediaController?.transportControls?.skipToPrevious()
    }
}
