package com.ompatel.expressivewidgetlab.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
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

class TicTacToeWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ExpressiveWidgetTheme.GlanceSurface {
                val preferences = currentState<Preferences>()
                val uiState = TicTacToeWidgetState.snapshot(preferences)
                TicTacToeWidgetContent(uiState)
            }
        }
    }
}

class TicTacToeWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TicTacToeWidget()
}

private class ResetGameAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(context, glanceId) { preferences: MutablePreferences ->
            TicTacToeWidgetState.reset(preferences)
        }
        TicTacToeWidget().update(context, glanceId)
    }
}

private abstract class BaseMoveAction(
    private val index: Int,
) : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(context, glanceId) { preferences: MutablePreferences ->
            TicTacToeWidgetState.playMove(preferences, index)
        }
        TicTacToeWidget().update(context, glanceId)
    }
}

private class Move0Action : BaseMoveAction(0)
private class Move1Action : BaseMoveAction(1)
private class Move2Action : BaseMoveAction(2)
private class Move3Action : BaseMoveAction(3)
private class Move4Action : BaseMoveAction(4)
private class Move5Action : BaseMoveAction(5)
private class Move6Action : BaseMoveAction(6)
private class Move7Action : BaseMoveAction(7)
private class Move8Action : BaseMoveAction(8)

@Composable
private fun TicTacToeWidgetContent(
    uiState: TicTacToeUiState,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(ExpressiveWidgetTheme.OuterCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
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
                        provider = ImageProvider(R.drawable.ic_widget_game),
                        contentDescription = "Tic-tac-toe icon",
                        modifier = GlanceModifier.size(22.dp),
                    )
                }

                Spacer(modifier = GlanceModifier.width(ExpressiveWidgetTheme.ComfortableSpacing))

                Column {
                    Text(
                        text = "Expressive Tic-Tac-Toe",
                        style = ExpressiveWidgetTheme.labelStyle(GlanceTheme.colors.onSurface),
                    )
                    Text(
                        text = uiState.statusText,
                        style = ExpressiveWidgetTheme.labelStyle(GlanceTheme.colors.onSurfaceVariant),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            TicTacToeRow(uiState = uiState, indexes = listOf(0, 1, 2))
            Spacer(modifier = GlanceModifier.height(ExpressiveWidgetTheme.GridSpacing))
            TicTacToeRow(uiState = uiState, indexes = listOf(3, 4, 5))
            Spacer(modifier = GlanceModifier.height(ExpressiveWidgetTheme.GridSpacing))
            TicTacToeRow(uiState = uiState, indexes = listOf(6, 7, 8))

            Spacer(modifier = GlanceModifier.height(14.dp))

            Box(
                modifier = GlanceModifier
                    .cornerRadius(999.dp)
                    .background(GlanceTheme.colors.secondaryContainer)
                    .clickable(actionRunCallback<ResetGameAction>())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.isFinished) "Play again" else "Reset board",
                    style = ExpressiveWidgetTheme.chipStyle(GlanceTheme.colors.onSecondaryContainer),
                )
            }
        }
    }
}

@Composable
private fun TicTacToeRow(
    uiState: TicTacToeUiState,
    indexes: List<Int>,
) {
    Row {
        TicTacToeCell(uiState, indexes[0])
        Spacer(modifier = GlanceModifier.width(ExpressiveWidgetTheme.GridSpacing))
        TicTacToeCell(uiState, indexes[1])
        Spacer(modifier = GlanceModifier.width(ExpressiveWidgetTheme.GridSpacing))
        TicTacToeCell(uiState, indexes[2])
    }
}

@Composable
private fun TicTacToeCell(
    uiState: TicTacToeUiState,
    index: Int,
) {
    val cellValue = uiState.cells[index]
    val action = when (index) {
        0 -> actionRunCallback<Move0Action>()
        1 -> actionRunCallback<Move1Action>()
        2 -> actionRunCallback<Move2Action>()
        3 -> actionRunCallback<Move3Action>()
        4 -> actionRunCallback<Move4Action>()
        5 -> actionRunCallback<Move5Action>()
        6 -> actionRunCallback<Move6Action>()
        7 -> actionRunCallback<Move7Action>()
        else -> actionRunCallback<Move8Action>()
    }

    val backgroundColor = when (cellValue) {
        "X" -> GlanceTheme.colors.primaryContainer
        "O" -> GlanceTheme.colors.tertiaryContainer
        else -> GlanceTheme.colors.surfaceVariant
    }

    Box(
        modifier = GlanceModifier
            .size(60.dp)
            .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
            .background(backgroundColor)
            .clickable(action)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (cellValue.isEmpty()) " " else cellValue,
            style = ExpressiveWidgetTheme.timeStyle(
                expanded = false,
                color = when (cellValue) {
                    "X" -> GlanceTheme.colors.onPrimaryContainer
                    "O" -> GlanceTheme.colors.onTertiaryContainer
                    else -> GlanceTheme.colors.onSurfaceVariant
                },
            ),
        )
    }
}
