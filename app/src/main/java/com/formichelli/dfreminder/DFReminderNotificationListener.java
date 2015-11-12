package com.formichelli.dfreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Listen for notifications
 * <p/>
 * Created by daniele on 29/10/15.
 */
public class DFReminderNotificationListener extends NotificationListenerService {
    private final Map<String, Collection<Integer>> notifications;
    private RemindTimer reminderTimer;
    private SharedPreferences sharedPreferences;
    private String isEnabledPreferenceString;
    private Set<String> packageWhitelist;

    public DFReminderNotificationListener() {
        notifications = new HashMap<>();
    }

    @Override
    public void onCreate() {
        final Context context = getApplicationContext();
        reminderTimer = new RemindTimer(getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isEnabledPreferenceString = context.getString(R.string.pref_key_enable_dfreminder);
        packageWhitelist = new HashSet<>();
        packageWhitelist.add("android");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        if (packageWhitelist.contains(packageName)) {
            Log.d(DFReminderMainActivity.TAG, "New notification from " + sbn.getPackageName() + " ignored");
            return;
        }

        synchronized (notifications) {
            if (!sharedPreferences.getBoolean(isEnabledPreferenceString, false)) {
                Log.d(DFReminderMainActivity.TAG, "New notification from " + sbn.getPackageName() + ": DF reminder disabled");
                reminderTimer.stopReminderTimerIfRunning();
                notifications.clear();
                return;
            }

            Collection<Integer> packageNotifications = notifications.get(packageName);
            if (packageNotifications == null) {
                packageNotifications = new HashSet<>();
                notifications.put(packageName, packageNotifications);
            }
            packageNotifications.add(sbn.getId());

            Log.d(DFReminderMainActivity.TAG, "New notification from " + sbn.getPackageName() + ", total: " + packageNotifications.size());
            reminderTimer.restartReminderTimer();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        if (!packageWhitelist.contains(packageName)) {
            Log.d(DFReminderMainActivity.TAG, "Removed notification from " + sbn.getPackageName() + " ignored");
            return;
        }

        synchronized (notifications) {
            Collection<Integer> packageNotifications = notifications.get(packageName);
            if (packageNotifications == null)
                return;

            packageNotifications.remove(sbn.getId());
            if (packageNotifications.isEmpty()) {
                notifications.remove(packageName);
                if (notifications.isEmpty())
                    reminderTimer.stopReminderTimerIfRunning();
            }

            Log.d(DFReminderMainActivity.TAG, "Removed notification from " + sbn.getPackageName() + ", total: " + packageNotifications.size());
        }
    }
}
