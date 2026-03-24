package com.ompatel.expressivewidgetlab.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ompatel.expressivewidgetlab.worker.WidgetUpdateWorker

class WidgetTimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetUpdateWorker.ensureClockSchedules(context)
        WidgetUpdateWorker.enqueueImmediateRefresh(
            context = context,
            source = WidgetRefreshSource.SYSTEM,
        )
    }
}
