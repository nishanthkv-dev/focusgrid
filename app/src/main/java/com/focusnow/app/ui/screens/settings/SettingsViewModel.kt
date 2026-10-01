package com.focusnow.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.datastore.ThemeMode
import com.focusnow.app.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userProfile: UserProfile? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isNotificationsEnabled: Boolean = true,
    val isExportSuccess: Boolean = false,
    val exportedJson: String = "",
    val importStatusMessage: String = "",
    val isEditProfileDialogOpen: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                app.database.userProfileDao().getUserProfile().collect { profile ->
                    _uiState.value = _uiState.value.copy(userProfile = profile)
                }
            }

            launch {
                app.preferencesRepository.themeMode.collect { mode ->
                    _uiState.value = _uiState.value.copy(themeMode = mode)
                }
            }

            launch {
                app.preferencesRepository.isNotificationsEnabled.collect { enabled ->
                    _uiState.value = _uiState.value.copy(isNotificationsEnabled = enabled)
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            app.preferencesRepository.setThemeMode(mode)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            app.preferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun openEditProfileDialog() { _uiState.value = _uiState.value.copy(isEditProfileDialogOpen = true) }
    fun closeEditProfileDialog() { _uiState.value = _uiState.value.copy(isEditProfileDialogOpen = false) }

    fun updateProfile(name: String, college: String, department: String, year: String, studyTargetMins: Int, sleepTargetMins: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value.userProfile ?: UserProfile()
            val updated = current.copy(
                name = name,
                college = college,
                department = department,
                currentYear = year,
                dailyStudyTargetMinutes = studyTargetMins,
                sleepTargetMinutes = sleepTargetMins
            )
            app.database.userProfileDao().insertOrUpdateProfile(updated)
            closeEditProfileDialog()
        }
    }

    fun exportBackup(onExportReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = app.backupRepository.exportDataToJson()
            _uiState.value = _uiState.value.copy(exportedJson = json, isExportSuccess = true)
            onExportReady(json)
        }
    }

    fun restoreBackup(json: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = app.backupRepository.restoreDataFromJson(json)
            _uiState.value = _uiState.value.copy(
                importStatusMessage = if (success) "Backup restored successfully! 🎉" else "Failed to restore backup. Invalid JSON format."
            )
        }
    }

    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            app.backupRepository.resetAllData()
            _uiState.value = _uiState.value.copy(importStatusMessage = "All data reset successfully.")
        }
    }
}
