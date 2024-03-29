package com.lnzpk.chat_app.settings.profileSettings

import android.content.res.Configuration
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.preference.PreferenceManager
import com.lnzpk.chat_app.R
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider


class ProfilePictureSettings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }

        EmojiManager.install(GoogleCompatEmojiProvider(EmojiCompat.init(FontRequestEmojiCompatConfig(this, FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs,
        )).setReplaceAll(true))))

        setContentView(R.layout.profile_picture_settings)

        val rootView = findViewById<ConstraintLayout>(R.id.profilePicSettingsRootView)
        val emojiEditText: EditText = findViewById(R.id.openProfileEmojiBadge2)
        val emojiPopup = EmojiPopup(rootView, emojiEditText)
        emojiEditText.isClickable = false
        emojiEditText.setOnClickListener{

            emojiPopup.toggle() // Toggles visibility of the Popup.
            //emojiPopup.dismiss() // Dismisses the Popup.
            //emojiPopup.isShowing // Returns true when Popup is showing.
        }
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