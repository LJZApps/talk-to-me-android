package de.ljz.talktome.rewrite.core.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@NavGraph
annotation class LoginNavGraph(
    val start: Boolean = false
)