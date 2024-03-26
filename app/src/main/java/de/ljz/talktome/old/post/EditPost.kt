package de.ljz.talktome.old.post

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import de.ljz.talktome.R
import de.ljz.talktome.old.newDatabase.DBHelper
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class EditPost : AppCompatActivity() {
    var database: DatabaseReference? = null
    var username: String? = null
    var key: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.edit_post)
        val db = DBHelper(this, null)
        val toolbar = findViewById<Toolbar>(R.id.editPostToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        database = FirebaseDatabase.getInstance().reference
        username = db.getCurrentUsername()
        val intent = intent
        key = intent.extras!!["key"].toString()
        val titleIL = findViewById<TextInputLayout>(R.id.editPostTitle)
        val titleET = titleIL.editText
        val textET = findViewById<EditText>(R.id.editPostText)
        val publish = findViewById<Button>(R.id.editPostUpload)
        setButtonColor(publish)
        val aSwitch = findViewById<SwitchCompat>(R.id.editPostPublicSwitch)
        setSwitchColor(aSwitch)
        val titleMax = findViewById<TextView>(R.id.editPostTitleMax)
        val textMax = findViewById<TextView>(R.id.editPostTextMax)
        val MaxTitleLength = titleET!!.text.toString().trim { it <= ' ' }.length
        titleMax.text = "$MaxTitleLength/40"
        val MaxTextLength = textET.text.toString().trim { it <= ' ' }.length
        textMax.text = "$MaxTextLength/1000"
        typeListener()
        database!!.child("posts/$key").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val message = snapshot.child("text").value.toString()
                val title = snapshot.child("title").value.toString()
                val from = snapshot.child("from").value.toString()
                val date = snapshot.child("date").value.toString()
                val time = snapshot.child("time").value.toString()
                val key = snapshot.key
                val announcement = snapshot.child("isAnnouncement").value.toString()
                val publicPost = snapshot.child("publicPost").value.toString()
                aSwitch.isChecked = java.lang.Boolean.parseBoolean(publicPost)
                titleET.setText(title)
                textET.setText(message)

                publish.setOnClickListener { v: View? ->
                    val publicPost1 = aSwitch.isChecked.toString()
                    publishChanges(
                        titleET.text.toString().trim { it <= ' ' },
                        textET.text.toString().trim { it <= ' ' },
                        from,
                        date,
                        time,
                        announcement,
                        publicPost1
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setSwitchColor(switch1: SwitchCompat) {
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
                val `object` = JSONObject(jsn.opt(5).toString())
                if (`object`.opt("switchColor").toString().toInt() != 0) {
                    switch1.trackTintList =
                        ColorStateList.valueOf(`object`.opt("switchColor").toString().toInt())
                    switch1.thumbTintList =
                        ColorStateList.valueOf(`object`.opt("switchColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setButtonColor(button: Button) {
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
                    button.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("buttonColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setToolbarColor(toolbar: Toolbar) {
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

    fun typeListener() {
        val titleIL = findViewById<TextInputLayout>(R.id.editPostTitle)
        val titleET = titleIL.editText
        val textET = findViewById<EditText>(R.id.editPostText)
        val titleMax = findViewById<TextView>(R.id.editPostTitleMax)
        val textMax = findViewById<TextView>(R.id.editPostTextMax)
        val MaxTitleLength = titleET!!.text.toString().trim { it <= ' ' }.length
        titleMax.text = "$MaxTitleLength/40"
        val MaxTextLength = textET.text.toString().trim { it <= ' ' }.length
        textMax.text = "$MaxTextLength/1000"
        titleET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val MaxTitleLength = s.toString().trim { it <= ' ' }.length
                titleMax.text = "$MaxTitleLength/40"
            }
        })
        textET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val MaxTextLength = s.toString().trim { it <= ' ' }.length
                textMax.text = "$MaxTextLength/1000"
            }
        })
    }

    fun publishChanges(
        title: String?,
        message: String?,
        from: String?,
        date: String?,
        time: String?,
        isAnnouncement: String?,
        publicPost: String?
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.wait)
            .setCancelable(false)
        val alert = builder.show()
        val post = Post(title, message, publicPost, from, date, time, isAnnouncement, "true")
        database!!.child("posts/$key").setValue(post).addOnSuccessListener { aVoid: Void? ->
            alert.cancel()
            Toast.makeText(this@EditPost, "Änderungen gespeichert.", Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@EditPost,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @IgnoreExtraProperties
    class Post(
        var title: String?,
        var text: String?,
        var publicPost: String?,
        var from: String?,
        var date: String?,
        var time: String?,
        var isAnnouncement: String?,
        var edited: String
    )

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