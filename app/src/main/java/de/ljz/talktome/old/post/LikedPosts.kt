package de.ljz.talktome.old.post

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import com.google.firebase.database.*
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.newDatabase.DBHelper

class LikedPosts : AppCompatActivity() {
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
        setContentView(R.layout.liked_posts)
        val db = DBHelper(this, null)
        toolbar = findViewById(R.id.likedPostsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar!!)
        database = FirebaseDatabase.getInstance().reference
        username = db.getCurrentUsername()
        likedPostsFun
    }

    private val likedPostsFun: Unit
        get() {
            val feeds = findViewById<LinearLayout>(R.id.likedPostsList)
            database!!.child("posts").addChildEventListener(object : ChildEventListener {
                @SuppressLint("InflateParams", "SetTextI18n")
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        if (snapshot.child("social/likes/$username").exists()) {
                            val message = snapshot.child("text").value.toString()
                            val title = snapshot.child("title").value.toString()
                            val date = snapshot.child("date").value.toString()
                            val time = snapshot.child("time").value.toString()
                            val from = snapshot.child("from").value.toString()
                            val key = snapshot.key
                            val Public = snapshot.child("isAnnouncement").value.toString()
                            val isAnnouncement = java.lang.Boolean.parseBoolean(Public)
                            val v: View
                            v = if (isDarkMode) {
                                LayoutInflater.from(this@LikedPosts)
                                    .inflate(R.layout.feed_dark, null, false)
                            } else {
                                LayoutInflater.from(this@LikedPosts)
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
                                cardHolder.title!!.setText(newTitle)
                            } else {
                                cardHolder.title!!.setText(title)
                            }
                            if (isAnnouncement) {
                                cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                            }
                            cardHolder.title!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))

                            //User
                            val messageLen = message.length
                            val message1 = message.replace("\n", "  ")
                            if (messageLen >= 190) {
                                val newMessage = message1.substring(0, 190) + "..."
                                cardHolder.message!!.setText(newMessage)
                            } else {
                                cardHolder.message!!.setText(message1)
                            }

                            //Summary
                            cardHolder.summary!!.setTextColor(Color.GRAY)
                            if (snapshot.child("edited").exists()) {
                                if (isAnnouncement) {
                                    cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                                    cardHolder.summary!!.setText("$from • $date • $time • Neuigkeit • Bearbeitet")
                                } else {
                                    cardHolder.summary!!.setText("$from • $date • $time • Bearbeitet")
                                }
                            } else {
                                if (isAnnouncement) {
                                    cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                                    cardHolder.summary!!.setText("$from • $date • $time • Neuigkeit")
                                } else {
                                    cardHolder.summary!!.setText("$from • $date • $time")
                                }
                            }

                            //Card
                            val feedParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            feedParams.setMargins(20, 20, 20, 20)
                            feedParams.gravity = Gravity.CENTER
                            cardHolder.card!!.setLayoutParams(feedParams)
                            if (isDarkMode) {
                                cardHolder.card!!.setCardBackgroundColor(getColor(R.color.cardDark))
                            }
                            cardHolder.card!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                val intent = Intent(this@LikedPosts, OpenPost::class.java)
                                intent.putExtra("title", title)
                                    .putExtra("message", message)
                                    .putExtra("from", from)
                                    .putExtra("date", date)
                                    .putExtra("time", time)
                                    .putExtra("key", key)
                                startActivity(intent)
                                //overridePendingTransition(R.anim.slide_in_left, R.anim.fade_in)
                            })
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

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}