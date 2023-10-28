package com.lnzpk.chat_app.old.customThings

import android.R
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.lnzpk.chat_app.old.colors.Colors.isDarkMode
import com.lnzpk.chat_app.old.newDatabase.DBHelper

class MyPreferenceCategory : PreferenceCategory {
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        PreferenceManager.getDefaultSharedPreferences(context)
        val db = DBHelper(context, null)
        if (db.getSettingBoolean("useAccentColors", false)) {
            try {
                var titleView: TextView = holder.findViewById(R.id.title) as TextView
                if (isDarkMode(context)){
                    if(db.getColor("groupTextColor", "dark").toInt() != 0){
                        titleView.setTextColor(db.getColor("groupTextColor", "dark").toInt())
                    }
                }else{
                    if(db.getColor("groupTextColor", "light").toInt() != 0){
                        titleView.setTextColor(db.getColor("groupTextColor", "light").toInt())
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}