package de.ljz.talktome.old.post

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import com.google.firebase.database.*
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors.setButtonColor
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.newDatabase.DBHelper
import java.text.SimpleDateFormat
import java.util.*

class PostComments : AppCompatActivity() {
    var database: DatabaseReference? = null
    var myUsername: String? = null
    var key: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.post_comments)
        val db = DBHelper(this, null)
        val toolbar = findViewById<Toolbar>(R.id.commentsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        database = FirebaseDatabase.getInstance().reference
        myUsername = db.getCurrentUsername()
        val intent = intent
        key = intent.extras!!["key"].toString()
        val button = findViewById<Button>(R.id.uploadCommentButton)
        setButtonColor(this, button)
        button.setOnClickListener { v: View? -> uploadComment() }
        comments
    }

    fun uploadComment() {
        val newComment = findViewById<EditText>(R.id.writeComment)
        val comment = newComment.text.toString().trim { it <= ' ' }
        if (comment.isEmpty()) {
            Toast.makeText(this, "Bitte gebe zuerst dein Kommentar oben ein!", Toast.LENGTH_SHORT)
                .show()
        } else {
            val dateFormat = Date()
            val date = SimpleDateFormat("dd.MM.yyyy").format(dateFormat)
            val comment1 = Comment(comment, date, myUsername)
            database!!.child("posts/$key/social/comments").push().setValue(comment1)
                .addOnSuccessListener {
                    newComment.text = null
                    Toast.makeText(
                        this@PostComments,
                        "Kommentar erfolgreich veröffentlicht!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@PostComments,
                        "Error: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    val comments: Unit
        get() {
            val comments = findViewById<LinearLayout>(R.id.comments)
            database!!.child("posts/$key/social/comments")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val from = snapshot.child("from").value.toString()
                        val comment = snapshot.child("comment").value.toString()
                        val date = snapshot.child("date").value.toString()
                        val v: View
                        v = LayoutInflater.from(this@PostComments)
                            .inflate(R.layout.comment, null, false)
                        val commentHolder = commentHolder()
                        commentHolder.title = v.findViewById(R.id.commentTitle)
                        commentHolder.summary = v.findViewById(R.id.commentSummary)
                        commentHolder.card = v.findViewById(R.id.commentCard)
                        v.tag = commentHolder
                        try {
                            commentHolder.title!!.text = comment
                            commentHolder.summary!!.text = "$from • $date"
                            commentHolder.summary!!.setTextColor(Color.GRAY)
                        } catch (e: Exception) {
                            Toast.makeText(this@PostComments, e.message, Toast.LENGTH_SHORT).show()
                        }
                        commentHolder.card!!.radius = 30f
                        val feedParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        feedParams.setMargins(10, 10, 10, 10)
                        feedParams.gravity = Gravity.CENTER
                        commentHolder.card!!.layoutParams = feedParams
                        if (isDarkMode) {
                            commentHolder.card!!.setCardBackgroundColor(getColor(R.color.cardDark))
                        }
                        comments.addView(v)
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    @IgnoreExtraProperties
    class Comment(var comment: String, var date: String, var from: String?)
    private inner class commentHolder {
        var title: TextView? = null
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

    fun setLightMode() {
        setTheme(R.style.homeLight)
    }

    fun setDarkMode() {
        setTheme(R.style.homeDark)
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
}