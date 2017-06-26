package com.formichelli.dfreminder

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class RemindTimer(private val context: Context, val notifications: Collection<String>) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val frequencyPreferenceString: String = context.getString(R.string.pref_key_remind_frequency)

    private var scheduler: ScheduledExecutorService? = null

    @Synchronized private fun startReminderTimerIfNotRunning() {
        if (scheduler != null)
            return

        val reminderInterval = Integer.valueOf(sharedPreferences.getString(frequencyPreferenceString, "300000"))!!.toLong()
        scheduler = Executors.newScheduledThreadPool(1)
        scheduler?.scheduleAtFixedRate(RemindTask(context, this), reminderInterval, reminderInterval, TimeUnit.MILLISECONDS)
        Log.d(DFReminderMainActivity.TAG, "Started notification timer with interval: " + reminderInterval)
    }

    @Synchronized fun stopReminderTimerIfRunning() {
        scheduler?.shutdownNow()
        scheduler = null
        Log.d(DFReminderMainActivity.TAG, "Stopped notification timer")
    }

    @Synchronized fun restartReminderTimer() {
        stopReminderTimerIfRunning()
        startReminderTimerIfNotRunning()
    }
}
