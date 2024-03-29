package com.lnzpk.chat_app.post

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors.setToolbarColor
import com.lnzpk.chat_app.newDatabase.DBHelper

class BlockedPosts : AppCompatActivity() {
    var database = FirebaseDatabase.getInstance().reference
    var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.blocked_posts)
        val db = DBHelper(this, null)
        username = db.getCurrentUsername()
        val toolbar = findViewById<Toolbar>(R.id.blockedPostsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        posts
    }

    val posts: Unit
        get() {
            val feeds = findViewById<LinearLayout>(R.id.blockedPostsView)
            database.child("posts").addChildEventListener(object : ChildEventListener {
                @SuppressLint("InflateParams", "SetTextI18n")
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        val from = snapshot.child("from").value.toString()
                        val message = snapshot.child("text").value.toString()
                        val title = snapshot.child("title").value.toString()
                        val date = snapshot.child("date").value.toString()
                        val time = snapshot.child("time").value.toString()
                        val key = snapshot.key
                        val Public = snapshot.child("isAnnouncement").value.toString()
                        val isAnnouncement = java.lang.Boolean.parseBoolean(Public)
                        val v: View
                        v = if (isDarkMode) {
                            LayoutInflater.from(this@BlockedPosts)
                                .inflate(R.layout.feed_dark, null, false)
                        } else {
                            LayoutInflater.from(this@BlockedPosts)
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
                        cardHolder.card!!.setOnClickListener(View.OnClickListener { v1: View? ->
                            val intent = Intent(this@BlockedPosts, OpenPost::class.java)
                            intent.putExtra("title", title)
                                .putExtra("message", message)
                                .putExtra("from", from)
                                .putExtra("date", date)
                                .putExtra("time", time)
                                .putExtra("key", key)
                            startActivity(intent)
                            //overridePendingTransition(R.anim.slide_in_left, R.anim.fade_in)
                        })
                        cardHolder.card!!.setOnLongClickListener(OnLongClickListener { v1: View? ->
                            val menu = PopupMenu(this@BlockedPosts, v1)
                            menu.menuInflater.inflate(R.menu.blocked_post_menu, menu.menu)
                            menu.setOnMenuItemClickListener { item: MenuItem ->
                                when (item.itemId) {
                                    R.id.unblockPost -> database.child(
                                        "users/$username/blockedPost/$from"
                                    ).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                snapshot.ref.removeValue()
                                                    .addOnSuccessListener { unused: Void? ->
                                                        Toast.makeText(
                                                            this@BlockedPosts,
                                                            "Dir werden nun wieder Beiträge von diesem Benutzer angezeigt!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener { e: Exception ->
                                                        Toast.makeText(
                                                            this@BlockedPosts,
                                                            "Ein Fehler ist aufgetreten: " + e.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            } else {
                                                Toast.makeText(
                                                    this@BlockedPosts,
                                                    "Ein Fehler ist aufgetreten!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                }
                                false
                            }
                            menu.show()
                            false
                        })
                        database.child("users/$username/blockedPost/$from")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        feeds.addView(v)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
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

    private class cardHolder {
        var title: TextView? = null
        var message: TextView? = null
        var summary: TextView? = null
        var card: CardView? = null
    }
}