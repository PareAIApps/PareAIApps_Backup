package pnj.pk.pareaipk.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * This BroadcastReceiver restarts the daily notification after the device reboots
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check notification preferences and reschedule if needed
            CoroutineScope(Dispatchers.IO).launch {
                val preferences = SettingsPreferences.getInstance(context)
                val isNotificationEnabled = preferences.isNotificationEnabled.first()

                if (isNotificationEnabled) {
                    DailyReminderWorker.scheduleReminder(context)
                }
            }
        }
    }
}