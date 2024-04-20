package de.ljz.talktome.ui.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@NavGraph
annotation class LoginAndRegisterNavGraph(
  val start: Boolean = false
)