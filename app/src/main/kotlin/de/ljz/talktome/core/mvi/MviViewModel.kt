package de.ljz.talktome.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<State: Any, Action: Any, Effect: Any>(
  initialState: State
) : ViewModel() {

  private val _state = MutableStateFlow(initialState)
  val state: StateFlow<State> = _state.asStateFlow()

  private val _effect by lazy { Channel<Effect>() }
  val effect: Flow<Effect> by lazy { _effect.receiveAsFlow() }

  val currentState: State
    get() = _state.value

  open fun onAction(action: Action) {}

  protected fun updateState(block: State.() -> State) {
    _state.update { block(it) }
  }

  protected fun sendEffect(effect: Effect) {
    viewModelScope.launch { _effect.send(effect) }
  }
}