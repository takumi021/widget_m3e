package com.ompatel.expressivewidgetlab.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.ompatel.expressivewidgetlab.worker.SamsungHealthWidgetWorker

class SamsungHealthWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ExpressiveWidgetTheme.GlanceSurface {
                val preferences = currentState<Preferences>()
                val uiState = SamsungHealthWidgetState.snapshot(preferences)
                SamsungHealthWidgetContent(uiState)
            }
        }
    }

    companion object {
        suspend fun refreshAll(context: Context) {
            SamsungHealthWidget().updateAll(context)
        }
    }
}

class SamsungHealthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SamsungHealthWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        SamsungHealthWidgetWorker.ensureScheduled(context)
        SamsungHealthWidgetWorker.enqueueImmediateRefresh(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        SamsungHealthWidgetWorker.cancelScheduled(context)
    }
}

private class RefreshSamsungHealthAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        SamsungHealthWidgetWorker.enqueueImmediateRefresh(context)
        SamsungHealthWidget().update(context, glanceId)
    }
}

@Composable
private fun SamsungHealthWidgetContent(
    uiState: SamsungHealthWidgetUiState,
) {
    val widgetSize = LocalSize.current
    val compact = widgetSize.width < 220.dp || widgetSize.height < 200.dp
    val contentPadding = if (compact) 12.dp else 16.dp
    val sectionGap = if (compact) 8.dp else 10.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(ExpressiveWidgetTheme.OuterCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionRunCallback<RefreshSamsungHealthAction>())
            .padding(contentPadding),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            HealthMetricRow(
                leading = MetricBlock("Steps", uiState.steps),
                trailing = MetricBlock("Heart", uiState.heartRate),
            )

            Spacer(modifier = GlanceModifier.height(sectionGap))

            HealthMetricRow(
                leading = MetricBlock("Sleep", uiState.sleep),
                trailing = MetricBlock("Stress", uiState.stress),
            )

            Spacer(modifier = GlanceModifier.height(sectionGap))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
                    .background(GlanceTheme.colors.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = uiState.status,
                    style = ExpressiveWidgetTheme.compactStatusStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HealthMetricRow(
    leading: MetricBlock,
    trailing: MetricBlock,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
    ) {
        MetricCard(
            modifier = GlanceModifier.defaultWeight(),
            metric = leading,
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        MetricCard(
            modifier = GlanceModifier.defaultWeight(),
            metric = trailing,
        )
    }
}

@Composable
private fun MetricCard(
    modifier: GlanceModifier,
    metric: MetricBlock,
) {
    Box(
        modifier = modifier
            .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column {
            Text(
                text = metric.label,
                style = ExpressiveWidgetTheme.compactStatusStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = metric.value,
                style = ExpressiveWidgetTheme.healthMetricValueStyle(
                    color = GlanceTheme.colors.onSurface,
                ),
            )
        }
    }
}

private data class MetricBlock(
    val label: String,
    val value: String,
)
