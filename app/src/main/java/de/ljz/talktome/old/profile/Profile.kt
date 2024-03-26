package de.ljz.talktome.old.profile

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.main.StartIcon
import de.ljz.talktome.old.newDatabase.DBHelper
import de.ljz.talktome.old.settings.ChangePass
import de.ljz.talktome.old.settings.profileSettings.ProfilePictureSettings
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity() {
    private var username: String? = null
    private var pic: ByteArray? = null
    var infoEmpty = false

    private var pictureSettingsLayout: ConstraintLayout? = null
    private var nameSettingsLayout: ConstraintLayout? = null
    private var informationSettingsLayout: ConstraintLayout? = null
    private var dangerSettingsLayout: ConstraintLayout? = null

    private var pictureSettingsSummary: TextView? = null
    private var nameSettingsSummary: TextView? = null
    private var informationSettingsSummary: TextView? = null
    private var dangerSettingsSummary: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.profile)

        window.navigationBarColor = Color.BLACK

        val db = DBHelper(this, null)

        username = db.getCurrentUsername()

        //pictureSettingsLayout = findViewById(R.id.profileSettingPictureImage)
        //nameSettingsLayout = findViewById(R.id.profileSettingName)
        //informationSettingsLayout = findViewById(R.id.profileSettingInformation)
        //dangerSettingsLayout = findViewById(R.id.profileSettingDanger)

        //pictureSettingsSummary = findViewById(R.id.profileSettingPictureSummary)
        //nameSettingsSummary = findViewById(R.id.profileSettingNameSummary)
        //informationSettingsSummary = findViewById(R.id.profileSettingInformationSummary)
        //dangerSettingsSummary = findViewById(R.id.profileSettingDangerSummary)

//        if (isDarkMode) {
//            pictureSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_dark)
//            nameSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_dark)
//            informationSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_dark)
//            dangerSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_dark)
//
//            pictureSettingsSummary!!.setTextColor(getColor(R.color.secondary_text_dark))
//            nameSettingsSummary!!.setTextColor(getColor(R.color.secondary_text_dark))
//            informationSettingsSummary!!.setTextColor(getColor(R.color.secondary_text_dark))
//            dangerSettingsSummary!!.setTextColor(getColor(R.color.secondary_text_dark))
//        } else {
//            pictureSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_light)
//            nameSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_light)
//            informationSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_light)
//            dangerSettingsLayout!!.background =
//                ContextCompat.getDrawable(this, R.drawable.chat_background_light)
//
//            pictureSettingsSummary!!.setTextColor(getColor(android.R.color.secondary_text_light))
//            nameSettingsSummary!!.setTextColor(getColor(android.R.color.secondary_text_light))
//            informationSettingsSummary!!.setTextColor(getColor(android.R.color.secondary_text_light))
//            dangerSettingsSummary!!.setTextColor(getColor(android.R.color.secondary_text_light))
//        }

