package com.lnzpk.chat_app.twoFA

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.main.Home
import com.lnzpk.chat_app.newDatabase.DBHelper

class TwoFaCheck : AppCompatActivity() {
    private var codeIL: TextInputLayout? = null
    private var codeET: EditText? = null
    private var checkCode: Button? = null
    private var faCheckBack: Button? = null
    private var database = FirebaseDatabase.getInstance().reference
    private var username: String? = null
    private var preferences: SharedPreferences? = null
    private var checkImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }

        setContentView(R.layout.two_fa_check)

        val db = DBHelper(this, null)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        username = db.getSettingString("username", "UNKNOWN")
        codeIL = findViewById(R.id.twoFaCheckCode)
        codeET = codeIL!!.editText
        checkCode = findViewById(R.id.twoFaCheckCheckCode)
        faCheckBack = findViewById(R.id.faCheckBack)
        checkImage = findViewById(R.id.faCheckImage)
        if (isDarkMode) {
            checkImage!!.setColorFilter(
                ContextCompat.getColor(this, android.R.color.white),
                PorterDuff.Mode.SRC_IN
            )
        }
        faCheckBack!!.setOnClickListener { goBack() }
        checkCode!!.setOnClickListener { checkCode() }
    }

    private fun checkCode() {
        val db = DBHelper(this, null)

        codeIL!!.error = null
        codeIL!!.isEnabled = false
        checkCode!!.isEnabled = false
        database.child("users/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("informations/2fa_code").exists()) {
                        val isCode = snapshot.child("informations/2fa_code").value.toString()
                        val enterCode = codeET!!.text.toString().trim { it <= ' ' }
                        if (enterCode == isCode) {
                            preferences!!.edit().clear()
                            createTables()

                            db.deleteSetting("username")

                            db.setEveryUserToLoggedOut()
                            db.loginNewUser(username!!, snapshot.child("informations/password").value.toString(), snapshot.child("informations/info").value.toString(), snapshot.child("informations/name").value.toString(), true)

                            val home = Intent(this@TwoFaCheck, Home::class.java)
                            startActivity(home)
                            finishAffinity()
                        } else if (enterCode.length < 6) {
                            codeIL!!.isEnabled = true
                            checkCode!!.isEnabled = true
                            codeIL!!.error = getString(R.string.codeError_6dig)
                        } else {
                            codeIL!!.isEnabled = true
                            checkCode!!.isEnabled = true
                            codeIL!!.error = getString(R.string.faCheck_wrongCode)
                        }
                    } else {
                        codeIL!!.error = "Code not found."
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun createTables(){
        val db = DBHelper(this, null)
        val writableDB = db.writableDatabase

        try{
            // Preparing tables
            if(!db.tableExists("app_settings")) writableDB.execSQL("CREATE TABLE 'app_settings' ('setting_name' TEXT PRIMARY KEY NOT NULL, 'setting_value' TEXT NOT NULL)")
            if(!db.tableExists("profile_settings")) writableDB.execSQL("CREATE TABLE 'profile_settings' ('username' TEXT NOT NULL, 'setting_name' TEXT NOT NULL, 'setting_value' TEXT NOT NULL)")
            if(!db.tableExists("chats")) writableDB.execSQL("CREATE TABLE 'chats' ('chat_username' TEXT PRIMARY KEY NOT NULL, 'unread' INTEGER DEFAULT 0 NOT NULL, 'archived' INTEGER DEFAULT 0 NOT NULL, 'pinned' INTEGER DEFAULT 0 NOT NULL, 'pin_nr' INTEGER)")
            if(!db.tableExists("profiles")) writableDB.execSQL("CREATE TABLE 'profiles' ('username' TEXT PRIMARY KEY NOT NULL, 'password' TEXT NOT NULL, 'info' TEXT NOT NULL, 'name' TEXT NOT NULL, 'logged_in' INTEGER NOT NULL)")
            if(!db.tableExists("notifications")) writableDB.execSQL("CREATE TABLE 'notifications' ('notify_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'notify_type' TEXT NOT NULL, 'notify_text' TEXT NOT NULL)")
            if(!db.tableExists("groups")) writableDB.execSQL("CREATE TABLE 'groups' ('group_id' TEXT PRIMARY KEY NOT NULL, 'unread' INTEGER DEFAULT 0 NOT NULL, 'archived' INTEGER DEFAULT 0 NOT NULL, 'pinned' INTEGER DEFAULT 0 NOT NULL, 'pin_nr' INTEGER)")
            if(!db.tableExists("friends")) writableDB.execSQL("CREATE TABLE 'friends' ('username' TEXT PRIMARY KEY NOT NULL, 'blocked' INTEGER DEFAULT 0 NOT NULL)")
            if(!db.tableExists("requests")) writableDB.execSQL("CREATE TABLE 'requests' ('username' TEXT PRIMARY KEY NOT NULL)")
            if(!db.tableExists("posts")) writableDB.execSQL("CREATE TABLE 'posts' ('post_id' TEXT PRIMARY KEY NOT NULL, 'timestamp' TEXT NOT NULL, 'isAnnouncement' INTEGER DEFAULT 0 NOT NULL, 'public' INTEGER DEFAULT 0 NOT NULL, 'content_text' TEXT NOT NULL, 'content_title' TEXT NOT NULL, 'content_image' BLOB, 'categories' TEXT, 'likes' INTEGER DEFAULT 0)")
            if(!db.tableExists("app_colors_light")) writableDB.execSQL("CREATE TABLE 'app_colors_light' ('color_name' TEXT PRIMARY KEY NOT NULL, 'color_value' TEXT DEFAULT 0 NOT NULL)")
            if(!db.tableExists("app_colors_dark")) writableDB.execSQL("CREATE TABLE 'app_colors_dark' ('color_name' TEXT PRIMARY KEY NOT NULL, 'color_value' TEXT DEFAULT 0 NOT NULL)")
        }catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goBack() {
        finish()
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