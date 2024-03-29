package com.lnzpk.chat_app.settings

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.lnzpk.chat_app.BuildConfig
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.colors.Colors.setToolbarColor

class AppVersionScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.app_version_screen)
        val toolbar = findViewById<Toolbar>(R.id.appInfoToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        val versionVersionName = findViewById<TextView>(R.id.versionVersionName)
        val newVersionName =
            versionVersionName.text.toString().replace("{versionName}", BuildConfig.VERSION_NAME)
        versionVersionName.text = newVersionName
        val versionVersionCode = findViewById<TextView>(R.id.versionVersionCode)
        val newVersionCode = versionVersionCode.text.toString()
            .replace("{versionCode}", BuildConfig.VERSION_CODE.toString())
        versionVersionCode.text = newVersionCode
        val versionBuildType = findViewById<TextView>(R.id.versionBuildType)
        val newBuildType =
            versionBuildType.text.toString().replace("{buildType}", BuildConfig.BUILD_TYPE)
        versionBuildType.text = newBuildType

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
}