package com.ompatel.expressivewidgetlab.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.ompatel.expressivewidgetlab.worker.WidgetUpdateWorker

class ExpressiveWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: ExpressiveClockWidget = ExpressiveClockWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.ensureClockSchedules(context)
        WidgetUpdateWorker.enqueueImmediateRefresh(context, WidgetRefreshSource.RECEIVER)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateWorker.ensureClockSchedules(context)
        WidgetUpdateWorker.enqueueImmediateRefresh(context, WidgetRefreshSource.RECEIVER)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateWorker.cancelClockSchedules(context)
    }
}
