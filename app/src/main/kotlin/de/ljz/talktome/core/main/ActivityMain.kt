package de.ljz.talktome.core.main

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import de.ljz.talktome.core.main.MainViewContract.State
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import de.ljz.talktome.core.application.TAG
import de.ljz.talktome.core.mvi.EffectCollector
import de.ljz.talktome.ui.ds.theme.TalkToMeTheme
import de.ljz.talktome.ui.features.getstarted.GetStartedViewModel
import de.ljz.talktome.ui.features.loginandregister.LoginViewModel
import de.ljz.talktome.ui.features.setup.SetupViewModel
import de.ljz.talktome.ui.navigation.NavGraphs
import io.sentry.Sentry

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val snackbarHostState = remember { SnackbarHostState() }
      val navController = rememberNavController()
      val vm: AppViewModel by viewModels()

      val uiState: androidx.compose.runtime.State<State> = vm.state.collectAsStateWithLifecycle()
      val isSetupDone by uiState.value.isSetupDone.collectAsState(initial = false)
      val isLoggedIn = uiState.value.isLoggedIn

      TalkToMeTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
          DestinationsNavHost(
            navGraph = NavGraphs.root,
            navController = navController,
            dependenciesContainerBuilder = {
              dependency(NavGraphs.getStarted) {
                val parentEntry = remember(navBackStackEntry) {
                  navController.getBackStackEntry(NavGraphs.getStarted.route)
                }
                hiltViewModel<GetStartedViewModel>(parentEntry)
              }
              dependency(NavGraphs.loginAndRegister) {
                val parentEntry = remember(navBackStackEntry) {
                  navController.getBackStackEntry(NavGraphs.loginAndRegister.route)
                }
                hiltViewModel<LoginViewModel>(parentEntry)
              }
              dependency(NavGraphs.setup) {
                val parentEntry = remember(navBackStackEntry) {
                  navController.getBackStackEntry(NavGraphs.setup.route)
                }
                hiltViewModel<SetupViewModel>(parentEntry)
              }
            },
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            //                                              Setup/Login done     Login done (no setup)        Not logged in
            startRoute = if (isLoggedIn) if (isSetupDone) NavGraphs.getStarted else NavGraphs.setup else NavGraphs.loginAndRegister
          )
        }
      }
    }
  }
}