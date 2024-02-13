package com.lnzpk.chat_app.old.settings.profileSettings

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.lnzpk.chat_app.R


class ProfilePictureSettings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }

        setContentView(R.layout.profile_picture_settings)
    }

    fun setLightMode() {
        setTheme(R.style.profileLight)
    }

    fun setDarkMode() {
        setTheme(R.style.profileDark)
    }

    val isDarkMode: Boolean get() {
        var darkMode = false
        when (PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")) {
            "system" -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> darkMode = true
                    Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> darkMode = false
                }
            }
            "light" -> {
                darkMode = false
            }
            "dark" -> {
                darkMode = true
            }
        }
        return darkMode
    }
}