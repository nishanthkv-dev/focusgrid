package com.focusnow.app.ui.screens.analytics

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusnow.app.ui.components.QuickStatPill
import com.focusnow.app.ui.components.WeeklyBarChart
import com.focusnow.app.ui.navigation.Screen
import com.focusnow.app.util.DateTimeUtils

@Composable
fun AnalyticsScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header & Monthly Report Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analytics & Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = { onNavigateTo(Screen.MonthlyReport.route) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Monthly Report", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: Study vs Sleep vs Correlation
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Study Metrics", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Sleep Tracker", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("Productivity Insight", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    when (uiState.selectedTab) {
                        0 -> {
                            // Study Analytics Tab
                            item {
                                val study = uiState.studySummary
                                if (study != null) {
                                    // Study Weekly Stats Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        QuickStatPill(
                                            label = "7-Day Total",
                                            value = DateTimeUtils.formatDuration(study.weeklyTotalMinutes),
                                            iconEmoji = "📚",
                                            modifier = Modifier.weight(1f)
                                        )
                                        QuickStatPill(
                                            label = "Daily Avg",
                                            value = DateTimeUtils.formatDuration(study.weeklyAverageMinutes),
                                            iconEmoji = "⏱️",
                                            modifier = Modifier.weight(1f)
                                        )
                                        QuickStatPill(
                                            label = "Target Met",
                                            value = "${study.weeklyAchievementPercentage.toInt()}%",
                                            iconEmoji = "🎯",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Daily Breakdown Chart
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Daily Study Activity (Last 7 Days)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            WeeklyBarChart(
                                                data = study.dailyBreakdownLast7Days,
                                                barColor = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Highlights Card: Longest Session, Most Productive Day, Peak Time
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Productivity Highlights",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            HighlightRow(
                                                label = "Longest Study Session",
                                                value = DateTimeUtils.formatDuration(study.longestSessionMinutes)
                                            )
                                            HighlightRow(
                                                label = "Most Productive Day",
                                                value = study.mostProductiveDayOfWeek
                                            )
                                            HighlightRow(
                                                label = "Peak Study Time",
                                                value = study.mostProductiveTimeSlot
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Subject Breakdown
                                    if (study.subjectBreakdown.isNotEmpty()) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Subject Breakdown",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))

                                                study.subjectBreakdown.forEach { item ->
                                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(item.subject, fontWeight = FontWeight.SemiBold)
                                                            Text(DateTimeUtils.formatDuration(item.durationMinutes), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        LinearProgressIndicator(
                                                            progress = { item.percentage / 100f },
                                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
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
                        1 -> {
                            // Sleep Tracker Tab
                            item {
                                val sleep = uiState.sleepSummary
                                if (sleep != null) {
                                    // Log Sleep Action Button
                                    Button(
                                        onClick = { viewModel.openLogSleepDialog() },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Log Last Night's Sleep", fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Sleep Stats Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        QuickStatPill(
                                            label = "Avg Sleep",
                                            value = DateTimeUtils.formatDuration(sleep.weeklyAverageMinutes),
                                            iconEmoji = "🌙",
                                            modifier = Modifier.weight(1f)
                                        )
                                        QuickStatPill(
                                            label = "Consistency",
                                            value = "${sleep.sleepConsistencyPercentage}%",
                                            iconEmoji = "💤",
                                            modifier = Modifier.weight(1f)
                                        )
                                        QuickStatPill(
                                            label = "Met Target",
                                            value = "${sleep.daysMeetingTarget} days",
                                            iconEmoji = "✅",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Sleep 7-Day Chart
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Weekly Sleep Duration (Last 7 Days)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            WeeklyBarChart(
                                                data = sleep.dailyBreakdownLast7Days,
                                                barColor = Color(0xFF8B5CF6)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Routine Averages
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Sleep Routine Averages",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            HighlightRow(label = "Average Bedtime", value = sleep.averageBedtimeStr)
                                            HighlightRow(label = "Average Wake Time", value = sleep.averageWakeTimeStr)
                                            HighlightRow(label = "Target Sleep Met", value = "${sleep.daysMeetingTarget} of ${sleep.daysMeetingTarget + sleep.daysBelowTarget} logged days")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(30.dp))
                                }
                            }
                        }
                        2 -> {
                            // Sleep + Study Correlation Insight Tab
                            item {
                                val corr = uiState.correlation
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Insights,
                                                contentDescription = null,
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Productivity & Sleep Correlation",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E3A8A)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = corr?.insightMessage ?: "Calculating correlation...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1E293B)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "Note: Focus Now calculates insights exclusively from your own locally recorded study sessions and sleep logs to help you find your personal peak schedule.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isLogSleepDialogOpen) {
        LogSleepDialog(
            bedtimeStr = uiState.sleepBedtimeStr,
            wakeTimeStr = uiState.sleepWakeTimeStr,
            quality = uiState.sleepQuality,
            onBedtimeChange = { viewModel.updateBedtime(it) },
            onWakeTimeChange = { viewModel.updateWakeTime(it) },
            onQualityChange = { viewModel.updateQuality(it) },
            onDismiss = { viewModel.closeLogSleepDialog() },
            onSave = { viewModel.saveSleepRecord() }
        )
    }
}

@Composable
fun HighlightRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun LogSleepDialog(
    bedtimeStr: String,
    wakeTimeStr: String,
    quality: Int,
    onBedtimeChange: (String) -> Unit,
    onWakeTimeChange: (String) -> Unit,
    onQualityChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val durationMinutes = DateTimeUtils.calculateSleepMinutes(bedtimeStr, wakeTimeStr)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Sleep", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bedtimeStr,
                        onValueChange = onBedtimeChange,
                        label = { Text("Bedtime (24h)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wakeTimeStr,
                        onValueChange = onWakeTimeChange,
                        label = { Text("Wake Time (24h)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total Duration: ${DateTimeUtils.formatDuration(durationMinutes)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Sleep Quality", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { onQualityChange(star) }) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (star <= quality) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Save Sleep") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
