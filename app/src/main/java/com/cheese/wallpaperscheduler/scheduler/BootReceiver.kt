package com.cheese.wallpaperscheduler.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cheese.wallpaperscheduler.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != "android.intent.action.QUICKBOOT_POWERON") {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(appContext).settingsFlow.first()
                AlarmScheduler(appContext).rescheduleAll(settings)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule alarms after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
