package com.example.minidesk

import android.R
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.minidesk.data.settings.MiniDeskPreferences
import com.example.minidesk.data.settings.MiniDeskSettingsRepository
import com.example.minidesk.deck.WidgetHostManager
import com.example.minidesk.service.MiniDeskWidgetHost
import com.example.minidesk.ui.theme.MiniDeskTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MiniDeskOverlayService : Service(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private lateinit var windowManager: WindowManager
    private var notchView: ComposeView? = null
    private var notchLayoutParams: WindowManager.LayoutParams? = null
    private var drawerView: ComposeView? = null
    private var drawerLayoutParams: WindowManager.LayoutParams? = null

    private var drawerDismissSignalCounter by mutableIntStateOf(0)
    private var currentDrawerMode by mutableStateOf(OverlayState.IDLE_NOTCH)
    private var openTimestamp: Long = 0L
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var volumeController: VolumeController
    private lateinit var brightnessController: BrightnessController
    private lateinit var flashlightController: FlashlightController
    private lateinit var mediaController: MediaController
    private lateinit var scratchpadHelper: ScratchpadHelper
    private lateinit var clipboardHelper: ClipboardManagerHelper
    private lateinit var widgetHost: MiniDeskWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        volumeController = VolumeController(this)
        brightnessController = BrightnessController(this)
        flashlightController = FlashlightController(this)
        mediaController = MediaController(this)
        scratchpadHelper = ScratchpadHelper(this)
        clipboardHelper = ClipboardManagerHelper(this)
        widgetHost = MiniDeskWidgetHost(applicationContext)
        appWidgetManager = AppWidgetManager.getInstance(applicationContext)

        WidgetHostManager.get(this).startListening()

        createNotificationChannel()
        startForegroundServiceWithNotification()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupNotchWindow()
        setupDrawerWindow()

        observePreferences()
        startForegroundAppMonitoring()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "MiniDesk Overlay Service",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps the floating sidebar notch active"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun startForegroundServiceWithNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MiniDesk Sidebar Active")
            .setContentText("Organic side notch running on screen edge")
            .setSmallIcon(R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setupNotchWindow() {
        val density = resources.displayMetrics.density
        val notchParams = WindowManager.LayoutParams(
            (24 * density).toInt(),
            (96 * density).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.END or Gravity.TOP
            y = (resources.displayMetrics.heightPixels * 0.55f).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
        notchLayoutParams = notchParams

        notchView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@MiniDeskOverlayService)
            setViewTreeViewModelStoreOwner(this@MiniDeskOverlayService)
            setViewTreeSavedStateRegistryOwner(this@MiniDeskOverlayService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                MiniDeskTheme {
                    MiniDeskNotch(
                        onStateChange = { targetState ->
                            openDrawerWindow(targetState)
                        }
                    )
                }
            }
        }

        windowManager.addView(notchView, notchParams)
    }

    private fun setupDrawerWindow() {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenWidthDp = (displayMetrics.widthPixels / density).toInt()

        val spineWidthDp = 64
        val gapDp = 8
        val leftMarginDp = 12

        val bentoWidthDp = (screenWidthDp - spineWidthDp - gapDp - leftMarginDp).coerceIn(280, 360)
        val initialWidthDp = bentoWidthDp + gapDp + spineWidthDp + 4

        val drawerParams = WindowManager.LayoutParams(
            (initialWidthDp * density).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
        drawerLayoutParams = drawerParams

        drawerView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@MiniDeskOverlayService)
            setViewTreeViewModelStoreOwner(this@MiniDeskOverlayService)
            setViewTreeSavedStateRegistryOwner(this@MiniDeskOverlayService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                MiniDeskTheme {
                    if (currentDrawerMode != OverlayState.IDLE_NOTCH) {
                        MiniDeskDrawerContainer(
                            drawerMode = currentDrawerMode,
                            dismissSignal = drawerDismissSignalCounter,
                            onStateChange = { newMode ->
                                if (newMode == OverlayState.IDLE_NOTCH) {
                                    triggerDrawerDismiss()
                                } else if (newMode != currentDrawerMode) {
                                    openDrawerWindow(newMode)
                                }
                            },
                            onDismissComplete = {
                                closeDrawerWindowAndRestoreNotch()
                            },
                            onFocusChanged = { isFocused ->
                                setOverlayFocusable(isFocused)
                            },
                            volumeController = volumeController,
                            brightnessController = brightnessController,
                            flashlightController = flashlightController,
                            mediaController = mediaController,
                            scratchpadHelper = scratchpadHelper,
                            clipboardHelper = clipboardHelper,
                            widgetHost = widgetHost,
                            appWidgetManager = appWidgetManager
                        )
                    }
                }
            }

            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    val elapsed = SystemClock.uptimeMillis() - openTimestamp
                    if (elapsed < 500L) {
                        Log.d("MiniDeskDebug", "BLOCKED NATIVE_ACTION_OUTSIDE - only ${elapsed}ms since open!")
                        return@setOnTouchListener true
                    }
                    Log.d("MiniDeskDebug", "NATIVE_ACTION_OUTSIDE verified after ${elapsed}ms -> dismissing")
                    triggerDrawerDismiss()
                    v.performClick()
                    return@setOnTouchListener true
                }
                false
            }

            visibility = View.GONE
        }

        // Attach drawerView ONCE during service startup. Surface buffer remains warm in memory.
        windowManager.addView(drawerView, drawerParams)
    }

    private fun observePreferences() {
        val settingsRepo = MiniDeskSettingsRepository.get(this)
        serviceScope.launch {
            settingsRepo.preferences.collect { prefs ->
                updateNotchLayout(prefs)
            }
        }
    }

    private fun updateNotchLayout(prefs: MiniDeskPreferences) {
        val view = notchView ?: return
        val params = notchLayoutParams ?: return

        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenHeightPx = displayMetrics.heightPixels

        val notchWidthPx = (prefs.notchWidthDp * density).toInt()
        val notchHeightPx = (prefs.notchHeightDp * density).toInt()

        val gravity = (if (prefs.notchEdgeRight) Gravity.END else Gravity.START) or Gravity.TOP
        val yPx = ((screenHeightPx * prefs.notchYPercentage) - (notchHeightPx / 2f)).toInt()

        params.gravity = gravity
        params.y = yPx
        params.width = notchWidthPx
        params.height = notchHeightPx

        if (view.isAttachedToWindow) {
            try {
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startForegroundAppMonitoring() {
        val settingsRepo = MiniDeskSettingsRepository.get(this)
        val am = getSystemService(ACTIVITY_SERVICE) as? ActivityManager

        serviceScope.launch(Dispatchers.IO) {
            while (true) {
                delay(2000L)
                val blacklist = settingsRepo.preferences.value.blacklistedPackages
                if (blacklist.isNotEmpty() && am != null) {
                    val foregroundPkg = try {
                        val processes = am.runningAppProcesses
                        processes?.firstOrNull { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }?.processName
                    } catch (e: Exception) {
                        null
                    }

                    if (foregroundPkg != null) {
                        withContext(Dispatchers.Main) {
                            if (blacklist.contains(foregroundPkg)) {
                                notchView?.visibility = View.GONE
                            } else if (currentDrawerMode == OverlayState.IDLE_NOTCH) {
                                notchView?.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    fun setOverlayFocusable(focusable: Boolean) {
        val params = drawerLayoutParams ?: return
        if (currentDrawerMode != OverlayState.COMMAND_DECK) return
        if (focusable) {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        } else {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        if (drawerView?.isAttachedToWindow == true) {
            windowManager.updateViewLayout(drawerView, params)
        }
    }

    @Suppress("DEPRECATION")
    private fun openDrawerWindow(targetMode: OverlayState) {
        if (targetMode == OverlayState.IDLE_NOTCH) return
        val params = drawerLayoutParams ?: return

        try {
            Log.d("MiniDeskDebug", ">>> openDrawer CALLED with state: $targetMode")
            val previousMode = currentDrawerMode

            if (previousMode == OverlayState.IDLE_NOTCH) {
                openTimestamp = SystemClock.uptimeMillis()
            }
            currentDrawerMode = targetMode

            if (targetMode == OverlayState.COMMAND_DECK) {
                try {
                    widgetHost.startListening()
                } catch (e: Exception) {
                    Log.e("MiniDeskCrash", "Error starting widgetHost listening", e)
                }
            }

            notchView?.visibility = View.INVISIBLE

            val displayMetrics = resources.displayMetrics
            val density = displayMetrics.density
            val screenWidthDp = (displayMetrics.widthPixels / density).toInt()

            val spineWidthDp = 64
            val gapDp = 8
            val leftMarginDp = 12

            val bentoWidthDp = (screenWidthDp - spineWidthDp - gapDp - leftMarginDp).coerceIn(280, 360)
            val expandedWidthDp = bentoWidthDp + gapDp + spineWidthDp + 4
            val spineWidthParamsDp = spineWidthDp + 4

            // Make touchable
            var flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            if (targetMode != OverlayState.COMMAND_DECK) {
                flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            params.flags = flags
            params.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            params.y = 0

            if (targetMode == OverlayState.COMMAND_DECK) {
                params.width = (expandedWidthDp * density).toInt()
            } else {
                params.width = (spineWidthParamsDp * density).toInt()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            if (targetMode == OverlayState.COMMAND_DECK) {
                params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            }

            drawerView?.let { view ->
                if (view.isAttachedToWindow) {
                    windowManager.updateViewLayout(view, params)
                }
                view.visibility = View.VISIBLE

                view.handler?.removeCallbacksAndMessages(null)
                view.postDelayed({
                    if (currentDrawerMode != OverlayState.IDLE_NOTCH) {
                        params.flags = params.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        try {
                            if (view.isAttachedToWindow) {
                                windowManager.updateViewLayout(view, params)
                                Log.d("MiniDeskDebug", "Outside touch watcher ARMED")
                            }
                        } catch (e: Exception) {
                            Log.e("MiniDeskCrash", "Error arming outside touch", e)
                        }
                    }
                }, 350L)
            }
        } catch (e: Throwable) {
            Log.e("MiniDeskCrash", "Fatal error opening drawer window. Resetting notch visibility.", e)
            currentDrawerMode = OverlayState.IDLE_NOTCH
            notchView?.visibility = View.VISIBLE
            drawerView?.visibility = View.GONE
        }
    }

    private fun triggerDrawerDismiss() {
        drawerDismissSignalCounter++
    }

    private fun closeDrawerWindowAndRestoreNotch() {
        try {
            widgetHost.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        drawerView?.handler?.removeCallbacksAndMessages(null)
        val params = drawerLayoutParams
        if (params != null && drawerView?.isAttachedToWindow == true) {
            // Make untouchable and hidden WITHOUT removing the view or destroying the surface
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(drawerView, params)
        }

        drawerView?.visibility = View.GONE
        notchView?.visibility = View.VISIBLE
        currentDrawerMode = OverlayState.IDLE_NOTCH
        Log.d("MiniDeskDebug", "Drawer closed & notch restored (warm surface preserved)")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        try {
            widgetHost.stopListening()
            WidgetHostManager.get(this).stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        drawerView?.handler?.removeCallbacksAndMessages(null)

        drawerView?.let { view ->
            if (view.isAttachedToWindow) {
                windowManager.removeView(view)
            }
        }
        drawerView = null

        notchView?.let { view ->
            if (view.isAttachedToWindow) {
                windowManager.removeView(view)
            }
        }
        notchView = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "minidesk_overlay_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
