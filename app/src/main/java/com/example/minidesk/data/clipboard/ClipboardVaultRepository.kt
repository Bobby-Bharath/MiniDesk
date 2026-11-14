package com.example.minidesk.data.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ClipItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean
)

class ClipboardVaultRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("minidesk_clipboard_vault", Context.MODE_PRIVATE)
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _clips = MutableStateFlow<List<ClipItem>>(emptyList())
    val clips: StateFlow<List<ClipItem>> = _clips.asStateFlow()

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        syncSystemClipboardInternal()
    }

    init {
        loadFromStorage()
        try {
            clipboard.addPrimaryClipChangedListener(clipListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncSystemClipboardInternal()
    }

    fun syncSystemClipboard() {
        scope.launch {
            delay(100L)
            syncSystemClipboardInternal()
        }
    }

    fun syncSystemClipboardInternal() {
        try {
            val primary = clipboard.primaryClip
            if (primary != null && primary.itemCount > 0) {
                val rawText = primary.getItemAt(0).text?.toString()?.trim()
                if (!rawText.isNullOrBlank()) {
                    addTextClip(rawText, false)
                }
            }
        } catch (e: Exception) {
            Log.w("MiniDeskClipboard", "Clipboard read restricted by OS", e)
        }
    }

    fun addTextClip(rawText: String, isPinned: Boolean = false) {
        val current = _clips.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.text == rawText }

        if (existingIndex != -1) {
            val existing = current.removeAt(existingIndex)
            current.add(0, existing.copy(timestamp = System.currentTimeMillis(), isPinned = isPinned || existing.isPinned))
        } else {
            val newItem = ClipItem(
                id = "clip_${System.currentTimeMillis()}",
                text = rawText,
                timestamp = System.currentTimeMillis(),
                isPinned = isPinned
            )
            current.add(0, newItem)
            if (current.size > 30) {
                val lastUnpinned = current.indexOfLast { !it.isPinned }
                if (lastUnpinned != -1) current.removeAt(lastUnpinned)
            }
        }
        _clips.value = current
        saveToStorage()
    }

    fun copyToClipboard(text: String) {
        try {
            val clip = ClipData.newPlainText("MiniDesk Clip", text)
            clipboard.setPrimaryClip(clip)
            addTextClip(text, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePin(clipId: String) {
        val updated = _clips.value.map {
            if (it.id == clipId) it.copy(isPinned = !it.isPinned) else it
        }
        _clips.value = updated
        saveToStorage()
    }

    fun clearUnpinned() {
        val pinnedOnly = _clips.value.filter { it.isPinned }
        _clips.value = pinnedOnly
        saveToStorage()
    }

    private fun loadFromStorage() {
        val list = mutableListOf<ClipItem>()

        // 1. Try JSON storage
        val jsonStr = prefs.getString("saved_clips", null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        ClipItem(
                            id = obj.getString("id"),
                            text = obj.getString("text"),
                            timestamp = obj.getLong("timestamp"),
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Try pipe-delimited storage ("clip_history_v2")
        val set = prefs.getStringSet("clip_history_v2", emptySet()) ?: emptySet()
        set.forEach { entry ->
            val parts = entry.split("|", limit = 4)
            if (parts.size == 4) {
                val text = parts[3]
                if (list.none { it.text == text }) {
                    list.add(
                        ClipItem(
                            id = parts[0],
                            isPinned = parts[1] == "1",
                            timestamp = parts[2].toLongOrNull() ?: System.currentTimeMillis(),
                            text = text
                        )
                    )
                }
            }
        }

        // Sort descending by timestamp
        _clips.value = list.sortedByDescending { it.timestamp }
    }

    private fun saveToStorage() {
        val currentList = _clips.value

        // Save JSON format
        val arr = JSONArray()
        currentList.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("timestamp", item.timestamp)
                put("isPinned", item.isPinned)
            }
            arr.put(obj)
        }

        // Save pipe-delimited format for backward compatibility
        val set = currentList.map { "${it.id}|${if (it.isPinned) 1 else 0}|${it.timestamp}|${it.text}" }.toSet()

        prefs.edit()
            .putString("saved_clips", arr.toString())
            .putStringSet("clip_history_v2", set)
            .apply()
    }

    companion object {
        @Volatile
        private var instance: ClipboardVaultRepository? = null

        fun get(context: Context): ClipboardVaultRepository {
            return instance ?: synchronized(this) {
                instance ?: ClipboardVaultRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
