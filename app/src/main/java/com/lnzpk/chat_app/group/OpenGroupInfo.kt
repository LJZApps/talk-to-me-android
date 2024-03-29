package com.lnzpk.chat_app.group

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.colors.Colors.setButtonColor
import com.lnzpk.chat_app.colors.Colors.setToolbarColor
import com.lnzpk.chat_app.newDatabase.DBHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class OpenGroupInfo : AppCompatActivity() {
    var username: String? = null
    var database: DatabaseReference? = null
    var toolbar: Toolbar? = null
    var groupKey: String? = null
    var groupName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.open_group_info)
        val db = DBHelper(this, null)
        username = db.getCurrentUsername()
        database = FirebaseDatabase.getInstance().reference
        toolbar = findViewById(R.id.publicGroupInfoToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar!!)
        val button = findViewById<Button>(R.id.publicGroupInfoJoinButton)
        setButtonColor(this, button)
        val deleteButton = findViewById<Button>(R.id.publicGroupDelete)
        setButtonColor(this, deleteButton)
        deleteButton.setOnClickListener { v: View? -> deleteGroup() }
        val inviteButton = findViewById<Button>(R.id.publicGroupInfoInvite)
        setButtonColor(this, inviteButton)
        inviteButton.setOnClickListener { v: View? -> invite() }
        val openChat = findViewById<Button>(R.id.publicGroupInfoOpenChatButton)
        groupKey = intent.extras!!["groupKey"].toString()
        openChat.setOnClickListener { v: View? ->
            val comeFrom = intent.extras!!["comeFrom"].toString()
            when (comeFrom) {
                "home", "privateChat", "groupList" -> {
                    val intent = Intent(this, OpenGroupChat::class.java)
                    intent.putExtra("groupKey", groupKey)
                        .putExtra("publicGroup", true)
                    startActivity(intent)
                    finish()
                }
                "chat" -> finish()
            }
        }
        setButtonColor(this, openChat)
        val editGroup = findViewById<Button>(R.id.publicGroupEditButton)
        editGroup.visibility = View.GONE
        setButtonColor(this, editGroup)
        val groupMembers = findViewById<Button>(R.id.publicGroupMembersButton)
        groupMembers.visibility = View.GONE
        setButtonColor(this, groupMembers)
        information
    }

    fun deleteGroup() {
        AlertDialog.Builder(this)
            .setTitle("Gruppe löschen?")
            .setMessage("Bist du dir sicher, das du diese Gruppe löschen möchtest?\n\nHiermit werden alle Nachrichten gelöscht und alle Mitglieder entfernt.")
            .setPositiveButton("löschen") { dialog: DialogInterface?, which: Int ->
                val builder = AlertDialog.Builder(this)
                builder.setView(R.layout.wait)
                    .setCancelable(false)
                val alert = builder.show()
                database!!.child("groups/$groupKey").ref.removeValue()
                    .addOnSuccessListener { unused: Void? ->
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        val logo = storageRef.child("groupLogos/$groupKey.jpg")
                        val banner = storageRef.child("groupBanners/$groupKey.jpg")
                        logo.downloadUrl.addOnSuccessListener { uri: Uri? ->
                            logo.delete().addOnSuccessListener { unused1: Void? ->
                                banner.downloadUrl.addOnSuccessListener { uri2: Uri? ->
                                    banner.delete().addOnSuccessListener { unused2: Void? ->
                                        alert.cancel()
                                        Toast.makeText(
                                            this@OpenGroupInfo,
                                            "Gruppe gelöscht.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                }.addOnFailureListener { e: Exception? ->
                                    alert.cancel()
                                    Toast.makeText(
                                        this@OpenGroupInfo,
                                        "Gruppe gelöscht.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        }
                            .addOnFailureListener { e: Exception? ->
                                banner.downloadUrl.addOnSuccessListener { uri2: Uri? ->
                                    banner.delete().addOnSuccessListener { unused2: Void? ->
                                        alert.cancel()
                                        Toast.makeText(
                                            this@OpenGroupInfo,
                                            "Gruppe gelöscht.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                }.addOnFailureListener { e2: Exception? ->
                                    alert.cancel()
                                    Toast.makeText(
                                        this@OpenGroupInfo,
                                        "Gruppe gelöscht.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                    }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showLogo() {
        val intent = Intent(this, OpenGroupLogo::class.java)
        intent.putExtra("groupKey", groupKey)
            .putExtra("groupName", groupName)
        startActivity(intent)
    }

    private fun showBanner() {
        val intent = Intent(this, OpenGroupBanner::class.java)
        intent.putExtra("groupKey", groupKey)
            .putExtra("groupName", groupName)
        startActivity(intent)
    }

    fun getGroupLogo(groupKey: String?) {
        val profilePicture = findViewById<ImageView>(R.id.openGroupLogo)
        profilePicture.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_launcher_playstore
            )
        )
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        profilePicture.setOnClickListener { v: View? -> showLogo() }
        val getPic = storageRef.child("groupLogos/$groupKey.jpg")
        try {
            val picture = File("$cacheDir/groupLogos/$groupKey.jpg")
            if (picture.exists()) {
                val is1: InputStream = FileInputStream(picture)
                val size1 = is1.available()
                val picBuffer = ByteArray(size1)
                is1.read(picBuffer)
                is1.close()
                val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                val d: Drawable = BitmapDrawable(resources, bmp)
                profilePicture.setImageDrawable(d)
            }
        } catch (e: Exception) {
        }
        getPic.downloadUrl.addOnSuccessListener { uri: Uri? ->
            getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                val date = storageMetadata.getCustomMetadata("date")
                try {
                    val config = File("$cacheDir/groupLogos/$groupKey.txt")
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
                                val d: Drawable = BitmapDrawable(resources, bmp)
                                profilePicture.setImageDrawable(d)
                                val myDir = File("$cacheDir/groupLogos")
                                if (!myDir.exists()) {
                                    myDir.mkdir()
                                }
                                val config1 = "$groupKey.txt"
                                val picture = "$groupKey.jpg"
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
                                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            val picture = File("$cacheDir/groupLogos/$groupKey.jpg")
                            val is1: InputStream = FileInputStream(picture)
                            val size1 = is1.available()
                            val picBuffer = ByteArray(size1)
                            is1.read(picBuffer)
                            is1.close()
                            val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                            val d: Drawable = BitmapDrawable(resources, bmp)
                            profilePicture.setImageDrawable(d)
                        }
                    } else {
                        getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                            val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                            val d: Drawable = BitmapDrawable(resources, bmp)
                            profilePicture.setImageDrawable(d)
                            val myDir = File("$cacheDir/groupLogos")
                            if (!myDir.exists()) {
                                myDir.mkdir()
                            }
                            val config1 = "$groupKey.txt"
                            val picture = "$groupKey.jpg"
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
                                    val configS = FileOutputStream(configFile.absolutePath, true)
                                    configS.write(date!!.toByteArray())
                                    configS.close()
                                    val pictureS = FileOutputStream(pictureFile.absolutePath, true)
                                    pictureS.write(bytes1)
                                    pictureS.close()
                                } catch (e: Exception) {
                                }
                            }
                        }
                            .addOnFailureListener { exception: Exception ->
                                val error = exception.localizedMessage
                                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e: Exception? ->
            val myDir = File("$cacheDir/groupLogos")
            val picture = "$groupKey.jpg"
            val config1 = "$groupKey.txt"
            val pictureFile = File(myDir, picture)
            if (pictureFile.exists()) {
                pictureFile.delete()
            }
            val configFile = File(myDir, config1)
            if (configFile.exists()) {
                configFile.delete()
            }
            profilePicture.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_launcher_playstore
                )
            )
        }
    }

    fun getGroupBanner(groupKey: String?) {
        val profilePicture = findViewById<ImageView>(R.id.openGroupBanner)
        profilePicture.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.background_light
            )
        )
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        profilePicture.setOnClickListener { v: View? -> showBanner() }
        val getPic = storageRef.child("groupBanners/$groupKey.jpg")
        try {
            val picture = File("$cacheDir/groupBanners/$groupKey.jpg")
            if (picture.exists()) {
                val is1: InputStream = FileInputStream(picture)
                val size1 = is1.available()
                val picBuffer = ByteArray(size1)
                is1.read(picBuffer)
                is1.close()
                val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                val d: Drawable = BitmapDrawable(resources, bmp)
                profilePicture.setImageDrawable(d)
            }
        } catch (e: Exception) {
        }
        getPic.downloadUrl.addOnSuccessListener { uri: Uri? ->
            getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                val date = storageMetadata.getCustomMetadata("date")
                try {
                    val config = File("$cacheDir/groupBanners/$groupKey.txt")
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
                                val d: Drawable = BitmapDrawable(resources, bmp)
                                profilePicture.setImageDrawable(d)
                                val myDir = File("$cacheDir/groupBanners")
                                if (!myDir.exists()) {
                                    myDir.mkdir()
                                }
                                val config1 = "$groupKey.txt"
                                val picture = "$groupKey.jpg"
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
                                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            val picture = File("$cacheDir/groupBanners/$groupKey.jpg")
                            val is1: InputStream = FileInputStream(picture)
                            val size1 = is1.available()
                            val picBuffer = ByteArray(size1)
                            is1.read(picBuffer)
                            is1.close()
                            val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                            val d: Drawable = BitmapDrawable(resources, bmp)
                            profilePicture.setImageDrawable(d)
                        }
                    } else {
                        getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                            val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                            val d: Drawable = BitmapDrawable(resources, bmp)
                            profilePicture.setImageDrawable(d)
                            val myDir = File("$cacheDir/groupBanners")
                            if (!myDir.exists()) {
                                myDir.mkdir()
                            }
                            val config1 = "$groupKey.txt"
                            val picture = "$groupKey.jpg"
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
                                    val configS = FileOutputStream(configFile.absolutePath, true)
                                    configS.write(date!!.toByteArray())
                                    configS.close()
                                    val pictureS = FileOutputStream(pictureFile.absolutePath, true)
                                    pictureS.write(bytes1)
                                    pictureS.close()
                                } catch (e: Exception) {
                                }
                            }
                        }
                            .addOnFailureListener { exception: Exception ->
                                val error = exception.localizedMessage
                                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e: Exception? ->
            val myDir = File("$cacheDir/groupBanners")
            val picture = "$groupKey.jpg"
            val config1 = "$groupKey.txt"
            val pictureFile = File(myDir, picture)
            if (pictureFile.exists()) {
                pictureFile.delete()
            }
            val configFile = File(myDir, config1)
            if (configFile.exists()) {
                configFile.delete()
            }
            profilePicture.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.background_light
                )
            )
        }
    }

    val information: Unit
        get() {
            val nameView = findViewById<TextView>(R.id.publicGroupInfoName)
            val infoView = findViewById<TextView>(R.id.publicGroupInfoInfo)
            val membersView = findViewById<TextView>(R.id.publicGroupInfoMembers)
            val messagesView = findViewById<TextView>(R.id.publicGroupInfoMessages)
            val joinButton = findViewById<Button>(R.id.publicGroupInfoJoinButton)
            val editGroup = findViewById<Button>(R.id.publicGroupEditButton)
            val groupMembers = findViewById<Button>(R.id.publicGroupMembersButton)
            val deleteGroup = findViewById<Button>(R.id.publicGroupDelete)
            val inviteButton = findViewById<Button>(R.id.publicGroupInfoInvite)
            getGroupLogo(groupKey)
            getGroupBanner(groupKey)
            database!!.child("groups/$groupKey")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            groupName = snapshot.child("name").value.toString()
                            val info = snapshot.child("info").value.toString()
                            if (snapshot.child("members").exists()) {
                                val members = snapshot.child("members").childrenCount.toString()
                                membersView.text = members
                                if (snapshot.child("members").child(username!!).exists()) {
                                    inviteButton.visibility = View.VISIBLE
                                    val admin =
                                        snapshot.child("members").child(username!!).value.toString()
                                    when (admin) {
                                        "1" -> {
                                            editGroup.visibility = View.VISIBLE
                                            editGroup.setOnClickListener { v: View? -> editGroup() }
                                            groupMembers.visibility = View.VISIBLE
                                            groupMembers.setOnClickListener { v: View? -> members() }
                                            deleteGroup.visibility = View.VISIBLE
                                            deleteGroup.setOnClickListener { v: View? -> deleteGroup() }
                                        }
                                        "0" -> {
                                            editGroup.visibility = View.GONE
                                            groupMembers.visibility = View.GONE
                                            deleteGroup.visibility = View.GONE
                                        }
                                    }
                                } else {
                                    inviteButton.visibility = View.GONE
                                    editGroup.visibility = View.GONE
                                    groupMembers.visibility = View.GONE
                                    deleteGroup.visibility = View.GONE
                                }
                            } else {
                                inviteButton.visibility = View.GONE
                                editGroup.visibility = View.GONE
                                groupMembers.visibility = View.GONE
                                deleteGroup.visibility = View.GONE
                                membersView.text = "0"
                            }
                            if (snapshot.child("messages").exists()) {
                                val messages = snapshot.child("messages").childrenCount.toString()
                                messagesView.text = messages
                            } else {
                                messagesView.text = "0"
                            }
                            nameView.text = groupName
                            if (!Colors.isDarkMode(this@OpenGroupInfo)) {
                                nameView.setTextColor(getColor(android.R.color.secondary_text_light))
                            }
                            infoView.text = info
                            if (snapshot.child("members/$username").exists()) {
                                joinButton.text = "Gruppe verlassen"
                                joinButton.setOnClickListener { v: View? ->
                                    AlertDialog.Builder(this@OpenGroupInfo)
                                        .setTitle("Gruppe verlassen")
                                        .setMessage("Möchtest du diese Gruppe wirklich verlassen?")
                                        .setPositiveButton("Ja") { dialog: DialogInterface?, which: Int ->
                                            val builder = AlertDialog.Builder(this@OpenGroupInfo)
                                            builder.setView(R.layout.wait)
                                                .setCancelable(false)
                                            val alert = builder.show()
                                            snapshot.child("members/$username").ref.removeValue()
                                                .addOnSuccessListener { unused: Void? ->
                                                    val date = Date()
                                                    val time =
                                                        SimpleDateFormat("HH:mm").format(date)
                                                    val messageDate =
                                                        SimpleDateFormat("dd.MM.yyyy").format(date)
                                                    val mDatabase =
                                                        FirebaseDatabase.getInstance().reference
                                                    val push = mDatabase.push().key
                                                    val notifyId =
                                                        generateRandom(15).toInt().toString()
                                                    information
                                                    val groupMessage = NewGroup.Message(
                                                        "system",
                                                        "$username hat die Gruppe verlassen",
                                                        time,
                                                        messageDate,
                                                        "SENT",
                                                        notifyId
                                                    )
                                                    mDatabase.child("groups/$groupKey/messages")
                                                        .child(push!!).setValue(groupMessage)
                                                        .addOnSuccessListener { aVoid: Void? ->
                                                            alert.cancel()
                                                            information
                                                        }
                                                        .addOnFailureListener { e: Exception ->
                                                            Toast.makeText(
                                                                this@OpenGroupInfo, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                }
                                                .addOnFailureListener { e: Exception ->
                                                    Toast.makeText(
                                                        this@OpenGroupInfo,
                                                        "Error: " + e.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        .setNegativeButton("Abbrechen", null)
                                        .show()
                                }
                            } else {
                                joinButton.text = "Gruppe beitreten"
                                joinButton.setOnClickListener { v: View? ->
                                    val builder = AlertDialog.Builder(this@OpenGroupInfo)
                                    builder.setView(R.layout.wait)
                                        .setCancelable(false)
                                    val alert = builder.show()
                                    database!!.child("groups/$groupKey/members")
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val membersCounts =
                                                    snapshot.childrenCount.toString().toInt()
                                                if (membersCounts > 0) {
                                                    database!!.child("groups/$groupKey/members/$username")
                                                        .setValue("0")
                                                        .addOnSuccessListener { unused: Void? ->
                                                            val date = Date()
                                                            val time =
                                                                SimpleDateFormat("HH:mm").format(
                                                                    date
                                                                )
                                                            val messageDate =
                                                                SimpleDateFormat("dd.MM.yyyy").format(
                                                                    date
                                                                )
                                                            val mDatabase =
                                                                FirebaseDatabase.getInstance().reference
                                                            val push = mDatabase.push().key
                                                            val notifyId =
                                                                generateRandom(15).toInt()
                                                                    .toString()
                                                            val groupMessage = NewGroup.Message(
                                                                "system",
                                                                "$username hat die Gruppe betreten",
                                                                time,
                                                                messageDate,
                                                                "SENT",
                                                                notifyId
                                                            )
                                                            mDatabase.child("groups/$groupKey/messages")
                                                                .child(push!!)
                                                                .setValue(groupMessage)
                                                                .addOnSuccessListener { aVoid: Void? ->
                                                                    alert.cancel()
                                                                    information
                                                                }
                                                                .addOnFailureListener { e: Exception ->
                                                                    Toast.makeText(
                                                                        this@OpenGroupInfo, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                        }
                                                        .addOnFailureListener { e: Exception ->
                                                            Toast.makeText(
                                                                this@OpenGroupInfo,
                                                                "Error: " + e.message,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                } else {
                                                    database!!.child("groups/$groupKey/members/$username")
                                                        .setValue("1")
                                                        .addOnSuccessListener { unused: Void? ->
                                                            val date = Date()
                                                            val time =
                                                                SimpleDateFormat("HH:mm").format(
                                                                    date
                                                                )
                                                            val messageDate =
                                                                SimpleDateFormat("dd.MM.yyyy").format(
                                                                    date
                                                                )
                                                            val mDatabase =
                                                                FirebaseDatabase.getInstance().reference
                                                            val push = mDatabase.push().key
                                                            val notifyId =
                                                                generateRandom(15).toInt()
                                                                    .toString()
                                                            val groupMessage = NewGroup.Message(
                                                                "system",
                                                                "$username hat die Gruppe betreten",
                                                                time,
                                                                messageDate,
                                                                "SENT",
                                                                notifyId
                                                            )
                                                            mDatabase.child("groups/$groupKey/messages")
                                                                .child(push!!)
                                                                .setValue(groupMessage)
                                                                .addOnSuccessListener { aVoid: Void? ->
                                                                    alert.cancel()
                                                                    information
                                                                }
                                                                .addOnFailureListener { e: Exception ->
                                                                    Toast.makeText(
                                                                        this@OpenGroupInfo, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                        }
                                                        .addOnFailureListener { e: Exception ->
                                                            Toast.makeText(
                                                                this@OpenGroupInfo,
                                                                "Error: " + e.message,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                }
                            }
                        } else {
                            inviteButton.visibility = View.GONE
                            deleteGroup.visibility = View.GONE
                            nameView.text = "Gruppe gelöscht."
                            infoView.text = "Gruppe gelöscht."
                            joinButton.text = "Gruppe beitreten"
                            joinButton.setOnClickListener { v: View? ->
                                Toast.makeText(
                                    this@OpenGroupInfo,
                                    "Dieser Gruppe kann nicht mehr beigetreten werden.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            messagesView.text = "0"
                            membersView.text = "0"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    fun invite() {
        val intent = Intent(this, InviteFriends::class.java)
        intent.putExtra("groupKey", groupKey)
            .putExtra("groupName", groupName)
        startActivity(intent)
    }

    fun editGroup() {
        val intent = Intent(this, EditGroup::class.java)
        intent.putExtra("groupKey", groupKey)
        startActivity(intent)
    }

    fun members() {
        val intent = Intent(this, GroupMembers::class.java)
        intent.putExtra("groupKey", groupKey)
        startActivity(intent)
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

    override fun onResume() {
        super.onResume()
        information
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