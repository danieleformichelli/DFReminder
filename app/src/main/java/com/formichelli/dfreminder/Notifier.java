package com.formichelli.dfreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

/**
 * Play a sound and vibrates
 * Created by daniele on 19/11/15.
 */
public class Notifier {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final String ringtonePreferenceString;
    private final String vibratePreferenceString;
    private final String vibratePatternPreferenceString;

    public Notifier(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ringtonePreferenceString = context.getString(R.string.pref_key_ringtone);
        vibratePreferenceString = context.getString(R.string.pref_key_vibrate_list);
        vibratePatternPreferenceString = context.getString(R.string.pref_key_vibrate_pattern);
    }

    public void playNotification() {

        // ringtone
        final String ringtone = sharedPreferences.getString(ringtonePreferenceString, "DEFAULT_SOUND");

        // ringtone
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

        // vibrate pattern
        final String vibrate = sharedPreferences.getString(vibratePreferenceString, "Pattern");
        long[] vibratePattern;
        switch (vibrate) {
            case "Once":
                vibratePattern = new long[2];
                vibratePattern[0] = 0;
                vibratePattern[1] = 300;
                break;

            case "Pattern":
                final String vibratePatternString = sharedPreferences.getString(vibratePatternPreferenceString, "0,200,50,200,50,200");
                final String[] vibratePatternElements = vibratePatternString.split(",");
                //Set the pattern for vibration
                vibratePattern = new long[vibratePatternElements.length];
                for (int i = 0; i < vibratePattern.length; i++)
                    vibratePattern[i] = Integer.valueOf(vibratePatternElements[i].trim());
                break;

            default:
                return;
        }

        // vibration
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(vibratePattern, -1);
    }
}
