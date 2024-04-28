package de.ljz.talktome.ui.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph

@NavGraph<RootGraph>
annotation class LoginAndRegisterNavGraph(
  val start: Boolean = false
)