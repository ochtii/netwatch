package com.netwatch.firewall.presentation.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netwatch.firewall.domain.model.AppEntry
import com.netwatch.firewall.domain.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the app list screen
 * Manages the state of installed apps and VPN service
 */
class AppListViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppListUiState>(AppListUiState.Loading)
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    private val _isVpnActive = MutableStateFlow(false)
    val isVpnActive: StateFlow<Boolean> = _isVpnActive.asStateFlow()

    init {
        loadApps()
    }

    /**
     * Load installed apps from repository
     */
    private fun loadApps() {
        viewModelScope.launch {
            appRepository.getInstalledApps()
                .catch { exception ->
                    _uiState.value = AppListUiState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { apps ->
                    _uiState.value = if (apps.isEmpty()) {
                        AppListUiState.Empty
                    } else {
                        AppListUiState.Success(apps)
                    }
                }
        }
    }

    /**
     * Toggle block status for an app
     */
    fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            appRepository.setAppBlocked(packageName, isBlocked)
        }
    }

    /**
     * Start or stop the VPN service
     * Returns true if action was initiated, false if VPN permission is needed
     */
    fun toggleVpnService(start: Boolean): Boolean {
        _isVpnActive.value = start
        if (!start) {
            // Clear data usage when stopping VPN
            viewModelScope.launch {
                appRepository.clearDataUsage()
            }
        }
        return true
    }

    /**
     * Refresh the app list
     */
    fun refresh() {
        loadApps()
    }
}

/**
 * UI State for the app list screen
 */
sealed class AppListUiState {
    data object Loading : AppListUiState()
    data object Empty : AppListUiState()
    data class Success(val apps: List<AppEntry>) : AppListUiState()
    data class Error(val message: String) : AppListUiState()
}
