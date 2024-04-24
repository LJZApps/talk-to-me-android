package de.ljz.talktome.ui.features.loginAndRegister

object LoginViewContract {
  data class State(
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoginErrorShown: Boolean = false,
    val loginErrorMessage: String = "",
    val isLoading: Boolean = false,
    val count: Int = 0
  )

  sealed interface Action {
    data object OnOpenRegisterButtonClick : Action
    data object OnOpenLoginButtonClick : Action
    data object OnLoginButtonClick : Action
  }

  sealed interface Effect {
    data object NavigateBack: Effect
    data object NavigateRegisterScreen : Effect
    data object NavigateLoginScreen : Effect
  }
}