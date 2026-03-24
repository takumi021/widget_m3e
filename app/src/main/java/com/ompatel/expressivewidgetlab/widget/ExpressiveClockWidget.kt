package com.ompatel.expressivewidgetlab.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import com.ompatel.expressivewidgetlab.R

class ExpressiveClockWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ExpressiveWidgetTheme.GlanceSurface {
                val preferences = currentState<Preferences>()
                val uiState = ExpressiveWidgetState.snapshot(preferences)
                ExpressiveClockWidgetContent(uiState)
            }
        }
    }

    companion object {
        suspend fun refreshAll(
            context: Context,
            source: WidgetRefreshSource,
        ) {
            ExpressiveWidgetState.markAllWidgetsRefreshed(context, source)
            ExpressiveClockWidget().updateAll(context)
        }

        fun hasInstances(context: Context): Boolean {
            return AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, ExpressiveWidgetReceiver::class.java))
                .isNotEmpty()
        }
    }
}

private class RefreshClockAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        ExpressiveWidgetState.markWidgetRefreshed(
            context = context,
            glanceId = glanceId,
            source = WidgetRefreshSource.TAP,
        )
        ExpressiveClockWidget().update(context, glanceId)
    }
}

@Composable
private fun ExpressiveClockWidgetContent(
    uiState: ExpressiveWidgetUiState,
) {
    val widgetSize = LocalSize.current
    val compact = widgetSize.width < 180.dp || widgetSize.height < 130.dp
    val roomy = widgetSize.width >= 260.dp && widgetSize.height >= 180.dp
    val horizontalLayout = widgetSize.width >= 300.dp && widgetSize.height >= 150.dp
    val iconSize = when {
        roomy -> 44.dp
        compact -> 32.dp
        else -> 40.dp
    }
    val iconInnerSize = when {
        roomy -> 24.dp
        compact -> 18.dp
        else -> 22.dp
    }
    val topSpacing = if (compact) 12.dp else 18.dp
    val dateSpacing = if (compact) 10.dp else 14.dp
    val titleSpacing = if (compact) 8.dp else ExpressiveWidgetTheme.ComfortableSpacing
    val contentPadding = if (compact) 14.dp else ExpressiveWidgetTheme.ContentPadding

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(ExpressiveWidgetTheme.OuterCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionRunCallback<RefreshClockAction>())
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start,
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(iconSize)
                        .cornerRadius(14.dp)
                        .background(GlanceTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_clock),
                        contentDescription = "Clock icon",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                        modifier = GlanceModifier.size(iconInnerSize),
                    )
                }

                Spacer(modifier = GlanceModifier.width(titleSpacing))

                Column {
                    Text(
                        text = "Expressive Clock",
                        style = ExpressiveWidgetTheme.labelStyle(GlanceTheme.colors.onSurface),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(topSpacing))

            if (horizontalLayout) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    TimeRow(
                        uiState = uiState,
                        compact = compact,
                        roomy = roomy,
                    )

                    Spacer(modifier = GlanceModifier.width(16.dp))

                    Text(
                        text = uiState.dateText,
                        style = ExpressiveWidgetTheme.dateStyle(
                            compact = compact,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            } else {
                TimeRow(
                    uiState = uiState,
                    compact = compact,
                    roomy = roomy,
                )

                Spacer(modifier = GlanceModifier.height(dateSpacing))

                Text(
                    text = uiState.dateText,
                    style = ExpressiveWidgetTheme.dateStyle(
                        compact = compact,
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TimeRow(
    uiState: ExpressiveWidgetUiState,
    compact: Boolean,
    roomy: Boolean,
) {
    Row(
        verticalAlignment = Alignment.Vertical.Bottom,
    ) {
        Text(
            text = uiState.timeText,
            style = ExpressiveWidgetTheme.timeStyle(
                sizeClass = when {
                    roomy -> ExpressiveWidgetTheme.ClockSizeClass.Large
                    compact -> ExpressiveWidgetTheme.ClockSizeClass.Compact
                    else -> ExpressiveWidgetTheme.ClockSizeClass.Regular
                },
                color = GlanceTheme.colors.onSurface,
            ),
        )

        Spacer(modifier = GlanceModifier.width(if (compact) 6.dp else 8.dp))

        Text(
            text = uiState.meridiemText,
            style = ExpressiveWidgetTheme.meridiemStyle(
                compact = compact,
                color = GlanceTheme.colors.primary,
            ),
        )
    }
}
