package de.ljz.talktome.core.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import de.ljz.talktome.ui.ds.theme.TalkToMeTheme
import de.ljz.talktome.ui.features.NavGraphs
import de.ljz.talktome.ui.features.destinations.LoginAndRegisterScreenDestination
import de.ljz.talktome.ui.features.getstarted.GetStartedViewModel
import de.ljz.talktome.ui.features.loginAndRegister.LoginViewModel

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val snackbarHostState = remember { SnackbarHostState() }
      val navController = rememberNavController()
      val vm: AppViewModel by viewModels()

      val isLoggedIn = vm.isLoggedIn.collectAsState()

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
            },
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            startRoute = if (isLoggedIn.value) NavGraphs.getStarted else NavGraphs.loginAndRegister
          )
        }
      }
    }
  }
}