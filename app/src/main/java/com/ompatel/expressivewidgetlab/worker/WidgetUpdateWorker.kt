package com.ompatel.expressivewidgetlab.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ompatel.expressivewidgetlab.widget.ExpressiveClockWidget
import com.ompatel.expressivewidgetlab.widget.WidgetRefreshSource
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val source = WidgetRefreshSource.fromStorage(inputData.getString(KEY_REFRESH_SOURCE))
        val rescheduleMinuteWork = inputData.getBoolean(KEY_RESCHEDULE_MINUTE_WORK, false)

        return runCatching {
            if (ExpressiveClockWidget.hasInstances(applicationContext)) {
                ExpressiveClockWidget.refreshAll(
                    context = applicationContext,
                    source = source,
                )
            }

            if (rescheduleMinuteWork && ExpressiveClockWidget.hasInstances(applicationContext)) {
                enqueueNextMinuteRefresh(applicationContext)
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "expressive-clock-widget-periodic-refresh"
        private const val IMMEDIATE_WORK_NAME = "expressive-clock-widget-immediate-refresh"
        private const val MINUTE_WORK_NAME = "expressive-clock-widget-minute-refresh"
        private const val KEY_REFRESH_SOURCE = "refresh_source"
        private const val KEY_RESCHEDULE_MINUTE_WORK = "reschedule_minute_work"

        fun ensureClockSchedules(context: Context) {
            enqueuePeriodicWork(context)
            enqueueNextMinuteRefresh(context)
        }

        fun enqueuePeriodicWork(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            )
                .setInitialDelay(nextQuarterHourDelay())
                .setInputData(
                    workDataOf(KEY_REFRESH_SOURCE to WidgetRefreshSource.PERIODIC.storageValue),
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun enqueueNextMinuteRefresh(context: Context) {
            if (!ExpressiveClockWidget.hasInstances(context)) return

            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setInitialDelay(nextMinuteDelay())
                .setInputData(
                    workDataOf(
                        KEY_REFRESH_SOURCE to WidgetRefreshSource.SYSTEM.storageValue,
                        KEY_RESCHEDULE_MINUTE_WORK to true,
                    ),
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                MINUTE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun enqueueImmediateRefresh(
            context: Context,
            source: WidgetRefreshSource,
        ) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf(KEY_REFRESH_SOURCE to source.storageValue))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun cancelClockSchedules(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(MINUTE_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(IMMEDIATE_WORK_NAME)
        }

        private fun nextQuarterHourDelay(): Duration {
            val now = ZonedDateTime.now()
            val minutesUntilQuarter = ((15 - (now.minute % 15)) % 15).let { minutes ->
                if (minutes == 0) 15 else minutes
            }
            val next = now
                .plusMinutes(minutesUntilQuarter.toLong())
                .withSecond(0)
                .withNano(0)
            return Duration.between(now, next)
        }

        private fun nextMinuteDelay(): Duration {
            val now = ZonedDateTime.now()
            val next = now
                .plusMinutes(1)
                .withSecond(0)
                .withNano(0)
            return Duration.between(now, next)
        }
    }
}
