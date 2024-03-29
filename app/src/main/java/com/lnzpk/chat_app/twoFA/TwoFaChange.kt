package com.lnzpk.chat_app.twoFA

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lnzpk.chat_app.R
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TwoFaChange : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var newCodeIL: TextInputLayout? = null
    private var againCodeIL: TextInputLayout? = null
    private var passwordIL: TextInputLayout? = null
    private var newCodeET: EditText? = null
    private var againCodeET: EditText? = null
    private var passwordET: EditText? = null
    private var checkNewCode: Button? = null
    private var confirmCodeChange: Button? = null
    private var changeNewCode2: Button? = null
    private var text1: TextView? = null
    private var text2: TextView? = null
    private var databaseReference = FirebaseDatabase.getInstance().reference
    private var preferences: SharedPreferences? = null
    private var username: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }

        setContentView(R.layout.activity_two_fa_change)

        // Declare views
        newCodeIL = findViewById(R.id.changeNewCode)
        againCodeIL = findViewById(R.id.codeChangeAgain)
        changeNewCode2 = findViewById(R.id.codeChangeNext2)
        passwordIL = findViewById(R.id.codeChangeConfirmPassword)
        newCodeET = newCodeIL!!.getEditText()
        againCodeET = againCodeIL!!.getEditText()
        passwordET = passwordIL!!.getEditText()
        checkNewCode = findViewById(R.id.codeChangeNext)
        confirmCodeChange = findViewById(R.id.codeChangeConfirm)
        text1 = findViewById(R.id.codeChangeText1)
        text2 = findViewById(R.id.codeChangeText2)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        username = preferences!!.getString("username", "UNKNOWN")
        databaseReference.child("users/$username/informations/password")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        password = snapshot.value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // Set colors
        setButtonColor(checkNewCode)
        setButtonColor(confirmCodeChange)

        // Setup views
        text1!!.setVisibility(View.GONE)
        checkNewCode!!.setVisibility(View.VISIBLE)
        confirmCodeChange!!.setVisibility(View.GONE)
        againCodeIL!!.setVisibility(View.GONE)
        newCodeET!!.requestFocus()
        changeNewCode2!!.setVisibility(View.GONE)
        passwordIL!!.setVisibility(View.GONE)
        checkNewCode!!.setOnClickListener { checkNewCode() }
        toolbar = findViewById(R.id.codeChangeToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
    }

    fun checkNewCode() {
        newCodeIL!!.error = null
        checkNewCode!!.isEnabled = false
        newCodeIL!!.isEnabled = false
        databaseReference.child("users/$username/informations/2fa_code")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val oldCode = snapshot.value.toString()
                        val newCode = newCodeET!!.text.toString().trim { it <= ' ' }
                        if (oldCode == newCode) {
                            checkNewCode!!.isEnabled = true
                            newCodeIL!!.isEnabled = true
                            newCodeIL!!.error = getString(R.string.faChange_notSame)
                        } else if (newCode.length < 6) {
                            checkNewCode!!.isEnabled = true
                            newCodeIL!!.isEnabled = true
                            newCodeIL!!.error = getString(R.string.codeError_6dig)
                        } else {
                            newCodeIL!!.clearFocus()
                            newCodeIL!!.isEnabled = false
                            checkNewCode!!.isEnabled = false
                            newCodeET!!.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            toNextCode(newCode)
                        }
                    } else {
                        newCodeIL!!.error = getString(R.string.error_occurred)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun toNextCode(code: String) {
        text1!!.visibility = View.VISIBLE
        checkNewCode!!.visibility = View.GONE
        confirmCodeChange!!.visibility = View.VISIBLE
        againCodeIL!!.visibility = View.VISIBLE
        confirmCodeChange!!.setOnClickListener { v: View? -> checkNewCode(code) }
    }

    fun checkNewCode(code: String) {
        againCodeIL!!.error = null
        val confirmCode = againCodeET!!.text.toString().trim { it <= ' ' }
        if (confirmCode == code) {
            checkPassword(code)
            againCodeIL!!.clearFocus()
            checkNewCode!!.isEnabled = false
            againCodeIL!!.isEnabled = false
            againCodeET!!.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else if (confirmCode.length < 6) {
            againCodeIL!!.error = getString(R.string.codeError_6dig)
        } else {
            againCodeIL!!.error = getString(R.string.codeError_noMatch)
        }
    }

    fun checkPassword(code: String?) {
        passwordET!!.error = null
        val confirmPassword = passwordET!!.text.toString().trim { it <= ' ' }
        if (confirmPassword == password) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.faChange_askChange)
                .setPositiveButton(R.string.faButton_changeCode) { dialog: DialogInterface?, which: Int ->
                    setNewCode(
                        code
                    )
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .show()
            passwordIL!!.clearFocus()
            passwordIL!!.isEnabled = false
            passwordET!!.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else if (confirmPassword.isEmpty()) {
            passwordIL!!.error = getString(R.string.loginAndRegister_passwordIsEmpty)
        } else {
            passwordIL!!.error = getString(R.string.error_wrongPassword)
        }
    }

    fun setNewCode(newCode: String?) {
        checkNewCode!!.isEnabled = false
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.wait)
            .setCancelable(false)
        val alert = builder.show()
        databaseReference.child("users/$username/informations/2fa_code").setValue(newCode)
            .addOnSuccessListener { unused: Void? ->
                alert.cancel()
                Toast.makeText(this, R.string.faChange_success, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e: Exception ->
                alert.cancel()
                Toast.makeText(
                    this, """
     ${getString(R.string.error_occurred)}
     
     ${e.message}
     """.trimIndent(), Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun setButtonColor(button: Button?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
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
                val `object` = JSONObject(jsn.opt(0).toString())
                if (`object`.opt("buttonColor").toString().toInt() != 0) {
                    button!!.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("buttonColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setToolbarColor(toolbar: Toolbar?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
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
                    toolbar!!.backgroundTintList =
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
}