package com.lnzpk.chat_app.group

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.customThings.chatPreference
import com.lnzpk.chat_app.newDatabase.DBHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class InviteFriends : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.activity_invite_friends)

        val db = DBHelper(this, null)

        val toolbar = findViewById<Toolbar>(R.id.inviteFriendsToolbar)
        setSupportActionBar(toolbar)
        Colors.setToolbarColor(this, this, toolbar)
        context = baseContext
        groupKey = intent.extras!!["groupKey"].toString()
        groupName = intent.extras!!["groupName"].toString()
        myUsername = db.getCurrentUsername()
        loading = findViewById(R.id.friendsInviteLoading)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.friendsInviteFrame, InviteFriendsLoader())
            .commit()
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

    fun setLightMode() {
        setTheme(R.style.contactsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.contactsDark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class InviteFriendsLoader : PreferenceFragmentCompat() {
        var listener: ChildEventListener? = null
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.invite_friends, rootKey)
            loadFriends()
        }

        private fun loadFriends() {

            val db = DBHelper(requireContext(), null)

            loading!!.visibility = View.VISIBLE
            val username = db.getCurrentUsername()
            val contacts = preferenceScreen
            contacts.removeAll()
            database.child("users/$username/friends")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            listener = object : ChildEventListener {
                                override fun onChildAdded(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    val contactUsername = snapshot.key
                                    val contact = chatPreference(preferenceScreen.context)
                                    if (isDarkMode) {
                                        contact.layoutResource =
                                            R.layout.chat_preference_design_dark
                                    } else {
                                        contact.layoutResource =
                                            R.layout.chat_preference_design_light
                                    }
                                    getProfilePicture(contactUsername, contact)
                                    database.child("users/$contactUsername")
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    val contactName =
                                                        snapshot.child("informations/name").value.toString()
                                                    val contactInfo =
                                                        snapshot.child("informations/info").value.toString()
                                                    if (snapshot.child("publicInfo").exists()) {
                                                        val publicInfo =
                                                            snapshot.child("settings/publicInfo").value.toString()
                                                        if (publicInfo == "true") {
                                                            contact.summary = contactInfo
                                                        } else {
                                                            database.child("users/$contactUsername/friends/$username")
                                                                .addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            if (snapshot.exists()) {
                                                                                contact.summary =
                                                                                    contactInfo
                                                                            } else {
                                                                                contact.summary =
                                                                                    "Ich benutze Talk to me."
                                                                            }
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                        }
                                                    } else {
                                                        contact.summary = contactInfo
                                                    }
                                                    contact.title = contactName
                                                    contact.onPreferenceClickListener =
                                                        Preference.OnPreferenceClickListener {
                                                            inviteAlert(
                                                                contactUsername,
                                                                contactName,
                                                                contact
                                                            )
                                                            false
                                                        }
                                                    database.child("users/$contactUsername/blockedUser/$myUsername")
                                                        .addListenerForSingleValueEvent(object :
                                                            ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                if (!snapshot.exists()) {
                                                                    database.child("groups/$groupKey/members/$contactUsername")
                                                                        .addListenerForSingleValueEvent(
                                                                            object :
                                                                                ValueEventListener {
                                                                                override fun onDataChange(
                                                                                    snapshot: DataSnapshot
                                                                                ) {
                                                                                    if (!snapshot.exists()) {
                                                                                        if (contactUsername != myUsername) {
                                                                                            loading!!.visibility =
                                                                                                View.GONE
                                                                                            contacts.addPreference(
                                                                                                contact
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                }

                                                                                override fun onCancelled(
                                                                                    error: DatabaseError
                                                                                ) {
                                                                                }
                                                                            })
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {}
                                                        })
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                }

                                override fun onChildChanged(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {}
                                override fun onChildMoved(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            }
                            database.child("users/$username/friends").addChildEventListener(listener!!)
                        } else {
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            InviteFriends.context,
                            "Fehler: " + error.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        fun inviteAlert(username: String?, name: String, preference: chatPreference?) {
            AlertDialog.Builder(requireContext())
                .setTitle("Freund einladen")
                .setMessage("Möchtest du $name zur Gruppe \"$groupName\" einladen?")
                .setPositiveButton("Einladen") { _: DialogInterface?, _: Int ->
                    sendInvite(
                        username,
                        preference
                    )
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }

        fun sendInvite(username: String?, preference: chatPreference?) {
            val screen = preferenceScreen
            val date = Date()
            val time = SimpleDateFormat("HH:mm").format(date)
            val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
            val mDatabase = FirebaseDatabase.getInstance().reference
            val push = mDatabase.push().key
            val notifyId = createNotificationId(15).toInt().toString()
            mDatabase.child("users/$username/blockedUser/$myUsername")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            mDatabase.child("users/$myUsername/chats/$username")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val normalMessage = InviteMessage(myUsername, "Einladung", time, messageDate, "SENT", notifyId, "groupInvite", groupKey)
                                        mDatabase.child("users/$myUsername/chats/$username/messages")
                                            .child(push!!
                                            ).setValue(normalMessage)
                                            .addOnSuccessListener {
                                                mDatabase.child("users/$username/chats/$myUsername/messages")
                                                    .child(
                                                        push
                                                    ).setValue(normalMessage)
                                                    .addOnSuccessListener {
                                                        screen.removePreference(preference!!)
                                                        Toast.makeText(
                                                            InviteFriends.context,
                                                            "Einladung gesendet.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                            .addOnFailureListener { e: Exception ->
                                                Toast.makeText(InviteFriends.context, """Nachricht konnte nicht gesendet werden! ${e.message}""".trimIndent(), Toast.LENGTH_LONG).show()
                                            }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            InviteFriends.context, """Fehler! Fehler-Nachricht: ${error.message}Fehler-Code: ${error.code}""".trimIndent(), Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        fun getProfilePicture(username: String?, preference: chatPreference) {
            preference.setIcon(R.drawable.ic_no_profile_picture)
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val getPic = storageRef.child("profile_pictures/$username.jpg")
            try {
                val picture =
                    File(InviteFriends.context!!.cacheDir.toString() + "/profilePictures/" + username + ".jpg")
                if (picture.exists()) {
                    val is1: InputStream = FileInputStream(picture)
                    val size1 = is1.available()
                    val picBuffer = ByteArray(size1)
                    is1.read(picBuffer)
                    is1.close()
                    val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                    val d: Drawable = BitmapDrawable(resources, bmp)
                    preference.icon = d
                }
            } catch (e: Exception) {
            }
            getPic.downloadUrl.addOnSuccessListener {
                getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                    val date = storageMetadata.getCustomMetadata("date")
                    try {
                        val config =
                            File(InviteFriends.context!!.cacheDir.toString() + "/profilePictures/" + username + ".txt")
                        if (config.exists()) {
                            val `is`: InputStream = FileInputStream(config)
                            val size = `is`.available()
                            val buffer = ByteArray(size)
                            `is`.read(buffer)
                            `is`.close()
                            val lastUpdated = String(buffer)
                            if (lastUpdated != date) {
                                getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                                    val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                                    val d: Drawable =
                                        BitmapDrawable(InviteFriends.context!!.resources, bmp)
                                    preference.icon = d
                                    val myDir =
                                        File(InviteFriends.context!!.cacheDir.toString() + "/profilePictures")
                                    if (!myDir.exists()) {
                                        myDir.mkdir()
                                    }
                                    val config1 = "$username.txt"
                                    val picture = "$username.jpg"
                                    val configFile = File(myDir, config1)
                                    if (configFile.exists()) {
                                        configFile.delete()
                                    }
                                    val pictureFile = File(myDir, picture)
                                    if (pictureFile.exists()) {
                                        pictureFile.delete()
                                    }
                                    getPic.metadata.addOnSuccessListener {
                                        try {
                                            val configS =
                                                FileOutputStream(configFile.absolutePath, true)
                                            configS.flush()
                                            configS.write(date!!.toByteArray())
                                            configS.close()
                                            val pictureS =
                                                FileOutputStream(pictureFile.absolutePath, true)
                                            pictureS.write(bytes1)
                                            pictureS.close()
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                                    .addOnFailureListener { exception: Exception ->
                                        val error = exception.localizedMessage
                                        Toast.makeText(
                                            InviteFriends.context,
                                            error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                val picture =
                                    File(InviteFriends.context!!.cacheDir.toString() + "/profilePictures/" + username + ".jpg")
                                val is1: InputStream = FileInputStream(picture)
                                val size1 = is1.available()
                                val picBuffer = ByteArray(size1)
                                is1.read(picBuffer)
                                is1.close()
                                val bmp =
                                    BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                                val d: Drawable = BitmapDrawable(resources, bmp)
                                preference.icon = d
                            }
                        } else {
                            getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                                val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                                val d: Drawable =
                                    BitmapDrawable(InviteFriends.context!!.resources, bmp)
                                preference.icon = d
                                val myDir =
                                    File(InviteFriends.context!!.cacheDir.toString() + "/profilePictures")
                                if (!myDir.exists()) {
                                    myDir.mkdir()
                                }
                                val config1 = "$username.txt"
                                val picture = "$username.jpg"
                                val configFile = File(myDir, config1)
                                if (configFile.exists()) {
                                    configFile.delete()
                                }
                                val pictureFile = File(myDir, picture)
                                if (pictureFile.exists()) {
                                    pictureFile.delete()
                                }
                                getPic.metadata.addOnSuccessListener {
                                    try {
                                        val configS =
                                            FileOutputStream(configFile.absolutePath, true)
                                        configS.write(date!!.toByteArray())
                                        configS.close()
                                        val pictureS =
                                            FileOutputStream(pictureFile.absolutePath, true)
                                        pictureS.write(bytes1)
                                        pictureS.close()
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                                .addOnFailureListener { exception: Exception ->
                                    val error = exception.localizedMessage
                                    Toast.makeText(InviteFriends.context, error, Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    } catch (e: Exception) {
                        //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }.addOnFailureListener {
                val myDir = File(InviteFriends.context!!.cacheDir.toString() + "/profilePictures")
                if (!myDir.exists()) {
                    myDir.mkdir()
                }
                val config1 = "$username.txt"
                val picture = "$username.jpg"
                val configFile = File(myDir, config1)
                if (configFile.exists()) {
                    configFile.delete()
                }
                val pictureFile = File(myDir, picture)
                if (pictureFile.exists()) {
                    pictureFile.delete()
                }
                preference.setIcon(R.drawable.ic_no_profile_picture)
            }
        }

        val isDarkMode: Boolean
            get() {
                val theme = preferenceManager.sharedPreferences
                return theme!!.getBoolean("dark", false)
            }

        @IgnoreExtraProperties
        class InviteMessage(
            var from: String?,
            var message: String,
            var time: String,
            var date: String,
            var status: String,
            var notifyId: String,
            var type: String,
            var groupKey: String?
        )

        companion object {
            fun createNotificationId(length: Int): Long {
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

    companion object {
        var context: Context? = null
        var groupKey: String? = null
        var groupName: String? = null
        var myUsername: String? = null
        var loading: ProgressBar? = null
        var database = FirebaseDatabase.getInstance().reference
    }
}