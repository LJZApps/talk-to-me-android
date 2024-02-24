package com.lnzpk.chat_app.rewrite.getstarted

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.rewrite.getstarted.pages.GetStartedMain
import com.lnzpk.chat_app.rewrite.getstarted.pages.GetStartedNews
import com.lnzpk.chat_app.rewrite.getstarted.pages.GetStartedSetup
import com.lnzpk.chat_app.rewrite.ui.theme.TalkToMeTheme
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
                ) { innerPadding ->
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val (
                            contentRef
                        ) = createRefs()

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
                            composable(route = GET_STARTED_MAIN) {
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
                        }
                    }
                }
            }
        }
    }
}