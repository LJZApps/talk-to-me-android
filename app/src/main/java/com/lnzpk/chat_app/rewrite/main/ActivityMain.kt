package com.lnzpk.chat_app.rewrite.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.lnzpk.chat_app.rewrite.app.module.NavGraphs
import com.lnzpk.chat_app.rewrite.app.module.destinations.GetStartedMainDestination
import com.lnzpk.chat_app.rewrite.app.module.getstarted.GetStartedViewModel
import com.lnzpk.chat_app.rewrite.app.module.login.LoginViewModel
import com.lnzpk.chat_app.rewrite.core.navigation.GetStartedNavGraph
import com.lnzpk.chat_app.rewrite.core.navigation.LoginNavGraph
import com.lnzpk.chat_app.rewrite.core.ui.theme.TalkToMeTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val navController = rememberNavController()
            val vm: AppViewModel by viewModels()
            val tooltipState = rememberTooltipState(isPersistent = true)

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
                            dependency(NavGraphs.login) {
                                val parentEntry = remember(navBackStackEntry) {
                                    navController.getBackStackEntry(NavGraphs.login.route)
                                }
                                hiltViewModel<LoginViewModel>(parentEntry)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        startRoute = if (isLoggedIn.value) NavGraphs.getStarted else NavGraphs.login
                    )
                }
            }
        }
    }
}