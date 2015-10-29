package com.formichelli.dfreminder;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;

/**
 * Listen for notifications
 *
 * Created by daniele on 29/10/15.
 */
public class DFReminderNotificationListener extends NotificationListenerService {
    private final Collection<Integer> activeNotifications;
    private final SharedPreferences sharedPreferences;
    private final String frequencyPreferenceString;
    private Timer reminderTimer;

    public DFReminderNotificationListener() {
        activeNotifications = new HashSet<>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        frequencyPreferenceString = getString(R.string.pref_title_remind_frequency);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (activeNotifications.isEmpty()) {
            startReminderTimer();
        }

        activeNotifications.add(sbn.getId());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        activeNotifications.remove(sbn.getId());

        if (activeNotifications.isEmpty()) {
            stopReminderTimer();
        }
    }

    private void startReminderTimer() {
        if (reminderTimer != null) {
            Log.e("DFReminderListener", "reminderTimer should never be null");
            stopReminderTimer();
        }

        final int reminderInterval = sharedPreferences.getInt(frequencyPreferenceString, 60) * 1000;
        reminderTimer = new Timer(false);
        reminderTimer.scheduleAtFixedRate(new RemindTask(), reminderInterval, reminderInterval);
    }

    private void stopReminderTimer() {
        reminderTimer.cancel();
        reminderTimer = null;
    }

}
