package com.lnzpk.chat_app.settings

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.newDatabase.DBHelper
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class NotificationSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.notification_settings)
        window.navigationBarColor = Color.BLACK
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.notificationsFragment, notificationsFragment())
            .commit()
        val toolbar = findViewById<Toolbar>(R.id.notificationsSettingsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
    }

    fun setToolbarColor(toolbar: Toolbar) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
                val colorFile: File
                colorFile = if (isDarkMode) {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val window = window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.statusBarColor = `object`.opt("toolbarColor").toString().toInt()
                    }
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_right)
    }

    class notificationsFragment : PreferenceFragmentCompat() {
        var preferences: SharedPreferences? = null
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.notifications_settings, rootKey)

            val db = DBHelper(requireContext(), null)

            preferences = preferenceManager.sharedPreferences
            val getNotification = findPreference<SwitchPreference>("getNotification")
            val notiMessages = findPreference<SwitchPreference>("notiMessages")
            val notiRequests = findPreference<SwitchPreference>("notiRequests")
            //preferences!!.getString("notificationCheck", "notSet")
            when (db.getSettingString("notificationCheck", "notSet")) {
                "accepted" -> {
                    getNotification!!.isChecked = true
                    notiRequests!!.isEnabled = true
                    notiMessages!!.isEnabled = true
                }
                "declined", "notSet" -> {
                    getNotification!!.isChecked = false
                    notiRequests!!.isEnabled = false
                    notiMessages!!.isEnabled = false
                }
            }

            notiMessages!!.isChecked = db.getSettingBoolean("notiMessages", false)
            notiRequests!!.isChecked = db.getSettingBoolean("notiRequests", false)

            if (notiMessages != null) {
                notiMessages.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener{ _, newValue ->
                        val checked = java.lang.Boolean.parseBoolean(newValue.toString())
                        db.putSettingBoolean("notiMessages", checked)
                        notiMessages.isChecked = checked
                        false
                    }
            }

            if (notiRequests != null) {
                notiRequests.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener{ _, newValue ->
                        val checked = java.lang.Boolean.parseBoolean(newValue.toString())
                        db.putSettingBoolean("notiRequests", checked)
                        notiRequests.isChecked = checked
                        false
                    }
            }

            getNotification!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    val checked = java.lang.Boolean.parseBoolean(newValue.toString())
                    if (checked) {
                        notiRequests!!.isEnabled = true
                        notiMessages!!.isEnabled = true
                        getNotification.isChecked = true
                        //preferences!!.edit().putString("notificationCheck", "accepted").apply()
                        db.putSettingString("notificationCheck", "accepted")
                    } else {
                        notiRequests!!.isEnabled = false
                        notiMessages!!.isEnabled = false
                        getNotification.isChecked = false
                        //preferences!!.edit().putString("notificationCheck", "declined").apply()
                        db.putSettingString("notificationCheck", "declined")
                    }
                    false
                }
        }
    }
}