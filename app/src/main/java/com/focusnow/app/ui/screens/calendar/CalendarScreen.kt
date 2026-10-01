package com.focusnow.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusnow.app.ui.components.CategoryBadge
import com.focusnow.app.ui.components.PriorityBadge
import com.focusnow.app.util.DateTimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel()
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

                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Month Switcher Header
                val monthName = uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val year = uiState.currentMonth.year

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$monthName $year",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(onClick = { viewModel.changeMonth(-1) }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                                }
                                IconButton(onClick = { viewModel.changeMonth(1) }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day of week labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayOfWeek.entries.forEach { day ->
                                Text(
                                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Month Days Grid
                        val firstDayOfMonth = uiState.currentMonth.atDay(1)
                        val daysInMonth = uiState.currentMonth.lengthOfMonth()
                        val leadingEmptyDays = firstDayOfMonth.dayOfWeek.value - 1 // Mon = 1

                        val totalGridCells = leadingEmptyDays + daysInMonth
                        val rows = (totalGridCells + 6) / 7

                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0..6) {
                                    val dayIndex = r * 7 + c
                                    val dayNumber = dayIndex - leadingEmptyDays + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val date = uiState.currentMonth.atDay(dayNumber)
                                        val isSelected = date == uiState.selectedDate
                                        val isToday = date == LocalDate.now()
                                        val dateStr = DateTimeUtils.formatDateStr(date)
                                        val activities = uiState.dateActivitiesMap[dateStr]

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable { viewModel.selectDate(date) },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp
                                            )

                                            // Activity Indicator Dots
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.height(4.dp)
                                            ) {
                                                if (activities?.hasStudy == true) {
                                                    Box(modifier = Modifier.size(3.dp).background(if (isSelected) Color.White else Color(0xFF3B82F6), CircleShape))
                                                }
                                                if (activities?.hasTasks == true) {
                                                    Box(modifier = Modifier.size(3.dp).background(if (isSelected) Color.White else Color(0xFF10B981), CircleShape))
                                                }
                                                if (activities?.hasExams == true) {
                                                    Box(modifier = Modifier.size(3.dp).background(if (isSelected) Color.White else Color(0xFFEF4444), CircleShape))
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Agenda Header for selected date
                Text(
                    text = "Activities on ${DateTimeUtils.formatMillisToDate(DateTimeUtils.getStartOfDayMillis(uiState.selectedDate))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // College Classes on this date
            if (uiState.selectedDateClasses.isNotEmpty()) {
                item {
                    Text("College Classes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(uiState.selectedDateClasses) { cls ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏛️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cls.subject, fontWeight = FontWeight.Bold)
                                Text("Room ${cls.room} • ${cls.faculty}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${cls.startTimeStr} - ${cls.endTimeStr}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Study Sessions on this date
            if (uiState.selectedDateSessions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Study Sessions Logged", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(uiState.selectedDateSessions) { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📚", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(session.subject, fontWeight = FontWeight.Bold)
                                Text("${session.category} • ${DateTimeUtils.formatMillisToTime(session.startTimeMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${session.durationMinutes} min", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Tasks on this date
            if (uiState.selectedDateTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tasks Due", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(uiState.selectedDateTasks) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (task.isCompleted) "✅" else "📌", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CategoryBadge(task.category)
                                    PriorityBadge(task.priority)
                                }
                            }
                            Text("Due ${task.dueTimeStr}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Exams on this date
            if (uiState.selectedDateExams.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Exams / Deadlines", style = MaterialTheme.typography.labelLarge, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(uiState.selectedDateExams) { exam ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exam.title, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                Text(exam.type.displayName, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB91C1C))
                            }
                        }
                    }
                }
            }

            // If empty
            if (uiState.selectedDateClasses.isEmpty() && uiState.selectedDateSessions.isEmpty() && uiState.selectedDateTasks.isEmpty() && uiState.selectedDateExams.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No scheduled activities recorded on this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
