package com.ompatel.expressivewidgetlab.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.ompatel.expressivewidgetlab.health.SamsungHealthRepository
import com.ompatel.expressivewidgetlab.worker.SamsungHealthWidgetWorker

class SamsungHealthWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
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
        val snapshot = SamsungHealthRepository(context).loadSnapshot()
        SamsungHealthWidgetState.writeSnapshot(context, snapshot)
        SamsungHealthWidget().update(context, glanceId)
    }
}

@Composable
private fun SamsungHealthWidgetContent(
    uiState: SamsungHealthWidgetUiState,
) {
    val widgetSize = LocalSize.current
    val layout = healthWidgetLayoutSpec(
        width = widgetSize.width,
        height = widgetSize.height,
    )
    val showStatus = !uiState.isConnected
    val metrics = listOf(
        MetricBlock(
            label = "Steps",
            value = uiState.steps,
            unit = "",
            tone = metricToneForSteps(uiState.steps),
        ),
        MetricBlock(
            label = "Heart",
            value = uiState.heartRate,
            unit = "bpm",
            tone = metricToneForHeart(uiState.heartRate),
        ),
        MetricBlock(
            label = "Sleep",
            value = uiState.sleep,
            unit = "",
            tone = metricToneForSleep(uiState.sleep),
        ),
        MetricBlock(
            label = "Energy",
            value = uiState.energyScore,
            unit = "",
            tone = metricToneForEnergy(uiState.energyScore),
        ),
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(ExpressiveWidgetTheme.OuterCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionRunCallback<RefreshSamsungHealthAction>())
            .padding(layout.outerPadding),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
        ) {
            val metricsModifier = if (showStatus) {
                GlanceModifier.defaultWeight()
            } else {
                GlanceModifier.fillMaxSize()
            }

            when (layout.profile) {
                HealthWidgetLayoutProfile.Ribbon -> HealthMetricRibbon(
                    metrics = metrics,
                    layout = layout,
                    modifier = metricsModifier,
                )
                HealthWidgetLayoutProfile.TallList -> HealthMetricTallList(
                    metrics = metrics,
                    layout = layout,
                    modifier = metricsModifier,
                )
                HealthWidgetLayoutProfile.CompactGrid,
                HealthWidgetLayoutProfile.BalancedGrid,
                HealthWidgetLayoutProfile.RoomyGrid,
                -> HealthMetricGrid(
                    metrics = metrics,
                    layout = layout,
                    modifier = metricsModifier,
                )
            }

            if (showStatus) {
                Spacer(modifier = GlanceModifier.height(layout.cardGap))
                StatusCard(
                    message = uiState.status,
                    layout = layout,
                )
            }
        }
    }
}

private enum class HealthWidgetLayoutProfile {
    Ribbon,
    TallList,
    CompactGrid,
    BalancedGrid,
    RoomyGrid,
}

private data class HealthWidgetLayoutSpec(
    val profile: HealthWidgetLayoutProfile,
    val sizeClass: ExpressiveWidgetTheme.HealthMetricSizeClass,
    val outerPadding: Dp,
    val cardGap: Dp,
    val cardPaddingHorizontal: Dp,
    val cardPaddingVertical: Dp,
    val labelValueGap: Dp,
    val unitGap: Dp,
    val statusPaddingHorizontal: Dp,
    val statusPaddingVertical: Dp,
)

private fun healthWidgetLayoutSpec(
    width: Dp,
    height: Dp,
): HealthWidgetLayoutSpec {
    val ribbon = width >= 270.dp && height < 172.dp
    val tall = width < 182.dp && height >= 205.dp
    val roomy = width >= 320.dp && height >= 230.dp
    val compactGrid = width < 215.dp || height < 185.dp
    val sizeClass = when {
        width < 172.dp || height < 132.dp -> ExpressiveWidgetTheme.HealthMetricSizeClass.Dense
        width < 230.dp || height < 180.dp -> ExpressiveWidgetTheme.HealthMetricSizeClass.Compact
        roomy -> ExpressiveWidgetTheme.HealthMetricSizeClass.Roomy
        else -> ExpressiveWidgetTheme.HealthMetricSizeClass.Regular
    }
    val profile = when {
        ribbon -> HealthWidgetLayoutProfile.Ribbon
        tall -> HealthWidgetLayoutProfile.TallList
        roomy -> HealthWidgetLayoutProfile.RoomyGrid
        compactGrid -> HealthWidgetLayoutProfile.CompactGrid
        else -> HealthWidgetLayoutProfile.BalancedGrid
    }

    return when (sizeClass) {
        ExpressiveWidgetTheme.HealthMetricSizeClass.Dense -> HealthWidgetLayoutSpec(
            profile = profile,
            sizeClass = sizeClass,
            outerPadding = if (profile == HealthWidgetLayoutProfile.Ribbon) 8.dp else 7.dp,
            cardGap = 5.dp,
            cardPaddingHorizontal = 8.dp,
            cardPaddingVertical = 6.dp,
            labelValueGap = 2.dp,
            unitGap = 2.dp,
            statusPaddingHorizontal = 8.dp,
            statusPaddingVertical = 5.dp,
        )
        ExpressiveWidgetTheme.HealthMetricSizeClass.Compact -> HealthWidgetLayoutSpec(
            profile = profile,
            sizeClass = sizeClass,
            outerPadding = if (profile == HealthWidgetLayoutProfile.Ribbon) 9.dp else 8.dp,
            cardGap = 6.dp,
            cardPaddingHorizontal = 9.dp,
            cardPaddingVertical = 7.dp,
            labelValueGap = 3.dp,
            unitGap = 3.dp,
            statusPaddingHorizontal = 9.dp,
            statusPaddingVertical = 6.dp,
        )
        ExpressiveWidgetTheme.HealthMetricSizeClass.Regular -> HealthWidgetLayoutSpec(
            profile = profile,
            sizeClass = sizeClass,
            outerPadding = 10.dp,
            cardGap = 8.dp,
            cardPaddingHorizontal = 10.dp,
            cardPaddingVertical = 9.dp,
            labelValueGap = 4.dp,
            unitGap = 4.dp,
            statusPaddingHorizontal = 10.dp,
            statusPaddingVertical = 7.dp,
        )
        ExpressiveWidgetTheme.HealthMetricSizeClass.Roomy -> HealthWidgetLayoutSpec(
            profile = profile,
            sizeClass = sizeClass,
            outerPadding = 12.dp,
            cardGap = 10.dp,
            cardPaddingHorizontal = 12.dp,
            cardPaddingVertical = 12.dp,
            labelValueGap = 5.dp,
            unitGap = 4.dp,
            statusPaddingHorizontal = 12.dp,
            statusPaddingVertical = 8.dp,
        )
    }
}

