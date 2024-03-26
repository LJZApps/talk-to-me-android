package de.ljz.talktome.old.twoFA

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
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
import de.ljz.talktome.R
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TwoFaDeactivate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.two_fa_deactivate)
        val toolbar = findViewById<Toolbar>(R.id.twoFaDeactivateToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        setupThings()
    }

    fun setButtonColor(button: Button) {
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
                val `object` = JSONObject(jsn.opt(0).toString())
                if (`object`.opt("buttonColor").toString().toInt() != 0) {
                    button.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("buttonColor").toString().toInt())
                }
            } catch (_: Exception) {
            }
        }
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
            } catch (_: Exception) {
            }
        }
    }

    fun setupThings() {
        val button = findViewById<Button>(R.id.twoFaDeactivateConfirm)
        setButtonColor(button)
        val passwordIL = findViewById<TextInputLayout>(R.id.twoFaDeactivatePassword)
        val passwordEdit = passwordIL.editText
        button.setOnClickListener { v: View? ->
            passwordIL.error = null
            passwordIL.isEnabled = false
            button.isEnabled = false
            val password1 = passwordEdit!!.text.toString().trim { it <= ' ' }
            val database = FirebaseDatabase.getInstance().reference
            val user = PreferenceManager.getDefaultSharedPreferences(this@TwoFaDeactivate)
            val username = user.getString("username", "UNKNOWN")
            database.child("users/$username")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val password2 = snapshot.child("informations/password").value.toString()
                        if (password1 == password2) {
                            button.isEnabled = true
                            val builder = AlertDialog.Builder(this@TwoFaDeactivate)
                            builder.setMessage("2FA deaktivieren?")
                                .setPositiveButton("Deaktivieren") { dialog: DialogInterface?, which: Int -> deactivateFa() }
                                .setNegativeButton("Abbrechen", null)
                                .setCancelable(false)
                                .show()
                        } else {
                            passwordIL.error = getString(R.string.error_wrongPassword)
                            passwordIL.isEnabled = true
                            button.isEnabled = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    fun deactivateFa() {
        val database = FirebaseDatabase.getInstance().reference
        val user = PreferenceManager.getDefaultSharedPreferences(this@TwoFaDeactivate)
        val username = user.getString("username", "UNKNOWN")
        val builder = AlertDialog.Builder(this@TwoFaDeactivate)
        builder.setView(R.layout.wait)
            .setCancelable(false)
        val alert = builder.show()
        database.child("users/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.child("settings/2fa").ref.removeValue()
                        .addOnSuccessListener { aVoid: Void? ->
                            if (snapshot.child("informations/2fa_code").exists()) {
                                snapshot.child("informations/2fa_code").ref.removeValue()
                                    .addOnSuccessListener { unused: Void? ->
                                        alert.cancel()
                                        Toast.makeText(
                                            this@TwoFaDeactivate,
                                            "2FA deaktiviert.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        finish()
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        alert.cancel()
                                        Toast.makeText(
                                            this@TwoFaDeactivate,
                                            "Error:" + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                alert.cancel()
                                Toast.makeText(
                                    this@TwoFaDeactivate,
                                    "2FA deaktiviert.",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        }.addOnFailureListener { e: Exception ->
                            alert.cancel()
                            Toast.makeText(
                                this@TwoFaDeactivate,
                                "Error:" + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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