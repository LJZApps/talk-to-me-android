package com.lnzpk.chat_app.old.friends

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import com.lnzpk.chat_app.old.profile.OpenProfile
import com.lnzpk.chat_app.old.profile.OpenProfilePicture
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class Friends : AppCompatActivity() {
    private var friendList: LinearLayout? = null
    private var publicUserList: LinearLayout? = null
    private var requestList: LinearLayout? = null
    private var noFriendsLayout: ConstraintLayout? = null
    private var noRequestsLayout: ConstraintLayout? = null
    private var friendsListener: ChildEventListener? = null
    private var publicUserListener: ChildEventListener? = null
    private var requestsListener: ChildEventListener? = null
    private var friendScroller: ScrollView? = null
    private var publicUserScroller: ScrollView? = null
    private var requestScroller: ScrollView? = null
    private var specifyNext = ""
    private var database = FirebaseDatabase.getInstance().reference
    private var username: String? = null
    private var whereAmI = "friends"

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.friends)

        val db = DBHelper(this, null)

        window.navigationBarColor = Color.BLACK
        startLoading()
        coordinatorLayout = findViewById(R.id.snackBarCoLayout)
        val navigation = findViewById<NavigationBarView>(R.id.friendsNavigation)
        setNavBarColor(navigation)
        if (isDarkMode) {
            navigation.setBackgroundResource(R.drawable.nav_bar_dark)
        }
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("specifyNext")) {
                specifyNext = extras.getString("specifyNext", "")
                if (specifyNext != "") {
                    if (specifyNext == "request") {
                        val item = findViewById<BottomNavigationItemView>(R.id.friendsRequest)
                        item.setChecked(true)
                        loadRequests()
                    }
                }
            }
        }
        context = baseContext
        username = db.getCurrentUsername()
        if (specifyNext != "") {
            when (specifyNext) {
                "request" -> {
                    val item = findViewById<BottomNavigationItemView>(R.id.friendsRequest)
                    val friendsItem = findViewById<BottomNavigationItemView>(R.id.friendsFriends)
                    friendsItem.setChecked(false)
                    item.setChecked(true)
                    loadRequests()
                }
                "public" -> {
                    val item = findViewById<BottomNavigationItemView>(R.id.friendsPublic)
                    val friendsItem = findViewById<BottomNavigationItemView>(R.id.friendsFriends)
                    friendsItem.setChecked(false)
                    item.setChecked(true)
                    loadPublicUsers()
                }
                "friends" -> {
                    loadFriends()
                }
            }
        } else {
            when (navigation.selectedItemId) {
                R.id.friendsFriends -> loadFriends()
                R.id.friendsPublic -> loadPublicUsers()
                R.id.friendsRequest -> loadRequests()
            }
        }
        navigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.friendsFriends -> {
                    item.isChecked = true
                    loadFriends()
                }
                R.id.friendsPublic -> {
                    item.isChecked = true
                    loadPublicUsers()
                }
                R.id.friendsRequest -> {
                    item.isChecked = true
                    loadRequestsWithMenu(item)
                }
            }
            false
        }
        val bottomNavigationMenuView = navigation.getChildAt(0) as BottomNavigationMenuView
        val v = bottomNavigationMenuView.getChildAt(2)
        val itemView = v as BottomNavigationItemView
        val badge = LayoutInflater.from(this).inflate(R.layout.badge_view, itemView, true)
        val requestsBadge = badge.findViewById<TextView>(R.id.badgeNumber)
        setBadgeColor(requestsBadge)
        getRequestsForBadge(requestsBadge)
        val toolbar = findViewById<Toolbar>(R.id.contactsToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
    }

    private fun getRequestsForBadge(requestsBadge: TextView) {
        database.child("users/$username/requests")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val requests = snapshot.childrenCount.toInt()
                        if (requests != 0) {
                            requestsBadge.visibility = View.VISIBLE
                            requestsBadge.text = requests.toString()
                        } else {
                            requestsBadge.visibility = View.GONE
                        }
                    } else {
                        requestsBadge.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        database.child("users/$username/requests")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    database.child("users/$username/requests")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val requests = snapshot.childrenCount.toInt()
                                    if (requests != 0) {
                                        requestsBadge.visibility = View.VISIBLE
                                        requestsBadge.text = requests.toString()
                                    } else {
                                        requestsBadge.visibility = View.GONE
                                    }
                                } else {
                                    requestsBadge.visibility = View.GONE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    database.child("users/$username/requests")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val requests = snapshot.childrenCount.toInt()
                                    if (requests != 0) {
                                        requestsBadge.visibility = View.VISIBLE
                                        requestsBadge.text = requests.toString()
                                    } else {
                                        requestsBadge.visibility = View.GONE
                                    }
                                } else {
                                    requestsBadge.visibility = View.GONE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    database.child("users/$username/requests")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val requests = snapshot.childrenCount.toInt()
                                    if (requests != 0) {
                                        requestsBadge.visibility = View.VISIBLE
                                        requestsBadge.text = requests.toString()
                                    } else {
                                        requestsBadge.visibility = View.GONE
                                    }
                                } else {
                                    requestsBadge.visibility = View.GONE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setBadgeColor(textView: TextView) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false)) {
            try {
                var json: String?
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
                val `object` = JSONObject(jsn.opt(12).toString())
                if (`object`.opt("badgeColor").toString().toInt() != 0) {
                    textView.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("badgeColor").toString().toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setToolbarColor(toolbar: Toolbar) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false)) {
            try {
                var json: String?
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

    fun setFabColor(fab: FloatingActionButton) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false)) {
            try {
                var json: String?
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
                val `object` = JSONObject(jsn.opt(2).toString())
                if (`object`.opt("floatingActionButtonColor").toString().toInt() != 0) {
                    fab.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("floatingActionButtonColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun setNavBarColor(navigationView: NavigationBarView) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("useAccentColors", false)) {
            try {
                var json: String?
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
                val `object` = JSONObject(jsn.opt(6).toString())
                if (`object`.opt("navigationItemColor").toString().toInt() != 0) {
                    navigationView.itemTextColor = ColorStateList.valueOf(
                        `object`.opt("navigationItemColor").toString().toInt()
                    )
                    navigationView.itemIconTintList = ColorStateList.valueOf(
                        `object`.opt("navigationItemColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun startLoading() {
        //Declare
        val noFriendsImage = findViewById<ImageView>(R.id.friendsImage)
        val noRequestsImage = findViewById<ImageView>(R.id.requestsImage)
        val noFriendsText = findViewById<TextView>(R.id.noFriendsText)
        val noRequestsText = findViewById<TextView>(R.id.noRequestsText)

        //All Gone
        noFriendsImage.visibility = View.GONE
        noRequestsImage.visibility = View.GONE
        noFriendsText.visibility = View.GONE
        noRequestsText.visibility = View.GONE

        //Declare layouts
        friendList = findViewById(R.id.friendsList)
        publicUserList = findViewById(R.id.publicUserList)
        requestList = findViewById(R.id.requestList)
        noFriendsLayout = findViewById(R.id.noFriendsLayout)
        noRequestsLayout = findViewById(R.id.noRequestsLayout)
        friendScroller = findViewById(R.id.friendsScrollView)
        publicUserScroller = findViewById(R.id.publicUserScrollView)
        requestScroller = findViewById(R.id.requestScrollView)

        // Declare friends listener
        friendsListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val contactUsername = snapshot.key
                val v: View = if (isDarkMode) {
                    LayoutInflater.from(this@Friends).inflate(R.layout.chat_preference_design_dark, null, false)
                } else {
                    LayoutInflater.from(this@Friends).inflate(R.layout.chat_preference_design_light, null, false)
                }
                val holder: PreferenceHolder = PreferenceHolder()
                holder.layout = v.findViewById(R.id.testChatDesign)
                holder.icon = v.findViewById(android.R.id.icon)
                holder.title = v.findViewById(android.R.id.title)
                holder.summary = v.findViewById(android.R.id.summary)
                holder.time = v.findViewById(R.id.timeTextViewChat)
                holder.unreadMessages = v.findViewById(R.id.unreadChatNumber)
                holder.verifiedImage = v.findViewById(R.id.verifiedImage)
                v.tag = holder
                holder.time!!.visibility = View.GONE
                holder.unreadMessages!!.visibility = View.GONE
                if (isDarkMode) {
                    holder.layout!!.background = ContextCompat.getDrawable(this@Friends, R.drawable.chat_background_dark)
                    holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_dark))
                } else {
                    holder.layout!!.background = ContextCompat.getDrawable(this@Friends, R.drawable.chat_background_light)
                    holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_light))
                }
                getProfilePicture(contactUsername, holder.icon!!)
                database.child("users/$contactUsername")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists() && snapshot.child("informations/name").exists()) {
                                val contactName =
                                    snapshot.child("informations/name").value.toString()
                                val contactInfo =
                                    snapshot.child("informations/info").value.toString()
                                if (snapshot.child("settings/publicInfo").exists()) {
                                    val publicInfo =
                                        snapshot.child("settings/publicInfo").value.toString()
                                    if (publicInfo == "true") {
                                        if (contactInfo == "string_default_info") {
                                            holder.summary!!.setText(R.string.default_info)
                                        } else {
                                            holder.summary!!.text = contactInfo
                                        }
                                    } else {
                                        database.child("users/$contactUsername/friends/$username")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()) {
                                                        if (contactInfo == "string_default_info") {
                                                            holder.summary!!.setText(R.string.default_info)
                                                        } else {
                                                            holder.summary!!.text = contactInfo
                                                        }
                                                    } else {
                                                        holder.summary!!.setText(R.string.default_info)
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                    }
                                } else {
                                    if (contactInfo == "string_default_info") {
                                        holder.summary!!.setText(R.string.default_info)
                                    } else {
                                        holder.summary!!.text = contactInfo
                                    }
                                }
                                if (snapshot.child("settings/staff").exists()) {
                                    val isStaff =
                                        java.lang.Boolean.parseBoolean(snapshot.child("settings/staff").value.toString())
                                    if (isStaff) {
                                        holder.verifiedImage!!.visibility = View.VISIBLE
                                    } else {
                                        holder.verifiedImage!!.visibility = View.GONE
                                    }
                                } else {
                                    holder.verifiedImage!!.visibility = View.GONE
                                }
                                holder.title!!.text = contactName
                                v.setOnClickListener {
                                    val intent = Intent(this@Friends, OpenProfile::class.java)
                                    intent.putExtra("username", contactUsername)
                                        .putExtra("comeFrom", "friends")
                                    startActivity(intent)
                                    //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
                                }
                                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                params.setMargins(5, 5, 5, 5)
                                holder.layout!!.layoutParams = params
                                friendList!!.addView(v)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                friendList!!.removeAllViews()
                database.child("users/$username/friends").removeEventListener(friendsListener!!)
                loadFriendList()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        // Declare public user listener
        publicUserListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child("informations/name").exists() && snapshot.child("settings")
                        .exists()
                ) {
                    val contactUsername = snapshot.key
                    val v: View = if (isDarkMode) {
                        LayoutInflater.from(this@Friends)
                            .inflate(R.layout.chat_preference_design_dark, null, false)
                    } else {
                        LayoutInflater.from(this@Friends)
                            .inflate(R.layout.chat_preference_design_light, null, false)
                    }
                    val holder: PreferenceHolder = PreferenceHolder()
                    holder.layout = v.findViewById(R.id.testChatDesign)
                    holder.icon = v.findViewById(android.R.id.icon)
                    holder.title = v.findViewById(android.R.id.title)
                    holder.summary = v.findViewById(android.R.id.summary)
                    holder.time = v.findViewById(R.id.timeTextViewChat)
                    holder.unreadMessages = v.findViewById(R.id.unreadChatNumber)
                    holder.verifiedImage = v.findViewById(R.id.verifiedImage)
                    v.tag = holder
                    holder.time!!.visibility = View.GONE
                    holder.unreadMessages!!.visibility = View.GONE
                    if (isDarkMode) {
                        holder.layout!!.background = ContextCompat.getDrawable(this@Friends, R.drawable.chat_background_dark)
                        holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_dark))
                    } else {
                        holder.layout!!.background = ContextCompat.getDrawable(this@Friends, R.drawable.chat_background_light)
                        holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_light))
                    }
                    getProfilePicture(contactUsername, holder.icon!!)
                    val contactName = snapshot.child("informations/name").value.toString()
                    val showInList = snapshot.child("settings/showInList").value.toString()
                    val info = snapshot.child("informations/info").value.toString()
                    if (snapshot.child("settings/publicInfo").exists()) {
                        val isPublicInfo = snapshot.child("settings/publicInfo").value.toString()
                        if (isPublicInfo == "true") {
                            if (info == "string_default_info") {
                                holder.summary!!.setText(R.string.default_info)
                            } else {
                                holder.summary!!.text = info
                            }
                        } else {
                            database.child("users/$contactUsername/friends/$username")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            if (info == "string_default_info") {
                                                holder.summary!!.setText(R.string.default_info)
                                            } else {
                                                holder.summary!!.text = info
                                            }
                                        } else {
                                            holder.summary!!.setText(R.string.default_info)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                })
                        }
                    } else {
                        if (info == "string_default_info") {
                            holder.summary!!.setText(R.string.default_info)
                        } else {
                            holder.summary!!.text = info
                        }
                    }
                    if (snapshot.child("settings/staff").exists()) {
                        val isStaff =
                            java.lang.Boolean.parseBoolean(snapshot.child("settings/staff").value.toString())
                        if (isStaff) {
                            holder.verifiedImage!!.visibility = View.VISIBLE
                        } else {
                            holder.verifiedImage!!.visibility = View.GONE
                        }
                    } else {
                        holder.verifiedImage!!.visibility = View.GONE
                    }
                    holder.title!!.text = contactName
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(5, 5, 5, 5)
                    holder.layout!!.layoutParams = params
                    v.setOnClickListener {
                        val intent = Intent(this@Friends, OpenProfile::class.java)
                        intent.putExtra("username", contactUsername)
                            .putExtra("comeFrom", "friends")
                        startActivity(intent)
                        //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
                    }
                    
                    if (contactUsername != username) {
                        if (showInList == "true") {
                            publicUserList!!.addView(v)
                        }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }

        // Declare request listener
        requestsListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()) {
                    val username1 = snapshot.key
                    database.child("users/$username1")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists() && snapshot.child("informations/name").exists()) {
                                    val v: View = if (isDarkMode) {
                                        LayoutInflater.from(this@Friends).inflate(R.layout.request_preference_layout_dark, null, false)
                                    } else {
                                        LayoutInflater.from(this@Friends).inflate(R.layout.request_preference_layout_light, null, false)
                                    }
                                    val holder = RequestHolder()
                                    holder.layout = v.findViewById(R.id.requestTemplateLayout)
                                    holder.icon = v.findViewById(android.R.id.icon)
                                    holder.title = v.findViewById(android.R.id.title)
                                    holder.summary = v.findViewById(android.R.id.summary)
                                    holder.verifiedImage = v.findViewById(R.id.verifiedImage)
                                    holder.acceptButton = v.findViewById(R.id.requestAcceptButton)
                                    holder.declineButton = v.findViewById(R.id.denyRequestButton)
                                    v.tag = holder
                                    setButtonColor(holder.acceptButton)
                                    setButtonColor(holder.declineButton)
                                    if (isDarkMode) {
                                        holder.layout!!.background = ContextCompat.getDrawable(this@Friends, R.drawable.chat_background_dark)
                                        holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_dark))
                                    } else {
                                        holder.layout!!.background = ContextCompat.getDrawable(this@Friends, R.drawable.chat_background_light)
                                        holder.summary!!.setTextColor(getColor(android.R.color.secondary_text_light))
                                    }
                                    getProfilePicture(username1, holder.icon!!)
                                    if (snapshot.child("settings/staff").exists()) {
                                        val isStaff =
                                            java.lang.Boolean.parseBoolean(snapshot.child("settings/staff").value.toString())
                                        if (isStaff) {
                                            holder.verifiedImage!!.visibility = View.VISIBLE
                                        } else {
                                            holder.verifiedImage!!.visibility = View.GONE
                                        }
                                    } else {
                                        holder.verifiedImage!!.visibility = View.GONE
                                    }
                                    val name = snapshot.child("informations/name").value.toString()
                                    holder.title!!.text = name
                                    if (snapshot.child("informations/info").exists()) {
                                        val info =
                                            snapshot.child("informations/info").value.toString()
                                        if (info == "string_default_info") {
                                            holder.summary!!.setText(R.string.default_info)
                                        } else {
                                            holder.summary!!.text = info
                                        }
                                    } else {
                                        holder.summary!!.setText(R.string.default_info)
                                    }
                                    v.setOnClickListener {
                                        val intent = Intent(context, OpenProfile::class.java)
                                        intent.putExtra("username", username1)
                                            .putExtra("comeFrom", "friends")
                                        startActivity(intent)
                                        //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
                                    }
                                    holder.acceptButton!!.setOnClickListener {
                                        acceptRequest(username1, v, holder.acceptButton!!, holder.declineButton!!)
                                    }
                                    holder.declineButton!!.setOnClickListener {
                                        removeRequest(username1, v, holder.acceptButton!!, holder.declineButton!!)
                                    }
                                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                    params.setMargins(5, 5, 5, 5)
                                    holder.layout!!.layoutParams = params

                                    requestList!!.addView(v)
                                } else {
                                    database.child("users/$username/requests/$username1").ref.removeValue()
                                        .addOnSuccessListener { }
                                        .addOnFailureListener { e: Exception ->
                                            Toast.makeText(this@Friends, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                database.child("users/$username/requests").removeEventListener(requestsListener!!)
                loadRequestList()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java)!!
                if (connected) {
                    // Alles verbunden

                    //Load requests
                    loadRequestList()

                    //Load friends
                    loadFriendList()

                    //Load public users
                    loadUserList()
                } else {
                    // Nicht mit Datenbank verbunden
                    startLoading()
                }
            }

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
        var verifiedImage: ImageView? = null
    }

    private inner class RequestHolder {
        var layout: ConstraintLayout? = null
        var icon: ImageView? = null
        var title: TextView? = null
        var summary: TextView? = null
        var verifiedImage: ImageView? = null
        var acceptButton: Button? = null
        var declineButton: Button? = null
    }

    fun loadFriendList() {
        val noFriendsImage = findViewById<ImageView>(R.id.friendsImage)
        val noFriendsText = findViewById<TextView>(R.id.noFriendsText)
        database.child("users/$username/friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        noFriendsImage.visibility = View.GONE
                        noFriendsText!!.visibility = View.GONE
                    } else {
                        noFriendsImage.visibility = View.VISIBLE
                        noFriendsText!!.visibility = View.VISIBLE
                    }
                    friendList!!.removeAllViews()
                    database.child("users/$username/friends")
                        .addChildEventListener(friendsListener!!)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun loadUserList() {
        publicUserList!!.removeAllViews()
        database.child("users").addChildEventListener(publicUserListener!!)
    }

    fun loadRequestList() {
        val noRequestsImage = findViewById<ImageView>(R.id.requestsImage)
        val noRequestsText = findViewById<TextView>(R.id.noRequestsText)
        database.child("users/$username/requests")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        noRequestsImage.visibility = View.GONE
                        noRequestsText.visibility = View.GONE
                        requestList!!.removeAllViews()
                        database.child("users/$username/requests").addChildEventListener(
                            requestsListener!!
                        )
                    } else {
                        noRequestsImage.visibility = View.VISIBLE
                        noRequestsText.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun acceptRequest(requestUsername: String?, v: View?, acceptButton: Button, declineButton: Button) {
        acceptButton.isEnabled = false
        declineButton.isEnabled = false
        database.child("users/$username/requests/$requestUsername").ref.removeValue()
            .addOnSuccessListener {
                database.child("users/$username/friends/$requestUsername").setValue("0")
                    .addOnSuccessListener {
                        database.child("users/$requestUsername/friends/$username").setValue("0")
                            .addOnSuccessListener {
                                acceptButton.isEnabled = true
                                declineButton.isEnabled = true
                                requestList!!.removeView(v)
                                Toast.makeText(this@Friends, R.string.profile_acceptRequestSuccess, Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e: Exception ->
                                Toast.makeText(this@Friends, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(this@Friends, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(this@Friends, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun removeRequest(requestUsername: String?, v: View?, acceptButton: Button, declineButton: Button) {
        acceptButton.isEnabled = false
        declineButton.isEnabled = false
        database.child("users/$username/requests/$requestUsername").ref.removeValue()
            .addOnSuccessListener {
                acceptButton.isEnabled = true
                declineButton.isEnabled = true
                requestList!!.removeView(v)
                Toast.makeText(this@Friends, R.string.profile_declineRequestSuccess, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(this@Friends, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun setButtonColor(button: Button?) {
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
                val `object` = JSONObject(jsn.opt(0).toString())
                if (`object`.opt("buttonColor").toString().toInt() != 0) {
                    button!!.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("buttonColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun getProfilePicture(username: String?, profilePicture: ImageView) {
        profilePicture.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_no_profile_picture
            )
        )
        val user = PreferenceManager.getDefaultSharedPreferences(this)
        val myUsername = user.getString("username", "UNKNOWN")
        database.child("users/$username/blockedUser/$myUsername")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        profilePicture.setImageDrawable(ContextCompat.getDrawable(this@Friends, R.drawable.ic_no_profile_picture))
                    } else {
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        profilePicture.setOnClickListener {
                            val intent = Intent(this@Friends, OpenProfilePicture::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
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
                                            getPic.getBytes(7000000)
                                                .addOnSuccessListener { bytes1: ByteArray ->
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
                                                    Toast.makeText(this@Friends, exception.message, Toast.LENGTH_SHORT).show()
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
                                        getPic.getBytes(7000000)
                                            .addOnSuccessListener { bytes1: ByteArray ->
                                                val bmp = BitmapFactory.decodeByteArray(bytes1, 0,
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
                                                    this@Friends,
                                                    error,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@Friends, e.message, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }.addOnFailureListener {
                            profilePicture.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@Friends,
                                    R.drawable.ic_no_profile_picture
                                )
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadRequests() {
        whereAmI = "requests"
        publicUserList!!.visibility = View.GONE
        friendList!!.visibility = View.GONE
        requestList!!.visibility = View.VISIBLE
        noRequestsLayout!!.visibility = View.VISIBLE
        noFriendsLayout!!.visibility = View.GONE
        publicUserScroller!!.visibility = View.GONE
        requestScroller!!.visibility = View.VISIBLE
        friendScroller!!.visibility = View.GONE
        val fab2 = findViewById<FloatingActionButton>(R.id.addFriendsByUsername)
        fab2.visibility = View.GONE
    }

    private fun loadRequestsWithMenu(item: MenuItem) {
        item.isChecked = true
        whereAmI = "requests"
        publicUserList!!.visibility = View.GONE
        friendList!!.visibility = View.GONE
        requestList!!.visibility = View.VISIBLE
        noRequestsLayout!!.visibility = View.VISIBLE
        noFriendsLayout!!.visibility = View.GONE
        publicUserScroller!!.visibility = View.GONE
        requestScroller!!.visibility = View.VISIBLE
        friendScroller!!.visibility = View.GONE
        val fab2 = findViewById<FloatingActionButton>(R.id.addFriendsByUsername)
        fab2.visibility = View.GONE
    }

    private fun loadFriends() {
        whereAmI = "friends"
        friendList!!.visibility = View.VISIBLE
        publicUserList!!.visibility = View.GONE
        requestList!!.visibility = View.GONE
        noFriendsLayout!!.visibility = View.VISIBLE
        noRequestsLayout!!.visibility = View.GONE
        friendScroller!!.visibility = View.VISIBLE
        publicUserScroller!!.visibility = View.GONE
        requestScroller!!.visibility = View.GONE
        val fab2 = findViewById<FloatingActionButton>(R.id.addFriendsByUsername)
        setFabColor(fab2)

        fab2.setOnClickListener{addContactByUsername()}

        fab2.visibility = View.VISIBLE
        fab2.setOnLongClickListener {
            val toast = Toast.makeText(this@Friends, "Anfrage per Benutzernamen senden", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 40)
            toast.show()
            false
        }
    }

    private fun loadPublicUsers() {
        whereAmI = "publicUsers"
        publicUserList!!.visibility = View.VISIBLE
        friendList!!.visibility = View.GONE
        requestList!!.visibility = View.GONE
        publicUserScroller!!.visibility = View.VISIBLE
        friendScroller!!.visibility = View.GONE
        requestScroller!!.visibility = View.GONE
        noRequestsLayout!!.visibility = View.GONE
        noFriendsLayout!!.visibility = View.GONE
        val fab2 = findViewById<FloatingActionButton>(R.id.addFriendsByUsername)
        fab2.visibility = View.GONE
    }

    private fun searchContact() {
        val intent = Intent(this, SearchUser::class.java)
        startActivity(intent)
        //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
    }

    fun addContactByUsername() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val username = preferences.getString("username", "UNKNOWN")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.friends_addUserTitle)
        val input = EditText(this)
        input.setHint(R.string.hint_username)
        val lp = LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        input.isSingleLine = true
        builder.setView(input)
            .setPositiveButton(R.string.friends_addUserAddButton) { _: DialogInterface?, _: Int ->
                val usernameText = input.text.toString()
                if (usernameText.trim { it <= ' ' }.isEmpty()) {
                    Toast.makeText(
                        this@Friends,
                        "Bitte gebe einen Benutzernamen ein!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (usernameText.contains("/") or usernameText.contains(".") or usernameText.contains(" ")
                ) {
                    Toast.makeText(
                        this@Friends,
                        "Die Eingabe enthält unzulässige Symoble!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (usernameText.trim { it <= ' ' } == username) {
                    Toast.makeText(
                        this@Friends,
                        "Du kannst dich selber nicht als Kontakt hinzufügen!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    checkUsername(usernameText)
                }
            }
            .setNeutralButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateThisList() {
        when (whereAmI) {
            "friends" -> {
                database.child("users/$username/friends").removeEventListener(friendsListener!!)
                loadFriendList()
            }
            "publicUsers" -> {
                database.child("users").removeEventListener(publicUserListener!!)
                loadUserList()
            }
            "requests" -> {
                database.child("users/$username/requests").removeEventListener(requestsListener!!)
                loadRequestList()
            }
        }
    }

    fun checkUsername(contactUsername: String) {
        val database = FirebaseDatabase.getInstance().reference
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val username = preferences.getString("username", "UNKNOWN")
        database.child("users/$contactUsername")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        database.child("users/$username/friends/$contactUsername")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        Toast.makeText(
                                            this@Friends,
                                            "Dieser Benutzer ist bereits dein Freund",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val notifyId = generateRandom(5).toInt()
                                        database.child("users/$contactUsername/requests/$username")
                                            .setValue(notifyId)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this@Friends,
                                                    "Anfrage erfolgreich gesendet!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener { e: Exception ->
                                                Toast.makeText(
                                                    this@Friends,
                                                    "Fehler: " + e.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    } else {
                        Toast.makeText(
                            this@Friends,
                            "Dieser Benutzer existiert nicht!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun setLightMode() {
        setTheme(R.style.contactsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.contactsDark)
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

    @IgnoreExtraProperties
    class Contact(var chatName: String)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                //overridePendingTransition(R.anim.fade_out, R.anim.slide_down)
            }
            R.id.searchForUsersItem -> searchContact()
            R.id.updateThisList -> updateThisList()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        //overridePendingTransition(R.anim.fade_out, R.anim.slide_down)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.friends_toolbar_menu, menu)
        return true
    }

    companion object {
        var requestFrame: FrameLayout? = null
        var coordinatorLayout: CoordinatorLayout? = null
        var context: Context? = null
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