//        nameSettingsLayout!!.setOnClickListener {
//            EditProfileNameSheet(username!!).apply {
//                show(supportFragmentManager, tag)
//            }
//        }

        //setClickActions()
        start()
    }

    private fun setClickActions() {
        pictureSettingsLayout!!.setOnClickListener {
            var intent = Intent(this, ProfilePictureSettings::class.java)
            startActivity(intent)
        }
    }

    fun start() {
        val db = DBHelper(this, null)
        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        val changeNameIL = findViewById<TextInputLayout>(R.id.changeName);
        val changeInfoIL = findViewById<TextInputLayout>(R.id.changeInfo);
        val changeNameInput = changeNameIL.editText
        val changeInfoInput = changeInfoIL.editText

        toolbar.subtitle = db.getCurrentUsername()
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        val database = FirebaseDatabase.getInstance().reference
        getProfilePicture(username)
        database.child("users/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nameText = snapshot.child("informations/name").value.toString()
                    if (snapshot.child("informations/name").exists()) {
                        changeNameInput!!.setText(nameText)
                        changeInfoInput!!.setText(snapshot.child("informations/info").value.toString())

                        if (snapshot.child("settings/staff").exists()) {
                            val isStaff =
                                java.lang.Boolean.parseBoolean(snapshot.child("settings/staff").value.toString())
                            if (isStaff) {
                                val verifiedImage =
                                    findViewById<ImageView>(R.id.verifiedProfileImage)
                                verifiedImage.visibility = View.VISIBLE
                            }
                        }
                    }
                    //nameSettingsSummary!!.text = nameText
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getProfilePicture(username: String?) {
        val profilePicture = findViewById<ImageView>(R.id.profilePic)
        profilePicture.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_no_profile_picture
            )
        )
        profilePicture.setOnClickListener {
            requestPicture()
        }
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val getPic = storageRef.child("profile_pictures/$username.jpg")
        try {
            val picture = File("$cacheDir/profilePictures/$username.jpg")
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
        getPic.downloadUrl.addOnSuccessListener {
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
                            getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                                val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
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
                                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            val picture = File("$cacheDir/profilePictures/$username.jpg")
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
                            getPic.metadata.addOnSuccessListener {
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
        }.addOnFailureListener {
            val myDir = File("$cacheDir/profilePictures")
            val picture = "$username.jpg"
            val config1 = "$username.txt"
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
                    R.drawable.ic_no_profile_picture
                )
            )
        }
    }

    fun checkData(v: View?) {
        val nameIL = findViewById<TextInputLayout>(R.id.changeName)
        val infoIL = findViewById<TextInputLayout>(R.id.changeInfo)
        val info = infoIL.editText
        val name = nameIL.editText
        assert(info != null)
        val infoText = info!!.text.toString().trim { it <= ' ' }
        assert(name != null)
        val nameText = name!!.text.toString().trim { it <= ' ' }
        val infoCount = infoText.length
        val nameCount = nameText.length
        var canAgo = true
        nameIL.error = null
        infoIL.error = null
        if (nameCount > 60) {
            canAgo = false
            nameIL.error = "Darf nicht mehr als 60 Zeichen enthalten!"
        } else if (nameCount == 0) {
            canAgo = false
            nameIL.error = "Darf nicht leer sein!"
        }
        if (infoCount > 150) {
            canAgo = false
            infoIL.error = "Darf nicht mehr als 150 Zeichen enthalten!"
        } /*else if(infoCount == 0){
            canAgo = false;
            infoIL.setError("Darf nicht leer sein!");
        }*/
        if (canAgo) {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val db = DBHelper(this, null)
        val nameIL = findViewById<TextInputLayout>(R.id.changeName)
        val infoIL = findViewById<TextInputLayout>(R.id.changeInfo)
        val info = infoIL.editText
        val name = nameIL.editText
        val database = FirebaseDatabase.getInstance().reference
        val username = db.getCurrentUsername()
        val newName = name!!.text.toString().trim { it <= ' ' }
        var newInfo = info!!.text.toString().trim { it <= ' ' }
        if (newInfo.isEmpty() || infoEmpty) {
            newInfo = "string_default_info"
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.wait4)
            .setCancelable(false)
        val shower = builder.show()
        val finalNewInfo = newInfo
        database.child("users/$username/informations/name").setValue(newName)
            .addOnSuccessListener {
                database.child(
                    "users/$username/informations/info"
                ).setValue(finalNewInfo).addOnSuccessListener {
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    val pb = storageRef.child("profile_pictures/$username.jpg")
                    val date = Date()
                    val picDate = SimpleDateFormat("dd.MM.yyyy_HH:mm:ss").format(date)
                    val metadata = StorageMetadata.Builder()
                        .setCustomMetadata("date", picDate)
                        .build()
                    if (pic != null) {
                        val uploadTask = pb.putBytes(pic!!)
                        uploadTask.addOnFailureListener {
                            Toast.makeText(
                                this@Profile,
                                "Fehler!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                            .addOnSuccessListener {
                                pb.updateMetadata(metadata)
                                    .addOnSuccessListener {
                                        shower.cancel()
                                        Toast.makeText(
                                            this@Profile,
                                            "Änderungen gespeichert.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                            }
                    } else {
                        shower.cancel()
                        Toast.makeText(this@Profile, "Änderungen gespeichert.", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }.addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this@Profile,
                        "Error: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@Profile,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun requestPicture() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Profilbild")
            .setPositiveButton("Ansehen") { _: DialogInterface?, _: Int -> showPb() }
            .setNegativeButton("Neu auswählen") { _: DialogInterface?, _: Int ->
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
            }
            .setNeutralButton("Entfernen") { _: DialogInterface?, _: Int -> removePb() }
            .show()
    }

    private fun showPb() {
        val intent = Intent(this, OpenProfilePicture::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    private fun removePb() {
        AlertDialog.Builder(this)
            .setMessage("Profilbild löschen?")
            .setPositiveButton("löschen") { _: DialogInterface?, _: Int ->
                val builder = AlertDialog.Builder(this)
                builder.setView(R.layout.wait)
                    .setCancelable(false)
                val alert = builder.show()
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val pb = storageRef.child("profile_pictures/$username.jpg")
                pb.delete().addOnSuccessListener {
                    val image = findViewById<CircleImageView>(R.id.profilePic)
                    image.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@Profile,
                            R.drawable.ic_no_profile_picture
                        )
                    )
                    alert.cancel()
                }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(
                            this@Profile, """
     Error:
     ${e.message}
     """.trimIndent(), Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("abbrechen", null)
            .show()
    }

    fun changePass() {
        val intent = Intent(this, ChangePass::class.java)
        startActivity(intent)
        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                val imageUri = data!!.data
                val cropLayout = findViewById<ConstraintLayout>(R.id.cropLayout)
                cropLayout.visibility = View.VISIBLE
                val cropImageButton = findViewById<Button>(R.id.cropImageButton)
                cropImageButton.visibility = View.VISIBLE
                val cancelButton = findViewById<Button>(R.id.cancelCroppingImageButton)
                cancelButton.visibility = View.VISIBLE
                cancelButton.setOnClickListener {
                    cropImageButton.visibility = View.GONE
                    cropLayout.visibility = View.GONE
                }
                cropImageButton.setOnClickListener {
                    cropImageButton.visibility = View.GONE
                    cropLayout.visibility = View.GONE

                    val baos = ByteArrayOutputStream()

                    pic = baos.toByteArray()

                    if (pic!!.size > 3000000) {
                        Toast.makeText(this, "Bild ist zu groß!", Toast.LENGTH_SHORT).show()
                    } else {
                        val image = findViewById<CircleImageView>(R.id.profilePic)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@Profile, e.message, Toast.LENGTH_LONG).show()
            } catch (e: Error) {
                Toast.makeText(this@Profile, e.message, Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("Talk to me", resultCode.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    fun setLightMode() {
        setTheme(R.style.profileLight)
    }

    fun setDarkMode() {
        setTheme(R.style.profileDark)
    }

    val isDarkMode: Boolean
        get() {
            var darkMode = false
            val db = DBHelper(this, null)
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

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.logoutProfile -> logout()
            R.id.deleteProfile -> {
                val intent = Intent(this, DelAccount::class.java)
                startActivity(intent)
            }

            R.id.changePass -> changePass()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        val db = DBHelper(this, null)

        AlertDialog.Builder(this@Profile)
            .setTitle("Abmelden")
            .setMessage("Bist du sicher, dass du dich abmelden möchtest?")
            .setCancelable(false)
            .setPositiveButton("Ja") { _: DialogInterface?, _: Int ->
                db.changeLoggedInUser(username!!, false)

                val intent = Intent(this@Profile, StartIcon::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                finishAffinity()
                startActivity(intent)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }
}