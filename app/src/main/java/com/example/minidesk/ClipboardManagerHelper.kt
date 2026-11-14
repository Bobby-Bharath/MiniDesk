package com.example.minidesk

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.minidesk.data.clipboard.ClipboardVaultRepository
import java.util.UUID

data class ClipItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)

class ClipboardManagerHelper(context: Context) {
    private val repository = ClipboardVaultRepository.get(context)

    var clips by mutableStateOf<List<ClipItem>>(emptyList())
        private set

    init {
        syncPrimaryClip()
    }

    fun syncPrimaryClip() {
        repository.syncSystemClipboard()
    }

    fun addClip(text: String, isPinned: Boolean = false) {
        repository.addTextClip(text, isPinned)
    }

    fun togglePin(item: ClipItem) {
        repository.togglePin(item.id)
    }

    fun copyToClipboard(text: String) {
        repository.copyToClipboard(text)
    }

    fun clearUnpinnedHistory() {
        repository.clearUnpinned()
    }
}
