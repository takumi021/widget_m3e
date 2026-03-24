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
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
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

class ResetGameAction : ActionCallback {
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

class MoveAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val index = parameters[CellIndexKey] ?: return
        var boardChanged = false
        updateAppWidgetState(context, glanceId) { preferences: MutablePreferences ->
            boardChanged = TicTacToeWidgetState.playMove(preferences, index)
        }
        if (boardChanged) {
            TicTacToeWidget().update(context, glanceId)
        }
    }
}

private val CellIndexKey = ActionParameters.Key<Int>("cell_index")

@Composable
private fun TicTacToeWidgetContent(
    uiState: TicTacToeUiState,
) {
    val widgetSize = LocalSize.current
    val compact = widgetSize.width < 240.dp || widgetSize.height < 220.dp
    val roomy = widgetSize.width >= 300.dp && widgetSize.height >= 260.dp
    val cellSize = when {
        roomy -> 68.dp
        compact -> 50.dp
        else -> 58.dp
    }
    val gridSpacing = if (compact) 6.dp else ExpressiveWidgetTheme.GridSpacing
    val topSpacing = if (compact) 10.dp else 14.dp
    val bottomSpacing = if (compact) 8.dp else 12.dp
    val iconSize = if (compact) 34.dp else 40.dp
    val iconInnerSize = if (compact) 18.dp else 22.dp
    val contentPadding = if (compact) 14.dp else ExpressiveWidgetTheme.ContentPadding

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(ExpressiveWidgetTheme.OuterCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
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
                        provider = ImageProvider(R.drawable.ic_widget_game),
                        contentDescription = "Tic-tac-toe icon",
                        modifier = GlanceModifier.size(iconInnerSize),
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

            Spacer(modifier = GlanceModifier.height(topSpacing))

            TicTacToeRow(uiState = uiState, indexes = listOf(0, 1, 2), cellSize = cellSize, spacing = gridSpacing)
            Spacer(modifier = GlanceModifier.height(gridSpacing))
            TicTacToeRow(uiState = uiState, indexes = listOf(3, 4, 5), cellSize = cellSize, spacing = gridSpacing)
            Spacer(modifier = GlanceModifier.height(gridSpacing))
            TicTacToeRow(uiState = uiState, indexes = listOf(6, 7, 8), cellSize = cellSize, spacing = gridSpacing)

            Spacer(modifier = GlanceModifier.height(bottomSpacing))

            Box(
                modifier = GlanceModifier
                    .cornerRadius(999.dp)
                    .background(GlanceTheme.colors.secondaryContainer)
                    .clickable(actionRunCallback<ResetGameAction>())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.isFinished) "New round" else "Reset",
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
    cellSize: androidx.compose.ui.unit.Dp,
    spacing: androidx.compose.ui.unit.Dp,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        TicTacToeCell(uiState, indexes[0], cellSize)
        Spacer(modifier = GlanceModifier.width(spacing))
        TicTacToeCell(uiState, indexes[1], cellSize)
        Spacer(modifier = GlanceModifier.width(spacing))
        TicTacToeCell(uiState, indexes[2], cellSize)
    }
}

@Composable
private fun TicTacToeCell(
    uiState: TicTacToeUiState,
    index: Int,
    cellSize: androidx.compose.ui.unit.Dp,
) {
    val cellValue = uiState.cells[index]
    val action = actionRunCallback<MoveAction>(
        parameters = actionParametersOf(CellIndexKey to index),
    )

    val backgroundColor = when (cellValue) {
        "X" -> GlanceTheme.colors.primaryContainer
        "O" -> GlanceTheme.colors.tertiaryContainer
        else -> GlanceTheme.colors.surfaceVariant
    }

    Box(
        modifier = GlanceModifier
            .size(cellSize)
            .cornerRadius(ExpressiveWidgetTheme.InnerCornerRadius)
            .background(backgroundColor)
            .clickable(action)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (cellValue.isEmpty()) " " else cellValue,
            style = ExpressiveWidgetTheme.boardCellStyle(
                large = cellSize >= 60.dp,
                color = when (cellValue) {
                    "X" -> GlanceTheme.colors.onPrimaryContainer
                    "O" -> GlanceTheme.colors.onTertiaryContainer
                    else -> GlanceTheme.colors.onSurfaceVariant
                },
            ),
        )
    }
}
