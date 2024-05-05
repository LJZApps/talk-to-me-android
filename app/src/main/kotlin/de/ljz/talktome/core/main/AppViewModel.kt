package de.ljz.talktome.core.main

import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.main.MainViewContract.Action
import de.ljz.talktome.core.main.MainViewContract.Effect
import de.ljz.talktome.core.main.MainViewContract.State
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.sharedpreferences.SessionManager
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
  private val sessionManager: SessionManager
) : MviViewModel<State, Action, Effect>(
  MainViewContract.State()
) {
  init {
    updateState { copy(isLoggedIn = sessionManager.isAccessTokenPresent()) }
  }

  override fun onAction(action: Action) {
    when (action) {
      Action.GetStartedButtonClicked -> {
        sendEffect(Effect.NavigateLoginScreen)
      }
    }
  }
  // Function to check, if a user is logged in, is coming soon.
}