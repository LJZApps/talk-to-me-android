package de.ljz.talktome.data.api.core

import com.squareup.moshi.Moshi
import de.ljz.talktome.data.api.responses.common.SuccessResponse

object NetworkUtils {

  fun parseSuccessResponse(moshi: Moshi, response: String): SuccessResponse? {
    return try {
      moshi.adapter(SuccessResponse::class.java)?.fromJson(response)
    } catch (e: Exception) {
      null
    }
  }
}