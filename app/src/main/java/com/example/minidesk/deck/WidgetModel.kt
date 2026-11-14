package com.example.minidesk.deck

/** Shared grid constants. The editor publishes the real cell size so the picker can size new widgets. */
object DeckGrid {
    const val COLS = 4
    const val GAP_DP = 8f
    private const val FALLBACK_CELL_DP = 64f
    @Volatile var cellDp: Float = FALLBACK_CELL_DP
}

/**
 * One thing on the Deck. Either a built-in widget ([builtinType] set) or an installed app's
 * widget ([appWidgetId] and [provider] set). Position and size are in grid cells.
 */
data class WidgetEntry(
    val id: String,
    val col: Int,
    val row: Int,
    val w: Int,
    val h: Int,
    val builtinType: String? = null,
    val appWidgetId: Int = NO_ID,
    val provider: String? = null,
) {
    val isApp: Boolean get() = appWidgetId != NO_ID
    fun toCell() = Cell(id, col, row, w, h)
    fun withCell(c: Cell) = copy(col = c.col, row = c.row, w = c.w, h = c.h)

    companion object { const val NO_ID = -1 }
}
