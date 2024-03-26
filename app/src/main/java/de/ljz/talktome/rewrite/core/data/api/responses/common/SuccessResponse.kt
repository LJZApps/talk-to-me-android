package de.ljz.talktome.rewrite.core.data.api.responses.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SuccessResponse(
    @Json(name = "success")
    val success: Boolean?,

    @Json(name = "message")
    val message: String?
)
