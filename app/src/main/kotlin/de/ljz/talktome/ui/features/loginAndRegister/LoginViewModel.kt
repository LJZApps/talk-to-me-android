package de.ljz.talktome.ui.features.loginAndRegister

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.application.TAG
import de.ljz.talktome.core.coroutine.ContextProvider
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.repositories.LoginRepository
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.Action
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.Effect
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewContract.State
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val loginRepository: LoginRepository,
  private val contextProvider: ContextProvider
) : MviViewModel<State, Action, Effect>(State()) {

  override fun onAction(action: Action) {
    when (action) {
      Action.OnLoginButtonClick -> {
        viewModelScope.launch {
          loginRepository.login(
            username = state.value.username,
            password = state.value.password,
            onSuccess = {
              Log.d(TAG, it.toString())
            },
            onError = {
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

  fun updatePassword(password: String) {
    updateState { copy(password = password) }
  }

  fun updateUsername(username: String) {
    updateState { copy(username = username) }
  }

  fun togglePasswordVisibility() {
    updateState { copy(passwordVisible = !passwordVisible) }
  }

  fun login() {
    viewModelScope.launch {
      loginRepository.login(
        username = "leonzapke@gmail.com",
        password = "Leon.230",
        onSuccess = {
          Log.d(TAG, it.toString())
        },
        onError = {
          updateState { copy(loginErrorMessage = it.localizedMessage) }
          Log.e(TAG, it.message.toString())
        }
      )
    }
  }

  fun register() {
    viewModelScope.launch {
      loginRepository.register(
        displayName = state.value.username,
        username = "LnZpk",
        onSuccess = { response ->
          Log.d(TAG, response.toString())
        },
        onError = { exception ->
          Log.d(TAG, exception.message.toString())
        }
      )
    }
  }
}