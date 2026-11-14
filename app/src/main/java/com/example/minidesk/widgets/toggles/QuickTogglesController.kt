package com.example.minidesk.widgets.toggles

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class QuickTogglesController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var torchCameraId: String? = null

    var isTorchOn by mutableStateOf(false)
        private set

    var isAutoRotateOn by mutableStateOf(false)
        private set

    var isWifiOn by mutableStateOf(false)
        private set

    var isBtOn by mutableStateOf(false)
        private set

    var isLocationOn by mutableStateOf(false)
        private set

    var isDndOn by mutableStateOf(false)
        private set

    init {
        try {
            val manager = cameraManager
            if (manager != null) {
                torchCameraId = manager.cameraIdList.firstOrNull { id ->
                    val characteristics = manager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
                } ?: manager.cameraIdList.firstOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateAllStates()
    }

    fun canWriteSettings(): Boolean = Settings.System.canWrite(context)

    fun getWriteSettingsIntent(): Intent = Intent(
        Settings.ACTION_MANAGE_WRITE_SETTINGS,
        Uri.parse("package:${context.packageName}")
    )

    fun isDndPolicyGranted(): Boolean {
        return notificationManager?.isNotificationPolicyAccessGranted == true
    }

    fun getDndPolicyIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    fun updateAllStates() {
        // Auto Rotate
        isAutoRotateOn = try {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
        } catch (e: Exception) {
            false
        }

        // Wi-Fi
        isWifiOn = try {
            wifiManager?.isWifiEnabled == true
        } catch (e: Exception) {
            false
        }

        // BT
        isBtOn = try {
            bluetoothManager?.adapter?.isEnabled == true
        } catch (e: Exception) {
            false
        }

        // Location
        isLocationOn = try {
            locationManager?.isLocationEnabled == true
        } catch (e: Exception) {
            false
        }

        // DND
        isDndOn = try {
            val filter = notificationManager?.currentInterruptionFilter
            filter == NotificationManager.INTERRUPTION_FILTER_NONE || filter == NotificationManager.INTERRUPTION_FILTER_ALARMS || filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
        } catch (e: Exception) {
            false
        }
    }

    fun toggleTorch() {
        val id = torchCameraId ?: return
        val manager = cameraManager ?: return
        try {
            val target = !isTorchOn
            manager.setTorchMode(id, target)
            isTorchOn = target
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleAutoRotate() {
        if (!canWriteSettings()) return
        val target = if (isAutoRotateOn) 0 else 1
        try {
            Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, target)
            isAutoRotateOn = target == 1
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openWifiPanel(onRequestDismiss: () -> Unit) {
        onRequestDismiss()
        try {
            context.startActivity(Intent("android.settings.panel.action.INTERNET_CONNECTIVITY").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun openBluetoothSettings(onRequestDismiss: () -> Unit) {
        onRequestDismiss()
        try {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openLocationSettings(onRequestDismiss: () -> Unit) {
        onRequestDismiss()
        try {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleDnd() {
        val manager = notificationManager ?: return
        if (!isDndPolicyGranted()) return

        try {
            val targetFilter = if (isDndOn) {
                NotificationManager.INTERRUPTION_FILTER_ALL
            } else {
                NotificationManager.INTERRUPTION_FILTER_NONE
            }
            manager.setInterruptionFilter(targetFilter)
            isDndOn = targetFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerListeners(onChanged: () -> Unit): Triple<CameraManager.TorchCallback?, ContentObserver?, BroadcastReceiver?> {
        val torchCallback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == torchCameraId) {
                    isTorchOn = enabled
                    onChanged()
                }
            }
        }
        try {
            cameraManager?.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                updateAllStates()
                onChanged()
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                false,
                observer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateAllStates()
                onChanged()
            }
        }
        try {
            val filter = IntentFilter().apply {
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                @Suppress("DEPRECATION")
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(LocationManager.MODE_CHANGED_ACTION)
                addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            }
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateAllStates()
        return Triple(torchCallback, observer, receiver)
    }

    fun unregisterListeners(torchCallback: CameraManager.TorchCallback?, observer: ContentObserver?, receiver: BroadcastReceiver?) {
        try {
            if (torchCallback != null) cameraManager?.unregisterTorchCallback(torchCallback)
            if (observer != null) context.contentResolver.unregisterContentObserver(observer)
            if (receiver != null) context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
