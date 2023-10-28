package com.lnzpk.chat_app.old.friends

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.profile.OpenProfile
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Locale

class SearchUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.search_user)
        val toolbar = findViewById<Toolbar>(R.id.searchUserToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        searchButton = findViewById(R.id.searchUserButton)
        searchInput = findViewById(R.id.searchUserInput)
        setButtonColor(searchButton)
        searchButton!!.setOnClickListener {
            searchUser(searchInput!!.text.toString().trim { it <= ' ' }
                .lowercase(Locale.getDefault()))
        }
    }

    private fun searchUser(search: String?) {
        val searchUserList = findViewById<LinearLayout>(R.id.searchUserList)
        searchUserList.removeAllViews()
        database.child("users").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child("informations/name")
                        .exists() && snapshot.child("settings/showInList").exists()
                ) {
                    val searchUsername = snapshot.key!!.lowercase(Locale.getDefault())
                    val searchName = snapshot.child("informations/name").value.toString().lowercase(
                        Locale.getDefault()
                    )
                    val showInList = snapshot.child("settings/showInList").value.toString()
                    val username = snapshot.key
                    val name = snapshot.child("informations/name").value.toString()
                    if (searchUsername.contains(search!!) && showInList == "true" || searchName.contains(search) && showInList == "true") {
                        val v: View = if (isDarkMode) {
                            LayoutInflater.from(this@SearchUser).inflate(R.layout.chat_preference_design_dark, null, false)
                        } else {
                            LayoutInflater.from(this@SearchUser).inflate(R.layout.chat_preference_design_light, null, false)
                        }
                        val holder = PreferenceHolder()
                        holder.layout = v.findViewById(R.id.testChatDesign)
                        holder.icon = v.findViewById(android.R.id.icon)
                        holder.title = v.findViewById(android.R.id.title)
                        holder.summary = v.findViewById(android.R.id.summary)
                        holder.time = v.findViewById(R.id.timeTextViewChat)
                        holder.unreadMessages = v.findViewById(R.id.unreadChatNumber)
                        holder.verifiedImage = v.findViewById(R.id.verifiedImage)
                        v.tag = holder
                        if (isDarkMode) {
                            holder.layout!!.background = ContextCompat.getDrawable(this@SearchUser, R.drawable.chat_background_dark)
                            holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_dark))
                        } else {
                            holder.layout!!.background = ContextCompat.getDrawable(this@SearchUser, R.drawable.chat_background_light)
                            holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_light))
                        }
                        holder.title!!.text = name
                        holder.summary!!.text = username
                        holder.time!!.visibility = View.GONE
                        holder.unreadMessages!!.visibility = View.INVISIBLE
                        if (snapshot.child("settings/staff").exists()) {
                            val isStaff =
                                java.lang.Boolean.parseBoolean(snapshot.child("settings/staff").value.toString())
                            if (isStaff) {
                                holder.verifiedImage!!.visibility = View.VISIBLE
                            } else {
                                holder.verifiedImage!!.visibility = View.GONE
                            }
                        } else {
                            holder.verifiedImage!!.visibility = View.GONE
                        }
                        v.setOnClickListener {
                            val intent = Intent(this@SearchUser, OpenProfile::class.java)
                            intent.putExtra("username", username)
                                .putExtra("comeFrom", "friends")
                            startActivity(intent)
                            //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
                        }
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(5, 5, 5, 5)
                        holder.layout!!.layoutParams = params
                        searchUserList.addView(v)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private inner class PreferenceHolder {
        var layout: ConstraintLayout? = null
        var icon: ImageView? = null
        var title: TextView? = null
        var summary: TextView? = null
        var time: TextView? = null
        var unreadMessages: TextView? = null
        var verifiedImage: ImageView? = null
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

    fun setButtonColor(button: Button?) {
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
                    button!!.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("buttonColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setLightMode() {
        setTheme(R.style.contactsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.contactsDark)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        //overridePendingTransition(R.anim.fade_out, R.anim.slide_down)
    }

    companion object {
        var searchButton: Button? = null
        var searchInput: EditText? = null
        var database = FirebaseDatabase.getInstance().reference
    }
}