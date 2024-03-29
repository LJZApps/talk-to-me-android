package com.lnzpk.chat_app.colors

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lnzpk.chat_app.newDatabase.DBHelper

object Colors {

    fun setToolbarColor(activity: Activity, context: Context, toolbar: Toolbar) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if(isDarkMode(context)){
                    if(db.getColor("toolbarColor", "dark").toInt() != 0){
                        toolbar.backgroundTintList = ColorStateList.valueOf(db.getColor("toolbarColor", "dark").toInt())
                        val window = activity.window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.statusBarColor = db.getColor("toolbarColor", "dark").toInt()
                    }
                }else{
                    if(db.getColor("toolbarColor", "light").toInt() != 0){
                        toolbar.backgroundTintList = ColorStateList.valueOf(db.getColor("toolbarColor", "light").toInt())
                        val window = activity.window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.statusBarColor = db.getColor("toolbarColor", "dark").toInt()
                    }
                }
            } catch (e: Exception) {
            }
        }
        db.close()
    }

    fun setNavBarColor(context: Context, navigationView: BottomNavigationView) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if(isDarkMode(context)){
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
        db.close()
    }

    fun setBadgeColor(context: Context, textView: TextView) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if(isDarkMode(context)){
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
        db.close()
    }

    fun setButtonColor(context: Context, button: Button) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if(isDarkMode(context)){
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
        db.close()
    }

    fun setFabColor(context: Context, fab: FloatingActionButton) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if(isDarkMode(context)){
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
        db.close()
    }

    fun setSwitchColor(context: Context, switch1: Switch) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if(isDarkMode(context)){
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
        db.close()
    }

    fun setMessageReplyBubbleColor(context: Context, replyBubble: RelativeLayout) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if (isDarkMode(context)) {
                    if (db.getColor("messageReplyBubbleColor", "dark").toInt() != 0) {
                        replyBubble.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageReplyBubbleColor", "dark").toInt()
                        )
                    }
                } else {
                    if (db.getColor("messageReplyBubbleColor", "light").toInt() != 0) {
                        replyBubble.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageReplyBubbleColor", "light").toInt()
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }
        db.close()
    }

    fun setMessageReplyColor(context: Context, constraintLayout: ConstraintLayout) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if (isDarkMode(context)) {
                    if (db.getColor("messageReplyColor", "dark").toInt() != 0) {
                        constraintLayout.backgroundTintList =
                            ColorStateList.valueOf(db.getColor("messageReplyColor", "dark").toInt())
                    }
                } else {
                    if (db.getColor("messageReplyColor", "light").toInt() != 0) {
                        constraintLayout.backgroundTintList = ColorStateList.valueOf(
                            db.getColor("messageReplyColor", "light").toInt()
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }
        db.close()
    }

    fun setMessageEtColor(context: Context, editText: LinearLayout) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if (isDarkMode(context)) {
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
        db.close()
    }

    fun setConstraintMessageColor(context: Context, frameLayout: ConstraintLayout) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if (isDarkMode(context)) {
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
        db.close()
    }

    fun setDateMessageBubbleColor(context: Context, textView: TextView) {
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                if (isDarkMode(context)) {
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
        }
        db.close()
    }

    fun isDarkMode(context: Context): Boolean {
        var darkMode = false
        val db = DBHelper(context, null)
        when (db.getSettingString("app_theme", "system")) {
            "system" -> {
                when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
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
        db.close()
        return darkMode
    }
}