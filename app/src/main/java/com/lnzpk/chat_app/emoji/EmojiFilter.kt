package com.lnzpk.chat_app.emoji

import android.text.InputFilter
import android.text.Spanned

class EmojiFilter : InputFilter {

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        for( i in start until end) {
            var type = Character.getType(source!![i]).toByte()
            if(type != Character.SURROGATE && type != Character.OTHER_SYMBOL) {
                return "";
            }
        }
        return null;
    }
}