package com.lnzpk.chat_app.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors

class MyBottomSheetDialogFragment(val username: String, val messageView: View): BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.bottom_sheet_concept_2, container, false)

        val viewList = view.findViewById<LinearLayout>(R.id.chatasdildhas)

        var testText = TextView(requireContext())
        testText.text = "This is a test."

        viewList.addView(testText)

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

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityCreated(savedInstanceState)",
        "com.google.android.material.bottomsheet.BottomSheetDialogFragment"
    )
    )
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*

        val options = listOf<String>(
            "Share with Friends",
            "Bookmark",
            "Add to Favourites",
            "More Information"
        )

        listViewOptions.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            options
        )
         */
    }
}