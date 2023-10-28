package com.lnzpk.chat_app.old.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.colors.Colors

class MessageContextSheet(val otherUsername: String) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_concept_2, container, false)
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