@Composable
private fun HealthMetricRibbon(
    metrics: List<MetricBlock>,
    layout: HealthWidgetLayoutSpec,
    modifier: GlanceModifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        metrics.forEachIndexed { index, metric ->
            MetricCard(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight(),
                metric = metric,
                layout = layout,
                dense = true,
            )
            if (index != metrics.lastIndex) {
                Spacer(modifier = GlanceModifier.width(layout.cardGap))
            }
        }
    }
}

@Composable
private fun HealthMetricGrid(
    metrics: List<MetricBlock>,
    layout: HealthWidgetLayoutSpec,
    modifier: GlanceModifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        HealthMetricRow(
            modifier = GlanceModifier.defaultWeight(),
            leading = metrics[0],
            trailing = metrics[1],
            layout = layout,
        )

        Spacer(modifier = GlanceModifier.height(layout.cardGap))

        HealthMetricRow(
            modifier = GlanceModifier.defaultWeight(),
            leading = metrics[2],
            trailing = metrics[3],
            layout = layout,
        )
    }
}

@Composable
private fun HealthMetricRow(
    modifier: GlanceModifier,
    leading: MetricBlock,
    trailing: MetricBlock,
    layout: HealthWidgetLayoutSpec,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        MetricCard(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight(),
            metric = leading,
            layout = layout,
            dense = layout.sizeClass != ExpressiveWidgetTheme.HealthMetricSizeClass.Roomy,
        )
        Spacer(modifier = GlanceModifier.width(layout.cardGap))
        MetricCard(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight(),
            metric = trailing,
            layout = layout,
            dense = layout.sizeClass != ExpressiveWidgetTheme.HealthMetricSizeClass.Roomy,
        )
    }
}

@Composable
private fun HealthMetricTallList(
    metrics: List<MetricBlock>,
    layout: HealthWidgetLayoutSpec,
    modifier: GlanceModifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        metrics.forEachIndexed { index, metric ->
            MetricCard(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxWidth(),
                metric = metric,
                layout = layout,
                dense = true,
            )
            if (index != metrics.lastIndex) {
                Spacer(modifier = GlanceModifier.height(layout.cardGap))
            }
        }
    }
}

@Composable
private fun StatusCard(
    message: String,
    layout: HealthWidgetLayoutSpec,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(
                horizontal = layout.statusPaddingHorizontal,
                vertical = layout.statusPaddingVertical,
            ),
    ) {
        Text(
            text = message,
            style = ExpressiveWidgetTheme.compactStatusStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun MetricCard(
    modifier: GlanceModifier,
    metric: MetricBlock,
    layout: HealthWidgetLayoutSpec,
    dense: Boolean,
) {
    val verticalPadding = when {
        dense && layout.sizeClass == ExpressiveWidgetTheme.HealthMetricSizeClass.Dense -> layout.cardPaddingVertical
        dense -> layout.cardPaddingVertical
        else -> layout.cardPaddingVertical + 1.dp
    }
    Box(
        modifier = modifier
            .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
            .background(metric.tone.container)
            .padding(
                horizontal = layout.cardPaddingHorizontal,
                vertical = verticalPadding,
            ),
    ) {
        Column {
            Text(
                text = metric.label,
                style = ExpressiveWidgetTheme.healthMetricLabelStyle(
                    sizeClass = layout.sizeClass,
                    color = metric.tone.onContainer,
                ),
            )
            Spacer(modifier = GlanceModifier.height(layout.labelValueGap))
            Row(
                verticalAlignment = Alignment.Vertical.Bottom,
            ) {
                Text(
                    text = metric.value,
                    style = ExpressiveWidgetTheme.healthMetricValueStyle(
                        sizeClass = layout.sizeClass,
                        color = metric.tone.onContainer,
                    ),
                )
                if (metric.unit.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.width(layout.unitGap))
                    Text(
                        text = metric.unit,
                        style = ExpressiveWidgetTheme.healthMetricLabelStyle(
                            sizeClass = layout.sizeClass,
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
