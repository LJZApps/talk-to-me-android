package com.lnzpk.chat_app.profile

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.chat.OpenChat
import com.lnzpk.chat_app.colors.Colors.setButtonColor
import com.lnzpk.chat_app.colors.Colors.setToolbarColor
import com.lnzpk.chat_app.newDatabase.DBHelper
import com.lnzpk.chat_app.post.OpenPost
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class OpenProfile : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var requestButton: Button? = null
    var messageButton: Button? = null
    var blockUser: Button? = null
    var profileName: TextView? = null
    var profileInfo: TextView? = null
    var intent1: Intent? = null
    var otherUsername: String? = null
    var myUsername: String? = null
    var comeFrom: String? = null
    var database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.open_profile)

        requestButton = findViewById(R.id.openProfileRequestButton)
        messageButton = findViewById(R.id.openProfileMessageButton)
        blockUser = findViewById(R.id.openProfileBlockUser)
        profileName = findViewById(R.id.openProfileNameText)
        profileInfo = findViewById(R.id.openProfileInfoText)
        if (isDarkMode) {
            profileInfo!!.setTextColor(getColor(android.R.color.secondary_text_dark))
        } else {
            profileInfo!!.setTextColor(getColor(android.R.color.secondary_text_light))
        }
        setButtonColor(this, requestButton!!)
        setButtonColor(this, messageButton!!)
        setButtonColor(this, blockUser!!)
        intent1 = intent
        otherUsername = intent1!!.extras!!.getString("username")
        comeFrom = intent1!!.extras!!.getString("comeFrom")
        toolbar = findViewById(R.id.openProfileToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar!!)
        showInfo()
    }

    private fun showInfo() {
        val username = intent1!!.extras!!.getString("username")
        val db = DBHelper(this, null)
        myUsername = db.getCurrentUsername()
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$username/blockedUser/$myUsername")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        toolbar!!.title = username
                        profileInfo!!.text = ""
                        toolbar!!.title = "N/A"
                        messageButton!!.setOnClickListener { v: View? ->
                            Toast.makeText(
                                this@OpenProfile,
                                R.string.profile_notAvailable,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        requestButton!!.setText(R.string.profile_sendRequest)
                        requestButton!!.setOnClickListener { v: View? ->
                            Toast.makeText(
                                this@OpenProfile,
                                R.string.profile_profileNotFound,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        profileName!!.setText(R.string.profile_profileNotFound)
                    } else {
                        posts
                        checkForFriendship(username)
                        database.child("users/$username")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        toolbar!!.title = username
                                        if (snapshot.child("settings/publicInfo").exists()) {
                                            val isPublicInfo =
                                                snapshot.child("settings/publicInfo").value.toString()
                                            if (isPublicInfo == "true") {
                                                if (snapshot.child("informations/info").value.toString() == "string_default_info") {
                                                    profileInfo!!.setText(R.string.default_info)
                                                } else {
                                                    profileInfo!!.text =
                                                        snapshot.child("informations/info").value.toString()
                                                }
                                            } else {
                                                database.child("users/$username/friends/$myUsername")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot2: DataSnapshot) {
                                                            if (snapshot2.exists()) {
                                                                if (snapshot.child("informations/info").value.toString() == "string_default_info") {
                                                                    profileInfo!!.setText(R.string.default_info)
                                                                } else {
                                                                    profileInfo!!.text =
                                                                        snapshot.child("informations/info").value.toString()
                                                                }
                                                            } else {
                                                                profileInfo!!.setText(R.string.default_info)
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            }
                                        } else {
                                            if (snapshot.child("informations/info").value.toString() == "string_default_info" || !snapshot.child(
                                                    "informations/info"
                                                ).exists()
                                            ) {
                                                profileInfo!!.setText(R.string.default_info)
                                            } else {
                                                profileInfo!!.text =
                                                    snapshot.child("informations/info").value.toString()
                                            }
                                        }
                                        profileName!!.text =
                                            snapshot.child("informations/name").value.toString()
                                        val messagesAllowed =
                                            snapshot.child("settings/messagesAllowed").value.toString()
                                        val message =
                                            java.lang.Boolean.parseBoolean(messagesAllowed)
                                        if (!message) {
                                            if (snapshot.child("friends/$myUsername").exists()) {
                                                messageButton!!.setOnClickListener { v: View? ->
                                                    when (comeFrom) {
                                                        "group", "post", "home", "friends" -> {
                                                            val intent = Intent(
                                                                this@OpenProfile,
                                                                OpenChat::class.java
                                                            )
                                                            intent.putExtra("username", username)
                                                            startActivity(intent)
                                                            finish()
                                                        }

                                                        "chat" -> finish()
                                                    }
                                                }
                                            } else {
                                                messageButton!!.isEnabled = false
                                            }
                                        } else {
                                            messageButton!!.setOnClickListener { v: View? ->
                                                when (comeFrom) {
                                                    "group", "post", "home", "friends" -> {
                                                        val intent = Intent(
                                                            this@OpenProfile,
                                                            OpenChat::class.java
                                                        )
                                                        intent.putExtra("username", username)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                    "chat" -> finish()
                                                }
                                            }
                                        }
                                        val verified =
                                            findViewById<ImageView>(R.id.openProfileVerified)
                                        if (snapshot.child("settings/staff").exists()) {
                                            val staffStr =
                                                snapshot.child("settings/staff").value.toString()
                                            val isStaff = java.lang.Boolean.parseBoolean(staffStr)
                                            if (isStaff) {
                                                verified.visibility = View.VISIBLE
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        checkForBlockedUser(username)
        getProfilePicture(username)
    }

    fun getProfilePicture(username: String?) {
        val profilePicture = findViewById<ImageView>(R.id.openProfileProfilePicture)
        profilePicture.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_no_profile_picture
            )
        )
        val db = DBHelper(this, null)
        val myUsername = db.getCurrentUsername()
        database.child("users/$username/blockedUser/$myUsername")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        profilePicture.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@OpenProfile,
                                R.drawable.ic_no_profile_picture
                            )
                        )
                    } else {
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        profilePicture.setOnClickListener { v: View? ->
                            val intent = Intent(this@OpenProfile, OpenProfilePicture::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)

                        }
                        val getPic = storageRef.child("profile_pictures/$username.jpg")
                        try {
                            val picture = File("$cacheDir/profilePictures/$username.jpg")
                            if (picture.exists()) {
                                val is1: InputStream = FileInputStream(picture)
                                val size1 = is1.available()
                                val picBuffer = ByteArray(size1)
                                is1.read(picBuffer)
                                is1.close()
                                val bmp =
                                    BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                                val d: Drawable = BitmapDrawable(resources, bmp)
                                profilePicture.setImageDrawable(d)
                            }
                        } catch (e: Exception) {
                        }
                        getPic.downloadUrl.addOnSuccessListener { uri: Uri? ->
                            getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                                val date = storageMetadata.getCustomMetadata("date")
                                try {
                                    val config = File("$cacheDir/profilePictures/$username.txt")
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
                                                    val d: Drawable = BitmapDrawable(resources, bmp)
                                                    profilePicture.setImageDrawable(d)
                                                    val myDir = File("$cacheDir/profilePictures")
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
                                                    getPic.metadata.addOnSuccessListener { storageMetadata1: StorageMetadata? ->
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
                                                        this@OpenProfile,
                                                        error,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } else {
                                            val picture =
                                                File("$cacheDir/profilePictures/$username.jpg")
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
                                            profilePicture.setImageDrawable(d)
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
                                                profilePicture.setImageDrawable(d)
                                                val myDir = File("$cacheDir/profilePictures")
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
                                                getPic.metadata.addOnSuccessListener { storageMetadata1: StorageMetadata? ->
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
                                                    this@OpenProfile,
                                                    error,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@OpenProfile, e.message, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }.addOnFailureListener { e: Exception? ->
                            profilePicture.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@OpenProfile,
                                    R.drawable.ic_no_profile_picture
                                )
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun reportUser(username: String?) {
        val database = FirebaseDatabase.getInstance().reference
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.setHint(R.string.hint_reportReason)
        val lp = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        lp.marginEnd = 100
        lp.marginStart = 100
        lp.validate()
        input.layoutParams = lp
        input.isSingleLine = false
        builder.setTitle(R.string.profile_reportTitle)
            .setMessage(R.string.profile_reportMessage)
            .setView(input)
            .setPositiveButton(R.string.profile_reportSend) { dialog: DialogInterface?, which: Int ->
                val wait = AlertDialog.Builder(this)
                wait.setView(R.layout.wait3)
                    .setCancelable(false)
                val show = wait.show()
                database.child("users/$username")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val reportReason = input.text.toString()
                            val db = FirebaseFirestore.getInstance()
                            val name = snapshot.child("informations/name").value.toString()
                            val report: MutableMap<String, Any?> = HashMap()
                            report["reportFrom"] = myUsername
                            report["reportedUser"] = username
                            report["reportReason"] = reportReason
                            db.collection("reports")
                                .add(report)
                                .addOnSuccessListener { documentReference: DocumentReference? ->
                                    show.cancel()
                                    AlertDialog.Builder(this@OpenProfile)
                                        .setTitle(R.string.profile_reportDoneTitle)
                                        .setMessage(R.string.profile_reportDoneMessage)
                                        .setPositiveButton(R.string.profile_reportBlockAndDone) { dialog1: DialogInterface?, which1: Int ->
                                            blockUser(
                                                username
                                            )
                                        }
                                        .setNegativeButton(
                                            R.string.profile_reportExitWithoutBlock,
                                            null
                                        )
                                        .show()
                                }
                                .addOnFailureListener { e: Exception ->
                                    show.cancel()
                                    Toast.makeText(
                                        this@OpenProfile,
                                        "Error: " + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            .setNeutralButton(android.R.string.cancel, null)
            .setCancelable(false)
            .show()
    }

    fun checkForBlockedUser(username: String?) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$myUsername/blockedUser")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("RestrictedApi")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child(username!!).exists()) {
                            blockUser!!.setText(R.string.profile_unblockButton)
                            blockUser!!.setOnClickListener { v: View? ->
                                val builder = AlertDialog.Builder(this@OpenProfile)
                                builder.setTitle(R.string.profile_unblockTitle)
                                    .setPositiveButton(R.string.profile_unblockButton) { dialog: DialogInterface?, which: Int ->
                                        unblockUser(
                                            username
                                        )
                                    }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setCancelable(false)
                                    .show()
                            }
                        } else {
                            blockUser!!.setText(R.string.profile_blockButton)
                            blockUser!!.setOnClickListener { v: View? ->
                                val builder = AlertDialog.Builder(this@OpenProfile)
                                builder.setTitle(R.string.profile_blockTitle)
                                    .setPositiveButton(R.string.profile_blockButton) { dialog: DialogInterface?, which: Int ->
                                        blockUser(
                                            username
                                        )
                                    }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setCancelable(false)
                                    .show()
                            }
                        }
                    } else {
                        blockUser!!.setText(R.string.profile_blockButton)
                        blockUser!!.setOnClickListener { v: View? ->
                            val builder = AlertDialog.Builder(this@OpenProfile)
                            builder.setTitle(R.string.profile_blockTitle)
                                .setPositiveButton(R.string.profile_blockButton) { dialog: DialogInterface?, which: Int ->
                                    blockUser(
                                        username
                                    )
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .setCancelable(false)
                                .show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun blockUser(username: String?) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$myUsername").child("blockedUser").child(username!!).setValue("0")
            .addOnSuccessListener { aVoid: Void? ->
                Toast.makeText(
                    this@OpenProfile,
                    R.string.profile_blockUserSuccess,
                    Toast.LENGTH_SHORT
                ).show()
                checkForBlockedUser(username)
            }.addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun unblockUser(username: String?) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$myUsername/blockedUser/$username").removeValue()
            .addOnSuccessListener { aVoid: Void? ->
                Toast.makeText(
                    this@OpenProfile,
                    R.string.profile_unblockUserSuccess,
                    Toast.LENGTH_SHORT
                ).show()
                checkForBlockedUser(username)
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun acceptRequest(username: String?, requestLayout: ConstraintLayout) {
        database.child("users/$myUsername/requests/$username").ref.removeValue()
            .addOnSuccessListener { unused: Void? ->
                database.child("users/$myUsername/friends/$username").setValue("0")
                    .addOnSuccessListener { unused1: Void? ->
                        database.child("users/$username/friends/$myUsername").setValue("0")
                            .addOnSuccessListener { unused2: Void? ->
                                val slide = AnimationUtils.loadAnimation(
                                    applicationContext, R.anim.slide_down
                                )
                                requestLayout.startAnimation(slide)
                                requestLayout.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    R.string.profile_acceptRequestSuccess,
                                    Toast.LENGTH_SHORT
                                ).show()
                                checkForFriendship(username)
                            }
                            .addOnFailureListener { e: Exception ->
                                Toast.makeText(
                                    this,
                                    "Error: " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(
                            this,
                            "Error: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun removeRequest(username: String?, requestLayout: ConstraintLayout) {
        database.child("users/$myUsername/requests/$username").ref.removeValue()
            .addOnSuccessListener { unused: Void? ->
                val slide = AnimationUtils.loadAnimation(
                    applicationContext, R.anim.slide_down
                )
                requestLayout.startAnimation(slide)
                requestLayout.visibility = View.GONE
                Toast.makeText(this, R.string.profile_declineRequestSuccess, Toast.LENGTH_SHORT)
                    .show()
                checkForFriendship(username)
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun checkForFriendship(username: String?) {
        val requestLayout = findViewById<ConstraintLayout>(R.id.openProfileRequestLayout)
        val acceptButton = findViewById<Button>(R.id.openProfileAcceptRequest)
        val declineButton = findViewById<Button>(R.id.openProfileDeclineRequest)
        setRequestColor(requestLayout)
        setButtonColor(this, acceptButton)
        setButtonColor(this, declineButton)
        val me = myUsername
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$me/friends/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        requestButton!!.setText(R.string.profile_removeFriendButton)
                        requestButton!!.setOnClickListener { v: View? -> removeFriend(username) }
                    } else {
                        database.child("users/$myUsername/requests/$username")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val slide = AnimationUtils.loadAnimation(
                                            applicationContext, R.anim.slide_up_from_buttom
                                        )
                                        requestLayout.visibility = View.VISIBLE
                                        requestLayout.startAnimation(slide)
                                        acceptButton.setOnClickListener { v: View? ->
                                            acceptRequest(
                                                username,
                                                requestLayout
                                            )
                                        }
                                        declineButton.setOnClickListener { v: View? ->
                                            removeRequest(
                                                username,
                                                requestLayout
                                            )
                                        }
                                    } else {
                                        val slide = AnimationUtils.loadAnimation(
                                            applicationContext, R.anim.slide_down
                                        )
                                        requestLayout.startAnimation(slide)
                                        requestLayout.visibility = View.GONE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        database.child("users/$username/requests/$me")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        requestButton!!.setText(R.string.profile_abortRequest)
                                        requestButton!!.setOnClickListener { v: View? ->
                                            snapshot.ref.removeValue()
                                                .addOnSuccessListener { unused: Void? ->
                                                    checkForFriendship(
                                                        username
                                                    )
                                                }
                                                .addOnFailureListener { e: Exception ->
                                                    Toast.makeText(
                                                        this@OpenProfile,
                                                        "Error: " + e.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    } else {
                                        requestButton!!.setText(R.string.profile_sendRequest)
                                        requestButton!!.setOnClickListener { preference: View? ->
                                            addRequest(
                                                username
                                            )
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OpenProfile, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun addRequest(username: String?) {
        val me = myUsername
        val notifyId = generateRandom(5).toInt()
        val database = FirebaseDatabase.getInstance().reference
        database.child("users/$username/requests/$me").setValue(notifyId)
            .addOnSuccessListener { unused: Void? -> checkForFriendship(username) }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@OpenProfile,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun removeFriend(username: String?) {
        val me = myUsername
        val reference = FirebaseDatabase.getInstance().getReference("users/$me")
        reference.child("friends/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.ref.removeValue()
                        .addOnSuccessListener { aVoid: Void? -> checkForFriendship(username) }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                this@OpenProfile,
                                e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    val posts: Unit
        get() {
            val noPostsImage = findViewById<ImageView>(R.id.openProfileNoPostsImage)
            val noPostsText = findViewById<TextView>(R.id.openProfileNoPostsText)
            val feeds = findViewById<LinearLayout>(R.id.openProfilePostList)
            val username = myUsername
            val postsCount = intArrayOf(0)
            val database = FirebaseDatabase.getInstance().reference
            database.child("posts").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val map = snapshot.value as Map<*, *>?
                    val from = map!!["from"].toString()
                    if (from == otherUsername) {
                        postsCount[0] = postsCount[0] + 1
                        noPostsImage.visibility = View.GONE
                        noPostsText.visibility = View.GONE
                        val message = snapshot.child("text").value.toString()
                        val title = snapshot.child("title").value.toString()
                        val date = snapshot.child("date").value.toString()
                        val time = snapshot.child("time").value.toString()
                        val publicPost = snapshot.child("publicPost").value.toString()
                        val key = snapshot.key
                        val announcement = snapshot.child("isAnnouncement").value.toString()
                        val isAnnouncement = java.lang.Boolean.parseBoolean(announcement)
                        val v: View
                        v = if (isDarkMode) {
                            LayoutInflater.from(this@OpenProfile)
                                .inflate(R.layout.feed_dark, null, false)
                        } else {
                            LayoutInflater.from(this@OpenProfile)
                                .inflate(R.layout.feed_light, null, false)
                        }
                        val cardHolder: cardHolder = cardHolder()

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
                                cardHolder.summary!!.text = from + " • " + date + " • " + time + getString(
                                    R.string.home_postsNewsEdited
                                )
                            } else {
                                cardHolder.summary!!.text = from + " • " + date + " • " + time + getString(
                                    R.string.home_postEdited
                                )
                            }
                        } else {
                            if (isAnnouncement) {
                                cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                                cardHolder.summary!!.text = from + " • " + date + " • " + time + getString(
                                    R.string.home_postNews
                                )
                            } else {
                                cardHolder.summary!!.text = "$from • $date • $time"
                            }
                        }

                        //Card
                        val feedParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        feedParams.setMargins(10, 10, 10, 10)
                        feedParams.gravity = Gravity.CENTER
                        cardHolder.card!!.layoutParams = feedParams
                        if (isDarkMode) {
                            cardHolder.card!!.setCardBackgroundColor(getColor(R.color.cardDark))
                        }
                        cardHolder.card!!.setOnClickListener(View.OnClickListener { v1: View? ->
                            val intent = Intent(this@OpenProfile, OpenPost::class.java)
                            intent.putExtra("title", title)
                                .putExtra("message", message)
                                .putExtra("from", from)
                                .putExtra("date", date)
                                .putExtra("time", time)
                                .putExtra("key", key)
                            startActivity(intent)

                        })
                        if (username == from) {
                            feeds.addView(v)
                        } else {
                            database.child("users/$username/blockedPost/$from")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (!snapshot.exists()) {
                                            if (publicPost == "true") {
                                                feeds.addView(v)
                                            } else {
                                                database.child("users/$from/friends/$username")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if (snapshot.exists()) {
                                                                feeds.addView(v)
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    } else {
                        if (postsCount[0] == 0) {
                            noPostsImage.visibility = View.VISIBLE
                            noPostsText.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }

    fun setRequestColor(constraintLayout: ConstraintLayout) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
                val colorFile: File
                colorFile = if (isDarkMode) {
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
                    constraintLayout.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("toolbarColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.homeDark)
    }

    val isDarkMode: Boolean
        get() {
            val db = DBHelper(this, null)
            var darkMode = false
            when (db.getSettingString("app_theme", "system")) {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.open_profile_menu, menu)
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()

            }

            R.id.openProfileReportUser -> reportUser(otherUsername)
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class cardHolder {
        var title: TextView? = null
        var message: TextView? = null
        var summary: TextView? = null
        var card: CardView? = null
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