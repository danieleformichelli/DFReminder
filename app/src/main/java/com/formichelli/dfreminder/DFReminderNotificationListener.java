package com.formichelli.dfreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Listen for notifications
 * <p>
 * Created by daniele on 29/10/15.
 */
public class DFReminderNotificationListener extends NotificationListenerService {
    private Collection<String> notifications;
    private RemindTimer remindTimer;
    private SharedPreferences sharedPreferences;
    private String isEnabledPreferenceString;
    private Set<String> packageWhiteList;

    @Override
    public void onCreate() {
        final Context context = getApplicationContext();
        notifications = new HashSet<>();
        remindTimer = new RemindTimer(getApplicationContext(), notifications);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isEnabledPreferenceString = context.getString(R.string.pref_key_enable_dfreminder);
        packageWhiteList = new HashSet<>();
        packageWhiteList.add("android");
        packageWhiteList.add("com.android.providers.downloads");
        packageWhiteList.add("com.android.vending");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        synchronized (remindTimer) {
            if (shouldBeIgnored(sbn, true))
                return;

            notifications.add(sbn.getPackageName());
            remindTimer.restartReminderTimer();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        synchronized (remindTimer) {
            if (shouldBeIgnored(sbn, false))
                return;

            notifications.remove(sbn.getPackageName());
            if (notifications.isEmpty())
                remindTimer.stopReminderTimerIfRunning();
        }
    }

    private boolean shouldBeIgnored(StatusBarNotification sbn, boolean posted) {
        final String prefix = posted ? "New" : "Removed";
        final String packageName = sbn.getPackageName();

        if (!sharedPreferences.getBoolean(isEnabledPreferenceString, false)) {
            Log.d(DFReminderMainActivity.TAG, prefix + " notification from " + packageName + ": DF reminder disabled");
            return true;
        }

        if (packageWhiteList.contains(packageName)) {
            Log.d(DFReminderMainActivity.TAG, prefix + " notification from " + packageName + " ignored");
            return true;
        }

        if (!sbn.isClearable()) {
            Log.d(DFReminderMainActivity.TAG, prefix + " not clearable notification from " + packageName + " ignored");
            return true;
        }

        Log.d(DFReminderMainActivity.TAG, prefix + " notification from " + packageName + ", total: " + notifications.size());

        return false;
    }
}
