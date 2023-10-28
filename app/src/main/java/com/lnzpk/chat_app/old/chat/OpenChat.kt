package com.lnzpk.chat_app.old.chat

import android.app.Activity
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.bottomSheets.MyBottomSheetDialogFragment
import com.lnzpk.chat_app.old.colors.Colors
import com.lnzpk.chat_app.old.colors.Colors.setButtonColor
import com.lnzpk.chat_app.old.colors.Colors.setDateMessageBubbleColor
import com.lnzpk.chat_app.old.colors.Colors.setFabColor
import com.lnzpk.chat_app.old.colors.Colors.setToolbarColor
import com.lnzpk.chat_app.old.group.OpenGroupInfo
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import com.lnzpk.chat_app.old.profile.OpenProfile
import com.lnzpk.chat_app.old.service.StatusService
import com.lnzpk.chat_app.old.settings.BackgroundSettings
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Random
import java.util.TimeZone

class OpenChat : AppCompatActivity() {
    private var myUsername: String? = null
    private var database: DatabaseReference? = null
    private var hdlr = Handler()
    private var replying = false
    private var replyKey: String? = null
    private var editing = false
    private var editKey: String? = null
    private var editedText: TextView? = null
    private var blocked = false
    private var otherBlocked = false
    private var otherUsername: String? = null
    private var childEventListener: ChildEventListener? = null
    private var toolbar: Toolbar? = null
    private var otherName: String? = null
    private var myName: String? = null
    private var db: DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this@OpenChat)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.chat_layout)

        db = DBHelper(this, null)

        val statusService = Intent(this, StatusService::class.java)
        statusService.addFlags(Intent.FLAG_FROM_BACKGROUND).addFlags(Intent.FLAG_RECEIVER_NO_ABORT)
        startService(statusService)

        toolbar = findViewById(R.id.chatToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar!!)
        database = FirebaseDatabase.getInstance().reference
        myUsername = db!!.getCurrentUsername()
        replying = false

        // Clear all notification
        val nMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nMgr.cancelAll()

        val chat = intent
        otherUsername = chat.extras!!["username"].toString()
        toolbar!!.setOnClickListener {
            val openIntent = Intent(this, OpenProfile::class.java)
            openIntent.putExtra("username", otherUsername)
                .putExtra("comeFrom", "chat")
            startActivity(openIntent)
        }

        // Get online status
        database!!.child("users/$otherUsername").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                database!!.child("users/$otherUsername")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.child("blockedUser/$myUsername").exists()) {
                                    toolbar!!.subtitle = null
                                    toolbar!!.title = otherUsername
                                    otherBlocked = true
                                } else {
                                    otherBlocked = false
                                    otherName = snapshot.child("informations/name").value.toString()
                                    toolbar!!.title = otherName
                                    if (snapshot.child("informations/status").exists()) {
                                        val status =
                                            snapshot.child("informations/status").value.toString()
                                        if (status == "offline") {
                                            toolbar!!.setSubtitleTextAppearance(
                                                this@OpenChat,
                                                R.style.offlineSubtitle
                                            )
                                            database!!.child("users/$otherUsername")
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (snapshot.child("informations/lastOnline")
                                                                .exists()
                                                        ) {
                                                            if (snapshot.child("settings/lastOnline")
                                                                    .exists()
                                                            ) {
                                                                val lastOnlineStr =
                                                                    snapshot.child("settings/lastOnline").value.toString()
                                                                if (lastOnlineStr == "none") {
                                                                    toolbar!!.subtitle = null
                                                                    if (Colors.isDarkMode(this@OpenChat)) {
                                                                        toolbar!!.setSubtitleTextColor(
                                                                            getColor(android.R.color.secondary_text_dark)
                                                                        )
                                                                    } else {
                                                                        toolbar!!.setSubtitleTextColor(
                                                                            getColor(android.R.color.secondary_text_light)
                                                                        )
                                                                    }
                                                                } else if (lastOnlineStr == "everyone") {
                                                                    try {
                                                                        val sdf =
                                                                            SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                        //LastLogin-Time
                                                                        val lastLogin =
                                                                            snapshot.child("informations/lastOnline").value.toString()
                                                                        val cal =
                                                                            Calendar.getInstance()
                                                                        cal.time =
                                                                            sdf.parse(lastLogin)
                                                                        cal.timeZone =
                                                                            TimeZone.getDefault()
                                                                        val oldYear =
                                                                            DateFormat.format(
                                                                                "yyyy",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldMonth =
                                                                            DateFormat.format(
                                                                                "MM",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldDay =
                                                                            DateFormat.format(
                                                                                "dd",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldHour =
                                                                            DateFormat.format(
                                                                                "HH",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldMin =
                                                                            DateFormat.format(
                                                                                "mm",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String

                                                                        //Current-Time
                                                                        val rightNow1 =
                                                                            Calendar.getInstance()
                                                                        val date3 = rightNow1.time
                                                                        val format1 =
                                                                            SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                        val formattedDate1 =
                                                                            format1.format(date3)
                                                                        val date2 = format1.parse(
                                                                            formattedDate1
                                                                        )
                                                                        rightNow1.time = date2
                                                                        rightNow1.timeZone =
                                                                            TimeZone.getDefault()
                                                                        val currentYear =
                                                                            DateFormat.format(
                                                                                "yyyy",
                                                                                date2
                                                                            ) as String
                                                                        val currentMonth =
                                                                            DateFormat.format(
                                                                                "MM",
                                                                                date2
                                                                            ) as String
                                                                        val currentDay =
                                                                            DateFormat.format(
                                                                                "dd",
                                                                                date2
                                                                            ) as String
                                                                        val currentHour =
                                                                            DateFormat.format(
                                                                                "HH",
                                                                                date2
                                                                            ) as String
                                                                        val currentMin =
                                                                            DateFormat.format(
                                                                                "mm",
                                                                                date2
                                                                            ) as String
                                                                        val date =
                                                                            "$oldDay.$oldMonth.$oldYear"
                                                                        val time =
                                                                            "$oldHour:$oldMin"

                                                                        //check values
                                                                        val i =
                                                                            currentDay.toInt() - oldDay.toInt()
                                                                        if (Colors.isDarkMode(this@OpenChat)) {
                                                                            //dark-mode
                                                                            if (oldYear == currentYear) {
                                                                                if (oldMonth == currentMonth) {
                                                                                    if (oldDay == currentDay) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        if (i == 0) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else if (i == 1) {
                                                                                            toolbar!!.subtitle =
                                                                                                getString(
                                                                                                    R.string.lastOnline_yesterday
                                                                                                )
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else if (i <= -1) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_date
                                                                                                ).replace(
                                                                                                    "{date}",
                                                                                                    date
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_daysAgo
                                                                                                ).replace(
                                                                                                    "{days}",
                                                                                                    i.toString()
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_dark
                                                                                        )
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_dark)
                                                                                )
                                                                            }
                                                                        } else {
                                                                            //light-Mode
                                                                            if (oldYear == currentYear) {
                                                                                if (oldMonth == currentMonth) {
                                                                                    if (oldDay == currentDay) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        if (i == 0) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else if (i == 1) {
                                                                                            toolbar!!.subtitle =
                                                                                                getString(
                                                                                                    R.string.lastOnline_yesterday
                                                                                                )
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else if (i <= -1) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_date
                                                                                                ).replace(
                                                                                                    "{date}",
                                                                                                    date
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_daysAgo
                                                                                                ).replace(
                                                                                                    "{days}",
                                                                                                    i.toString()
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_light
                                                                                        )
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_light)
                                                                                )
                                                                            }
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(
                                                                            this@OpenChat,
                                                                            "Error: " + e.message,
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                } else if (lastOnlineStr == "justFriends") {
                                                                    if (snapshot.child("friends/$myUsername")
                                                                            .exists()
                                                                    ) {
                                                                        try {
                                                                            val sdf =
                                                                                SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                            //LastLogin-Time
                                                                            val lastLogin =
                                                                                snapshot.child("informations/lastOnline").value.toString()
                                                                            val cal =
                                                                                Calendar.getInstance()
                                                                            cal.time =
                                                                                sdf.parse(lastLogin)
                                                                            cal.timeZone =
                                                                                TimeZone.getDefault()
                                                                            val oldYear =
                                                                                DateFormat.format(
                                                                                    "yyyy",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldMonth =
                                                                                DateFormat.format(
                                                                                    "MM",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldDay =
                                                                                DateFormat.format(
                                                                                    "dd",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldHour =
                                                                                DateFormat.format(
                                                                                    "HH",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldMin =
                                                                                DateFormat.format(
                                                                                    "mm",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String

                                                                            //Current-Time
                                                                            val rightNow1 =
                                                                                Calendar.getInstance()
                                                                            val date3 =
                                                                                rightNow1.time
                                                                            val format1 =
                                                                                SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                            val formattedDate1 =
                                                                                format1.format(date3)
                                                                            val date2 =
                                                                                format1.parse(
                                                                                    formattedDate1
                                                                                )
                                                                            rightNow1.time = date2
                                                                            rightNow1.timeZone =
                                                                                TimeZone.getDefault()
                                                                            val currentYear =
                                                                                DateFormat.format(
                                                                                    "yyyy",
                                                                                    date2
                                                                                ) as String
                                                                            val currentMonth =
                                                                                DateFormat.format(
                                                                                    "MM",
                                                                                    date2
                                                                                ) as String
                                                                            val currentDay =
                                                                                DateFormat.format(
                                                                                    "dd",
                                                                                    date2
                                                                                ) as String
                                                                            val currentHour =
                                                                                DateFormat.format(
                                                                                    "HH",
                                                                                    date2
                                                                                ) as String
                                                                            val currentMin =
                                                                                DateFormat.format(
                                                                                    "mm",
                                                                                    date2
                                                                                ) as String
                                                                            val date =
                                                                                "$oldDay.$oldMonth.$oldYear"
                                                                            val time =
                                                                                "$oldHour:$oldMin"

                                                                            //check values
                                                                            val i =
                                                                                currentDay.toInt() - oldDay.toInt()
                                                                            if (Colors.isDarkMode(
                                                                                    this@OpenChat
                                                                                )
                                                                            ) {
                                                                                //dark-mode
                                                                                if (oldYear == currentYear) {
                                                                                    if (oldMonth == currentMonth) {
                                                                                        if (oldDay == currentDay) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            if (i == 0) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_time
                                                                                                    ).replace(
                                                                                                        "{time}",
                                                                                                        time
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            } else if (i == 1) {
                                                                                                toolbar!!.subtitle =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_yesterday
                                                                                                    )
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            } else if (i <= -1) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_date
                                                                                                    ).replace(
                                                                                                        "{date}",
                                                                                                        date
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            } else {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_daysAgo
                                                                                                    ).replace(
                                                                                                        "{days}",
                                                                                                        i.toString()
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_dark
                                                                                        )
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                //light-Mode
                                                                                if (oldYear == currentYear) {
                                                                                    if (oldMonth == currentMonth) {
                                                                                        if (oldDay == currentDay) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            if (i == 0) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_time
                                                                                                    ).replace(
                                                                                                        "{time}",
                                                                                                        time
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            } else if (i == 1) {
                                                                                                toolbar!!.subtitle =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_yesterday
                                                                                                    )
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            } else if (i <= -1) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_date
                                                                                                    ).replace(
                                                                                                        "{date}",
                                                                                                        date
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            } else {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_daysAgo
                                                                                                    ).replace(
                                                                                                        "{days}",
                                                                                                        i.toString()
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_light
                                                                                        )
                                                                                    )
                                                                                }
                                                                            }
                                                                        } catch (e: Exception) {
                                                                            Toast.makeText(
                                                                                this@OpenChat,
                                                                                "Fehler: " + e.message,
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                try {
                                                                    val sdf =
                                                                        SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                    //LastLogin-Time
                                                                    val lastLogin =
                                                                        snapshot.child("informations/lastOnline").value.toString()
                                                                    val cal = Calendar.getInstance()
                                                                    cal.time = sdf.parse(lastLogin)
                                                                    cal.timeZone =
                                                                        TimeZone.getDefault()
                                                                    val oldYear = DateFormat.format(
                                                                        "yyyy",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String
                                                                    val oldMonth =
                                                                        DateFormat.format(
                                                                            "MM",
                                                                            sdf.parse(lastLogin)
                                                                        ) as String
                                                                    val oldDay = DateFormat.format(
                                                                        "dd",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String
                                                                    val oldHour = DateFormat.format(
                                                                        "HH",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String
                                                                    val oldMin = DateFormat.format(
                                                                        "mm",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String

                                                                    //Current-Time
                                                                    val rightNow1 =
                                                                        Calendar.getInstance()
                                                                    val date3 = rightNow1.time
                                                                    val format1 =
                                                                        SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                    val formattedDate1 =
                                                                        format1.format(date3)
                                                                    val date2 =
                                                                        format1.parse(formattedDate1)
                                                                    rightNow1.time = date2
                                                                    rightNow1.timeZone =
                                                                        TimeZone.getDefault()
                                                                    val currentYear =
                                                                        DateFormat.format(
                                                                            "yyyy",
                                                                            date2
                                                                        ) as String
                                                                    val currentMonth =
                                                                        DateFormat.format(
                                                                            "MM",
                                                                            date2
                                                                        ) as String
                                                                    val currentDay =
                                                                        DateFormat.format(
                                                                            "dd",
                                                                            date2
                                                                        ) as String
                                                                    val currentHour =
                                                                        DateFormat.format(
                                                                            "HH",
                                                                            date2
                                                                        ) as String
                                                                    val currentMin =
                                                                        DateFormat.format(
                                                                            "mm",
                                                                            date2
                                                                        ) as String
                                                                    val date =
                                                                        "$oldDay.$oldMonth.$oldYear"
                                                                    val time = "$oldHour:$oldMin"

                                                                    //check values
                                                                    val i =
                                                                        currentDay.toInt() - oldDay.toInt()
                                                                    if (Colors.isDarkMode(this@OpenChat)) {
                                                                        //dark-mode
                                                                        if (oldYear == currentYear) {
                                                                            if (oldMonth == currentMonth) {
                                                                                if (oldDay == currentDay) {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_time).replace(
                                                                                            "{time}",
                                                                                            time
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_dark
                                                                                        )
                                                                                    )
                                                                                } else {
                                                                                    if (i == 0) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else if (i == 1) {
                                                                                        toolbar!!.subtitle =
                                                                                            getString(
                                                                                                R.string.lastOnline_yesterday
                                                                                            )
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else if (i <= -1) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_daysAgo
                                                                                            ).replace(
                                                                                                "{days}",
                                                                                                i.toString()
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_dark)
                                                                                )
                                                                            }
                                                                        } else {
                                                                            val onlineText =
                                                                                getString(R.string.lastOnline_date).replace(
                                                                                    "{date}",
                                                                                    date
                                                                                )
                                                                            toolbar!!.subtitle =
                                                                                onlineText
                                                                            toolbar!!.setSubtitleTextColor(
                                                                                getColor(android.R.color.secondary_text_dark)
                                                                            )
                                                                        }
                                                                    } else {
                                                                        //light-Mode
                                                                        if (oldYear == currentYear) {
                                                                            if (oldMonth == currentMonth) {
                                                                                if (oldDay == currentDay) {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_time).replace(
                                                                                            "{time}",
                                                                                            time
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_light
                                                                                        )
                                                                                    )
                                                                                } else {
                                                                                    if (i == 0) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else if (i == 1) {
                                                                                        toolbar!!.subtitle =
                                                                                            getString(
                                                                                                R.string.lastOnline_yesterday
                                                                                            )
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else if (i <= -1) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_daysAgo
                                                                                            ).replace(
                                                                                                "{days}",
                                                                                                i.toString()
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_light)
                                                                                )
                                                                            }
                                                                        } else {
                                                                            val onlineText =
                                                                                getString(R.string.lastOnline_date).replace(
                                                                                    "{date}",
                                                                                    date
                                                                                )
                                                                            toolbar!!.subtitle =
                                                                                onlineText
                                                                            toolbar!!.setSubtitleTextColor(
                                                                                getColor(android.R.color.secondary_text_light)
                                                                            )
                                                                        }
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Toast.makeText(
                                                                        this@OpenChat,
                                                                        "Fehler: " + e.message,
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        } else {
                                                            toolbar!!.subtitle = null
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                        } else if (status == "online") {
                                            toolbar!!.setSubtitleTextAppearance(
                                                this@OpenChat,
                                                R.style.onlineSubtitle
                                            )
                                            toolbar!!.subtitle = "online"
                                            toolbar!!.setSubtitleTextColor(Color.GREEN)
                                        }
                                    } else {
                                        toolbar!!.subtitle = null
                                        if (Colors.isDarkMode(this@OpenChat)) {
                                            toolbar!!.setSubtitleTextColor(getColor(android.R.color.secondary_text_dark))
                                        } else {
                                            toolbar!!.setSubtitleTextColor(getColor(android.R.color.secondary_text_light))
                                        }
                                    }
                                }
                            } else {
                                toolbar!!.setTitle(R.string.chats_deletedProfile)
                                toolbar!!.subtitle = null
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                database!!.child("users/$otherUsername")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.child("blockedUser/$myUsername").exists()) {
                                    toolbar!!.subtitle = ""
                                    toolbar!!.title = otherUsername
                                    otherBlocked = true
                                } else {
                                    otherBlocked = false
                                    if (snapshot.child("informations/status").exists()) {
                                        toolbar!!.title = snapshot.child("informations/name").value.toString()
                                        val status = snapshot.child("informations/status").getValue(
                                            String::class.java
                                        )
                                        if (status == "offline") {
                                            database!!.child("users/$otherUsername")
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (snapshot.child("informations/lastOnline")
                                                                .exists()
                                                        ) {
                                                            if (snapshot.child("settings/lastOnline")
                                                                    .exists()
                                                            ) {
                                                                val lastOnlineStr =
                                                                    snapshot.child("settings/lastOnline").value.toString()
                                                                if (lastOnlineStr == "none") {
                                                                    toolbar!!.subtitle = null
                                                                    if (Colors.isDarkMode(this@OpenChat)) {
                                                                        toolbar!!.setSubtitleTextColor(
                                                                            getColor(android.R.color.secondary_text_dark)
                                                                        )
                                                                    } else {
                                                                        toolbar!!.setSubtitleTextColor(
                                                                            getColor(android.R.color.secondary_text_light)
                                                                        )
                                                                    }
                                                                } else if (lastOnlineStr == "everyone") {
                                                                    try {
                                                                        val sdf =
                                                                            SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                        //LastLogin-Time
                                                                        val lastLogin =
                                                                            snapshot.child("informations/lastOnline").value.toString()
                                                                        val cal =
                                                                            Calendar.getInstance()
                                                                        cal.time =
                                                                            sdf.parse(lastLogin)
                                                                        cal.timeZone =
                                                                            TimeZone.getDefault()
                                                                        val oldYear =
                                                                            DateFormat.format(
                                                                                "yyyy",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldMonth =
                                                                            DateFormat.format(
                                                                                "MM",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldDay =
                                                                            DateFormat.format(
                                                                                "dd",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldHour =
                                                                            DateFormat.format(
                                                                                "HH",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String
                                                                        val oldMin =
                                                                            DateFormat.format(
                                                                                "mm",
                                                                                sdf.parse(lastLogin)
                                                                            ) as String

                                                                        //Current-Time
                                                                        val rightNow1 =
                                                                            Calendar.getInstance()
                                                                        val date3 = rightNow1.time
                                                                        val format1 =
                                                                            SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                        val formattedDate1 =
                                                                            format1.format(date3)
                                                                        val date2 = format1.parse(
                                                                            formattedDate1
                                                                        )
                                                                        rightNow1.time = date2
                                                                        rightNow1.timeZone =
                                                                            TimeZone.getDefault()
                                                                        val currentYear =
                                                                            DateFormat.format(
                                                                                "yyyy",
                                                                                date2
                                                                            ) as String
                                                                        val currentMonth =
                                                                            DateFormat.format(
                                                                                "MM",
                                                                                date2
                                                                            ) as String
                                                                        val currentDay =
                                                                            DateFormat.format(
                                                                                "dd",
                                                                                date2
                                                                            ) as String
                                                                        val currentHour =
                                                                            DateFormat.format(
                                                                                "HH",
                                                                                date2
                                                                            ) as String
                                                                        val currentMin =
                                                                            DateFormat.format(
                                                                                "mm",
                                                                                date2
                                                                            ) as String
                                                                        val date =
                                                                            "$oldDay.$oldMonth.$oldYear"
                                                                        val time =
                                                                            "$oldHour:$oldMin"

                                                                        //check values
                                                                        val i =
                                                                            currentDay.toInt() - oldDay.toInt()
                                                                        if (Colors.isDarkMode(this@OpenChat)) {
                                                                            //dark-mode
                                                                            if (oldYear == currentYear) {
                                                                                if (oldMonth == currentMonth) {
                                                                                    if (oldDay == currentDay) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        if (i == 0) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else if (i == 1) {
                                                                                            toolbar!!.subtitle =
                                                                                                getString(
                                                                                                    R.string.lastOnline_yesterday
                                                                                                )
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else if (i <= -1) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_date
                                                                                                ).replace(
                                                                                                    "{date}",
                                                                                                    date
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_daysAgo
                                                                                                ).replace(
                                                                                                    "{days}",
                                                                                                    i.toString()
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_dark
                                                                                        )
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_dark)
                                                                                )
                                                                            }
                                                                        } else {
                                                                            //light-Mode
                                                                            if (oldYear == currentYear) {
                                                                                if (oldMonth == currentMonth) {
                                                                                    if (oldDay == currentDay) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        if (i == 0) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else if (i == 1) {
                                                                                            toolbar!!.subtitle =
                                                                                                getString(
                                                                                                    R.string.lastOnline_yesterday
                                                                                                )
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else if (i <= -1) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_date
                                                                                                ).replace(
                                                                                                    "{date}",
                                                                                                    date
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_daysAgo
                                                                                                ).replace(
                                                                                                    "{days}",
                                                                                                    i.toString()
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_light
                                                                                        )
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_light)
                                                                                )
                                                                            }
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(
                                                                            this@OpenChat,
                                                                            "Fehler: " + e.message,
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                } else if (lastOnlineStr == "justFriends") {
                                                                    if (snapshot.child("friends/$myUsername")
                                                                            .exists()
                                                                    ) {
                                                                        try {
                                                                            val sdf =
                                                                                SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                            //LastLogin-Time
                                                                            val lastLogin =
                                                                                snapshot.child("informations/lastOnline").value.toString()
                                                                            val cal =
                                                                                Calendar.getInstance()
                                                                            cal.time =
                                                                                sdf.parse(lastLogin)
                                                                            cal.timeZone =
                                                                                TimeZone.getDefault()
                                                                            val oldYear =
                                                                                DateFormat.format(
                                                                                    "yyyy",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldMonth =
                                                                                DateFormat.format(
                                                                                    "MM",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldDay =
                                                                                DateFormat.format(
                                                                                    "dd",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldHour =
                                                                                DateFormat.format(
                                                                                    "HH",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String
                                                                            val oldMin =
                                                                                DateFormat.format(
                                                                                    "mm",
                                                                                    sdf.parse(
                                                                                        lastLogin
                                                                                    )
                                                                                ) as String

                                                                            //Current-Time
                                                                            val rightNow1 =
                                                                                Calendar.getInstance()
                                                                            val date3 =
                                                                                rightNow1.time
                                                                            val format1 =
                                                                                SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                            val formattedDate1 =
                                                                                format1.format(date3)
                                                                            val date2 =
                                                                                format1.parse(
                                                                                    formattedDate1
                                                                                )
                                                                            rightNow1.time = date2
                                                                            rightNow1.timeZone =
                                                                                TimeZone.getDefault()
                                                                            val currentYear =
                                                                                DateFormat.format(
                                                                                    "yyyy",
                                                                                    date2
                                                                                ) as String
                                                                            val currentMonth =
                                                                                DateFormat.format(
                                                                                    "MM",
                                                                                    date2
                                                                                ) as String
                                                                            val currentDay =
                                                                                DateFormat.format(
                                                                                    "dd",
                                                                                    date2
                                                                                ) as String
                                                                            val currentHour =
                                                                                DateFormat.format(
                                                                                    "HH",
                                                                                    date2
                                                                                ) as String
                                                                            val currentMin =
                                                                                DateFormat.format(
                                                                                    "mm",
                                                                                    date2
                                                                                ) as String
                                                                            val date =
                                                                                "$oldDay.$oldMonth.$oldYear"
                                                                            val time =
                                                                                "$oldHour:$oldMin"

                                                                            //check values
                                                                            val i =
                                                                                currentDay.toInt() - oldDay.toInt()
                                                                            if (Colors.isDarkMode(
                                                                                    this@OpenChat
                                                                                )
                                                                            ) {
                                                                                //dark-mode
                                                                                if (oldYear == currentYear) {
                                                                                    if (oldMonth == currentMonth) {
                                                                                        if (oldDay == currentDay) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_dark
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            if (i == 0) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_time
                                                                                                    ).replace(
                                                                                                        "{time}",
                                                                                                        time
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            } else if (i == 1) {
                                                                                                toolbar!!.subtitle =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_yesterday
                                                                                                    )
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            } else if (i <= -1) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_date
                                                                                                    ).replace(
                                                                                                        "{date}",
                                                                                                        date
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            } else {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_daysAgo
                                                                                                    ).replace(
                                                                                                        "{days}",
                                                                                                        i.toString()
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_dark
                                                                                                    )
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_dark
                                                                                        )
                                                                                    )
                                                                                }
                                                                            } else {
                                                                                //light-Mode
                                                                                if (oldYear == currentYear) {
                                                                                    if (oldMonth == currentMonth) {
                                                                                        if (oldDay == currentDay) {
                                                                                            val onlineText =
                                                                                                getString(
                                                                                                    R.string.lastOnline_time
                                                                                                ).replace(
                                                                                                    "{time}",
                                                                                                    time
                                                                                                )
                                                                                            toolbar!!.subtitle =
                                                                                                onlineText
                                                                                            toolbar!!.setSubtitleTextColor(
                                                                                                getColor(
                                                                                                    android.R.color.secondary_text_light
                                                                                                )
                                                                                            )
                                                                                        } else {
                                                                                            if (i == 0) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_time
                                                                                                    ).replace(
                                                                                                        "{time}",
                                                                                                        time
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            } else if (i == 1) {
                                                                                                toolbar!!.subtitle =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_yesterday
                                                                                                    )
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            } else if (i <= -1) {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_date
                                                                                                    ).replace(
                                                                                                        "{date}",
                                                                                                        date
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            } else {
                                                                                                val onlineText =
                                                                                                    getString(
                                                                                                        R.string.lastOnline_daysAgo
                                                                                                    ).replace(
                                                                                                        "{days}",
                                                                                                        i.toString()
                                                                                                    )
                                                                                                toolbar!!.subtitle =
                                                                                                    onlineText
                                                                                                toolbar!!.setSubtitleTextColor(
                                                                                                    getColor(
                                                                                                        android.R.color.secondary_text_light
                                                                                                    )
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                } else {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_date).replace(
                                                                                            "{date}",
                                                                                            date
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_light
                                                                                        )
                                                                                    )
                                                                                }
                                                                            }
                                                                        } catch (e: Exception) {
                                                                            Toast.makeText(
                                                                                this@OpenChat,
                                                                                "Fehler: " + e.message,
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                try {
                                                                    val sdf =
                                                                        SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                    //LastLogin-Time
                                                                    val lastLogin =
                                                                        snapshot.child("informations/lastOnline").value.toString()
                                                                    val cal = Calendar.getInstance()
                                                                    cal.time = sdf.parse(lastLogin)
                                                                    cal.timeZone =
                                                                        TimeZone.getDefault()
                                                                    val oldYear = DateFormat.format(
                                                                        "yyyy",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String
                                                                    val oldMonth =
                                                                        DateFormat.format(
                                                                            "MM",
                                                                            sdf.parse(lastLogin)
                                                                        ) as String
                                                                    val oldDay = DateFormat.format(
                                                                        "dd",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String
                                                                    val oldHour = DateFormat.format(
                                                                        "HH",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String
                                                                    val oldMin = DateFormat.format(
                                                                        "mm",
                                                                        sdf.parse(lastLogin)
                                                                    ) as String

                                                                    //Current-Time
                                                                    val rightNow1 =
                                                                        Calendar.getInstance()
                                                                    val date3 = rightNow1.time
                                                                    val format1 =
                                                                        SimpleDateFormat("yyyy-MM-dd HH:mm")
                                                                    val formattedDate1 =
                                                                        format1.format(date3)
                                                                    val date2 =
                                                                        format1.parse(formattedDate1)
                                                                    rightNow1.time = date2
                                                                    rightNow1.timeZone =
                                                                        TimeZone.getDefault()
                                                                    val currentYear =
                                                                        DateFormat.format(
                                                                            "yyyy",
                                                                            date2
                                                                        ) as String
                                                                    val currentMonth =
                                                                        DateFormat.format(
                                                                            "MM",
                                                                            date2
                                                                        ) as String
                                                                    val currentDay =
                                                                        DateFormat.format(
                                                                            "dd",
                                                                            date2
                                                                        ) as String
                                                                    val currentHour =
                                                                        DateFormat.format(
                                                                            "HH",
                                                                            date2
                                                                        ) as String
                                                                    val currentMin =
                                                                        DateFormat.format(
                                                                            "mm",
                                                                            date2
                                                                        ) as String
                                                                    val date =
                                                                        "$oldDay.$oldMonth.$oldYear"
                                                                    val time = "$oldHour:$oldMin"

                                                                    //check values
                                                                    val i =
                                                                        currentDay.toInt() - oldDay.toInt()
                                                                    if (Colors.isDarkMode(this@OpenChat)) {
                                                                        //dark-mode
                                                                        if (oldYear == currentYear) {
                                                                            if (oldMonth == currentMonth) {
                                                                                if (oldDay == currentDay) {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_time).replace(
                                                                                            "{time}",
                                                                                            time
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_dark
                                                                                        )
                                                                                    )
                                                                                } else {
                                                                                    if (i == 0) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else if (i == 1) {
                                                                                        toolbar!!.subtitle =
                                                                                            getString(
                                                                                                R.string.lastOnline_yesterday
                                                                                            )
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else if (i <= -1) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_daysAgo
                                                                                            ).replace(
                                                                                                "{days}",
                                                                                                i.toString()
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_dark
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_dark)
                                                                                )
                                                                            }
                                                                        } else {
                                                                            val onlineText =
                                                                                getString(R.string.lastOnline_date).replace(
                                                                                    "{date}",
                                                                                    date
                                                                                )
                                                                            toolbar!!.subtitle =
                                                                                onlineText
                                                                            toolbar!!.setSubtitleTextColor(
                                                                                getColor(android.R.color.secondary_text_dark)
                                                                            )
                                                                        }
                                                                    } else {
                                                                        //light-Mode
                                                                        if (oldYear == currentYear) {
                                                                            if (oldMonth == currentMonth) {
                                                                                if (oldDay == currentDay) {
                                                                                    val onlineText =
                                                                                        getString(R.string.lastOnline_time).replace(
                                                                                            "{time}",
                                                                                            time
                                                                                        )
                                                                                    toolbar!!.subtitle =
                                                                                        onlineText
                                                                                    toolbar!!.setSubtitleTextColor(
                                                                                        getColor(
                                                                                            android.R.color.secondary_text_light
                                                                                        )
                                                                                    )
                                                                                } else {
                                                                                    if (i == 0) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_time
                                                                                            ).replace(
                                                                                                "{time}",
                                                                                                time
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else if (i == 1) {
                                                                                        toolbar!!.subtitle =
                                                                                            getString(
                                                                                                R.string.lastOnline_yesterday
                                                                                            )
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else if (i <= -1) {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_date
                                                                                            ).replace(
                                                                                                "{date}",
                                                                                                date
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    } else {
                                                                                        val onlineText =
                                                                                            getString(
                                                                                                R.string.lastOnline_daysAgo
                                                                                            ).replace(
                                                                                                "{days}",
                                                                                                i.toString()
                                                                                            )
                                                                                        toolbar!!.subtitle =
                                                                                            onlineText
                                                                                        toolbar!!.setSubtitleTextColor(
                                                                                            getColor(
                                                                                                android.R.color.secondary_text_light
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                val onlineText =
                                                                                    getString(R.string.lastOnline_date).replace(
                                                                                        "{date}",
                                                                                        date
                                                                                    )
                                                                                toolbar!!.subtitle =
                                                                                    onlineText
                                                                                toolbar!!.setSubtitleTextColor(
                                                                                    getColor(android.R.color.secondary_text_light)
                                                                                )
                                                                            }
                                                                        } else {
                                                                            val onlineText =
                                                                                getString(R.string.lastOnline_date).replace(
                                                                                    "{date}",
                                                                                    date
                                                                                )
                                                                            toolbar!!.subtitle =
                                                                                onlineText
                                                                            toolbar!!.setSubtitleTextColor(
                                                                                getColor(android.R.color.secondary_text_light)
                                                                            )
                                                                        }
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Toast.makeText(
                                                                        this@OpenChat,
                                                                        "Fehler: " + e.message,
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        } else {
                                                            toolbar!!.subtitle = null
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                        } else if (status == "online") {
                                            toolbar!!.subtitle = "online"
                                            toolbar!!.setSubtitleTextColor(Color.GREEN)
                                        }
                                    } else {
                                        toolbar!!.subtitle = ""
                                        if (Colors.isDarkMode(this@OpenChat)) {
                                            toolbar!!.setSubtitleTextColor(getColor(android.R.color.secondary_text_dark))
                                        } else {
                                            toolbar!!.setSubtitleTextColor(getColor(android.R.color.secondary_text_light))
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
        bg
        try {
            getChats(chat.extras!!["username"].toString())
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        val sendButton = findViewById<FloatingActionButton>(R.id.send2)
        sendButton.setOnClickListener { v: View? -> sendMessage() }
        if (Colors.isDarkMode(this@OpenChat)) {
            sendButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0F6185"))
        } else {
            sendButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#26C6DA"))
        }
        setFabColor(this, sendButton)
        val messageET = findViewById<EditText>(R.id.messageText)
        // preferences!!.getBoolean("enterToSendSwitch", false)
        if (db!!.getSettingBoolean("enterIsSend", false)) {
            messageET.imeOptions = EditorInfo.IME_ACTION_SEND
            messageET.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
            messageET.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage()
                    return@setOnEditorActionListener true
                }
                false
            }
        }
        val scroll1 = findViewById<ScrollView>(R.id.scroll)
        setEventListener(this, KeyboardVisibilityEventListener { isOpen: Boolean ->
            if (isOpen && messageET.hasFocus()) {
                //scroll to last view
                val lastChild = scroll1.getChildAt(scroll1.childCount - 1)
                val bottom = lastChild.bottom + scroll1.paddingBottom
                val sy = scroll1.scrollY
                val sh = scroll1.height
                val delta = bottom - (sy + sh)
                scroll1.scrollBy(0, delta)
            }
        })
        val messageInput = findViewById<ConstraintLayout>(R.id.chatBox)
        if (Colors.isDarkMode(this@OpenChat)) {
            messageInput.background = ContextCompat.getDrawable(this, R.drawable.chat_input_dark)
        } else {
            messageInput.background = ContextCompat.getDrawable(this, R.drawable.chat_input_light)
        }
        setMessageEtColor(messageInput)

        // Update Scroll-Button
        val updateStatus: Runnable = object : Runnable {
            override fun run() {
                val scroll1 = findViewById<ScrollView>(R.id.scroll)
                val button = findViewById<FloatingActionButton>(R.id.scrollDownButton)
                button.setOnClickListener { v: View? -> scroll1.fullScroll(View.FOCUS_DOWN) }
                setFabColor(this@OpenChat, button)
                if (scroll1.getChildAt(0).bottom <= scroll1.height + scroll1.scrollY) {
                    button.visibility = View.GONE
                } else {
                    button.visibility = View.VISIBLE
                }
                val replyBox = findViewById<ConstraintLayout>(R.id.expertReplyBox)
                val input = findViewById<EditText>(R.id.messageText)
                val send = findViewById<FloatingActionButton>(R.id.send2)
                val blockedText = findViewById<TextView>(R.id.chatBlockedText)
                if (blocked == true) {
                    replying = false
                    replyBox.visibility = View.GONE
                    input.visibility = View.GONE
                    send.visibility = View.GONE
                    blockedText.visibility = View.VISIBLE
                    hideKeyboard(this@OpenChat)
                    blockedText.setOnClickListener { v: View? ->
                        AlertDialog.Builder(this@OpenChat)
                            .setMessage("Benutzer freigeben?")
                            .setPositiveButton("Freigeben") { dialog: DialogInterface?, which: Int -> unblockUser() }
                            .setNegativeButton("Abbrechen", null)
                            .show()
                    }
                } else if (otherBlocked == true) {
                    replying = false
                    replyBox.visibility = View.GONE
                    input.visibility = View.GONE
                    send.visibility = View.GONE
                    blockedText.visibility = View.VISIBLE
                    blockedText.text = "Du kannst diesem Benutzer keine Nachrichten mehr senden."
                    hideKeyboard(this@OpenChat)
                } else {
                    input.visibility = View.VISIBLE
                    send.visibility = View.VISIBLE
                    blockedText.visibility = View.GONE
                }
                hdlr.postDelayed(this, 1)
            }
        }
        updateStatus.run()

        val replyBox = findViewById<ConstraintLayout>(R.id.expertReplyBox)
        replyBox.visibility = View.GONE
    }

    fun setMessageReplyBubbleColor(replyBubble: RelativeLayout?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences!!.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
                val colorFile: File
                colorFile = if (Colors.isDarkMode(this@OpenChat)) {
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
                val `object` = JSONObject(jsn.opt(11).toString())
                if (`object`.opt("messageReplyBubbleColor").toString().toInt() != 0) {
                    replyBubble!!.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("messageReplyBubbleColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setMessageReplyColor(constraintLayout: ConstraintLayout) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences!!.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
                val colorFile: File
                colorFile = if (Colors.isDarkMode(this@OpenChat)) {
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
                val `object` = JSONObject(jsn.opt(10).toString())
                if (`object`.opt("messageReplyColor").toString().toInt() != 0) {
                    constraintLayout.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("messageReplyColor").toString().toInt())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setMessageColor(frameLayout: FrameLayout) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences!!.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
                val colorFile: File
                colorFile = if (Colors.isDarkMode(this@OpenChat)) {
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
                val `object` = JSONObject(jsn.opt(7).toString())
                if (`object`.opt("messageBubbleColor").toString().toInt() != 0) {
                    frameLayout.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("messageBubbleColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setConstraintMessageColor(constraintLayout: ConstraintLayout?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences!!.getBoolean("useAccentColors", false) == true) {
            try {
                var json: String? = null
                val colorFile: File
                colorFile = if (Colors.isDarkMode(this@OpenChat)) {
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
                val `object` = JSONObject(jsn.opt(7).toString())
                if (`object`.opt("messageBubbleColor").toString().toInt() != 0) {
                    constraintLayout!!.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("messageBubbleColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setMessageEtColor(messageInput: ConstraintLayout) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences!!.getBoolean("useAccentColors", false)) {
            try {
                var json: String? = null
                val colorFile: File
                colorFile = if (Colors.isDarkMode(this@OpenChat)) {
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
                val `object` = JSONObject(jsn.opt(9).toString())
                if (`object`.opt("messageEditTextColor").toString().toInt() != 0) {
                    messageInput.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("messageEditTextColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun getChats(otherUsername: String) {
        val scroll1 = findViewById<ScrollView>(R.id.scroll)
        val button = findViewById<FloatingActionButton>(R.id.scrollDownButton)
        button.setOnClickListener { v: View? -> scroll1.fullScroll(View.FOCUS_DOWN) }
        setFabColor(this@OpenChat, button)
        scroll1.viewTreeObserver
            .addOnScrollChangedListener {
                if (scroll1.getChildAt(0).bottom <= scroll1.height + scroll1.scrollY) {
                    //scroll view is at bottom
                    //set the button visibility to visible here
                    button.visibility = View.GONE
                } else {
                    button.visibility = View.VISIBLE
                    //scroll view is not at bottom
                    //set the button visibility to gone here
                }
            }
        val oldDate = arrayOf("")
        val layout = findViewById<LinearLayout>(R.id.messagesList)
        childEventListener = object : ChildEventListener {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    if (snapshot.child("message").exists() && snapshot.child("from").exists()) {
                        val messages = snapshot.value as Map<*, *>?
                        val date = messages!!["date"].toString()
                        val fromUsername = messages["from"].toString()
                        val messageText = messages["message"].toString()
                        val time = messages["time"].toString()
                        val key = snapshot.key
                        var status = ""
                        if (snapshot.child("type").exists()) {
                            val type = snapshot.child("type").value.toString()
                            when (type) {
                                "text" -> {
                                    status = snapshot.child("status").value.toString()
                                    if (fromUsername == myUsername) {
                                        //show read-status
                                        val dateView: View = LayoutInflater.from(this@OpenChat)
                                            .inflate(R.layout.date_message_category, null, false)
                                        val dateHolder: DateHolder = DateHolder()
                                        dateHolder.dateTextView =
                                            dateView.findViewById(R.id.dateTextView)
                                        dateView.tag = dateHolder
                                        dateHolder.dateTextView!!.text = date
                                        if (Colors.isDarkMode(this@OpenChat)) {
                                            dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_dark)
                                        } else {
                                            dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_light)
                                        }
                                        setDateMessageBubbleColor(
                                            this@OpenChat,
                                            dateHolder.dateTextView!!
                                        )
                                        val v = arrayOfNulls<View>(1)
                                        if (fromUsername == myUsername) {
                                            if (snapshot.child("reply").exists()) {
                                                // sending replying message with status
                                                v[0] = LayoutInflater.from(this@OpenChat).inflate(
                                                    R.layout.chat_reply_user_2,
                                                    null,
                                                    false
                                                )
                                                val replyKey =
                                                    snapshot.child("reply").value.toString()
                                                val finalStatus = status
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$replyKey")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if (snapshot.exists()) {
                                                                val replyFrom =
                                                                    snapshot.child("from").value.toString()
                                                                val replyMessage =
                                                                    snapshot.child("message").value.toString()
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                holder.statusTextView =
                                                                    v[0]!!.findViewById(R.id.textview_status)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                if (replyFrom == myUsername) {
                                                                    holder.replyFrom!!.setText(R.string.you)
                                                                } else {
                                                                    holder.replyFrom!!.text =
                                                                        replyFrom
                                                                }
                                                                holder.replyMessage!!.text =
                                                                    replyMessage
                                                                val messageData1 = snapshot.key
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1/status")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            val status =
                                                                                                snapshot.value.toString()
                                                                                            if (status == "SENT") {
                                                                                                holder.statusTextView!!.setText(
                                                                                                    R.string.message_delivered
                                                                                                )
                                                                                            } else if (status == "READ") {
                                                                                                holder.statusTextView!!.setText(
                                                                                                    R.string.message_read
                                                                                                )
                                                                                            }
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                            Toast.makeText(
                                                                                this@OpenChat,
                                                                                error.message,
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    })
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                                if (finalStatus == "SENT") {
                                                                    holder.statusTextView!!.setText(
                                                                        R.string.message_delivered
                                                                    )
                                                                } else if (finalStatus == "READ") {
                                                                    holder.statusTextView!!.setText(
                                                                        R.string.message_read
                                                                    )
                                                                }

                                                                //Context-Menu
                                                                v[0]!!.setOnClickListener { v1: View? ->
                                                                    if (v1 != null) {
                                                                        MyBottomSheetDialogFragment(
                                                                            fromUsername,
                                                                            v1
                                                                        ).apply {
                                                                            show(
                                                                                supportFragmentManager,
                                                                                tag
                                                                            )
                                                                        }
                                                                    }

                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_me,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.deleteMessage -> {
                                                                                val newView: View
                                                                                newView =
                                                                                    LayoutInflater.from(
                                                                                        this@OpenChat
                                                                                    ).inflate(
                                                                                        R.layout.chat_reply_user_2,
                                                                                        null,
                                                                                        false
                                                                                    )
                                                                                val holder2: ReplyMessageHolder =
                                                                                    ReplyMessageHolder()
                                                                                holder2.messageTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_message
                                                                                    )
                                                                                holder2.timeTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_time
                                                                                    )
                                                                                holder2.bubble =
                                                                                    newView.findViewById(
                                                                                        R.id.incoming_layout_bubble
                                                                                    )
                                                                                holder2.replyFrom =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyFrom
                                                                                    )
                                                                                holder2.replyMessage =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyMessage
                                                                                    )
                                                                                holder2.layoutBox =
                                                                                    newView.findViewById(
                                                                                        R.id.incomming_layout_box
                                                                                    )
                                                                                holder2.replyBox =
                                                                                    newView.findViewById(
                                                                                        R.id.replyBox
                                                                                    )
                                                                                if (Colors.isDarkMode(
                                                                                        this@OpenChat
                                                                                    )
                                                                                ) {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                } else {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                }
                                                                                setMessageReplyBubbleColor(
                                                                                    holder2.replyBox
                                                                                )
                                                                                setConstraintMessageColor(
                                                                                    holder2.layoutBox
                                                                                )
                                                                                newView.tag =
                                                                                    holder2
                                                                                holder2.messageTextView!!.text =
                                                                                    messageText
                                                                                holder2.timeTextView!!.text =
                                                                                    time
                                                                                if (replyFrom == myUsername) {
                                                                                    holder2.replyFrom!!.setText(
                                                                                        R.string.you
                                                                                    )
                                                                                } else {
                                                                                    holder2.replyFrom!!.text =
                                                                                        replyFrom
                                                                                }
                                                                                holder2.replyMessage!!.text =
                                                                                    replyMessage
                                                                                val builder =
                                                                                    AlertDialog.Builder(
                                                                                        this@OpenChat
                                                                                    )
                                                                                builder.setTitle(R.string.contextMenu_messageDelete)
                                                                                    .setView(newView)
                                                                                    .setPositiveButton(
                                                                                        R.string.deleteMessage_all
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                database!!.child(
                                                                                                                    "users/$otherUsername/chats/$myUsername/messages"
                                                                                                                )
                                                                                                                    .addListenerForSingleValueEvent(
                                                                                                                        object :
                                                                                                                            ValueEventListener {
                                                                                                                            override fun onDataChange(
                                                                                                                                snapshot141: DataSnapshot
                                                                                                                            ) {
                                                                                                                                if (snapshot141.exists()) {
                                                                                                                                    snapshot141.child(
                                                                                                                                        key
                                                                                                                                    ).ref.removeValue()
                                                                                                                                        .addOnSuccessListener { aVoid12: Void? ->
                                                                                                                                            layout.removeView(
                                                                                                                                                v[0]
                                                                                                                                            )
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                R.string.deleteMessage_success,
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                        }
                                                                                                                                        .addOnFailureListener { e: Exception ->
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                getString(
                                                                                                                                                    R.string.deleteMessage_failure
                                                                                                                                                ),
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                            Log.e(
                                                                                                                                                "MESSAGE_DELETE_ERROR",
                                                                                                                                                e.message!!
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
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    e.message,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNegativeButton(
                                                                                        R.string.deleteMessage_me
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                layout.removeView(
                                                                                                                    v[0]
                                                                                                                )
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_success,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_failure,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                                Log.e(
                                                                                                                    MESSAGE_DELETE_ERROR,
                                                                                                                    e.message!!
                                                                                                                )
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNeutralButton(
                                                                                        android.R.string.cancel,
                                                                                        null
                                                                                    )
                                                                                    .show()
                                                                            }

                                                                            R.id.editMessage -> editMessage(
                                                                                messageText,
                                                                                key,
                                                                                holder.messageTextView
                                                                            )

                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                }
                                                            } else {
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                holder.statusTextView =
                                                                    v[0]!!.findViewById(R.id.textview_status)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                holder.replyFrom!!.setText(R.string.replyMessage_deletedTitle)
                                                                holder.replyMessage!!.setText(R.string.replyMessage_deletedSummary)
                                                                val messageData1 = snapshot.key
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1/status")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            val status =
                                                                                                snapshot.value.toString()
                                                                                            if (status == "SENT") {
                                                                                                holder.statusTextView!!.setText(
                                                                                                    R.string.message_delivered
                                                                                                )
                                                                                            } else if (status == "READ") {
                                                                                                holder.statusTextView!!.setText(
                                                                                                    R.string.message_read
                                                                                                )
                                                                                            }
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                            Toast.makeText(
                                                                                this@OpenChat,
                                                                                error.message,
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    })
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                                if (finalStatus == "SENT") {
                                                                    holder.statusTextView!!.setText(
                                                                        R.string.message_delivered
                                                                    )
                                                                } else if (finalStatus == "READ") {
                                                                    holder.statusTextView!!.setText(
                                                                        R.string.message_read
                                                                    )
                                                                }

                                                                //Context-Menu
                                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_me,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.deleteMessage -> {
                                                                                val newView: View
                                                                                newView =
                                                                                    LayoutInflater.from(
                                                                                        this@OpenChat
                                                                                    ).inflate(
                                                                                        R.layout.chat_reply_user_2,
                                                                                        null,
                                                                                        false
                                                                                    )
                                                                                val holder2: ReplyMessageHolder =
                                                                                    ReplyMessageHolder()
                                                                                holder2.messageTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_message
                                                                                    )
                                                                                holder2.timeTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_time
                                                                                    )
                                                                                holder2.bubble =
                                                                                    newView.findViewById(
                                                                                        R.id.incoming_layout_bubble
                                                                                    )
                                                                                holder2.replyFrom =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyFrom
                                                                                    )
                                                                                holder2.replyMessage =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyMessage
                                                                                    )
                                                                                holder2.layoutBox =
                                                                                    newView.findViewById(
                                                                                        R.id.incomming_layout_box
                                                                                    )
                                                                                holder2.replyBox =
                                                                                    newView.findViewById(
                                                                                        R.id.replyBox
                                                                                    )
                                                                                if (Colors.isDarkMode(
                                                                                        this@OpenChat
                                                                                    )
                                                                                ) {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                } else {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                }
                                                                                setMessageReplyBubbleColor(
                                                                                    holder2.replyBox
                                                                                )
                                                                                setConstraintMessageColor(
                                                                                    holder2.layoutBox
                                                                                )
                                                                                newView.tag =
                                                                                    holder2
                                                                                holder2.messageTextView!!.text =
                                                                                    messageText
                                                                                holder2.timeTextView!!.text =
                                                                                    time
                                                                                holder2.replyFrom!!.setText(
                                                                                    R.string.replyMessage_deletedTitle
                                                                                )
                                                                                holder2.replyMessage!!.setText(
                                                                                    R.string.replyMessage_deletedTitle
                                                                                )
                                                                                val builder =
                                                                                    AlertDialog.Builder(
                                                                                        this@OpenChat
                                                                                    )
                                                                                builder.setTitle(R.string.contextMenu_messageDelete)
                                                                                    .setView(newView)
                                                                                    .setPositiveButton(
                                                                                        R.string.deleteMessage_all
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                database!!.child(
                                                                                                                    "users/$otherUsername/chats/$myUsername/messages"
                                                                                                                )
                                                                                                                    .addListenerForSingleValueEvent(
                                                                                                                        object :
                                                                                                                            ValueEventListener {
                                                                                                                            override fun onDataChange(
                                                                                                                                snapshot141: DataSnapshot
                                                                                                                            ) {
                                                                                                                                if (snapshot141.exists()) {
                                                                                                                                    snapshot141.child(
                                                                                                                                        key
                                                                                                                                    ).ref.removeValue()
                                                                                                                                        .addOnSuccessListener { aVoid12: Void? ->
                                                                                                                                            layout.removeView(
                                                                                                                                                v[0]
                                                                                                                                            )
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                R.string.deleteMessage_success,
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                        }
                                                                                                                                        .addOnFailureListener { e: Exception ->
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                R.string.deleteMessage_failure,
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                            Log.e(
                                                                                                                                                MESSAGE_DELETE_ERROR,
                                                                                                                                                e.message!!
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
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    e.message,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNegativeButton(
                                                                                        R.string.deleteMessage_me
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                layout.removeView(
                                                                                                                    v[0]
                                                                                                                )
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_success,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_failure,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                                Log.e(
                                                                                                                    MESSAGE_DELETE_ERROR,
                                                                                                                    e.message!!
                                                                                                                )
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNeutralButton(
                                                                                        android.R.string.cancel,
                                                                                        null
                                                                                    )
                                                                                    .show()
                                                                            }

                                                                            R.id.editMessage -> editMessage(
                                                                                messageText,
                                                                                key,
                                                                                holder.messageTextView
                                                                            )

                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                })
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            } else {
                                                // sending no-reply message with status
                                                v[0] = LayoutInflater.from(this@OpenChat)
                                                    .inflate(R.layout.chat_user_2, null, false)
                                                val holder1 = StatusMessageHolder()
                                                holder1.messageTextView =
                                                    v[0]!!.findViewById(R.id.textview_message)
                                                holder1.timeTextView =
                                                    v[0]!!.findViewById(R.id.textview_time)
                                                holder1.bubble =
                                                    v[0]!!.findViewById(R.id.outgoing_layout_bubble)
                                                holder1.statusTextView =
                                                    v[0]!!.findViewById(R.id.textview_status)
                                                v[0]!!.tag = holder1
                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_dark
                                                    )
                                                } else {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_light
                                                    )
                                                }
                                                setConstraintMessageColor(holder1.bubble)
                                                holder1.messageTextView!!.text = messageText
                                                holder1.timeTextView!!.text = time
                                                val messageData1 = snapshot.key
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                    .addChildEventListener(object :
                                                        ChildEventListener {
                                                        override fun onChildAdded(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onChildChanged(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1/status")
                                                                .addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            val status =
                                                                                snapshot.value.toString()
                                                                            if (status == "SENT") {
                                                                                holder1.statusTextView!!.setText(
                                                                                    R.string.message_delivered
                                                                                )
                                                                            } else if (status == "READ") {
                                                                                holder1.statusTextView!!.setText(
                                                                                    R.string.message_read
                                                                                )
                                                                            }
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                        }

                                                        override fun onChildRemoved(snapshot: DataSnapshot) {}
                                                        override fun onChildMoved(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {
                                                            Toast.makeText(
                                                                this@OpenChat,
                                                                error.message,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    })
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                    .addChildEventListener(object :
                                                        ChildEventListener {
                                                        override fun onChildAdded(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onChildChanged(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                .addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            holder1.messageTextView!!.text =
                                                                                snapshot.child("message").value.toString()
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                        }

                                                        override fun onChildRemoved(snapshot: DataSnapshot) {
                                                            layout.removeView(v[0])
                                                        }

                                                        override fun onChildMoved(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                                if (status == "SENT") {
                                                    holder1.statusTextView!!.setText(R.string.message_delivered)
                                                } else if (status == "READ") {
                                                    holder1.statusTextView!!.setText(R.string.message_read)
                                                }

                                                //Context-Menu
                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                    // sending no-reply actions
                                                    val menu =
                                                        PopupMenu(this@OpenChat, v1, Gravity.CENTER)
                                                    menu.menuInflater.inflate(
                                                        R.menu.message_menu_me,
                                                        menu.menu
                                                    )
                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                        when (item.itemId) {
                                                            R.id.deleteMessage -> {
                                                                val newView: View
                                                                newView =
                                                                    LayoutInflater.from(this@OpenChat)
                                                                        .inflate(
                                                                            R.layout.chat_user_2,
                                                                            null,
                                                                            false
                                                                        )
                                                                val holder2: MessageHolder =
                                                                    MessageHolder()
                                                                holder2.messageTextView =
                                                                    newView.findViewById(R.id.textview_message)
                                                                holder2.timeTextView =
                                                                    newView.findViewById(R.id.textview_time)
                                                                holder2.bubble =
                                                                    newView.findViewById(R.id.outgoing_layout_bubble)
                                                                newView.tag = holder2
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder2.bubble!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                } else {
                                                                    holder2.bubble!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                }
                                                                setConstraintMessageColor(holder2.bubble)
                                                                holder2.messageTextView!!.text =
                                                                    messageText
                                                                holder2.timeTextView!!.text = time
                                                                val messageData = snapshot.key
                                                                val builder =
                                                                    AlertDialog.Builder(this@OpenChat)
                                                                builder.setTitle(R.string.contextMenu_messageDelete)
                                                                    .setView(newView)
                                                                    .setPositiveButton(R.string.deleteMessage_all) { dialog: DialogInterface?, which: Int ->
                                                                        database!!.child(
                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                        )
                                                                            .addListenerForSingleValueEvent(
                                                                                object :
                                                                                    ValueEventListener {
                                                                                    override fun onDataChange(
                                                                                        snapshot12: DataSnapshot
                                                                                    ) {
                                                                                        snapshot12.child(
                                                                                            messageData!!
                                                                                        ).ref.removeValue()
                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                database!!.child(
                                                                                                    "users/$otherUsername/chats/$myUsername/messages"
                                                                                                )
                                                                                                    .addListenerForSingleValueEvent(
                                                                                                        object :
                                                                                                            ValueEventListener {
                                                                                                            override fun onDataChange(
                                                                                                                snapshot1: DataSnapshot
                                                                                                            ) {
                                                                                                                if (snapshot1.exists()) {
                                                                                                                    snapshot1.child(
                                                                                                                        messageData
                                                                                                                    ).ref.removeValue()
                                                                                                                        .addOnSuccessListener { aVoid1: Void? ->
                                                                                                                            layout.removeView(
                                                                                                                                v[0]
                                                                                                                            )
                                                                                                                            Toast.makeText(
                                                                                                                                this@OpenChat,
                                                                                                                                R.string.deleteMessage_success,
                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                            )
                                                                                                                                .show()
                                                                                                                        }
                                                                                                                        .addOnFailureListener { e: Exception ->
                                                                                                                            Toast.makeText(
                                                                                                                                this@OpenChat,
                                                                                                                                R.string.deleteMessage_failure,
                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                            )
                                                                                                                                .show()
                                                                                                                            Log.e(
                                                                                                                                MESSAGE_DELETE_ERROR,
                                                                                                                                e.message!!
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
                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                Toast.makeText(
                                                                                                    this@OpenChat,
                                                                                                    e.message,
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
                                                                                            }
                                                                                    }

                                                                                    override fun onCancelled(
                                                                                        error: DatabaseError
                                                                                    ) {
                                                                                    }
                                                                                })
                                                                    }
                                                                    .setNegativeButton(R.string.deleteMessage_me) { dialog: DialogInterface?, which: Int ->
                                                                        database!!.child(
                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                        )
                                                                            .addListenerForSingleValueEvent(
                                                                                object :
                                                                                    ValueEventListener {
                                                                                    override fun onDataChange(
                                                                                        snapshot13: DataSnapshot
                                                                                    ) {
                                                                                        snapshot13.child(
                                                                                            messageData!!
                                                                                        ).ref.removeValue()
                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                layout.removeView(
                                                                                                    v[0]
                                                                                                )
                                                                                                Toast.makeText(
                                                                                                    this@OpenChat,
                                                                                                    R.string.deleteMessage_success,
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
                                                                                            }
                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                Toast.makeText(
                                                                                                    this@OpenChat,
                                                                                                    R.string.deleteMessage_failure,
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
                                                                                                Log.e(
                                                                                                    MESSAGE_DELETE_ERROR,
                                                                                                    e.message!!
                                                                                                )
                                                                                            }
                                                                                    }

                                                                                    override fun onCancelled(
                                                                                        error: DatabaseError
                                                                                    ) {
                                                                                    }
                                                                                })
                                                                    }
                                                                    .setNeutralButton(
                                                                        android.R.string.cancel,
                                                                        null
                                                                    )
                                                                    .show()
                                                            }

                                                            R.id.editMessage -> editMessage(
                                                                messageText,
                                                                key,
                                                                holder1.messageTextView
                                                            )

                                                            R.id.copyMessage -> {
                                                                val clipboard = getSystemService(
                                                                    CLIPBOARD_SERVICE
                                                                ) as ClipboardManager
                                                                val clip = ClipData.newPlainText(
                                                                    "message text",
                                                                    messageText
                                                                )
                                                                clipboard.setPrimaryClip(clip)
                                                                Toast.makeText(
                                                                    this@OpenChat,
                                                                    R.string.copyMessage_success,
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }

                                                            R.id.replyToMessage -> replyTo(
                                                                fromUsername,
                                                                messageText,
                                                                key
                                                            )
                                                        }
                                                        false
                                                    }
                                                    menu.show()
                                                })
                                            }
                                        } else {
                                            // incoming replying message
                                            val messageData1 = snapshot.key
                                            if (snapshot.child("reply").exists()) {
                                                v[0] = LayoutInflater.from(this@OpenChat).inflate(
                                                    R.layout.chat_reply_user_1,
                                                    null,
                                                    false
                                                )
                                                val replyKey =
                                                    snapshot.child("reply").value.toString()
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$replyKey")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if (snapshot.exists()) {
                                                                val replyFrom =
                                                                    snapshot.child("from").value.toString()
                                                                val replyMessage =
                                                                    snapshot.child("message").value.toString()
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                if (replyFrom == myUsername) {
                                                                    holder.replyFrom!!.setText(R.string.you)
                                                                } else {
                                                                    holder.replyFrom!!.text =
                                                                        replyFrom
                                                                }
                                                                holder.replyMessage!!.text =
                                                                    replyMessage
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_other,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                })
                                                            } else {
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                holder.replyFrom!!.setText(R.string.replyMessage_deletedTitle)
                                                                holder.replyMessage!!.setText(R.string.replyMessage_deletedSummary)
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_other,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                })
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            } else {
                                                // incoming no-reply message
                                                v[0] = LayoutInflater.from(this@OpenChat)
                                                    .inflate(R.layout.chat_user_1, null, false)
                                                val holder1: MessageHolder = MessageHolder()
                                                holder1.messageTextView =
                                                    v[0]!!.findViewById(R.id.textview_message)
                                                holder1.timeTextView =
                                                    v[0]!!.findViewById(R.id.textview_time)
                                                holder1.bubble =
                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_dark
                                                    )
                                                } else {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_light
                                                    )
                                                }
                                                setConstraintMessageColor(holder1.bubble)
                                                v[0]!!.tag = holder1
                                                holder1.messageTextView!!.text = messageText
                                                holder1.timeTextView!!.text = time
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                    .addChildEventListener(object :
                                                        ChildEventListener {
                                                        override fun onChildAdded(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onChildChanged(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                .addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            holder1.messageTextView!!.text =
                                                                                snapshot.child("message").value.toString()
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                        }

                                                        override fun onChildRemoved(snapshot: DataSnapshot) {
                                                            layout.removeView(v[0])
                                                        }

                                                        override fun onChildMoved(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                    val menu =
                                                        PopupMenu(this@OpenChat, v1, Gravity.CENTER)
                                                    menu.menuInflater.inflate(
                                                        R.menu.message_menu_other,
                                                        menu.menu
                                                    )
                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                        when (item.itemId) {
                                                            R.id.copyMessage -> {
                                                                val clipboard = getSystemService(
                                                                    CLIPBOARD_SERVICE
                                                                ) as ClipboardManager
                                                                val clip = ClipData.newPlainText(
                                                                    "message text",
                                                                    messageText
                                                                )
                                                                clipboard.setPrimaryClip(clip)
                                                                Toast.makeText(
                                                                    this@OpenChat,
                                                                    R.string.copyMessage_success,
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }

                                                            R.id.replyToMessage -> replyTo(
                                                                fromUsername,
                                                                messageText,
                                                                key
                                                            )
                                                        }
                                                        false
                                                    }
                                                    menu.show()
                                                })
                                            }
                                        }
                                        val scroll = findViewById<ScrollView>(R.id.scroll)
                                        //add View
                                        if (oldDate[0] != date) {
                                            oldDate[0] = date
                                            layout.addView(dateView)
                                            scroll.post { scroll.scrollBy(0, dateView.height) }
                                        }
                                        if (v[0] != null) {
                                            layout.addView(v[0])
                                            scroll.post {
                                                scroll.scrollBy(
                                                    0, v[0]!!
                                                        .height
                                                )
                                            }
                                        }
                                    } else {
                                        if (status == "SENT") {
                                            //set status to "READ"
                                            database!!.child("users/$myUsername/settings/markAsRead")
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (snapshot.exists()) {
                                                            val markAsRead =
                                                                snapshot.value.toString()
                                                            if (markAsRead == "true") {
                                                                database!!.child("users/$otherUsername/chats/$myUsername/messages/$key/status")
                                                                    .setValue("READ")
                                                                    .addOnFailureListener { e: Exception ->
                                                                        Toast.makeText(
                                                                            this@OpenChat,
                                                                            "Error: " + e.message,
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                            }
                                                        } else {
                                                            database!!.child("users/$otherUsername/chats/$myUsername/messages/$key/status")
                                                                .setValue("READ")
                                                                .addOnFailureListener { e: Exception ->
                                                                    Toast.makeText(
                                                                        this@OpenChat,
                                                                        "Error: " + e.message,
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$key/status")
                                                .setValue("READ")
                                                .addOnFailureListener { e: Exception ->
                                                    Toast.makeText(
                                                        this@OpenChat,
                                                        "Error: " + e.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        val dateView: View
                                        dateView = LayoutInflater.from(this@OpenChat)
                                            .inflate(R.layout.date_message_category, null, false)
                                        val dateHolder: DateHolder = DateHolder()
                                        dateHolder.dateTextView =
                                            dateView.findViewById(R.id.dateTextView)
                                        dateView.tag = dateHolder
                                        dateHolder.dateTextView!!.text = date
                                        if (Colors.isDarkMode(this@OpenChat)) {
                                            dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_dark)
                                        } else {
                                            dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_light)
                                        }
                                        setDateMessageBubbleColor(
                                            this@OpenChat,
                                            dateHolder.dateTextView!!
                                        )
                                        val v = arrayOfNulls<View>(1)
                                        if (fromUsername == myUsername) {
                                            val messageData1 = snapshot.key
                                            if (snapshot.child("reply").exists()) {
                                                // sending replying message with status
                                                v[0] = LayoutInflater.from(this@OpenChat).inflate(
                                                    R.layout.chat_reply_user_2,
                                                    null,
                                                    false
                                                )
                                                val replyKey =
                                                    snapshot.child("reply").value.toString()
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$replyKey")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if (snapshot.exists()) {
                                                                val replyFrom =
                                                                    snapshot.child("from").value.toString()
                                                                val replyMessage =
                                                                    snapshot.child("message").value.toString()
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                if (replyFrom == myUsername) {
                                                                    holder.replyFrom!!.setText(R.string.you)
                                                                } else {
                                                                    holder.replyFrom!!.text =
                                                                        replyFrom
                                                                }
                                                                holder.replyMessage!!.text =
                                                                    replyMessage
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })

                                                                //Context-Menu
                                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_me,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.deleteMessage -> {
                                                                                val newView: View
                                                                                newView =
                                                                                    LayoutInflater.from(
                                                                                        this@OpenChat
                                                                                    ).inflate(
                                                                                        R.layout.chat_reply_user_2,
                                                                                        null,
                                                                                        false
                                                                                    )
                                                                                val holder2: ReplyMessageHolder =
                                                                                    ReplyMessageHolder()
                                                                                holder2.messageTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_message
                                                                                    )
                                                                                holder2.timeTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_time
                                                                                    )
                                                                                holder2.bubble =
                                                                                    newView.findViewById(
                                                                                        R.id.incoming_layout_bubble
                                                                                    )
                                                                                holder2.replyFrom =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyFrom
                                                                                    )
                                                                                holder2.replyMessage =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyMessage
                                                                                    )
                                                                                holder2.layoutBox =
                                                                                    newView.findViewById(
                                                                                        R.id.incomming_layout_box
                                                                                    )
                                                                                holder2.replyBox =
                                                                                    newView.findViewById(
                                                                                        R.id.replyBox
                                                                                    )
                                                                                if (Colors.isDarkMode(
                                                                                        this@OpenChat
                                                                                    )
                                                                                ) {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                } else {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                }
                                                                                setMessageReplyBubbleColor(
                                                                                    holder2.replyBox
                                                                                )
                                                                                setConstraintMessageColor(
                                                                                    holder2.layoutBox
                                                                                )
                                                                                newView.tag =
                                                                                    holder2
                                                                                holder2.messageTextView!!.text =
                                                                                    messageText
                                                                                holder2.timeTextView!!.text =
                                                                                    time
                                                                                if (replyFrom == myUsername) {
                                                                                    holder2.replyFrom!!.setText(
                                                                                        R.string.you
                                                                                    )
                                                                                } else {
                                                                                    holder2.replyFrom!!.text =
                                                                                        replyFrom
                                                                                }
                                                                                holder2.replyMessage!!.text =
                                                                                    replyMessage
                                                                                val builder =
                                                                                    AlertDialog.Builder(
                                                                                        this@OpenChat
                                                                                    )
                                                                                builder.setTitle(R.string.contextMenu_messageDelete)
                                                                                    .setView(newView)
                                                                                    .setPositiveButton(
                                                                                        R.string.deleteMessage_all
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                database!!.child(
                                                                                                                    "users/$otherUsername/chats/$myUsername/messages"
                                                                                                                )
                                                                                                                    .addListenerForSingleValueEvent(
                                                                                                                        object :
                                                                                                                            ValueEventListener {
                                                                                                                            override fun onDataChange(
                                                                                                                                snapshot141: DataSnapshot
                                                                                                                            ) {
                                                                                                                                if (snapshot141.exists()) {
                                                                                                                                    snapshot141.child(
                                                                                                                                        key
                                                                                                                                    ).ref.removeValue()
                                                                                                                                        .addOnSuccessListener { aVoid12: Void? ->
                                                                                                                                            layout.removeView(
                                                                                                                                                v[0]
                                                                                                                                            )
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                R.string.deleteMessage_success,
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                        }
                                                                                                                                        .addOnFailureListener { e: Exception ->
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                R.string.deleteMessage_failure,
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                            Log.e(
                                                                                                                                                MESSAGE_DELETE_ERROR,
                                                                                                                                                e.message!!
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
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    e.message,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNegativeButton(
                                                                                        R.string.deleteMessage_me
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                layout.removeView(
                                                                                                                    v[0]
                                                                                                                )
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_success,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_failure,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                                Log.e(
                                                                                                                    MESSAGE_DELETE_ERROR,
                                                                                                                    e.message!!
                                                                                                                )
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNeutralButton(
                                                                                        android.R.string.cancel,
                                                                                        null
                                                                                    )
                                                                                    .show()
                                                                            }

                                                                            R.id.editMessage -> editMessage(
                                                                                messageText,
                                                                                key,
                                                                                holder.messageTextView
                                                                            )

                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                })
                                                            } else {
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                holder.replyFrom!!.setText(R.string.replyMessage_deletedTitle)
                                                                holder.replyMessage!!.setText(R.string.replyMessage_deletedSummary)
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })

                                                                //Context-Menu
                                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_me,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.deleteMessage -> {
                                                                                val newView: View
                                                                                newView =
                                                                                    LayoutInflater.from(
                                                                                        this@OpenChat
                                                                                    ).inflate(
                                                                                        R.layout.chat_reply_user_2,
                                                                                        null,
                                                                                        false
                                                                                    )
                                                                                val holder2: ReplyMessageHolder =
                                                                                    ReplyMessageHolder()
                                                                                holder2.messageTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_message
                                                                                    )
                                                                                holder2.timeTextView =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_time
                                                                                    )
                                                                                holder2.bubble =
                                                                                    newView.findViewById(
                                                                                        R.id.incoming_layout_bubble
                                                                                    )
                                                                                holder2.replyFrom =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyFrom
                                                                                    )
                                                                                holder2.replyMessage =
                                                                                    newView.findViewById(
                                                                                        R.id.textview_replyMessage
                                                                                    )
                                                                                holder2.layoutBox =
                                                                                    newView.findViewById(
                                                                                        R.id.incomming_layout_box
                                                                                    )
                                                                                holder2.replyBox =
                                                                                    newView.findViewById(
                                                                                        R.id.replyBox
                                                                                    )
                                                                                if (Colors.isDarkMode(
                                                                                        this@OpenChat
                                                                                    )
                                                                                ) {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                } else {
                                                                                    holder2.layoutBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_light
                                                                                        )
                                                                                    holder2.replyBox!!.background =
                                                                                        ContextCompat.getDrawable(
                                                                                            this@OpenChat,
                                                                                            R.drawable.message_design_dark
                                                                                        )
                                                                                }
                                                                                setMessageReplyBubbleColor(
                                                                                    holder2.replyBox
                                                                                )
                                                                                setConstraintMessageColor(
                                                                                    holder2.layoutBox
                                                                                )
                                                                                newView.tag =
                                                                                    holder2
                                                                                holder2.messageTextView!!.text =
                                                                                    messageText
                                                                                holder2.timeTextView!!.text =
                                                                                    time
                                                                                holder2.replyFrom!!.setText(
                                                                                    R.string.replyMessage_deletedTitle
                                                                                )
                                                                                holder2.replyMessage!!.setText(
                                                                                    R.string.replyMessage_deletedSummary
                                                                                )
                                                                                val builder =
                                                                                    AlertDialog.Builder(
                                                                                        this@OpenChat
                                                                                    )
                                                                                builder.setTitle(R.string.contextMenu_messageDelete)
                                                                                    .setView(newView)
                                                                                    .setPositiveButton(
                                                                                        R.string.deleteMessage_all
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                database!!.child(
                                                                                                                    "users/$otherUsername/chats/$myUsername/messages"
                                                                                                                )
                                                                                                                    .addListenerForSingleValueEvent(
                                                                                                                        object :
                                                                                                                            ValueEventListener {
                                                                                                                            override fun onDataChange(
                                                                                                                                snapshot141: DataSnapshot
                                                                                                                            ) {
                                                                                                                                if (snapshot141.exists()) {
                                                                                                                                    snapshot141.child(
                                                                                                                                        key
                                                                                                                                    ).ref.removeValue()
                                                                                                                                        .addOnSuccessListener { aVoid12: Void? ->
                                                                                                                                            layout.removeView(
                                                                                                                                                v[0]
                                                                                                                                            )
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                R.string.deleteMessage_success,
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                        }
                                                                                                                                        .addOnFailureListener { e: Exception ->
                                                                                                                                            Toast.makeText(
                                                                                                                                                this@OpenChat,
                                                                                                                                                R.string.deleteMessage_failure,
                                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                                            )
                                                                                                                                                .show()
                                                                                                                                            Log.e(
                                                                                                                                                MESSAGE_DELETE_ERROR,
                                                                                                                                                e.message!!
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
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    e.message,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNegativeButton(
                                                                                        R.string.deleteMessage_me
                                                                                    ) { dialog: DialogInterface?, which: Int ->
                                                                                        database!!.child(
                                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                                        )
                                                                                            .addListenerForSingleValueEvent(
                                                                                                object :
                                                                                                    ValueEventListener {
                                                                                                    override fun onDataChange(
                                                                                                        snapshot14: DataSnapshot
                                                                                                    ) {
                                                                                                        snapshot14.child(
                                                                                                            key!!
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                                layout.removeView(
                                                                                                                    v[0]
                                                                                                                )
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_success,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_failure,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                                Log.e(
                                                                                                                    MESSAGE_DELETE_ERROR,
                                                                                                                    e.message!!
                                                                                                                )
                                                                                                            }
                                                                                                    }

                                                                                                    override fun onCancelled(
                                                                                                        error: DatabaseError
                                                                                                    ) {
                                                                                                    }
                                                                                                })
                                                                                    }
                                                                                    .setNeutralButton(
                                                                                        android.R.string.cancel,
                                                                                        null
                                                                                    )
                                                                                    .show()
                                                                            }

                                                                            R.id.editMessage -> editMessage(
                                                                                messageText,
                                                                                key,
                                                                                holder.messageTextView
                                                                            )

                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                })
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            } else {
                                                // sending no-reply message with status
                                                v[0] = LayoutInflater.from(this@OpenChat)
                                                    .inflate(R.layout.chat_user_2, null, false)
                                                val holder1 = StatusMessageHolder()
                                                holder1.messageTextView =
                                                    v[0]!!.findViewById(R.id.textview_message)
                                                holder1.timeTextView =
                                                    v[0]!!.findViewById(R.id.textview_time)
                                                holder1.bubble =
                                                    v[0]!!.findViewById(R.id.outgoing_layout_bubble)
                                                holder1.statusTextView =
                                                    v[0]!!.findViewById(R.id.textview_status)
                                                v[0]!!.tag = holder1
                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_dark
                                                    )
                                                } else {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_light
                                                    )
                                                }
                                                setConstraintMessageColor(holder1.bubble)
                                                holder1.messageTextView!!.text = messageText
                                                holder1.timeTextView!!.text = time
                                                if (status == "SENT") {
                                                    holder1.statusTextView!!.setText(R.string.message_read)
                                                } else if (status == "READ") {
                                                    holder1.statusTextView!!.setText(R.string.message_delivered)
                                                }
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                    .addChildEventListener(object :
                                                        ChildEventListener {
                                                        override fun onChildAdded(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onChildChanged(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                .addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            holder1.messageTextView!!.text =
                                                                                snapshot.child("message").value.toString()
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                        }

                                                        override fun onChildRemoved(snapshot: DataSnapshot) {
                                                            layout.removeView(v[0])
                                                        }

                                                        override fun onChildMoved(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })

                                                //Context-Menu
                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                    // sending no-reply actions
                                                    val menu =
                                                        PopupMenu(this@OpenChat, v1, Gravity.CENTER)
                                                    menu.menuInflater.inflate(
                                                        R.menu.message_menu_me,
                                                        menu.menu
                                                    )
                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                        when (item.itemId) {
                                                            R.id.deleteMessage -> {
                                                                val newView: View
                                                                newView =
                                                                    LayoutInflater.from(this@OpenChat)
                                                                        .inflate(
                                                                            R.layout.chat_reply_user_2,
                                                                            null,
                                                                            false
                                                                        )
                                                                val holder2: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder2.messageTextView =
                                                                    newView.findViewById(R.id.textview_message)
                                                                holder2.timeTextView =
                                                                    newView.findViewById(R.id.textview_time)
                                                                holder2.bubble =
                                                                    newView.findViewById(R.id.incoming_layout_bubble)
                                                                holder2.replyFrom =
                                                                    newView.findViewById(R.id.textview_replyFrom)
                                                                holder2.replyMessage =
                                                                    newView.findViewById(R.id.textview_replyMessage)
                                                                holder2.layoutBox =
                                                                    newView.findViewById(R.id.incomming_layout_box)
                                                                holder2.replyBox =
                                                                    newView.findViewById(R.id.replyBox)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder2.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder2.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder2.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder2.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder2.replyBox)
                                                                setConstraintMessageColor(holder2.layoutBox)
                                                                newView.tag = holder2
                                                                holder2.messageTextView!!.text =
                                                                    messageText
                                                                holder2.timeTextView!!.text = time
                                                                holder2.replyFrom!!.setText(R.string.replyMessage_deletedTitle)
                                                                holder2.replyMessage!!.setText(R.string.replyMessage_deletedSummary)
                                                                val builder =
                                                                    AlertDialog.Builder(this@OpenChat)
                                                                builder.setTitle(R.string.contextMenu_messageDelete)
                                                                    .setView(newView)
                                                                    .setPositiveButton(R.string.deleteMessage_all) { dialog: DialogInterface?, which: Int ->
                                                                        database!!.child(
                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                        )
                                                                            .addListenerForSingleValueEvent(
                                                                                object :
                                                                                    ValueEventListener {
                                                                                    override fun onDataChange(
                                                                                        snapshot14: DataSnapshot
                                                                                    ) {
                                                                                        snapshot14.child(
                                                                                            key!!
                                                                                        ).ref.removeValue()
                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                database!!.child(
                                                                                                    "users/$otherUsername/chats/$myUsername/messages"
                                                                                                )
                                                                                                    .addListenerForSingleValueEvent(
                                                                                                        object :
                                                                                                            ValueEventListener {
                                                                                                            override fun onDataChange(
                                                                                                                snapshot141: DataSnapshot
                                                                                                            ) {
                                                                                                                if (snapshot141.exists()) {
                                                                                                                    snapshot141.child(
                                                                                                                        key
                                                                                                                    ).ref.removeValue()
                                                                                                                        .addOnSuccessListener { aVoid12: Void? ->
                                                                                                                            layout.removeView(
                                                                                                                                v[0]
                                                                                                                            )
                                                                                                                            Toast.makeText(
                                                                                                                                this@OpenChat,
                                                                                                                                R.string.deleteMessage_success,
                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                            )
                                                                                                                                .show()
                                                                                                                        }
                                                                                                                        .addOnFailureListener { e: Exception ->
                                                                                                                            Toast.makeText(
                                                                                                                                this@OpenChat,
                                                                                                                                R.string.deleteMessage_failure,
                                                                                                                                Toast.LENGTH_SHORT
                                                                                                                            )
                                                                                                                                .show()
                                                                                                                            Log.e(
                                                                                                                                MESSAGE_DELETE_ERROR,
                                                                                                                                e.message!!
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
                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                Toast.makeText(
                                                                                                    this@OpenChat,
                                                                                                    e.message,
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
                                                                                            }
                                                                                    }

                                                                                    override fun onCancelled(
                                                                                        error: DatabaseError
                                                                                    ) {
                                                                                    }
                                                                                })
                                                                    }
                                                                    .setNegativeButton(R.string.deleteMessage_me) { dialog: DialogInterface?, which: Int ->
                                                                        database!!.child(
                                                                            "users/$myUsername/chats/$otherUsername/messages"
                                                                        )
                                                                            .addListenerForSingleValueEvent(
                                                                                object :
                                                                                    ValueEventListener {
                                                                                    override fun onDataChange(
                                                                                        snapshot14: DataSnapshot
                                                                                    ) {
                                                                                        snapshot14.child(
                                                                                            key!!
                                                                                        ).ref.removeValue()
                                                                                            .addOnSuccessListener { aVoid: Void? ->
                                                                                                layout.removeView(
                                                                                                    v[0]
                                                                                                )
                                                                                                Toast.makeText(
                                                                                                    this@OpenChat,
                                                                                                    R.string.deleteMessage_success,
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
                                                                                            }
                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                Toast.makeText(
                                                                                                    this@OpenChat,
                                                                                                    R.string.deleteMessage_failure,
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
                                                                                                Log.e(
                                                                                                    MESSAGE_DELETE_ERROR,
                                                                                                    e.message!!
                                                                                                )
                                                                                            }
                                                                                    }

                                                                                    override fun onCancelled(
                                                                                        error: DatabaseError
                                                                                    ) {
                                                                                    }
                                                                                })
                                                                    }
                                                                    .setNeutralButton(
                                                                        android.R.string.cancel,
                                                                        null
                                                                    )
                                                                    .show()
                                                            }

                                                            R.id.copyMessage -> {
                                                                val clipboard = getSystemService(
                                                                    CLIPBOARD_SERVICE
                                                                ) as ClipboardManager
                                                                val clip = ClipData.newPlainText(
                                                                    "message text",
                                                                    messageText
                                                                )
                                                                clipboard.setPrimaryClip(clip)
                                                                Toast.makeText(
                                                                    this@OpenChat,
                                                                    R.string.copyMessage_success,
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }

                                                            R.id.replyToMessage -> replyTo(
                                                                fromUsername,
                                                                messageText,
                                                                key
                                                            )
                                                        }
                                                        false
                                                    }
                                                    menu.show()
                                                })
                                            }
                                        } else {
                                            // incoming replying message
                                            val messageData1 = snapshot.key
                                            if (snapshot.child("reply").exists()) {
                                                v[0] = LayoutInflater.from(this@OpenChat).inflate(
                                                    R.layout.chat_reply_user_1,
                                                    null,
                                                    false
                                                )
                                                val replyKey =
                                                    snapshot.child("reply").value.toString()
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$replyKey")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if (snapshot.exists()) {
                                                                val replyFrom =
                                                                    snapshot.child("from").value.toString()
                                                                val replyMessage =
                                                                    snapshot.child("message").value.toString()
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                if (replyFrom == myUsername) {
                                                                    holder.replyFrom!!.setText(R.string.you)
                                                                } else {
                                                                    holder.replyFrom!!.text =
                                                                        replyFrom
                                                                }
                                                                holder.replyMessage!!.text =
                                                                    replyMessage
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_other,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                })
                                                            } else {
                                                                val holder: ReplyMessageHolder =
                                                                    ReplyMessageHolder()
                                                                holder.messageTextView =
                                                                    v[0]!!.findViewById(R.id.textview_message)
                                                                holder.timeTextView =
                                                                    v[0]!!.findViewById(R.id.textview_time)
                                                                holder.bubble =
                                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                                holder.replyFrom =
                                                                    v[0]!!.findViewById(R.id.textview_replyFrom)
                                                                holder.replyMessage =
                                                                    v[0]!!.findViewById(R.id.textview_replyMessage)
                                                                holder.layoutBox =
                                                                    v[0]!!.findViewById(R.id.incomming_layout_box)
                                                                holder.replyBox =
                                                                    v[0]!!.findViewById(R.id.replyBox)
                                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                } else {
                                                                    holder.layoutBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_light
                                                                        )
                                                                    holder.replyBox!!.background =
                                                                        ContextCompat.getDrawable(
                                                                            this@OpenChat,
                                                                            R.drawable.message_design_dark
                                                                        )
                                                                }
                                                                setMessageReplyBubbleColor(holder.replyBox)
                                                                setConstraintMessageColor(holder.layoutBox)
                                                                v[0]!!.tag = holder
                                                                holder.messageTextView!!.text =
                                                                    messageText
                                                                holder.timeTextView!!.text = time
                                                                holder.replyFrom!!.setText(R.string.replyMessage_deletedTitle)
                                                                holder.replyMessage!!.setText(R.string.replyMessage_deletedSummary)
                                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                    .addChildEventListener(object :
                                                                        ChildEventListener {
                                                                        override fun onChildAdded(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onChildChanged(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            holder.messageTextView!!.text =
                                                                                                snapshot.child(
                                                                                                    "message"
                                                                                                ).value.toString()
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                        }
                                                                                    })
                                                                        }

                                                                        override fun onChildRemoved(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            layout.removeView(v[0])
                                                                        }

                                                                        override fun onChildMoved(
                                                                            snapshot: DataSnapshot,
                                                                            previousChildName: String?
                                                                        ) {
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                                    val menu = PopupMenu(
                                                                        this@OpenChat,
                                                                        v1,
                                                                        Gravity.CENTER
                                                                    )
                                                                    menu.menuInflater.inflate(
                                                                        R.menu.message_menu_other,
                                                                        menu.menu
                                                                    )
                                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                                        when (item.itemId) {
                                                                            R.id.copyMessage -> {
                                                                                val clipboard =
                                                                                    getSystemService(
                                                                                        CLIPBOARD_SERVICE
                                                                                    ) as ClipboardManager
                                                                                val clip =
                                                                                    ClipData.newPlainText(
                                                                                        "message text",
                                                                                        messageText
                                                                                    )
                                                                                clipboard.setPrimaryClip(
                                                                                    clip
                                                                                )
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.copyMessage_success,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }

                                                                            R.id.replyToMessage -> replyTo(
                                                                                fromUsername,
                                                                                messageText,
                                                                                key
                                                                            )
                                                                        }
                                                                        false
                                                                    }
                                                                    menu.show()
                                                                })
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            } else {
                                                // incoming no-reply message
                                                v[0] = LayoutInflater.from(this@OpenChat)
                                                    .inflate(R.layout.chat_user_1, null, false)
                                                val holder1: MessageHolder = MessageHolder()
                                                holder1.messageTextView =
                                                    v[0]!!.findViewById(R.id.textview_message)
                                                holder1.timeTextView =
                                                    v[0]!!.findViewById(R.id.textview_time)
                                                holder1.bubble =
                                                    v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                                if (Colors.isDarkMode(this@OpenChat)) {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_dark
                                                    )
                                                } else {
                                                    holder1.bubble!!.background = ContextCompat.getDrawable(
                                                        this@OpenChat,
                                                        R.drawable.message_design_light
                                                    )
                                                }
                                                setConstraintMessageColor(holder1.bubble)
                                                v[0]!!.tag = holder1
                                                holder1.messageTextView!!.text = messageText
                                                holder1.timeTextView!!.text = time
                                                database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                    .addChildEventListener(object :
                                                        ChildEventListener {
                                                        override fun onChildAdded(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onChildChanged(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                                                .addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            holder1.messageTextView!!.text =
                                                                                snapshot.child("message").value.toString()
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                        }

                                                        override fun onChildRemoved(snapshot: DataSnapshot) {
                                                            layout.removeView(v[0])
                                                        }

                                                        override fun onChildMoved(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                                v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                                    val menu =
                                                        PopupMenu(this@OpenChat, v1, Gravity.CENTER)
                                                    menu.menuInflater.inflate(
                                                        R.menu.message_menu_other,
                                                        menu.menu
                                                    )
                                                    menu.setOnMenuItemClickListener { item: MenuItem ->
                                                        when (item.itemId) {
                                                            R.id.copyMessage -> {
                                                                val clipboard = getSystemService(
                                                                    CLIPBOARD_SERVICE
                                                                ) as ClipboardManager
                                                                val clip = ClipData.newPlainText(
                                                                    "message text",
                                                                    messageText
                                                                )
                                                                clipboard.setPrimaryClip(clip)
                                                                Toast.makeText(
                                                                    this@OpenChat,
                                                                    R.string.copyMessage_success,
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }

                                                            R.id.replyToMessage -> replyTo(
                                                                fromUsername,
                                                                messageText,
                                                                key
                                                            )
                                                        }
                                                        false
                                                    }
                                                    menu.show()
                                                })
                                            }
                                        }
                                        val scroll = findViewById<ScrollView>(R.id.scroll)
                                        //add View
                                        if (oldDate[0] != date) {
                                            oldDate[0] = date
                                            layout.addView(dateView)
                                            scroll.post { scroll.scrollBy(0, dateView.height) }
                                        }
                                        if (v[0] != null) {
                                            layout.addView(v[0])
                                            scroll.post {
                                                scroll.scrollBy(
                                                    0, v[0]!!
                                                        .height
                                                )
                                            }
                                        }
                                    }
                                }

                                "groupInvite" -> {
                                    val dateView: View
                                    dateView = LayoutInflater.from(this@OpenChat)
                                        .inflate(R.layout.date_message_category, null, false)
                                    val dateHolder: DateHolder = DateHolder()
                                    dateHolder.dateTextView =
                                        dateView.findViewById(R.id.dateTextView)
                                    dateView.tag = dateHolder
                                    dateHolder.dateTextView!!.text = date
                                    if (Colors.isDarkMode(this@OpenChat)) {
                                        dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_dark)
                                    } else {
                                        dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_light)
                                    }
                                    setDateMessageBubbleColor(
                                        this@OpenChat,
                                        dateHolder.dateTextView!!
                                    )

                                    // sending invite-message
                                    val groupKey = snapshot.child("groupKey").value.toString()
                                    val v = arrayOfNulls<View>(1)
                                    if (fromUsername == myUsername) {
                                        v[0] = LayoutInflater.from(this@OpenChat)
                                            .inflate(R.layout.chat_invite_user_2, null, false)
                                        val invite = InviteMessageHolder()
                                        invite.groupName = v[0]!!.findViewById(R.id.chatGroupName)
                                        invite.groupInfo = v[0]!!.findViewById(R.id.chatGroupInfo)
                                        invite.banner = v[0]!!.findViewById(R.id.chatInviteBanner)
                                        invite.logo = v[0]!!.findViewById(R.id.chatInviteLogo)
                                        invite.joinButton =
                                            v[0]!!.findViewById(R.id.chatGroupJoinButton)
                                        invite.timeTextView =
                                            v[0]!!.findViewById(R.id.textview_time)
                                        invite.bubble =
                                            v[0]!!.findViewById(R.id.outgoing_layout_bubble)
                                        invite.statusTextView =
                                            v[0]!!.findViewById(R.id.status_textview)
                                        v[0]!!.tag = invite
                                        if (Colors.isDarkMode(this@OpenChat)) {
                                            invite.bubble!!.background = ContextCompat.getDrawable(
                                                this@OpenChat,
                                                R.drawable.message_design_dark
                                            )
                                        } else {
                                            invite.bubble!!.background = ContextCompat.getDrawable(
                                                this@OpenChat,
                                                R.drawable.message_design_light
                                            )
                                        }
                                        getGroupBanner(groupKey, invite.banner)
                                        getGroupLogo(groupKey, invite.logo)
                                        setConstraintMessageColor(invite.bubble)
                                        setButtonColor(this@OpenChat, invite.joinButton!!)
                                        v[0]!!.setOnClickListener(View.OnClickListener { v1: View? ->
                                            val menu = PopupMenu(this@OpenChat, v1, Gravity.CENTER)
                                            menu.menuInflater.inflate(
                                                R.menu.message_menu_invite,
                                                menu.menu
                                            )
                                            menu.setOnMenuItemClickListener { item: MenuItem ->
                                                when (item.itemId) {
                                                    R.id.deleteMessage -> {
                                                        var v2: View? = null
                                                        v2 = LayoutInflater.from(this@OpenChat)
                                                            .inflate(
                                                                R.layout.chat_invite_user_2,
                                                                null,
                                                                false
                                                            )
                                                        val invite2 = InviteMessageHolder()
                                                        invite2.groupName =
                                                            v2.findViewById(R.id.chatGroupName)
                                                        invite2.groupInfo =
                                                            v2.findViewById(R.id.chatGroupInfo)
                                                        invite2.banner =
                                                            v2.findViewById(R.id.chatInviteBanner)
                                                        invite2.logo =
                                                            v2.findViewById(R.id.chatInviteLogo)
                                                        invite2.joinButton =
                                                            v2.findViewById(R.id.chatGroupJoinButton)
                                                        invite2.timeTextView =
                                                            v2.findViewById(R.id.textview_time)
                                                        invite2.bubble =
                                                            v2.findViewById(R.id.outgoing_layout_bubble)
                                                        invite2.statusTextView =
                                                            v2.findViewById(R.id.status_textview)
                                                        v2.tag = invite2
                                                        invite2.statusTextView!!.text = ""
                                                        if (Colors.isDarkMode(this@OpenChat)) {
                                                            invite2.bubble!!.background =
                                                                ContextCompat.getDrawable(
                                                                    this@OpenChat,
                                                                    R.drawable.message_design_dark
                                                                )
                                                        } else {
                                                            invite2.bubble!!.background =
                                                                ContextCompat.getDrawable(
                                                                    this@OpenChat,
                                                                    R.drawable.message_design_light
                                                                )
                                                        }
                                                        getGroupBanner(groupKey, invite2.banner)
                                                        getGroupLogo(groupKey, invite2.logo)
                                                        setConstraintMessageColor(invite2.bubble)
                                                        setButtonColor(
                                                            this@OpenChat,
                                                            invite2.joinButton!!
                                                        )
                                                        database!!.child("groups/$groupKey")
                                                            .addListenerForSingleValueEvent(object :
                                                                ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    if (snapshot.exists()) {
                                                                        val groupName =
                                                                            snapshot.child("name").value.toString()
                                                                        val groupInfo =
                                                                            snapshot.child("info").value.toString()
                                                                        invite2.groupName!!.text =
                                                                            groupName
                                                                        invite2.groupInfo!!.text =
                                                                            groupInfo
                                                                    } else {
                                                                        invite2.groupName!!.setText(
                                                                            R.string.inviteMessage_deletedTitle
                                                                        )
                                                                        invite2.groupInfo!!.setText(
                                                                            R.string.inviteMessage_deletedSummary
                                                                        )
                                                                        invite2.joinButton!!.isEnabled =
                                                                            false
                                                                        invite2.joinButton!!.setOnClickListener(
                                                                            View.OnClickListener { v: View? ->
                                                                                Toast.makeText(
                                                                                    this@OpenChat,
                                                                                    R.string.inviteMessage_deletedToast,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            })
                                                                    }
                                                                }

                                                                override fun onCancelled(error: DatabaseError) {}
                                                            })
                                                        val builder =
                                                            AlertDialog.Builder(this@OpenChat)
                                                        builder.setTitle(R.string.contextMenu_messageDelete)
                                                            .setView(v2)
                                                            .setPositiveButton(R.string.deleteMessage_all) { dialog: DialogInterface?, which: Int ->
                                                                database!!.child(
                                                                    "users/$myUsername/chats/$otherUsername/messages"
                                                                ).addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot14: DataSnapshot
                                                                        ) {
                                                                            snapshot14.child(key!!).ref.removeValue()
                                                                                .addOnSuccessListener { aVoid: Void? ->
                                                                                    database!!.child(
                                                                                        "users/$otherUsername/chats/$myUsername/messages"
                                                                                    )
                                                                                        .addListenerForSingleValueEvent(
                                                                                            object :
                                                                                                ValueEventListener {
                                                                                                override fun onDataChange(
                                                                                                    snapshot141: DataSnapshot
                                                                                                ) {
                                                                                                    if (snapshot141.exists()) {
                                                                                                        snapshot141.child(
                                                                                                            key
                                                                                                        ).ref.removeValue()
                                                                                                            .addOnSuccessListener { aVoid12: Void? ->
                                                                                                                layout.removeView(
                                                                                                                    v[0]
                                                                                                                )
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_success,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                            }
                                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                                Toast.makeText(
                                                                                                                    this@OpenChat,
                                                                                                                    R.string.deleteMessage_failure,
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                )
                                                                                                                    .show()
                                                                                                                Log.e(
                                                                                                                    MESSAGE_DELETE_ERROR,
                                                                                                                    e.message!!
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
                                                                                .addOnFailureListener { e: Exception ->
                                                                                    Toast.makeText(
                                                                                        this@OpenChat,
                                                                                        e.message,
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                            }
                                                            .setNegativeButton(R.string.deleteMessage_me) { dialog: DialogInterface?, which: Int ->
                                                                database!!.child(
                                                                    "users/$myUsername/chats/$otherUsername/messages"
                                                                ).addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot14: DataSnapshot
                                                                        ) {
                                                                            snapshot14.child(key!!).ref.removeValue()
                                                                                .addOnSuccessListener { aVoid: Void? ->
                                                                                    layout.removeView(
                                                                                        v[0]
                                                                                    )
                                                                                    Toast.makeText(
                                                                                        this@OpenChat,
                                                                                        R.string.deleteMessage_success,
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }
                                                                                .addOnFailureListener { e: Exception ->
                                                                                    Toast.makeText(
                                                                                        this@OpenChat,
                                                                                        R.string.deleteMessage_failure,
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                    Log.e(
                                                                                        MESSAGE_DELETE_ERROR,
                                                                                        e.message!!
                                                                                    )
                                                                                }
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {
                                                                        }
                                                                    })
                                                            }
                                                            .setNeutralButton(
                                                                android.R.string.cancel,
                                                                null
                                                            )
                                                            .show()
                                                    }

                                                    R.id.copyMessage -> {
                                                        val clipboard = getSystemService(
                                                            CLIPBOARD_SERVICE
                                                        ) as ClipboardManager
                                                        val clip = ClipData.newPlainText(
                                                            "message text",
                                                            messageText
                                                        )
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(
                                                            this@OpenChat,
                                                            R.string.copyMessage_success,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }

                                                    R.id.replyToMessage -> replyTo(
                                                        fromUsername,
                                                        messageText,
                                                        key
                                                    )
                                                }
                                                false
                                            }
                                            menu.show()
                                        })
                                        invite.timeTextView!!.text = time
                                        val finalStatus = snapshot.child("status").value.toString()
                                        val messageData1 = snapshot.key
                                        database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                            .addChildEventListener(object : ChildEventListener {
                                                override fun onChildAdded(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onChildChanged(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onChildRemoved(snapshot: DataSnapshot) {
                                                    layout.removeView(v[0])
                                                }

                                                override fun onChildMoved(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                        database!!.child("users/$myUsername/chats/$otherUsername/messages/$key")
                                            .addChildEventListener(object : ChildEventListener {
                                                override fun onChildAdded(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onChildChanged(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                    database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1/status")
                                                        .addListenerForSingleValueEvent(object :
                                                            ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val status =
                                                                    snapshot.value.toString()
                                                                if (status == "SENT") {
                                                                    invite.statusTextView!!.setText(
                                                                        R.string.message_delivered
                                                                    )
                                                                } else if (status == "READ") {
                                                                    invite.statusTextView!!.setText(
                                                                        R.string.message_read
                                                                    )
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {}
                                                        })
                                                }

                                                override fun onChildRemoved(snapshot: DataSnapshot) {}
                                                override fun onChildMoved(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(
                                                        this@OpenChat,
                                                        error.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                        if (finalStatus == "SENT") {
                                            invite.statusTextView!!.setText(R.string.message_delivered)
                                        } else if (finalStatus == "READ") {
                                            invite.statusTextView!!.setText(R.string.message_read)
                                        }
                                        database!!.child("groups/$groupKey")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()) {
                                                        val groupName =
                                                            snapshot.child("name").value.toString()
                                                        val groupInfo =
                                                            snapshot.child("info").value.toString()
                                                        invite.groupName!!.text = groupName
                                                        invite.groupInfo!!.text = groupInfo
                                                        invite.joinButton!!.setOnClickListener(View.OnClickListener { v: View? ->
                                                            val intent = Intent(
                                                                this@OpenChat,
                                                                OpenGroupInfo::class.java
                                                            )
                                                            intent.putExtra("groupKey", groupKey)
                                                                .putExtra("comeFrom", "privateChat")
                                                            startActivity(intent)
                                                            //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
                                                        })
                                                    } else {
                                                        invite.groupName!!.setText(R.string.inviteMessage_deletedTitle)
                                                        invite.groupInfo!!.setText(R.string.inviteMessage_deletedSummary)
                                                        invite.joinButton!!.isEnabled = false
                                                        invite.joinButton!!.setOnClickListener(View.OnClickListener { v: View? ->
                                                            Toast.makeText(
                                                                this@OpenChat,
                                                                R.string.inviteMessage_deletedToast,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        })
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                        val scroll = findViewById<ScrollView>(R.id.scroll)
                                        //add View
                                        if (oldDate[0] != date) {
                                            oldDate[0] = date
                                            layout.addView(dateView)
                                            scroll.post { scroll.scrollBy(0, dateView.height) }
                                        }
                                        if (v[0] != null) {
                                            layout.addView(v[0])
                                            scroll.post {
                                                scroll.scrollBy(
                                                    0, v[0]!!
                                                        .height
                                                )
                                            }
                                        }
                                    } else {
                                        //incomming invite-message
                                        v[0] = LayoutInflater.from(this@OpenChat)
                                            .inflate(R.layout.chat_invite_user_1, null, false)
                                        val invite = InviteMessageHolder()
                                        invite.groupName = v[0]!!.findViewById(R.id.chatGroupName)
                                        invite.groupInfo = v[0]!!.findViewById(R.id.chatGroupInfo)
                                        invite.banner = v[0]!!.findViewById(R.id.chatInviteBanner)
                                        invite.logo = v[0]!!.findViewById(R.id.chatInviteLogo)
                                        invite.joinButton =
                                            v[0]!!.findViewById(R.id.chatGroupJoinButton)
                                        invite.timeTextView =
                                            v[0]!!.findViewById(R.id.textview_time)
                                        invite.bubble =
                                            v[0]!!.findViewById(R.id.incoming_layout_bubble)
                                        v[0]!!.tag = invite
                                        status = snapshot.child("status").value.toString()
                                        if (status == "SENT") {
                                            //set status to "READ"
                                            database!!.child("users/$myUsername/settings/markAsRead")
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (snapshot.exists()) {
                                                            val markAsRead =
                                                                snapshot.value.toString()
                                                            if (markAsRead == "true") {
                                                                database!!.child("users/$otherUsername/chats/$myUsername/messages/$key/status")
                                                                    .setValue("READ")
                                                                    .addOnFailureListener { e: Exception ->
                                                                        Toast.makeText(
                                                                            this@OpenChat,
                                                                            "Error: " + e.message,
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                            }
                                                        } else {
                                                            database!!.child("users/$otherUsername/chats/$myUsername/messages/$key/status")
                                                                .setValue("READ")
                                                                .addOnFailureListener { e: Exception ->
                                                                    Toast.makeText(
                                                                        this@OpenChat,
                                                                        "Error: " + e.message,
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                            database!!.child("users/$myUsername/chats/$otherUsername/messages/$key/status")
                                                .setValue("READ")
                                                .addOnFailureListener { e: Exception ->
                                                    Toast.makeText(
                                                        this@OpenChat,
                                                        "Error: " + e.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        if (Colors.isDarkMode(this@OpenChat)) {
                                            invite.bubble!!.background = ContextCompat.getDrawable(
                                                this@OpenChat,
                                                R.drawable.message_design_dark
                                            )
                                        } else {
                                            invite.bubble!!.background = ContextCompat.getDrawable(
                                                this@OpenChat,
                                                R.drawable.message_design_light
                                            )
                                        }
                                        getGroupBanner(groupKey, invite.banner)
                                        getGroupLogo(groupKey, invite.logo)
                                        setConstraintMessageColor(invite.bubble)
                                        setButtonColor(this@OpenChat, invite.joinButton!!)
                                        invite.timeTextView!!.text = time
                                        val messageData1 = snapshot.key
                                        database!!.child("users/$myUsername/chats/$otherUsername/messages/$messageData1")
                                            .addChildEventListener(object : ChildEventListener {
                                                override fun onChildAdded(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onChildChanged(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onChildRemoved(snapshot: DataSnapshot) {
                                                    layout.removeView(v[0])
                                                }

                                                override fun onChildMoved(
                                                    snapshot: DataSnapshot,
                                                    previousChildName: String?
                                                ) {
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                        database!!.child("groups/$groupKey")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()) {
                                                        val groupName =
                                                            snapshot.child("name").value.toString()
                                                        val groupInfo =
                                                            snapshot.child("info").value.toString()
                                                        invite.groupName!!.text = groupName
                                                        invite.groupInfo!!.text = groupInfo
                                                        invite.joinButton!!.setOnClickListener(View.OnClickListener { v: View? ->
                                                            val intent = Intent(
                                                                this@OpenChat,
                                                                OpenGroupInfo::class.java
                                                            )
                                                            intent.putExtra("groupKey", groupKey)
                                                                .putExtra("comeFrom", "privateChat")
                                                            startActivity(intent)
                                                            //overridePendingTransition( R.anim.slide_up_from_buttom, R.anim.fade_in )
                                                        })
                                                    } else {
                                                        invite.groupName!!.setText(R.string.inviteMessage_deletedTitle)
                                                        invite.groupInfo!!.setText(R.string.inviteMessage_deletedSummary)
                                                        invite.joinButton!!.isEnabled = false
                                                        invite.joinButton!!.setOnClickListener(View.OnClickListener { v: View? ->
                                                            Toast.makeText(
                                                                this@OpenChat,
                                                                R.string.inviteMessage_deletedToast,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        })
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                        val scroll = findViewById<ScrollView>(R.id.scroll)
                                        //add View
                                        if (oldDate[0] != date) {
                                            oldDate[0] = date
                                            layout.addView(dateView)
                                            scroll.post { scroll.scrollBy(0, dateView.height) }
                                        }
                                        if (v[0] != null) {
                                            layout.addView(v[0])
                                            scroll.post {
                                                scroll.scrollBy(
                                                    0, v[0]!!
                                                        .height
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@OpenChat, "Error: ${e.message}".trimIndent(), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        //getAllMessages
        database!!.child("users/$myUsername/chats/$otherUsername/messages")
            .addChildEventListener(childEventListener!!)
    }

    fun getGroupLogo(groupKey: String, logo: ImageView?) {
        logo!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_launcher_playstore))
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
                logo.setImageDrawable(d)
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
                                logo.setImageDrawable(d)
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
                            logo.setImageDrawable(d)
                        }
                    } else {
                        getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                            val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                            val d: Drawable = BitmapDrawable(resources, bmp)
                            logo.setImageDrawable(d)
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
            logo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_launcher_playstore))
        }
    }

    fun getGroupBanner(groupKey: String, banner: ImageView?) {
        banner!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.background_light))
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
                banner.setImageDrawable(d)
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
                                banner.setImageDrawable(d)
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
                            banner.setImageDrawable(d)
                        }
                    } else {
                        getPic.getBytes(7000000).addOnSuccessListener { bytes1: ByteArray ->
                            val bmp = BitmapFactory.decodeByteArray(bytes1, 0, bytes1.size)
                            val d: Drawable = BitmapDrawable(resources, bmp)
                            banner.setImageDrawable(d)
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
            banner.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.background_light))
        }
    }

    fun editMessage(message: String?, key: String?, text: TextView?) {
        editing = true
        editKey = key
        editedText = text
        replying = false
        replyKey = null
        val replyBox = findViewById<ConstraintLayout>(R.id.expertReplyBox)
        setMessageReplyColor(replyBox)
        val abortReply = findViewById<ImageButton>(R.id.expertAbortReply)
        //setButtonColor(abortReply)
        val messageText = findViewById<EditText>(R.id.messageText)
        abortReply.setOnClickListener {
            replyBox.visibility = View.GONE
            editing = false
            editKey = null
            editedText = null
            messageText.text = null
        }
        database!!.child("users/$myUsername/chats/$otherUsername/messages/$key")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messageToEdit = snapshot.child("message").value.toString()
                    val replyFrom = findViewById<TextView>(R.id.openChatReplyFrom)
                    replyFrom.setText(R.string.editMessage_title)
                    val replyMessage = findViewById<TextView>(R.id.openChatReplyMessage)
                    replyBox.visibility = View.VISIBLE
                    if (messageToEdit.length > 150) {
                        val newMessage = messageToEdit.replace("\n", "  ")
                        replyMessage.text = newMessage
                    } else {
                        replyMessage.text = messageToEdit
                    }
                    messageText.setText(messageToEdit)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun replyTo(from: String?, message: String, key: String?) {
        replying = true
        replyKey = key
        editing = false
        editedText = null
        editKey = null
        val replyBox = findViewById<ConstraintLayout>(R.id.expertReplyBox)
        setMessageReplyColor(replyBox)
        val abortReply = findViewById<ImageButton>(R.id.expertAbortReply)
        //setButtonColor(abortReply)
        abortReply.setOnClickListener { v: View? ->
            replyBox.visibility = View.GONE
            replying = false
            replyKey = null
        }
        val replyFrom = findViewById<TextView>(R.id.openChatReplyFrom)
        val replyMessage = findViewById<TextView>(R.id.openChatReplyMessage)
        replyBox.visibility = View.VISIBLE
        replyFrom.text = from
        if (message.length > 150) {
            val newMessage = message.substring(0, 150) + "..."
            replyMessage.text = newMessage
        } else {
            replyMessage.text = message
        }
    }

    fun stopEditing() {
        editing = false
        replyKey = null
        editedText = null
        val replyBox = findViewById<ConstraintLayout>(R.id.expertReplyBox)
        val replyFrom = findViewById<TextView>(R.id.openChatReplyFrom)
        val replyMessage = findViewById<TextView>(R.id.openChatReplyMessage)
        replyBox.visibility = View.GONE
        replyFrom.text = null
        replyMessage.text = null
        val messageText = findViewById<EditText>(R.id.messageText)
        messageText.text = null
    }

    fun stopReplying() {
        replying = false
        replyKey = null
        val replyBox = findViewById<ConstraintLayout>(R.id.expertReplyBox)
        val replyFrom = findViewById<TextView>(R.id.openChatReplyFrom)
        val replyMessage = findViewById<TextView>(R.id.openChatReplyMessage)
        replyBox.visibility = View.GONE
        replyFrom.text = null
        replyMessage.text = null
    }

    val bg: Unit
        get() {
            val bg = findViewById<ImageView>(R.id.openChatBg)
            val yourFilePath = "$filesDir/pics/background.jpg"
            val yourFile = File(yourFilePath)
            if (yourFile.exists()) {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeFile(yourFilePath, options)
                bg.setImageBitmap(bitmap)
            } else {
                if (Colors.isDarkMode(this@OpenChat)) {
                    bg.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.background_picture
                        )
                    )
                } else {
                    bg.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.background_light
                        )
                    )
                }
            }
        }

    fun setLightMode() {
        setTheme(R.style.chatLight)
    }

    fun setDarkMode() {
        setTheme(R.style.chatDark)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_left)
            }

            R.id.removeChat -> removeChat()
            R.id.openChatProfile -> {
                val chat = intent
                val intent = Intent(this, OpenProfile::class.java)
                intent.putExtra("username", chat.extras!!["username"].toString())
                    .putExtra("comeFrom", "chat")
                startActivity(intent)
                //overridePendingTransition(R.anim.slide_up_from_buttom, R.anim.fade_in)
            }

            R.id.backgroundMenuButton -> {
                val intent1 = Intent(this, BackgroundSettings::class.java)
                startActivity(intent1)
                //overridePendingTransition(R.anim.slide_in_right, R.anim.fade_in)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun removeChat() {
        val intent = intent
        val chatUsername = intent.extras!!["username"].toString()
        val username = db!!.getCurrentUsername()
        val database = FirebaseDatabase.getInstance().reference
        val builder = AlertDialog.Builder(this@OpenChat)
        builder.setTitle(R.string.deleteChat_title)
            .setPositiveButton(R.string.deleteChat_me) { dialog: DialogInterface?, which: Int ->
                val builder2 = AlertDialog.Builder(this@OpenChat)
                builder2.setView(R.layout.wait)
                    .setCancelable(false)
                val shower = builder2.show()
                database.child("users/$username/chats/$chatUsername")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.ref.removeValue().addOnSuccessListener { aVoid: Void? ->
                                shower.cancel()
                                Toast.makeText(
                                    this@OpenChat,
                                    R.string.deleteChat_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                                //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_left)
                            }
                                .addOnFailureListener { e: Exception ->
                                    Toast.makeText(
                                        this@OpenChat,
                                        "Error: " + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            .setNegativeButton(R.string.deleteChat_all) { dialog: DialogInterface?, which: Int ->
                val builder2 = AlertDialog.Builder(this@OpenChat)
                builder2.setView(R.layout.wait)
                    .setCancelable(false)
                val shower = builder2.show()
                database.child("users/$username/chats/$chatUsername")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.ref.removeValue().addOnSuccessListener { aVoid: Void? ->
                                database.child(
                                    "users/$chatUsername/chats/$username"
                                ).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot1: DataSnapshot) {
                                        snapshot1.ref.removeValue()
                                            .addOnSuccessListener { aVoid1: Void? ->
                                                shower.cancel()
                                                Toast.makeText(
                                                    this@OpenChat,
                                                    R.string.deleteChat_success,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                                //overridePendingTransition(R.anim.fade_out, R.anim.slide_out_left )
                                            }
                                            .addOnFailureListener { e: Exception ->
                                                Toast.makeText(
                                                    this@OpenChat,
                                                    "Error: " + e.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                                .addOnFailureListener { e: Exception? -> }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            .setNeutralButton(android.R.string.cancel, null)
            .show()
    }

    fun sendMessage() {
        val messageText = findViewById<EditText>(R.id.messageText)
        val intent = intent
        val sendTo = intent.extras!!["username"].toString()
        val date = Date()
        val time = SimpleDateFormat("HH:mm").format(date)
        val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
        val mDatabase = FirebaseDatabase.getInstance().reference
        val push = mDatabase.push().key
        val notifyId = createNotificationId(15).toInt().toString()
        if (messageText.text.toString().trim { it <= ' ' } != "") {
            mDatabase.child("users/$sendTo/blockedUser/$myUsername")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                        } else {
                            val message = messageText.text.toString().trim { it <= ' ' }
                            mDatabase.child("users/$myUsername/chats/$sendTo")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (replying) {
                                            val messageFrom = ReplyMessage(
                                                myUsername,
                                                message,
                                                time,
                                                messageDate,
                                                replyKey,
                                                "SENT",
                                                notifyId,
                                                "text"
                                            )
                                            messageText.text = null
                                            mDatabase.child("users/$myUsername/chats/$sendTo/messages")
                                                .child(
                                                    push!!
                                                ).setValue(messageFrom)
                                                .addOnSuccessListener { aVoid: Void? ->
                                                    val messageTo = ReplyMessage(
                                                        myUsername,
                                                        message,
                                                        time,
                                                        messageDate,
                                                        replyKey,
                                                        "SENT",
                                                        notifyId,
                                                        "text"
                                                    )
                                                    stopReplying()
                                                    mDatabase.child("users/$sendTo/chats/$myUsername/messages")
                                                        .child(
                                                            push
                                                        ).setValue(messageTo)
                                                        .addOnSuccessListener { aVoid1: Void? -> }
                                                }
                                                .addOnFailureListener { e: Exception? ->
                                                    Toast.makeText(
                                                        this@OpenChat,
                                                        R.string.messageError_notSent,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        } else if (editing) {
                                            // Edited message

                                            //User normalMessage = new User(myUsername, message, time, messageDate, "SENT", notifyId, "text");
                                            messageText.text = null
                                            mDatabase.child("users/$myUsername/chats/$sendTo/messages")
                                                .child(
                                                    "$editKey/message"
                                                ).setValue(message)
                                                .addOnSuccessListener { aVoid: Void? ->
                                                    stopEditing()
                                                    mDatabase.child("users/$sendTo/chats/$myUsername/messages")
                                                        .child(
                                                            "$editKey/message"
                                                        ).setValue(message)
                                                        .addOnSuccessListener { aVoid12: Void? -> }
                                                }
                                                .addOnFailureListener { e: Exception? ->
                                                    Toast.makeText(
                                                        this@OpenChat,
                                                        R.string.messageError_notSent,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        } else {
                                            val normalMessage = Message(
                                                myUsername,
                                                message,
                                                time,
                                                messageDate,
                                                "SENT",
                                                notifyId,
                                                "text"
                                            )
                                            messageText.text = null
                                            mDatabase.child("users/$myUsername/chats/$sendTo/messages")
                                                .child(
                                                    push!!
                                                ).setValue(normalMessage)
                                                .addOnSuccessListener { aVoid: Void? ->
                                                    mDatabase.child(
                                                        "users/$sendTo/chats/$myUsername/messages"
                                                    ).child(
                                                        push
                                                    ).setValue(normalMessage)
                                                        .addOnSuccessListener { aVoid12: Void? -> }
                                                }
                                                .addOnFailureListener { e: Exception? ->
                                                    Toast.makeText(
                                                        this@OpenChat,
                                                        R.string.messageError_notSent,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            this@OpenChat,
                                            R.string.messageError_database,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e(MESSAGE_DATABASE_ERROR, error.message)
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    fun unblockUser() {
        database!!.child("users/$myUsername/blockedUser/$otherUsername").removeValue()
            .addOnSuccessListener { aVoid: Void? ->
                Toast.makeText(this@OpenChat, R.string.chat_userUnblocked, Toast.LENGTH_SHORT)
                    .show()
                blocked = false
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private inner class MessageHolder {
        var messageTextView: TextView? = null
        var timeTextView: TextView? = null
        var statusTextView: TextView? = null
        var bubble: ConstraintLayout? = null
    }

    private inner class InviteMessageHolder {
        var groupName: TextView? = null
        var groupInfo: TextView? = null
        var joinButton: Button? = null
        var timeTextView: TextView? = null
        var statusTextView: TextView? = null
        var bubble: ConstraintLayout? = null
        var logo: ImageView? = null
        var banner: ImageView? = null
    }

    private inner class StatusMessageHolder {
        var messageTextView: TextView? = null
        var timeTextView: TextView? = null
        var statusTextView: TextView? = null
        var bubble: ConstraintLayout? = null
    }

    private inner class ReplyMessageHolder {
        var messageTextView: TextView? = null
        var timeTextView: TextView? = null
        var statusTextView: TextView? = null
        var replyFrom: TextView? = null
        var replyMessage: TextView? = null
        var bubble: ConstraintLayout? = null
        var layoutBox: ConstraintLayout? = null
        var replyBox: RelativeLayout? = null
    }

    private inner class DateHolder {
        var dateTextView: TextView? = null
    }

    @IgnoreExtraProperties
    class ReplyMessage(
        var from: String?,
        var message: String,
        var time: String,
        var date: String,
        var reply: String?,
        var status: String,
        var notifyId: String,
        var type: String
    )

    @IgnoreExtraProperties
    class Message(
        var from: String?,
        var message: String,
        var time: String,
        var date: String,
        var status: String,
        var notifyId: String,
        var type: String
    )

    override fun finish() {
        super.finish()
        database!!.child("users/$myUsername/chats/$otherUsername/messages").removeEventListener(
            childEventListener!!
        )
        db!!.close()
    }

    override fun onPause() {
        super.onPause()

        db!!.close()
    }

    override fun onDestroy() {
        super.onDestroy()

        db!!.close()
    }

    override fun onResume() {
        super.onResume()
        bg

        db = DBHelper(this, null)

        // Clear all notification
        val nMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nMgr.cancelAll()
        database!!.child("users/$myUsername/blockedUser/$otherUsername")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blocked = snapshot.exists()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        database!!.child("users/$otherUsername/blockedUser/$myUsername")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    otherBlocked = snapshot.exists()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    companion object {
        private const val MESSAGE_DATABASE_ERROR = "MESSAGE_DATABASE_ERROR"
        private const val MESSAGE_DELETE_ERROR = "MESSAGE_DELETE_ERROR"
        private const val MESSAGE_SEND_ERROR = "MESSAGE_SEND_ERROR"
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

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