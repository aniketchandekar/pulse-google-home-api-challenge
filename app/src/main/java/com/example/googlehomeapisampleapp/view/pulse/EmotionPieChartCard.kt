package com.example.googlehomeapisampleapp.view.pulse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EmotionPieChartCard(emotionData: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📊 Emotion Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmotionPieChart(emotionData)
                }
                
                // Legend
                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emotionData.entries.take(5).forEach { (emotion, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        getEmotionColor(emotion),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Text(
                                text = emotion,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionPieChart(emotionData: Map<String, Int>) {
    val total = emotionData.values.sum().toFloat()
    if (total == 0f) return
    
    Canvas(
        modifier = Modifier.size(120.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        
        var startAngle = 0f
        emotionData.entries.forEach { (emotion, count) ->
            val sweepAngle = (count / total) * 360f
            
            drawArc(
                color = getEmotionColor(emotion),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
    }
}

private fun getEmotionColor(emotion: String): Color {
    return when (emotion) {
        "Great" -> Color(0xFF4CAF50)     // Green
        "Happy" -> Color(0xFFFFEB3B)     // Yellow
        "Calm" -> Color(0xFF2196F3)      // Blue
        "Confident" -> Color(0xFF9C27B0) // Purple
        "Loved" -> Color(0xFFE91E63)     // Pink
        "Tired" -> Color(0xFF795548)     // Brown
        "Sleepy" -> Color(0xFF607D8B)    // Blue Grey
        "Thoughtful" -> Color(0xFF00BCD4) // Cyan
        "Upset" -> Color(0xFFFF5722)     // Deep Orange
        "Sad" -> Color(0xFF3F51B5)       // Indigo
        "Angry" -> Color(0xFFF44336)     // Red
        "Anxious" -> Color(0xFFFF9800)   // Orange
        else -> Color.Gray
    }
}