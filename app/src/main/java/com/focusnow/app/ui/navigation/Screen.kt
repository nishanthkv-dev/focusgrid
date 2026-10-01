package com.focusnow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    data object Onboarding : Screen("onboarding", "Onboarding")
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Tasks : Screen("tasks", "Tasks", Icons.Default.CheckCircle)
    data object Planner : Screen("planner", "Planner", Icons.Default.DateRange)
    data object Study : Screen("study", "Study", Icons.Default.Timer)
    data object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    data object Profile : Screen("profile", "Settings", Icons.Default.Person)

    // Sub-screens
    data object Goals : Screen("goals", "Academic Goals")
    data object Exams : Screen("exams", "Exams & Deadlines")
    data object AppBlocking : Screen("app_blocking", "App Blocker")
    data object Calendar : Screen("calendar", "Full Calendar", Icons.Default.CalendarMonth)
    data object MonthlyReport : Screen("monthly_report", "Monthly Review")
    data object SmartPlan : Screen("smart_plan", "Plan My Day")
    data object Settings : Screen("settings", "Settings")
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Tasks,
    Screen.Planner,
    Screen.Study,
    Screen.Analytics,
    Screen.Profile
)
