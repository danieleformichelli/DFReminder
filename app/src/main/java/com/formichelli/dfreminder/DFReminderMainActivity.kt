package com.formichelli.dfreminder

import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
   * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
   * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class DFReminderMainActivity : AppCompatPreferenceActivity() {
    private var notifier: Notifier? = null
    private var sharedPreferences: SharedPreferences? = null
    private var ringtonePreferenceString: String? = null
    private var vibratePreferenceString: String? = null
    private var vibratePatternPreferenceString: String? = null

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
        val stringValue = value.toString()

        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val listPreference = preference
            val index = listPreference.findIndexOfValue(stringValue)

            // Set the summary to reflect the new value.
            preference.setSummary(if (index >= 0) listPreference.entries[index] else null)
        } else if (preference is RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            if (TextUtils.isEmpty(stringValue)) {
                // Empty values correspond to 'silent' (no ringtone).
                preference.setSummary(R.string.pref_ringtone_silent)

            } else {
                val ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue))

                if (ringtone == null) {
                    // Clear the summary if there was a lookup error.
                    preference.setSummary(null)
                } else {
                    // Set the summary to reflect the new ringtone display
                    // name.
                    val name = ringtone.getTitle(preference.getContext())
                    preference.setSummary(name)
                }
            }

        } else {
            if (preference.key == vibratePatternPreferenceString) {
                // patterna must contain at least 2 numbers and must not contain two consecutive commas
                if (stringValue.contains(",,") || stringValue.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size < 2)
                    return@OnPreferenceChangeListener false
            }

            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.summary = stringValue
        }

        true
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.

     * @see .sBindPreferenceSummaryToValueListener
     */
    private fun bindPreferenceSummaryToValue(preference: Preference) {
        // Set the listener to watch for value changes.
        preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.context)
                        .getString(preference.key, ""))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        addPreferencesFromResource(R.xml.dfreminder)

        notifier = Notifier(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        ringtonePreferenceString = getString(R.string.pref_key_ringtone)
        vibratePreferenceString = getString(R.string.pref_key_vibrate_list)
        vibratePatternPreferenceString = getString(R.string.pref_key_vibrate_pattern)

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_remind_frequency)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ringtone)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_vibrate_list)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_vibrate_pattern)))

        findPreference(getString(R.string.pref_key_try)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            tryNotification()
            false
        }

        if (isFirstRun)
            showInfoDialog()
    }

    private fun tryNotification() {

        // ringtone
        val ringtone = sharedPreferences!!.getString(ringtonePreferenceString, "DEFAULT_SOUND")

        // vibrate pattern
        val vibrate = sharedPreferences!!.getString(vibratePreferenceString, "Pattern")
        val vibratePattern: LongArray
        when (vibrate) {
            "Once" -> {
                vibratePattern = LongArray(2)
                vibratePattern[0] = 0
                vibratePattern[1] = 300
            }

            "Pattern" -> {
                val vibratePatternString = sharedPreferences!!.getString(vibratePatternPreferenceString, "0,200,50,200,50,200")
                val vibratePatternElements = vibratePatternString!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                //Set the pattern for vibration
                vibratePattern = LongArray(vibratePatternElements.size)
                for (i in vibratePattern.indices)
                    vibratePattern[i] = Integer.valueOf(vibratePatternElements[i].trim { it <= ' ' })!!.toLong()
            }

            else -> return
        }

        notifier!!.playNotification()
    }

    private val isFirstRun: Boolean
        get() {
            if (!sharedPreferences!!.getBoolean("firstRun", true))
                return false

            sharedPreferences!!.edit().putBoolean("firstRun", false).apply()
            return true
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.dfreminder_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> {
                showInfoDialog()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showInfoDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.info_title)
                .setMessage(R.string.info_msg)
                .setPositiveButton("OK", null)
                .setNeutralButton(R.string.goto_notification_access) { _, _ -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
                .create()
                .show()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane() = false

    companion object {
        val TAG = "DF Reminder"
    }
}
