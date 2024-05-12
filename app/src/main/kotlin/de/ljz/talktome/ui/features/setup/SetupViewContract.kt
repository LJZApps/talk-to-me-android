package de.ljz.talktome.ui.features.setup

object SetupViewContract {
    data class State(
        val theme: String = "system"
    )

    sealed interface Action {
        data object ChangeTheme : Action
    }

    sealed interface Effect {

    }
}