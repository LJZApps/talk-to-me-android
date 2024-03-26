package de.ljz.talktome.old.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.newDatabase.DBHelper

class DesignSettings : AppCompatActivity() {
    val LOG_TAG = "Talk to me - Design-Log"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.design_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.designFragment, DesignSettingsFragment())
            .commit()
        val toolbar = findViewById<Toolbar>(R.id.designToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
    }

    val appTheme: Unit
        get() {
            val theme =
                PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")
            if ("system" == theme) {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> setDarkMode()
                    Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setLightMode()
                }
            } else if ("light" == theme) {
                setLightMode()
            } else if ("dark" == theme) {
                setDarkMode()
            }
        }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    class DesignSettingsFragment : PreferenceFragmentCompat() {
        val LOG_TAG = "Talk to me - Design-Log"

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.chats_settings, rootKey)

            val db = DBHelper(requireContext(), null)

            val app_theme = findPreference<ListPreference>("app_theme")
            app_theme!!.value = db.getSettingString("app_theme", "system")
            app_theme!!.summary = app_theme.entry
            val settings = preferenceManager.sharedPreferences
            assert(app_theme != null)

            app_theme.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    val themeValue = newValue.toString()

                    val oldTheme = db.getSettingString("app_theme", "system")

                    if (themeValue != oldTheme) {
                        app_theme.value = themeValue
                        app_theme.summary = app_theme.entry

                        db.putSettingString("app_theme", themeValue)

                        requireActivity().recreate()
                    }
                    false
                }

            val background = findPreference<Preference>("chat_background")!!
            background.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, BackgroundSettings::class.java)
                    startActivity(intent)
                    false
                }

            val accent = findPreference<Preference>("accent_color")!!
            accent.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val expertSettings = Intent(activity, AccentColorExpertSettings::class.java)
                    startActivity(expertSettings)
                    false
                }

            val useAccentColors = findPreference<SwitchPreference>("useAccentColors")
            useAccentColors!!.isChecked = db.getSettingBoolean("useAccentColors", false)
            useAccentColors!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    if (!java.lang.Boolean.parseBoolean(newValue.toString())) {

                        db.putSettingBoolean("useAccentColors", false)

                        useAccentColors.isChecked = false
                    } else if (java.lang.Boolean.parseBoolean(newValue.toString())) {

                        db.putSettingBoolean("useAccentColors", true)

                        useAccentColors.isChecked = true
                    }
                    false
                }

            val enterToSendSwitch = findPreference<SwitchPreference>("enterToSendSwitch")
            enterToSendSwitch!!.isChecked = db.getSettingBoolean("enterIsSend", false)
            enterToSendSwitch!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    if (!java.lang.Boolean.parseBoolean(newValue.toString())) {

                        db.putSettingBoolean("enterIsSend", false)

                        enterToSendSwitch.isChecked = false
                    } else if (java.lang.Boolean.parseBoolean(newValue.toString())) {

                        db.putSettingBoolean("enterIsSend", true)

                        enterToSendSwitch.isChecked = true
                    }
                    false
                }
        }
    }
}
