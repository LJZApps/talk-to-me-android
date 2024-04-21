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
  val success: Boolean,

  @Json(name = "access_token")
  @SerializedName("access_token")
  val accessToken: AccessToken,

  @Json(name = "refresh_token")
  @SerializedName("refresh_token")
  val refreshToken: RefreshToken
)

@JsonClass(generateAdapter = true)
@Serializable
data class AccessToken(
  @Json(name = "token")
  @SerializedName("token")
  val token: String,

  @Json(name = "exp")
  @SerializedName("exp")
  val exp: Long
)

@JsonClass(generateAdapter = true)
@Serializable
data class RefreshToken(
  @Json(name = "token")
  @SerializedName("token")
  val token: String,

  @Json(name = "exp")
  @SerializedName("exp")
  val exp: Long
)