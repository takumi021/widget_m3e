package com.ompatel.expressivewidgetlab.health

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

data class SamsungHealthMetricSnapshot(
    val steps: String,
    val heartRate: String,
    val sleep: String,
    val stress: String,
    val status: String,
)

class SamsungHealthRepository(
    private val context: Context,
) {
    suspend fun loadSnapshot(): SamsungHealthMetricSnapshot {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return SamsungHealthMetricSnapshot(
                steps = "--",
                heartRate = "--",
                sleep = "--",
                stress = "N/A",
                status = "Requires Android 10+ for Samsung Health Data SDK.",
            )
        }

        if (!isPackageInstalled(SamsungHealthPackageName)) {
            return SamsungHealthMetricSnapshot(
                steps = "--",
                heartRate = "--",
                sleep = "--",
                stress = "N/A",
                status = "Install Samsung Health 6.30.2 or newer.",
            )
        }

        if (!isClassPresent(SamsungHealthDataStoreClass)) {
            return SamsungHealthMetricSnapshot(
                steps = "--",
                heartRate = "--",
                sleep = "--",
                stress = "N/A",
                status = "Add samsung-health-data-api.aar to app/libs to enable live reads.",
            )
        }

        // Samsung's current documented data types cover steps, heart rate, and sleep.
        // Stress is not listed, so the widget surfaces it as unavailable for now.
        return SamsungHealthMetricSnapshot(
            steps = "--",
            heartRate = "--",
            sleep = "--",
            stress = "N/A",
            status = "Samsung Health SDK detected. Wire permissioned queries next.",
        )
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        }.isSuccess
    }

    private fun isClassPresent(className: String): Boolean {
        return runCatching {
            Class.forName(className)
        }.isSuccess
    }

    private companion object {
        const val SamsungHealthPackageName = "com.sec.android.app.shealth"
        const val SamsungHealthDataStoreClass = "com.samsung.android.sdk.healthdata.HealthDataStore"
    }
}
