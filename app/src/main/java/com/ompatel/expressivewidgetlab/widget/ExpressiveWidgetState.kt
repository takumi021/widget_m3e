package com.ompatel.expressivewidgetlab.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class WidgetRefreshSource(
    val storageValue: String,
    val label: String,
) {
    PERIODIC("periodic", "Auto"),
    TAP("tap", "Tap"),
    RECEIVER("receiver", "Place"),
    APP("app", "App");

    companion object {
        fun fromStorage(value: String?): WidgetRefreshSource {
            return entries.firstOrNull { it.storageValue == value } ?: PERIODIC
        }
    }
}

data class ExpressiveWidgetUiState(
    val timeText: String,
    val meridiemText: String,
    val dateText: String,
    val lastUpdatedText: String,
    val refreshSourceLabel: String,
)

object ExpressiveWidgetState {
    private val lastUpdatedKey = longPreferencesKey("last_updated_epoch_millis")
    private val refreshSourceKey = stringPreferencesKey("refresh_source")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.getDefault())
    private val meridiemFormatter = DateTimeFormatter.ofPattern("a", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    private val shortTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    suspend fun markWidgetRefreshed(
        context: Context,
        glanceId: GlanceId,
        source: WidgetRefreshSource,
        instant: Instant = Instant.now(),
    ) {
        updateAppWidgetState(context, glanceId) { preferences: MutablePreferences ->
            preferences[lastUpdatedKey] = instant.toEpochMilli()
            preferences[refreshSourceKey] = source.storageValue
        }
    }

    suspend fun markAllWidgetsRefreshed(
        context: Context,
        source: WidgetRefreshSource,
        instant: Instant = Instant.now(),
    ) {
        val manager = GlanceAppWidgetManager(context)
        val widget = ExpressiveClockWidget()
        manager.getGlanceIds(widget.javaClass).forEach { glanceId ->
            markWidgetRefreshed(
                context = context,
                glanceId = glanceId,
                source = source,
                instant = instant,
            )
        }
    }

    fun snapshot(
        preferences: Preferences,
        clock: Clock = Clock.systemDefaultZone(),
        zoneId: ZoneId = clock.zone,
    ): ExpressiveWidgetUiState {
        val now = Instant.now(clock).atZone(zoneId)
        val lastUpdated = Instant.ofEpochMilli(
            preferences[lastUpdatedKey] ?: now.toInstant().toEpochMilli(),
        ).atZone(zoneId)
        val source = WidgetRefreshSource.fromStorage(preferences[refreshSourceKey])

        return ExpressiveWidgetUiState(
            timeText = now.format(timeFormatter),
            meridiemText = now.format(meridiemFormatter),
            dateText = now.format(dateFormatter),
            lastUpdatedText = "Updated ${lastUpdated.format(shortTimeFormatter)}",
            refreshSourceLabel = source.label,
        )
    }
}
