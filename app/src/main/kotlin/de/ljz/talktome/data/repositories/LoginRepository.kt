package de.ljz.talktome.data.repositories

import android.util.Log
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import de.ljz.talktome.core.application.TAG
import de.ljz.talktome.data.api.core.ApiClient
import de.ljz.talktome.data.api.responses.common.ErrorResponse
import de.ljz.talktome.data.api.responses.login.LoginResponse
import de.ljz.talktome.data.api.responses.register.RegisterResponse
import de.ljz.talktome.data.mapper.ErrorResponseMapper
import de.ljz.talktome.data.sharedpreferences.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
  private val apiClient: ApiClient,
  private val sessionManager: SessionManager
) : BaseRepository() {

  suspend fun login(
    username: String,
    password: String,
    onSuccess: (suspend (LoginResponse) -> Unit)? = null,
    onError: (suspend (ErrorResponse) -> Unit)? = null
  ) {
    apiClient.loginService.login(username, password)
      .suspendOnSuccess {
        sessionManager.setAccessToken(data.accessToken.token)
        sessionManager.setRefreshToken(data.refreshToken.token)
        sessionManager.setExpirationTime(data.accessToken.exp)

        onSuccess?.invoke(data)
      }
      .suspendOnError(ErrorResponseMapper) {
        onError?.invoke(this)
      }
      .suspendOnException {
        this.run {
          Log.d(TAG, message.toString())
        }
      }
  }

  suspend fun register(
    displayName: String,
    username: String,
    biography: String? = null,
    onSuccess: (suspend (RegisterResponse) -> Unit)? = null,
    onError: (suspend (ErrorResponse) -> Unit)? = null
  ) {
    apiClient.loginService.register(displayName, username, biography)
      .suspendOnSuccess {
        onSuccess?.invoke(data)
      }
      .suspendOnError(ErrorResponseMapper) {
        onError?.invoke(this)
      }
  }
}