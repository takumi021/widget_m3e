package com.ompatel.expressivewidgetlab

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ompatel.expressivewidgetlab.health.SamsungHealthRepository
import com.ompatel.expressivewidgetlab.widget.SamsungHealthWidget
import com.ompatel.expressivewidgetlab.widget.SamsungHealthWidgetState
import com.ompatel.expressivewidgetlab.worker.SamsungHealthWidgetWorker
import com.ompatel.expressivewidgetlab.worker.WidgetUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val statusMessage: String = "Widgets are ready from the home screen picker.",
    val samsungHealthMessage: String = "Connect Samsung Health to populate the health widget.",
    val isConnectingSamsungHealth: Boolean = false,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val samsungHealthRepository = SamsungHealthRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        WidgetUpdateWorker.ensureClockSchedules(getApplication())
        SamsungHealthWidgetWorker.ensureScheduled(getApplication())
    }

    fun connectSamsungHealth(activity: Activity) {
        if (_uiState.value.isConnectingSamsungHealth) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isConnectingSamsungHealth = true,
                samsungHealthMessage = "Connecting Samsung Health...",
            )

            val permissionState = samsungHealthRepository.ensureReadPermissions(activity)

            if (permissionState.isReady) {
                val snapshot = samsungHealthRepository.loadSnapshot()
                SamsungHealthWidgetState.writeSnapshot(getApplication(), snapshot)
                SamsungHealthWidget.refreshAll(getApplication())
            }

            _uiState.value = _uiState.value.copy(
                isConnectingSamsungHealth = false,
                samsungHealthMessage = permissionState.message,
            )
        }
    }

    fun refreshSamsungHealth() {
        viewModelScope.launch {
            val snapshot = samsungHealthRepository.loadSnapshot()
            SamsungHealthWidgetState.writeSnapshot(getApplication(), snapshot)
            SamsungHealthWidget.refreshAll(getApplication())
            _uiState.value = _uiState.value.copy(
                samsungHealthMessage = "Refreshing Samsung Health widget data...",
            )
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
