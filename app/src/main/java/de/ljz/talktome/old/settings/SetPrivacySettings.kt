package de.ljz.talktome.old.settings

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.main.StartIcon
import de.ljz.talktome.old.newDatabase.DBHelper

class SetPrivacySettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.settings_activity)
        val db = DBHelper(this, null)
        window.navigationBarColor = Color.BLACK
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.userSettings, privacySettings())
            .commit()
        val toolbar = findViewById<Toolbar>(R.id.dataToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        username = db.getCurrentUsername()
    }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_right)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_right)
    }

    class privacySettings : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.privacy_settings, rootKey)
            checkNetwork()
            val takeBack = findPreference<Preference>("takeConfirmBack")
            takeBack!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference? ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Zustimmung wiederrufen?")
                        .setMessage("Dadurch verlierst du die Rechte, die App zu verwenden.")
                        .setPositiveButton("Wiederrufen") { dialog: DialogInterface?, which: Int -> rejectConfirm() }
                        .setNegativeButton("Abbrechen", null)
                        .show()
                    false
                }
            val privacy = findPreference<Preference>("privacy")
            privacy!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference? ->
                    val intent = Intent(activity, DataProtection::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
            val agb = findPreference<Preference>("agb")
            agb!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference? ->
                    val intent = Intent(activity, AgbScreen::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
            val publicProfile = findPreference<SwitchPreference>("public_profile")
            publicProfile!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    val database = FirebaseDatabase.getInstance().reference
                    val user = preferenceManager.sharedPreferences
                    val showInList = newValue.toString()
                    val username = user!!.getString("username", "UNKNOWN")
                    database.child("users/$username").child("settings/showInList")
                        .setValue(showInList)
                        .addOnSuccessListener { aVoid: Void? ->
                            publicProfile.isChecked = (newValue as Boolean)
                        }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                context,
                                e.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    false
                }
            val publicChat = findPreference<SwitchPreference>("public_chat")
            publicChat!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    val database = FirebaseDatabase.getInstance().reference
                    val user = preferenceManager.sharedPreferences
                    val showInList = newValue.toString()
                    val username = user!!.getString("username", "UNKNOWN")
                    database.child("users/$username").child("settings/messagesAllowed")
                        .setValue(showInList)
                        .addOnSuccessListener { aVoid: Void? ->
                            publicChat.isChecked = (newValue as Boolean)
                        }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                context,
                                e.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    false
                }
            val publicInfo = findPreference<SwitchPreference>("public_info")
            publicInfo!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    val database = FirebaseDatabase.getInstance().reference
                    val user = preferenceManager.sharedPreferences
                    val username = user!!.getString("username", "UNKNOWN")
                    val isPublicInfo = newValue.toString()
                    database.child("users/$username").child("settings/publicInfo")
                        .setValue(isPublicInfo)
                        .addOnSuccessListener { aVoid: Void? ->
                            publicInfo.isChecked = (newValue as Boolean)
                        }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                activity,
                                e.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    false
                }
            val markAsRead = findPreference<SwitchPreference>("markAsRead")
            markAsRead!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    val isMarkAsRead = newValue.toString()
                    database.child("users/" + username + "/settings/markAsRead")
                        .setValue(isMarkAsRead)
                        .addOnSuccessListener { unused: Void? ->
                            markAsRead.isChecked = (newValue as Boolean)
                        }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                activity,
                                e.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    false
                }
            val lastOnline = findPreference<ListPreference>("lastOnline")
            database.child("users/" + username + "/settings/lastOnline")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val lastOnlineValue = snapshot.value.toString()
                            when (lastOnlineValue) {
                                "everyone" -> {
                                    lastOnline!!.setSummary(R.string.lastOnline_everyone)
                                    lastOnline.value = "everyone"
                                }
                                "justFriends" -> {
                                    lastOnline!!.setSummary(R.string.lastOnline_myFriends)
                                    lastOnline.value = "justFriends"
                                }
                                "none" -> {
                                    lastOnline!!.setSummary(R.string.lastOnline_none)
                                    lastOnline.value = "none"
                                }
                            }
                        } else {
                            lastOnline!!.setSummary(R.string.lastOnline_everyone)
                            lastOnline.value = "everyone"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            lastOnline!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    val newLastOnline = newValue.toString()
                    when (newLastOnline) {
                        "everyone" ->                         // EVERYONE
                            database.child("users/" + username + "/settings/lastOnline")
                                .setValue("everyone")
                                .addOnSuccessListener { unused: Void? ->
                                    lastOnline.value = "everyone"
                                    lastOnline.setSummary(R.string.lastOnline_everyone)
                                }
                                .addOnFailureListener { e: Exception ->
                                    Toast.makeText(
                                        activity,
                                        e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        "justFriends" ->                         // JUST FRIENDS
                            database.child("users/" + username + "/settings/lastOnline")
                                .setValue("justFriends")
                                .addOnSuccessListener { unused: Void? ->
                                    lastOnline.value = "justFriends"
                                    lastOnline.setSummary(R.string.lastOnline_myFriends)
                                }
                                .addOnFailureListener { e: Exception ->
                                    Toast.makeText(
                                        activity,
                                        e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        "none" ->                         // NONE
                            database.child("users/" + username + "/settings/lastOnline")
                                .setValue("none")
                                .addOnSuccessListener { unused: Void? ->
                                    lastOnline.value = "none"
                                    lastOnline.setSummary(R.string.lastOnline_none)
                                }
                                .addOnFailureListener { e: Exception ->
                                    Toast.makeText(
                                        activity,
                                        e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                    }
                    false
                }
            val blockedUserPref = findPreference<Preference>("blockedUser")
            blockedNum
            blockedUserPref!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(activity, BlockedUser::class.java)
                    startActivity(intent)
                    //requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
                    false
                }
        }

        private fun rejectConfirm() {
            val preferences = preferenceManager.sharedPreferences
            preferences!!.edit().putBoolean("acceptedData", false).apply()
            val intent = Intent(activity, StartIcon::class.java)
            requireActivity().finishAffinity()
            startActivity(intent)
            //requireActivity().overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
        }

        private val blockedNum: Unit
            get() {
                val preference = findPreference<Preference>("blockedUser")
                database.child("users/$username/blockedUser")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                preference!!.summary = snapshot.childrenCount.toString()
                            } else {
                                preference!!.summary = "0"
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

        fun checkNetwork() {
            val publicChat = findPreference<SwitchPreference>("public_chat")
            val publicUser = findPreference<SwitchPreference>("public_profile")
            val publicInfo = findPreference<SwitchPreference>("public_info")
            val markAsRead = findPreference<SwitchPreference>("markAsRead")
            if (isNetworkConnected) {
                val reference = FirebaseDatabase.getInstance().getReference("users")
                reference.child(username + "/settings")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.child("showInList").exists()) {
                                val publicProfile = snapshot.child("showInList").value as String?
                                val profile = java.lang.Boolean.parseBoolean(publicProfile)
                                publicUser!!.isChecked = profile
                            } else {
                                publicUser!!.isChecked = false
                            }
                            if (snapshot.child("messagesAllowed").exists()) {
                                val publicChat1 = snapshot.child("messagesAllowed").value as String?
                                val chat = java.lang.Boolean.parseBoolean(publicChat1)
                                publicChat!!.isChecked = chat
                            } else {
                                publicChat!!.isChecked = false
                            }
                            if (snapshot.child("publicInfo").exists()) {
                                val isPublicInfo = snapshot.child("publicInfo").value.toString()
                                val info = java.lang.Boolean.parseBoolean(isPublicInfo)
                                publicInfo!!.isChecked = info
                            } else {
                                publicInfo!!.isChecked = false
                            }
                            if (snapshot.child("markAsRead").exists()) {
                                val isMarkAsRead = snapshot.child("markAsRead").value.toString()
                                val markAsRad = java.lang.Boolean.parseBoolean(isMarkAsRead)
                                markAsRead!!.isChecked = markAsRad
                            } else {
                                markAsRead!!.isChecked = false
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(activity, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                val builder = AlertDialog.Builder(requireActivity())
                builder.setTitle("Keine Internetverbindung!")
                    .setMessage("Zum Abrufen der Benutzerdaten wird eine aktive Internetverbindung gebraucht! Bitte stelle eine Internetverbindung her und versuche es erneut!")
                    .setCancelable(false)
                    .setPositiveButton("Wiederholen") { _: DialogInterface?, _: Int -> checkNetwork() }
                    .setNegativeButton("Zurück") { _: DialogInterface?, _: Int ->
                        requireActivity().finish()
                        //requireActivity().overridePendingTransition(R.anim.fade_out, R.anim.slide_out_right)
                    }
                    .show()
            }
        }

        private val isNetworkConnected: Boolean
            private get() {
                val cm =
                    requireActivity().getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
            }

        override fun onResume() {
            super.onResume()
            blockedNum
        }
    }

    companion object {
        var username: String? = null
        var database = FirebaseDatabase.getInstance().reference
    }
}