package de.ljz.talktome.old.post

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors.setButtonColor
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class NewPost : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.new_post)
        window.navigationBarColor = Color.BLACK
        val toolbar = findViewById<Toolbar>(R.id.newPostToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        val button = findViewById<Button>(R.id.button5)
        setButtonColor(this, button)
        val postSwitch = findViewById<SwitchCompat>(R.id.postPublicSwitch)
        setSwitchColor(postSwitch)
        startTypeListener()
    }

    fun setSwitchColor(switch1: SwitchCompat) {
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
                val `object` = JSONObject(jsn.opt(5).toString())
                if (`object`.opt("switchColor").toString().toInt() != 0) {
                    switch1.trackTintList =
                        ColorStateList.valueOf(`object`.opt("switchColor").toString().toInt())
                    switch1.thumbTintList =
                        ColorStateList.valueOf(`object`.opt("switchColor").toString().toInt())
                }
            } catch (_: Exception) {
            }
        }
    }

    fun startTypeListener() {
        val newPostTitleTIL = findViewById<TextInputLayout>(R.id.newPostTitle)
        val newPostTitle = newPostTitleTIL.editText
        val newPostText = findViewById<EditText>(R.id.newPostText)
        val titleMax = findViewById<TextView>(R.id.newPostTitleMax)
        val textMax = findViewById<TextView>(R.id.newPostTextMax)
        val MaxTitleLength = newPostTitle!!.text.toString().trim { it <= ' ' }.length
        titleMax.text = "$MaxTitleLength/40"
        val MaxTextLength = newPostText.text.toString().trim { it <= ' ' }.length
        textMax.text = "$MaxTextLength/1000"
        newPostTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val MaxTitleLength = s.toString().trim { it <= ' ' }.length
                titleMax.text = "$MaxTitleLength/40"
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val MaxTitleLength = s.toString().trim { it <= ' ' }.length
                titleMax.text = "$MaxTitleLength/40"
            }
        })
        newPostText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val MaxTextLength = s.toString().trim { it <= ' ' }.length
                textMax.text = "$MaxTextLength/1000"
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val MaxTextLength = s.toString().trim { it <= ' ' }.length
                textMax.text = "$MaxTextLength/1000"
            }
        })
    }

    fun setLightMode() {
        setTheme(R.style.homeLight)
    }

    fun setDarkMode() {
        setTheme(R.style.homeDark)
    }

    val isDarkMode: Boolean
        get() {
            var darkMode = false
            val theme =
                PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")
            if (("system" == theme)) {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> darkMode = true
                    Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> darkMode =
                        false
                }
            } else if (("light" == theme)) {
                darkMode = false
            } else if (("dark" == theme)) {
                darkMode = true
            }
            return darkMode
        }

    fun checkData() {
        //Declare views
        val postTitleIL = findViewById<TextInputLayout>(R.id.newPostTitle)
        val postTitle = postTitleIL.editText
        val postText = findViewById<EditText>(R.id.newPostText)
        val postSwitch = findViewById<SwitchCompat>(R.id.postPublicSwitch)

        //Get Strings
        val publicPost = postSwitch.isChecked
        val newPostTitle = postTitle!!.text.toString().trim { it <= ' ' }
        val newPostText = postText.text.toString().trim { it <= ' ' }

        //Check data
        if ((newPostTitle == "")) {
            postTitleIL.error = "Darf nicht leer sein!"
        } else if ((newPostText == "")) {
            val toast = Toast.makeText(this, "Der Text darf nicht leer sein!", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        } else {
            createNewPost(newPostTitle, newPostText, publicPost.toString())
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun createNewPost(postTitle: String?, postText: String?, publicPost: String?) {
        val dateFormat = Date()
        val postTime = SimpleDateFormat("HH:mm").format(dateFormat)
        val postDate = SimpleDateFormat("dd.MM.yyyy").format(dateFormat)
        val user = PreferenceManager.getDefaultSharedPreferences(this)
        val postFrom = user.getString("username", "UNKNOWN")
        val announcement = "false"
        val post = Post(postTitle, postText, publicPost, postFrom, postDate, postTime, announcement)
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.wait)
            .setCancelable(false)
        val alert = builder.show()
        val database = FirebaseDatabase.getInstance().reference
        database.child("posts").push().setValue(post).addOnSuccessListener {
            alert.cancel()
            Toast.makeText(this, "Beitrag hochgeladen.", Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@NewPost,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun requestFinish() {
        //Declare inputs
        val postTitleIL = findViewById<TextInputLayout>(R.id.newPostTitle)
        val postTitle = postTitleIL.editText
        val postText = findViewById<EditText>(R.id.newPostText)
        assert(postTitle != null)
        val newPostTitle = postTitle!!.text.toString().trim { it <= ' ' }
        val newPostText = postText.text.toString().trim { it <= ' ' }

        //Check if empty
        if ((newPostTitle == "")) {
            if ((newPostText == "")) {
                finish()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Änderungen verwerfen")
                    .setMessage("Möchtest du die Änderungen verwefen?")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Ja",
                        DialogInterface.OnClickListener { dialog, which -> finish() })
                    .setNeutralButton("Nein", null)
                    .show()
            }
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Änderungen verwerfen")
                .setMessage("Möchtest du die Änderungen verwefen?")
                .setCancelable(false)
                .setPositiveButton("Ja", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        finish()
                    }
                })
                .setNeutralButton("Nein", null)
                .show()
        }
    }

    @IgnoreExtraProperties
    class Post(
        var title: String?,
        var text: String?,
        var publicPost: String?,
        var from: String?,
        var date: String,
        var time: String,
        var isAnnouncement: String
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requestFinish()
        }
        return super.onOptionsItemSelected(item)
    }
}