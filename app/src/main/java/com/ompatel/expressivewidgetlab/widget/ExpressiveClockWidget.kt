package com.ompatel.expressivewidgetlab.widget

import android.content.Context
import android.os.Build
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
            WidgetTheme {
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
private fun WidgetTheme(content: @Composable () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceTheme(content = content)
    } else {
        GlanceTheme(colors = ExpressiveWidgetTheme.fallbackColors(), content = content)
    }
}

@Composable
private fun ExpressiveClockWidgetContent(
    uiState: ExpressiveWidgetUiState,
) {
    val expanded = LocalSize.current.width >= 180.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(ExpressiveWidgetTheme.OuterCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionRunCallback<RefreshClockAction>())
            .padding(ExpressiveWidgetTheme.ContentPadding),
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
                        .size(40.dp)
                        .cornerRadius(14.dp)
                        .background(GlanceTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_clock),
                        contentDescription = "Clock icon",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                        modifier = GlanceModifier.size(22.dp),
                    )
                }

                Spacer(modifier = GlanceModifier.width(ExpressiveWidgetTheme.ComfortableSpacing))

                Column {
                    Text(
                        text = "Expressive Clock",
                        style = ExpressiveWidgetTheme.labelStyle(GlanceTheme.colors.onSurface),
                    )
                    Text(
                        text = uiState.lastUpdatedText,
                        style = ExpressiveWidgetTheme.labelStyle(GlanceTheme.colors.onSurfaceVariant),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.Vertical.Bottom,
            ) {
                Text(
                    text = uiState.timeText,
                    style = ExpressiveWidgetTheme.timeStyle(
                        expanded = expanded,
                        color = GlanceTheme.colors.onSurface,
                    ),
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = uiState.meridiemText,
                    style = ExpressiveWidgetTheme.meridiemStyle(
                        color = GlanceTheme.colors.primary,
                    ),
                )
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            Text(
                text = uiState.dateText,
                style = ExpressiveWidgetTheme.dateStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
            )

            Spacer(modifier = GlanceModifier.height(10.dp))

            Box(
                modifier = GlanceModifier
                    .cornerRadius(999.dp)
                    .background(GlanceTheme.colors.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = uiState.refreshSourceLabel,
                    style = ExpressiveWidgetTheme.chipStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                    ),
                )
            }
        }
    }
}
