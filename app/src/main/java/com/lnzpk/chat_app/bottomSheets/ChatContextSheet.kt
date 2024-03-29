package com.lnzpk.chat_app.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors

class ChatContextSheet(val chatUsername: String): BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.bottom_sheet_chat_context, container, false)

        val holder = PreferenceHolder()

        holder.layout = view.findViewById(R.id.chatSheetChatLayout)
        holder.icon = view.findViewById(R.id.chatSheetProfilePic)
        holder.title = view.findViewById(R.id.chatSheetProfileTitle)
        holder.summary = view.findViewById(R.id.chatSheetProfileSummary)
        holder.time = view.findViewById(R.id.chatSheetProfileTime)
        holder.unreadMessages = view.findViewById(R.id.chatSheetUnreadChatNumber)
        holder.verifiedImage = view.findViewById(R.id.chatSheetVerifiedImage)
        holder.emojiBadge = view.findViewById(R.id.chatSheetEmojiBadge)

        view.tag = holder

        if (Colors.isDarkMode(requireContext())) {
            holder.layout!!.background = ContextCompat.getDrawable(requireContext(), R.drawable.chat_background_dark)
            holder.summary!!.setTextColor(requireContext().getColor(R.color.secondary_text_dark))
            holder.time!!.setTextColor(requireContext().getColor(R.color.secondary_text_dark))
            holder.title!!.setTextColor(requireContext().getColor(android.R.color.white))
        }

        holder.title!!.text = chatUsername
        //TODO("Profile request to get data")
        holder.summary!!.text = "PLACEHOLDER"

        //dismiss()

        //(messageView?.parent as? ViewGroup)?.removeView(messageView)
        //viewList.addView(messageView)

        return view
    }

    inner class PreferenceHolder {
        var layout: ConstraintLayout? = null
        var icon: ImageView? = null
        var title: TextView? = null
        var summary: TextView? = null
        var time: TextView? = null
        var unreadMessages: TextView? = null
        var verifiedImage: ImageView? = null
        var emojiBadge: TextView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Colors.isDarkMode(requireContext())){
            setStyle(STYLE_NORMAL, R.style.sheetDark)
        }else{
            setStyle(STYLE_NORMAL, R.style.sheetLight)
        }
    }
}