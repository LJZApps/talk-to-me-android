package com.lnzpk.chat_app.old.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.lnzpk.chat_app.R
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class OtherSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.other_settings)
        val toolbar = findViewById<Toolbar>(R.id.otherSettingsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.otherSettingsFrame, other())
            .commit()
    }

    fun setToolbarColor(toolbar: Toolbar) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false)) {
            try {
                var json: String? = null
                val colorFile: File = if (isDarkMode) {
                    File("$filesDir/colors/darkColors.json")
                } else {
                    File("$filesDir/colors/lightColors.json")
                }
                val `is`: InputStream = FileInputStream(colorFile)
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()
                json = String(buffer)
                val obj = JSONObject(json)
                val jsn = obj.getJSONArray("colors")
                val `object` = JSONObject(jsn.opt(1).toString())
                if (`object`.opt("toolbarColor").toString().toInt() != 0) {
                    toolbar.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("toolbarColor").toString().toInt())
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = `object`.opt("toolbarColor").toString().toInt()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
    }

    val isDarkMode: Boolean
        get() {
            var darkMode = false
            val theme =
                PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")
            if ("system" == theme) {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> darkMode = true
                    Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> darkMode =
                        false
                }
            } else if ("light" == theme) {
                darkMode = false
            } else if ("dark" == theme) {
                darkMode = true
            }
            return darkMode
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_right)
        }
        return super.onOptionsItemSelected(item)
    }

    class other : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.other, rootKey)
            val discord = findPreference<Preference>("discord")
            discord!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference? ->
                    val uri = Uri.parse("https://discord.gg/SJT639ruKv")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                    false
                }
            val aboutTheDev = findPreference<Preference>("about_the_dev")
            aboutTheDev!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                Toast.makeText(requireContext(), "Comming soon!", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    companion object {
        private const val TAG = "otherSettings"
    }
}