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
 * <p/>
 * Created by daniele on 29/10/15.
 */
public class DFReminderNotificationListener extends NotificationListenerService {
    private final Collection<String> notifications;
    private RemindTimer remindTimer;
    private SharedPreferences sharedPreferences;
    private String isEnabledPreferenceString;
    private Set<String> packageWhiteList;

    public DFReminderNotificationListener() {
        notifications = new HashSet<String>();
    }

    @Override
    public void onCreate() {
        final Context context = getApplicationContext();
        remindTimer = new RemindTimer(getApplicationContext(), notifications);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isEnabledPreferenceString = context.getString(R.string.pref_key_enable_dfreminder);
        packageWhiteList = new HashSet<>();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        synchronized (remindTimer) {
            final String packageName = sbn.getPackageName();
            if (packageWhiteList.contains(packageName)) {
                Log.d(DFReminderMainActivity.TAG, "New notification from " + sbn.getPackageName() + " ignored");
                return;
            }

            if (!sbn.isClearable()) {
                Log.d(DFReminderMainActivity.TAG, "Not clearable notification from " + sbn.getPackageName() + " ignored");
                return;
            }

            if (!sharedPreferences.getBoolean(isEnabledPreferenceString, false)) {
                Log.d(DFReminderMainActivity.TAG, "New notification from " + sbn.getPackageName() + ": DF reminder disabled");
                return;
            }

            notifications.add(sbn.getPackageName());

            Log.d(DFReminderMainActivity.TAG, "New notification from " + sbn.getPackageName() + ", total: " + notifications.size());
            remindTimer.restartReminderTimer();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        synchronized (remindTimer) {
            final String packageName = sbn.getPackageName();
            if (!packageWhiteList.contains(packageName)) {
                Log.d(DFReminderMainActivity.TAG, "Removed notification from " + sbn.getPackageName() + " ignored");
                return;
            }

            if (!sbn.isClearable()) {
                Log.d(DFReminderMainActivity.TAG, "Removed not clearable notification from " + sbn.getPackageName() + " ignored");
                return;
            }

            notifications.remove(packageName);
            Log.d(DFReminderMainActivity.TAG, "Removed notifications from " + sbn.getPackageName());
        }
    }
}
