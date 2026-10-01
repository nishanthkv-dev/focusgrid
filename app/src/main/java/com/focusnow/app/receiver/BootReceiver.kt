package com.focusnow.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusnow.app.FocusNowApplication
import com.focusnow.app.worker.DailyStreakWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DailyStreakWorker.enqueuePeriodicWork(context)

            val app = context.applicationContext as? FocusNowApplication
            app?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    it.streakRepository.evaluateStreaksForNewDay()
                }
            }
        }
    }
}
