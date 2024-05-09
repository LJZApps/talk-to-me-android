package de.ljz.talktome.ui.features.loginandregister

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.coroutine.ContextProvider
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.repositories.LoginRepository
import de.ljz.talktome.data.sharedpreferences.SessionManager
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.Action
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.Effect
import de.ljz.talktome.ui.features.loginandregister.LoginViewContract.State
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
      Action.OnLoginButtonClick -> login()

      Action.OnOpenRegisterButtonClick -> {
        sendEffect(Effect.NavigateRegisterScreen)
      }

      Action.OnOpenLoginButtonClick -> {
        sendEffect(Effect.NavigateLoginScreen)
      }
    }
  }

  private fun login() {
    updateState { copy(isLoading = true, loadingText = "Logging in") }
    viewModelScope.launch {
      loginRepository.login(
        username = state.value.username,
        password = state.value.password,
        onSuccess = {
          if (it.success) {
            sessionManager.setAccessToken(it.accessToken.token)
            sessionManager.setRefreshToken(it.refreshToken.token)
            sessionManager.setExpirationTime(it.accessToken.exp)

            updateState {
              copy(
                loadingText = "Done! Now setup your app!"
              )
            }

            sendEffect(Effect.NavigateSetupScreen)
          }
        },
        onError = {
          updateState {
            copy(
              isLoading = false,
              loginErrorMessage = it.errorMessage.toString(),
              isLoginErrorShown = true,
              loadingText = ""
            )
          }
        }
      )
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