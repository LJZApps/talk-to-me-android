package de.ljz.talktome.core.application

import de.ljz.talktome.BuildConfig
import javax.inject.Inject

class AppConfig @Inject constructor() {
  fun isDebugMode() = BuildConfig.DEBUG
}