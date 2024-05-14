package de.ljz.talktome.core.provider

object ModelProvider {
  val Factory = viewModelFactory {
    initializer {
      //Not yet implemented
      ThemeModel(appViewModelProvider().storeRepository)
    }

    initializer {
      //Not yet implemented
      SettingsModel(appViewModelProvider().storeRepository)
    }
  }
}


fun CreationExtras.appViewModelProvider(): ApplicationSetup =
  (this[AndroidViewModelFactory.APPLICATION_KEY] as ApplicationSetup)