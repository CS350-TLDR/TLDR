//package com.comp350.tldr
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.navigation.NavController
//import androidx.navigation.NavDestination.Companion.hierarchy
//import androidx.navigation.NavGraph.Companion.findStartDestination
//import androidx.navigation.compose.currentBackStackEntryAsState
//
//// Data class for bottom nav items
//data class BottomNavItem(
//    val name: String,
//    val route: String,
//    val icon: ImageVector
//)
//
//@Composable
//fun BottomNavBar(navController: NavController) {
//    // Define navigation items
//    val items = listOf(
//        BottomNavItem("Home", "main_menu", Icons.Filled.Home),
//        BottomNavItem("Profile", "profile", Icons.Filled.Person)
//    )
//
//    NavigationBar(
//        containerColor = Color.White,
//    ) {
//        val navBackStackEntry by navController.currentBackStackEntryAsState()
//        val currentDestination = navBackStackEntry?.destination
//
//        items.forEach { item ->
//            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
//
//            NavigationBarItem(
//                icon = { Icon(item.icon, contentDescription = item.name) },
//                label = { Text(text = item.name) },
//                selected = selected,
//                onClick = {
//                    navController.navigate(item.route) {
//                        // Pop up to the start destination of the graph to
//                        // avoid building up a large stack of destinations
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                        }
//                        // Avoid multiple copies of the same destination when
//                        // reselecting the same item
//                        launchSingleTop = true
//                        // Restore state when reselecting a previously selected item
//                        restoreState = true
//                    }
//                }
//            )
//        }
//    }
//}