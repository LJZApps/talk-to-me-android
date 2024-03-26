package de.ljz.talktome.old.settings

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors
import de.ljz.talktome.old.colors.Colors.setButtonColor
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.newDatabase.DBHelper

class ChangePass : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.change_pass)
        window.navigationBarColor = Color.BLACK
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        val toolbar = findViewById<Toolbar>(R.id.changePassToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        val button = findViewById<Button>(R.id.button2)
        setButtonColor(this, button)
        val showPasswords = findViewById<CheckBox>(R.id.showEditPassword)
        showPasswords.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                showPassword()
            } else {
                hidePassword()
            }
        }
        val cNPassword = findViewById<TextInputLayout>(R.id.checkNewPassword)
        val checkNewPassword = cNPassword.editText!!
        checkNewPassword.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT -> checkPasswords(
                    window.decorView.rootView
                )
            }
            false
        }
    }

    fun setLightMode() {
        setTheme(R.style.changePassLight)
    }

    fun setDarkMode() {
        setTheme(R.style.changePassDark)
    }

    fun checkPasswords(view: View?) {
        val oPassword = findViewById<TextInputLayout>(R.id.oldPassword)
        val nPassword = findViewById<TextInputLayout>(R.id.newPassword)
        val cNPassword = findViewById<TextInputLayout>(R.id.checkNewPassword)
        val oldPassword = oPassword.editText
        val newPassword = nPassword.editText
        val checkNewPassword = cNPassword.editText
        oPassword.error = null
        nPassword.error = null
        cNPassword.error = null
        val db = DBHelper(this, null)
        val username = db.getCurrentUsername()
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val currentPasswordText =
                            snapshot.child("informations/password").value.toString()
                        val oldPasswordText = oldPassword!!.text.toString().trim { it <= ' ' }
                        val newPasswordText = newPassword!!.text.toString().trim { it <= ' ' }
                        val checkNewPasswordText =
                            checkNewPassword!!.text.toString().trim { it <= ' ' }
                        if (oldPasswordText.isEmpty() or (oldPasswordText == " ") or (oldPasswordText == "   ")) {
                            oPassword.error = getString(R.string.loginAndRegister_passwordIsEmpty)
                        } else {
                            if (newPasswordText.isEmpty() or (newPasswordText == " ") or (newPasswordText == "   ")) {
                                nPassword.error =
                                    getString(R.string.loginAndRegister_passwordIsEmpty)
                            } else {
                                if (currentPasswordText == oldPasswordText) {
                                    if (newPasswordText == oldPasswordText) {
                                        nPassword.error =
                                            getString(R.string.error_passwordCannotBeSame)
                                    } else {
                                        if (newPasswordText == checkNewPasswordText) {
                                            saveNewPassword(checkNewPasswordText)
                                        } else {
                                            cNPassword.error =
                                                getString(R.string.loginAndRegister_passwordError3)
                                        }
                                    }
                                } else {
                                    oPassword.error = getString(R.string.error_wrongPassword)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun saveNewPassword(newPassword: String) {
        val db = DBHelper(this, null)
        val username = db.getCurrentUsername()
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$username").child("informations/password").setValue(newPassword)
            .addOnSuccessListener {
                Toast.makeText(
                    this@ChangePass,
                    R.string.editPassword_changeSuccess,
                    Toast.LENGTH_SHORT
                ).show()
                db.changeProfilePassword(username, newPassword)
                finish()
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@ChangePass,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun hidePassword() {
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.abeezee)
        val oPassword = findViewById<TextInputLayout>(R.id.oldPassword)
        val nPassword = findViewById<TextInputLayout>(R.id.newPassword)
        val cNPassword = findViewById<TextInputLayout>(R.id.checkNewPassword)
        val oldPassword = oPassword.editText
        val newPassword = nPassword.editText
        val checkNewPassword = cNPassword.editText
        assert(oldPassword != null)
        oldPassword!!.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        assert(newPassword != null)
        newPassword!!.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        assert(checkNewPassword != null)
        checkNewPassword!!.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        oldPassword.typeface = typeface
        newPassword.typeface = typeface
        checkNewPassword.typeface = typeface
    }

    fun showPassword() {
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.abeezee)
        val oPassword = findViewById<TextInputLayout>(R.id.oldPassword)
        val nPassword = findViewById<TextInputLayout>(R.id.newPassword)
        val cNPassword = findViewById<TextInputLayout>(R.id.checkNewPassword)
        val oldPassword = oPassword.editText
        val newPassword = nPassword.editText
        val checkNewPassword = cNPassword.editText
        assert(oldPassword != null)
        oldPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        assert(newPassword != null)
        newPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        assert(checkNewPassword != null)
        checkNewPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        oldPassword.typeface = typeface
        newPassword.typeface = typeface
        checkNewPassword.typeface = typeface
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}