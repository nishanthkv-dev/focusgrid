package com.focusnow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusnow.app.data.model.DeadlinePriority
import com.focusnow.app.data.model.TaskCategory
import com.focusnow.app.data.model.TaskPriority
import com.focusnow.app.data.repository.DayStudyData
import com.focusnow.app.ui.theme.CatCodingColor
import com.focusnow.app.ui.theme.CatCollegeColor
import com.focusnow.app.ui.theme.CatGateColor
import com.focusnow.app.ui.theme.CatOtherColor
import com.focusnow.app.ui.theme.CatPersonalColor
import com.focusnow.app.ui.theme.CatPlacementColor
import com.focusnow.app.ui.theme.CatProjectColor
import com.focusnow.app.ui.theme.PriorityHighColor
import com.focusnow.app.ui.theme.PriorityLowColor
import com.focusnow.app.ui.theme.PriorityMediumColor
import com.focusnow.app.ui.theme.PriorityUrgentColor
import com.focusnow.app.util.DateTimeUtils

@Composable
fun CircularProgressCard(
    currentMinutes: Int,
    targetMinutes: Int,
    modifier: Modifier = Modifier,
    title: String = "Study",
    subtitle: String = "Today's Target"
) {
    val progress = if (targetMinutes > 0) {
        (currentMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnim")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${DateTimeUtils.formatDuration(currentMinutes)} / ${DateTimeUtils.formatDuration(targetMinutes)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val remaining = (targetMinutes - currentMinutes).coerceAtLeast(0)
                Text(
                    text = if (remaining == 0) "🎉 Target Achieved!" else "${DateTimeUtils.formatDuration(remaining)} remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (remaining == 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surface
                Canvas(modifier = Modifier.size(76.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun QuickStatPill(
    label: String,
    value: String,
    iconEmoji: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = iconEmoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(category: TaskCategory) {
    val (color, name) = when (category) {
        TaskCategory.COLLEGE -> CatCollegeColor to "College"
        TaskCategory.GATE -> CatGateColor to "GATE"
        TaskCategory.PLACEMENT -> CatPlacementColor to "Placement"
        TaskCategory.CODING -> CatCodingColor to "Coding"
        TaskCategory.PROJECT -> CatProjectColor to "Project"
        TaskCategory.PERSONAL -> CatPersonalColor to "Personal"
        TaskCategory.OTHER -> CatOtherColor to "Other"
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority) {
    val (color, name) = when (priority) {
        TaskPriority.LOW -> PriorityLowColor to "Low"
        TaskPriority.MEDIUM -> PriorityMediumColor to "Medium"
        TaskPriority.HIGH -> PriorityHighColor to "High"
        TaskPriority.URGENT -> PriorityUrgentColor to "Urgent"
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DeadlinePriorityBadge(priority: DeadlinePriority) {
    val color = when (priority) {
        DeadlinePriority.LOW -> PriorityLowColor
        DeadlinePriority.MEDIUM -> PriorityMediumColor
        DeadlinePriority.HIGH -> PriorityHighColor
        DeadlinePriority.URGENT -> PriorityUrgentColor
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = priority.displayName,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WeeklyBarChart(
    data: List<DayStudyData>,
    maxMinutes: Int = 300,
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(data.maxOfOrNull { it.durationMinutes } ?: 1, maxMinutes, 60)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val ratio = (item.durationMinutes.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (item.durationMinutes > 0) {
                        Text(
                            text = "${item.durationMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height((100 * ratio).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (item.durationMinutes > 0) barColor else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
