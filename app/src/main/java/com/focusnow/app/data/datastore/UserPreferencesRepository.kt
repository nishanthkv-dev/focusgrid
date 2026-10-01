package com.focusnow.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_now_preferences")

data class ActiveTimerState(
    val isActive: Boolean = false,
    val startTimeMillis: Long = 0L,
    val targetDurationMinutes: Int = 25,
    val subject: String = "",
    val category: String = "General",
    val sessionType: String = "FOCUS", // FOCUS or POMODORO
    val isAppBlockingEnabled: Boolean = false,
    val isPaused: Boolean = false,
    val pausedRemainingSeconds: Int = 0,
    val pomodoroRound: Int = 1,
    val isBreak: Boolean = false
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_BLOCKING_GLOBAL_ENABLED = booleanPreferencesKey("app_blocking_global_enabled")
        val ACTIVE_BLOCKING_PROFILE_ID = longPreferencesKey("active_blocking_profile_id")

        // Active Timer Persistence
        val TIMER_IS_ACTIVE = booleanPreferencesKey("timer_is_active")
        val TIMER_START_TIME = longPreferencesKey("timer_start_time")
        val TIMER_TARGET_MINUTES = intPreferencesKey("timer_target_minutes")
        val TIMER_SUBJECT = stringPreferencesKey("timer_subject")
        val TIMER_CATEGORY = stringPreferencesKey("timer_category")
        val TIMER_SESSION_TYPE = stringPreferencesKey("timer_session_type")
        val TIMER_BLOCKING_ENABLED = booleanPreferencesKey("timer_blocking_enabled")
        val TIMER_IS_PAUSED = booleanPreferencesKey("timer_is_paused")
        val TIMER_PAUSED_REMAINING_SEC = intPreferencesKey("timer_paused_remaining_sec")
        val TIMER_POMODORO_ROUND = intPreferencesKey("timer_pomodoro_round")
        val TIMER_IS_BREAK = booleanPreferencesKey("timer_is_break")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val isAppBlockingGlobalEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_BLOCKING_GLOBAL_ENABLED] ?: true
    }

    suspend fun setAppBlockingGlobalEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_BLOCKING_GLOBAL_ENABLED] = enabled
        }
    }

    val activeBlockingProfileId: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVE_BLOCKING_PROFILE_ID] ?: 1L
    }

    suspend fun setActiveBlockingProfileId(profileId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_BLOCKING_PROFILE_ID] = profileId
        }
    }

    val activeTimerState: Flow<ActiveTimerState> = context.dataStore.data.map { preferences ->
        ActiveTimerState(
            isActive = preferences[PreferencesKeys.TIMER_IS_ACTIVE] ?: false,
            startTimeMillis = preferences[PreferencesKeys.TIMER_START_TIME] ?: 0L,
            targetDurationMinutes = preferences[PreferencesKeys.TIMER_TARGET_MINUTES] ?: 25,
            subject = preferences[PreferencesKeys.TIMER_SUBJECT] ?: "",
            category = preferences[PreferencesKeys.TIMER_CATEGORY] ?: "General",
            sessionType = preferences[PreferencesKeys.TIMER_SESSION_TYPE] ?: "FOCUS",
            isAppBlockingEnabled = preferences[PreferencesKeys.TIMER_BLOCKING_ENABLED] ?: false,
            isPaused = preferences[PreferencesKeys.TIMER_IS_PAUSED] ?: false,
            pausedRemainingSeconds = preferences[PreferencesKeys.TIMER_PAUSED_REMAINING_SEC] ?: 0,
            pomodoroRound = preferences[PreferencesKeys.TIMER_POMODORO_ROUND] ?: 1,
            isBreak = preferences[PreferencesKeys.TIMER_IS_BREAK] ?: false
        )
    }

    suspend fun saveActiveTimerState(state: ActiveTimerState) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIMER_IS_ACTIVE] = state.isActive
            preferences[PreferencesKeys.TIMER_START_TIME] = state.startTimeMillis
            preferences[PreferencesKeys.TIMER_TARGET_MINUTES] = state.targetDurationMinutes
            preferences[PreferencesKeys.TIMER_SUBJECT] = state.subject
            preferences[PreferencesKeys.TIMER_CATEGORY] = state.category
            preferences[PreferencesKeys.TIMER_SESSION_TYPE] = state.sessionType
            preferences[PreferencesKeys.TIMER_BLOCKING_ENABLED] = state.isAppBlockingEnabled
            preferences[PreferencesKeys.TIMER_IS_PAUSED] = state.isPaused
            preferences[PreferencesKeys.TIMER_PAUSED_REMAINING_SEC] = state.pausedRemainingSeconds
            preferences[PreferencesKeys.TIMER_POMODORO_ROUND] = state.pomodoroRound
            preferences[PreferencesKeys.TIMER_IS_BREAK] = state.isBreak
        }
    }

    suspend fun clearActiveTimer() {
        saveActiveTimerState(ActiveTimerState())
    }
}
