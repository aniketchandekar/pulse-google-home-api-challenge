package com.example.googlehomeapisampleapp.view.pulse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel

@Composable
fun MyJournalsSection(homeAppVM: HomeAppViewModel) {
    val allJournals by homeAppVM.getAllJournals().collectAsState(initial = emptyList())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = "Journals",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "My Journals",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            // View All Button
            TextButton(
                onClick = { homeAppVM.navigateToAllJournals() }
            ) {
                Text("View All")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View All",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Recent Journals Preview (show last 3)
        if (allJournals.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allJournals.take(3).forEach { journal ->
                    JournalPreviewCard(
                        journal = journal,
                        onClick = { homeAppVM.navigateToJournalDetail(journal.id) }
                    )
                }
            }
        } else {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📝",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No journals yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start your first check-in to create a journal entry",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun JournalPreviewCard(
    journal: CheckInEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date and emotions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = journal.timestamp,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Emotion chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    journal.emotions.take(2).forEach { emotion ->
                        Text(
                            text = getEmojiForEmotion(emotion),
                            fontSize = 16.sp
                        )
                    }
                    if (journal.emotions.size > 2) {
                        Text(
                            text = "+${journal.emotions.size - 2}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Thoughts preview
            journal.thoughts?.let { thoughts ->
                Text(
                    text = thoughts,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun getEmojiForEmotion(emotion: String): String {
    return when (emotion) {
        "Great" -> "😊"
        "Happy" -> "😄"
        "Calm" -> "😌"
        "Tired" -> "😴"
        "Upset" -> "😕"
        "Sad" -> "😢"
        "Angry" -> "😠"
        "Anxious" -> "😰"
        "Thoughtful" -> "🤔"
        "Confident" -> "😎"
        "Loved" -> "🥰"
        "Sleepy" -> "😪"
        else -> "😊"
    }
}