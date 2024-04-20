package de.ljz.talktome.ui.features.loginAndRegister

object LoginViewContract {
  data class State(
    val username: String = "",
    val password: String = "",
    val count: Int = 0
  )

  sealed interface Action {
    data object OnLoginButtonClick : Action
    data object OnOpenRegisterButtonClick : Action
  }

  sealed interface Effect {
    data object NavigateBack: Effect
    data object NavigateRegisterScreen : Effect
  }
}