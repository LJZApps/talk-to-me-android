package de.ljz.talktome.data.repositories

import android.util.Log
import de.ljz.talktome.core.application.TAG
import de.ljz.talktome.data.api.core.ApiClient
import de.ljz.talktome.data.api.core.exceptions.RequestFailedException
import de.ljz.talktome.data.api.responses.login.LoginResponse
import de.ljz.talktome.data.api.responses.register.RegisterResponse
import de.ljz.talktome.data.sharedpreferences.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
  private val apiClient: ApiClient,
  private val sessionManager: SessionManager
): BaseRepository() {

  suspend fun login(
    username: String,
    password: String,
    onSuccess: (suspend (LoginResponse) -> Unit)? = null,
    onError: (suspend (RequestFailedException) -> Unit)? = null
  ) {
    apiClient.call(
      block = {
        Log.d(TAG, "login: $username $password")
          apiClient.loginService.login(username, password)
      },
      onSuccess = { response ->
        sessionManager.setAccessToken(response.accessToken.token)
        sessionManager.setRefreshToken(response.refreshToken.token)
        sessionManager.setExpirationTime(response.accessToken.exp)

        onSuccess?.invoke(response)
      },
      onError = {
        onError?.invoke(it)
      }
    )
  }

  suspend fun register(
    displayName: String,
    username: String,
    biography: String? = null,
    onSuccess: (suspend (RegisterResponse) -> Unit)? = null,
    onError: (suspend (Exception) -> Unit)? = null
  ) {
    apiClient.call(
      block = {
        apiClient.loginService.register(
          displayName = displayName,
          username = username,
          biography = biography,
        )
      },
      onSuccess = { response ->
        onSuccess?.invoke(response)
      },
      onError = { exception ->
        onError?.invoke(exception)
      }
    )
  }
}