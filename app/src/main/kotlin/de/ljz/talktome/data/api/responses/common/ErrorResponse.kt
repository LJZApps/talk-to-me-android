package de.ljz.talktome.data.api.responses.common

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    @SerializedName("error_code")
    val errorCode: Int? = 200,

    @SerializedName("error_message")
    val errorMessage: String? = "An unknown error occurred"
)
