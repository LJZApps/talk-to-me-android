package de.ljz.talktome.core.main

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.main.MainViewContract.Action
import de.ljz.talktome.core.main.MainViewContract.Effect
import de.ljz.talktome.core.main.MainViewContract.State
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.repositories.AppSettingsRepository
import de.ljz.talktome.data.sharedpreferences.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
  private val sessionManager: SessionManager,
  private val appSettingsRepository: AppSettingsRepository,
) : MviViewModel<State, Action, Effect>(State()) {
  init {
    updateState {
      copy(
        isLoggedIn = sessionManager.isAccessTokenPresent(),
        isSetupDone = appSettingsRepository.getAppSettings().map { it.setupDone }
      )
    }
  }
}