package com.example.minidesk.tools

import java.util.Locale

/** Countdown text. Rounds up so 00:00 only shows when the timer is truly done. */
fun formatTimer(ms: Long): String {
    val totalSec = (ms.coerceAtLeast(0L) + 999L) / 1000L
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}

fun formatStopwatch(ms: Long): String {
    val v = ms.coerceAtLeast(0L)
    return String.format(Locale.US, "%02d:%02d.%02d", v / 60000, (v % 60000) / 1000, (v % 1000) / 10)
}
