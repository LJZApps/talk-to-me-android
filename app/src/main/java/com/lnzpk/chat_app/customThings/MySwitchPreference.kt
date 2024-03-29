package com.lnzpk.chat_app.customThings

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.Switch
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.newDatabase.DBHelper

class MySwitchPreference : SwitchPreference {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val db = DBHelper(context, null)

        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                val switchElement = holder.findViewById(R.id.switch_widget) as Switch
                if (Colors.isDarkMode(context)){
                    if(db.getColor("switchColor", "dark").toInt() != 0){
                        switchElement.trackTintList = ColorStateList.valueOf(db.getColor("switchColor", "dark").toInt())
                        switchElement.thumbTintList = ColorStateList.valueOf(db.getColor("switchColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("switchColor", "light").toInt() != 0){
                        switchElement.trackTintList = ColorStateList.valueOf(db.getColor("switchColor", "light").toInt())
                        switchElement.thumbTintList = ColorStateList.valueOf(db.getColor("switchColor", "light").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}