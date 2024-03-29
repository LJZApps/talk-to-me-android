package com.lnzpk.chat_app.settings

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.colors.Colors.setButtonColor
import com.lnzpk.chat_app.colors.Colors.setToolbarColor
import com.lnzpk.chat_app.newDatabase.DBHelper

class Feedback : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.feedback)

        val db = DBHelper(this, null)

        window.navigationBarColor = Color.BLACK

        val usernameEditText = findViewById<TextInputLayout>(R.id.feedbackUsername)
        val username = db.getCurrentUsername()
        usernameEditText.editText!!.setText(username)

        val toolbar = findViewById<Toolbar>(R.id.feedbackToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)

        val button = findViewById<Button>(R.id.feedbackButtonSend)
        setButtonColor(this, button)

        button.setOnClickListener { checkFeedbackData() }
    }

    fun setLightMode() {
        setTheme(R.style.feedbackLight)
    }

    fun setDarkMode() {
        setTheme(R.style.feedbackDark)
    }


    @SuppressLint("WrongConstant")
    fun checkFeedbackData() {
        if (isNetworkConnected) {
            val user = findViewById<TextInputLayout>(R.id.feedbackUsername)
            val feedbackTitle = findViewById<TextInputLayout>(R.id.feedbackTitle)
            val feedbackText = findViewById<EditText>(R.id.feedbackText)
            val title = feedbackTitle.editText!!.text.toString().trim { it <= ' ' }
            val username = user.editText!!.text.toString().trim { it <= ' ' }
            val feedback = feedbackText.text.toString().trim { it <= ' ' }
            user.error = null
            feedbackTitle.error = null
            feedbackText.error = null
            var canAgo = true
            if (username.isEmpty()) {
                canAgo = false
                user.error = getString(R.string.loginAndRegister_usernameError2)
            } else if (username.contains(".") or username.contains("/") or username.contains(" ")) {
                canAgo = false
                user.error = getString(R.string.loginAndRegister_usernameError1)
            }
            if (title.isEmpty()) {
                canAgo = false
                feedbackTitle.error = getString(R.string.feedback_titleError1)
            }
            if (feedback.isEmpty()) {
                canAgo = false
                val toast = Toast.makeText(this, R.string.feedback_textError1, Toast.LENGTH_SHORT)
                toast.show()
            }
            if (canAgo) {
                sendFeedback(title, username, feedback)
            }
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.home_noInternet)
                .setMessage(R.string.feedback_networkError1)
                .setPositiveButton(R.string.retry) { _: DialogInterface?, _: Int ->
                    checkFeedbackData()
                }
                .show()
        }
    }

    private val isNetworkConnected: Boolean
        private get() {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }

    private fun sendFeedback(title: String, username: String, feedbackText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.wait)
            .setCancelable(false)
        val alert = builder.show()
        val db = FirebaseFirestore.getInstance()
        val feedback: MutableMap<String, Any> = HashMap()
        feedback["Username"] = username
        feedback["Title"] = title
        feedback["Feedback"] = feedbackText
        db.collection("feedback").add(feedback)
            .addOnSuccessListener {
                alert.cancel()
                Toast.makeText(this@Feedback, R.string.feedback_sentSuccess, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@Feedback,
                    "ERROR:" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}