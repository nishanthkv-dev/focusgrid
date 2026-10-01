package com.focusnow.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.focusnow.app.ui.screens.analytics.AnalyticsScreen
import com.focusnow.app.ui.screens.blocking.AppBlockingScreen
import com.focusnow.app.ui.screens.calendar.CalendarScreen
import com.focusnow.app.ui.screens.exams.ExamsScreen
import com.focusnow.app.ui.screens.goals.GoalsScreen
import com.focusnow.app.ui.screens.home.HomeScreen
import com.focusnow.app.ui.screens.monthly.MonthlyReportScreen
import com.focusnow.app.ui.screens.onboarding.OnboardingScreen
import com.focusnow.app.ui.screens.planner.PlannerScreen
import com.focusnow.app.ui.screens.settings.SettingsScreen
import com.focusnow.app.ui.screens.smartplan.SmartPlanScreen
import com.focusnow.app.ui.screens.study.StudyScreen
import com.focusnow.app.ui.screens.tasks.TasksScreen

@Composable
fun FocusNowNavGraph(
    navController: NavHostController,
    isOnboardingCompleted: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route
    val isBottomBarVisible = BottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    BottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateTo = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Tasks.route) {
                TasksScreen()
            }

            composable(Screen.Planner.route) {
                PlannerScreen(
                    onNavigateTo = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Study.route) {
                StudyScreen()
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    onNavigateTo = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Profile.route) {
                SettingsScreen(
                    onNavigateTo = { route -> navController.navigate(route) }
                )
            }

            // Sub-destinations
            composable(Screen.Goals.route) {
                GoalsScreen()
            }

            composable(Screen.Exams.route) {
                ExamsScreen()
            }

            composable(Screen.AppBlocking.route) {
                AppBlockingScreen()
            }

            composable(Screen.Calendar.route) {
                CalendarScreen()
            }

            composable(Screen.MonthlyReport.route) {
                MonthlyReportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SmartPlan.route) {
                SmartPlanScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
