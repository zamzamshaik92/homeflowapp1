package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.Formatters

val CategoryColors = listOf(
    Color(0xFF0D9488), // Groceries - Teal
    Color(0xFF3B82F6), // Housing - Blue
    Color(0xFFF59E0B), // Utilities - Amber
    Color(0xFFEC4899), // Dining - Pink
    Color(0xFF8B5CF6), // Entertainment - Purple
    Color(0xFF10B981), // Education - Emerald
    Color(0xFFF43F5E), // Shopping - Rose
    Color(0xFF6366F1), // Transport - Indigo
    Color(0xFF06B6D4), // Healthcare - Cyan
    Color(0xFF64748B)  // Other - Slate
)

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "groceries" -> Color(0xFF0D9488)
        "housing" -> Color(0xFF3B82F6)
        "utilities" -> Color(0xFFF59E0B)
        "dining" -> Color(0xFFEC4899)
        "entertainment" -> Color(0xFF8B5CF6)
        "education" -> Color(0xFF10B981)
        "shopping" -> Color(0xFFF43F5E)
        "transport" -> Color(0xFF6366F1)
        "healthcare" -> Color(0xFF06B6D4)
        "allowance" -> Color(0xFFD97706)
        "savings" -> Color(0xFF059669)
        else -> Color(0xFF64748B)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    categorySpending: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalSpend = categorySpending.values.sum()

    if (totalSpend <= 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses recorded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(categorySpending) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    val sortedEntries = categorySpending.entries.sortedByDescending { it.value }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val strokeWidth = 24.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)

                var startAngle = -90f
                sortedEntries.forEachIndexed { index, entry ->
                    val sweep = ((entry.value / totalSpend) * 360f * animatedProgress.value).toFloat()
                    val color = getCategoryColor(entry.key)

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweep
                }
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatCurrency(totalSpend),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedEntries.take(6).forEach { entry ->
                val percentage = ((entry.value / totalSpend) * 100).toInt()
                val color = getCategoryColor(entry.key)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${entry.key} ($percentage%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    spent: Double,
    limit: Double,
    category: String,
    modifier: Modifier = Modifier
) {
    val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val isExceeded = spent > limit
    val isWarning = spent / limit >= 0.85 && !isExceeded

    val progressColor = when {
        isExceeded -> MaterialTheme.colorScheme.error
        isWarning -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(category))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${Formatters.formatCurrency(spent)} / ${Formatters.formatCurrency(limit)}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val statusText = when {
                isExceeded -> "Over budget by ${Formatters.formatCurrency(spent - limit)}"
                isWarning -> "${((1 - progress) * 100).toInt()}% remaining - Near limit"
                else -> "${Formatters.formatCurrency(limit - spent)} left"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = progressColor
            )
        }
    }
}
