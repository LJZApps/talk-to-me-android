package com.lnzpk.chat_app.rewrite.app.module.login.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lnzpk.chat_app.rewrite.app.module.getstarted.GetStartedViewModel
import com.lnzpk.chat_app.rewrite.app.module.login.LoginViewModel
import com.lnzpk.chat_app.rewrite.core.navigation.LoginNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@LoginNavGraph(start = true)
@Destination
@Composable
fun LoginMain(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
    vm: LoginViewModel
) {

}