package com.lnzpk.chat_app.old.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.admin.AdminCenter
import com.lnzpk.chat_app.old.colors.Colors
import com.lnzpk.chat_app.old.colors.Colors.setToolbarColor
import com.lnzpk.chat_app.old.customThings.chatPreference
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import com.lnzpk.chat_app.old.profile.Profile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale

class Settings : AppCompatActivity() {
    var darkMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.settings)
        context = baseContext
        window.navigationBarColor = Color.BLACK
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        val toolbar = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        //setToolbarColor(toolbar)
        setToolbarColor(this, this, toolbar)
    }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
        darkMode = false
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
        darkMode = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_right)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (darkMode != Colors.isDarkMode(this)) {
            recreate()
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.main_settings, rootKey)
            val userSettings = findPreference<Preference>("userSettings")!!
            userSettings.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, SetPrivacySettings::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }

            val design = findPreference<Preference>("design")
            design!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, DesignSettings::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }

            val safety = findPreference<Preference>("safety")
            safety!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, SafetySettings::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }

            setProfileData()

            val help = findPreference<Preference>("help")
            help!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, HelpSettings::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }

            val other = findPreference<Preference>("notificationsSettings")
            other!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, NotificationSettings::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }

            val postSettingsPref = findPreference<Preference>("postSettings")
            postSettingsPref!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, PostSettings::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }

            //val adminPref = findPreference<Preference>("admin")
            //adminPref!!.isVisible = false
