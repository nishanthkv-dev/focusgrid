package com.focusnow.app.ui.screens.study

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.data.datastore.ActiveTimerState
import com.focusnow.app.data.model.SessionType
import com.focusnow.app.data.model.StudySession
import com.focusnow.app.service.StudyTimerService
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class StudyUiState(
    val sessionType: SessionType = SessionType.FOCUS,
    val subject: String = "",
    val category: String = "General",
    val targetDurationMinutes: Int = 25,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val isAppBlockingEnabled: Boolean = true,
    val pomodoroRound: Int = 1,
    val isPomodoroBreak: Boolean = false,
    val recentSessions: List<StudySession> = emptyList(),
    val todayTotalStudyMinutes: Int = 0,
    val dailyTargetMinutes: Int = 240
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusNowApplication
    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        restoreTimerState()
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = DateTimeUtils.getTodayDateStr()

            launch {
                app.studyRepository.getSessionsForDate(todayStr).collect { sessions ->
                    val totalMins = sessions.sumOf { it.durationMinutes }
                    _uiState.value = _uiState.value.copy(
                        recentSessions = sessions,
                        todayTotalStudyMinutes = totalMins
                    )
                }
            }

            launch {
                app.database.userProfileDao().getUserProfile().collect { profile ->
                    _uiState.value = _uiState.value.copy(
                        dailyTargetMinutes = profile?.dailyStudyTargetMinutes ?: 240
                    )
                }
            }
        }
    }

    private fun restoreTimerState() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = app.preferencesRepository.activeTimerState.first()
            if (state.isActive) {
                val sessionType = try { SessionType.valueOf(state.sessionType) } catch (e: Exception) { SessionType.FOCUS }

                if (state.isPaused) {
                    _uiState.value = _uiState.value.copy(
                        sessionType = sessionType,
                        subject = state.subject,
                        category = state.category,
                        targetDurationMinutes = state.targetDurationMinutes,
                        isRunning = true,
                        isPaused = true,
                        remainingSeconds = state.pausedRemainingSeconds,
                        isAppBlockingEnabled = state.isAppBlockingEnabled,
                        pomodoroRound = state.pomodoroRound,
                        isPomodoroBreak = state.isBreak
                    )
                } else {
                    val now = System.currentTimeMillis()
                    val elapsedSec = ((now - state.startTimeMillis) / 1000).toInt()
                    val totalSec = state.targetDurationMinutes * 60
                    val remaining = (totalSec - elapsedSec).coerceAtLeast(0)

                    _uiState.value = _uiState.value.copy(
                        sessionType = sessionType,
                        subject = state.subject,
                        category = state.category,
                        targetDurationMinutes = state.targetDurationMinutes,
                        isRunning = true,
                        isPaused = false,
                        remainingSeconds = remaining,
                        isAppBlockingEnabled = state.isAppBlockingEnabled,
                        pomodoroRound = state.pomodoroRound,
                        isPomodoroBreak = state.isBreak
                    )

                    startLocalTimerCountdown(state.startTimeMillis, state.targetDurationMinutes)
                }
            }
        }
    }

    fun setSessionType(type: SessionType) {
        if (!_uiState.value.isRunning) {
            _uiState.value = _uiState.value.copy(
                sessionType = type,
                targetDurationMinutes = if (type == SessionType.FOCUS) 25 else 25,
                remainingSeconds = 25 * 60
            )
        }
    }

    fun setPresetDuration(minutes: Int) {
        if (!_uiState.value.isRunning) {
            _uiState.value = _uiState.value.copy(
                targetDurationMinutes = minutes,
                remainingSeconds = minutes * 60
            )
        }
    }

    fun updateSubject(subject: String) { _uiState.value = _uiState.value.copy(subject = subject) }
    fun updateCategory(category: String) { _uiState.value = _uiState.value.copy(category = category) }
    fun toggleAppBlocking(enabled: Boolean) { _uiState.value = _uiState.value.copy(isAppBlockingEnabled = enabled) }

    fun startTimer() {
        val state = _uiState.value
        val now = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            val timerState = ActiveTimerState(
                isActive = true,
                startTimeMillis = now,
                targetDurationMinutes = state.targetDurationMinutes,
                subject = state.subject.ifBlank { "Focus Session" },
                category = state.category,
                sessionType = state.sessionType.name,
                isAppBlockingEnabled = state.isAppBlockingEnabled,
                isPaused = false,
                pomodoroRound = state.pomodoroRound,
                isBreak = state.isPomodoroBreak
            )
            app.preferencesRepository.saveActiveTimerState(timerState)

            // Start foreground service
            StudyTimerService.startService(
                context = app,
                subject = timerState.subject,
                category = timerState.category,
                durationMinutes = timerState.targetDurationMinutes
            )

            _uiState.value = state.copy(
                isRunning = true,
                isPaused = false,
                remainingSeconds = state.targetDurationMinutes * 60
            )

            startLocalTimerCountdown(now, state.targetDurationMinutes)
        }
    }

    private fun startLocalTimerCountdown(startTimeMillis: Long, targetMinutes: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.IO) {
            val totalSeconds = targetMinutes * 60

            while (isActive) {
                val now = System.currentTimeMillis()
                val elapsed = ((now - startTimeMillis) / 1000).toInt()
                val remaining = (totalSeconds - elapsed).coerceAtLeast(0)

                _uiState.value = _uiState.value.copy(remainingSeconds = remaining)

                if (remaining <= 0) {
                    completeSession()
                    break
                }
                delay(1000)
            }
        }
    }

    fun pauseTimer() {
        val currentRemaining = _uiState.value.remainingSeconds
        timerJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            val current = app.preferencesRepository.activeTimerState.first()
            app.preferencesRepository.saveActiveTimerState(
                current.copy(
                    isPaused = true,
                    pausedRemainingSeconds = currentRemaining
                )
            )
            _uiState.value = _uiState.value.copy(isPaused = true)
        }
    }

    fun resumeTimer() {
        val remaining = _uiState.value.remainingSeconds
        val newTargetMinutes = (remaining / 60) + 1
        val now = System.currentTimeMillis() - ((newTargetMinutes * 60 - remaining) * 1000L)

        viewModelScope.launch(Dispatchers.IO) {
            val current = app.preferencesRepository.activeTimerState.first()
            app.preferencesRepository.saveActiveTimerState(
                current.copy(
                    isPaused = false,
                    startTimeMillis = now,
                    targetDurationMinutes = newTargetMinutes
                )
            )
            _uiState.value = _uiState.value.copy(isPaused = false)
            startLocalTimerCountdown(now, newTargetMinutes)
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val elapsedSeconds = (state.targetDurationMinutes * 60 - state.remainingSeconds).coerceAtLeast(0)
            val elapsedMinutes = (elapsedSeconds / 60).coerceAtLeast(1)

            if (elapsedMinutes >= 1 && !state.isPomodoroBreak) {
                val session = StudySession(
                    subject = state.subject.ifBlank { "Deep Focus" },
                    category = state.category,
                    startTimeMillis = System.currentTimeMillis() - (elapsedSeconds * 1000L),
                    endTimeMillis = System.currentTimeMillis(),
                    durationMinutes = elapsedMinutes,
                    sessionType = state.sessionType,
                    dateStr = DateTimeUtils.getTodayDateStr(),
                    targetDurationMinutes = state.targetDurationMinutes
                )
                app.studyRepository.saveStudySession(session, state.dailyTargetMinutes)
            }

            app.preferencesRepository.clearActiveTimer()
            StudyTimerService.stopService(app)

            _uiState.value = state.copy(
                isRunning = false,
                isPaused = false,
                remainingSeconds = state.targetDurationMinutes * 60
            )
        }
    }

    private suspend fun completeSession() {
        val state = _uiState.value
        val session = StudySession(
            subject = state.subject.ifBlank { "Deep Focus" },
            category = state.category,
            startTimeMillis = System.currentTimeMillis() - (state.targetDurationMinutes * 60 * 1000L),
            endTimeMillis = System.currentTimeMillis(),
            durationMinutes = state.targetDurationMinutes,
            sessionType = state.sessionType,
            dateStr = DateTimeUtils.getTodayDateStr(),
            targetDurationMinutes = state.targetDurationMinutes
        )
        app.studyRepository.saveStudySession(session, state.dailyTargetMinutes)
        app.preferencesRepository.clearActiveTimer()
        StudyTimerService.stopService(app)

        if (state.sessionType == SessionType.POMODORO && !state.isPomodoroBreak) {
            // Switch to Break
            _uiState.value = state.copy(
                isRunning = false,
                isPaused = false,
                isPomodoroBreak = true,
                targetDurationMinutes = 5,
                remainingSeconds = 5 * 60
            )
        } else {
            _uiState.value = state.copy(
                isRunning = false,
                isPaused = false,
                isPomodoroBreak = false,
                targetDurationMinutes = 25,
                remainingSeconds = 25 * 60,
                pomodoroRound = state.pomodoroRound + 1
            )
        }
    }
}
