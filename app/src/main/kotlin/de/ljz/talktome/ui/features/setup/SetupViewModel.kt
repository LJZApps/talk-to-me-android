package de.ljz.talktome.ui.features.setup

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.repositories.AppSettingsRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import de.ljz.talktome.ui.features.setup.SetupViewContract.State
import de.ljz.talktome.ui.features.setup.SetupViewContract.Action
import de.ljz.talktome.ui.features.setup.SetupViewContract.Effect
import de.ljz.talktome.ui.state.ThemeBehavior
import kotlinx.coroutines.launch

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
) : MviViewModel<State, Action, Effect>(State()) {
    override fun onAction(action: Action) {
        when (action) {
            Action.ChangeTheme -> {
                viewModelScope.launch {
                    appSettingsRepository.setDarkModeBehavior(ThemeBehavior.SYSTEM_STANDARD)
                }
            }
            else -> {}
        }
    }
}