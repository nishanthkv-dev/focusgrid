package com.focusnow.app.ui.screens.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.repository.SleepAnalyticsSummary
import com.focusnow.app.data.repository.StudyAnalyticsSummary
import com.focusnow.app.util.DateTimeUtils
import com.focusnow.app.util.SleepStudyCorrelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val selectedTab: Int = 0, // 0: Study, 1: Sleep, 2: Correlation
    val studySummary: StudyAnalyticsSummary? = null,
    val sleepSummary: SleepAnalyticsSummary? = null,
    val correlation: SleepStudyCorrelation? = null,
    val isLogSleepDialogOpen: Boolean = false,
    val sleepBedtimeStr: String = "23:00",
    val sleepWakeTimeStr: String = "07:00",
    val sleepQuality: Int = 4,
    val isLoading: Boolean = false
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        refreshAnalytics()
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun refreshAnalytics() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val study = app.analyticsRepository.getStudyAnalytics()
            val sleep = app.analyticsRepository.getSleepAnalytics()
            val corr = app.analyticsRepository.getCorrelation()

            _uiState.value = _uiState.value.copy(
                studySummary = study,
                sleepSummary = sleep,
                correlation = corr,
                isLoading = false
            )
        }
    }

    fun openLogSleepDialog() {
        _uiState.value = _uiState.value.copy(isLogSleepDialogOpen = true)
    }

    fun closeLogSleepDialog() {
        _uiState.value = _uiState.value.copy(isLogSleepDialogOpen = false)
    }

    fun updateBedtime(time: String) { _uiState.value = _uiState.value.copy(sleepBedtimeStr = time) }
    fun updateWakeTime(time: String) { _uiState.value = _uiState.value.copy(sleepWakeTimeStr = time) }
    fun updateQuality(rating: Int) { _uiState.value = _uiState.value.copy(sleepQuality = rating) }

    fun saveSleepRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val profile = app.database.userProfileDao().getUserProfileOnce()
            val targetMinutes = profile?.sleepTargetMinutes ?: 480

            app.sleepRepository.logSleep(
                bedtimeStr = state.sleepBedtimeStr,
                wakeTimeStr = state.sleepWakeTimeStr,
                qualityRating = state.sleepQuality,
                sleepTargetMinutes = targetMinutes
            )

            closeLogSleepDialog()
            refreshAnalytics()
        }
    }
}
