package com.lnzpk.chat_app.rewrite.core.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph(start = true)
@NavGraph
annotation class GetStartedNavGraph(
    val start: Boolean = false
)