package com.focusnow.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusnow.app.util.NotificationHelper

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Focus Now Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have a scheduled activity."
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: NotificationHelper.CHANNEL_TASKS
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 2000)

        NotificationHelper.showReminderNotification(
            context = context,
            channelId = channelId,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
