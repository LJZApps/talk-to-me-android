package de.ljz.talktome.old.main

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.ljz.talktome.BuildConfig
import de.ljz.talktome.R
import de.ljz.talktome.databinding.StartIconBinding
import de.ljz.talktome.old.colors.Colors
import de.ljz.talktome.old.newDatabase.DBHelper
import de.ljz.talktome.old.settings.AppAuthentication
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class StartIcon : AppCompatActivity() {
    //var settings: SharedPreferences? = null
    var database = FirebaseDatabase.getInstance().reference
    var next = ""
    var specifyNext = ""
    var statusText: TextView? = null
    val LOG_TAG = "Talk to me - Log data"
    private lateinit var binding: StartIconBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StartIconBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        try {
            if (Colors.isDarkMode(this)) {
                setDarkMode()
            } else {
                setLightMode()
            }
            setContentView(view)
            window.navigationBarColor = Color.TRANSPARENT

            val versionTxt = getString(R.string.startIcon_version) + BuildConfig.VERSION_NAME
            binding.versionText.text = versionTxt

            val db = DBHelper(this, null)

            if (!db.isDataDownloaded()) {
                createTables()
            }

            statusText = findViewById(R.id.startIconStatusText)
            binding.startIconStatusText.text = "Checking intent data..."

            val intent = intent
            val extras = intent.extras

            if (extras != null) {
                if (extras.containsKey("next")) {
                    next = extras.getString("next", "")
                }
                if (extras.containsKey("specifyNext")) {
                    specifyNext = extras.getString("specifyNext", "")
                }
            }

            statusText!!.text = "Checking network connection..."
            if (checkNetwork()) {
                statusText!!.text = "You are online."
                val login = db.isSomeoneLoggedIn()

                statusText!!.text = "Checking login data..."
                if (!login) {
                    statusText!!.text = "You are not logged in.\nRedirecting to login..."
                    val loginIntent = Intent(this@StartIcon, LoginAndRegister::class.java)
                    startActivity(loginIntent)
                    finish()
                    overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
                } else {
                    statusText!!.text = "You are logged in."
                    Log.d(LOG_TAG, db.getCurrentUsername())
                    val username = db.getCurrentUsername()

                    statusText!!.text = "Checking databases..."

                    statusText!!.text = "Checking profile data..."
                    database.child("users/$username")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    statusText!!.text =
                                        "Checking profile data...\nChecking banned data..."
                                    database.child("banned/$username")
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    val builder =
                                                        AlertDialog.Builder(this@StartIcon)
                                                    builder.setTitle(getString(R.string.startIcon_banned))
                                                        .setMessage(
                                                            getString(R.string.startIcon_bannedReason) + snapshot.value.toString() + getString(
                                                                R.string.startIcon_bannedHelp
                                                            )
                                                        )
                                                        .setCancelable(false)
                                                        .show()
                                                } else {
                                                    statusText!!.text =
                                                        "Checking fingerprint authentication..."

                                                    val authTime =
                                                        db.getSettingString("authTime", "instantly")
                                                    val lastLogin = db.getLastOnline(username)
                                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                    try {
                                                        //lastLogin-time
                                                        val cal = Calendar.getInstance()
                                                        cal.time = sdf.parse(lastLogin)
                                                        cal.timeZone = TimeZone.getDefault()
                                                        val year1 = DateFormat.format(
                                                            "yyyy",
                                                            sdf.parse(lastLogin)
                                                        ) as String
                                                        val month1 = DateFormat.format(
                                                            "MM",
                                                            sdf.parse(lastLogin)
                                                        ) as String
                                                        val day1 = DateFormat.format(
                                                            "dd",
                                                            sdf.parse(lastLogin)
                                                        ) as String
                                                        val hour1 = DateFormat.format(
                                                            "HH",
                                                            sdf.parse(lastLogin)
                                                        ) as String
                                                        val min1 = DateFormat.format(
                                                            "mm",
                                                            sdf.parse(lastLogin)
                                                        ) as String

                                                        //Current time
                                                        val rightNow1 = Calendar.getInstance()
                                                        val date3 = rightNow1.time
                                                        val format1 =
                                                            SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                        val formattedDate1 = format1.format(date3)
                                                        val date2 = format1.parse(formattedDate1)
                                                        rightNow1.time = date2
                                                        rightNow1.timeZone = TimeZone.getDefault()
                                                        val year3 = DateFormat.format(
                                                            "yyyy",
                                                            date2
                                                        ) as String
                                                        val month3 =
                                                            DateFormat.format("MM", date2) as String
                                                        val day3 =
                                                            DateFormat.format("dd", date2) as String
                                                        val hour3 =
                                                            DateFormat.format("HH", date2) as String
                                                        val min3 =
                                                            DateFormat.format("mm", date2) as String

                                                        //Check dates and Times
                                                        if ("instantly" == authTime) {
                                                            //instantly lock

                                                            statusText!!.text = "All set!"
                                                            checkAuth()
                                                        } else {
                                                            val b = hour1.toInt() > hour3.toInt()
                                                            if ("1min" == authTime) {
                                                                //1 min lock
                                                                if (year1 == year3) {
                                                                    if (month1 == month3) {
                                                                        if (day1 == day3) {
                                                                            if (hour1 == hour3) {
                                                                                val lookingMin =
                                                                                    min1.toInt() + 1
                                                                                if (min3.toInt() >= lookingMin) {
                                                                                    statusText!!.text =
                                                                                        "All set!"
                                                                                    checkAuth()
                                                                                } else {
                                                                                    statusText!!.text =
                                                                                        "All set!"

                                                                                    val intent =
                                                                                        Intent(
                                                                                            this@StartIcon,
                                                                                            Home::class.java
                                                                                        )
                                                                                    if (next != "") {
                                                                                        intent.putExtra(
                                                                                            "next",
                                                                                            next
                                                                                        )
                                                                                    }
                                                                                    if (specifyNext != "") {
                                                                                        intent.putExtra(
                                                                                            "specifyNext",
                                                                                            specifyNext
                                                                                        )
                                                                                    }
                                                                                    startActivity(
                                                                                        intent
                                                                                    )
                                                                                    finish()
                                                                                    overridePendingTransition(
                                                                                        R.anim.fade_out,
                                                                                        R.anim.fade_in
                                                                                    )
                                                                                }
                                                                            } else if (b) {
                                                                                statusText!!.text =
                                                                                    "All set!"

                                                                                checkAuth()
                                                                            } else {
                                                                                statusText!!.text =
                                                                                    "All set!"

                                                                                checkAuth()
                                                                            }
                                                                        } else {
                                                                            statusText!!.text =
                                                                                "All set!"

                                                                            checkAuth()
                                                                        }
                                                                    } else {
                                                                        statusText!!.text =
                                                                            "All set!"

                                                                        checkAuth()
                                                                    }
                                                                } else {
                                                                    statusText!!.text = "All set!"

                                                                    checkAuth()
                                                                }
                                                            } else if ("10min" == authTime) {
                                                                //10 min lock
                                                                if (year1 == year3) {
                                                                    if (month1 == month3) {
                                                                        if (day1 == day3) {
                                                                            if (hour1 == hour3) {
                                                                                val lookingMin =
                                                                                    min1.toInt() + 10
                                                                                if (min3.toInt() >= lookingMin) {
                                                                                    statusText!!.text =
                                                                                        "All set!"

                                                                                    checkAuth()
                                                                                } else {
                                                                                    statusText!!.text =
                                                                                        "All set!"

                                                                                    val intent =
                                                                                        Intent(
                                                                                            this@StartIcon,
                                                                                            Home::class.java
                                                                                        )
                                                                                    if (next != "") {
                                                                                        intent.putExtra(
                                                                                            "next",
                                                                                            next
                                                                                        )
                                                                                    }
                                                                                    if (specifyNext != "") {
                                                                                        intent.putExtra(
                                                                                            "specifyNext",
                                                                                            specifyNext
                                                                                        )
                                                                                    }
                                                                                    startActivity(
                                                                                        intent
                                                                                    )
                                                                                    finish()
                                                                                    overridePendingTransition(
                                                                                        R.anim.fade_out,
                                                                                        R.anim.fade_in
                                                                                    )
                                                                                }
                                                                            } else if (b) {
                                                                                statusText!!.text =
                                                                                    "All set!"

                                                                                checkAuth()
                                                                            } else {
                                                                                statusText!!.text =
                                                                                    "All set!"

                                                                                checkAuth()
                                                                            }
                                                                        } else {
                                                                            statusText!!.text =
                                                                                "All set!"

                                                                            checkAuth()
                                                                        }
                                                                    } else {
                                                                        statusText!!.text =
                                                                            "All set!"

                                                                        checkAuth()
                                                                    }
                                                                } else {
                                                                    statusText!!.text = "All set!"

                                                                    checkAuth()
                                                                }
                                                            } else if ("custom" == authTime) {
                                                                //custom lock
                                                                if (year1 == year3) {
                                                                    if (month1 == month3) {
                                                                        if (day1 == day3) {
                                                                            if (hour1 == hour3) {
                                                                                val customMin =
                                                                                    db.getSettingString(
                                                                                        "customTime",
                                                                                        "1"
                                                                                    )
                                                                                //settings!!.getString("customTime", "1")
                                                                                val lookingMin =
                                                                                    min1.toInt() + customMin!!.toInt()
                                                                                if (min3.toInt() >= lookingMin) {
                                                                                    statusText!!.text =
                                                                                        "All set!"

                                                                                    checkAuth()
                                                                                } else {
                                                                                    statusText!!.text =
                                                                                        "All set!"

                                                                                    val intent =
                                                                                        Intent(
                                                                                            this@StartIcon,
                                                                                            Home::class.java
                                                                                        )
                                                                                    if (next != "") {
                                                                                        intent.putExtra(
                                                                                            "next",
                                                                                            next
                                                                                        )
                                                                                    }
                                                                                    if (specifyNext != "") {
                                                                                        intent.putExtra(
                                                                                            "specifyNext",
                                                                                            specifyNext
                                                                                        )
                                                                                    }
                                                                                    startActivity(
                                                                                        intent
                                                                                    )
                                                                                    finish()
                                                                                    overridePendingTransition(
                                                                                        R.anim.fade_out,
                                                                                        R.anim.fade_in
                                                                                    )
                                                                                }
                                                                            } else if (b) {
                                                                                statusText!!.text =
                                                                                    "All set!"

                                                                                checkAuth()
                                                                            } else {
                                                                                statusText!!.text =
                                                                                    "All set!"

                                                                                checkAuth()
                                                                            }
                                                                        } else {
                                                                            statusText!!.text =
                                                                                "All set!"

                                                                            checkAuth()
                                                                        }
                                                                    } else {
                                                                        statusText!!.text =
                                                                            "All set!"

                                                                        checkAuth()
                                                                    }
                                                                } else {
                                                                    statusText!!.text = "All set!"

                                                                    checkAuth()
                                                                }
                                                            }
                                                        }
                                                    } catch (e: ParseException) {
                                                        statusText!!.text =
                                                            "An error occurred.\n\n${e.message}"
                                                        Toast.makeText(
                                                            this@StartIcon,
                                                            e.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                } else {
                                    statusText!!.text =
                                        "Profile doesn't exists.\nPlease login in again."
                                    AlertDialog.Builder(this@StartIcon)
                                        .setMessage(getString(R.string.startIcon_profileNotFound))
                                        .setMessage(getString(R.string.startIcon_profileNotFoundHelp))
                                        .setPositiveButton(R.string.startIcon_loginRegister) { _: DialogInterface?, _: Int ->
                                            db.deleteProfile(username)
                                            val intent =
                                                Intent(this@StartIcon, StartIcon::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            } else {
                statusText!!.text =
                    "You are offline.\nPlease restart the app, when you are connected to the network again."
                // offline
            }
        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("Du hast einen Fehler entdeckt!")
                .setCancelable(false)
                .setMessage(e.stackTraceToString())
                .show()
        }
    }

    private fun tellDatabaseUpdater() {
        val builder = AlertDialog.Builder(this@StartIcon)
        builder.setTitle("Datenbank muss aktualisiert werden.")
            .setMessage("Die App läuft mit einer In-App Datenbank. Diese Datenbank wird benötigt, um dir ein besseres App-Erlebnis zu bieten.\nBitte klicke auf \"Update\" um das Datenbank-Update zu starten!")
            .setPositiveButton("Update") { _: DialogInterface?, _: Int ->
                updateDatabase()
            }
            .setCancelable(false)
            .show()
    }

    fun createTables() {
        val db = DBHelper(this, null)
        val writableDB = db.writableDatabase

        try {
            // Preparing tables
            if (!db.tableExists("app_settings")) writableDB.execSQL("CREATE TABLE 'app_settings' ('setting_name' TEXT PRIMARY KEY NOT NULL, 'setting_value' TEXT NOT NULL)")
            if (!db.tableExists("profile_settings")) writableDB.execSQL("CREATE TABLE 'profile_settings' ('username' TEXT NOT NULL, 'setting_name' TEXT NOT NULL, 'setting_value' TEXT NOT NULL)")
            if (!db.tableExists("chats")) writableDB.execSQL("CREATE TABLE 'chats' ('chat_username' TEXT PRIMARY KEY NOT NULL, 'unread' INTEGER DEFAULT 0 NOT NULL, 'archived' INTEGER DEFAULT 0 NOT NULL, 'pinned' INTEGER DEFAULT 0 NOT NULL, 'pin_nr' INTEGER)")
            if (!db.tableExists("profiles")) writableDB.execSQL("CREATE TABLE 'profiles' ('username' TEXT PRIMARY KEY NOT NULL, 'password' TEXT NOT NULL, 'info' TEXT NOT NULL, 'name' TEXT NOT NULL, 'logged_in' INTEGER NOT NULL, 'last_online' TEXT, 'deleted' INTEGER DEFAULT 0 NOT NULL)")
            if (!db.tableExists("notifications")) writableDB.execSQL("CREATE TABLE 'notifications' ('notify_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'notify_type' TEXT NOT NULL, 'notify_text' TEXT NOT NULL)")
            if (!db.tableExists("groups")) writableDB.execSQL("CREATE TABLE 'groups' ('group_id' TEXT PRIMARY KEY NOT NULL, 'unread' INTEGER DEFAULT 0 NOT NULL, 'archived' INTEGER DEFAULT 0 NOT NULL, 'pinned' INTEGER DEFAULT 0 NOT NULL, 'pin_nr' INTEGER)")
            if (!db.tableExists("friends")) writableDB.execSQL("CREATE TABLE 'friends' ('username' TEXT PRIMARY KEY NOT NULL, 'blocked' INTEGER DEFAULT 0 NOT NULL)")
            if (!db.tableExists("requests")) writableDB.execSQL("CREATE TABLE 'requests' ('username' TEXT PRIMARY KEY NOT NULL)")
            if (!db.tableExists("posts")) writableDB.execSQL("CREATE TABLE 'posts' ('post_id' TEXT PRIMARY KEY NOT NULL, 'timestamp' TEXT NOT NULL, 'isAnnouncement' INTEGER DEFAULT 0 NOT NULL, 'public' INTEGER DEFAULT 0 NOT NULL, 'content_text' TEXT NOT NULL, 'content_title' TEXT NOT NULL, 'content_image' BLOB, 'categories' TEXT, 'likes' INTEGER DEFAULT 0)")
            if (!db.tableExists("app_colors_light")) writableDB.execSQL("CREATE TABLE 'app_colors_light' ('color_name' TEXT PRIMARY KEY NOT NULL, 'color_value' TEXT DEFAULT 0 NOT NULL)")
            if (!db.tableExists("app_colors_dark")) writableDB.execSQL("CREATE TABLE 'app_colors_dark' ('color_name' TEXT PRIMARY KEY NOT NULL, 'color_value' TEXT DEFAULT 0 NOT NULL)")
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDatabase() {
        val db = DBHelper(this, null)
        val writableDB = db.writableDatabase
        statusText!!.text = "Preparing update..."

        try {
            // Preparing tables
            if (!db.tableExists("app_settings")) writableDB.execSQL("CREATE TABLE 'app_settings' ('setting_name' TEXT PRIMARY KEY NOT NULL, 'setting_value' TEXT NOT NULL)")
            if (!db.tableExists("profile_settings")) writableDB.execSQL("CREATE TABLE 'profile_settings' ('username' TEXT NOT NULL, 'setting_name' TEXT NOT NULL, 'setting_value' TEXT NOT NULL)")
            if (!db.tableExists("chats")) writableDB.execSQL("CREATE TABLE 'chats' ('chat_username' TEXT PRIMARY KEY NOT NULL, 'unread' INTEGER DEFAULT 0 NOT NULL, 'archived' INTEGER DEFAULT 0 NOT NULL, 'pinned' INTEGER DEFAULT 0 NOT NULL, 'pin_nr' INTEGER)")
            if (!db.tableExists("profiles")) writableDB.execSQL("CREATE TABLE 'profiles' ('username' TEXT PRIMARY KEY NOT NULL, 'password' TEXT NOT NULL, 'info' TEXT NOT NULL, 'name' TEXT NOT NULL, 'logged_in' INTEGER NOT NULL)")
            if (!db.tableExists("notifications")) writableDB.execSQL("CREATE TABLE 'notifications' ('notify_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'notify_type' TEXT NOT NULL, 'notify_text' TEXT NOT NULL)")
            if (!db.tableExists("groups")) writableDB.execSQL("CREATE TABLE 'groups' ('group_id' TEXT PRIMARY KEY NOT NULL, 'unread' INTEGER DEFAULT 0 NOT NULL, 'archived' INTEGER DEFAULT 0 NOT NULL, 'pinned' INTEGER DEFAULT 0 NOT NULL, 'pin_nr' INTEGER)")
            if (!db.tableExists("friends")) writableDB.execSQL("CREATE TABLE 'friends' ('username' TEXT PRIMARY KEY NOT NULL, 'blocked' INTEGER DEFAULT 0 NOT NULL)")
            if (!db.tableExists("requests")) writableDB.execSQL("CREATE TABLE 'requests' ('username' TEXT PRIMARY KEY NOT NULL)")
            if (!db.tableExists("posts")) writableDB.execSQL("CREATE TABLE 'posts' ('post_id' TEXT PRIMARY KEY NOT NULL, 'timestamp' TEXT NOT NULL, 'isAnnouncement' INTEGER DEFAULT 0 NOT NULL, 'public' INTEGER DEFAULT 0 NOT NULL, 'content_text' TEXT NOT NULL, 'content_title' TEXT NOT NULL, 'content_image' BLOB, 'categories' TEXT, 'likes' INTEGER DEFAULT 0)")
            if (!db.tableExists("app_colors_light")) writableDB.execSQL("CREATE TABLE 'app_colors_light' ('color_name' TEXT PRIMARY KEY NOT NULL, 'color_value' TEXT DEFAULT 0 NOT NULL)")
            if (!db.tableExists("app_colors_dark")) writableDB.execSQL("CREATE TABLE 'app_colors_dark' ('color_name' TEXT PRIMARY KEY NOT NULL, 'color_value' TEXT DEFAULT 0 NOT NULL)")

            migrationPrepareDatabase()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun migrationPrepareDatabase() {
        statusText!!.text = "Checking database connection..."
    }

    private fun checkNetwork(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun checkAuth() {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val fingerprint = settings.getBoolean("fingerprint", false)
        val intent: Intent
        if (fingerprint) {
            intent = Intent(this, AppAuthentication::class.java)
            intent.putExtra("auth", "fingerprint")
        } else {
            intent = Intent(this, Home::class.java)
        }
        if (next != "") {
            intent.putExtra("next", next)
        }
        if (specifyNext != "") {
            intent.putExtra("specifyNext", specifyNext)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
    }

    fun setLightMode() {
        setTheme(R.style.startStyleLight)
    }

    fun setDarkMode() {
        setTheme(R.style.startStyleDark)
    }

    fun easterEgg() {
        Toast.makeText(this, "It's a EasterEgg!", Toast.LENGTH_SHORT).show()
    }
}