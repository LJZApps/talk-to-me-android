package de.ljz.talktome.old.settings

import android.content.Context
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.customThings.chatPreference
import de.ljz.talktome.old.newDatabase.DBHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class BlockedUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.blocked_user)
        val db = DBHelper(this, null)
        val toolbar = findViewById<Toolbar>(R.id.blockedUserToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        username = db.getCurrentUsername()
        noBlockedText = findViewById(R.id.noBlockedText)
        context = baseContext
        window.navigationBarColor = Color.BLACK
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.blockedUserFrame, BlockedUserPref())
            .commit()
    }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_right)
    }

    class BlockedUserPref : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.blocked_user, rootKey)
            blockedUser
        }

        val blockedUser: Unit
            get() {
                val screen = preferenceScreen
                screen.removeAll()
                database.child("users/$username/blockedUser")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                noBlockedText!!.visibility = View.GONE
                                database.child("users/$username/blockedUser")
                                    .addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(
                                            snapshot: DataSnapshot,
                                            previousChildName: String?
                                        ) {
                                            val otherUsername = snapshot.key
                                            val user = chatPreference(requireContext())
                                            database.child("users/$otherUsername/informations")
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val name =
                                                            snapshot.child("name").value.toString()
                                                        val info =
                                                            snapshot.child("info").value.toString()
                                                        user.title = name
                                                        user.summary = info
                                                        getProfilePicture(otherUsername, user)
                                                        if (Colors.isDarkMode(requireContext())) {
                                                            user.layoutResource =
                                                                R.layout.chat_preference_design_dark
                                                        } else {
                                                            user.layoutResource =
                                                                R.layout.chat_preference_design_light
                                                        }
                                                        user.onPreferenceClickListener =
                                                            Preference.OnPreferenceClickListener {
                                                                AlertDialog.Builder(
                                                                    activity!!
                                                                )
                                                                    .setMessage("Benutzer freigeben?")
                                                                    .setPositiveButton("Freigeben") { _: DialogInterface?, _: Int ->
                                                                        unblockUser(
                                                                            otherUsername,
                                                                            user
                                                                        )
                                                                    }
                                                                    .setNegativeButton(
                                                                        "Abbrechen",
                                                                        null
                                                                    )
                                                                    .show()
                                                                false
                                                            }
                                                        screen.addPreference(user)
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
                                    })
                            } else {
                                noBlockedText!!.visibility = View.VISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

        fun unblockUser(otherUsername: String?, preference: chatPreference?) {
            database.child("users/$username/blockedUser/$otherUsername")
                .ref.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Benutzer freigegeben.",
                        Toast.LENGTH_SHORT
                    ).show()
                    blockedUser
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        requireContext(),
                        "Etwas ist schiefgelaufen.\n${e.message}".trimIndent(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun getProfilePicture(username: String?, preference: chatPreference) {
            preference.setIcon(R.drawable.ic_no_profile_picture)
            val user = preferenceManager.sharedPreferences
            val myUsername = user!!.getString("username", "UNKNOWN")
            FirebaseDatabase.getInstance().reference.child("users/$username/blockedUser/$myUsername")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            preference.setIcon(R.drawable.ic_no_profile_picture)
                        } else {
                            val storage = FirebaseStorage.getInstance()
                            val storageRef = storage.reference
                            val getPic = storageRef.child("profile_pictures/$username.jpg")
                            try {
                                val picture: File = File(
                                    requireContext().cacheDir.toString()+"/profilePictures/$username.jpg"
                                )
                                if (picture.exists()) {
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
                            } catch (e: Exception) {
                            }
                            getPic.downloadUrl.addOnSuccessListener {
                                getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                                    val date = storageMetadata.getCustomMetadata("date")
                                    try {
                                        val config: File = File(
                                            requireContext().cacheDir.toString() + "/profilePictures/$username.txt"
                                        )
                                        if (config.exists()) {
                                            val `is`: InputStream = FileInputStream(config)
                                            val size = `is`.available()
                                            val buffer = ByteArray(size)
                                            `is`.read(buffer)
                                            `is`.close()
                                            val lastUpdated = String(buffer)
                                            if (lastUpdated != date) {
                                                getPic.getBytes(7000000)
                                                    .addOnSuccessListener { bytes1: ByteArray ->
                                                        val bmp = BitmapFactory.decodeByteArray(
                                                            bytes1,
                                                            0,
                                                            bytes1.size
                                                        )
                                                        val d: Drawable =
                                                            BitmapDrawable(resources, bmp)
                                                        preference.icon = d
                                                        val myDir = File(
                                                            requireContext().cacheDir.toString() + "/profilePictures"
                                                        )
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
                                                                val configS = FileOutputStream(
                                                                    configFile.absolutePath,
                                                                    true
                                                                )
                                                                configS.flush()
                                                                configS.write(date!!.toByteArray())
                                                                configS.close()
                                                                val pictureS = FileOutputStream(
                                                                    pictureFile.absolutePath,
                                                                    true
                                                                )
                                                                pictureS.write(bytes1)
                                                                pictureS.close()
                                                            } catch (e: Exception) {
                                                            }
                                                        }
                                                    }
                                                    .addOnFailureListener { exception: Exception ->
                                                        val error = exception.localizedMessage
                                                        Toast.makeText(
                                                            requireContext(),
                                                            error,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            } else {
                                                val picture: File = File(
                                                    requireContext().cacheDir.toString() + "/profilePictures/" + username + ".jpg"
                                                )
                                                val is1: InputStream = FileInputStream(picture)
                                                val size1 = is1.available()
                                                val picBuffer = ByteArray(size1)
                                                is1.read(picBuffer)
                                                is1.close()
                                                val bmp = BitmapFactory.decodeByteArray(
                                                    picBuffer,
                                                    0,
                                                    picBuffer.size
                                                )
                                                val d: Drawable = BitmapDrawable(resources, bmp)
                                                preference.icon = d
                                            }
                                        } else {
                                            getPic.getBytes(7000000)
                                                .addOnSuccessListener { bytes1: ByteArray ->
                                                    val bmp = BitmapFactory.decodeByteArray(
                                                        bytes1,
                                                        0,
                                                        bytes1.size
                                                    )
                                                    val d: Drawable = BitmapDrawable(resources, bmp)
                                                    preference.icon = d
                                                    val myDir: File = File(
                                                        requireContext().cacheDir.toString() + "/profilePictures"
                                                    )
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
                                                            val configS = FileOutputStream(
                                                                configFile.absolutePath,
                                                                true
                                                            )
                                                            configS.write(date!!.toByteArray())
                                                            configS.close()
                                                            val pictureS = FileOutputStream(
                                                                pictureFile.absolutePath,
                                                                true
                                                            )
                                                            pictureS.write(bytes1)
                                                            pictureS.close()
                                                        } catch (e: Exception) {
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { exception: Exception ->
                                                    val error = exception.localizedMessage
                                                    Toast.makeText(
                                                        requireContext(),
                                                        error,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    } catch (e: Exception) {
                                        //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }.addOnFailureListener {
                                val myDir: File = File(
                                    requireContext().cacheDir.toString() + "/profilePictures"
                                )
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
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    companion object {
        var database = FirebaseDatabase.getInstance().reference
        var username: String? = null
        var noBlockedText: TextView? = null
        var context: Context? = null
    }
}