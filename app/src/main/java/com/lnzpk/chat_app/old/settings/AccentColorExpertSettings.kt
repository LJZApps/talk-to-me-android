package com.lnzpk.chat_app.old.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import com.lnzpk.chat_app.rewrite.core.ui.components.TextDescription
import com.lnzpk.chat_app.rewrite.core.ui.components.TextTitle
import com.lnzpk.chat_app.rewrite.core.ui.theme.TalkToMeTheme

class AccentColorExpertSettings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DBHelper(this, null)

        setContent {
            TalkToMeTheme {
                Scaffold { innerPadding ->
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val (
                            titleRef,
                            descriptionRef,
                            resetAndBackRef
                        ) = createRefs()

                        TextTitle(
                            text = "This setting has been removed.",
                            modifier = Modifier.constrainAs(titleRef) {
                                top.linkTo(parent.top, 12.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                        )

                        TextDescription(
                            text = "Because we are rebuilding the app from scratch, we have to remove some outdated features so that we can add new ones.\n" +
                                    "With this update, the color settings have been removed.\n" +
                                    "\n" +
                                    "We apologize for any inconvenience.",
                            modifier = Modifier.constrainAs(descriptionRef) {
                                top.linkTo(titleRef.bottom, 6.dp)
                                start.linkTo(parent.start, 12.dp)
                                end.linkTo(parent.end, 12.dp)

                                width = Dimension.fillToConstraints
                            }
                        )

                        Button(
                            onClick = {
                                db.resetColors()

                                finish()
                            },
                            modifier = Modifier.constrainAs(resetAndBackRef) {
                                bottom.linkTo(parent.bottom, 12.dp)
                                start.linkTo(parent.start, 12.dp)
                                end.linkTo(parent.end, 12.dp)

                                width = Dimension.fillToConstraints
                            }
                        ) {
                            Text(text = "Reset colors and go back")
                        }
                    }
                }
            }
        }

        /*
        setContentView(R.layout.accent_color_expert_settings)
        toolbar = findViewById(R.id.accentColorExpertToolbar)

        db = DBHelper(this, null)

        setSupportActionBar(toolbar)
        val bottomView = findViewById<BottomNavigationView>(R.id.accentNavigationView1)
        if (Colors.isDarkMode(this)) {
            bottomView.setBackgroundResource(R.drawable.nav_bar_dark)
        } else {
            bottomView.setBackgroundResource(R.drawable.nav_bar_light)
        }
        toolbar!!.inflateMenu(R.menu.accent_color_expert_menu)
        toolbar!!.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.accentColorExpertSave -> saveConfig()
                R.id.resetExpertAccentColors -> resetColors()
            }
            false
        })
        if (Colors.isDarkMode(this)) {
            toolbar!!.setSubtitle(R.string.colors_darkMode)
        } else {
            toolbar!!.setSubtitle(R.string.colors_lightMode)
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if(db.getSettingBoolean("useAccentColors", false)){
            Colors.setToolbarColor(this, this, toolbar!!)
        }

        config()
         */
    }
    /*
        fun setMessageReplyBubbleColor(replyBubble: RelativeLayout) {
            try {
                if(Colors.isDarkMode(this)){
                    if(db.getColor("messageReplyBubbleColor", "dark").toInt() != 0){
                        replyBubble.backgroundTintList = ColorStateList.valueOf(db.getColor("messageReplyBubbleColor", "dark").toInt())
                        messageReplyBubbleColor = db.getColor("messageReplyBubbleColor", "dark").toInt()
                    }
                }else{
                    if(db.getColor("messageReplyBubbleColor", "light").toInt() != 0){
                        replyBubble.backgroundTintList = ColorStateList.valueOf(db.getColor("messageReplyBubbleColor", "light").toInt())
                        messageReplyBubbleColor = db.getColor("messageReplyBubbleColor", "light").toInt()
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setFabColor(context: Context, fab: FloatingActionButton) {
            var db = DBHelper(context, null)
            try {
                if(Colors.isDarkMode(context)){
                    if(db.getColor("floatingActionButtonColor", "dark").toInt() != 0){
                        fab.backgroundTintList = ColorStateList.valueOf(db.getColor("floatingActionButtonColor", "dark").toInt())
                        fab.rippleColor = db.getColor("floatingActionButtonColor", "dark").toInt()
                    }
                }else{
                    if(db.getColor("floatingActionButtonColor", "light").toInt() != 0){
                        fab.backgroundTintList = ColorStateList.valueOf(db.getColor("floatingActionButtonColor", "light").toInt())
                        fab.rippleColor = db.getColor("floatingActionButtonColor", "light").toInt()
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setButtonColor(context: Context, button: Button) {
            var db = DBHelper(context, null)
            try {
                if(Colors.isDarkMode(context)){
                    if(db.getColor("buttonColor", "dark").toInt() != 0){
                        button.backgroundTintList = ColorStateList.valueOf(db.getColor("buttonColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("buttonColor", "light").toInt() != 0){
                        button.backgroundTintList = ColorStateList.valueOf(db.getColor("buttonColor", "light").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setNavBarColor(context: Context, navigationView: BottomNavigationView) {
            var db = DBHelper(context, null)
            try {
                if(Colors.isDarkMode(context)){
                    if(db.getColor("navigationItemColor", "dark").toInt() != 0){
                        navigationView.itemTextColor = ColorStateList.valueOf(db.getColor("navigationItemColor", "dark").toInt())
                        navigationView.itemIconTintList = ColorStateList.valueOf(db.getColor("navigationItemColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("navigationItemColor", "light").toInt() != 0){
                        navigationView.itemTextColor = ColorStateList.valueOf(db.getColor("navigationItemColor", "light").toInt())
                        navigationView.itemIconTintList = ColorStateList.valueOf(db.getColor("navigationItemColor", "dark").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setToolbarColor(toolbar: Toolbar) {
            var db = DBHelper(this, null)
            try {
                if(Colors.isDarkMode(this)){
                    if(db.getColor("toolbarColor", "dark").toInt() != 0){
                        toolbar.backgroundTintList = ColorStateList.valueOf(db.getColor("toolbarColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("toolbarColor", "light").toInt() != 0){
                        toolbar.backgroundTintList = ColorStateList.valueOf(db.getColor("toolbarColor", "light").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setBadgeColor(context: Context, textView: TextView) {
            var db = DBHelper(context, null)
            try {
                if(Colors.isDarkMode(context)){
                    if(db.getColor("badgeColor", "dark").toInt() != 0){
                        textView.backgroundTintList = ColorStateList.valueOf(db.getColor("badgeColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("badgeColor", "light").toInt() != 0){
                        textView.backgroundTintList = ColorStateList.valueOf(db.getColor("badgeColor", "light").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setMessageReplyColor(constraintLayout: ConstraintLayout) {
            try {
                if(Colors.isDarkMode(this)){
                    if(db.getColor("messageReplyColor", "dark").toInt() != 0){
                        constraintLayout.backgroundTintList = ColorStateList.valueOf(db.getColor("messageReplyColor", "dark").toInt())
                        messageReplyColor = db.getColor("messageReplyColor", "dark").toInt()
                    }
                }else{
                    if(db.getColor("messageReplyColor", "light").toInt() != 0){
                        constraintLayout.backgroundTintList = ColorStateList.valueOf(db.getColor("messageReplyColor", "light").toInt())
                        messageReplyColor = db.getColor("messageReplyColor", "light").toInt()
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setRelativeMessageColor(frameLayout: RelativeLayout) {
            try {
                if (Colors.isDarkMode(this)) {
                    if (db.getColor("messageBubbleColor", "dark").toInt() != 0) {
                        frameLayout.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageBubbleColor", "dark").toInt()
                        )
                    }
                } else {
                    if (db.getColor("messageBubbleColor", "light").toInt() != 0) {
                        frameLayout.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageBubbleColor", "light").toInt()
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setConstraintMessageColor(frameLayout: ConstraintLayout) {
            try {
                if (Colors.isDarkMode(this)) {
                    if (db.getColor("messageBubbleColor", "dark").toInt() != 0) {
                        frameLayout.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageBubbleColor", "dark").toInt()
                        )
                    }
                } else {
                    if (db.getColor("messageBubbleColor", "light").toInt() != 0) {
                        frameLayout.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageBubbleColor", "light").toInt()
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setMessageEtColor(editText: LinearLayout) {
            try {
                if (Colors.isDarkMode(this)) {
                    if (db.getColor("messageEditTextColor", "dark").toInt() != 0) {
                        editText.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageEditTextColor", "dark").toInt()
                        )
                    }
                } else {
                    if (db.getColor("messageEditTextColor", "light").toInt() != 0) {
                        editText.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageEditTextColor", "light").toInt()
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }

        fun setDateMessageBubbleColor(textView: TextView) {
            var db = DBHelper(this, null)
            try {
                if (Colors.isDarkMode(this)) {
                    if (db.getColor("dateGroupMessageColor", "dark").toInt() != 0) {
                        textView.backgroundTintList = ColorStateList.valueOf(db.getColor("dateGroupMessageColor", "dark").toInt()
                        )
                    }
                } else {
                    if (db.getColor("dateGroupMessageColor", "light").toInt() != 0) {
                        textView.backgroundTintList = ColorStateList.valueOf(db.getColor("dateGroupMessageColor", "light").toInt()
                        )
                    }
                }
            } catch (e: Exception) {
            }
            db.close()
        }

        private fun setGroupTextColor(textView: TextView) {
            try {
                if (Colors.isDarkMode(this)){
                    if(db.getColor("groupTextColor", "dark").toInt() != 0){
                        textView.setTextColor(db.getColor("groupTextColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("groupTextColor", "light").toInt() != 0){
                        textView.setTextColor(db.getColor("groupTextColor", "light").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }

        private fun setSwitchColor(switch1: Switch) {
            try {
                if (Colors.isDarkMode(this)){
                    if(db.getColor("switchColor", "dark").toInt() != 0){
                        switch1.trackTintList = ColorStateList.valueOf(db.getColor("switchColor", "dark").toInt())
                        switch1.thumbTintList = ColorStateList.valueOf(db.getColor("switchColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("switchColor", "light").toInt() != 0){
                        switch1.trackTintList = ColorStateList.valueOf(db.getColor("switchColor", "light").toInt())
                        switch1.thumbTintList = ColorStateList.valueOf(db.getColor("switchColor", "light").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }
     */

    /*
    fun resetColors() {
        AlertDialog.Builder(this)
            .setTitle("Akzentfarben zurücksetzen")
            .setMessage("Möchtest du wirklich alle Akzentfarben zurücksetzen?")
            .setPositiveButton("Ja") { dialog: DialogInterface?, which: Int ->
                buttonColor = 0
                badgeColor = 0
                toolbarColor = 0
                floatingActionButtonColor = 0
                switchColor = 0
                seekBarColor = 0
                groupTextColor = 0
                itemColor = 0
                messageBubbleColor = 0
                dateGroupMessageColor = 0
                messageEditTextColor = 0
                messageReplyColor = 0
                messageReplyBubbleColor = 0
                //saveConfig()
            }
            .setNegativeButton("Nein", null)
            .show()
    }
     */

    /*
    fun saveConfig() {
        if (Colors.isDarkMode(this)) {
            try {
                if (buttonColor != 0) {
                    db.saveColor("buttonColor", buttonColor.toString(), "dark")
                } else {
                    db.saveColor("buttonColor", "0", "dark")
                }
                if (toolbarColor != 0) {
                    db.saveColor("toolbarColor", toolbarColor.toString(), "dark")
                } else {
                    db.saveColor("toolbarColor", "0", "dark")
                }
                if (floatingActionButtonColor != 0) {
                    db.saveColor("floatingActionButtonColor", floatingActionButtonColor.toString(), "dark")
                } else {
                    db.saveColor("floatingActionButtonColor", "0", "dark")
                }
                if (seekBarColor != 0) {
                    db.saveColor("seekBarColor", seekBarColor.toString(), "dark")
                } else {
                    db.saveColor("seekBarColor", "0", "dark")
                }
                if (groupTextColor != 0) {
                    db.saveColor("groupTextColor", groupTextColor.toString(), "dark")
                } else {
                    db.saveColor("groupTextColor", "0", "dark")
                }
                if (switchColor != 0) {
                    db.saveColor("switchColor", switchColor.toString(), "dark")
                } else {
                    db.saveColor("switchColor", "0", "dark")
                }
                if (itemColor != 0) {
                    db.saveColor("navigationItemColor", itemColor.toString(), "dark")
                } else {
                    db.saveColor("navigationItemColor", "0", "dark")
                }
                if (messageBubbleColor != 0) {
                    db.saveColor("messageBubbleColor", messageBubbleColor.toString(), "dark")
                } else {
                    db.saveColor("messageBubbleColor", messageBubbleColor.toString(), "dark")
                }
                if (dateGroupMessageColor != 0) {
                    db.saveColor("dateGroupMessageColor", dateGroupMessageColor.toString(), "dark")
                } else {
                    db.saveColor("dateGroupMessageColor", dateGroupMessageColor.toString(), "dark")
                }
                if (messageEditTextColor != 0) {
                    db.saveColor("messageEditTextColor", messageEditTextColor.toString(), "dark")
                } else {
                    db.saveColor("messageEditTextColor", "0", "dark")
                }
                if (messageReplyColor != 0) {
                    db.saveColor("messageReplyColor", messageReplyColor.toString(), "dark")
                } else {
                    db.saveColor("messageReplyColor", "0", "dark")
                }
                if (messageReplyBubbleColor != 0) {
                    db.saveColor("messageReplyBubbleColor", messageReplyBubbleColor.toString(), "dark")
                } else {
                    db.saveColor("messageReplyBubbleColor", "0", "dark")
                }
                if (badgeColor != 0) {
                    db.saveColor("badgeColor", badgeColor.toString(), "dark")
                } else {
                    db.saveColor("badgeColor", "0", "dark")
                }
                Toast.makeText(this, R.string.colors_savedSuccess, Toast.LENGTH_SHORT).show()
                config()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                if (buttonColor != 0) {
                    db.saveColor("buttonColor", buttonColor.toString(), "light")
                } else {
                    db.saveColor("buttonColor", "0", "light")
                }
                if (toolbarColor != 0) {
                    db.saveColor("toolbarColor", toolbarColor.toString(), "light")
                } else {
                    db.saveColor("toolbarColor", "0", "light")
                }
                if (floatingActionButtonColor != 0) {
                    db.saveColor("floatingActionButtonColor", floatingActionButtonColor.toString(), "light")
                } else {
                    db.saveColor("floatingActionButtonColor", "0", "light")
                }
                if (seekBarColor != 0) {
                    db.saveColor("seekBarColor", seekBarColor.toString(), "light")
                } else {
                    db.saveColor("seekBarColor", "0", "light")
                }
                if (groupTextColor != 0) {
                    db.saveColor("groupTextColor", groupTextColor.toString(), "light")
                } else {
                    db.saveColor("groupTextColor", "0", "light")
                }
                if (switchColor != 0) {
                    db.saveColor("switchColor", switchColor.toString(), "light")
                } else {
                    db.saveColor("switchColor", "0", "light")
                }
                if (itemColor != 0) {
                    db.saveColor("navigationItemColor", itemColor.toString(), "light")
                } else {
                    db.saveColor("navigationItemColor", "0", "light")
                }
                if (messageBubbleColor != 0) {
                    db.saveColor("messageBubbleColor", messageBubbleColor.toString(), "light")
                } else {
                    db.saveColor("messageBubbleColor", messageBubbleColor.toString(), "light")
                }
                if (dateGroupMessageColor != 0) {
                    db.saveColor("dateGroupMessageColor", dateGroupMessageColor.toString(), "light")
                } else {
                    db.saveColor("dateGroupMessageColor", dateGroupMessageColor.toString(), "light")
                }
                if (messageEditTextColor != 0) {
                    db.saveColor("messageEditTextColor", messageEditTextColor.toString(), "light")
                } else {
                    db.saveColor("messageEditTextColor", "0", "light")
                }
                if (messageReplyColor != 0) {
                    db.saveColor("messageReplyColor", messageReplyColor.toString(), "light")
                } else {
                    db.saveColor("messageReplyColor", "0", "light")
                }
                if (messageReplyBubbleColor != 0) {
                    db.saveColor("messageReplyBubbleColor", messageReplyBubbleColor.toString(), "light")
                } else {
                    db.saveColor("messageReplyBubbleColor", "0", "light")
                }
                if (badgeColor != 0) {
                    db.saveColor("badgeColor", badgeColor.toString(), "light")
                } else {
                    db.saveColor("badgeColor", "0", "light")
                }
                Toast.makeText(this, R.string.colors_savedSuccess, Toast.LENGTH_SHORT).show()
                config()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
     */

    /*
    fun config() {
        toolbarColor = if(Colors.isDarkMode(this)){
            db.getColor("toolbarColor", "dark").toInt()
        }else{
            db.getColor("toolbarColor", "dark").toInt()
        }

        floatingActionButtonColor = if(Colors.isDarkMode(this)){
            db.getColor("floatingActionButtonColor", "dark").toInt()
        }else{
            db.getColor("floatingActionButtonColor", "light").toInt()
        }

        switchColor = if(Colors.isDarkMode(this)){
            db.getColor("switchColor", "dark").toInt()
        }else{
            db.getColor("switchColor", "light").toInt()
        }

        seekBarColor = if(Colors.isDarkMode(this)){
            db.getColor("seekBarColor", "dark").toInt()
        }else{
            db.getColor("seekBarColor", "light").toInt()
        }

        buttonColor = if(Colors.isDarkMode(this)){
            db.getColor("buttonColor", "dark").toInt()
        }else{
            db.getColor("buttonColor", "light").toInt()
        }

        groupTextColor = if(Colors.isDarkMode(this)){
            db.getColor("groupTextColor", "dark").toInt()
        }else{
            db.getColor("groupTextColor", "light").toInt()
        }

        itemColor = if(Colors.isDarkMode(this)){
            db.getColor("navigationItemColor", "dark").toInt()
        }else{
            db.getColor("navigationItemColor", "light").toInt()
        }

        messageBubbleColor = if(Colors.isDarkMode(this)){
            db.getColor("messageBubbleColor", "dark").toInt()
        }else{
            db.getColor("messageBubbleColor", "light").toInt()
        }

        dateGroupMessageColor = if(Colors.isDarkMode(this)){
            db.getColor("dateGroupMessageColor", "dark").toInt()
        }else{
            db.getColor("dateGroupMessageColor", "light").toInt()
        }

        messageEditTextColor = if(Colors.isDarkMode(this)){
            db.getColor("messageEditTextColor", "dark").toInt()
        }else{
            db.getColor("messageEditTextColor", "light").toInt()
        }

        messageReplyColor = if(Colors.isDarkMode(this)){
            db.getColor("messageReplyColor", "dark").toInt()
        }else{
            db.getColor("messageReplyColor", "light").toInt()
        }

        messageReplyBubbleColor = if(Colors.isDarkMode(this)){
            db.getColor("messageReplyBubbleColor", "dark").toInt()
        }else{
            db.getColor("messageReplyBubbleColor", "light").toInt()
        }

        badgeColor = if(Colors.isDarkMode(this)){
            db.getColor("badgeColor", "dark").toInt()
        }else{
            db.getColor("badgeColor", "light").toInt()
        }

        val send2 = findViewById<FloatingActionButton>(R.id.send2)
        setFabColor(this, send2)

        val accentButton1 = findViewById<Button>(R.id.accentButton1)
        setButtonColor(this, accentButton1)

        val accentToolbar1 = findViewById<Toolbar>(R.id.accentToolbar1)
        setToolbarColor(accentToolbar1)

        val accentFAB1 = findViewById<FloatingActionButton>(R.id.accentFAB1)
        setFabColor(this, accentFAB1)

        val accentSwitch1 = findViewById<Switch>(R.id.accentSwitch1)
        setSwitchColor(accentSwitch1)

        val accentGroupText1 = findViewById<TextView>(R.id.accentGroupText1)
        setGroupTextColor(accentGroupText1)

        val accentNavigationView1 = findViewById<BottomNavigationView>(R.id.accentNavigationView1)
        setNavBarColor(this, accentNavigationView1)

        val accentMessageBubble1 = findViewById<ConstraintLayout>(R.id.outgoing_layout_bubble)
        setConstraintMessageColor(accentMessageBubble1)

        val accentDateGroupMessage1 = findViewById<RelativeLayout>(R.id.accentDateGroupMessage1)
        val accentDateText = findViewById<TextView>(R.id.accentDateTextView)
        setDateMessageBubbleColor(accentDateText)

        val messageBox = findViewById<LinearLayout>(R.id.chatBoxExpertSettings)
        val mockView = findViewById<MockView>(R.id.mockView)

        val replyBox = findViewById<ConstraintLayout>(R.id.expertReplyBox)
        setMessageReplyColor(replyBox)

        val abortReply = findViewById<Button>(R.id.expertAbortReply)
        val replyBubble = findViewById<RelativeLayout>(R.id.replyBox)
        setMessageReplyBubbleColor(replyBubble)

        val replyMessageBubble = findViewById<RelativeLayout>(R.id.expertReplyBubble)
        setRelativeMessageColor(replyMessageBubble)
        setButtonColor(this, abortReply)

        val badgeView = findViewById<TextView>(R.id.badgeColorView)
        setBadgeColor(this, badgeView)

        mockView.setOnClickListener { v: View? ->
            val builder = AlertDialog.Builder(this@AccentColorExpertSettings)
            val pickerView = ColorPickerView(this@AccentColorExpertSettings)
            builder.setTitle("MessageBox color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        setMessageEtColor(messageBox)

        accentButton1.setOnClickListener { v: View? ->
            val pickerView = ColorPickerView(this)
            AlertDialog.Builder(this)
                .setTitle("Button color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        accentToolbar1.setOnClickListener { v: View? ->
            val pickerView = ColorPickerView(this)
            AlertDialog.Builder(this)
                .setTitle("Toolbar color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        accentFAB1.setOnClickListener { v: View? ->
            val pickerView = ColorPickerView(this)
            AlertDialog.Builder(this)
                .setTitle("FloatingActionBar color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        accentSwitch1.isClickable = false
        accentSwitch1.setOnClickListener { v: View? ->
            val pickerView = ColorPickerView(this)
            AlertDialog.Builder(this)
                .setTitle("Switch color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        accentGroupText1.setOnClickListener { v: View? ->
            val pickerView = ColorPickerView(this)
            AlertDialog.Builder(this)
                .setTitle("GroupText color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        accentNavigationView1.setOnNavigationItemSelectedListener { item: MenuItem? ->
            val pickerView = ColorPickerView(this@AccentColorExpertSettings)
            AlertDialog.Builder(this@AccentColorExpertSettings)
                .setTitle("NavigationItem color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            false
        }

        accentMessageBubble1.setOnClickListener { v: View? ->
            val pickerView = ColorPickerView(this)
            AlertDialog.Builder(this)
                .setTitle("MessageBubble color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        accentDateGroupMessage1.setOnClickListener { v: View? ->
            val pickerView = ColorPickerView(this)
            AlertDialog.Builder(this)
                .setTitle("InfoMessage color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        messageBox.setOnClickListener { v: View? ->
            val builder = AlertDialog.Builder(this@AccentColorExpertSettings)
            val pickerView = ColorPickerView(this@AccentColorExpertSettings)
            builder.setTitle("MessageBox color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        replyBox.setOnClickListener { v: View? ->
            val builder = AlertDialog.Builder(this@AccentColorExpertSettings)
            val pickerView = ColorPickerView(this@AccentColorExpertSettings)
            builder.setTitle("ReplyBox color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        replyMessageBubble.setOnClickListener { v: View? ->
            val builder = AlertDialog.Builder(this@AccentColorExpertSettings)
            val pickerView = ColorPickerView(this@AccentColorExpertSettings)
            builder.setTitle("ReplyBubble color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        badgeView.setOnClickListener { v: View? ->
            val builder = AlertDialog.Builder(this@AccentColorExpertSettings)
            val pickerView = ColorPickerView(this@AccentColorExpertSettings)
            builder.setTitle("Badge color")
                .setView(pickerView)
                .setCancelable(false)
                .setPositiveButton(R.string.colors_select) { dialog: DialogInterface?, which: Int ->
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
     */
}