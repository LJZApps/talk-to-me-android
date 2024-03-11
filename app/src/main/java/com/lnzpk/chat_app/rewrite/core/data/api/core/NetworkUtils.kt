package com.lnzpk.chat_app.rewrite.core.data.api.core

import com.squareup.moshi.Moshi
import com.lnzpk.chat_app.rewrite.core.data.api.responses.common.SuccessResponse

object NetworkUtils {

    fun parseSuccessResponse(moshi: Moshi, response: String): SuccessResponse? {
        return try {
            moshi.adapter(SuccessResponse::class.java)?.fromJson(response)
        } catch (e: Exception) {
            null
        }
    }
}