package de.ljz.talktome.data.api.responses.register

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
data class RegisterResponse (
    @Json(name = "success")
    @SerializedName("success")
    val success: Boolean
)