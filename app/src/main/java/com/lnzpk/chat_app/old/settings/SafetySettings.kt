package com.lnzpk.chat_app.old.settings

import android.Manifest
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.twoFA.TwoFA
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class SafetySettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.safety_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.safetySettingsFrame, SafetySettingsFragment())
            .commit()
        val toolbar = findViewById<Toolbar>(R.id.safetySettingsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
    }

    fun setToolbarColor(toolbar: Toolbar) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false) == true) {
            try {
                val json: String
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

    fun setLightMode() {
        setTheme(R.style.profileLight)
    }

    fun setDarkMode() {
        setTheme(R.style.profileDark)
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

    class SafetySettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.safety_settings, rootKey)
            val appSecurity = findPreference<Preference>("appSecurity")!!
            appSecurity.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, AppSecurityActivity::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
            if(requireContext().getSystemService(FINGERPRINT_SERVICE) != null){
                val fingerprintManager = requireContext().getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
                val keyguardManager = requireContext().getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                if (fingerprintManager != null) {
                    if (!fingerprintManager.isHardwareDetected) {
                        appSecurity.isEnabled = false
                        appSecurity.setSummary(R.string.fingerprint_notAv1)
                    } else {
                        // Checks whether fingerprint permission is set on manifest
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.USE_FINGERPRINT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            //If permission is not set show error message
                            appSecurity.isEnabled = false
                            appSecurity.setSummary(R.string.fingerprint_notAv2)
                        } else {
                            // Check whether at least one fingerprint is registered on your device
                            if (!fingerprintManager.hasEnrolledFingerprints()) {
                                //If no fingerprint is registered show error message
                                appSecurity.isEnabled = false
                                appSecurity.setSummary(R.string.fingerprint_notAv3)
                            } else {
                                // Checks whether lock screen security is enabled or not
                                if (!keyguardManager.isKeyguardSecure) {
                                    //Show error message when screen security is disabled
                                    appSecurity.isEnabled = false
                                    appSecurity.setSummary(R.string.fingerprint_notAv4)
                                }
                            }
                        }
                    }
                } else {
                    appSecurity.isEnabled = false
                    appSecurity.setSummary(R.string.fingerprint_notAv1)
                }
            }else{
                appSecurity.isEnabled = false
                appSecurity.setSummary(R.string.fingerprint_notAv1)
            }
            val twoFAPref = findPreference<Preference>("2fa")!!
            twoFAPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val intent = Intent(activity, TwoFA::class.java)
                startActivity(intent)
                //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                false
            }
        }
    }
}