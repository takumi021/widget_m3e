package com.ompatel.expressivewidgetlab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ompatel.expressivewidgetlab.widget.ExpressiveClockWidget
import com.ompatel.expressivewidgetlab.widget.WidgetRefreshSource
import com.ompatel.expressivewidgetlab.worker.WidgetUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val statusMessage: String = "Schedule the worker once, place the widget, and tap it anytime for an on-demand refresh.",
    val isRefreshing: Boolean = false,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        ensureSchedule()
    }

    fun ensureSchedule() {
        WidgetUpdateWorker.enqueuePeriodicWork(getApplication())
        _uiState.value = _uiState.value.copy(
            statusMessage = "Periodic widget updates are scheduled with WorkManager every 15 minutes.",
        )
    }

    fun refreshWidgets() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                statusMessage = "Refreshing all widget instances now...",
            )

            runCatching {
                ExpressiveClockWidget.refreshAll(
                    context = getApplication(),
                    source = WidgetRefreshSource.APP,
                )
            }.onSuccess {
                _uiState.value = HomeUiState(
                    statusMessage = "All placed widgets were refreshed successfully.",
                    isRefreshing = false,
                )
            }.onFailure { error ->
                _uiState.value = HomeUiState(
                    statusMessage = "Refresh failed: ${error.message ?: "unknown error"}.",
                    isRefreshing = false,
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                MainViewModel(application)
            }
        }
    }
}
