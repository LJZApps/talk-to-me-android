package com.lnzpk.chat_app.old.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.post.BlockedPosts
import com.lnzpk.chat_app.old.post.LikedPosts
import com.lnzpk.chat_app.old.post.MyPosts
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class PostSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.post_settings)
        val toolbar = findViewById<Toolbar>(R.id.postSettingToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.postSettingFrame, PostSettingsLoader())
            .commit()
    }

    fun setToolbarColor(toolbar: Toolbar) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false)) {
            try {
                var json: String?
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

    fun setLightMode() {
        setTheme(R.style.contactsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.contactsDark)
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

    class PostSettingsLoader : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.post_settings, rootKey)
            val yourPosts = findPreference<Preference>("yourPosts")
            yourPosts!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(requireContext(), MyPosts::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
            val hiddenPosts = findPreference<Preference>("hiddenPosts")
            hiddenPosts!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(requireContext(), BlockedPosts::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
            val likedPostsPref = findPreference<Preference>("likedPosts")
            likedPostsPref!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(requireContext(), LikedPosts::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
        }
    }
}