package com.example.daybrief.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.daybrief.di.DataStoreModule
import com.example.daybrief.worker.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = DataStoreModule.provideLocalDataSource(context).settings.first()
                AlarmScheduler.scheduleDailyBriefing(
                    context,
                    settings.notificationHour,
                    settings.notificationMinute,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
