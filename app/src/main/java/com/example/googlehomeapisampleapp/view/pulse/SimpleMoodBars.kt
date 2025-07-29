package com.example.googlehomeapisampleapp.view.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleMoodBars(sentimentData: Map<String, Int>) {
    val total = sentimentData.values.sum().toFloat()
    if (total == 0f) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sentimentData.entries.forEach { (sentiment, count) ->
            val percentage = (count / total)
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = getSentimentEmoji(sentiment),
                            fontSize = 16.sp
                        )
                        Text(
                            text = sentiment,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = getSentimentColor(sentiment)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .background(
                                getSentimentColor(sentiment),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}

private fun getSentimentEmoji(sentiment: String): String {
    return when (sentiment) {
        "Positive" -> "😊"
        "Neutral" -> "😐"
        "Negative" -> "😔"
        else -> "😊"
    }
}

private fun getSentimentColor(sentiment: String): Color {
    return when (sentiment) {
        "Positive" -> Color(0xFF4CAF50)  // Green
        "Neutral" -> Color(0xFF9E9E9E)   // Grey
        "Negative" -> Color(0xFFF44336)  // Red
        else -> Color.Gray
    }
}