package de.ljz.talktome.old.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors
import de.ljz.talktome.old.colors.Colors.setToolbarColor

class AppSecurityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.app_security)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.appSecurityFrame, AppSecurity())
            .commit()
        val toolbar = findViewById<Toolbar>(R.id.appSecurityToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setLightMode() {
        setTheme(R.style.profileLight)
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
    }
}