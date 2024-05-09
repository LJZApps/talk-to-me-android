package de.ljz.talktome.data.api.responses.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "error_code")
    val errorCode: String? = "unknown_error",

    @Json(name = "error_message")
    val errorMessage: String? = "An unknown error occurred"
)
