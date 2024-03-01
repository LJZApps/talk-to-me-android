package com.lnzpk.chat_app.rewrite.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lnzpk.chat_app.rewrite.modules.getstarted.GET_STARTED_MAIN
import com.lnzpk.chat_app.rewrite.modules.getstarted.GET_STARTED_NEWS
import com.lnzpk.chat_app.rewrite.modules.getstarted.GET_STARTED_SETUP
import com.lnzpk.chat_app.rewrite.modules.getstarted.GetStartedViewModel
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.GetStartedMain
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.GetStartedNews
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.GetStartedSetup
import com.lnzpk.chat_app.rewrite.ui.theme.TalkToMeTheme

class ActivityMain : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
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
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = "Talk to me")
                            },
                            actions = {
                                IconButton(onClick = { /* do something */ }) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(10.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        )
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

//                        NavHost(
//                            navController = navController,
//                            modifier = Modifier.constrainAs(contentRef) {
//                                start.linkTo(parent.start)
//                                top.linkTo(parent.top)
//                                end.linkTo(parent.end)
//                                bottom.linkTo(parent.bottom)
//
//                                width = Dimension.matchParent
//                                height = Dimension.fillToConstraints
//                            },
//                            startDestination = GET_STARTED_MAIN
//                        ) {
//                            composable(route = GET_STARTED_MAIN) {
//                                GetStartedMain(
//                                    navController = navController,
//                                    vm = vm
//                                )
//                            }
//                            composable(route = GET_STARTED_NEWS) {
//                                GetStartedNews(
//                                    navController = navController,
//                                    vm = vm
//                                )
//                            }
//                            composable(route = GET_STARTED_SETUP) {
//                                GetStartedSetup(
//                                    navController = navController,
//                                    vm = vm
//                                )
//                            }
//                        }
                    }
                }
            }
        }
    }
}