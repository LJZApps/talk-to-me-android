package com.lnzpk.chat_app.main

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.bottomSheets.ChatContextSheet
import com.lnzpk.chat_app.chat.OpenChat
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.colors.Colors.setBadgeColor
import com.lnzpk.chat_app.colors.Colors.setFabColor
import com.lnzpk.chat_app.customThings.chatPreference
import com.lnzpk.chat_app.friends.Friends
import com.lnzpk.chat_app.group.NewGroup
import com.lnzpk.chat_app.group.OpenGroupChat
import com.lnzpk.chat_app.group.PublicGroups
import com.lnzpk.chat_app.newDatabase.DBHelper
import com.lnzpk.chat_app.post.NewPost
import com.lnzpk.chat_app.post.OpenPost
import com.lnzpk.chat_app.profile.OpenProfile
import com.lnzpk.chat_app.service.AcceptNotifications
import com.lnzpk.chat_app.service.Notification
import com.lnzpk.chat_app.service.StatusService
import com.lnzpk.chat_app.settings.Settings
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {
    var database = Firebase.database.reference
    var refresher: SwipeRefreshLayout? = null
    var toolbar: Toolbar? = null
    var childEventListener: ChildEventListener? = null
    var chatListener: ChildEventListener? = null
    var chatList: LinearLayout? = null
    private var chatScroller: ScrollView? = null
    var username: String? = null
    var darkMode = false
    var onPosts: Boolean? = null
    //var preferences: SharedPreferences? = null
    var requestsBadge: TextView? = null
    var next = ""
    private var specifyNext = ""

    @SuppressLint("NonConstantResourceId")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val db = DBHelper(this, null)

        if (!db.getSettingBoolean("acceptedData", false)) {
            val intent = Intent(this, ConfirmDataProtection::class.java)
            startActivity(intent)
        }

        val statusService = Intent(this, StatusService::class.java)
        statusService.addFlags(Intent.FLAG_FROM_BACKGROUND).addFlags(Intent.FLAG_RECEIVER_NO_ABORT)
        startService(statusService)

        context = baseContext
        noChatText = findViewById(R.id.no_chat_text)
        noChatPic = findViewById(R.id.no_chat_pic)
        noGroupText = findViewById(R.id.no_group_text)
        noGroupPic = findViewById(R.id.no_group_pic)
        requestsBadge = findViewById(R.id.home_requestsBadge)
        chatList = findViewById(R.id.chatList)
        chatScroller = findViewById(R.id.chatScrollView)


        chatListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                noChatPic!!.visibility = View.GONE
                noChatText!!.visibility = View.GONE
                val chatUsername = snapshot.key
                database.child("users/$chatUsername").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val v: View = if (isDarkMode()) {
                            LayoutInflater.from(this@Home).inflate(R.layout.chat_preference_design_dark, null, false)
                        } else {
                            LayoutInflater.from(this@Home).inflate(R.layout.chat_preference_design_light, null, false)
                        }
                        val holder = PreferenceHolder()
                        holder.layout = v.findViewById(R.id.testChatDesign)
                        holder.icon = v.findViewById(android.R.id.icon)
                        holder.title = v.findViewById(android.R.id.title)
                        holder.summary = v.findViewById(android.R.id.summary)
                        holder.time = v.findViewById(R.id.timeTextViewChat)
                        holder.unreadMessages = v.findViewById(R.id.unreadChatNumber)
                        holder.verifiedImage = v.findViewById(R.id.verifiedImage)
                        v.tag = holder
                        holder.time!!.visibility = View.VISIBLE
                        holder.unreadMessages!!.visibility = View.GONE
                        if (Colors.isDarkMode(this@Home)) {
                            holder.layout!!.background = ContextCompat.getDrawable(this@Home, R.drawable.chat_background_dark)
                            holder.summary!!.setTextColor(getColor(R.color.secondary_text_dark))
                            holder.time!!.setTextColor(getColor(R.color.secondary_text_dark))
                        }
                        getProfilePicture(chatUsername, holder.icon!!)
                        if (snapshot.exists()) {
                            if (snapshot.child("blockedUser/$username").exists()) {
                                holder.title!!.text = chatUsername
                            } else {
                                val chatName = snapshot.child("informations/name").value.toString()
                                holder.title!!.text = chatName
                                if (snapshot.child("settings/staff").exists()) {
                                    val staff = java.lang.Boolean.parseBoolean(snapshot.child("settings/staff").value.toString())
                                    if (staff) {
                                        holder.verifiedImage!!.visibility = View.VISIBLE
                                    } else {
                                        holder.verifiedImage!!.visibility = View.GONE
                                    }
                                }
                            }
                        } else {
                            holder.title!!.setText(R.string.chats_deletedProfile)
                        }
                        try {
                            val date = Date()
                            val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
                            database.child("users/$username/chats/$chatUsername/messages").limitToLast(1).addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                    if (snapshot.child("from").exists() && snapshot.child("message").exists()) {
                                        val message = snapshot.child("message").value.toString()
                                        val from = snapshot.child("from").value.toString()
                                        val time = snapshot.child("time").value.toString()
                                        val date = snapshot.child("date").value.toString()
                                        val equals = messageDate.trim { it <= ' ' } == date.trim { it <= ' ' }
                                        if (snapshot.child("status").exists()) {
                                            val status = snapshot.child("status").value.toString()
                                            if (status == "SENT") {
                                                if (from != username) {
                                                    holder.unreadMessages!!.visibility = View.VISIBLE
                                                } else {
                                                    holder.unreadMessages!!.visibility = View.GONE
                                                }
                                            }
                                            if (equals) {
                                                holder.time!!.text = time
                                            } else {
                                                holder.time!!.text = date
                                            }
                                            val messageText: String = if (from == username) {
                                                "Du: " + message.replace("\n", "  ")
                                            } else {
                                                message.replace("\n", "  ")
                                            }
                                            holder.summary!!.text = messageText
                                        } else {
                                            if (equals) {
                                                holder.time!!.text = time
                                            } else {
                                                holder.time!!.text = date
                                            }
                                            val messageText: String = if (from == username) {
                                                "Du: " + message.replace("\n", "  ")
                                            } else {
                                                message.replace("\n", "  ")
                                            }
                                            holder.summary!!.text = messageText
                                        }
                                    }
                                }

                                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                    if (snapshot.child("from").exists() && snapshot.child("message").exists()) {
                                        val map = snapshot.value as Map<*, *>?
                                        val map1: MutableMap<Int, String> = HashMap()
                                        val message = map!!["message"].toString()
                                        val from = map["from"].toString()
                                        map1[0] = from
                                        val time = map["time"].toString()
                                        val date = map["date"].toString().trim { it <= ' ' }
                                        val equals = messageDate.trim { it <= ' ' } == date.trim { it <= ' ' }
                                        if (snapshot.child("status").exists()) {
                                            val status = snapshot.child("status").value.toString()
                                            if (status == "SENT") {
                                                if (from != username) {
                                                    holder.unreadMessages!!.visibility = View.VISIBLE
                                                    //holder.unreadMessages.setText("1");
                                                } else {
                                                    holder.unreadMessages!!.visibility = View.GONE
                                                }
                                            } else {
                                                holder.unreadMessages!!.visibility = View.GONE
                                            }
                                            if (equals) {
                                                holder.time!!.text = time
                                            } else {
                                                holder.time!!.text = date
                                            }
                                            val messageText: String = if (from == username) {
                                                "Du: " + message.replace("\n", "  ")
                                            } else {
                                                message.replace("\n", "  ")
                                            }
                                            holder.summary!!.text = messageText
                                        } else {
                                            if (equals) {
                                                holder.time!!.text = time
                                            } else {
                                                holder.time!!.text = date
                                            }
                                            val messageText: String = if (from == username) {
                                                "Du: " + message.replace("\n", "  ")
                                            } else {
                                                message.replace("\n", "  ")
                                            }
                                            holder.summary!!.text = messageText
                                        }
                                    }
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {
                                    if (snapshot.child("from").exists() && snapshot.child("message").exists()) {
                                        val map = snapshot.value as Map<*, *>?
                                        val map1: MutableMap<Int, String> = HashMap()
                                        val message = map!!["message"].toString()
                                        val from = map["from"].toString()
                                        map1[0] = from
                                        val time = map["time"].toString()
                                        val date = map["date"].toString().trim { it <= ' ' }
                                        val equals = messageDate.trim { it <= ' ' } == date.trim { it <= ' ' }
                                        if (snapshot.child("status").exists()) {
                                            val status = snapshot.child("status").value.toString()
                                            if (status == "SENT") {
                                                if (from != username) {
                                                    holder.unreadMessages!!.visibility = View.VISIBLE
                                                } else {
                                                    holder.unreadMessages!!.visibility = View.GONE
                                                }
                                            } else {
                                                holder.unreadMessages!!.visibility = View.GONE
                                            }
                                            if (equals) {
                                                holder.time!!.text = time
                                            } else {
                                                holder.time!!.text = date
                                            }

                                            val messageText: String = if (from == username) {
                                                "Du: " + message.replace("\n", "  ")
                                            } else {
                                                message.replace("\n", "  ")
                                            }
                                            holder.summary!!.text = messageText
                                        } else {
                                            holder.unreadMessages!!.visibility = View.GONE
                                            if (equals) {
                                                holder.time!!.text = time
                                            } else {
                                                holder.time!!.text = date
                                            }

                                            val messageText: String = if (from == username) {
                                                "Du: " + message.replace("\n", "  ")
                                            } else {
                                                message.replace("\n", "  ")
                                            }
                                            holder.summary!!.text = messageText
                                        }
                                    }
                                }

                                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                                override fun onCancelled(error: DatabaseError) {}
                            })
                            v.setOnClickListener {
                                val intent = Intent(this@Home, OpenChat::class.java)
                                intent.putExtra("username", chatUsername)
                                        .putExtra("nameMe", username)
                                startActivity(intent)
                            }
                            val params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(5, 5, 5, 5)
                            holder.layout!!.layoutParams = params

                            v.setOnLongClickListener {
                                if (chatUsername != null) {
                                    ChatContextSheet(chatUsername).apply {
                                        show(supportFragmentManager, tag)
                                    }
                                }
                                false
                            }

                            chatList!!.addView(v)
                            v.isHapticFeedbackEnabled = true
                        } catch (e: Exception) {
                            Toast.makeText(this@Home, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@Home, error.message, Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                chatList!!.removeAllViews()
                database.child("users/$username/chats").removeEventListener(chatListener!!)
                loadChat()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("next")) {
                next = extras.getString("next", "")
            }
            if (extras.containsKey("specifyNext")) {
                specifyNext = extras.getString("specifyNext", "")
            }
        }
        if (next != "") {
            if (next == "friends") {
                val friendsIntent = Intent(this, Friends::class.java)
                if (specifyNext != "") {
                    if (specifyNext == "request") {
                        friendsIntent.putExtra("specifyNext", "request")
                    }
                }
                startActivity(friendsIntent)
            } else if (next == "chat") {
                val chatIntent = Intent(this, OpenChat::class.java)
                chatIntent.putExtra("username", specifyNext)
                startActivity(chatIntent)
            }
        }
        if (specifyNext != "") {
        }
        val homeNavBar = findViewById<BottomNavigationView>(R.id.homeNavigation)
        Colors.setNavBarColor(this, homeNavBar)
        if (Colors.isDarkMode(this@Home)) {
            homeNavBar.setBackgroundResource(R.drawable.nav_bar_dark)
        } else {
            homeNavBar.setBackgroundResource(R.drawable.nav_bar_light)
        }

        if (db.getSettingString("notificationCheck", "notSet") == "notSet") {
            Toast.makeText(this, db.getSettingString("notificationCheck", "notSet"), Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName = packageName
                val pm = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    val intent2 = Intent(this, AcceptNotifications::class.java)
                    startActivity(intent2)
                } else {

                    val notificationService = Intent(this, Notification::class.java)
                    notificationService.addFlags(Intent.FLAG_FROM_BACKGROUND).addFlags(Intent.FLAG_RECEIVER_NO_ABORT)
                    startService(notificationService)

                    // Clear all notification
                    val nMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    nMgr.cancelAll()
                }
            }
        } else if (db.getSettingString("notificationCheck", "notSet") == "accepted") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName = packageName
                val pm = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    val intent2 = Intent(this, AcceptNotifications::class.java)
                    startActivity(intent2)
                } else {
                    val notificationService = Intent(this, Notification::class.java)
                    notificationService.addFlags(Intent.FLAG_FROM_BACKGROUND).addFlags(Intent.FLAG_RECEIVER_NO_ABORT)
                    startService(notificationService)

                    // Clear all notification
                    val nMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    nMgr.cancelAll()
                }

                // Clear all notification
                val nMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nMgr.cancelAll()
            }
        }
        onPosts = false
        if (isNetworkConnected) {
            startRefresher()
        }
        username = db.getCurrentUsername()
        toolbar = findViewById(R.id.homeToolbar)
        Colors.setToolbarColor(this, this, toolbar!!)
        checkNetwork()
        toolbar!!.inflateMenu(R.menu.home_menu)
        toolbar!!.setOnMenuItemClickListener { item2: MenuItem ->
            when (item2.itemId) {
                R.id.settings -> {
                    val settings = Intent(this@Home, Settings::class.java)
                    startActivity(settings)
                }
            }
            false
        }
        val navigationView = findViewById<BottomNavigationView>(R.id.homeNavigation)
        navigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.homeChats -> {
                    item.isChecked = true
                    onPosts = false
                    loadChats()
                }
                R.id.homeArticle -> {
                    item.isChecked = true
                    onPosts = true
                    loadPosts()
                }
                R.id.homeGroups -> {
                    item.isChecked = true
                    loadGroups()
                    onPosts = false
                }
            }
            false
        }
        if (isNetworkConnected) {
            when (navigationView.selectedItemId) {
                R.id.homeChats -> {
                    loadChats()
                    onPosts = false
                }
                R.id.homeArticle -> {
                    loadPosts()
                    onPosts = true
                }
                R.id.homeGroups -> {
                    loadGroups()
                    onPosts = false
                }
            }
            refreshPost()
            loadChat()
            loadGroupClass()
        }
        requestsForBadge
        setBadgeColor(this, requestsBadge!!)
    }

    inner class PreferenceHolder {
        var layout: ConstraintLayout? = null
        var icon: ImageView? = null
        var title: TextView? = null
        var summary: TextView? = null
        var time: TextView? = null
        var unreadMessages: TextView? = null
        var verifiedImage: ImageView? = null
    }

    private val requestsForBadge: Unit
        get() {
            database.child("users/$username/requests").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val requests = snapshot.childrenCount.toInt()
                        if (requests != 0) {
                            requestsBadge!!.visibility = View.VISIBLE
                            requestsBadge!!.text = requests.toString()
                        } else {
                            requestsBadge!!.visibility = View.GONE
                        }
                    } else {
                        requestsBadge!!.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
            database.child("users/$username/requests").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    database.child("users/$username/requests").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val requests = snapshot.childrenCount.toInt()
                                if (requests != 0) {
                                    requestsBadge!!.visibility = View.VISIBLE
                                    requestsBadge!!.text = requests.toString()
                                } else {
                                    requestsBadge!!.visibility = View.GONE
                                }
                            } else {
                                requestsBadge!!.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    database.child("users/$username/requests").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val requests = snapshot.childrenCount.toInt()
                                if (requests != 0) {
                                    requestsBadge!!.visibility = View.VISIBLE
                                    requestsBadge!!.text = requests.toString()
                                } else {
                                    requestsBadge!!.visibility = View.GONE
                                }
                            } else {
                                requestsBadge!!.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    database.child("users/$username/requests").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val requests = snapshot.childrenCount.toInt()
                                if (requests != 0) {
                                    requestsBadge!!.visibility = View.VISIBLE
                                    requestsBadge!!.text = requests.toString()
                                } else {
                                    requestsBadge!!.visibility = View.GONE
                                }
                            } else {
                                requestsBadge!!.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }

    fun getProfilePicture(username: String?, profilePicture: ImageView) {
        profilePicture.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_no_profile_picture))
        val user = PreferenceManager.getDefaultSharedPreferences(this)
        val myUsername = user.getString("username", "UNKNOWN")
        database.child("users/$username/blockedUser/$myUsername").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    profilePicture.setImageDrawable(ContextCompat.getDrawable(this@Home, R.drawable.ic_no_profile_picture))
                } else {
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    profilePicture.setOnClickListener {
                        val intent = Intent(this@Home, OpenProfile::class.java)
                        intent.putExtra("username", username).putExtra("comeFrom", "home")
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
                            val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                            val newBitmap = Bitmap.createScaledBitmap(bmp, 100, 100, false)
                            val d: Drawable = BitmapDrawable(resources, newBitmap)
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
                                            val newBitmap = Bitmap.createScaledBitmap(bmp, 100, 100, false)
                                            val d: Drawable = BitmapDrawable(resources, newBitmap)
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
                                                    configS.flush()
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
                                                    Toast.makeText(this@Home, error, Toast.LENGTH_SHORT).show()
                                                }
                                    } else {
                                        val picture = File("$cacheDir/profilePictures/$username.jpg")
                                        val is1: InputStream = FileInputStream(picture)
                                        val size1 = is1.available()
                                        val picBuffer = ByteArray(size1)
                                        is1.read(picBuffer)
                                        is1.close()
                                        val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                                        val newBitmap = Bitmap.createScaledBitmap(bmp, 100, 100, false)
                                        val d: Drawable = BitmapDrawable(resources, newBitmap)
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
                                                Toast.makeText(this@Home, error, Toast.LENGTH_SHORT).show()
                                            }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@Home, e.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.addOnFailureListener { profilePicture.setImageDrawable(ContextCompat.getDrawable(this@Home, R.drawable.ic_no_profile_picture)) }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadChat() {
        database.child("users/$username/chats").addChildEventListener(chatListener!!)
        database.child("users/$username/chats").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    noChatPic!!.visibility = View.GONE
                    noChatText!!.visibility = View.GONE
                } else {
                    noChatPic!!.visibility = View.VISIBLE
                    noChatText!!.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun startRefresher() {
        if (refresher == null) {
            refresher = findViewById(R.id.postRefresher)
        }
        refresher!!.setOnRefreshListener { refreshPost() }
    }

    private fun loadGroupClass() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.groups, Groups())
                .commit()
    }

    private fun loadGroups() {
        //refresher
        refresher!!.visibility = View.GONE
        onGroups = true

        //Buttons
        val joinPrivateGroups = findViewById<Button>(R.id.joinPrivateGroupButton)
        val joinPublicGroups = findViewById<Button>(R.id.joinPublicGroupButton)
        Colors.setButtonColor(this, joinPrivateGroups)
        Colors.setButtonColor(this, joinPublicGroups)
        joinPublicGroups.setOnClickListener { joinGroup() }

        //FAB's
        val fab1 = findViewById<FloatingActionButton>(R.id.contactsFAB)
        setFabColor(this, fab1)
        fab1.visibility = View.GONE
        val fab2 = findViewById<FloatingActionButton>(R.id.newPostFAB)
        setFabColor(this, fab2)
        fab2.visibility = View.GONE
        val fab3 = findViewById<FloatingActionButton>(R.id.newGroupsFAB)
        setFabColor(this, fab3)
        fab3.visibility = View.VISIBLE
        fab3.setOnClickListener {
            val intent = Intent(this, NewGroup::class.java)
            startActivity(intent)
        }

        //layouts
        val layout = findViewById<LinearLayout>(R.id.feeds)
        layout.visibility = View.GONE
        chatList!!.visibility = View.GONE
        chatScroller!!.visibility = View.GONE
        val groups = findViewById<FrameLayout>(R.id.groups)
        groups.visibility = View.VISIBLE
        val groupConst = findViewById<ConstraintLayout>(R.id.groupConst)
        groupConst.visibility = View.VISIBLE
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.groupRefresher)
        refreshLayout.visibility = View.VISIBLE

        //noPosts
        val noArticleText = findViewById<TextView>(R.id.no_article_text)
        val noArticlePic = findViewById<ImageView>(R.id.no_article_pic)
        noArticleText.visibility = View.GONE
        noArticlePic.visibility = View.GONE

        //noChats
        val noChatText = findViewById<TextView>(R.id.no_chat_text)
        val noChatPic = findViewById<ImageView>(R.id.no_chat_pic)
        noChatPic.visibility = View.GONE
        noChatText.visibility = View.GONE
        val noGroups = booleanArrayOf(true)
        val joinedGroups = intArrayOf(0)
        val groupListener: ChildEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (onGroups) {
                    if (joinedGroups[0] < 1) {
                        if (snapshot.child("members/$username").exists()) {
                            noGroups[0] = false
                            joinedGroups[0]++
                            noGroupText!!.visibility = View.GONE
                            noGroupPic!!.visibility = View.GONE
                        } else {
                            if (noGroups[0]) {
                                noGroupText!!.visibility = View.VISIBLE
                                noGroupPic!!.visibility = View.VISIBLE
                            } else {
                                noGroupText!!.visibility = View.GONE
                                noGroupPic!!.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    noGroupText!!.visibility = View.GONE
                    noGroupPic!!.visibility = View.GONE
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("groups").addChildEventListener(groupListener)
    }

    private fun loadChats() {
        //refresher
        refresher!!.visibility = View.GONE
        onPosts = false

        //FAB's
        val fab1 = findViewById<FloatingActionButton>(R.id.contactsFAB)
        setFabColor(this, fab1)
        fab1.visibility = View.VISIBLE
        fab1.setOnClickListener {
            val intent = Intent(this, Friends::class.java)
            startActivity(intent)
        }
        val fab2 = findViewById<FloatingActionButton>(R.id.newPostFAB)
        setFabColor(this, fab2)
        fab2.visibility = View.GONE
        val fab3 = findViewById<FloatingActionButton>(R.id.newGroupsFAB)
        setFabColor(this, fab3)
        fab3.visibility = View.GONE

        //layouts
        val layout = findViewById<LinearLayout>(R.id.feeds)
        layout.visibility = View.GONE
        chatScroller!!.visibility = View.VISIBLE
        chatList!!.visibility = View.VISIBLE
        val groups = findViewById<FrameLayout>(R.id.groups)
        groups.visibility = View.GONE
        val groupConst = findViewById<ConstraintLayout>(R.id.groupConst)
        groupConst.visibility = View.GONE
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.groupRefresher)
        refreshLayout.visibility = View.GONE

        //noPosts
        val noArticleText = findViewById<TextView>(R.id.no_article_text)
        val noArticlePic = findViewById<ImageView>(R.id.no_article_pic)
        val noGroupText = findViewById<TextView>(R.id.no_group_text)
        val noGroupPic = findViewById<ImageView>(R.id.no_group_pic)
        noGroupText.visibility = View.GONE
        noGroupPic.visibility = View.GONE
        noArticleText.visibility = View.GONE
        noArticlePic.visibility = View.GONE
        database.child("users/$username/chats").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val noChatText = findViewById<TextView>(R.id.no_chat_text)
                    val noChatPic = findViewById<ImageView>(R.id.no_chat_pic)
                    noChatPic.visibility = View.VISIBLE
                    noChatText.visibility = View.VISIBLE
                } else {
                    val noChatText = findViewById<TextView>(R.id.no_chat_text)
                    val noChatPic = findViewById<ImageView>(R.id.no_chat_pic)
                    noChatPic.visibility = View.GONE
                    noChatText.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadPosts() {
        //refresher
        refresher!!.visibility = View.VISIBLE
        onGroups = false

        //FAB's
        val fab1 = findViewById<FloatingActionButton>(R.id.contactsFAB)
        setFabColor(this, fab1)
        fab1.visibility = View.GONE
        val fab2 = findViewById<FloatingActionButton>(R.id.newPostFAB)
        setFabColor(this, fab2)
        fab2.visibility = View.VISIBLE
        val fab3 = findViewById<FloatingActionButton>(R.id.newGroupsFAB)
        setFabColor(this, fab3)
        fab3.visibility = View.GONE

        //layouts
        val layout = findViewById<LinearLayout>(R.id.feeds)
        layout.visibility = View.VISIBLE
        chatList!!.visibility = View.GONE
        chatScroller!!.visibility = View.GONE
        val groups = findViewById<FrameLayout>(R.id.groups)
        groups.visibility = View.GONE
        val groupConst = findViewById<ConstraintLayout>(R.id.groupConst)
        groupConst.visibility = View.GONE
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.groupRefresher)
        refreshLayout.visibility = View.GONE

        //noChats
        val noChatText = findViewById<TextView>(R.id.no_chat_text)
        val noChatPic = findViewById<ImageView>(R.id.no_chat_pic)
        val noGroupText = findViewById<TextView>(R.id.no_group_text)
        val noGroupPic = findViewById<ImageView>(R.id.no_group_pic)
        noGroupText.visibility = View.GONE
        noGroupPic.visibility = View.GONE
        noChatPic.visibility = View.GONE
        noChatText.visibility = View.GONE
        database.child("posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val noArticleText = findViewById<TextView>(R.id.no_article_text)
                    val noArticlePic = findViewById<ImageView>(R.id.no_article_pic)
                    if (onPosts!!) {
                        noArticlePic.visibility = View.VISIBLE
                        noArticleText.visibility = View.VISIBLE
                    } else {
                        noArticlePic.visibility = View.GONE
                        noArticleText.visibility = View.GONE
                    }
                } else {
                    val noArticleText = findViewById<TextView>(R.id.no_article_text)
                    val noArticlePic = findViewById<ImageView>(R.id.no_article_pic)
                    noArticlePic.visibility = View.GONE
                    noArticleText.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun refreshPost() {
        val feeds = findViewById<LinearLayout>(R.id.feeds)
        feeds.removeAllViews()
        if (childEventListener != null) {
            database.child("posts").removeEventListener(childEventListener!!)
        } else {
            refresher = findViewById(R.id.postRefresher)
            refresher!!.isRefreshing = false
        }
        posts
    }

    val posts: Unit get() {
            val database1 = FirebaseDatabase.getInstance().reference
            val noArticleText = findViewById<TextView>(R.id.no_article_text)
            val noArticlePic = findViewById<ImageView>(R.id.no_article_pic)
            val feeds = findViewById<LinearLayout>(R.id.feeds)
            database.child("posts").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        childEventListener = object : ChildEventListener {
                            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                refresher!!.isRefreshing = false
                                if (snapshot.exists()) {
                                    noArticlePic.visibility = View.GONE
                                    noArticleText.visibility = View.GONE
                                    val message = snapshot.child("text").value.toString()
                                    val title = snapshot.child("title").value.toString()
                                    val from = snapshot.child("from").value.toString()
                                    val date = snapshot.child("date").value.toString()
                                    val time = snapshot.child("time").value.toString()
                                    val publicPost = snapshot.child("publicPost").value.toString()
                                    val key = snapshot.key
                                    val announcement = snapshot.child("isAnnouncement").value.toString()
                                    val isAnnouncement = java.lang.Boolean.parseBoolean(announcement)
                                    val v: View = if (isDarkMode()) {
                                        LayoutInflater.from(this@Home).inflate(R.layout.feed_dark, null, false)
                                    } else {
                                        LayoutInflater.from(this@Home).inflate(R.layout.feed_light, null, false)
                                    }
                                    val cardHolder: CardHolder = CardHolder()

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
                                            cardHolder.summary!!.text = from + " • " + date + " • " + time + getString(R.string.home_postsNewsEdited)
                                        } else {
                                            cardHolder.summary!!.text = from + " • " + date + " • " + time + getString(R.string.home_postEdited)
                                        }
                                    } else {
                                        if (isAnnouncement) {
                                            cardHolder.title!!.setTextColor(getColor(R.color.greenText))
                                            cardHolder.summary!!.text = from + " • " + date + " • " + time + getString(R.string.home_postNews)
                                        } else {
                                            cardHolder.summary!!.text = "$from • $date • $time"
                                        }
                                    }

                                    //Card
                                    val feedParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                    feedParams.setMargins(10, 10, 10, 10)
                                    feedParams.gravity = Gravity.CENTER
                                    cardHolder.card!!.layoutParams = feedParams
                                    if (isDarkMode()) {
                                        cardHolder.card!!.setCardBackgroundColor(getColor(R.color.cardDark))
                                    }
                                    cardHolder.card!!.isClickable = true
                                    cardHolder.card!!.setOnClickListener {
                                        val intent = Intent(this@Home, OpenPost::class.java)
                                        intent.putExtra("title", title)
                                            .putExtra("message", message)
                                            .putExtra("from", from)
                                            .putExtra("date", date)
                                            .putExtra("time", time)
                                            .putExtra("key", key)
                                        startActivity(intent)
                                    }
                                    if (username == from) {
                                        feeds.addView(v)
                                    } else {
                                        database.child("users/$username").addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (!snapshot.child("blockedPost/$from").exists() || !snapshot.child("blockedUser/$from").exists()) {
                                                    if (publicPost == "true") {
                                                        feeds.addView(v)
                                                    } else {
                                                        database.child("users/$from/friends/$username").addListenerForSingleValueEvent(object : ValueEventListener {
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
                                    if (onPosts!!) {
                                        noArticlePic.visibility = View.VISIBLE
                                        noArticleText.visibility = View.VISIBLE
                                    } else {
                                        noArticlePic.visibility = View.GONE
                                        noArticleText.visibility = View.GONE
                                    }
                                }
                                refresher!!.isRefreshing = false
                            }

                            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                            override fun onChildRemoved(snapshot: DataSnapshot) {}
                            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                            override fun onCancelled(error: DatabaseError) {}
                        }
                        database1.child("posts").addChildEventListener(childEventListener as ChildEventListener)
                    } else {
                        childEventListener = null
                        if (onPosts!!) {
                            noArticlePic.visibility = View.VISIBLE
                            noArticleText.visibility = View.VISIBLE
                        } else {
                            noArticlePic.visibility = View.GONE
                            noArticleText.visibility = View.GONE
                        }
                        refresher!!.isRefreshing = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

    private fun joinGroup() {
        val intent = Intent(this, PublicGroups::class.java)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        if (childEventListener != null) {
            database.removeEventListener(childEventListener!!)
        }
    }

    fun newPost(view: View?) {
        val intent = Intent(this, NewPost::class.java)
        startActivity(intent)
    }

    private fun userExist() {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val reference = FirebaseDatabase.getInstance().getReference("users")
        reference.child(username!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val builder = AlertDialog.Builder(this@Home)
                    builder.setTitle(R.string.startIcon_profileNotFound)
                            .setMessage(R.string.startIcon_profileNotFoundHelp)
                            .setPositiveButton(R.string.startIcon_loginRegister) { _: DialogInterface?, _: Int ->
                                val editor = settings.edit()
                                editor.clear().apply()
                                val intent = Intent(this@Home, StartIcon::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .setCancelable(false)
                            .show()
                } else {
                    getData(username!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setLightMode() {
        darkMode = false
        setTheme(R.style.lightTheme)
    }

    fun setDarkMode() {
        darkMode = true
        setTheme(R.style.homeDark)
    }

    fun isDarkMode(): Boolean {
        var darkMode = false
        when (PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")) {
            "system" -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> darkMode = true
                    Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> darkMode = false
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

    private fun checkNetwork() {
        if (isNetworkConnected) {
            refresher!!.isRefreshing = true
            userExist()
        } else {
            val toolbar = findViewById<Toolbar>(R.id.homeToolbar)
            toolbar.setTitle(R.string.home_noInternet)
            refresher!!.isRefreshing = false
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.home_noInternet)
                    .setMessage(getString(R.string.home_noInternetHelp))
                    .setCancelable(false)
                    .setPositiveButton(R.string.retry) { _: DialogInterface?, _: Int -> checkNetwork() }
                    .show()
        }
    }

    private val isNetworkConnected: Boolean
        private get() {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }

    fun getData(username: String) {
        val toolbar = findViewById<Toolbar>(R.id.homeToolbar)
        val reference = FirebaseDatabase.getInstance().getReference("users")
        reference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setData()
            }

            override fun onCancelled(error: DatabaseError) {
                toolbar.title = "ERROR: ${error.message}"
            }
        })
    }

    fun setData() {
        val toolbar = findViewById<Toolbar>(R.id.homeToolbar)
        toolbar.setTitle(R.string.app_name)
        refresher!!.isRefreshing = false
    }

    fun contacts() {
        val intent = Intent(this, Friends::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (darkMode != isDarkMode()) {
            recreate()
        }
        checkNetwork()
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val username = settings.getString("username", "USERNAME")
        val toolbar = findViewById<Toolbar>(R.id.homeToolbar)
        val db = DBHelper(this, null)
        if (isNetworkConnected) {
            loadGroupClass()
            // preferences!!.getString("notificationCheck", "notSet") == "accepted"
            if (db.getSettingString("notificationCheck", "notSet") == "accepted") {
                val notificationService = Intent(this, Notification::class.java)
                notificationService.addFlags(Intent.FLAG_FROM_BACKGROUND).addFlags(Intent.FLAG_RECEIVER_NO_ABORT)
                startService(notificationService)
            }
            val reference = FirebaseDatabase.getInstance().getReference("users")
            reference.child(username!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    setData()
                }

                override fun onCancelled(error: DatabaseError) {
                    toolbar.subtitle = "ERROR: " + error.message
                    refresher!!.isRefreshing = false
                    val dialog = AlertDialog.Builder(this@Home)
                    dialog.setTitle("ERROR: " + error.message)
                            .setMessage("Error-Code: " + error.code + getString(R.string.home_errorConnection))
                            .setCancelable(false)
                            .show()
                }
            })
        }
    }

    private inner class CardHolder {
        var title: TextView? = null
        var message: TextView? = null
        var summary: TextView? = null
        var card: CardView? = null
    }

    class Groups : PreferenceFragmentCompat() {
        var username: String? = null
        var database: DatabaseReference? = null
        private var listener: ChildEventListener? = null
        var refresher: SwipeRefreshLayout? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.groups, rootKey)

            val db = DBHelper(preferenceScreen.context, null)

            username = db.getCurrentUsername()
            database = FirebaseDatabase.getInstance().reference
            refresher = requireActivity().findViewById(R.id.groupRefresher)
            val noGroups = booleanArrayOf(true)
            val joinedGroups = intArrayOf(0)
            listener = object : ChildEventListener {
                override fun onChildAdded(snapshot1: DataSnapshot, previousChildName: String?) {
                    val groupKey = snapshot1.key
                    if (refresher != null) {
                        if (refresher!!.isRefreshing) {
                            refresher!!.isRefreshing = false
                        }
                    }
                    if (onGroups) {
                        if (joinedGroups[0] < 1) {
                            if (snapshot1.child("members/$username").exists()) {
                                noGroups[0] = false
                                joinedGroups[0]++
                                noGroupText!!.visibility = View.GONE
                                noGroupPic!!.visibility = View.GONE
                            } else {
                                if (noGroups[0]) {
                                    noGroupText!!.visibility = View.VISIBLE
                                    noGroupPic!!.visibility = View.VISIBLE
                                } else {
                                    noGroupText!!.visibility = View.GONE
                                    noGroupPic!!.visibility = View.GONE
                                }
                            }
                        }
                    } else {
                        noGroupText!!.visibility = View.GONE
                        noGroupPic!!.visibility = View.GONE
                    }
                    if (snapshot1.child("members/$username").exists()) {
                        val preferences = preferenceScreen
                        val group = chatPreference(preferenceScreen.context)
                        group.layoutResource = R.layout.chat_preference_design_dark
                        group.title = snapshot1.child("name").value.toString()
                        getGroupLogo(groupKey, group)
                        group.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                            val intent = Intent(requireContext(), OpenGroupChat::class.java)
                            intent.putExtra("groupKey", groupKey)
                                    .putExtra("publicGroup", snapshot1.child("publicGroup").value.toString())
                                    .putExtra("comeFrom", "home")
                            startActivity(intent)
                            false
                        }
                        val date = Date()
                        val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
                        database!!.child("groups/$groupKey/messages").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    database!!.child("groups/$groupKey/messages").limitToLast(1).addChildEventListener(object : ChildEventListener {
                                        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                            val from = snapshot.child("from").value.toString()
                                            val time = snapshot.child("time").value.toString()
                                            val message = snapshot.child("message").value.toString()
                                            val date = snapshot.child("date").value.toString()
                                            if (messageDate.trim { it <= ' ' } == date.trim { it <= ' ' }) {
                                                group.setTime(time)
                                            } else {
                                                group.setTime(date)
                                            }
                                            if (from == username) {
                                                val messageText = "Du: " + message.replace("\n", "  ")
                                                val length = messageText.length
                                                if (length < 40) {
                                                    group.summary = messageText
                                                } else {
                                                    group.summary = messageText.substring(0, 40) + "..."
                                                }
                                            } else {
                                                if (from == "system") {
                                                    val messageText = message.replace("\n", "  ")
                                                    val length = messageText.length
                                                    if (length >= 40) {
                                                        val messageText1 = messageText.substring(0, 40) + "..."
                                                        group.summary = messageText1
                                                    } else {
                                                        group.summary = messageText
                                                    }
                                                } else {
                                                    val messageText = from + ": " + message.replace("\n", "  ")
                                                    val length = messageText.length
                                                    if (length >= 40) {
                                                        val messageText1 = messageText.substring(0, 40) + "..."
                                                        group.summary = messageText1
                                                    } else {
                                                        group.summary = messageText
                                                    }
                                                }
                                            }
                                        }

                                        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                                        override fun onChildRemoved(snapshot: DataSnapshot) {}
                                        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                        preferences.addPreference(group)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            groups
            refresher()
        }

        val groups: Unit
            get() {
                database!!.child("groups").addChildEventListener(listener!!)
            }

        fun getGroupLogo(groupKey: String?, preference: chatPreference) {
            preference.setIcon(R.drawable.ic_launcher_playstore)
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val getPic = storageRef.child("groupLogos/$groupKey.jpg")
            try {
                val picture = File(Companion.context!!.cacheDir.toString() + "/groupLogos/" + groupKey + ".jpg")
                if (picture.exists()) {
                    val is1: InputStream = FileInputStream(picture)
                    val size1 = is1.available()
                    val picBuffer = ByteArray(size1)
                    is1.read(picBuffer)
                    is1.close()
                    val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                    val d: Drawable = BitmapDrawable(Companion.context!!.resources, bmp)
                    preference.icon = d
                }
            } catch (e: Exception) {
                Log.e("TALK TO ME-ERROR", e.message!!)
            }
            getPic.downloadUrl.addOnSuccessListener {
                getPic.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
                    val date = storageMetadata.getCustomMetadata("date")
                    try {
                        val config = File(Companion.context!!.cacheDir.toString() + "/groupLogos/" + groupKey + ".txt")
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
                                    val d: Drawable = BitmapDrawable(Companion.context!!.resources, bmp)
                                    preference.icon = d
                                    val myDir = File(Companion.context!!.cacheDir.toString() + "/groupLogos")
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
                                    getPic.metadata.addOnSuccessListener {
                                        try {
                                            val configS = FileOutputStream(configFile.absolutePath, true)
                                            configS.flush()
                                            configS.write(date!!.toByteArray())
                                            configS.close()
                                            val pictureS = FileOutputStream(pictureFile.absolutePath, true)
                                            pictureS.write(bytes1)
                                            pictureS.close()
                                        } catch (e: Exception) {
                                            Log.e("TALK TO ME-ERROR", e.message!!)
                                        }
                                    }
                                }
                                        .addOnFailureListener { exception: Exception ->
                                            val error = exception.localizedMessage
                                            Toast.makeText(Companion.context, error, Toast.LENGTH_SHORT).show()
                                        }
                            } else {
                                val picture = File(Companion.context!!.cacheDir.toString() + "/groupLogos/" + groupKey + ".jpg")
                                val is1: InputStream = FileInputStream(picture)
                                val size1 = is1.available()
                                val picBuffer = ByteArray(size1)
                                is1.read(picBuffer)
                                is1.close()
                                val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                                val d: Drawable = BitmapDrawable(Companion.context!!.resources, bmp)
                                preference.icon = d
                            }
                        } else {
                            getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                                val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                                val d: Drawable = BitmapDrawable(Companion.context!!.resources, bmp)
                                preference.icon = d
                                val myDir = File(Companion.context!!.cacheDir.toString() + "/groupLogos")
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
                                getPic.metadata.addOnSuccessListener {
                                    try {
                                        val configS = FileOutputStream(configFile.absolutePath, true)
                                        configS.write(date!!.toByteArray())
                                        configS.close()
                                        val pictureS = FileOutputStream(pictureFile.absolutePath, true)
                                        pictureS.write(bytes1)
                                        pictureS.close()
                                    } catch (e: Exception) {
                                        Log.e("TALK TO ME-ERROR", e.message!!)
                                    }
                                }
                            }
                                    .addOnFailureListener { exception: Exception ->
                                        val error = exception.localizedMessage
                                        Toast.makeText(Companion.context, error, Toast.LENGTH_SHORT).show()
                                    }
                        }
                    } catch (e: Exception) {
                        Log.e("TALK TO ME-ERROR", e.message!!)
                    }
                }
            }.addOnFailureListener {
                val myDir = File(Companion.context!!.cacheDir.toString() + "/groupLogos")
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

        private fun refresher() {
            if (refresher != null) {
                refresher!!.setOnRefreshListener {
                    if (listener != null) {
                        val preferences = preferenceScreen
                        preferences.removeAll()
                        database!!.child("groups").removeEventListener(listener!!)
                        database!!.child("groups").addChildEventListener(listener!!)
                        database!!.child("groups").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    refresher!!.isRefreshing = false
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        @SuppressLint("StaticFieldLeak")
        var noChatText: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var noChatPic: ImageView? = null

        @SuppressLint("StaticFieldLeak")
        var noGroupText: TextView? = null

        @SuppressLint("StaticFieldLeak")
        var noGroupPic: ImageView? = null
        var onGroups = false
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