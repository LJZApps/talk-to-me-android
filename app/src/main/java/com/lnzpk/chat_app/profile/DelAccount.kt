package com.lnzpk.chat_app.profile

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.main.StartIcon
import com.lnzpk.chat_app.newDatabase.DBHelper
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class DelAccount : AppCompatActivity() {
    var username: String? = null
    var password: String? = null
    var database = FirebaseDatabase.getInstance().reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.del_account)

        val db = DBHelper(this, null)

        window.navigationBarColor = Color.BLACK
        username = db.getCurrentUsername()
        val delButton = findViewById<Button>(R.id.deleteButton)
        val usernameIL = findViewById<TextInputLayout>(R.id.delAccUsername)
        val passwordIL = findViewById<TextInputLayout>(R.id.delAccPassword)
        delButton.isEnabled = false
        usernameIL.isEnabled = false
        passwordIL.isEnabled = false
        database.child("users/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("informations/password").exists()) {
                        password = snapshot.child("informations/password").value.toString()
                        delButton.isEnabled = true
                        usernameIL.isEnabled = true
                        passwordIL.isEnabled = true
                        delButton.setOnClickListener { checkData() }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        val toolbar = findViewById<Toolbar>(R.id.delToolbar)
        setSupportActionBar(toolbar)
        Colors.setToolbarColor(this, this, toolbar)
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

    fun checkData() {
        val usernameIL = findViewById<TextInputLayout>(R.id.delAccUsername)
        val passwordIL = findViewById<TextInputLayout>(R.id.delAccPassword)
        val usernameEdit = usernameIL.editText
        val passwordEdit = passwordIL.editText
        val usernameText = usernameEdit!!.text.toString().trim { it <= ' ' }
        val passwordText = passwordEdit!!.text.toString().trim { it <= ' ' }
        var canAgo = true
        usernameIL.error = null
        passwordIL.error = null
        if (usernameText.isEmpty()) {
            canAgo = false
            usernameIL.error = getString(R.string.loginAndRegister_usernameError2)
        } else if (usernameText != username) {
            canAgo = false
            usernameIL.error = getString(R.string.error_usernameNotMatch)
        }
        if (passwordText.isEmpty()) {
            canAgo = false
            passwordIL.error = getString(R.string.loginAndRegister_passwordIsEmpty)
        } else if (passwordText != password) {
            canAgo = false
            passwordIL.error = getString(R.string.loginAndRegister_passwordError3)
        }
        if (canAgo) {
            AlertDialog.Builder(this)
                .setMessage(R.string.delAccount_dialogTitle)
                .setPositiveButton(R.string.delAccount_dialogDelete) { dialog: DialogInterface?, which: Int -> deleteAccount() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun deleteAccount() {
        val dialog = AlertDialog.Builder(this)
        dialog.setView(R.layout.wait)
        val db = DBHelper(this, null)
        val show = dialog.show()
        val reference = FirebaseDatabase.getInstance().getReference("users")
        val username = db.getCurrentUsername()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.child(username!!).ref.removeValue().addOnSuccessListener {
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    val getPic = storageRef.child("profile_pictures/$username.jpg")
                    getPic.downloadUrl.addOnSuccessListener {
                        getPic.delete().addOnSuccessListener {
                            show.cancel()

                            db.setToDeletedProfile(username)
                            val builder2 = AlertDialog.Builder(this@DelAccount)
                            builder2.setTitle(R.string.delAccount_delSuccess)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                    val intent = Intent(this@DelAccount, StartIcon::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                }
                                .show()
                        }
                    }.addOnFailureListener {
                        show.cancel()


                        db.setToDeletedProfile(username)
                        val builder2 = AlertDialog.Builder(this@DelAccount)
                        builder2.setTitle(R.string.delAccount_delSuccess)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                val intent = Intent(this@DelAccount, StartIcon::class.java)
                                startActivity(intent)
                                finishAffinity()
                            }
                            .show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setLightMode() {
        setTheme(R.style.delAccountLight)
    }

    fun setDarkMode() {
        setTheme(R.style.delAccountDark)
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
        }
        return super.onOptionsItemSelected(item)
    }
}