package de.ljz.talktome.old.customThings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors

class chatPreference : Preference {
    var time: CharSequence? = null
    private var unreadNumber: CharSequence? = null
    private var verified = false

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context!!, attrs, defStyleAttr, defStyleRes
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?) : super(context!!) {
        verified = false
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        layoutResource = if (Colors.isDarkMode(context)) {
            R.layout.chat_preference_design_dark
        } else {
            R.layout.chat_preference_design_light
        }
        val textView = holder.itemView.findViewById<TextView>(R.id.timeTextViewChat)
        val summary = holder.itemView.findViewById<TextView>(android.R.id.summary)
        val verifiedImage = holder.itemView.findViewById<ImageView>(R.id.verifiedImage)

        if (textView != null) {
            if (Colors.isDarkMode(context)) {
                summary.setTextColor(context.getColor(android.R.color.secondary_text_dark))
                textView.setTextColor(context.getColor(android.R.color.secondary_text_dark))
            } else {
                summary.setTextColor(context.getColor(android.R.color.tertiary_text_light))
                textView.setTextColor(context.getColor(android.R.color.secondary_text_light))
            }
            textView.text = time
        }
        val unreadView = holder.itemView.findViewById<TextView>(R.id.unreadChatNumber)
        if (unreadView != null) {
            if (unreadNumber === "0" || unreadNumber == null) {
                unreadView.visibility = View.GONE
            } else {
                if (unreadNumber.toString().toInt() >= 100) {
                    unreadView.visibility = View.VISIBLE
                    unreadView.text = "99+"
                } else {
                    unreadView.visibility = View.VISIBLE
                    unreadView.text = ""
                }
            }
        }
        if (verifiedImage != null) {
            if (verified) {
                verifiedImage.visibility = View.VISIBLE
            } else {
                verifiedImage.visibility = View.GONE
            }
        }
    }

    @JvmName("setUnreadNumber1")
    fun setUnreadNumber(number: CharSequence?) {
        unreadNumber = number
        notifyChanged()
    }

    @JvmName("setTime1")
    fun setTime(newTime: CharSequence?) {
        time = newTime
        notifyChanged()
    }

    @JvmName("setVerified1")
    fun setVerified(b: Boolean) {
        verified = b
        notifyChanged()
    }
}