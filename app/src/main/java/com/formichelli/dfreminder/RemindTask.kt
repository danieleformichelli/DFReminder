package com.formichelli.dfreminder

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

import java.util.Calendar

/**
 * Remind notification
 * Created by daniele on 29/10/15.
 */
internal class RemindTask(context: Context, private val remindTimer: RemindTimer) : Runnable {
    private val notifier: Notifier = Notifier(context)
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val isEnabledPreferenceString: String = context.getString(R.string.pref_key_enable_dfreminder)
    private val startDoNotDisturbIntervalString: String = context.getString(R.string.pref_key_time_start)
    private val endDoNotDisturbIntervalString: String = context.getString(R.string.pref_key_time_end)

    override fun run() {
        Log.e(DFReminderMainActivity.TAG, "REMINDING!")
        synchronized(remindTimer) {
            checkPreferences()

            if (remindTimer.notifications.isEmpty()) {
                remindTimer.stopReminderTimerIfRunning()
                return
            }

            notifier.playNotification()
        }
    }

    private fun checkPreferences() {
        // check if in do not disturb interval
        val startDoNotDisturbIntervalTime = getIntTime(startDoNotDisturbIntervalString)
        val endDoNotDisturbIntervalTime = getIntTime(endDoNotDisturbIntervalString)
        if (startDoNotDisturbIntervalTime != endDoNotDisturbIntervalTime) {
            val isDoNotDisturb = isInRange(startDoNotDisturbIntervalTime, endDoNotDisturbIntervalTime)
            if (isDoNotDisturb) {
                remindTimer.notifications.clear()
                return
            }
        }

        // check if enabled
        if (!sharedPreferences.getBoolean(isEnabledPreferenceString, false))
            remindTimer.notifications.clear()
    }

    private fun isInRange(start: Int, end: Int): Boolean {
        val c = Calendar.getInstance()
        val currentTime = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)

        if (end > start)
        // no midnight crossing
            return currentTime > start || currentTime < end
        else
        // no midnight crossing
            return currentTime > start && currentTime < end
    }

    private fun getIntTime(preferenceKey: String): Int {
        val timeParts = sharedPreferences.getString(preferenceKey, "00:00").split(":").toTypedArray()
        return timeParts[0].toInt() * 60 + timeParts[1].toInt()
    }
}