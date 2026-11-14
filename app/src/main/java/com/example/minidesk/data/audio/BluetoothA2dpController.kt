package com.example.minidesk.data.audio

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

class BluetoothA2dpController(context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter = bluetoothManager?.adapter
    private var a2dpProxy: BluetoothA2dp? = null

    init {
        try {
            bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                    if (profile == BluetoothProfile.A2DP) {
                        a2dpProxy = proxy as? BluetoothA2dp
                        Log.d("MiniDeskAudio", "Bluetooth A2DP profile proxy CONNECTED")
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    if (profile == BluetoothProfile.A2DP) {
                        a2dpProxy = null
                    }
                }
            }, BluetoothProfile.A2DP)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnectA2dp(device: BluetoothDevice): Boolean {
        return try {
            @Suppress("MissingPermission")
            val method = a2dpProxy?.javaClass?.getMethod("disconnect", BluetoothDevice::class.java)
            @Suppress("MissingPermission")
            (method?.invoke(a2dpProxy, device) as? Boolean) ?: false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            Log.e("MiniDeskAudio", "Failed to disconnect A2DP", e)
            false
        }
    }

    fun connectA2dp(device: BluetoothDevice): Boolean {
        return try {
            @Suppress("MissingPermission")
            val method = a2dpProxy?.javaClass?.getMethod("connect", BluetoothDevice::class.java)
            @Suppress("MissingPermission")
            (method?.invoke(a2dpProxy, device) as? Boolean) ?: false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            Log.e("MiniDeskAudio", "Failed to connect A2DP", e)
            false
        }
    }

    fun getConnectedA2dpDevices(): List<BluetoothDevice> {
        return try {
            @Suppress("MissingPermission")
            a2dpProxy?.connectedDevices ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
