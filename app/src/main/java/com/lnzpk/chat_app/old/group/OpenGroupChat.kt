package com.lnzpk.chat_app.old.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
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
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.chat.OpenChat
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import com.lnzpk.chat_app.old.profile.OpenProfile
import com.lnzpk.chat_app.old.settings.BackgroundSettings
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class OpenGroupChat : AppCompatActivity() {
    var myUsername: String? = null
    var preferences: SharedPreferences? = null
    var database: DatabaseReference? = null
    private val hdlr = Handler()
    var replying = false
    var replyKey: String? = null
    var groupKey: String? = null
    var publicGroup: String? = null
    var joined = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.open_group_chat_layout)
        val db = DBHelper(this, null)
        val toolbar = findViewById<Toolbar>(R.id.GroupChatToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(toolbar)
        database = FirebaseDatabase.getInstance().reference
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        myUsername = db.getCurrentUsername()
        replying = false
        val chat = intent
        groupKey = chat.extras!!["groupKey"].toString()
        toolbar.setOnClickListener { v: View? ->
            val openIntent = Intent(this, OpenGroupInfo::class.java)
            openIntent.putExtra("groupKey", groupKey)
                .putExtra("comeFrom", "chat")
            startActivity(openIntent)
        }
        database!!.child("groups/$groupKey")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupName = snapshot.child("name").value.toString()
                    toolbar.title = groupName
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        val input = findViewById<EditText>(R.id.groupMessageText)
        val scroll1 = findViewById<ScrollView>(R.id.groupScroll)
        setEventListener(this, KeyboardVisibilityEventListener { isOpen: Boolean ->
            if (isOpen && input.hasFocus()) {
                //scroll to last view
                val lastChild = scroll1.getChildAt(scroll1.childCount - 1)
                val bottom = lastChild.bottom + scroll1.paddingBottom
                val sy = scroll1.scrollY
                val sh = scroll1.height
                val delta = bottom - (sy + sh)
                scroll1.scrollBy(0, delta)
            }
        })
        database!!.child("groups/$groupKey/members/$myUsername")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        joined = true
                        database!!.child("groups/$groupKey/members")
                            .addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    database!!.child("groups/$groupKey/members/$myUsername")
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                joined = snapshot.exists()
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
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OpenGroupChat, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        bg
        try {
            chats
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        val sendButton = findViewById<FloatingActionButton>(R.id.groupSend)
        if (isDarkMode) {
            sendButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0F6185"))
        } else {
            sendButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#26C6DA"))
        }
        setFabColor(sendButton)
        sendButton.setOnClickListener { view: View? -> sendMessage() }
        val messageInput = findViewById<ConstraintLayout>(R.id.groupChatBox)
        if (isDarkMode) {
            messageInput.background = ContextCompat.getDrawable(this, R.drawable.chat_input_dark)
        } else {
            messageInput.background = ContextCompat.getDrawable(this, R.drawable.chat_input_light)
        }
        setMessageEtColor(messageInput)
        val joinButton = findViewById<Button>(R.id.openGroupChatJoinButton)
        setButtonColor(joinButton)
        val notJoinedText = findViewById<TextView>(R.id.groupChatNotJoinedText)
        val updateStatus: Runnable = object : Runnable {
            override fun run() {
                val scroll1 = findViewById<ScrollView>(R.id.groupScroll)
                val button = findViewById<FloatingActionButton>(R.id.groupScrollDownButton)
                button.setOnClickListener { v: View? -> scroll1.fullScroll(View.FOCUS_DOWN) }
                setFabColor(button)
                if (scroll1.getChildAt(0).bottom <= scroll1.height + scroll1.scrollY) {
                    button.visibility = View.GONE
                } else {
                    button.visibility = View.VISIBLE
                }
                database!!.child("groups/$groupKey/members/$myUsername")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // joined
                                joinButton.visibility = View.GONE
                                notJoinedText.visibility = View.GONE
                                sendButton.visibility = View.VISIBLE
                                input.visibility = View.VISIBLE
                            } else {
                                // not joined
                                replying = false
                                database!!.child("groups/$groupKey")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                notJoinedText.visibility = View.VISIBLE
                                                joinButton.visibility = View.VISIBLE
                                                sendButton.visibility = View.GONE
                                                input.visibility = View.GONE
                                                joinButton.setOnClickListener { view: View? ->
                                                    val builder =
                                                        AlertDialog.Builder(this@OpenGroupChat)
                                                    builder.setView(R.layout.wait)
                                                        .setCancelable(false)
                                                    val alert = builder.show()
                                                    database!!.child("groups/$groupKey/members")
                                                        .addListenerForSingleValueEvent(object :
                                                            ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val membersCounts =
                                                                    snapshot.childrenCount.toString()
                                                                        .toInt()
                                                                if (membersCounts > 0) {
                                                                    database!!.child("groups/$groupKey/members/$myUsername")
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
                                                                            val push =
                                                                                mDatabase.push().key
                                                                            val notifyId =
                                                                                generateRandom(15).toInt()
                                                                                    .toString()
                                                                            val groupMessage =
                                                                                NewGroup.Message(
                                                                                    "system",
                                                                                    "$myUsername hat die Gruppe betreten",
                                                                                    time,
                                                                                    messageDate,
                                                                                    "SENT",
                                                                                    notifyId
                                                                                )
                                                                            mDatabase.child("groups/$groupKey/messages")
                                                                                .child(
                                                                                    push!!
                                                                                ).setValue(
                                                                                groupMessage
                                                                            )
                                                                                .addOnSuccessListener { aVoid: Void? -> alert.cancel() }
                                                                                .addOnFailureListener { e: Exception ->
                                                                                    Toast.makeText(
                                                                                        this@OpenGroupChat,
                                                                                        """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(),
                                                                                        Toast.LENGTH_LONG
                                                                                    ).show()
                                                                                }
                                                                        }
                                                                        .addOnFailureListener { e: Exception ->
                                                                            Toast.makeText(
                                                                                this@OpenGroupChat,
                                                                                "Error: " + e.message,
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                } else {
                                                                    database!!.child("groups/$groupKey/members/$myUsername")
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
                                                                            val push =
                                                                                mDatabase.push().key
                                                                            val notifyId =
                                                                                generateRandom(15).toInt()
                                                                                    .toString()
                                                                            val groupMessage =
                                                                                NewGroup.Message(
                                                                                    "system",
                                                                                    "$myUsername hat die Gruppe betreten",
                                                                                    time,
                                                                                    messageDate,
                                                                                    "SENT",
                                                                                    notifyId
                                                                                )
                                                                            mDatabase.child("groups/$groupKey/messages")
                                                                                .child(
                                                                                    push!!
                                                                                ).setValue(
                                                                                groupMessage
                                                                            )
                                                                                .addOnSuccessListener { aVoid: Void? -> alert.cancel() }
                                                                                .addOnFailureListener { e: Exception ->
                                                                                    Toast.makeText(
                                                                                        this@OpenGroupChat,
                                                                                        """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(),
                                                                                        Toast.LENGTH_LONG
                                                                                    ).show()
                                                                                }
                                                                        }
                                                                        .addOnFailureListener { e: Exception ->
                                                                            Toast.makeText(
                                                                                this@OpenGroupChat,
                                                                                "Error: " + e.message,
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {}
                                                        })
                                                }
                                            } else {
                                                notJoinedText.visibility = View.VISIBLE
                                                joinButton.visibility = View.VISIBLE
                                                sendButton.visibility = View.GONE
                                                input.visibility = View.GONE
                                                notJoinedText.text = "Diese Gruppe wurde gelöscht."
                                                joinButton.text = "Chat schließen"
                                                joinButton.setOnClickListener { v: View? -> finish() }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                hdlr.postDelayed(this, 10)
            }
        }
        updateStatus.run()
        val replyBox = findViewById<ConstraintLayout>(R.id.groupExpertReplyBox)
        replyBox.visibility = View.GONE
    }

    fun setMessageReplyBubbleColor(replyBubble: RelativeLayout?) {
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
                val `object` = JSONObject(jsn.opt(10).toString())
                if (`object`.opt("messageReplyColor").toString().toInt() != 0) {
                    constraintLayout.backgroundTintList =
                        ColorStateList.valueOf(`object`.opt("messageReplyColor").toString().toInt())
                }
            } catch (e: Exception) {
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

    fun setToolbarColor(toolbar: Toolbar) {
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

    fun setRelativeMessageColor(relativeLayout: RelativeLayout) {
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
                val `object` = JSONObject(jsn.opt(7).toString())
                if (`object`.opt("messageBubbleColor").toString().toInt() != 0) {
                    relativeLayout.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("messageBubbleColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setConstraintMessageColor(constraintLayout: ConstraintLayout?) {
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

    fun setMessageColor(frameLayout: FrameLayout) {
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

    fun setMessageBoxColor(relativeLayout: RelativeLayout) {
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
                val `object` = JSONObject(jsn.opt(7).toString())
                if (`object`.opt("messageBubbleColor").toString().toInt() != 0) {
                    relativeLayout.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("messageBubbleColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setFabColor(fab: FloatingActionButton) {
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

    fun setMessageEtColor(messageInput: ConstraintLayout) {
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

    fun setDateMessageBubbleColor(textView: TextView?) {
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
                val `object` = JSONObject(jsn.opt(8).toString())
                if (`object`.opt("dateGroupMessageColor").toString().toInt() != 0) {
                    textView!!.backgroundTintList = ColorStateList.valueOf(
                        `object`.opt("dateGroupMessageColor").toString().toInt()
                    )
                }
            } catch (e: Exception) {
            }
        }
    }
    val chats: Unit
        get() {
            val scroll1 = findViewById<ScrollView>(R.id.groupScroll)
            val button = findViewById<FloatingActionButton>(R.id.groupScrollDownButton)
            button.setOnClickListener { v: View? -> scroll1.fullScroll(View.FOCUS_DOWN) }
            setFabColor(button)
            scroll1.viewTreeObserver
                .addOnScrollChangedListener {
                    if (scroll1.getChildAt(0).bottom
                        <= scroll1.height + scroll1.scrollY
                    ) {
                        button.visibility = View.GONE
                        //scroll view is at bottom
                        //set the button visibility to visible here
                    } else {
                        button.visibility = View.VISIBLE
                        //scroll view is not at bottom
                        //set the button visibility to gone here
                    }
                }
            val oldDate = arrayOf("")
            val layout = findViewById<LinearLayout>(R.id.groupMessagesList)
            database!!.child("groups/$groupKey/messages")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        try {
                            val messages = snapshot.value as Map<*, *>?
                            val date = messages!!["date"].toString()
                            val fromUsername = messages["from"].toString()
                            val messageText = messages["message"].toString()
                            val time = messages["time"].toString()
                            val key = snapshot.key

                            val dateView: View
                            dateView = LayoutInflater.from(this@OpenGroupChat)
                                .inflate(R.layout.date_message_category, null, false)
                            val dateHolder: DateHolder = DateHolder()
                            dateHolder.dateTextView = dateView.findViewById(R.id.dateTextView)
                            dateView.tag = dateHolder
                            dateHolder.dateTextView!!.setText(date)
                            if (isDarkMode) {
                                dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_dark)
                            } else {
                                dateHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_light)
                            }
                            setDateMessageBubbleColor(dateHolder.dateTextView)

                            //add View
                            if (oldDate[0] != date) {
                                oldDate[0] = date
                                layout.addView(dateView)
                                scroll1.post { scroll1.scrollBy(0, dateView.height) }
                            }
                            if (fromUsername == "system") {
                                val noticeMessage: View
                                noticeMessage = LayoutInflater.from(this@OpenGroupChat)
                                    .inflate(R.layout.date_message_category, null, false)
                                val noticeHolder: DateHolder = DateHolder()
                                noticeHolder.dateTextView =
                                    noticeMessage.findViewById(R.id.dateTextView)
                                noticeMessage.tag = noticeMessage
                                noticeHolder.dateTextView!!.setText(messageText)
                                if (isDarkMode) {
                                    noticeHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_dark)
                                } else {
                                    noticeHolder.dateTextView!!.setBackgroundResource(R.drawable.message_date_category_design_light)
                                }
                                setDateMessageBubbleColor(noticeHolder.dateTextView)
                                database!!.child("groups/$groupKey/members/$myUsername")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                val admin = snapshot.value.toString()
                                                if (admin == "1") {
                                                    noticeMessage.setOnLongClickListener { view: View? ->
                                                        val menu = PopupMenu(
                                                            this@OpenGroupChat,
                                                            view,
                                                            Gravity.CENTER
                                                        )
                                                        menu.menuInflater.inflate(
                                                            R.menu.group_notice_menu,
                                                            menu.menu
                                                        )
                                                        menu.setOnMenuItemClickListener { item: MenuItem ->
                                                            if (item.itemId == R.id.deleteMessage) {
                                                                val noticeMessage2: View
                                                                noticeMessage2 =
                                                                    LayoutInflater.from(this@OpenGroupChat)
                                                                        .inflate(
                                                                            R.layout.date_message_category,
                                                                            null,
                                                                            false
                                                                        )
                                                                val noticeHolder2: DateHolder =
                                                                    DateHolder()
                                                                noticeHolder2.dateTextView =
                                                                    noticeMessage2.findViewById(R.id.dateTextView)
                                                                noticeMessage2.tag = noticeMessage2
                                                                noticeHolder2.dateTextView!!.setText(
                                                                    messageText
                                                                )
                                                                if (isDarkMode) {
                                                                    noticeHolder2.dateTextView!!.setBackgroundResource(
                                                                        R.drawable.message_date_category_design_dark
                                                                    )
                                                                } else {
                                                                    noticeHolder2.dateTextView!!.setBackgroundResource(
                                                                        R.drawable.message_date_category_design_light
                                                                    )
                                                                }
                                                                setDateMessageBubbleColor(
                                                                    noticeHolder2.dateTextView
                                                                )
                                                                AlertDialog.Builder(this@OpenGroupChat)
                                                                    .setTitle("Nachricht löschen?")
                                                                    .setMessage("Nachrichten werden immer für alle gelöscht.")
                                                                    .setView(noticeMessage2)
                                                                    .setPositiveButton("löschen") { dialog: DialogInterface?, which: Int ->
                                                                        database!!.child(
                                                                            "groups/$groupKey/messages"
                                                                        ).child(
                                                                            key!!
                                                                        )
                                                                            .addListenerForSingleValueEvent(
                                                                                object :
                                                                                    ValueEventListener {
                                                                                    override fun onDataChange(
                                                                                        snapshot: DataSnapshot
                                                                                    ) {
                                                                                        snapshot.ref.removeValue()
                                                                                            .addOnSuccessListener { unused: Void? ->
                                                                                                layout.removeView(
                                                                                                    noticeMessage
                                                                                                )
                                                                                                Toast.makeText(
                                                                                                    this@OpenGroupChat,
                                                                                                    "Nachricht gelöscht.",
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
                                                                                            }
                                                                                            .addOnFailureListener { e: Exception ->
                                                                                                Toast.makeText(
                                                                                                    this@OpenGroupChat,
                                                                                                    "Fehler: " + e.message,
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
                                                                        "Abbrechen",
                                                                        null
                                                                    )
                                                                    .show()
                                                            }
                                                            false
                                                        }
                                                        menu.show()
                                                        false
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                layout.addView(noticeMessage)
                                scroll1.post { scroll1.scrollBy(0, noticeMessage.height) }
                            } else {
                                val v: View?
                                if (fromUsername == myUsername) {
                                    if (snapshot.child("reply").exists()) {
                                        v = LayoutInflater.from(this@OpenGroupChat)
                                            .inflate(R.layout.group_chat_reply_user_2, null, false)
                                        val replyKey = snapshot.child("reply").value.toString()
                                        database!!.child("groups/$groupKey/messages/$replyKey")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val replyFrom =
                                                        snapshot.child("from").value.toString()
                                                    val replyMessage =
                                                        snapshot.child("message").value.toString()
                                                    val holder: ReplyMessageHolder =
                                                        ReplyMessageHolder()
                                                    holder.messageTextView =
                                                        v.findViewById(R.id.textview_message)
                                                    holder.timeTextView =
                                                        v.findViewById(R.id.textview_time)
                                                    holder.bubble =
                                                        v.findViewById(R.id.incoming_layout_bubble)
                                                    holder.replyFrom =
                                                        v.findViewById(R.id.textview_replyFrom)
                                                    holder.replyMessage =
                                                        v.findViewById(R.id.textview_replyMessage)
                                                    holder.layoutBox =
                                                        v.findViewById(R.id.incomming_layout_box)
                                                    holder.replyBox = v.findViewById(R.id.replyBox)
                                                    if (isDarkMode) {
                                                        holder.layoutBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_dark
                                                            )
                                                        )
                                                        holder.replyBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_light
                                                            )
                                                        )
                                                    } else {
                                                        holder.layoutBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_light
                                                            )
                                                        )
                                                        holder.replyBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_dark
                                                            )
                                                        )
                                                    }
                                                    setMessageReplyBubbleColor(holder.replyBox)
                                                    setConstraintMessageColor(holder.layoutBox)
                                                    v.tag = holder
                                                    holder.messageTextView!!.setText(messageText)
                                                    holder.timeTextView!!.setText(time)
                                                    if (replyFrom == myUsername) {
                                                        holder.replyFrom!!.setText("Du")
                                                    } else {
                                                        holder.replyFrom!!.setText(replyFrom)
                                                    }
                                                    holder.replyMessage!!.setText(replyMessage)
                                                    v.setOnClickListener(View.OnClickListener { v1: View? ->
                                                        val menu = PopupMenu(
                                                            this@OpenGroupChat,
                                                            v1,
                                                            Gravity.CENTER
                                                        )
                                                        menu.menuInflater.inflate(
                                                            R.menu.group_me_menu,
                                                            menu.menu
                                                        )
                                                        menu.setOnMenuItemClickListener { item: MenuItem ->
                                                            when (item.itemId) {
                                                                R.id.deleteMyGroupMessage -> {
                                                                    val v2: View
                                                                    v2 =
                                                                        LayoutInflater.from(this@OpenGroupChat)
                                                                            .inflate(
                                                                                R.layout.group_chat_reply_user_2,
                                                                                null,
                                                                                false
                                                                            )
                                                                    val holder2: ReplyMessageHolder =
                                                                        ReplyMessageHolder()
                                                                    holder2.messageTextView =
                                                                        v2.findViewById(R.id.textview_message)
                                                                    holder2.timeTextView =
                                                                        v2.findViewById(R.id.textview_time)
                                                                    holder2.bubble =
                                                                        v2.findViewById(R.id.incoming_layout_bubble)
                                                                    holder2.replyFrom =
                                                                        v2.findViewById(R.id.textview_replyFrom)
                                                                    holder2.replyMessage =
                                                                        v2.findViewById(R.id.textview_replyMessage)
                                                                    holder2.layoutBox =
                                                                        v2.findViewById(R.id.incomming_layout_box)
                                                                    holder2.replyBox =
                                                                        v2.findViewById(R.id.replyBox)
                                                                    if (isDarkMode) {
                                                                        holder2.layoutBox!!.setBackground(
                                                                            ContextCompat.getDrawable(
                                                                                this@OpenGroupChat,
                                                                                R.drawable.message_design_dark
                                                                            )
                                                                        )
                                                                        holder2.replyBox!!.setBackground(
                                                                            ContextCompat.getDrawable(
                                                                                this@OpenGroupChat,
                                                                                R.drawable.message_design_light
                                                                            )
                                                                        )
                                                                    } else {
                                                                        holder2.layoutBox!!.setBackground(
                                                                            ContextCompat.getDrawable(
                                                                                this@OpenGroupChat,
                                                                                R.drawable.message_design_light
                                                                            )
                                                                        )
                                                                        holder2.replyBox!!.setBackground(
                                                                            ContextCompat.getDrawable(
                                                                                this@OpenGroupChat,
                                                                                R.drawable.message_design_dark
                                                                            )
                                                                        )
                                                                    }
                                                                    setMessageReplyBubbleColor(
                                                                        holder2.replyBox
                                                                    )
                                                                    setConstraintMessageColor(
                                                                        holder2.layoutBox
                                                                    )
                                                                    v2.tag = holder2
                                                                    holder2.messageTextView!!.setText(
                                                                        messageText
                                                                    )
                                                                    holder2.timeTextView!!.setText(
                                                                        time
                                                                    )
                                                                    if (replyFrom == myUsername) {
                                                                        holder2.replyFrom!!.setText("Du")
                                                                    } else {
                                                                        holder2.replyFrom!!.setText(
                                                                            replyFrom
                                                                        )
                                                                    }
                                                                    holder2.replyMessage!!.setText(
                                                                        replyMessage
                                                                    )
                                                                    AlertDialog.Builder(this@OpenGroupChat)
                                                                        .setTitle("Nachricht löschen?")
                                                                        .setMessage("Nachrichten werden immer für alle gelöscht.")
                                                                        .setView(v2)
                                                                        .setPositiveButton("löschen") { dialog: DialogInterface?, which: Int ->
                                                                            database!!.child(
                                                                                "groups/$groupKey/messages"
                                                                            ).child(
                                                                                key!!
                                                                            )
                                                                                .addListenerForSingleValueEvent(
                                                                                    object :
                                                                                        ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            snapshot.ref.removeValue()
                                                                                                .addOnSuccessListener { unused: Void? ->
                                                                                                    layout.removeView(
                                                                                                        v
                                                                                                    )
                                                                                                    Toast.makeText(
                                                                                                        this@OpenGroupChat,
                                                                                                        "Nachricht gelöscht.",
                                                                                                        Toast.LENGTH_SHORT
                                                                                                    )
                                                                                                        .show()
                                                                                                }
                                                                                                .addOnFailureListener { e: Exception ->
                                                                                                    Toast.makeText(
                                                                                                        this@OpenGroupChat,
                                                                                                        "Fehler: " + e.message,
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
                                                                            "Abbrechen",
                                                                            null
                                                                        )
                                                                        .show()
                                                                }
                                                                R.id.copyMyGroupText -> {
                                                                    val clipboard =
                                                                        getSystemService(
                                                                            CLIPBOARD_SERVICE
                                                                        ) as ClipboardManager
                                                                    val clip =
                                                                        ClipData.newPlainText(
                                                                            "message text",
                                                                            messageText
                                                                        )
                                                                    clipboard.setPrimaryClip(clip)
                                                                    Toast.makeText(
                                                                        this@OpenGroupChat,
                                                                        "Nachricht kopiert.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                                R.id.replyToMyGroupMessage -> replyTo(
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

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                    } else {
                                        v = LayoutInflater.from(this@OpenGroupChat)
                                            .inflate(R.layout.group_chat_user2, null, false)
                                        val holder1: MessageHolder = MessageHolder()
                                        holder1.messageTextView =
                                            v.findViewById(R.id.textview_message)
                                        holder1.timeTextView = v.findViewById(R.id.textview_time)
                                        holder1.bubble = v.findViewById(R.id.outgoing_layout_bubble)
                                        v.tag = holder1
                                        if (isDarkMode) {
                                            holder1.bubble!!.setBackground(
                                                ContextCompat.getDrawable(
                                                    this@OpenGroupChat,
                                                    R.drawable.message_design_dark
                                                )
                                            )
                                        } else {
                                            holder1.bubble!!.setBackground(
                                                ContextCompat.getDrawable(
                                                    this@OpenGroupChat,
                                                    R.drawable.message_design_light
                                                )
                                            )
                                        }
                                        setConstraintMessageColor(holder1.bubble)
                                        holder1.messageTextView!!.setText(messageText)
                                        holder1.timeTextView!!.setText(time)
                                        v.setOnClickListener(View.OnClickListener { v1: View? ->
                                            val menu =
                                                PopupMenu(this@OpenGroupChat, v1, Gravity.CENTER)
                                            menu.menuInflater.inflate(
                                                R.menu.group_me_menu,
                                                menu.menu
                                            )
                                            menu.setOnMenuItemClickListener { item: MenuItem ->
                                                when (item.itemId) {
                                                    R.id.deleteMyGroupMessage -> {
                                                        val newView: View
                                                        newView =
                                                            LayoutInflater.from(this@OpenGroupChat)
                                                                .inflate(
                                                                    R.layout.chat_user_2,
                                                                    null,
                                                                    false
                                                                )
                                                        val holder2: MessageHolder = MessageHolder()
                                                        holder2.messageTextView =
                                                            newView.findViewById(R.id.textview_message)
                                                        holder2.timeTextView =
                                                            newView.findViewById(R.id.textview_time)
                                                        holder2.bubble =
                                                            newView.findViewById(R.id.outgoing_layout_bubble)
                                                        newView.tag = holder2
                                                        if (isDarkMode) {
                                                            holder2.bubble!!.setBackground(
                                                                ContextCompat.getDrawable(
                                                                    this@OpenGroupChat,
                                                                    R.drawable.message_design_dark
                                                                )
                                                            )
                                                        } else {
                                                            holder2.bubble!!.setBackground(
                                                                ContextCompat.getDrawable(
                                                                    this@OpenGroupChat,
                                                                    R.drawable.message_design_light
                                                                )
                                                            )
                                                        }
                                                        setConstraintMessageColor(holder2.bubble)
                                                        holder2.messageTextView!!.setText(messageText)
                                                        holder2.timeTextView!!.setText(time)
                                                        AlertDialog.Builder(this@OpenGroupChat)
                                                            .setTitle("Nachricht löschen?")
                                                            .setMessage("Nachrichten werden immer für alle gelöscht.")
                                                            .setView(newView)
                                                            .setPositiveButton("löschen") { dialog: DialogInterface?, which: Int ->
                                                                database!!.child(
                                                                    "groups/$groupKey/messages"
                                                                ).child(
                                                                    key!!
                                                                ).addListenerForSingleValueEvent(
                                                                    object : ValueEventListener {
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            snapshot.ref.removeValue()
                                                                                .addOnSuccessListener { unused: Void? ->
                                                                                    layout.removeView(
                                                                                        v
                                                                                    )
                                                                                    Toast.makeText(
                                                                                        this@OpenGroupChat,
                                                                                        "Nachricht gelöscht.",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }
                                                                                .addOnFailureListener { e: Exception ->
                                                                                    Toast.makeText(
                                                                                        this@OpenGroupChat,
                                                                                        "Fehler: " + e.message,
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
                                                            .setNegativeButton("Abbrechen", null)
                                                            .show()
                                                    }
                                                    R.id.copyMyGroupText -> {
                                                        val clipboard = getSystemService(
                                                            CLIPBOARD_SERVICE
                                                        ) as ClipboardManager
                                                        val clip = ClipData.newPlainText(
                                                            "message text",
                                                            messageText
                                                        )
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(
                                                            this@OpenGroupChat,
                                                            "Nachricht kopiert.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    R.id.replyToMyGroupMessage -> replyTo(
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
                                    if (snapshot.child("reply").exists()) {
                                        v = LayoutInflater.from(this@OpenGroupChat)
                                            .inflate(R.layout.group_chat_reply_user_1, null, false)
                                        val replyKey = snapshot.child("reply").value.toString()
                                        database!!.child("groups/$groupKey/messages/$replyKey")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val replyFrom =
                                                        snapshot.child("from").value.toString()
                                                    val replyMessage =
                                                        snapshot.child("message").value.toString()
                                                    val holder: ReplyMessageHolder =
                                                        ReplyMessageHolder()
                                                    holder.messageTextView =
                                                        v.findViewById(R.id.textview_message)
                                                    holder.timeTextView =
                                                        v.findViewById(R.id.textview_time)
                                                    holder.bubble =
                                                        v.findViewById(R.id.incoming_layout_bubble)
                                                    holder.replyFrom =
                                                        v.findViewById(R.id.textview_replyFrom)
                                                    holder.replyMessage =
                                                        v.findViewById(R.id.textview_replyMessage)
                                                    holder.layoutBox =
                                                        v.findViewById(R.id.incomming_layout_box)
                                                    holder.replyBox = v.findViewById(R.id.replyBox)
                                                    holder.usernameButton =
                                                        v.findViewById(R.id.button_username)
                                                    if (isDarkMode) {
                                                        holder.layoutBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_dark
                                                            )
                                                        )
                                                        holder.replyBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_light
                                                            )
                                                        )
                                                    } else {
                                                        holder.layoutBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_light
                                                            )
                                                        )
                                                        holder.replyBox!!.setBackground(
                                                            ContextCompat.getDrawable(
                                                                this@OpenGroupChat,
                                                                R.drawable.message_design_dark
                                                            )
                                                        )
                                                    }
                                                    setMessageReplyBubbleColor(holder.replyBox)
                                                    setConstraintMessageColor(holder.layoutBox)
                                                    v.tag = holder
                                                    holder.usernameButton!!.setText(fromUsername)
                                                    holder.usernameButton!!.setOnClickListener {
                                                        val charSequences = arrayOf<CharSequence>(
                                                            "Nachricht an $fromUsername",
                                                            "Profil öffnen"
                                                        )
                                                        AlertDialog.Builder(this@OpenGroupChat)
                                                            .setTitle("Wähle eine Aktion aus:")
                                                            .setItems(charSequences) { _: DialogInterface?, which: Int ->
                                                                when (which) {
                                                                    0 -> {
                                                                        val intent = Intent(
                                                                            this@OpenGroupChat,
                                                                            OpenChat::class.java
                                                                        )
                                                                        intent.putExtra(
                                                                            "username",
                                                                            fromUsername
                                                                        )
                                                                            .putExtra(
                                                                                "nameMe",
                                                                                myUsername
                                                                            )
                                                                        startActivity(intent)
                                                                        finish()
                                                                    }

                                                                    1 -> {
                                                                        val intent1 = Intent(
                                                                            this@OpenGroupChat,
                                                                            OpenProfile::class.java
                                                                        )
                                                                        intent1.putExtra(
                                                                            "username",
                                                                            fromUsername
                                                                        )
                                                                            .putExtra(
                                                                                "comeFrom",
                                                                                "group"
                                                                            )
                                                                        startActivity(intent1)
                                                                        finish()
                                                                    }
                                                                }
                                                            }
                                                            .show()
                                                    }
                                                    holder.messageTextView!!.setText(messageText)
                                                    holder.timeTextView!!.setText(time)
                                                    if (replyFrom == myUsername) {
                                                        holder.replyFrom!!.setText("Du")
                                                    } else {
                                                        holder.replyFrom!!.setText(replyFrom)
                                                    }
                                                    holder.replyMessage!!.setText(replyMessage)
                                                    v.setOnClickListener(View.OnClickListener { v1: View? ->
                                                        val menu = PopupMenu(
                                                            this@OpenGroupChat,
                                                            v1,
                                                            Gravity.CENTER
                                                        )
                                                        menu.menuInflater.inflate(
                                                            R.menu.group_other_menu,
                                                            menu.menu
                                                        )
                                                        menu.setOnMenuItemClickListener { item: MenuItem ->
                                                            when (item.itemId) {
                                                                R.id.copyOtherGroupText -> {
                                                                    val clipboard =
                                                                        getSystemService(
                                                                            CLIPBOARD_SERVICE
                                                                        ) as ClipboardManager
                                                                    val clip =
                                                                        ClipData.newPlainText(
                                                                            "message text",
                                                                            messageText
                                                                        )
                                                                    clipboard.setPrimaryClip(clip)
                                                                    Toast.makeText(
                                                                        this@OpenGroupChat,
                                                                        "Nachricht kopiert.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                                R.id.replyOtherGroupMessage -> replyTo(
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

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                    } else {
                                        v = LayoutInflater.from(this@OpenGroupChat)
                                            .inflate(R.layout.group_chat_user_1, null, false)
                                        val holder1: MessageHolder = MessageHolder()
                                        holder1.messageTextView =
                                            v.findViewById(R.id.textview_message)
                                        holder1.timeTextView = v.findViewById(R.id.textview_time)
                                        holder1.bubble = v.findViewById(R.id.incoming_layout_bubble)
                                        holder1.usernameButton =
                                            v.findViewById(R.id.button_username)
                                        holder1.layoutBubble =
                                            v.findViewById(R.id.group_layout_bubble)
                                        if (isDarkMode) {
                                            holder1.layoutBubble!!.setBackground(
                                                ContextCompat.getDrawable(
                                                    this@OpenGroupChat,
                                                    R.drawable.message_design_dark
                                                )
                                            )
                                        } else {
                                            holder1.layoutBubble!!.setBackground(
                                                ContextCompat.getDrawable(
                                                    this@OpenGroupChat,
                                                    R.drawable.message_design_light
                                                )
                                            )
                                        }
                                        setConstraintMessageColor(holder1.layoutBubble)
                                        v.tag = holder1
                                        holder1.messageTextView!!.setText(messageText)
                                        holder1.timeTextView!!.setText(time)
                                        holder1.usernameButton!!.setText(fromUsername)
                                        holder1.usernameButton!!.setOnClickListener {
                                            val charSequences = arrayOf<CharSequence>(
                                                "Nachricht an $fromUsername", "Profil öffnen"
                                            )
                                            AlertDialog.Builder(this@OpenGroupChat)
                                                .setTitle("Wähle eine Aktion aus:")
                                                .setItems(charSequences) { dialog: DialogInterface?, which: Int ->
                                                    when (which) {
                                                        0 -> {
                                                            val intent = Intent(
                                                                this@OpenGroupChat,
                                                                OpenChat::class.java
                                                            )
                                                            intent.putExtra(
                                                                "username",
                                                                fromUsername
                                                            )
                                                                .putExtra("nameMe", myUsername)
                                                            startActivity(intent)
                                                            finish()
                                                        }

                                                        1 -> {
                                                            val intent1 = Intent(
                                                                this@OpenGroupChat,
                                                                OpenProfile::class.java
                                                            )
                                                            intent1.putExtra(
                                                                "username",
                                                                fromUsername
                                                            )
                                                                .putExtra("comeFrom", "group")
                                                            startActivity(intent1)
                                                        }
                                                    }
                                                }
                                                .show()
                                        }
                                        v.setOnClickListener(View.OnClickListener { v1: View? ->
                                            val menu =
                                                PopupMenu(this@OpenGroupChat, v1, Gravity.CENTER)
                                            menu.menuInflater.inflate(
                                                R.menu.group_other_menu,
                                                menu.menu
                                            )
                                            menu.setOnMenuItemClickListener { item: MenuItem ->
                                                when (item.itemId) {
                                                    R.id.copyOtherGroupText -> {
                                                        val clipboard =
                                                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText(
                                                            "message text",
                                                            messageText
                                                        )
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(
                                                            this@OpenGroupChat,
                                                            "Nachricht kopiert.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    R.id.replyOtherGroupMessage -> replyTo(
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
                                if (v != null) {
                                    layout.addView(v)
                                    scroll1.post { scroll1.scrollBy(0, v.height) }
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@OpenGroupChat, """
     Error:
     ${e.message}
     """.trimIndent(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    fun replyTo(from: String?, message: String, key: String?) {
        replying = true
        replyKey = key
        val replyBox = findViewById<ConstraintLayout>(R.id.groupExpertReplyBox)
        setMessageReplyColor(replyBox)
        val abortReply = findViewById<Button>(R.id.groupExpertAbortReply)
        setButtonColor(abortReply)
        abortReply.setOnClickListener { v: View? ->
            replyBox.visibility = View.GONE
            replying = false
            replyKey = null
        }
        val replyFrom = findViewById<TextView>(R.id.openGroupChatReplyFrom)
        val replyMessage = findViewById<TextView>(R.id.openGroupChatReplyMessage)
        replyBox.visibility = View.VISIBLE
        if (message.length > 150) {
            val newMessage = message.substring(0, 150) + "..."
            replyMessage.text = newMessage
        } else {
            replyMessage.text = message
        }
        replyFrom.text = from
    }

    fun stopReplying() {
        replying = false
        replyKey = null
        val replyBox = findViewById<ConstraintLayout>(R.id.groupExpertReplyBox)
        val replyFrom = findViewById<TextView>(R.id.openGroupChatReplyFrom)
        val replyMessage = findViewById<TextView>(R.id.openGroupChatReplyMessage)
        replyBox.visibility = View.GONE
        replyFrom.text = null
        replyMessage.text = null
    }

    val bg: Unit
        get() {
            val bg = findViewById<ImageView>(R.id.openGroupChatBg)
            val yourFilePath = "$filesDir/pics/background.jpg"
            val yourFile = File(yourFilePath)
            if (yourFile.exists()) {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeFile(yourFilePath, options)
                bg.setImageBitmap(bitmap)
            } else {
                if (isDarkMode) {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.group_chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.openGroupInfoMenuButton -> {
                val intent: Intent = Intent(this, OpenGroupInfo::class.java)
                intent.putExtra("groupKey", groupKey)
                    .putExtra("comeFrom", "chat")
                startActivity(intent)
            }
            R.id.backgroundMenuButton -> {
                val intent1 = Intent(this, BackgroundSettings::class.java)
                startActivity(intent1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun sendMessage() {
        val messageText = findViewById<EditText>(R.id.groupMessageText)
        val date = Date()
        val time = SimpleDateFormat("HH:mm").format(date)
        val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
        val mDatabase = FirebaseDatabase.getInstance().reference
        val push = mDatabase.push().key
        val notifyId = generateRandom(15).toInt().toString()
        if (messageText.text.toString().trim { it <= ' ' } != "") {
            val message = messageText.text.toString().trim { it <= ' ' }
            mDatabase.child("groups/$groupKey")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (replying) {
                            val groupMessage = ReplyMessage(
                                myUsername,
                                message,
                                time,
                                messageDate,
                                replyKey,
                                "SENT",
                                notifyId
                            )
                            messageText.text = null
                            mDatabase.child("groups/$groupKey/messages").child(push!!)
                                .setValue(groupMessage)
                                .addOnSuccessListener { aVoid: Void? -> stopReplying() }
                                .addOnFailureListener { e: Exception ->
                                    Toast.makeText(
                                        this@OpenGroupChat, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            val groupMessage =
                                Message(myUsername, message, time, messageDate, "SENT", notifyId)
                            messageText.text = null
                            mDatabase.child("groups/$groupKey/messages").child(push!!)
                                .setValue(groupMessage)
                                .addOnSuccessListener { aVoid: Void? -> }
                                .addOnFailureListener { e: Exception ->
                                    Toast.makeText(
                                        this@OpenGroupChat, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@OpenGroupChat, """
     Fehler! 
     Fehler-Nachricht: ${error.message}
     Fehler-Code: ${error.code}
     """.trimIndent(), Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private inner class MessageHolder {
        var messageTextView: TextView? = null
        var timeTextView: TextView? = null
        var statusTextView: TextView? = null
        var bubble: ConstraintLayout? = null
        var usernameButton: Button? = null
        var layoutBubble: ConstraintLayout? = null
    }

    private inner class ReplyMessageHolder {
        var messageTextView: TextView? = null
        var timeTextView: TextView? = null
        var statusTextView: TextView? = null
        var replyFrom: TextView? = null
        var replyMessage: TextView? = null
        var bubble: FrameLayout? = null
        var layoutBox: ConstraintLayout? = null
        var replyBox: RelativeLayout? = null
        var usernameButton: Button? = null
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
        var notifyId: String
    )

    @IgnoreExtraProperties
    class Message(
        var from: String?,
        var message: String,
        var time: String,
        var date: String,
        var status: String,
        var notifyId: String
    )

    override fun onResume() {
        super.onResume()
        bg
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