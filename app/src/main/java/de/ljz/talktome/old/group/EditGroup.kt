package de.ljz.talktome.old.group

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors.setButtonColor
import de.ljz.talktome.old.colors.Colors.setSwitchColor
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.newDatabase.DBHelper
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class EditGroup : AppCompatActivity() {
    var database = FirebaseDatabase.getInstance().reference
    var username: String? = null
    var groupKey: String? = null
    var toolbar: Toolbar? = null
    var logo: ByteArray? = null
    var banner: ByteArray? = null
    var cropType: String? = null
    var groupName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.edit_group)

        val db = DBHelper(this, null)

        toolbar = findViewById(R.id.publicEditGroupToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar!!)
        username = db.getCurrentUsername()
        val publicSwitch = findViewById<Switch>(R.id.publicEditGroupSwitch)
        setSwitchColor(this, publicSwitch)
        val button = findViewById<Button>(R.id.publicEditGroupSaveButton)
        button.setOnClickListener { v: View? -> saveChanges() }
        setButtonColor(this, button)
        groupKey = intent.extras!!["groupKey"].toString()
        val groupBanner = findViewById<ImageView>(R.id.editGroupBanner)
        val groupLogo = findViewById<ImageView>(R.id.editGroupLogo)
        groupBanner.setOnClickListener { v: View? -> requestBanner() }
        groupLogo.setOnClickListener { v: View? -> requestLogo() }
        information
    }

    val information: Unit
        get() {
            val textGroupName = findViewById<TextInputLayout>(R.id.publicGroupEditName)
            val textGroupInfo = findViewById<TextInputLayout>(R.id.publicGroupEditInfo)
            val publicGroupSwitch = findViewById<Switch>(R.id.publicEditGroupSwitch)
            getGroupLogo(groupKey)
            getGroupBanner(groupKey)
            database.child("groups/$groupKey")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupName = snapshot.child("name").value.toString()
                        val groupInfo = snapshot.child("info").value.toString()
                        val publicGroup =
                            java.lang.Boolean.parseBoolean(snapshot.child("publicGroup").value.toString())
                        publicGroupSwitch.isChecked = publicGroup
                        textGroupName.editText!!.setText(groupName)
                        textGroupInfo.editText!!.setText(groupInfo)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    fun removeLogo() {
        AlertDialog.Builder(this)
            .setMessage(R.string.editGroup_deleteLogo)
            .setPositiveButton(R.string.editGroup_delete) { dialog: DialogInterface?, which: Int ->
                val builder = AlertDialog.Builder(this)
                builder.setView(R.layout.wait)
                    .setCancelable(false)
                val alert = builder.show()
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val pb = storageRef.child("groupLogos/$groupKey.jpg")
                pb.delete().addOnSuccessListener { unused: Void? ->
                    val image = findViewById<ImageView>(R.id.editGroupLogo)
                    image.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@EditGroup,
                            R.drawable.ic_launcher_playstore
                        )
                    )
                    val date = Date()
                    val time = SimpleDateFormat("HH:mm").format(date)
                    val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
                    val mDatabase = FirebaseDatabase.getInstance().reference
                    val push = mDatabase.push().key
                    val notifyId = generateRandom(15).toInt().toString()
                    val groupMessage = NewGroup.Message(
                        "system",
                        "$username hat das Logo entfernt",
                        time,
                        messageDate,
                        "SENT",
                        notifyId
                    )
                    mDatabase.child("groups/$groupKey/messages").child(push!!)
                        .setValue(groupMessage)
                        .addOnSuccessListener { aVoid: Void? -> alert.cancel() }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                this@EditGroup, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                            ).show()
                        }
                }
                    .addOnFailureListener { e: Exception? ->
                        alert.cancel()
                        Toast.makeText(
                            this@EditGroup,
                            "Das Logo wurde bereits entfernt.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("abbrechen", null)
            .show()
    }

    fun removeBanner() {
        AlertDialog.Builder(this)
            .setMessage(R.string.editGroup_deleteBanner)
            .setPositiveButton(R.string.editGroup_delete) { dialog: DialogInterface?, which: Int ->
                val builder = AlertDialog.Builder(this)
                builder.setView(R.layout.wait)
                    .setCancelable(false)
                val alert = builder.show()
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val pb = storageRef.child("groupBanners/$groupKey.jpg")
                pb.delete().addOnSuccessListener { unused: Void? ->
                    val image = findViewById<ImageView>(R.id.editGroupBanner)
                    image.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@EditGroup,
                            R.drawable.background_light
                        )
                    )
                    val date = Date()
                    val time = SimpleDateFormat("HH:mm").format(date)
                    val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
                    val mDatabase = FirebaseDatabase.getInstance().reference
                    val push = mDatabase.push().key
                    val notifyId = generateRandom(15).toInt().toString()
                    val groupMessage = NewGroup.Message(
                        "system",
                        "$username hat das Banner entfernt",
                        time,
                        messageDate,
                        "SENT",
                        notifyId
                    )
                    mDatabase.child("groups/$groupKey/messages").child(push!!)
                        .setValue(groupMessage)
                        .addOnSuccessListener { aVoid: Void? -> alert.cancel() }
                        .addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                this@EditGroup, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                            ).show()
                        }
                }
                    .addOnFailureListener { e: Exception? ->
                        alert.cancel()
                        Toast.makeText(
                            this@EditGroup,
                            "Das Banner wurde bereits entfernt.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("abbrechen", null)
            .show()
    }

    fun requestBanner() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.editGroup_bannerTitle)
            .setPositiveButton(R.string.editGroup_show) { dialog: DialogInterface?, which: Int -> showBanner() }
            .setNegativeButton(R.string.editGroup_chooseNew) { dialog: DialogInterface?, which: Int ->
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                cropType = "banner"
                startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
            }
            .setNeutralButton(R.string.editGroup_remove) { dialog: DialogInterface?, which: Int -> removeBanner() }
            .show()
    }

    fun requestLogo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logo")
            .setPositiveButton(R.string.editGroup_show) { dialog: DialogInterface?, which: Int -> showLogo() }
            .setNegativeButton(R.string.editGroup_chooseNew) { dialog: DialogInterface?, which: Int ->
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                cropType = "logo"
                startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
            }
            .setNeutralButton(R.string.editGroup_remove) { dialog: DialogInterface?, which: Int -> removeLogo() }
            .show()
    }

    fun showLogo() {
        val intent = Intent(this, OpenGroupLogo::class.java)
        intent.putExtra("groupKey", groupKey)
            .putExtra("groupName", groupName)
        startActivity(intent)
    }

    fun showBanner() {
        val intent = Intent(this, OpenGroupBanner::class.java)
        intent.putExtra("groupKey", groupKey)
            .putExtra("groupName", groupName)
        startActivity(intent)
    }

    fun getGroupLogo(groupKey: String?) {
        val profilePicture = findViewById<ImageView>(R.id.editGroupLogo)
        profilePicture.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_launcher_playstore
            )
        )
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
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
        val profilePicture = findViewById<ImageView>(R.id.editGroupBanner)
        profilePicture.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.background_light
            )
        )
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
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

    fun saveChanges() {
        var canAgo = true
        val publicGroupSwitch = findViewById<Switch>(R.id.publicEditGroupSwitch)
        val publicGroup = publicGroupSwitch.isChecked.toString()
        val textGroupName = findViewById<TextInputLayout>(R.id.publicGroupEditName)
        textGroupName.error = null
        val textGroupInfo = findViewById<TextInputLayout>(R.id.publicGroupEditInfo)
        textGroupInfo.error = null
        val groupName = textGroupName.editText!!.text.toString().trim { it <= ' ' }
        var groupInfo = textGroupInfo.editText!!.text.toString().trim { it <= ' ' }
        if (groupName.isEmpty()) {
            canAgo = false
            textGroupName.error = "Der Name darf nicht leer sein!"
        }
        if (groupInfo.isEmpty()) {
            groupInfo = "Dies ist eine Talk to me-Gruppe."
        }
        if (canAgo) {
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.wait)
                .setCancelable(false)
            val alert = builder.show()
            database.child("groups/$groupKey").child("info").setValue(groupInfo)
                .addOnSuccessListener { unused: Void? ->
                    database.child(
                        "groups/$groupKey"
                    ).child("name").setValue(groupName).addOnSuccessListener { unused1: Void? ->
                        database.child(
                            "groups/$groupKey"
                        ).child("publicGroup").setValue(publicGroup)
                            .addOnSuccessListener { unused2: Void? ->
                                val date = Date()
                                val time = SimpleDateFormat("HH:mm").format(date)
                                val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
                                val mDatabase = FirebaseDatabase.getInstance().reference
                                val push = mDatabase.push().key
                                val notifyId = generateRandom(15).toInt().toString()
                                val storage = FirebaseStorage.getInstance()
                                val storageRef = storage.reference
                                val logoRef = storageRef.child("groupLogos/$groupKey.jpg")
                                val bannerRef = storageRef.child("groupBanners/$groupKey.jpg")
                                val picDate = SimpleDateFormat("dd.MM.yyyy_HH:mm:ss").format(date)
                                val metadata = StorageMetadata.Builder()
                                    .setCustomMetadata("date", picDate)
                                    .build()
                                if (logo != null) {
                                    val uploadTask = logoRef.putBytes(logo!!)
                                    uploadTask.addOnFailureListener { exception: Exception? ->
                                        Toast.makeText(
                                            this@EditGroup,
                                            "Fehler!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                        .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                                            logoRef.updateMetadata(metadata)
                                                .addOnSuccessListener { storageMetadata: StorageMetadata? ->
                                                    if (banner != null) {
                                                        val uploadTask2 =
                                                            bannerRef.putBytes(banner!!)
                                                        uploadTask2.addOnFailureListener { exception: Exception? ->
                                                            Toast.makeText(
                                                                this@EditGroup,
                                                                "Fehler!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                            .addOnSuccessListener { taskSnapshot2: UploadTask.TaskSnapshot? ->
                                                                bannerRef.updateMetadata(metadata)
                                                                    .addOnSuccessListener { storageMetadata2: StorageMetadata? ->
                                                                        val groupMessage =
                                                                            NewGroup.Message(
                                                                                "system",
                                                                                "$username hat die Gruppe bearbeitet",
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
                                                                                Toast.makeText(
                                                                                    this,
                                                                                    "Änderungen gespeichert.",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                                finish()
                                                                            }
                                                                            .addOnFailureListener { e: Exception ->
                                                                                Toast.makeText(
                                                                                    this@EditGroup,
                                                                                    """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(),
                                                                                    Toast.LENGTH_LONG
                                                                                ).show()
                                                                            }
                                                                    }
                                                            }
                                                    } else {
                                                        val groupMessage = NewGroup.Message(
                                                            "system",
                                                            "$username hat die Gruppe bearbeitet",
                                                            time,
                                                            messageDate,
                                                            "SENT",
                                                            notifyId
                                                        )
                                                        mDatabase.child("groups/$groupKey/messages")
                                                            .child(push!!).setValue(groupMessage)
                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                alert.cancel()
                                                                Toast.makeText(
                                                                    this,
                                                                    "Änderungen gespeichert.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                finish()
                                                            }
                                                            .addOnFailureListener { e: Exception ->
                                                                Toast.makeText(
                                                                    this@EditGroup, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                    }
                                                }
                                        }
                                } else if (banner != null) {
                                    val uploadTask = bannerRef.putBytes(banner!!)
                                    uploadTask.addOnFailureListener { exception: Exception? ->
                                        Toast.makeText(
                                            this@EditGroup,
                                            "Fehler!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                        .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                                            bannerRef.updateMetadata(metadata)
                                                .addOnSuccessListener { storageMetadata: StorageMetadata? ->
                                                    if (logo != null) {
                                                        val uploadTask2 = logoRef.putBytes(logo!!)
                                                        uploadTask2.addOnFailureListener { exception: Exception? ->
                                                            Toast.makeText(
                                                                this@EditGroup,
                                                                "Fehler!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                            .addOnSuccessListener { taskSnapshot2: UploadTask.TaskSnapshot? ->
                                                                logoRef.updateMetadata(metadata)
                                                                    .addOnSuccessListener { storageMetadata2: StorageMetadata? ->
                                                                        val groupMessage =
                                                                            NewGroup.Message(
                                                                                "system",
                                                                                "$username hat die Gruppe bearbeitet",
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
                                                                                Toast.makeText(
                                                                                    this,
                                                                                    "Änderungen gespeichert.",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                                finish()
                                                                            }
                                                                            .addOnFailureListener { e: Exception ->
                                                                                Toast.makeText(
                                                                                    this@EditGroup,
                                                                                    """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(),
                                                                                    Toast.LENGTH_LONG
                                                                                ).show()
                                                                            }
                                                                    }
                                                            }
                                                    } else {
                                                        val groupMessage = NewGroup.Message(
                                                            "system",
                                                            "$username hat die Gruppe bearbeitet",
                                                            time,
                                                            messageDate,
                                                            "SENT",
                                                            notifyId
                                                        )
                                                        mDatabase.child("groups/$groupKey/messages")
                                                            .child(push!!).setValue(groupMessage)
                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                alert.cancel()
                                                                Toast.makeText(
                                                                    this,
                                                                    "Änderungen gespeichert.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                finish()
                                                            }
                                                            .addOnFailureListener { e: Exception ->
                                                                Toast.makeText(
                                                                    this@EditGroup, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                    }
                                                }
                                        }
                                } else {
                                    val groupMessage = NewGroup.Message(
                                        "system",
                                        "$username hat die Gruppe bearbeitet",
                                        time,
                                        messageDate,
                                        "SENT",
                                        notifyId
                                    )
                                    mDatabase.child("groups/$groupKey/messages").child(push!!)
                                        .setValue(groupMessage)
                                        .addOnSuccessListener { aVoid: Void? ->
                                            alert.cancel()
                                            Toast.makeText(
                                                this,
                                                "Änderungen gespeichert.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }
                                        .addOnFailureListener { e: Exception ->
                                            Toast.makeText(
                                                this@EditGroup, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                            }
                    }
                }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                if (cropType == "logo") {
                    val imageUri = data!!.data
                    val cropLayout = findViewById<ConstraintLayout>(R.id.cropLayout)
                    cropLayout.visibility = View.VISIBLE
                    val cropImageButton = findViewById<Button>(R.id.cropImageButton)
                    cropImageButton.visibility = View.VISIBLE
                    val cancelButton = findViewById<Button>(R.id.cancelCroppingImageButton)
                    cancelButton.visibility = View.VISIBLE
                    cancelButton.setOnClickListener { v: View? ->
                        cropImageButton.visibility = View.GONE
                        cropLayout.visibility = View.GONE
                    }
                    cropImageButton.setOnClickListener { v: View? ->
                        cropImageButton.visibility = View.GONE
                        cropLayout.visibility = View.GONE
                        val baos = ByteArrayOutputStream()
                        logo = baos.toByteArray()
                        if (logo!!.size > 3000000) {
                            Toast.makeText(this, "Bild ist zu groß!", Toast.LENGTH_SHORT).show()
                        } else {
                            val image = findViewById<CircleImageView>(R.id.editGroupLogo)
                        }
                    }
                } else if (cropType == "banner") {
                    val imageUri = data!!.data
                    val cropLayout = findViewById<ConstraintLayout>(R.id.cropLayout)
                    cropLayout.visibility = View.VISIBLE
                    val cropImageButton = findViewById<Button>(R.id.cropImageButton)
                    cropImageButton.visibility = View.VISIBLE
                    val cancelButton = findViewById<Button>(R.id.cancelCroppingImageButton)
                    cancelButton.visibility = View.VISIBLE
                    cancelButton.setOnClickListener { v: View? ->
                        cropImageButton.visibility = View.GONE
                        cropLayout.visibility = View.GONE
                    }
                    cropImageButton.setOnClickListener { v: View? ->
                        cropImageButton.visibility = View.GONE
                        cropLayout.visibility = View.GONE
                        val baos = ByteArrayOutputStream()
                        banner = baos.toByteArray()
                        if (banner!!.size > 3000000) {
                            Toast.makeText(this, "Bild ist zu groß!", Toast.LENGTH_SHORT).show()
                        } else {
                            val image = findViewById<ImageView>(R.id.editGroupBanner)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditGroup, e.message, Toast.LENGTH_LONG).show()
            } catch (e: Error) {
                Toast.makeText(this@EditGroup, e.message, Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("Talk to me", resultCode.toString())
        }
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