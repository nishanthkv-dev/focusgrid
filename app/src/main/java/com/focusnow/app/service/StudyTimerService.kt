package com.focusnow.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.MainActivity
import com.focusnow.app.util.DateTimeUtils
import com.focusnow.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StudyTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var tickerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START -> {
                val subject = intent?.getStringExtra(EXTRA_SUBJECT) ?: "Study Session"
                val category = intent?.getStringExtra(EXTRA_CATEGORY) ?: "General"
                val durationMinutes = intent?.getIntExtra(EXTRA_DURATION_MINUTES, 25) ?: 25

                startForeground(
                    NotificationHelper.NOTIFICATION_ID_TIMER,
                    createNotification(subject, durationMinutes * 60)
                )

                startTimerTicker()
            }
            ACTION_STOP -> {
                stopTimerTicker()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startTimerTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            val app = application as FocusNowApplication

            while (isActive) {
                val timerState = app.preferencesRepository.activeTimerState.first()
                if (!timerState.isActive) {
                    stopSelf()
                    break
                }

                if (!timerState.isPaused) {
                    val now = System.currentTimeMillis()
                    val elapsedSeconds = ((now - timerState.startTimeMillis) / 1000).toInt()
                    val totalTargetSeconds = timerState.targetDurationMinutes * 60
                    val remainingSeconds = (totalTargetSeconds - elapsedSeconds).coerceAtLeast(0)

                    val notification = createNotification(timerState.subject, remainingSeconds)
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    nm.notify(NotificationHelper.NOTIFICATION_ID_TIMER, notification)

                    if (remainingSeconds <= 0) {
                        // Timer completed!
                        NotificationHelper.showReminderNotification(
                            this@StudyTimerService,
                            NotificationHelper.CHANNEL_STUDY_TIMER,
                            NotificationHelper.NOTIFICATION_ID_TIMER + 1,
                            "Study Session Completed! 🎉",
                            "Great work on '${timerState.subject}'! Take a break or start a new session."
                        )
                        break
                    }
                }

                delay(1000)
            }
        }
    }

    private fun stopTimerTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun createNotification(subject: String, remainingSeconds: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timerStr = DateTimeUtils.formatSecondsToTimer(remainingSeconds)

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_STUDY_TIMER)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Studying: ${subject.ifBlank { "Deep Focus" }}")
            .setContentText("Remaining time: $timerStr")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        stopTimerTicker()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.focusnow.app.ACTION_START_TIMER"
        const val ACTION_STOP = "com.focusnow.app.ACTION_STOP_TIMER"
        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"

        fun startService(context: Context, subject: String, category: String, durationMinutes: Int) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SUBJECT, subject)
                putExtra(EXTRA_CATEGORY, category)
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
