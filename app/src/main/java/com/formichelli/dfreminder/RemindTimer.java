package com.formichelli.dfreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

public class RemindTimer {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final String frequencyPreferenceString;

    private Timer timer;

    public RemindTimer(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        frequencyPreferenceString = context.getString(R.string.pref_key_remind_frequency);
    }

    public synchronized void startReminderTimerIfNotRunning() {
        if (timer != null)
            return;

        final long reminderInterval = Integer.valueOf(sharedPreferences.getString(frequencyPreferenceString, "300000"));
        timer = new Timer(false);
        timer.scheduleAtFixedRate(new RemindTask(context, this, reminderInterval), reminderInterval, reminderInterval);
    }

    public synchronized void stopReminderTimerIfRunning() {
        if (timer == null)
            return;

        timer.cancel();
        timer = null;
    }

    public void restartReminderTimer() {
        stopReminderTimerIfRunning();
        startReminderTimerIfNotRunning();
    }

    /**
     * Remind notification
     * Created by daniele on 29/10/15.
     */
    static class RemindTask extends TimerTask {
        private final Context context;
        private final RemindTimer remindTimer;
        private long reminderInterval;
        private final String isEnabledPreferenceString;
        private final String frequencyPreferenceString;
        private final String ringtonePreferenceString;
        private final String vibratePreferenceString;
        private final String vibratePatternPreferenceString;

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
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (!sharedPreferences.getBoolean(isEnabledPreferenceString, false)) {
                remindTimer.stopReminderTimerIfRunning();
                return;
            }

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
            if (Long.valueOf(sharedPreferences.getString(frequencyPreferenceString, "300000")) != reminderInterval)
                remindTimer.restartReminderTimer();
        }
    }
}
