package com.focusnow.app.ui.screens.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.model.UserProfile
import com.focusnow.app.data.repository.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: Int = 1, // 1: Student Profile, 2: Choose Distractions
    val name: String = "",
    val college: String = "",
    val department: String = "",
    val currentYear: String = "2nd Year",
    val dailyStudyTargetMinutes: Int = 240, // 4h
    val normalSleepTime: String = "23:00",
    val normalWakeTime: String = "07:00",
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val selectedAppPackages: Set<String> = emptySet(),
    val isLoadingApps: Boolean = false,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoadingApps = true)
            val apps = app.appBlockingRepository.getInstalledApps()

            // Pre-select known common distractions
            val defaultDistractions = setOf(
                "com.instagram.android",
                "com.google.android.youtube",
                "com.whatsapp",
                "com.facebook.katana",
                "com.reddit.frontpage",
                "com.twitter.android",
                "com.snapchat.android"
            )
            val initialSelected = apps.filter { defaultDistractions.contains(it.packageName) }
                .map { it.packageName }.toSet()

            _uiState.value = _uiState.value.copy(
                installedApps = apps,
                selectedAppPackages = initialSelected,
                isLoadingApps = false
            )
        }
    }

    fun updateName(name: String) { _uiState.value = _uiState.value.copy(name = name) }
    fun updateCollege(college: String) { _uiState.value = _uiState.value.copy(college = college) }
    fun updateDepartment(department: String) { _uiState.value = _uiState.value.copy(department = department) }
    fun updateCurrentYear(year: String) { _uiState.value = _uiState.value.copy(currentYear = year) }
    fun updateDailyTarget(minutes: Int) { _uiState.value = _uiState.value.copy(dailyStudyTargetMinutes = minutes) }
    fun updateSleepTime(time: String) { _uiState.value = _uiState.value.copy(normalSleepTime = time) }
    fun updateWakeTime(time: String) { _uiState.value = _uiState.value.copy(normalWakeTime = time) }

    fun toggleAppSelection(packageName: String) {
        val current = _uiState.value.selectedAppPackages.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _uiState.value = _uiState.value.copy(selectedAppPackages = current)
    }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(step = 2)
    }

    fun completeOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value

            val profile = UserProfile(
                id = 1,
                name = state.name.ifBlank { "Student" },
                college = state.college,
                department = state.department,
                currentYear = state.currentYear,
                dailyStudyTargetMinutes = state.dailyStudyTargetMinutes,
                sleepTargetMinutes = 480,
                normalSleepTime = state.normalSleepTime,
                normalWakeTime = state.normalWakeTime,
                isOnboardingCompleted = true
            )
            app.database.userProfileDao().insertOrUpdateProfile(profile)
            app.preferencesRepository.setOnboardingCompleted(true)

            // Save selected blocked apps
            state.installedApps.forEach { installedApp ->
                if (state.selectedAppPackages.contains(installedApp.packageName)) {
                    app.appBlockingRepository.setAppBlockedStatus(
                        packageName = installedApp.packageName,
                        appName = installedApp.appName,
                        isBlocked = true
                    )
                }
            }

            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }
}
