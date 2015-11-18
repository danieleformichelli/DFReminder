package com.formichelli.dfreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.TimerTask;

/**
 * Remind notification
 * Created by daniele on 29/10/15.
 */
class RemindTask extends TimerTask {
    private final Context context;
    private final RemindTimer remindTimer;
    private long reminderInterval;
    private final String isEnabledPreferenceString;
    private final String frequencyPreferenceString;
    private final String ringtonePreferenceString;
    private final String vibratePreferenceString;
    private final String vibratePatternPreferenceString;

    public RemindTask(Context context) {
        this(context, null, 0);
    }

    public RemindTask(Context context, RemindTimer remindTimer, long reminderInterval) {
        this.context = context;
        this.remindTimer = remindTimer;
        this.reminderInterval = reminderInterval;
        isEnabledPreferenceString = context.getString(R.string.pref_key_enable_dfreminder);
        frequencyPreferenceString = context.getString(R.string.pref_key_remind_frequency);
        ringtonePreferenceString = context.getString(R.string.pref_key_ringtone);
        vibratePreferenceString = context.getString(R.string.pref_key_vibrate_list);
        vibratePatternPreferenceString = context.getString(R.string.pref_key_vibrate_pattern);
    }

    @Override
    public void run() {
        synchronized (remindTimer) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (remindTimer.getNotifications().size() == 0 || !sharedPreferences.getBoolean(isEnabledPreferenceString, false)) {
                remindTimer.stopReminderTimerIfRunning();
                return;
            }

            StringBuilder notifications = new StringBuilder();
            for (String packageString : remindTimer.getNotifications())
                notifications.append(packageString).append(",");
            Log.d("REMIND_TASK", "Active notifications: " + notifications.toString());

            // ringtone
            final String ringtone = sharedPreferences.getString(ringtonePreferenceString, "DEFAULT_SOUND");
            if (!ringtone.equals("None")) {
                try {
                    final MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                    mediaPlayer.setDataSource(context, Uri.parse(ringtone));
                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.release();
                        }
                    });
                    mediaPlayer.start();
                } catch (Exception e) {
                    // Nothing to do
                }
            }

            // vibrate
            final String vibrate = sharedPreferences.getString(vibratePreferenceString, "Pattern");
            long[] pattern;
            switch (vibrate) {
                case "Once":
                    pattern = new long[2];
                    pattern[0] = 0;
                    pattern[1] = 300;
                    break;

                case "Pattern":
                    final String vibratePattern = sharedPreferences.getString(vibratePatternPreferenceString, "0,200,50,200,50,200");
                    final String[] vibratePatternElements = vibratePattern.split(",");
                    //Set the pattern for vibration
                    pattern = new long[vibratePatternElements.length];
                    for (int i = 0; i < pattern.length; i++)
                        pattern[i] = Integer.valueOf(vibratePatternElements[i].trim());
                    break;

                default:
                    return;
            }

            //Start the vibration
            ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(pattern, -1);

            // modify the interval if necessary
            if (Long.valueOf(sharedPreferences.getString(frequencyPreferenceString, "300000")) != reminderInterval) {
                reminderInterval = Long.valueOf(sharedPreferences.getString(frequencyPreferenceString, "300000"));
                remindTimer.restartReminderTimer();
            }
        }
    }
}