package de.ljz.talktome.old.service

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors.setButtonColor
import de.ljz.talktome.old.newDatabase.DBHelper

class AcceptNotifications : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.accept_notifications)
        val db = DBHelper(this, null)
        val accept = findViewById<Button>(R.id.acceptNotificationsButton)
        setButtonColor(this, accept)
        val decline = findViewById<Button>(R.id.declineNotificationsButton)
        setButtonColor(this, decline)
        accept.setOnClickListener {
            val intent = Intent()
            val packageName = packageName
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            db.putSettingString("notificationCheck", "accepted")
            db.putSettingBoolean("acceptedNotifications", true)
            db.putSettingBoolean("notiMessages", true)
            db.putSettingBoolean("notiRequests", true)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        decline.setOnClickListener {
            db.putSettingString("notificationCheck", "declined")
            db.putSettingBoolean("acceptedNotifications", false)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    fun setLightMode() {
        setTheme(R.style.homeLight)
    }

    fun setDarkMode() {
        setTheme(R.style.homeDark)
    }

    val isDarkMode: Boolean
        get() {
            var darkMode = false
            val db = DBHelper(this, null)
            when (db.getSettingString("app_theme", "system")) {
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