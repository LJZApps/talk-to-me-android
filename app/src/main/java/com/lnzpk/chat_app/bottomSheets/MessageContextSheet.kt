package com.lnzpk.chat_app.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors

class MessageContextSheet(val otherUsername: String, val messageKey: String) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.bottom_sheet_concept_2, container, false)

        val viewList = view.findViewById<LinearLayout>(R.id.chatasdildhas)

        //(messageView?.parent as? ViewGroup)?.removeView(messageView)
        //viewList.addView(messageView)

        return view
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