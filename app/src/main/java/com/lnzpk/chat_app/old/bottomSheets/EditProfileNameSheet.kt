package com.lnzpk.chat_app.old.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.colors.Colors
import com.lnzpk.chat_app.old.colors.Colors.setButtonColor
import com.lnzpk.chat_app.old.newDatabase.DBHelper

class EditProfileNameSheet(val username: String): BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.edit_profile_name_sheet, container, false)

        val button = view.findViewById<Button>(R.id.editProfileNameSheetSaveButton)
        val input = view.findViewById<TextInputLayout>(R.id.editProfileNameSheetInput)
        val db = DBHelper(requireContext(), null)

        setButtonColor(requireContext(), button)

        input.editText!!.setText(db.getProfileName(username))

        button.setOnClickListener {
            val newName = input.editText!!.text.toString().trim()

            db.changeProfileName(username, newName)

            dismiss()
        }

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