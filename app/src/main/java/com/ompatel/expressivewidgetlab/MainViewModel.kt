package com.ompatel.expressivewidgetlab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ompatel.expressivewidgetlab.worker.WidgetUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val statusMessage: String = "The clock follows your device time automatically, and both widgets are ready from the home screen picker.",
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        ensureSchedule()
    }

    private fun ensureSchedule() {
        WidgetUpdateWorker.ensureClockSchedules(getApplication())
        _uiState.value = _uiState.value.copy(
            statusMessage = "The clock follows your device time automatically, and both widgets are ready from the home screen picker.",
        )
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
