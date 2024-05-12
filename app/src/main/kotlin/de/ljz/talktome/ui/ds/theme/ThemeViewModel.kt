package de.ljz.talktome.ui.ds.theme

import android.content.res.Resources.Theme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ljz.talktome.core.mvi.MviViewModel
import de.ljz.talktome.data.repositories.AppSettingsRepository
import de.ljz.talktome.ui.state.ThemeBehavior
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
  private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {
  private val _themeBehavior = appSettingsRepository.getAppSettings().map { it.themeBehavior }
  var themeBehavior = ThemeBehavior.SYSTEM_STANDARD

  private val _dynamicTheming = appSettingsRepository.getAppSettings().map { it.dynamicThemeColors }
  var dynamicTheming = false

  init {
    viewModelScope.launch {
      _themeBehavior.collectLatest {
        themeBehavior = it
      }
    }

    viewModelScope.launch {
      _dynamicTheming.collectLatest {
        dynamicTheming = it
      }
    }
  }
}