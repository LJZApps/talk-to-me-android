package com.lnzpk.chat_app.old.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import com.lnzpk.chat_app.old.twoFA.TwoFaCheck
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LoginAndRegister : AppCompatActivity() {
    var preferences: SharedPreferences? = null
    var username: String? = null
    var password: String? = null
    var picData: ByteArray? = null
    //var sqlDatabase: DatabaseManager? = null
    //var dao: DatabaseDao? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            if (isDarkMode) {
                setDarkMode()
            } else {
                setLightMode()
            }
            setContentView(R.layout.start_first)
            window.navigationBarColor = Color.BLACK

            val db = DBHelper(this, null)

            if (!db.getSettingBoolean("acceptedData", false)) {
                val intent = Intent(this, ConfirmDataProtection::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
            }
            val view = findViewById<View>(R.id.startScreen)
            val root = view.rootView
            if (isDarkMode) {
                root.setBackgroundResource(R.drawable.login_register_bg_dark)
            } else {
                root.setBackgroundResource(R.drawable.login_register_bg_light)
            }
        }catch (e: Exception){
            AlertDialog.Builder(this)
                .setTitle("Du hast einen Fehler entdeckt!")
                .setCancelable(false)
                .setMessage(e.stackTraceToString())
                .show()
        }
    }

    fun setLightMode() {
        setTheme(R.style.loginAndRegisterLight)
    }

    fun setDarkMode() {
        setTheme(R.style.loginAndRegisterDark)
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

    fun secondRegister() {
        val inflater = layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.register2, null)
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
        setContentView(view)
        val profilePicture = findViewById<CircleImageView>(R.id.registerProfilePicture)
        profilePicture.setOnClickListener { v: View? -> requestPicture() }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                val imageUri = data!!.data
                val cropLayout = findViewById<ConstraintLayout>(R.id.cropLayout)
                cropLayout.visibility = View.VISIBLE
                val cropImageButton = findViewById<Button>(R.id.cropImageButton)
                cropImageButton.visibility = View.VISIBLE
                val cancelButton = findViewById<Button>(R.id.cancelCroppingImageButton)
                cancelButton.visibility = View.VISIBLE
                cancelButton.setOnClickListener { v: View? ->
                    cropImageButton.visibility = View.GONE
                    cropLayout.visibility = View.GONE
                }
                cropImageButton.setOnClickListener { v: View? ->
                    cropImageButton.visibility = View.GONE
                    cropLayout.visibility = View.GONE
                    val baos = ByteArrayOutputStream()
                    picData = baos.toByteArray()
                    val profilePicture = findViewById<CircleImageView>(R.id.registerProfilePicture)
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginAndRegister, e.message, Toast.LENGTH_LONG).show()
            } catch (e: Error) {
                Toast.makeText(this@LoginAndRegister, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun requestPicture() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
    }

    fun checkFirstData(v: View?) {
        val pass1 = findViewById<TextInputLayout>(R.id.password2)
        val pass2 = findViewById<TextInputLayout>(R.id.password1)
        val user = findViewById<TextInputLayout>(R.id.username)
        val password1 = Objects.requireNonNull(pass1.editText)?.text.toString().trim { it <= ' ' }
        val password2 = Objects.requireNonNull(pass2.editText)?.text.toString().trim { it <= ' ' }
        val username = Objects.requireNonNull(user.editText)?.text.toString().trim { it <= ' ' }
        var canAgo = true
        user.error = null
        pass1.error = null
        pass2.error = null
        if (username.contains(" ") or username.contains(".") or username.contains("#") or username.contains(
                "/"
            )
        ) {
            canAgo = false
            user.error = getString(R.string.loginAndRegister_usernameError1)
        } else if (username.isEmpty()) {
            canAgo = false
            user.error = getString(R.string.loginAndRegister_usernameError2)
        } else if (username == "system") {
            canAgo = false
            user.error = getString(R.string.loginAndRegister_usernameError4)
        } else if (username.contains("ä") or username.contains("ü") or username.contains("ö")) {
            canAgo = false
            user.error = getString(R.string.loginAndRegister_usernameError3)
        }
        if (password1.isEmpty()) {
            canAgo = false
            pass1.error = getString(R.string.loginAndRegister_passwordIsEmpty)
        } else if (password1.contains(" ")) {
            canAgo = false
            pass1.error = getString(R.string.loginAndRegister_noSpaces)
        } else if (password1.length < 8) {
            canAgo = false
            pass1.error = getString(R.string.loginAndRegister_passwordError1)
        }
        if (password2.isEmpty()) {
            canAgo = false
            pass2.error = getString(R.string.loginAndRegister_passwordError2)
        } else if (password1 != password2) {
            canAgo = false
            pass2.error = getString(R.string.loginAndRegister_passwordError3)
        }
        if (canAgo) {
            checkForUsername(username, password2)
        }
    }

    fun checkSecondData(v: View?) {
        val name1 = findViewById<TextInputLayout>(R.id.name)
        val info1 = findViewById<TextInputLayout>(R.id.info)
        val name = name1.editText
        val info = info1.editText
        assert(name != null)
        val nameText = name!!.text.toString().trim { it <= ' ' }
        assert(info != null)
        val infoText = info!!.text.toString().trim { it <= ' ' }
        val infoCount = infoText.length
        val nameCount = nameText.length
        var canAgo = true
        val empty: Boolean
        if (nameCount == 0) {
            canAgo = false
            name1.error = getString(R.string.loginAndRegister_nameEmpty)
        }
        empty = infoCount == 0
        if (canAgo) {
            if (empty) {
                register(username, password, nameText, "string_default_info")
            } else {
                register(username, password, nameText, infoText)
            }
        }
    }

    fun startScreen(v: View?) {
        val inflater = layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.start_first, null)
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
        setContentView(view)
    }

    fun registerScreen(v: View?) {
        val inflater = layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.register, null)
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
        setContentView(view)
    }

    fun loginScreen(v: View?) {
        val inflater = layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.login, null)
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
        setContentView(view)
    }

    fun checkForUsername(username1: String?, password1: String?) {
        val dialog = AlertDialog.Builder(this)
        dialog.setView(R.layout.wait)
            .setCancelable(false)
        val show = dialog.show()
        val reference = FirebaseDatabase.getInstance().getReference("users")
        reference.child(username1!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                show.cancel()
                if (snapshot.exists()) {
                    val username = findViewById<TextInputLayout>(R.id.username)
                    username.error = getString(R.string.loginAndRegister_userExists)
                } else {
                    username = username1
                    password = password1
                    secondRegister()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginAndRegister, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun checkLogin(view: View?) {
        val user = findViewById<TextInputLayout>(R.id.loginUsername)
        val pass = findViewById<TextInputLayout>(R.id.loginPassword)
        val username = Objects.requireNonNull(user.editText)?.text.toString().trim { it <= ' ' }
        val password = Objects.requireNonNull(pass.editText)?.text.toString().trim { it <= ' ' }
        user.error = null
        pass.error = null
        var canAgo = true
        if (username.isEmpty()) {
            canAgo = false
            user.error = getString(R.string.loginAndRegister_enterUsername)
        } else if (username.contains(" ") or username.contains(".") or username.contains("#") or username.contains(
                "/"
            )
        ) {
            canAgo = false
            user.error = getString(R.string.loginAndRegister_usernameError1)
        }
        if (password.isEmpty()) {
            canAgo = false
            pass.error = getString(R.string.loginAndRegister_passwordIsEmpty)
        } else if (password.contains(" ")) {
            canAgo = false
            pass.error = getString(R.string.loginAndRegister_noSpaces)
        }
        if (canAgo) {
            checkLoginData(username, password)
        }
    }

    private fun checkLoginData(username: String, password: String) {
        val reference = FirebaseDatabase.getInstance().getReference("users")
        reference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val user = findViewById<TextInputLayout>(R.id.loginUsername)
                    user.error = getString(R.string.loginAndRegister_userDoesntExists)
                } else {
                    reference.child(username).child("informations/password")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val decryptedPassword = snapshot.value.toString()
                                if (!Objects.requireNonNull(decryptedPassword == password)) {
                                    val pass = findViewById<TextInputLayout>(R.id.loginPassword)
                                    pass.error = getString(R.string.error_wrongPassword)
                                } else {
                                    check2FA(username)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginAndRegister, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun check2FA(username: String) {
        val db = DBHelper(this, null)

        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("settings/2fa").exists()) {
                        db.putSettingString("username", username)
                        val twoFa = Intent(this@LoginAndRegister, TwoFaCheck::class.java)
                        twoFa.putExtra("username", username)
                        startActivity(twoFa)
                        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
                    } else {
                        createTables()

                        // TODO get password and info
                        db.setEveryUserToLoggedOut()
                        db.loginNewUser(username, password = "test", info = "test", "test-shit", true)

                        val home = Intent(this@LoginAndRegister, Home::class.java)
                        startActivity(home)
                        finish()
                        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
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
            if(!db.tableExists("profiles")) writableDB.execSQL("CREATE TABLE 'profiles' ('username' TEXT PRIMARY KEY NOT NULL, 'password' TEXT NOT NULL, 'info' TEXT NOT NULL, 'name' TEXT NOT NULL, 'logged_in' INTEGER NOT NULL, 'last_online' STRING NOT NULL)")
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

    @IgnoreExtraProperties
    class Informations(var name: String, var info: String, var password: String?)

    @IgnoreExtraProperties
    class Settings(
        var showInList: String,
        var messagesAllowed: String,
        var publicInfo: String,
        var markAsRead: String,
        var lastOnline: String
    )

    fun register(username: String?, password: String?, name: String, info: String) {
        val db = DBHelper(this, null)

        val dialog = AlertDialog.Builder(this)
        dialog.setView(R.layout.wait)
            .setCancelable(false)
        val show = dialog.show()
        val mDatabase = FirebaseDatabase.getInstance().reference
        val showInList = "true"
        val messagesAllowed = "true"
        val publicInfo = "true"
        val markAsRead = "true"
        val lastOnline = "everyone"
        val informations = Informations(name, info, password)
        val userSettings = Settings(showInList, messagesAllowed, publicInfo, markAsRead, lastOnline)
        mDatabase.child("users").child("$username/informations").setValue(informations)
            .addOnSuccessListener { unused: Void? ->
                mDatabase.child(
                    "users/$username/settings"
                ).setValue(userSettings).addOnSuccessListener { unused1: Void? ->
                    if (picData != null) {
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        val date = Date()
                        @SuppressLint("SimpleDateFormat") val picDate =
                            SimpleDateFormat("dd.MM.yyyy_HH:mm:ss").format(date)
                        val pb = storageRef.child("profile_pictures/$username.jpg")
                        val metadata = StorageMetadata.Builder()
                            .setCustomMetadata("date", picDate)
                            .build()
                        if (picData!!.size > 6000000) {
                            Toast.makeText(
                                this,
                                getString(R.string.uploadProfilePicTooBig),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val uploadTask = pb.putBytes(picData!!)
                            uploadTask.addOnFailureListener { exception: Exception? ->
                                Toast.makeText(
                                    this@LoginAndRegister,
                                    getString(R.string.uploadProfilePicError),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                                .addOnSuccessListener {
                                    pb.updateMetadata(metadata)
                                        .addOnSuccessListener {
                                            show.cancel()

                                            if (username != null && password != null) {
                                                db.setEveryUserToLoggedOut()
                                                db.loginNewUser(username, password, info, name, true)
                                            }

                                            val home =
                                                Intent(this@LoginAndRegister, Home::class.java)
                                            startActivity(home)
                                            finish()
                                            overridePendingTransition(
                                                R.anim.fade_out,
                                                R.anim.fade_in
                                            )
                                        }
                                }
                        }
                    } else {
                        show.cancel()

                        if (username != null && password != null) {
                            db.setEveryUserToLoggedOut()
                            db.loginNewUser(username, password, info, name, true)
                        }
                        val home = Intent(this@LoginAndRegister, Home::class.java)
                        startActivity(home)
                        finish()
                        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
                    }
                }
            }
    }
}