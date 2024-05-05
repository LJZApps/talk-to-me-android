package de.ljz.talktome.core.main

object MainViewContract {
  data class State(
    val isLoggedIn: Boolean = false,
    val isSetupDone: Boolean = false
  )

  sealed interface Action {
    data object GetStartedButtonClicked : Action
  }

  sealed interface Effect {
    data object NavigateLoginScreen : Effect
  }
}