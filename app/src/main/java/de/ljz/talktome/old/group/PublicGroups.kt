package de.ljz.talktome.old.group

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors
import de.ljz.talktome.old.customThings.chatPreference
import de.ljz.talktome.old.newDatabase.DBHelper
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class PublicGroups : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.public_groups)
        val db = DBHelper(this, null)
        val toolbar = findViewById<Toolbar>(R.id.publicGroupToolbar)
        Colors.setToolbarColor(this, this, toolbar)
        setSupportActionBar(toolbar)
        username = db.getCurrentUsername()
        database = FirebaseDatabase.getInstance().reference
        context = baseContext
        loadGroups()
    }

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

    fun getGroupLogo(groupKey: String?, profilePicture: ImageView?) {
        profilePicture!!.setImageDrawable(
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

    fun loadGroups() {
        val groupList = findViewById<LinearLayout>(R.id.publicGroupsList)
        database!!.child("groups").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child("publicGroup").value.toString() == "true") {
                    val v: View
                    v = if (isDarkMode) {
                        LayoutInflater.from(this@PublicGroups)
                            .inflate(R.layout.chat_preference_design_dark, null, false)
                    } else {
                        LayoutInflater.from(this@PublicGroups)
                            .inflate(R.layout.chat_preference_design_light, null, false)
                    }
                    val groupName = snapshot.child("name").value.toString()
                    val groupKey = snapshot.key
                    val groupInfo = snapshot.child("info").value.toString()
                    val groupInfoText = groupInfo.replace("\n", "  ")
                    val holder: PreferenceHolder = PreferenceHolder()
                    holder.layout = v.findViewById(R.id.testChatDesign)
                    holder.icon = v.findViewById(android.R.id.icon)
                    holder.title = v.findViewById(android.R.id.title)
                    holder.summary = v.findViewById(android.R.id.summary)
                    holder.time = v.findViewById(R.id.timeTextViewChat)
                    holder.unreadMessages = v.findViewById(R.id.unreadChatNumber)
                    v.tag = holder
                    if (isDarkMode) {
                        holder.layout!!.setBackground(
                            ContextCompat.getDrawable(
                                this@PublicGroups,
                                R.drawable.chat_background_dark
                            )
                        )
                        holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_dark))
                    } else {
                        holder.layout!!.setBackground(
                            ContextCompat.getDrawable(
                                this@PublicGroups,
                                R.drawable.chat_background_light
                            )
                        )
                        holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_light))
                    }
                    getGroupLogo(groupKey, holder.icon)
                    holder.title!!.setText(groupName)
                    holder.summary!!.setText(groupInfoText)
                    holder.time!!.setVisibility(View.GONE)
                    holder.unreadMessages!!.setVisibility(View.INVISIBLE)
                    v.setOnClickListener { v2: View? ->
                        val intent = Intent(this@PublicGroups, OpenGroupInfo::class.java)
                        intent.putExtra("groupKey", groupKey)
                            .putExtra("comeFrom", "groupList")
                        startActivity(intent)
                        //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
                    }
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(5, 5, 5, 5)
                    holder.layout!!.setLayoutParams(params)

                    // Add view
                    groupList.addView(v)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private inner class PreferenceHolder {
        var layout: ConstraintLayout? = null
        var icon: ImageView? = null
        var title: TextView? = null
        var summary: TextView? = null
        var time: TextView? = null
        var unreadMessages: TextView? = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setLightMode() {
        setTheme(R.style.contactsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.darkTheme)
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

    class PublicGroupsLoader : PreferenceFragmentCompat() {
        var context2: Context? = null
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.public_groups, rootKey)
            loadGroups()
        }

        fun getGroupLogo(groupKey: String?, preference: chatPreference) {
            preference.setIcon(R.drawable.ic_launcher_playstore)
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val getPic = storageRef.child("groupLogos/$groupKey.jpg")
            try {
                val picture =
                    File(context2!!.cacheDir.toString() + "/groupLogos/" + groupKey + ".jpg")
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
            getPic.downloadUrl.addOnSuccessListener { uri: Uri? ->
                getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                    val date = storageMetadata.getCustomMetadata("date")
                    try {
                        val config =
                            File(context2!!.cacheDir.toString() + "/groupLogos/" + groupKey + ".txt")
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
                                    preference.icon = d
                                    val myDir = File(context2!!.cacheDir.toString() + "/groupLogos")
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
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                val picture =
                                    File(context2!!.cacheDir.toString() + "/groupLogos/" + groupKey + ".jpg")
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
                                val d: Drawable = BitmapDrawable(resources, bmp)
                                preference.icon = d
                                val myDir = File(context2!!.cacheDir.toString() + "/groupLogos")
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
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { e: Exception? ->
                val myDir = File(context2!!.cacheDir.toString() + "/groupLogos")
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
                preference.setIcon(R.drawable.ic_launcher_playstore)
            }
        }

        fun loadGroups() {
            val screen = preferenceScreen
            database!!.child("groups").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.child("publicGroup").value.toString() == "true") {
                        val preference = chatPreference(screen.context)
                        val groupName = snapshot.child("name").value.toString()
                        val groupKey = snapshot.key
                        getGroupLogo(groupKey, preference)
                        val groupInfo = snapshot.child("info").value.toString()
                        if (isDarkMode) {
                            preference.layoutResource = R.layout.chat_preference_design_dark
                        } else {
                            preference.layoutResource = R.layout.chat_preference_design_light
                        }
                        preference.title = groupName
                        val groupInfoText = groupInfo.replace("\n", "  ")
                        val length = groupInfoText.length
                        if (length >= 40) {
                            val messageText1 = groupInfoText.substring(0, 40) + "..."
                            preference.summary = messageText1
                        } else {
                            preference.summary = groupInfoText
                        }
                        preference.onPreferenceClickListener =
                            Preference.OnPreferenceClickListener {
                                val intent = Intent(activity, OpenGroupInfo::class.java)
                                intent.putExtra("groupKey", groupKey)
                                    .putExtra("comeFrom", "groupList")
                                startActivity(intent)
                                false
                            }
                        screen.addPreference(preference)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        val isDarkMode: Boolean
            get() {
                var darkMode = false
                val theme = PreferenceManager.getDefaultSharedPreferences(
                    context2!!
                ).getString("app_theme", "system")
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
    }

    companion object {
        var username: String? = null
        var database: DatabaseReference? = null
        var context: Context? = null
    }
}