package com.lnzpk.chat_app.rewrite.modules.getstarted

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.lnzpk.chat_app.rewrite.modules.getstarted.components.BottomBar
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.GetStartedMain
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.NavGraphs
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.destinations.GetStartedMainDestination
import com.lnzpk.chat_app.rewrite.ui.theme.TalkToMeTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "ActivityGetStarted"

@AndroidEntryPoint
class ActivityGetStarted : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val navController = rememberNavController()
            val vm: GetStartedViewModel = hiltViewModel()

            TalkToMeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    /*
                    bottomBar = {
                        BottomBar(navController = navController)
                    }
                     */
                ) { innerPadding ->
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val (
                            contentRef
                        ) = createRefs()

                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController
                        ) {
                            composable(GetStartedMainDestination) {
                                GetStartedMain(navigator = destinationsNavigator)
                            }
                        }

                        /*
                        NavHost(
                            navController = navController,
                            modifier = Modifier.constrainAs(contentRef) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)

                                width = Dimension.matchParent
                                height = Dimension.fillToConstraints
                            },
                            startDestination = GET_STARTED_MAIN
                        ) {
                            /*
                            composable(route = Navigation.GetStarted.TEST) {
                                GetStartedMain(
                                    navController = navController,
                                    vm = vm
                                )
                            }
                            composable(route = GET_STARTED_NEWS) {
                                GetStartedNews(
                                    navController = navController,
                                    vm = vm
                                )
                            }
                            composable(route = GET_STARTED_SETUP) {
                                GetStartedSetup(
                                    navController = navController,
                                    vm = vm
                                )
                            }
                             */
                        }
                         */
                    }
                }
            }
        }
    }
}