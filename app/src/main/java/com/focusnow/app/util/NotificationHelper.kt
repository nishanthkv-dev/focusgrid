package com.focusnow.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.focusnow.app.MainActivity

object NotificationHelper {
    const val CHANNEL_STUDY_TIMER = "channel_study_timer"
    const val CHANNEL_TASKS = "channel_tasks"
    const val CHANNEL_CLASSES = "channel_classes"
    const val CHANNEL_SLEEP = "channel_sleep"
    const val CHANNEL_DAILY_TARGET = "channel_daily_target"

    const val NOTIFICATION_ID_TIMER = 1001
    const val NOTIFICATION_ID_TASK = 1002
    const val NOTIFICATION_ID_CLASS = 1003
    const val NOTIFICATION_ID_SLEEP = 1004
    const val NOTIFICATION_ID_TARGET = 1005

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_STUDY_TIMER,
                    "Study Timer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Active study session countdown and tracking"
                },
                NotificationChannel(
                    CHANNEL_TASKS,
                    "Tasks & Deadlines",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for scheduled tasks and upcoming exams"
                },
                NotificationChannel(
                    CHANNEL_CLASSES,
                    "College Classes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for upcoming college classes"
                },
                NotificationChannel(
                    CHANNEL_SLEEP,
                    "Sleep Schedule",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Bedtime and wake-up notifications"
                },
                NotificationChannel(
                    CHANNEL_DAILY_TARGET,
                    "Daily Targets & Streaks",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily study target reminders and streak alerts"
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun showReminderNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+
        }
    }
}