//            adminPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
//                val intent = Intent(activity, AdminCenter::class.java)
//                startActivity(intent)
//                false
//            }
        }

        fun getProfilePicture(username: String?, preference: chatPreference?) {
            preference!!.setIcon(R.drawable.ic_no_profile_picture)
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val getPic = storageRef.child("profile_pictures/$username.jpg")
            try {
                val picture =
                    File(Companion.context!!.cacheDir.toString() + "/profilePictures/" + username + ".jpg")
                if (picture.exists()) {
                    val is1: InputStream = FileInputStream(picture)
                    val size1 = is1.available()
                    val picBuffer = ByteArray(size1)
                    is1.read(picBuffer)
                    is1.close()
                    val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                    val d: Drawable = BitmapDrawable(resources, bmp)
                    preference.icon = d
                } else {
                    preference.setIcon(R.drawable.ic_no_profile_picture)
                }
            } catch (e: Exception) {
            }
            getPic.downloadUrl.addOnSuccessListener {
                getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                    val date = storageMetadata.getCustomMetadata("date")
                    try {
                        val config =
                            File(Companion.context!!.cacheDir.toString() + "/profilePictures/" + username + ".txt")
                        if (config.exists()) {
                            val `is`: InputStream = FileInputStream(config)
                            val size = `is`.available()
                            val buffer = ByteArray(size)
                            `is`.read(buffer)
                            `is`.close()
                            val lastUpdated = String(buffer)
                            if (lastUpdated != date) {
                                getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                                    val baos = ByteArrayOutputStream()
                                    val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 1, baos)
                                    val d: Drawable =
                                        BitmapDrawable(Companion.context!!.resources, bmp)
                                    preference.icon = d
                                    val myDir =
                                        File(Companion.context!!.cacheDir.toString() + "/profilePictures")
                                    if (!myDir.exists()) {
                                        myDir.mkdir()
                                    }
                                    val config1 = "$username.txt"
                                    val picture = "$username.jpg"
                                    val configFile = File(myDir, config1)
                                    if (configFile.exists()) {
                                        configFile.delete()
                                    }
                                    val pictureFile = File(myDir, picture)
                                    if (pictureFile.exists()) {
                                        pictureFile.delete()
                                    }
                                    getPic.metadata.addOnSuccessListener {
                                        try {
                                            val configS =
                                                FileOutputStream(configFile.absolutePath, true)
                                            configS.flush()
                                            configS.write(date!!.toByteArray())
                                            configS.close()
                                            val pictureS =
                                                FileOutputStream(pictureFile.absolutePath, true)
                                            pictureS.write(bytes1)
                                            pictureS.close()
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                                    .addOnFailureListener {
                                        val myDir = File(
                                            Companion.context!!.cacheDir.toString() + "/profilePictures"
                                        )
                                        val picture = "$username.jpg"
                                        val config1 = "$username.txt"
                                        val pictureFile = File(myDir, picture)
                                        if (pictureFile.exists()) {
                                            pictureFile.delete()
                                            preference.setIcon(R.drawable.ic_no_profile_picture)
                                        }
                                        val configFile = File(myDir, config1)
                                        if (configFile.exists()) {
                                            configFile.delete()
                                        }
                                    }
                            } else {
                                val picture =
                                    File(Companion.context!!.cacheDir.toString() + "/profilePictures/" + username + ".jpg")
                                val is1: InputStream = FileInputStream(picture)
                                val size1 = is1.available()
                                val picBuffer = ByteArray(size1)
                                is1.read(picBuffer)
                                is1.close()
                                val bmp =
                                    BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                                val d: Drawable = BitmapDrawable(resources, bmp)
                                preference.icon = d
                            }
                        } else {
                            getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                                val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                                val d: Drawable = BitmapDrawable(Companion.context!!.resources, bmp)
                                preference.icon = d
                                val myDir =
                                    File(Companion.context!!.cacheDir.toString() + "/profilePictures")
                                if (!myDir.exists()) {
                                    myDir.mkdir()
                                }
                                val config1 = "$username.txt"
                                val picture = "$username.jpg"
                                val configFile = File(myDir, config1)
                                if (configFile.exists()) {
                                    configFile.delete()
                                }
                                val pictureFile = File(myDir, picture)
                                if (pictureFile.exists()) {
                                    pictureFile.delete()
                                }
                                getPic.metadata.addOnSuccessListener {
                                    try {
                                        val configS =
                                            FileOutputStream(configFile.absolutePath, true)
                                        configS.write(date!!.toByteArray())
                                        configS.close()
                                        val pictureS =
                                            FileOutputStream(pictureFile.absolutePath, true)
                                        pictureS.write(bytes1)
                                        pictureS.close()
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                                .addOnFailureListener {
                                    val myDir = File(
                                        Companion.context!!.cacheDir.toString() + "/profilePictures"
                                    )
                                    val picture = "$username.jpg"
                                    val config1 = "$username.txt"
                                    val pictureFile = File(myDir, picture)
                                    if (pictureFile.exists()) {
                                        pictureFile.delete()
                                        preference.setIcon(R.drawable.ic_no_profile_picture)
                                    }
                                    val configFile = File(myDir, config1)
                                    if (configFile.exists()) {
                                        configFile.delete()
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        //Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }.addOnFailureListener {
                val myDir = File(Companion.context!!.cacheDir.toString() + "/profilePictures")
                val picture = "$username.jpg"
                val config1 = "$username.txt"
                val pictureFile = File(myDir, picture)
                if (pictureFile.exists()) {
                    pictureFile.delete()
                    preference.setIcon(R.drawable.ic_no_profile_picture)
                }
                val configFile = File(myDir, config1)
                if (configFile.exists()) {
                    configFile.delete()
                }
            }
        }

        private fun setProfileData() {
            val db = DBHelper(requireContext(), null)

            val profile = findPreference<chatPreference>("profile")
            if (Colors.isDarkMode(preferenceScreen.context)) {
                profile!!.layoutResource = R.layout.chat_preference_design_dark
            } else {
                profile!!.layoutResource = R.layout.chat_preference_design_light
            }
            profile.setIcon(R.drawable.ic_no_profile_picture)
            val database = FirebaseDatabase.getInstance().reference
            val username = db.getCurrentUsername()
            database.child("users/$username")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.child("informations/name").value.toString()
                        val info = snapshot.child("informations/info").value.toString()
                        if (snapshot.child("settings/staff").exists()) {
                            val isStaff =
                                java.lang.Boolean.parseBoolean(snapshot.child("settings/staff").value.toString())
                            if (isStaff) {
                                profile.setVerified(true)
                            }
                        }
                        profile.title = name
                        profile.summary = info
                        getProfilePicture(username, profile)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            profile.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val profile1 = Intent(activity, Profile::class.java)
                    startActivity(profile1)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
        }
    }

    companion object {
        var context: Context? = null
        fun setLocale(activity: Activity, languageCode: String?) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val resources = activity.resources
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}