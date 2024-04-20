package de.ljz.talktome.data.api.responses.login

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
data class LoginResponse(
    @Json(name = "success")
    @SerializedName("success")
    val success: Boolean
)
