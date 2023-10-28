package com.lnzpk.chat_app.old.twoFA

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lnzpk.chat_app.R
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TwoFA : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }

        setContentView(R.layout.two_f_a)

        val toolbar = findViewById<Toolbar>(R.id.two_fa_toolbar)

        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)

        setupText()
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

    @SuppressLint("SetTextI18n")
    fun setupText() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.twoFaFrame, TwoFaFragment())
            .commit()
        val twoFaStatus = findViewById<TextView>(R.id.two_fa_status)
        val twoFaImage = findViewById<ImageView>(R.id.twoFaImage)
        val explaining = findViewById<TextView>(R.id.twoFa_explaining)
        val database = FirebaseDatabase.getInstance().reference
        val user = PreferenceManager.getDefaultSharedPreferences(this)
        val username = user.getString("username", "UNKNOWN")

        twoFaImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.warning))
        twoFaStatus.setTextColor(Color.GRAY)
        twoFaStatus.setText(R.string.faStatus_notAv)

        database.child("users/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("settings/2fa").exists()) {
                        if (snapshot.child("informations/2fa_code").exists()) {
                            twoFaStatus.setText(R.string.faStatus_enabled)
                            twoFaStatus.setTextColor(getColor(R.color.green))
                            twoFaImage.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@TwoFA,
                                    R.drawable.protect
                                )
                            )
                        } else {
                            twoFaImage.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@TwoFA,
                                    R.drawable.warning
                                )
                            )
                            twoFaStatus.setTextColor(Color.GRAY)
                            twoFaStatus.setText(R.string.faStatus_notAv)
                            explaining.setText(R.string.faStatus_codeNotAv)
                        }
                    } else {
                        twoFaStatus.setText(R.string.faStatus_disabled)
                        twoFaStatus.setTextColor(getColor(R.color.red))
                        twoFaImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@TwoFA,
                                R.drawable.unprotected
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        setupText()
        super.onResume()
    }

    @SuppressLint("NonConstantResourceId")
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
        setTheme(R.style.profileDark)
    }

    val isDarkMode: Boolean
        get() {
            var darkMode = false
            when (PreferenceManager.getDefaultSharedPreferences(this)
                .getString("app_theme", "system")) {
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

    class TwoFaFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.two_fa_buttons, rootKey)

            setupButtons()
        }

        private fun setupButtons() {
            val twoFaStatus = findPreference<Preference>("two_fa_status")
            val twoFaChangePref = findPreference<Preference>("two_fa_change")
            val user = preferenceManager.sharedPreferences
            val username = user!!.getString("username", "UNKNOWN")
            val database = FirebaseDatabase.getInstance().reference

            twoFaStatus!!.isEnabled = false

            database.child("users/$username")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child("settings/2fa")
                                .exists() && snapshot.child("settings/2fa").value.toString() == "true"
                        ) {
                            if (snapshot.child("informations/2fa_code").exists()) {
                                twoFaStatus.setTitle(R.string.faButton_disable)
                                twoFaChangePref!!.isVisible = true
                                twoFaStatus.isEnabled = true
                                twoFaStatus.onPreferenceClickListener =
                                    Preference.OnPreferenceClickListener {
                                        val intent = Intent(activity, TwoFaDeactivate::class.java)
                                        startActivity(intent)
                                        false
                                    }
                                twoFaChangePref.onPreferenceClickListener =
                                    Preference.OnPreferenceClickListener {
                                        val intent = Intent(activity, TwoFaChange::class.java)
                                        startActivity(intent)
                                        false
                                    }
                            } else {
                                twoFaStatus.setTitle(R.string.faButton_disable)
                                twoFaChangePref!!.isVisible = false
                                twoFaStatus.isEnabled = true
                                twoFaStatus.onPreferenceClickListener =
                                    Preference.OnPreferenceClickListener {
                                        val intent = Intent(activity, TwoFaDeactivate::class.java)
                                        startActivity(intent)
                                        false
                                    }
                            }
                        } else {
                            twoFaStatus.setTitle(R.string.faButton_enable)
                            twoFaStatus.isEnabled = true
                            twoFaChangePref!!.isEnabled = false
                            twoFaChangePref.isVisible = false
                            twoFaStatus.onPreferenceClickListener =
                                Preference.OnPreferenceClickListener {
                                    val intent = Intent(activity, TwoFaActivate::class.java)
                                    startActivity(intent)
                                    false
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}