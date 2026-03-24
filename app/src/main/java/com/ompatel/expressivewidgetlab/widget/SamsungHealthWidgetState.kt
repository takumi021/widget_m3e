package com.ompatel.expressivewidgetlab.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.ompatel.expressivewidgetlab.health.SamsungHealthMetricSnapshot

data class SamsungHealthWidgetUiState(
    val steps: String,
    val heartRate: String,
    val sleep: String,
    val stress: String,
    val status: String,
)

object SamsungHealthWidgetState {
    private val stepsKey = stringPreferencesKey("samsung_health_steps")
    private val heartRateKey = stringPreferencesKey("samsung_health_heart_rate")
    private val sleepKey = stringPreferencesKey("samsung_health_sleep")
    private val stressKey = stringPreferencesKey("samsung_health_stress")
    private val statusKey = stringPreferencesKey("samsung_health_status")

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
                preferences[stressKey] = snapshot.stress
                preferences[statusKey] = snapshot.status
            }
        }
    }

    fun snapshot(preferences: Preferences): SamsungHealthWidgetUiState {
        return SamsungHealthWidgetUiState(
            steps = preferences[stepsKey] ?: "--",
            heartRate = preferences[heartRateKey] ?: "--",
            sleep = preferences[sleepKey] ?: "--",
            stress = preferences[stressKey] ?: "N/A",
            status = preferences[statusKey] ?: "Tap to refresh after Samsung Health setup.",
        )
    }
}
