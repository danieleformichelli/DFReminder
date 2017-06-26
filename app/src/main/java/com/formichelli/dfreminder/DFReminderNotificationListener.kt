package com.formichelli.dfreminder

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.*

/**
 * Listen for notifications
 *
 *
 * Created by daniele on 29/10/15.
 */
class DFReminderNotificationListener : NotificationListenerService() {
    private var notifications = HashSet<String>()
    private var remindTimer: RemindTimer? = null
    private var sharedPreferences: SharedPreferences? = null
    private var isEnabledPreferenceString: String? = null
    private var packageWhiteList = setOf("android", "com.android.providers.downloads", "com.android.vending", "com.android.systemui")

    override fun onCreate() {
        remindTimer = RemindTimer(applicationContext, notifications)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        isEnabledPreferenceString = applicationContext.getString(R.string.pref_key_enable_dfreminder)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        synchronized(remindTimer!!) {
            if (shouldBeIgnored(sbn, true))
                return

            notifications.add(sbn.packageName)
            remindTimer!!.restartReminderTimer()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        synchronized(remindTimer!!) {
            if (shouldBeIgnored(sbn, false))
                return

            notifications.remove(sbn.packageName)
            if (notifications.isEmpty())
                remindTimer!!.stopReminderTimerIfRunning()
        }
    }

    private fun shouldBeIgnored(sbn: StatusBarNotification, posted: Boolean): Boolean {
        val prefix = if (posted) "New" else "Removed"
        val packageName = sbn.packageName

        if (!sharedPreferences!!.getBoolean(isEnabledPreferenceString, false)) {
            Log.d(DFReminderMainActivity.TAG, "$prefix notification from $packageName: DF reminder disabled")
            return true
        }

        if (packageWhiteList.contains(packageName)) {
            Log.d(DFReminderMainActivity.TAG, "$prefix notification from $packageName ignored")
            return true
        }

        if (!sbn.isClearable) {
            Log.d(DFReminderMainActivity.TAG, "$prefix not clearable notification from $packageName ignored")
            return true
        }

        Log.d(DFReminderMainActivity.TAG, "$prefix notification from $packageName, total: " + notifications.size)

        return false
    }
}
