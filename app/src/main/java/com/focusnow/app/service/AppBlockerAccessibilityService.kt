package com.focusnow.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppBlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastBlockedPackage: String = ""
    private var lastBlockedTimestamp: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Skip our own package and system UI components
        if (packageName == this.packageName ||
            packageName == "com.android.systemui" ||
            packageName == "com.google.android.inputmethod.latin"
        ) {
            return
        }

        serviceScope.launch {
            val app = application as? FocusNowApplication ?: return@launch
            val timerState = app.preferencesRepository.activeTimerState.first()

            // App blocking only active if timer is active, not paused, and blocking enabled
            if (timerState.isActive && !timerState.isPaused && timerState.isAppBlockingEnabled) {
                val isBlocked = app.appBlockingRepository.isAppBlocked(packageName)

                if (isBlocked) {
                    val now = System.currentTimeMillis()
                    // Throttle repeated triggers for the same app within 2 seconds
                    if (packageName == lastBlockedPackage && (now - lastBlockedTimestamp) < 2000) {
                        return@launch
                    }

                    lastBlockedPackage = packageName
                    lastBlockedTimestamp = now

                    // Calculate remaining seconds
                    val elapsedSeconds = ((now - timerState.startTimeMillis) / 1000).toInt()
                    val totalTargetSeconds = timerState.targetDurationMinutes * 60
                    val remainingSeconds = (totalTargetSeconds - elapsedSeconds).coerceAtLeast(0)

                    val intent = Intent(this@AppBlockerAccessibilityService, BlockOverlayActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("EXTRA_SUBJECT", timerState.subject)
                        putExtra("EXTRA_CATEGORY", timerState.category)
                        putExtra("EXTRA_REMAINING_SECONDS", remainingSeconds)
                        putExtra("EXTRA_BLOCKED_PACKAGE", packageName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }
}
