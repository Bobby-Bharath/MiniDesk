package com.example.minidesk.deck

import androidx.compose.runtime.Composable

/**
 * Describes one built-in widget. [content] draws the widget body at whatever size the grid gives it
 * (it should fill the max size and lay itself out responsively, like Volume in your current Deck).
 */
class BuiltinSpec(
    val type: String,
    val title: String,
    val defW: Int,
    val defH: Int,
    val limits: Limits,
    val onFirstRun: Boolean = false,
    val content: @Composable () -> Unit,
)

class BuiltinRegistry(val all: List<BuiltinSpec>) {
    private val byType = all.associateBy { it.type }
    operator fun get(type: String?): BuiltinSpec? = type?.let { byType[it] }
    val firstRunDefaults: List<Triple<String, Int, Int>>
        get() = all.filter { it.onFirstRun }.map { Triple(it.type, it.defW, it.defH) }
}
