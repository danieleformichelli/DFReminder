package com.formichelli.dfreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collection;
import java.util.Timer;

public class RemindTimer {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final String frequencyPreferenceString;
    private final Collection<String> notifications;

    private Timer timer;

    public RemindTimer(Context context, Collection<String> notifications) {
        this.context = context;
        this.notifications = notifications;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        frequencyPreferenceString = context.getString(R.string.pref_key_remind_frequency);
    }

    public synchronized void startReminderTimerIfNotRunning() {
        if (timer != null)
            return;

        final long reminderInterval = Integer.valueOf(sharedPreferences.getString(frequencyPreferenceString, "300000"));
        timer = new Timer(false);
        timer.scheduleAtFixedRate(new RemindTask(context, this, reminderInterval), reminderInterval, reminderInterval);
        Log.d(DFReminderMainActivity.TAG, "Started notification timer with interval: " + reminderInterval);
    }

    public synchronized void stopReminderTimerIfRunning() {
        if (timer == null)
            return;

        timer.cancel();
        timer = null;
        Log.d(DFReminderMainActivity.TAG, "Stopped notification timer");
    }

    public synchronized void restartReminderTimer() {
        stopReminderTimerIfRunning();
        startReminderTimerIfNotRunning();
    }

    public synchronized Collection<String> getNotifications() {
        return notifications;
    }
}
