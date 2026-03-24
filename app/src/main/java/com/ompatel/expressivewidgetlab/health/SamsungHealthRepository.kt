package com.ompatel.expressivewidgetlab.health

import android.app.Activity
import android.content.Context
import android.os.Build
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.error.ResolvablePlatformException
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalDateFilter
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.LocalTimeGroup
import com.samsung.android.sdk.health.data.request.LocalTimeGroupUnit
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

data class SamsungHealthMetricSnapshot(
    val steps: String,
    val heartRate: String,
    val sleep: String,
    val energyScore: String,
    val status: String,
    val isConnected: Boolean,
)

data class SamsungHealthPermissionState(
    val isReady: Boolean,
    val message: String,
)

class SamsungHealthRepository(
    private val context: Context,
) {
    suspend fun ensureReadPermissions(activity: Activity): SamsungHealthPermissionState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return SamsungHealthPermissionState(
                isReady = false,
                message = "Samsung Health Data SDK requires Android 10 or newer.",
            )
        }

        return try {
            val store = HealthDataService.getStore(context)
            val granted = store.getGrantedPermissions(requiredPermissions)
            if (granted.containsAll(requiredPermissions)) {
                SamsungHealthPermissionState(
                    isReady = true,
                    message = "Samsung Health access is connected.",
                )
            } else {
                val requested = store.requestPermissions(requiredPermissions, activity)
                if (requested.containsAll(requiredPermissions)) {
                    SamsungHealthPermissionState(
                        isReady = true,
                        message = "Samsung Health permissions granted.",
                    )
                } else {
                    SamsungHealthPermissionState(
                        isReady = false,
                        message = "Samsung Health permissions were not fully granted.",
                    )
                }
            }
        } catch (error: HealthDataException) {
            if (error is ResolvablePlatformException && error.hasResolution) {
                error.resolve(activity)
                SamsungHealthPermissionState(
                    isReady = false,
                    message = "Complete Samsung Health setup, then try again.",
                )
            } else {
                SamsungHealthPermissionState(
                    isReady = false,
                    message = error.errorMessage.ifBlank { "Samsung Health setup failed." },
                )
            }
        }
    }

    suspend fun loadSnapshot(): SamsungHealthMetricSnapshot {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return unavailableSnapshot("Requires Android 10+.")
        }

        return try {
            val store = HealthDataService.getStore(context)
            val granted = store.getGrantedPermissions(requiredPermissions)
            if (!granted.containsAll(requiredPermissions)) {
                return unavailableSnapshot("Open the app and connect Samsung Health.")
            }

            SamsungHealthMetricSnapshot(
                steps = readSteps(store),
                heartRate = readHeartRate(store),
                sleep = readSleep(store),
                energyScore = readEnergyScore(store),
                status = "Connected",
                isConnected = true,
            )
        } catch (error: HealthDataException) {
            unavailableSnapshot(error.errorMessage.ifBlank { "Samsung Health read failed." })
        } catch (_: Throwable) {
            unavailableSnapshot("Samsung Health read failed.")
        }
    }

    private suspend fun readSteps(store: com.samsung.android.sdk.health.data.HealthDataStore): String {
        val now = LocalDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay()
        val request = DataType.StepsType.TOTAL.requestBuilder
            .setLocalTimeFilterWithGroup(
                LocalTimeFilter.of(startOfDay, now),
                LocalTimeGroup.of(LocalTimeGroupUnit.HOURLY, 1),
            )
            .build()
        val totalSteps = store.aggregateData(request).dataList.sumOf { it.value ?: 0L }
        return formatSteps(totalSteps)
    }

    private suspend fun readHeartRate(store: com.samsung.android.sdk.health.data.HealthDataStore): String {
        val now = LocalDateTime.now()
        val start = now.minusDays(2)
        val request = DataTypes.HEART_RATE.readDataRequestBuilder
            .setLocalTimeFilter(LocalTimeFilter.of(start, now))
            .setOrdering(Ordering.DESC)
            .build()
        val latest = store.readData(request).dataList.firstOrNull()
        val bpm = latest?.getValue(DataType.HeartRateType.HEART_RATE) ?: return "--"
        return bpm.toInt().toString()
    }

    private suspend fun readSleep(store: com.samsung.android.sdk.health.data.HealthDataStore): String {
        val today = LocalDate.now()
        val request = DataType.SleepType.TOTAL_DURATION.requestBuilder
            .setLocalDateFilter(LocalDateFilter.of(today, today))
            .build()
        val duration = store.aggregateData(request).dataList.lastOrNull()?.value
            ?: return "0h 00m"
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
    }

    private suspend fun readEnergyScore(store: com.samsung.android.sdk.health.data.HealthDataStore): String {
        val today = LocalDate.now()
        val request = DataTypes.ENERGY_SCORE.readDataRequestBuilder
            .setLocalDateFilter(LocalDateFilter.of(today, today))
            .setOrdering(Ordering.DESC)
            .build()
        val latest = store.readData(request).dataList.firstOrNull() ?: return "0"
        val score = latest.getValue(DataType.EnergyScoreType.ENERGY_SCORE) ?: 0f
        return score.toInt().toString()
    }

    private fun formatSteps(totalSteps: Long): String {
        return when {
            totalSteps >= 10_000 -> String.format(Locale.getDefault(), "%.1fk", totalSteps / 1000f)
            totalSteps >= 1_000 -> String.format(Locale.getDefault(), "%.1fk", totalSteps / 1000f)
            else -> totalSteps.toString()
        }
    }

    private fun unavailableSnapshot(message: String): SamsungHealthMetricSnapshot {
        return SamsungHealthMetricSnapshot(
            steps = "--",
            heartRate = "--",
            sleep = "--",
            energyScore = "--",
            status = message,
            isConnected = false,
        )
    }

    private companion object {
        val requiredPermissions = setOf(
            Permission.of(DataTypes.STEPS, AccessType.READ),
            Permission.of(DataTypes.HEART_RATE, AccessType.READ),
            Permission.of(DataTypes.SLEEP, AccessType.READ),
            Permission.of(DataTypes.ENERGY_SCORE, AccessType.READ),
        )
    }
}
