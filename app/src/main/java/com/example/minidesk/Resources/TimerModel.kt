package com.example.minidesk.tools

/** Time source. Production uses SystemClock.elapsedRealtime; tests use a fake. */
fun interface Clock { fun now(): Long }

data class TimerSnapshot(val running: Boolean, val endAt: Long, val pausedMs: Long)
data class StopwatchSnapshot(val running: Boolean, val startedAt: Long, val accumulatedMs: Long, val laps: List<Long>)

/**
 * Countdown timer stored as an end timestamp, so it keeps counting while the Deck is
 * closed and cannot drift. Nothing here depends on the UI being alive.
 */
class TimerModel(private val clock: Clock) {
    private var running = false
    private var endAt = 0L
    private var pausedMs = 0L

    /** Ends a run that has reached zero. Called at the start of every read or write. */
    private fun settle() {
        if (running && clock.now() >= endAt) {
            running = false
            endAt = 0L
            pausedMs = 0L
        }
    }

    fun isRunning(): Boolean { settle(); return running }

    fun remainingMs(): Long {
        settle()
        return if (running) endAt - clock.now() else pausedMs
    }

    fun add(ms: Long) {
        settle()
        if (running) endAt = minOf(endAt + ms, clock.now() + MAX_MS) else pausedMs = minOf(pausedMs + ms, MAX_MS)
    }

    fun start() {
        settle()
        if (!running && pausedMs > 0L) {
            endAt = clock.now() + pausedMs
            pausedMs = 0L
            running = true
        }
    }

    fun pause() {
        settle()
        if (running) {
            pausedMs = endAt - clock.now()
            endAt = 0L
            running = false
        }
    }

    fun toggle() { if (isRunning()) pause() else start() }

    fun reset() { running = false; endAt = 0L; pausedMs = 0L }

    fun snapshot() = TimerSnapshot(running, endAt, pausedMs)

    fun restore(s: TimerSnapshot) {
        running = s.running; endAt = s.endAt; pausedMs = s.pausedMs
        settle()
    }

    companion object { const val MAX_MS = 99L * 3_600_000L }
}

class StopwatchModel(private val clock: Clock) {
    private var running = false
    private var startedAt = 0L
    private var accumulated = 0L
    private val lapList = mutableListOf<Long>()

    /** Newest lap first. */
    val laps: List<Long> get() = lapList.toList()

    fun isRunning() = running

    fun elapsedMs(): Long = if (running) accumulated + (clock.now() - startedAt) else accumulated

    fun start() { if (!running) { running = true; startedAt = clock.now() } }

    fun pause() { if (running) { accumulated += clock.now() - startedAt; running = false } }

    fun toggle() { if (running) pause() else start() }

    fun lap() { if (running) lapList.add(0, elapsedMs()) }

    fun reset() { running = false; startedAt = 0L; accumulated = 0L; lapList.clear() }

    fun snapshot() = StopwatchSnapshot(running, startedAt, accumulated, laps)

    fun restore(s: StopwatchSnapshot) {
        running = s.running; startedAt = s.startedAt; accumulated = s.accumulatedMs
        lapList.clear(); lapList.addAll(s.laps)
    }
}
