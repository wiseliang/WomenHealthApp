package com.health.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.health.cycle.presentation.CalendarScreen
import com.health.diet.presentation.MealListScreen
import com.health.hormone.presentation.HormoneDashboardScreen
import com.health.onboarding.presentation.*
import com.health.profile.presentation.ProfileScreen
import com.health.recommendation.presentation.RecommendationListScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Determine if we should show bottom nav
    val isOnboarding = currentDestination?.let { dest ->
        dest.hasRoute(Screen.Welcome::class) ||
        dest.hasRoute(Screen.HealthInfo::class) ||
        dest.hasRoute(Screen.GoalSelection::class) ||
        dest.hasRoute(Screen.HuaweiPrompt::class)
    } ?: true

    Scaffold(
        bottomBar = {
            if (!isOnboarding) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Onboarding
            composable<Screen.Welcome> { WelcomeScreen(onNext = { navController.navigate(Screen.HealthInfo) }) }
            composable<Screen.HealthInfo> {
                HealthInfoScreen(
                    onNext = { navController.navigate(Screen.GoalSelection) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Screen.GoalSelection> {
                GoalSelectionScreen(
                    onNext = { navController.navigate(Screen.HuaweiPrompt) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Screen.HuaweiPrompt> {
                HuaweiPromptScreen(
                    onFinish = {
                        navController.navigate(Screen.Calendar) {
                            popUpTo(Screen.Welcome) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Main tabs
            composable<Screen.Calendar> { CalendarScreen() }
            composable<Screen.HormoneDashboard> { HormoneDashboardScreen() }
            composable<Screen.MealList> { MealListScreen() }
            composable<Screen.RecommendationList> { RecommendationListScreen() }
            composable<Screen.Profile> { ProfileScreen() }
        }
    }
}
