package com.example.minidesk.deck

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context

data class ProviderRow(
    val component: ComponentName,
    val label: String,
    val appLabel: String,
    val info: AppWidgetProviderInfo,
)

/**
 * Wraps AppWidgetHost. Hosting third-party widgets is a three-step handshake with the system
 * (allocate an id, get the user's permission to bind, run the widget's own config screen), and the
 * last two need an Activity. That part lives in [WidgetPickerActivity]; this class does the rest.
 */
class WidgetHostManager private constructor(private val app: Context) {
    private val manager: AppWidgetManager = AppWidgetManager.getInstance(app)
    val host: AppWidgetHost = AppWidgetHost(app, HOST_ID)
    private val density: Float get() = app.resources.displayMetrics.density

    /** Call once from the overlay service's onCreate. Without it widgets never update. */
    fun startListening() { try { host.startListening() } catch (e: Exception) { /* system service died */ } }
    fun stopListening() { try { host.stopListening() } catch (e: Exception) { } }

    fun allocateId(): Int = host.allocateAppWidgetId()
    fun deleteId(id: Int) { try { host.deleteAppWidgetId(id) } catch (e: Exception) { } }
    fun info(id: Int): AppWidgetProviderInfo? = try { manager.getAppWidgetInfo(id) } catch (e: Exception) { null }
    fun bindIfAllowed(id: Int, provider: ComponentName): Boolean =
        try { manager.bindAppWidgetIdIfAllowed(id, provider) } catch (e: Exception) { false }

    /** Never throws: a broken provider must not take the Deck down. */
    fun createView(context: Context, id: Int): AppWidgetHostView? {
        val info = info(id) ?: return null
        return try {
            host.createView(context, id, info).apply { setPadding(0, 0, 0, 0) }
        } catch (e: Exception) {
            null
        }
    }

    fun providers(): List<ProviderRow> {
        val pm = app.packageManager
        val out = ArrayList<ProviderRow>()
        for (info in manager.installedProviders) {
            if (info.widgetCategory and AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN == 0) continue
            try {
                val appLabel = pm.getApplicationLabel(pm.getApplicationInfo(info.provider.packageName, 0)).toString()
                out.add(ProviderRow(info.provider, info.loadLabel(pm), appLabel, info))
            } catch (e: Exception) { /* package vanished mid-scan */ }
        }
        return out.sortedWith(compareBy<ProviderRow>({ it.appLabel.lowercase() }, { it.label.lowercase() }))
    }

    /** Default size and resize limits, in grid cells, for a provider at the current cell size. */
    fun sizing(info: AppWidgetProviderInfo, cellDp: Float): Sizing {
        // AppWidgetProviderInfo sizes are documented as dp but the framework hands back pixels.
        fun dp(px: Int) = px / density
        val canH = info.resizeMode and AppWidgetProviderInfo.RESIZE_HORIZONTAL != 0
        val canV = info.resizeMode and AppWidgetProviderInfo.RESIZE_VERTICAL != 0
        return WidgetSizing.compute(
            defWdp = dp(info.minWidth), defHdp = dp(info.minHeight),
            minWdp = dp(if (info.minResizeWidth > 0) info.minResizeWidth else info.minWidth),
            minHdp = dp(if (info.minResizeHeight > 0) info.minResizeHeight else info.minHeight),
            maxWdp = dp(info.maxResizeWidth), maxHdp = dp(info.maxResizeHeight),
            canResizeH = canH, canResizeV = canV,
            cellDp = cellDp, gapDp = DeckGrid.GAP_DP, cols = DeckGrid.COLS,
        )
    }

    fun limitsFor(appWidgetId: Int, cellDp: Float): Limits? =
        info(appWidgetId)?.let { sizing(it, cellDp).limits }

    companion object {
        private const val HOST_ID = 0x4D44
        @Volatile private var instance: WidgetHostManager? = null
        fun get(context: Context): WidgetHostManager =
            instance ?: synchronized(this) {
                instance ?: WidgetHostManager(context.applicationContext).also { instance = it }
            }
    }
}
