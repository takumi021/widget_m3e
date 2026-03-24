package com.ompatel.expressivewidgetlab.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

data class TicTacToeUiState(
    val cells: List<String>,
    val statusText: String,
    val isFinished: Boolean,
)

object TicTacToeWidgetState {
    private const val EmptyBoard = "---------"
    private const val X = "X"
    private const val O = "O"
    private const val Draw = "DRAW"

    private val boardKey = stringPreferencesKey("tic_tac_toe_board")
    private val currentTurnKey = stringPreferencesKey("tic_tac_toe_current_turn")
    private val outcomeKey = stringPreferencesKey("tic_tac_toe_outcome")

    fun snapshot(preferences: Preferences): TicTacToeUiState {
        val board = preferences[boardKey] ?: EmptyBoard
        val currentTurn = preferences[currentTurnKey] ?: X
        val outcome = preferences[outcomeKey]

        val statusText = when (outcome) {
            X, O -> "$outcome wins"
            Draw -> "Draw game"
            else -> "$currentTurn to play"
        }

        return TicTacToeUiState(
            cells = board.map { if (it == '-') "" else it.toString() },
            statusText = statusText,
            isFinished = outcome != null,
        )
    }

    fun playMove(
        preferences: MutablePreferences,
        index: Int,
    ) {
        val board = (preferences[boardKey] ?: EmptyBoard).toCharArray()
        val currentTurn = (preferences[currentTurnKey] ?: X).first()
        val outcome = preferences[outcomeKey]

        if (index !in 0..8 || outcome != null || board[index] != '-') return

        board[index] = currentTurn

        val winner = detectWinner(board)
        when {
            winner != null -> {
                preferences[outcomeKey] = winner.toString()
            }

            '-' !in board -> {
                preferences[outcomeKey] = Draw
            }

            else -> {
                preferences[currentTurnKey] = if (currentTurn == 'X') O else X
            }
        }

        preferences[boardKey] = String(board)
    }

    fun reset(preferences: MutablePreferences) {
        preferences[boardKey] = EmptyBoard
        preferences[currentTurnKey] = X
        preferences.remove(outcomeKey)
    }

    private fun detectWinner(board: CharArray): Char? {
        val winningLines = listOf(
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            listOf(0, 4, 8),
            listOf(2, 4, 6),
        )

        return winningLines.firstNotNullOfOrNull { line ->
            val first = board[line[0]]
            if (first != '-' && line.all { board[it] == first }) first else null
        }
    }
}
