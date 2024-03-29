package com.lnzpk.chat_app.customThings

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.colors.Colors.setButtonColor
import java.util.*

class requestPreference : Preference {
    private var preference: Preference? = null
    private var username: String? = null
    private var frameLayout: CoordinatorLayout? = null
    private var accept: Button? = null
    private var deny: Button? = null
    var verified = false

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context!!, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(context: Context?) : super(context!!) {verified = false }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        layoutResource = if (Colors.isDarkMode(context)) {
            R.layout.request_preference_layout_dark
        } else {
            R.layout.request_preference_layout_light
        }
        val title = holder.itemView.findViewById<TextView>(android.R.id.title)
        if (Colors.isDarkMode(context)) {
            title.setTextColor(Color.WHITE)
        } else {
            title.setTextColor(Color.BLACK)
        }
        accept = holder.itemView.findViewById(R.id.requestAcceptButton)
        setButtonColor(context, accept!!)
        deny = holder.itemView.findViewById(R.id.denyRequestButton)
        setButtonColor(context, deny!!)
        accept!!.setOnClickListener {
            acceptRequest(
                username,
                preference,
                preferenceManager.preferenceScreen
            )
        }
        deny!!.setOnClickListener {
            removeRequest(
                username,
                preference,
                preferenceManager.preferenceScreen
            )
        }
        val verifiedImage = holder.itemView.findViewById<ImageView>(R.id.verifiedImage)
        if (verifiedImage != null) {
            if (verified) {
                verifiedImage.visibility = View.VISIBLE
            } else {
                verifiedImage.visibility = View.GONE
            }
        }
    }

    fun declarePreference(newPreference: Preference?) {
        preference = newPreference
    }

    fun setUsername(newUsername: String?) {
        username = newUsername
    }

    fun setView(layout: CoordinatorLayout?) {
        frameLayout = layout
    }

    private fun acceptRequest(username: String?, request: Preference?, screen: PreferenceScreen) {
        val me = sharedPreferences!!.getString("username", "UNKNOWN")
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$me/requests/$username").ref.removeValue()
            .addOnSuccessListener {
                database.child("users/$me/friends/$username").setValue("0")
                    .addOnSuccessListener {
                        database.child("users/$username/friends/$me").setValue("0")
                            .addOnSuccessListener {
                                screen.removePreference(request!!)
                                Snackbar.make(
                                    context,
                                    frameLayout!!,
                                    screen.context.getString(R.string.profile_acceptRequestSuccess),
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setGestureInsetBottomIgnored(false)
                                    .show()
                            }
                            .addOnFailureListener { e: Exception ->
                                Toast.makeText(
                                    context,
                                    "Error: " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(
                            context,
                            "Error: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    context,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun removeRequest(username: String?, request: Preference?, screen: PreferenceScreen) {
        val me = sharedPreferences!!.getString("username", "UNKNOWN")
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$me/requests/$username").ref.removeValue()
            .addOnSuccessListener {
                screen.removePreference(request!!)
                Snackbar.make(
                    context,
                    frameLayout!!,
                    screen.context.getString(R.string.profile_declineRequestSuccess),
                    Snackbar.LENGTH_SHORT
                )
                    .setGestureInsetBottomIgnored(false)
                    .show()
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    context,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @JvmName("setVerified1")
    fun setVerified(b: Boolean) {
        verified = b
        notifyChanged()
    }

    companion object {
        fun generateRandom(length: Int): Long {
            val random = Random()
            val digits = CharArray(length)
            digits[0] = (random.nextInt(9) + '1'.code).toChar()
            for (i in 1 until length) {
                digits[i] = (random.nextInt(10) + '0'.code).toChar()
            }
            return String(digits).toLong()
        }
    }
}