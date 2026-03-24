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
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import androidx.glance.state.PreferencesGlanceStateDefinition
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
    val compact = widgetSize.width < 220.dp || widgetSize.height < 180.dp
    val contentPadding = if (compact) 10.dp else 12.dp
    val sectionGap = if (compact) 6.dp else 8.dp
    val showStatus = !uiState.isConnected

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
                leading = MetricBlock(
                    label = "Steps",
                    value = uiState.steps,
                    unit = "",
                    tone = metricToneForSteps(uiState.steps),
                ),
                trailing = MetricBlock(
                    label = "Heart",
                    value = uiState.heartRate,
                    unit = "bpm",
                    tone = metricToneForHeart(uiState.heartRate),
                ),
                compact = compact,
            )

            Spacer(modifier = GlanceModifier.height(sectionGap))

            HealthMetricRow(
                leading = MetricBlock(
                    label = "Sleep",
                    value = uiState.sleep,
                    unit = "",
                    tone = metricToneForSleep(uiState.sleep),
                ),
                trailing = MetricBlock(
                    label = "Energy",
                    value = uiState.energyScore,
                    unit = "",
                    tone = metricToneForEnergy(uiState.energyScore),
                ),
                compact = compact,
            )

            if (showStatus) {
                Spacer(modifier = GlanceModifier.height(sectionGap))

                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
                        .background(GlanceTheme.colors.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
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
}

@Composable
private fun HealthMetricRow(
    leading: MetricBlock,
    trailing: MetricBlock,
    compact: Boolean,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
    ) {
        MetricCard(
            modifier = GlanceModifier.defaultWeight(),
            metric = leading,
            compact = compact,
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        MetricCard(
            modifier = GlanceModifier.defaultWeight(),
            metric = trailing,
            compact = compact,
        )
    }
}

@Composable
private fun MetricCard(
    modifier: GlanceModifier,
    metric: MetricBlock,
    compact: Boolean,
) {
    Box(
        modifier = modifier
            .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
            .background(metric.tone.container)
            .padding(horizontal = 10.dp, vertical = if (compact) 8.dp else 10.dp),
    ) {
        Column {
            Text(
                text = metric.label,
                style = ExpressiveWidgetTheme.compactStatusStyle(
                    color = metric.tone.onContainer,
                ),
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Vertical.Bottom,
            ) {
                Text(
                    text = metric.value,
                    style = ExpressiveWidgetTheme.healthMetricValueStyle(
                        compact = compact,
                        color = metric.tone.onContainer,
                    ),
                )
                if (metric.unit.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = metric.unit,
                        style = ExpressiveWidgetTheme.compactStatusStyle(
                            color = metric.tone.onContainer,
                        ),
                    )
                }
            }
        }
    }
}

private data class MetricBlock(
    val label: String,
    val value: String,
    val unit: String,
    val tone: MetricTone,
)

private data class MetricTone(
    val container: ColorProvider,
    val onContainer: ColorProvider,
)

@Composable
private fun metricToneForHeart(value: String): MetricTone {
    val bpm = value.toIntOrNull()
    return when {
        bpm == null -> MetricTone(GlanceTheme.colors.surfaceVariant, GlanceTheme.colors.onSurfaceVariant)
        bpm >= 110 -> MetricTone(GlanceTheme.colors.errorContainer, GlanceTheme.colors.onErrorContainer)
        bpm >= 95 -> MetricTone(GlanceTheme.colors.tertiaryContainer, GlanceTheme.colors.onTertiaryContainer)
        bpm in 55..94 -> MetricTone(GlanceTheme.colors.secondaryContainer, GlanceTheme.colors.onSecondaryContainer)
        else -> MetricTone(GlanceTheme.colors.primaryContainer, GlanceTheme.colors.onPrimaryContainer)
    }
}

@Composable
private fun metricToneForSteps(value: String): MetricTone {
    val steps = parseSteps(value)
    return when {
        steps >= 10_000 -> MetricTone(GlanceTheme.colors.secondaryContainer, GlanceTheme.colors.onSecondaryContainer)
        steps >= 6_000 -> MetricTone(GlanceTheme.colors.primaryContainer, GlanceTheme.colors.onPrimaryContainer)
        steps >= 3_000 -> MetricTone(GlanceTheme.colors.tertiaryContainer, GlanceTheme.colors.onTertiaryContainer)
        steps > 0 -> MetricTone(GlanceTheme.colors.surfaceVariant, GlanceTheme.colors.onSurfaceVariant)
        else -> MetricTone(GlanceTheme.colors.surfaceVariant, GlanceTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun metricToneForSleep(value: String): MetricTone {
    val hours = parseSleepHours(value)
    return when {
        hours == null -> MetricTone(GlanceTheme.colors.surfaceVariant, GlanceTheme.colors.onSurfaceVariant)
        hours >= 7.0 -> MetricTone(GlanceTheme.colors.secondaryContainer, GlanceTheme.colors.onSecondaryContainer)
        hours >= 6.0 -> MetricTone(GlanceTheme.colors.primaryContainer, GlanceTheme.colors.onPrimaryContainer)
        hours >= 5.0 -> MetricTone(GlanceTheme.colors.tertiaryContainer, GlanceTheme.colors.onTertiaryContainer)
        else -> MetricTone(GlanceTheme.colors.errorContainer, GlanceTheme.colors.onErrorContainer)
    }
}

@Composable
private fun metricToneForEnergy(value: String): MetricTone {
    val score = value.toIntOrNull()
    return when {
        score == null -> MetricTone(GlanceTheme.colors.surfaceVariant, GlanceTheme.colors.onSurfaceVariant)
        score >= 80 -> MetricTone(GlanceTheme.colors.secondaryContainer, GlanceTheme.colors.onSecondaryContainer)
        score >= 60 -> MetricTone(GlanceTheme.colors.primaryContainer, GlanceTheme.colors.onPrimaryContainer)
        score >= 40 -> MetricTone(GlanceTheme.colors.tertiaryContainer, GlanceTheme.colors.onTertiaryContainer)
        else -> MetricTone(GlanceTheme.colors.errorContainer, GlanceTheme.colors.onErrorContainer)
    }
}

private fun parseSteps(value: String): Int {
    val trimmed = value.lowercase()
    return when {
        trimmed.endsWith("k") -> ((trimmed.removeSuffix("k").toFloatOrNull() ?: 0f) * 1000).toInt()
        else -> trimmed.replace(",", "").toIntOrNull() ?: 0
    }
}

private fun parseSleepHours(value: String): Double? {
    val hourPart = Regex("(\\d+)h").find(value)?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: return null
    val minutePart = Regex("(\\d+)m").find(value)?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    return hourPart + (minutePart / 60.0)
}
