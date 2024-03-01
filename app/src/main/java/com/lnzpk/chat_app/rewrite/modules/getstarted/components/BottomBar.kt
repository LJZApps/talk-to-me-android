package com.lnzpk.chat_app.rewrite.modules.getstarted.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.NavGraphs
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.appCurrentDestinationAsState
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.destinations.Destination
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.destinations.GetStartedMainDestination
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.destinations.GetStartedNewsDestination
import com.lnzpk.chat_app.rewrite.modules.getstarted.pages.startAppDestination
import com.ramcosta.composedestinations.navigation.navigate

@Composable
fun BottomBar(
    navController: NavController
) {
    val currentDestination: Destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination

    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {
                navController.navigate(GetStartedMainDestination)
            },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("hallo") },
        )
        NavigationBarItem(
            selected = true,
            onClick = {
                navController.navigate(GetStartedNewsDestination)
            },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("hallo") },
        )
//        BottomBarDestination.entries.forEach { destination ->
//            NavigationBarItem(
//                selected = currentDestination == destination.direction,
//                onClick = {
//                    navController.navigate(destination.direction, fun NavOptionsBuilder.() {
//                        launchSingleTop = true
//                    })
//                },
//                icon = { Icon(destination.icon, contentDescription = destination.label) },
//                label = { Text(destination.label) },
//            )
//        }
    }
}