package com.lnzpk.chat_app.old.main

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.colors.Colors
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import com.lnzpk.chat_app.old.settings.AgbScreen
import com.lnzpk.chat_app.old.settings.DataProtection
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class ConfirmDataProtection : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout._confirm_data_protection)

        val db = DBHelper(this, null)

        val protection = findViewById<ImageView>(R.id.protectionImageView)
        if (isDarkMode) {
            protection.setColorFilter(Color.WHITE)
        } else {
            protection.setColorFilter(Color.BLACK)
        }
        val confirmButton = findViewById<Button>(R.id.confirmDataProtectionButton)
        Colors.setButtonColor(this, confirmButton)
        confirmButton.setOnClickListener {
            db.putSettingBoolean("acceptedData", true)
            finish()
        }
        val readText = findViewById<TextView>(R.id.readDataProtection)
        readText.setOnClickListener {
            val intent = Intent(this, DataProtection::class.java)
            startActivity(intent)
        }
        val readAgb = findViewById<TextView>(R.id.readAgb)
        readAgb.setOnClickListener {
            val intent = Intent(this, AgbScreen::class.java)
            startActivity(intent)
        }
    }

    fun setButtonColor(button: Button) {
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
                val `object` = JSONObject(jsn.opt(0).toString())
                if (`object`.opt("buttonColor").toString().toInt() != 0) {
                    button.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("buttonColor").toString().toInt())
                }
            } catch (_: Exception) {
            }
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
            when (PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")) {
                "system" -> {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> darkMode = true
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> darkMode =
                            false
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }
}