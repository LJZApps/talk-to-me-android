package com.lnzpk.chat_app.old.settings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.colors.Colors
import com.lnzpk.chat_app.old.colors.Colors.setToolbarColor

class HelpSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.activity_help_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.helpSettingsFragment, HelpSettingsFragment())
            .commit()
        val toolbar = findViewById<Toolbar>(R.id.helpSettingsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
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

    class HelpSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.help, rootKey)
            val version = findPreference<Preference>("version")
            version!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, AppVersionScreen::class.java)
                    startActivity(intent)
                    false
                }

            val feedback = findPreference<Preference>("feedback")
            feedback!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val settings = Intent(activity, Feedback::class.java)
                    startActivity(settings)
                    false
                }
        }
    }
}