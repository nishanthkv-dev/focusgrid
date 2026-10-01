package com.focusnow.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusnow.app.ui.components.CategoryBadge
import com.focusnow.app.ui.components.CircularProgressCard
import com.focusnow.app.ui.components.PriorityBadge
import com.focusnow.app.ui.components.QuickStatPill
import com.focusnow.app.ui.navigation.Screen
import com.focusnow.app.util.DateTimeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Student Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${uiState.userProfile?.name?.ifBlank { "Student" } ?: "Student"} 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (!uiState.userProfile?.college.isNullOrBlank()) {
                                "${uiState.userProfile?.department} • ${uiState.userProfile?.currentYear}"
                            } else "Let's make today productive!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Streak Badge
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        modifier = Modifier.clickable { onNavigateTo(Screen.Analytics.route) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.overallStreak}d",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main Circular Study Progress Card
                CircularProgressCard(
                    currentMinutes = uiState.studyMinutesToday,
                    targetMinutes = uiState.studyTargetMinutes,
                    modifier = Modifier.clickable { onNavigateTo(Screen.Study.route) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row: Tasks, Sleep, Productivity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickStatPill(
                        label = "Tasks",
                        value = "${uiState.completedTasksToday}/${uiState.totalTasksToday}",
                        iconEmoji = "✅",
                        modifier = Modifier.weight(1f).clickable { onNavigateTo(Screen.Tasks.route) }
                    )
                    QuickStatPill(
                        label = "Sleep",
                        value = uiState.todaySleepRecord?.let { DateTimeUtils.formatDuration(it.durationMinutes) } ?: "Log Sleep",
                        iconEmoji = "🌙",
                        modifier = Modifier.weight(1f).clickable { onNavigateTo(Screen.Analytics.route) }
                    )
                    QuickStatPill(
                        label = "Productivity",
                        value = "${uiState.productivityPercentage}%",
                        iconEmoji = "⚡",
                        modifier = Modifier.weight(1f).clickable { onNavigateTo(Screen.Analytics.route) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Actions Header
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 8 Quick Actions Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 4
                ) {
                    QuickActionIcon(
                        title = "Start Study",
                        icon = Icons.Default.PlayArrow,
                        color = Color(0xFF2563EB),
                        onClick = { onNavigateTo(Screen.Study.route) }
                    )
                    QuickActionIcon(
                        title = "Add Task",
                        icon = Icons.Default.Add,
                        color = Color(0xFF10B981),
                        onClick = { onNavigateTo(Screen.Tasks.route) }
                    )
                    QuickActionIcon(
                        title = "Plan Day",
                        icon = Icons.Default.DateRange,
                        color = Color(0xFF8B5CF6),
                        onClick = { onNavigateTo(Screen.SmartPlan.route) }
                    )
                    QuickActionIcon(
                        title = "Timetable",
                        icon = Icons.Default.School,
                        color = Color(0xFFF59E0B),
                        onClick = { onNavigateTo(Screen.Planner.route) }
                    )
                    QuickActionIcon(
                        title = "Sleep Log",
                        icon = Icons.Default.Nightlight,
                        color = Color(0xFF6366F1),
                        onClick = { onNavigateTo(Screen.Analytics.route) }
                    )
                    QuickActionIcon(
                        title = "Analytics",
                        icon = Icons.Default.Analytics,
                        color = Color(0xFF0D9488),
                        onClick = { onNavigateTo(Screen.Analytics.route) }
                    )
                    QuickActionIcon(
                        title = "Block Apps",
                        icon = Icons.Default.Lock,
                        color = Color(0xFFEF4444),
                        onClick = { onNavigateTo(Screen.AppBlocking.route) }
                    )
                    QuickActionIcon(
                        title = "Exams",
                        icon = Icons.Default.EventNote,
                        color = Color(0xFFEC4899),
                        onClick = { onNavigateTo(Screen.Exams.route) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Today's Classes Ticker
                if (uiState.todayClasses.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Timetable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateTo(Screen.Planner.route) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.todayClasses.forEach { cls ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp, 36.dp)
                                        .background(Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cls.subject,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Room ${cls.room} • ${cls.faculty}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${cls.startTimeStr} - ${cls.endTimeStr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // High Priority Tasks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateTo(Screen.Tasks.route) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.pendingTasks.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No pending tasks! Tap '+' above to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    uiState.pendingTasks.forEach { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTask(task) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CategoryBadge(task.category)
                                        PriorityBadge(task.priority)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Upcoming Exams Countdown
                if (uiState.upcomingExams.isNotEmpty()) {
                    Text(
                        text = "Upcoming Deadlines & Exams",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.upcomingExams.forEach { exam ->
                        val daysLeft = DateTimeUtils.getDaysDifference(exam.targetDateMillis)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exam.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${exam.type.displayName} • ${DateTimeUtils.formatMillisToDate(exam.targetDateMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (daysLeft <= 3) Color(0xFFFEE2E2) else Color(0xFFDBEAFE),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (daysLeft <= 0) "Today!" else "${daysLeft}d left",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (daysLeft <= 3) Color(0xFFDC2626) else Color(0xFF1D4ED8)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun QuickActionIcon(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
