package com.yourname.allowancetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourname.allowancetracker.ui.AllowanceViewModel
import com.yourname.allowancetracker.ui.screens.ChildDetailScreen
import com.yourname.allowancetracker.ui.screens.GoalsScreen
import com.yourname.allowancetracker.ui.screens.HomeScreen

@Composable
fun AppNavigation(viewModel: AllowanceViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // ============================================
        // HOME SCREEN ROUTE
        // ============================================
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onChildClick = { childId ->
                    // Navigate to Child Detail screen
                    navController.navigate("child/$childId")
                }
            )
        }

        // ============================================
        // CHILD DETAIL SCREEN ROUTE
        // ============================================
        composable(
            route = "child/{childId}",
            arguments = listOf(
                navArgument("childId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getInt("childId") ?: return@composable

            // Load child data when entering this screen
            LaunchedEffect(childId) {
                viewModel.selectChild(childId)
            }

            ChildDetailScreen(
                viewModel = viewModel,
                onBack = {
                    // Navigate back to Home
                    navController.popBackStack()
                },
                onNavigateToGoals = { childId, childName ->
                    // Navigate to Goals screen
                    navController.navigate("goals/$childId/$childName")
                }
            )
        }

        // ============================================
        // GOALS SCREEN ROUTE
        // ============================================
        composable(
            route = "goals/{childId}/{childName}",
            arguments = listOf(
                navArgument("childId") {
                    type = NavType.IntType
                },
                navArgument("childName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getInt("childId") ?: return@composable
            val childName = backStackEntry.arguments?.getString("childName") ?: ""

            GoalsScreen(
                viewModel = viewModel,
                childId = childId,
                childName = childName,
                onBack = {
                    // Navigate back to Child Detail
                    navController.popBackStack()
                }
            )
        }
    }
}