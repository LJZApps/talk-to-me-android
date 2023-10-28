package com.lnzpk.chat_app.old.post

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.colors.Colors.setToolbarColor
import com.lnzpk.chat_app.old.newDatabase.DBHelper

class MyPosts : AppCompatActivity() {
    var database: DatabaseReference? = null
    var username: String? = null
    var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.activity_my_posts)
        val db = DBHelper(this, null)
        toolbar = findViewById(R.id.myPostToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar!!)
        database = FirebaseDatabase.getInstance().reference
        username = db.getCurrentUsername()
        myPostsFun
    }

    private val myPostsFun: Unit
        get() {
            val feeds = findViewById<LinearLayout>(R.id.myPostsList)
            database!!.child("posts").addChildEventListener(object : ChildEventListener {
                @SuppressLint("InflateParams", "SetTextI18n")
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        val from = snapshot.child("from").value.toString()
                        if (from == username) {
                            val message = snapshot.child("text").value.toString()
                            val title = snapshot.child("title").value.toString()
                            val date = snapshot.child("date").value.toString()
                            val time = snapshot.child("time").value.toString()
                            val key = snapshot.key
                            val Public = snapshot.child("isAnnouncement").value.toString()
                            val isAnnouncement = java.lang.Boolean.parseBoolean(Public)
                            val v: View = if (isDarkMode) {
                                LayoutInflater.from(this@MyPosts)
                                    .inflate(R.layout.feed_dark, null, false)
                            } else {
                                LayoutInflater.from(this@MyPosts)
                                    .inflate(R.layout.feed_light, null, false)
                            }
                            val cardHolder = cardHolder()

                            //declare
                            cardHolder.title = v.findViewById(R.id.feedTitle)
                            cardHolder.message = v.findViewById(R.id.feedMessage)
                            cardHolder.summary = v.findViewById(R.id.feedSummary)
                            cardHolder.card = v.findViewById(R.id.feedCard)
                            v.tag = cardHolder

                            //Title
                            val titleL = title.length
                            if (titleL >= 35) {
                                val newTitle = title.substring(0, 33) + "..."
                                cardHolder.title!!.text = newTitle
                            } else {
                                cardHolder.title!!.text = title
                            }
                            if (isAnnouncement) {
                                cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                            }
                            cardHolder.title!!.typeface = Typeface.defaultFromStyle(Typeface.BOLD)

                            //User
                            val messageLen = message.length
                            val message1 = message.replace("\n", "  ")
                            if (messageLen >= 190) {
                                val newMessage = message1.substring(0, 190) + "..."
                                cardHolder.message!!.text = newMessage
                            } else {
                                cardHolder.message!!.text = message1
                            }

                            //Summary
                            cardHolder.summary!!.setTextColor(Color.GRAY)
                            if (snapshot.child("edited").exists()) {
                                if (isAnnouncement) {
                                    cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                                    cardHolder.summary!!.text = "$from • $date • $time • Neuigkeit • Bearbeitet"
                                } else {
                                    cardHolder.summary!!.text = "$from • $date • $time • Bearbeitet"
                                }
                            } else {
                                if (isAnnouncement) {
                                    cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                                    cardHolder.summary!!.text = "$from • $date • $time • Neuigkeit"
                                } else {
                                    cardHolder.summary!!.text = "$from • $date • $time"
                                }
                            }

                            //Card
                            val feedParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            feedParams.setMargins(20, 20, 20, 20)
                            feedParams.gravity = Gravity.CENTER
                            cardHolder.card!!.layoutParams = feedParams
                            if (isDarkMode) {
                                cardHolder.card!!.setCardBackgroundColor(getColor(R.color.cardDark))
                            }
                            cardHolder.card!!.setOnLongClickListener { v: View? ->
                                val menu = PopupMenu(this@MyPosts, v)
                                menu.menuInflater.inflate(R.menu.my_post_menu, menu.menu)
                                menu.setOnMenuItemClickListener { item: MenuItem ->
                                    when (item.itemId) {
                                        R.id.menuDeletePost -> delPost(key, v, feeds)
                                        R.id.menuEditPost -> editPost(key)
                                    }
                                    false
                                }
                                menu.show()
                                false
                            }
                            cardHolder.card!!.setOnClickListener {
                                val intent = Intent(this@MyPosts, OpenPost::class.java)
                                intent.putExtra("title", title)
                                    .putExtra("message", message)
                                    .putExtra("from", from)
                                    .putExtra("date", date)
                                    .putExtra("time", time)
                                    .putExtra("key", key)
                                startActivity(intent)
                            }
                            feeds.addView(v)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }

    fun delPost(key: String?, v: View?, layout: LinearLayout) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Beitrag löschen")
            .setMessage("Möchtest du wirklich diesen Beitrag löschen?")
            .setPositiveButton("Ja") { dialog: DialogInterface?, which: Int ->
                val builder1 = AlertDialog.Builder(this)
                builder1.setView(R.layout.wait)
                    .setCancelable(false)
                val alert = builder1.show()
                database!!.child("posts/$key").ref.removeValue()
                    .addOnSuccessListener { aVoid: Void? ->
                        alert.cancel()
                        layout.removeView(v)
                        Toast.makeText(this@MyPosts, "Beitrag gelöscht.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(
                            this@MyPosts,
                            e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNeutralButton("Abbrechen", null)
            .show()
    }

    fun editPost(key: String?) {
        val intent = Intent(this, EditPost::class.java)
        intent.putExtra("key", key)
        startActivity(intent)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
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

    private class cardHolder {
        var title: TextView? = null
        var message: TextView? = null
        var summary: TextView? = null
        var card: CardView? = null
    }
}