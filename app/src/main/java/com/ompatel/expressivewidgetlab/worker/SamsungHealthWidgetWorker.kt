package com.ompatel.expressivewidgetlab.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ompatel.expressivewidgetlab.health.SamsungHealthRepository
import com.ompatel.expressivewidgetlab.widget.SamsungHealthWidget
import com.ompatel.expressivewidgetlab.widget.SamsungHealthWidgetState
import java.util.concurrent.TimeUnit

class SamsungHealthWidgetWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            val snapshot = SamsungHealthRepository(applicationContext).loadSnapshot()
            SamsungHealthWidgetState.writeSnapshot(applicationContext, snapshot)
            SamsungHealthWidget.refreshAll(applicationContext)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        private const val PeriodicWorkName = "samsung-health-widget-periodic-refresh"
        private const val ImmediateWorkName = "samsung-health-widget-immediate-refresh"

        fun ensureScheduled(context: Context) {
            val request = PeriodicWorkRequestBuilder<SamsungHealthWidgetWorker>(
                repeatInterval = 30,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PeriodicWorkName,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun enqueueImmediateRefresh(context: Context) {
            val request = OneTimeWorkRequestBuilder<SamsungHealthWidgetWorker>().build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ImmediateWorkName,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun cancelScheduled(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PeriodicWorkName)
            WorkManager.getInstance(context).cancelUniqueWork(ImmediateWorkName)
        }
    }
}
