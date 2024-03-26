package de.ljz.talktome.old.group

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import de.ljz.talktome.R
import de.ljz.talktome.old.newDatabase.DBHelper
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class NewGroup : AppCompatActivity() {
    var database = FirebaseDatabase.getInstance().reference
    var username: String? = null
    var logo: ByteArray? = null
    var banner: ByteArray? = null
    var cropType: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.new_group)
        val db = DBHelper(this, null)
        username = db.getCurrentUsername()
        val button = findViewById<Button>(R.id.newGroupConfirmButton)
        button.setOnClickListener { v: View? -> newGroupConfirm() }
        setButtonColor(button)
        val toolbar = findViewById<Toolbar>(R.id.newGroupToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        val publicGroupSwitch = findViewById<Switch>(R.id.publicGroupSwitch)
        setSwitchColor(publicGroupSwitch)
        val groupBanner = findViewById<ImageView>(R.id.newGroupBanner)
        val groupLogo = findViewById<ImageView>(R.id.newGroupLogo)
        groupBanner.setOnClickListener { v: View? -> requestBanner() }
        groupLogo.setOnClickListener { v: View? -> requestLogo() }
    }

    fun newGroupConfirm() {
        val canAgo = booleanArrayOf(true)
        val textGroupName = findViewById<TextInputLayout>(R.id.textGroupName)
        textGroupName.error = null
        val groupName = textGroupName.editText!!.text.toString().trim { it <= ' ' }
        val textGroupInfo = findViewById<TextInputLayout>(R.id.textGroupInfo)
        var groupInfo = textGroupInfo.editText!!.text.toString().trim { it <= ' ' }
        val publicGroupSwitch = findViewById<Switch>(R.id.publicGroupSwitch)
        val publicGroupBool = publicGroupSwitch.isChecked
        val publicGroup = publicGroupBool.toString()
        val groupKey = database.child("groups").push().key
        database.child("groups/$groupKey")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        canAgo[0] = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        if (groupName.isEmpty()) {
            textGroupName.error = "Darf nicht leer sein."
            canAgo[0] = false
        }
        if (groupInfo.isEmpty()) {
            groupInfo = "Dies ist eine Talk to me-Gruppe."
        }
        if (canAgo[0] == true) {
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.wait8)
                .setCancelable(false)
            val alert = builder.show()
            val group = Group(groupInfo, groupName, publicGroup, username)
            database.child("groups").child(groupKey!!).setValue(group)
                .addOnSuccessListener { unused: Void? ->
                    database.child("groups/$groupKey").child("members").child(
                        username!!
                    ).setValue("1").addOnSuccessListener { unused1: Void? ->
                        val date = Date()
                        val time = SimpleDateFormat("HH:mm").format(date)
                        val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
                        val mDatabase = FirebaseDatabase.getInstance().reference
                        val push = mDatabase.child("groups").push().key
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
                                    this@NewGroup,
                                    "Fehler!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                                    logoRef.updateMetadata(metadata)
                                        .addOnSuccessListener { storageMetadata: StorageMetadata? ->
                                            if (banner != null) {
                                                val uploadTask2 = bannerRef.putBytes(banner!!)
                                                uploadTask2.addOnFailureListener { exception: Exception? ->
                                                    Toast.makeText(
                                                        this@NewGroup,
                                                        "Fehler!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                    .addOnSuccessListener { taskSnapshot2: UploadTask.TaskSnapshot? ->
                                                        bannerRef.updateMetadata(metadata)
                                                            .addOnSuccessListener { storageMetadata2: StorageMetadata? ->
                                                                val groupMessage = Message(
                                                                    "system",
                                                                    "$username hat die Gruppe erstellt",
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
                                                                        finish()
                                                                        //overridePendingTransition(R.anim.fade_out, R.anim.slide_up)
                                                                    }
                                                                    .addOnFailureListener { e: Exception ->
                                                                        Toast.makeText(this@NewGroup, "Nachricht konnte nicht gesendet werden! ${e.message}".trimIndent(), Toast.LENGTH_LONG).show()
                                                                    }
                                                            }
                                                    }
                                            } else {
                                                val groupMessage = Message(
                                                    "system",
                                                    "$username hat die Gruppe erstellt",
                                                    time,
                                                    messageDate,
                                                    "SENT",
                                                    notifyId
                                                )
                                                mDatabase.child("groups/$groupKey/messages")
                                                    .child(push!!).setValue(groupMessage)
                                                    .addOnSuccessListener { aVoid: Void? ->
                                                        alert.cancel()
                                                        finish()
                                                    }
                                                    .addOnFailureListener { e: Exception ->
                                                        Toast.makeText(this@NewGroup, " Nachricht konnte nicht gesendet werden! ${e.message}".trimIndent(), Toast.LENGTH_LONG).show()
                                                    }
                                            }
                                        }
                                }
                        } else if (banner != null) {
                            val uploadTask = bannerRef.putBytes(banner!!)
                            uploadTask.addOnFailureListener {
                                Toast.makeText(
                                    this@NewGroup,
                                    "Fehler!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                                .addOnSuccessListener {
                                    bannerRef.updateMetadata(metadata)
                                        .addOnSuccessListener {
                                            if (logo != null) {
                                                val uploadTask2 = logoRef.putBytes(logo!!)
                                                uploadTask2.addOnFailureListener {
                                                    Toast.makeText(
                                                        this@NewGroup,
                                                        "Fehler!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                    .addOnSuccessListener { taskSnapshot2: UploadTask.TaskSnapshot? ->
                                                        logoRef.updateMetadata(metadata)
                                                            .addOnSuccessListener { storageMetadata2: StorageMetadata? ->
                                                                val groupMessage = Message(
                                                                    "system",
                                                                    "$username hat die Gruppe erstellt",
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
                                                                        finish()
                                                                    }
                                                                    .addOnFailureListener { e: Exception ->
                                                                        Toast.makeText(
                                                                            this@NewGroup, "Nachricht konnte nicht gesendet werden! ${e.message}".trimIndent(), Toast.LENGTH_LONG
                                                                        ).show()
                                                                    }
                                                            }
                                                    }
                                            } else {
                                                val groupMessage = Message(
                                                    "system",
                                                    "$username hat die Gruppe erstellt",
                                                    time,
                                                    messageDate,
                                                    "SENT",
                                                    notifyId
                                                )
                                                mDatabase.child("groups/$groupKey/messages")
                                                    .child(push!!).setValue(groupMessage)
                                                    .addOnSuccessListener { aVoid: Void? ->
                                                        alert.cancel()
                                                        finish()
                                                    }
                                                    .addOnFailureListener { e: Exception ->
                                                        Toast.makeText(
                                                            this@NewGroup, "Nachricht konnte nicht gesendet werden! ${e.message}".trimIndent(), Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                            }
                                        }
                                }
                        } else {
                            val groupMessage = Message(
                                "system",
                                "$username hat die Gruppe erstellt",
                                time,
                                messageDate,
                                "SENT",
                                notifyId
                            )
                            mDatabase.child("groups/$groupKey/messages").child(push!!)
                                .setValue(groupMessage).addOnSuccessListener { aVoid: Void? ->
                                alert.cancel()
                                finish()
                            }
                                .addOnFailureListener { e: Exception ->
                                    Toast.makeText(this@NewGroup, "Nachricht konnte nicht gesendet werden! ${e.message}".trimIndent(), Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this,
                        "Fehler: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    fun requestBanner() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Banner")
            .setPositiveButton("Neu auswählen") { dialog: DialogInterface?, which: Int ->
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                cropType = "banner"
                startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    fun requestLogo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logo")
            .setPositiveButton("Neu auswählen") { dialog: DialogInterface?, which: Int ->
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                cropType = "logo"
                startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
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
                            val image = findViewById<CircleImageView>(R.id.newGroupLogo)
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
                            val image = findViewById<ImageView>(R.id.newGroupBanner)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@NewGroup, e.message, Toast.LENGTH_LONG).show()
            } catch (e: Error) {
                Toast.makeText(this@NewGroup, e.message, Toast.LENGTH_LONG).show()
            }
        } else {
            //Toast.makeText(this, resultCode, Toast.LENGTH_SHORT).show();
        }
    }

    @IgnoreExtraProperties
    class Message(
        var from: String,
        var message: String,
        var time: String,
        var date: String,
        var status: String,
        var notifyId: String
    )

    @IgnoreExtraProperties
    class Group(var info: String, var name: String, var publicGroup: String, var by: String?)

    fun setToolbarColor(toolbar: Toolbar) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false)) {
            try {
                var json: String? = null
                val colorFile: File = if (isDarkMode) {
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
                    toolbar.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("toolbarColor").toString().toInt())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val window = window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.statusBarColor = `object`.opt("toolbarColor").toString().toInt()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setSwitchColor(switch1: Switch) {
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
                val `object` = JSONObject(jsn.opt(5).toString())
                if (`object`.opt("switchColor").toString().toInt() != 0) {
                    switch1.trackTintList =
                        ColorStateList.valueOf(`object`.opt("switchColor").toString().toInt())
                    switch1.thumbTintList =
                        ColorStateList.valueOf(`object`.opt("switchColor").toString().toInt())
                }
            } catch (e: Exception) {
                AlertDialog.Builder(this)
                    .setTitle("SwitchPreferenceError:")
                    .setMessage(e.message)
                    .setPositiveButton("Verstanden", null)
                    .show()
            }
        }
    }

    fun setButtonColor(button: Button) {
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
                val `object` = JSONObject(jsn.opt(0).toString())
                if (`object`.opt("buttonColor").toString().toInt() != 0) {
                    button.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("buttonColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setLightMode() {
        setTheme(R.style.chatLight)
    }

    fun setDarkMode() {
        setTheme(R.style.chatDark)
    }

    val isDarkMode: Boolean
        get() {
            var darkMode = false
            val theme =
                PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")
            when (theme) {
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