package de.ljz.talktome.old.twoFA

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
import com.google.firebase.database.*
import de.ljz.talktome.R
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TwoFaActivate : AppCompatActivity() {
    private var confirmPassword: Button? = null
    private var codeNext: Button? = null
    private var activateFa: Button? = null
    private var passwordIL: TextInputLayout? = null
    private var firstCodeIL: TextInputLayout? = null
    private var secondCodeIL: TextInputLayout? = null
    private var passwordET: EditText? = null
    private var firstCodeET: EditText? = null
    private var secondCodeET: EditText? = null
    private var textFirst: TextView? = null
    private var textSecond: TextView? = null
    private var toolbar: Toolbar? = null
    private var database: DatabaseReference? = null
    private var preferences: SharedPreferences? = null
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }

        setContentView(R.layout.two_fa_activate)

        // Make window private
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Declare views
        confirmPassword = findViewById(R.id.faConfirmPasswordButton)
        codeNext = findViewById(R.id.faCodeNext)
        activateFa = findViewById(R.id.faActivateButton)
        passwordIL = findViewById(R.id.twoFaActivatePassword)
        firstCodeIL = findViewById(R.id.twoFaFirstCode)
        secondCodeIL = findViewById(R.id.twoFaSecondCode)
        passwordET = passwordIL!!.editText
        firstCodeET = firstCodeIL!!.editText
        secondCodeET = secondCodeIL!!.editText
        textFirst = findViewById(R.id.faTextFirst)
        textSecond = findViewById(R.id.faTextSecond)
        toolbar = findViewById(R.id.twoFaActivateToolbar)
        database = FirebaseDatabase.getInstance().reference
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        username = preferences!!.getString("username", "UNKNOWN")

        // Set color
        setButtonColor(confirmPassword)
        setButtonColor(codeNext)
        setButtonColor(activateFa)
        setToolbarColor(toolbar)

        // Setup views
        confirmPassword!!.visibility = View.VISIBLE
        textFirst!!.visibility = View.GONE
        textSecond!!.visibility = View.GONE
        firstCodeIL!!.visibility = View.GONE
        secondCodeIL!!.visibility = View.GONE
        codeNext!!.visibility = View.GONE
        activateFa!!.visibility = View.GONE
        confirmPassword!!.setOnClickListener { checkPassword() }
        passwordIL!!.requestFocus()
        if (passwordIL!!.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        // Setup toolbar
        setSupportActionBar(toolbar)
    }

    fun checkPassword() {
        confirmPassword!!.isEnabled = false
        passwordIL!!.isEnabled = false
        passwordIL!!.error = null

        database!!.child("users/$username/informations/password")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val password = snapshot.value.toString()
                        val passwordStr = passwordET!!.text.toString().trim { it <= ' ' }
                        if (password == passwordStr) {
                            confirmPassword!!.isEnabled = false
                            passwordIL!!.isEnabled = false
                            passwordET!!.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                            passwordIL!!.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                            passwordIL!!.setEndIconActivated(true)
                            passwordIL!!.isEndIconVisible = false

                            firstCode()
                        } else if (passwordStr.isEmpty()) {
                            confirmPassword!!.isEnabled = true
                            passwordIL!!.isEnabled = true
                            passwordIL!!.error =
                                getString(R.string.loginAndRegister_passwordIsEmpty)
                        } else {
                            confirmPassword!!.isEnabled = true
                            passwordIL!!.isEnabled = true
                            passwordIL!!.error = getString(R.string.error_wrongPassword)
                        }
                    } else {
                        passwordIL!!.error = getString(R.string.startIcon_profileNotFound)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun firstCode() {
        passwordIL!!.clearFocus()
        confirmPassword!!.visibility = View.GONE
        textFirst!!.visibility = View.VISIBLE
        firstCodeIL!!.visibility = View.VISIBLE
        codeNext!!.visibility = View.VISIBLE
        firstCodeIL!!.requestFocus()
        if (firstCodeIL!!.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        codeNext!!.setOnClickListener { v: View? ->
            firstCodeIL!!.error = null
            val codeStr = firstCodeET!!.text.toString().trim { it <= ' ' }
            val codeLength = codeStr.length
            if (codeLength == 6) {
                firstCodeIL!!.error = null
                firstCodeIL!!.isEnabled = false
                firstCodeET!!.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                secondCode(codeStr)
            } else if (codeLength < 6) {
                firstCodeIL!!.error = getString(R.string.codeError_6dig)
            } else if (codeStr.isEmpty()) {
                firstCodeIL!!.error = getString(R.string.codeError_empty)
            }
        }
    }

    fun secondCode(code: String) {
        firstCodeIL!!.clearFocus()
        codeNext!!.visibility = View.GONE
        activateFa!!.visibility = View.VISIBLE
        textSecond!!.visibility = View.VISIBLE
        secondCodeIL!!.visibility = View.VISIBLE
        secondCodeIL!!.requestFocus()
        if (secondCodeIL!!.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        activateFa!!.setOnClickListener { v: View? ->
            secondCodeIL!!.error = null
            val secondCodeStr = secondCodeET!!.text.toString().trim { it <= ' ' }
            if (code == secondCodeStr) {
                secondCodeIL!!.clearFocus()
                secondCodeIL!!.isEnabled = false
                secondCodeET!!.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.faActivate_ask)
                    .setPositiveButton(R.string.faActivate_activate) { dialog: DialogInterface?, which: Int ->
                        activateFa(
                            code
                        )
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(false)
                    .show()
            } else {
                secondCodeIL!!.error = getString(R.string.codeError_noMatch)
            }
        }
    }

    fun activateFa(code: String?) {
        activateFa!!.isEnabled = false
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.wait)
            .setCancelable(false)
        val alert = builder.show()
        database!!.child("users/$username/settings/2fa").setValue("true")
            .addOnSuccessListener { unused: Void? ->
                database!!.child("users/$username/informations/2fa_code").setValue(code)
                    .addOnSuccessListener { unused1: Void? ->
                        alert.cancel()
                        Toast.makeText(this, R.string.faActivate_success, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e: Exception ->
                        alert.cancel()
                        AlertDialog.Builder(this)
                            .setTitle(R.string.error_title)
                            .setMessage(
                                """
    ${getString(R.string.faActivate_error01)}
    
    ${e.message}
    """.trimIndent()
                            )
                            .show()
                    }
            }
            .addOnFailureListener { e: Exception ->
                alert.cancel()
                AlertDialog.Builder(this)
                    .setTitle(R.string.error_title)
                    .setMessage(
                        """
    ${getString(R.string.faActivate_error02)}
    
    ${e.message}
    """.trimIndent()
                    )
                    .show()
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