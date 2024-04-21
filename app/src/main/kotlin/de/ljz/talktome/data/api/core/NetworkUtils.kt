package de.ljz.talktome.data.api.core

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import de.ljz.talktome.BuildConfig
import de.ljz.talktome.core.application.TAG
import de.ljz.talktome.data.api.responses.common.SuccessResponse
import de.ljz.talktome.data.api.responses.login.LoginResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object NetworkUtils {

  fun parseSuccessResponse(moshi: Moshi, response: String): SuccessResponse? {
    return try {
      moshi.adapter(SuccessResponse::class.java)?.fromJson(response)
    } catch (e: Exception) {
      null
    }
  }

  suspend fun loginWithRefreshToken(refreshToken: String): LoginResponse? {
    val httpClient = HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
          }
        )
      }
    }

    return httpClient.request(
      builder = HttpRequestBuilder().apply {
        method = HttpMethod.Post
        url("${BuildConfig.BASE_URL}/api/oauth/token")
        parameter("refresh_token", refreshToken)
        parameter("grant_type", "refresh_token")
      }
    ).let { response ->
      try {
        response.body<LoginResponse>()
      } catch (e: Exception) {
        Log.e(TAG, "Error while refreshing access token: ${e.stackTraceToString()}\nReceived response: ${response.bodyAsText()}")
        null
      }
    }
  }
}