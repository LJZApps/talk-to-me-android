package de.ljz.talktome.ui.features.loginAndRegister

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.application.TAG
import de.ljz.talktome.core.coroutine.ContextProvider
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.repositories.LoginRepository
import de.ljz.talktome.data.sharedpreferences.SessionManager
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.Action
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.Effect
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.State
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val loginRepository: LoginRepository,
  private val contextProvider: ContextProvider,
  private val sessionManager: SessionManager
) : MviViewModel<State, Action, Effect>(State()) {

  override fun onAction(action: Action) {
    when (action) {
      Action.OnLoginButtonClick -> {
        updateState { copy(isLoading = true, loadingText = "Logging in") }
        Handler(Looper.getMainLooper()).postDelayed(
          {
            updateState { copy(loadingText = "Loading profile data") }
          },
          1000 // value in milliseconds
        )
        viewModelScope.launch {
          loginRepository.login(
            username = state.value.username,
            password = state.value.password,
            onSuccess = {
              sessionManager.setAccessToken(it.accessToken.token)
              sessionManager.setRefreshToken(it.refreshToken.token)
              sessionManager.setExpirationTime(it.accessToken.exp)
              updateState { copy(loadingText = "Loading profile data") }
              sendEffect(Effect.NavigateSetupScreen)
            },
            onError = {
              updateState { copy(isLoading = false, loginErrorMessage = it.message.toString(), isLoginErrorShown = true) }
              Log.e(TAG, "${it.message}")
            }
          )
        }
      }
      Action.OnOpenRegisterButtonClick -> {
        updateState { copy(count=2) }
        sendEffect(Effect.NavigateRegisterScreen)
      }
      Action.OnOpenLoginButtonClick -> {
        sendEffect(Effect.NavigateLoginScreen)
      }
    }
  }

  fun dismissDialog() {
    updateState { copy(isLoginErrorShown = false, loginErrorMessage = "") }
  }

  fun updatePassword(password: String) {
    updateState { copy(password = password) }
  }

  fun updateUsername(username: String) {
    updateState { copy(username = username) }
  }

  fun togglePasswordVisibility() {
    updateState { copy(passwordVisible = !passwordVisible) }
  }
}