package com.example.minidesk.deck

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Single source of truth for what is on the Deck. The service, the Deck UI and the picker
 * activity all live in one process and share this object, so a widget added by the picker
 * shows up in the Deck immediately.
 */
class WidgetRepository private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("minidesk_widgets", Context.MODE_PRIVATE)
    private val engine = GridEngine(DeckGrid.COLS)
    private val state = MutableStateFlow(load())

    val items: StateFlow<List<WidgetEntry>> = state.asStateFlow()

    @Synchronized
    fun seedIfFirstRun(defaults: List<Triple<String, Int, Int>>) {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        defaults.forEach { (type, w, h) -> addBuiltin(type, w, h) }
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    /** Applies positions and sizes from the editor. Unknown ids are ignored. */
    @Synchronized
    fun applyLayout(cells: List<Cell>) {
        val byId = cells.associateBy { it.id }
        commit(state.value.map { e -> byId[e.id]?.let(e::withCell) ?: e })
    }

    @Synchronized
    fun addBuiltin(type: String, w: Int, h: Int) {
        val (col, row) = engine.findFree(state.value.map { it.toCell() }, w, h)
        commit(state.value + WidgetEntry(UUID.randomUUID().toString(), col, row, w, h, builtinType = type))
    }

    @Synchronized
    fun addAppWidget(appWidgetId: Int, provider: String, w: Int, h: Int) {
        val (col, row) = engine.findFree(state.value.map { it.toCell() }, w, h)
        commit(
            state.value + WidgetEntry(
                UUID.randomUUID().toString(), col, row, w, h,
                appWidgetId = appWidgetId, provider = provider,
            )
        )
    }

    @Synchronized
    fun remove(id: String): WidgetEntry? {
        val gone = state.value.firstOrNull { it.id == id } ?: return null
        commit(state.value.filter { it.id != id })
        return gone
    }

    /** Undo for [remove]: puts the entry back and pushes neighbours aside if its spot was taken. */
    @Synchronized
    fun restore(entry: WidgetEntry) {
        if (state.value.any { it.id == entry.id }) return
        val placed = engine.place(state.value.map { it.toCell() }, entry.toCell())
        val byId = placed.associateBy { it.id }
        val all = state.value + entry
        commit(all.map { e -> byId[e.id]?.let(e::withCell) ?: e })
    }

    @Synchronized
    fun resetToDefaults(defaults: List<Triple<String, Int, Int>>) {
        commit(emptyList())
        prefs.edit().putBoolean(KEY_SEEDED, false).apply()
        seedIfFirstRun(defaults)
    }

    private fun commit(list: List<WidgetEntry>) {
        state.value = list
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(
                JSONObject()
                    .put("id", e.id).put("c", e.col).put("r", e.row).put("w", e.w).put("h", e.h)
                    .put("b", e.builtinType ?: JSONObject.NULL)
                    .put("a", e.appWidgetId)
                    .put("p", e.provider ?: JSONObject.NULL)
            )
        }
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply()
    }

    private fun load(): List<WidgetEntry> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                WidgetEntry(
                    id = o.getString("id"),
                    col = o.getInt("c"), row = o.getInt("r"), w = o.getInt("w"), h = o.getInt("h"),
                    builtinType = if (o.isNull("b")) null else o.getString("b"),
                    appWidgetId = o.optInt("a", WidgetEntry.NO_ID),
                    provider = if (o.isNull("p")) null else o.getString("p"),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_ITEMS = "items"
        private const val KEY_SEEDED = "seeded"
        @Volatile private var instance: WidgetRepository? = null
        fun get(context: Context): WidgetRepository =
            instance ?: synchronized(this) {
                instance ?: WidgetRepository(context.applicationContext).also { instance = it }
            }
    }
}
