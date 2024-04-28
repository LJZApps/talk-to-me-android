package de.ljz.talktome.ui.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph

@NavGraph<RootGraph>(start = true)
annotation class GetStartedNavGraph(
  val start: Boolean = false
)