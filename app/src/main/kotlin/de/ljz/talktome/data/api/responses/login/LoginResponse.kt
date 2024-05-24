package de.ljz.talktome.data.api.responses.login

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
data class LoginResponse(
  @Json(name = "success")
  val success: Boolean,

  @Json(name = "access_token")
  val accessToken: AccessToken,

  @Json(name = "refresh_token")
  val refreshToken: RefreshToken
)

@JsonClass(generateAdapter = true)
data class AccessToken(
  @Json(name = "token")
  val token: String,

  @Json(name = "exp")
  val exp: Long
)

@JsonClass(generateAdapter = true)
data class RefreshToken(
  @Json(name = "token")
  val token: String,

  @Json(name = "exp")
  val exp: Long
)