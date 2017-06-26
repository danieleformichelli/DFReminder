package com.formichelli.dfreminder

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Vibrator
import android.preference.PreferenceManager


/**
 * Play a sound and vibrates
 * Created by daniele on 19/11/15.
 */
class Notifier(private val context: Context) {
    private val DEFAULT_PATTERN = "0,200,50,200,50,200"
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val ringtonePreferenceString: String = context.getString(R.string.pref_key_ringtone)
    private val vibratePreferenceString: String = context.getString(R.string.pref_key_vibrate_list)
    private val vibratePatternPreferenceString: String = context.getString(R.string.pref_key_vibrate_pattern)

    fun playNotification() {
        // ringtone
        val ringtone = sharedPreferences.getString(ringtonePreferenceString, "DEFAULT_SOUND")

        // ringtone
        if (ringtone != "None") {
            try {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
                mediaPlayer.setDataSource(context, Uri.parse(ringtone))
                mediaPlayer.prepare()
                mediaPlayer.setOnCompletionListener { mediaPlayer.release() }
                mediaPlayer.start()
            } catch (e: Exception) {
                // Nothing to do
            }
        }

        // vibrate pattern
        val vibrate = sharedPreferences.getString(vibratePreferenceString, "Pattern")
        val vibratePattern: LongArray
        when (vibrate) {
            "Once" -> {
                vibratePattern = LongArray(2)
                vibratePattern[0] = 0
                vibratePattern[1] = 300
            }

            "Pattern" -> {
                val vibratePatternString = sharedPreferences.getString(vibratePatternPreferenceString, DEFAULT_PATTERN)
                vibratePattern = vibratePatternString.split(",").map { it.trim().toLong() }.toLongArray()
            }

            else -> return
        }

        // vibration
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(vibratePattern, -1)
    }
}
