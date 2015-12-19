package com.formichelli.dfreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Remind notification
 * Created by daniele on 29/10/15.
 */
class RemindTask implements Runnable {
    private final Notifier notifier;
    private final RemindTimer remindTimer;
    private final SharedPreferences sharedPreferences;
    private final String isEnabledPreferenceString;
    private String startDoNotDisturbIntervalString;
    private String endDoNotDisturbIntervalString;

    public RemindTask(Context context, RemindTimer remindTimer) {
        this.remindTimer = remindTimer;
        this.notifier = new Notifier(context);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.isEnabledPreferenceString = context.getString(R.string.pref_key_enable_dfreminder);
        this.startDoNotDisturbIntervalString = context.getString(R.string.pref_key_time_start);
        this.endDoNotDisturbIntervalString = context.getString(R.string.pref_key_time_end);
    }

    @Override
    public void run() {
        Log.e(DFReminderMainActivity.TAG, "REMINDING!");
        synchronized (remindTimer) {
            checkPreferences();

            if (remindTimer.getNotifications().size() == 0) {
                remindTimer.stopReminderTimerIfRunning();
                return;
            }

            notifier.playNotification();
        }
    }

    private void checkPreferences() {
        // check if in do not disturb interval
        final int startDoNotDisturbIntervalTime = getIntTime(startDoNotDisturbIntervalString);
        final int endDoNotDisturbIntervalTime = getIntTime(endDoNotDisturbIntervalString);
        if (startDoNotDisturbIntervalTime != endDoNotDisturbIntervalTime) {
            final boolean isDoNotDisturb = Utils.isInRange(startDoNotDisturbIntervalTime, endDoNotDisturbIntervalTime);
            if (isDoNotDisturb) {
                remindTimer.getNotifications().clear();
                return;
            }
        }

        // check if enabled
        if (!sharedPreferences.getBoolean(isEnabledPreferenceString, false))
            remindTimer.getNotifications().clear();
    }

    private int getIntTime(String preferenceKey) {
        String[] timeParts = sharedPreferences.getString(preferenceKey, "00:00").split(":");
        return Integer.valueOf(timeParts[0]) * 60 + Integer.valueOf(timeParts[1]);
    }
}