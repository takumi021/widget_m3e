package com.ompatel.expressivewidgetlab.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.ompatel.expressivewidgetlab.health.SamsungHealthMetricSnapshot

data class SamsungHealthWidgetUiState(
    val steps: String,
    val heartRate: String,
    val sleep: String,
    val energyScore: String,
    val status: String,
    val isConnected: Boolean,
)

object SamsungHealthWidgetState {
    private val stepsKey = stringPreferencesKey("samsung_health_steps")
    private val heartRateKey = stringPreferencesKey("samsung_health_heart_rate")
    private val sleepKey = stringPreferencesKey("samsung_health_sleep")
    private val energyScoreKey = stringPreferencesKey("samsung_health_energy_score")
    private val statusKey = stringPreferencesKey("samsung_health_status")
    private val connectedKey = booleanPreferencesKey("samsung_health_connected")

    suspend fun writeSnapshot(
        context: Context,
        snapshot: SamsungHealthMetricSnapshot,
    ) {
        val manager = GlanceAppWidgetManager(context)
        val widget = SamsungHealthWidget()
        manager.getGlanceIds(widget.javaClass).forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { preferences: MutablePreferences ->
                preferences[stepsKey] = snapshot.steps
                preferences[heartRateKey] = snapshot.heartRate
                preferences[sleepKey] = snapshot.sleep
                preferences[energyScoreKey] = snapshot.energyScore
                preferences[statusKey] = snapshot.status
                preferences[connectedKey] = snapshot.isConnected
            }
        }
    }

    fun snapshot(preferences: Preferences): SamsungHealthWidgetUiState {
        return SamsungHealthWidgetUiState(
            steps = preferences[stepsKey] ?: "--",
            heartRate = preferences[heartRateKey] ?: "--",
            sleep = preferences[sleepKey] ?: "--",
            energyScore = preferences[energyScoreKey] ?: "--",
            status = preferences[statusKey] ?: "Tap to refresh after Samsung Health setup.",
            isConnected = preferences[connectedKey] ?: false,
        )
    }
}
