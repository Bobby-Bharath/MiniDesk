package com.example.minidesk.deck

import kotlin.math.max
import kotlin.math.roundToInt

/** One widget's rectangle on the grid, in cell units. Pure data, no Android types. */
data class Cell(val id: String, val col: Int, val row: Int, val w: Int, val h: Int) {
    val right: Int get() = col + w
    val bottom: Int get() = row + h
    fun overlaps(o: Cell): Boolean =
        col < o.right && o.col < right && row < o.bottom && o.row < bottom
}

/**
 * All placement rules live here and nowhere else, so drag, resize, add and undo cannot
 * disagree about what a legal layout is. Rows are unbounded (the Deck scrolls); columns are fixed.
 *
 * Guarantee: [place] always returns a layout with no overlaps, no cell outside the columns,
 * and the same set of ids it was given (plus the moved one).
 */
class GridEngine(val cols: Int) {
    init { require(cols >= 1) { "cols must be >= 1" } }

    /** Forces a cell inside the columns, with at least 1x1 size and row >= 0. */
    fun clamp(c: Cell): Cell {
        val w = c.w.coerceIn(1, cols)
        val h = max(1, c.h)
        return c.copy(w = w, h = h, col = c.col.coerceIn(0, cols - w), row = max(0, c.row))
    }

    /**
     * Puts [moved] at its requested rectangle and pushes anything it collides with downwards,
     * cascading. Items that do not collide stay exactly where they are. Used for move, resize
     * and undo-restore, so all three behave the same way.
     */
    fun place(items: List<Cell>, moved: Cell): List<Cell> {
        val m = clamp(moved)
        val settled = ArrayList<Cell>(items.size + 1)
        settled.add(m)
        val resolved = HashMap<String, Cell>(items.size + 1)
        resolved[m.id] = m

        val others = items
            .filter { it.id != m.id }
            .map { clamp(it) }
            .sortedWith(compareBy<Cell>({ it.row }, { it.col }))

        for (c in others) {
            var cur = c
            while (true) {
                val hit = settled.firstOrNull { it.overlaps(cur) } ?: break
                cur = cur.copy(row = hit.bottom)   // strictly increases, so the loop ends
            }
            settled.add(cur)
            resolved[cur.id] = cur
        }

        val out = ArrayList<Cell>(items.size + 1)
        for (c in items) out.add(resolved.getValue(c.id))   // keep the caller's order
        if (items.none { it.id == m.id }) out.add(m)
        return out
    }

    /** First free spot for a w x h widget, scanning left-to-right, top-to-bottom. */
    fun findFree(items: List<Cell>, w: Int, h: Int): Pair<Int, Int> {
        val ww = w.coerceIn(1, cols)
        val hh = max(1, h)
        var row = 0
        while (true) {
            for (col in 0..(cols - ww)) {
                val probe = Cell("", col, row, ww, hh)
                if (items.none { it.overlaps(probe) }) return col to row
            }
            row++
        }
    }

    fun rowsUsed(items: List<Cell>): Int = items.maxOfOrNull { it.bottom } ?: 0
}

object GridMath {
    /** How many whole cells a pixel drag distance moves or grows something. */
    fun deltaCells(deltaPx: Float, stepPx: Float): Int =
        if (stepPx <= 0f) 0 else (deltaPx / stepPx).roundToInt()

    /** Pixel length of [n] cells including the gaps between them. */
    fun spanPx(n: Int, cellPx: Float, gapPx: Float): Float = n * cellPx + (n - 1) * gapPx
}
