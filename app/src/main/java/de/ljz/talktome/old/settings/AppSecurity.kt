package de.ljz.talktome.old.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import de.ljz.talktome.R

class AppSecurity : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_auth, rootKey)
        setupPreferences()
    }

    private fun setupPreferences() {
        val settings = preferenceManager.sharedPreferences
        val fingerprint = findPreference<SwitchPreference>("fingerprint_switch")
        val customTime = findPreference<SeekBarPreference>("custom_time")
        val fingerprintTime = findPreference<ListPreference>("fingerprint_time")
        fingerprint!!.isChecked = settings!!.getBoolean("fingerprint", false)
        if (!fingerprint.isChecked) {
            fingerprintTime!!.isEnabled = false
            fingerprintTime.isVisible = false
            customTime!!.isVisible = false
            fingerprintTime.summary = fingerprintTime.entry
        } else {
            fingerprint.isEnabled = true
            fingerprintTime!!.isVisible = true
            val authTime = settings.getString("authTime", "instantly")
            fingerprintTime.value = authTime
            fingerprintTime.summary = fingerprintTime.entry
            when (authTime) {
                "instantly", "10min", "1min" -> customTime!!.isVisible = false
                "custom" -> {
                    customTime!!.isVisible = true
                    val auth = preferenceManager.sharedPreferences!!
                        .edit()
                    val time = settings.getString("customTime", "1")
                    customTime.value = time!!.toInt()
                    customTime.onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                            customTime.value = Integer.valueOf(newValue.toString())
                            auth.putString("customTime", newValue.toString()).apply()
                            false
                        }
                }
            }
            fingerprintTime.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference? ->
                    val authTime1 = settings.getString("authTime", "instantly")
                    fingerprintTime.value = authTime1
                    fingerprintTime.summary = fingerprintTime.entry
                    false
                }
            fingerprintTime.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    val auth = preferenceManager.sharedPreferences!!
                        .edit()
                    val value = newValue.toString()
                    fingerprintTime.value = value
                    fingerprintTime.summary = fingerprintTime.entry
                    if (value == "instantly") {
                        auth.putString("authTime", "instantly").apply()
                        customTime!!.isVisible = false
                    } else if (value == "1min") {
                        auth.putString("authTime", "1min").apply()
                        customTime!!.isVisible = false
                    } else if (value == "10min") {
                        auth.putString("authTime", "10min").apply()
                        customTime!!.isVisible = false
                    } else if (value == "custom") {
                        auth.putString("authTime", "custom").apply()
                        val time = settings.getString("customTime", "1")
                        customTime!!.value = time!!.toInt()
                        customTime.isVisible = true
                        customTime.onPreferenceChangeListener =
                            Preference.OnPreferenceChangeListener { preference1: Preference?, newValue1: Any ->
                                customTime.value = Integer.valueOf(newValue1.toString())
                                auth.putString("customTime", newValue1.toString()).apply()
                                false
                            }
                    }
                    false
                }
        }
        fingerprint.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                val editor = settings.edit()
                fingerprint.isChecked = (newValue as Boolean)
                if (newValue == true) {
                    if (settings.getString("authTime", "instantly") == "custom") {
                        val time = settings.getString("customTime", "1")
                        customTime!!.value = time!!.toInt()
                        customTime.isVisible = true
                        customTime.onPreferenceChangeListener =
                            Preference.OnPreferenceChangeListener { preference12: Preference?, newValue12: Any ->
                                customTime.value = Integer.valueOf(newValue12.toString())
                                editor.putString("customTime", newValue12.toString()).apply()
                                false
                            }
                    }
                    editor.putBoolean("fingerprint", true)
                    fingerprintTime.isEnabled = true
                    fingerprintTime.isVisible = true
                    fingerprintTime.onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { preference13: Preference?, newValue13: Any ->
                            val auth = preferenceManager.sharedPreferences!!
                                .edit()
                            val value = newValue13.toString()
                            fingerprintTime.value = value
                            fingerprintTime.summary = fingerprintTime.entry
                            if (value == "instantly") {
                                auth.putString("authTime", "instantly").apply()
                                customTime!!.isVisible = false
                            } else if (value == "1min") {
                                auth.putString("authTime", "1min").apply()
                                customTime!!.isVisible = false
                            } else if (value == "10min") {
                                auth.putString("authTime", "10min").apply()
                                customTime!!.isVisible = false
                            } else if (value == "custom") {
                                auth.putString("authTime", "custom").apply()
                                customTime!!.isVisible = true
                                val time = settings.getString("customTime", "1")
                                customTime.value = time!!.toInt()
                                customTime.onPreferenceChangeListener =
                                    Preference.OnPreferenceChangeListener { preference131: Preference?, newValue131: Any ->
                                        customTime.value = Integer.valueOf(newValue131.toString())
                                        auth.putString("customTime", newValue131.toString()).apply()
                                        false
                                    }
                            }
                            false
                        }
                } else {
                    editor.putBoolean("fingerprint", false)
                    fingerprintTime.isEnabled = false
                    fingerprintTime.onPreferenceChangeListener = null
                    fingerprintTime.isVisible = false
                    customTime!!.isVisible = false
                }
                editor.apply()
                false
            }
    }
}