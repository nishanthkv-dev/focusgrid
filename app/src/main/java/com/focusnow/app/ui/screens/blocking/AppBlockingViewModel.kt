package com.focusnow.app.ui.screens.blocking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.BlockedApp
import com.focusnow.app.data.model.BlockingProfile
import com.focusnow.app.data.repository.InstalledAppInfo
import com.focusnow.app.service.AppUsageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AppBlockingUiState(
    val isGlobalBlockingEnabled: Boolean = true,
    val hasUsagePermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val blockedApps: List<BlockedApp> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val profiles: List<BlockingProfile> = emptyList(),
    val activeProfileId: Long = 1L,
    val searchQuery: String = "",
    val isAddProfileDialogOpen: Boolean = false
)

class AppBlockingViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(AppBlockingUiState())
    val uiState: StateFlow<AppBlockingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun checkPermissions() {
        _uiState.value = _uiState.value.copy(
            hasUsagePermission = AppUsageHelper.hasUsageStatsPermission(app),
            hasAccessibilityPermission = AppUsageHelper.isAccessibilityServiceEnabled(app),
            hasOverlayPermission = AppUsageHelper.canDrawOverlays(app)
        )
    }

    private fun loadData() {
        checkPermissions()
        viewModelScope.launch(Dispatchers.IO) {
            val installed = app.appBlockingRepository.getInstalledApps()
            _uiState.value = _uiState.value.copy(installedApps = installed)

            launch {
                app.appBlockingRepository.allBlockedApps.collect { apps ->
                    _uiState.value = _uiState.value.copy(blockedApps = apps)
                }
            }

            launch {
                app.appBlockingRepository.allProfiles.collect { profiles ->
                    _uiState.value = _uiState.value.copy(profiles = profiles)
                }
            }

            launch {
                app.preferencesRepository.isAppBlockingGlobalEnabled.collect { enabled ->
                    _uiState.value = _uiState.value.copy(isGlobalBlockingEnabled = enabled)
                }
            }

            launch {
                app.preferencesRepository.activeBlockingProfileId.collect { id ->
                    _uiState.value = _uiState.value.copy(activeProfileId = id)
                }
            }
        }
    }

    fun toggleGlobalBlocking(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            app.preferencesRepository.setAppBlockingGlobalEnabled(enabled)
        }
    }

    fun selectProfile(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            app.preferencesRepository.setActiveBlockingProfileId(profileId)
        }
    }

    fun toggleAppBlocked(packageName: String, appName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            app.appBlockingRepository.toggleAppBlocked(packageName, appName)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun openAddProfileDialog() { _uiState.value = _uiState.value.copy(isAddProfileDialogOpen = true) }
    fun closeAddProfileDialog() { _uiState.value = _uiState.value.copy(isAddProfileDialogOpen = false) }

    fun createProfile(name: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val activeApps = app.appBlockingRepository.activeBlockedApps.first().map { it.packageName }
            val newProfile = BlockingProfile(
                profileName = name,
                description = description,
                blockedPackageNamesJson = app.appBlockingRepository.serializePackageList(activeApps)
            )
            val id = app.appBlockingRepository.insertProfile(newProfile)
            app.preferencesRepository.setActiveBlockingProfileId(id)
            closeAddProfileDialog()
        }
    }
}
