package com.example.minidesk.deck

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** Smallest and largest size, in cells, a widget may be resized to. */
data class Limits(val minW: Int, val minH: Int, val maxW: Int, val maxH: Int) {
    fun clampW(w: Int, room: Int): Int {
        val hi = max(minW, min(maxW, room))
        return w.coerceIn(minW, hi)
    }
    fun clampH(h: Int): Int = h.coerceIn(minH, maxH)

    companion object {
        fun fixed(w: Int, h: Int) = Limits(w, h, w, h)
    }
}

data class Sizing(val defW: Int, val defH: Int, val limits: Limits)

/** Converts a provider's dp sizes into grid cells. Pure so it can be unit tested. */
object WidgetSizing {
    const val MAX_ROWS = 8

    fun span(dp: Float, cellDp: Float, gapDp: Float): Int =
        max(1, ceil(((dp + gapDp) / (cellDp + gapDp)).toDouble() - 1e-6).toInt())

    /** Largest whole number of cells that still fits inside [dp]. Used for maximum sizes. */
    fun spanFloor(dp: Float, cellDp: Float, gapDp: Float): Int =
        max(1, floor(((dp + gapDp) / (cellDp + gapDp)).toDouble() + 1e-6).toInt())

    fun compute(
        defWdp: Float, defHdp: Float,
        minWdp: Float, minHdp: Float,
        maxWdp: Float, maxHdp: Float,      // 0 = no limit declared
        canResizeH: Boolean, canResizeV: Boolean,
        cellDp: Float, gapDp: Float, cols: Int,
    ): Sizing {
        val defW = span(defWdp, cellDp, gapDp).coerceAtMost(cols)
        val defH = span(defHdp, cellDp, gapDp)
        val minW = if (canResizeH) span(minWdp, cellDp, gapDp).coerceIn(1, defW) else defW
        val minH = if (canResizeV) span(minHdp, cellDp, gapDp).coerceIn(1, defH) else defH
        val maxW = when {
            !canResizeH -> defW
            maxWdp > 0f -> spanFloor(maxWdp, cellDp, gapDp).coerceIn(defW, cols)
            else -> cols
        }
        val maxH = when {
            !canResizeV -> defH
            maxHdp > 0f -> max(defH, spanFloor(maxHdp, cellDp, gapDp))
            else -> max(defH, MAX_ROWS)
        }
        return Sizing(defW, defH, Limits(minW, minH, maxW, maxH))
    }
}
