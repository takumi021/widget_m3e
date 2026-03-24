package com.ompatel.expressivewidgetlab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val statusMessage: String = "The app shell is ready. Widget integrations land in the next feature layer.",
    val isRefreshing: Boolean = false,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun primaryAction() {
        _uiState.value = _uiState.value.copy(
            statusMessage = "The expressive app shell is in place and ready for widget actions.",
        )
    }

    fun secondaryAction() {
        _uiState.value = _uiState.value.copy(
            statusMessage = "Next up: widget scheduling, state, and live refresh behavior.",
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
