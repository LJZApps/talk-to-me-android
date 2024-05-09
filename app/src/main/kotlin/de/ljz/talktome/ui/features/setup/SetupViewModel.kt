package de.ljz.talktome.ui.features.setup

import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.repositories.AppSettingsRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
) :MviViewModel<SetupViewContract.State, SetupViewContract.Action, SetupViewContract.Effect>(SetupViewContract.State()) {
    init {
        appSettingsRepository.getAppSettings().map { it.setupDone }
    }
}