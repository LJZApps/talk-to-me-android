package com.lnzpk.chat_app.post

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.newDatabase.DBHelper
import com.lnzpk.chat_app.profile.OpenProfile
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class OpenPost : AppCompatActivity() {
    var key: String? = null
    var database: DatabaseReference? = null
    var username: String? = null
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.open_post)
        val db = DBHelper(this, null)
        val toolbar = findViewById<Toolbar>(R.id.openFeedToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        feed()
        username = db.getCurrentUsername()
        database = FirebaseDatabase.getInstance().reference
        val postItems = findViewById<BottomNavigationView>(R.id.postItems)
        val likeItem = findViewById<BottomNavigationItemView>(R.id.like)
        database!!.child("posts/$key/social")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    likeItem.setTitle(snapshot.child("likes").childrenCount.toString())
                    if (snapshot.child("likes/$username").exists()) {
                        likeItem.setIcon(ContextCompat.getDrawable(this@OpenPost, R.drawable.liked))
                    } else {
                        likeItem.setIcon(
                            ContextCompat.getDrawable(
                                this@OpenPost,
                                R.drawable.not_liked
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        if (isDarkMode) {
            postItems.background = ContextCompat.getDrawable(this, R.drawable.nav_bar_dark)
        } else {
            postItems.background = ContextCompat.getDrawable(this, R.drawable.nav_bar_light)
        }
        setNavBarColor(postItems)
        postItems.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.block_post -> blockPost()
                R.id.comments -> {
                    val intent = Intent(this@OpenPost, PostComments::class.java)
                    intent.putExtra("key", key)
                    startActivity(intent)
                }
                R.id.like -> like()
            }
            false
        }
    }

    private fun setNavBarColor(navigationView: BottomNavigationView) {
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
                val `object` = JSONObject(jsn.opt(6).toString())
                if (`object`.opt("navigationItemColor").toString().toInt() != 0) {
                    navigationView.itemTextColor = ColorStateList.valueOf(
                        `object`.opt("navigationItemColor").toString().toInt()
                    )
                    navigationView.itemIconTintList = ColorStateList.valueOf(
                        `object`.opt("navigationItemColor").toString().toInt()
                    )
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

    @SuppressLint("RestrictedApi")
    fun like() {
        val likeItem = findViewById<BottomNavigationItemView>(R.id.like)
        database!!.child("posts/$key/social")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("likes").child(username!!).exists()) {
                        snapshot.child("likes").child(username!!).ref.removeValue()
                            .addOnSuccessListener { unused: Void? ->
                                database!!.child(
                                    "posts/$key/social"
                                ).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        likeItem.setIcon(
                                            ContextCompat.getDrawable(
                                                this@OpenPost,
                                                R.drawable.not_liked
                                            )
                                        )
                                        likeItem.setTitle(snapshot.child("likes").childrenCount.toString())
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                    } else {
                        database!!.child("posts/$key/social/likes/$username").setValue("1")
                            .addOnSuccessListener { unused: Void? ->
                                likeItem.setIcon(
                                    ContextCompat.getDrawable(
                                        this@OpenPost,
                                        R.drawable.liked
                                    )
                                )
                                database!!.child("posts/$key/social")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            likeItem.setTitle(snapshot.child("likes").childrenCount.toString())
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    @SuppressLint("RestrictedApi")
    fun dislike() {
        val likeItem = findViewById<BottomNavigationItemView>(R.id.like)
        database!!.child("posts/$key/social")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("dislikes").child(username!!).exists()) {
                        snapshot.child("dislikes").child(username!!).ref.removeValue()
                            .addOnSuccessListener { unused: Void? ->
                                database!!.child(
                                    "posts/$key/social"
                                ).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        likeItem.setTitle(snapshot.child("likes").childrenCount.toString())
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                    } else {
                        database!!.child("posts/$key/social/dislikes/$username").setValue("1")
                            .addOnSuccessListener { unused: Void? ->
                                database!!.child(
                                    "posts/$key/social"
                                ).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        likeItem.setTitle(snapshot.child("likes").childrenCount.toString())
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                        if (snapshot.child("likes/$username").exists()) {
                            snapshot.child("likes/$username").ref.removeValue()
                                .addOnSuccessListener { unused: Void? ->
                                    likeItem.setIcon(
                                        ContextCompat.getDrawable(
                                            this@OpenPost,
                                            R.drawable.liked
                                        )
                                    )
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun blockPost() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Beiträge verbergen")
            .setMessage("Dir werden keine Beiträge mehr von diesem Benutzer angezeigt. Möchtest du fortfahren?")
            .setCancelable(true)
            .setPositiveButton("Ja") { dialog: DialogInterface?, which: Int ->
                val intent = intent
                val bundle = intent.extras
                val from = bundle!!["from"].toString()
                database!!.child("users/$username/blockedPost/$from").setValue("0")
                    .addOnSuccessListener { unused: Void? ->
                        Toast.makeText(
                            this,
                            "Dir werden nun keine Beiträge mehr von diesem Benutzer angezeigt!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }.addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this, """
     Ein Fehler ist aufgetreten! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Nein", null)
            .show()
    }

    fun feed() {
        //Retrieving Strings
        val intent = intent
        val bundle = intent.extras
        val date = bundle!!["date"].toString()
        val time = bundle["time"].toString()
        val message = bundle["message"].toString()
        val title = bundle["title"].toString()
        val from = bundle["from"].toString()
        key = bundle["key"].toString()

        //Declare TextView's
        val textDate = findViewById<TextView>(R.id.textFeedDate)
        val textMessage = findViewById<TextView>(R.id.textFeedMessage)
        val textTitle = findViewById<TextView>(R.id.textFeedTitle)
        val textFrom = findViewById<TextView>(R.id.textFeedUsername)

        //Put Strings into TextView
        textDate.text = "$date • $time"
        textMessage.text = message
        textTitle.text = title
        val content = SpannableString(from)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        textFrom.text = content
        textFrom.setOnClickListener { v: View? ->
            val intent1 = Intent(this@OpenPost, OpenProfile::class.java)
            intent1.putExtra("username", from)
                .putExtra("comeFrom", "post")
            startActivity(intent1)
        }
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

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}