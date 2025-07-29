package com.example.googlehomeapisampleapp.view.pulse.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.*

@Composable
fun TherapySummaryCard(homeAppVM: HomeAppViewModel) {
    val activeSuggestions by homeAppVM.activeSuggestions.collectAsState()
    val scope = rememberCoroutineScope()
    if (activeSuggestions.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable {
                    scope.launch { homeAppVM.showTherapySuggestionsView.emit(true) }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🧠", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "AI Therapy Suggestions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "${activeSuggestions.size} personalized recommendations",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    val highestPriority = activeSuggestions.maxByOrNull {
                        when(it.priority.uppercase()) {
                            "URGENT" -> 4
                            "HIGH" -> 3
                            "MEDIUM" -> 2
                            "LOW" -> 1
                            else -> 0
                        }
                    }?.priority ?: "LOW"
                    Badge(
                        containerColor = when (highestPriority.uppercase()) {
                            "URGENT" -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
                            "HIGH" -> androidx.compose.ui.graphics.Color(0xFFFF5722)
                            "MEDIUM" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                            "LOW" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            else -> androidx.compose.ui.graphics.Color.Gray
                        },
                        modifier = Modifier
                    ) {
                        Text(
                            text = highestPriority,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                val topSuggestion = activeSuggestions.firstOrNull()
                topSuggestion?.let { suggestion ->
                    Text(
                        text = suggestion.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = suggestion.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    topSuggestion?.let { suggestion ->
                        OutlinedButton(
                            onClick = { scope.launch { homeAppVM.executeSuggestion(suggestion) } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = when (suggestion.type) {
                                    "SOCIAL_SUPPORT" -> Icons.Default.People
                                    "SMART_HOME" -> Icons.Default.Home
                                    "WELLNESS" -> Icons.Default.Favorite
                                    "THERAPEUTIC" -> Icons.Default.Psychology
                                    "EMERGENCY" -> Icons.Default.Emergency
                                    else -> Icons.Default.AutoFixHigh
                                },
                                contentDescription = "Quick Action",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Quick Start", fontSize = 14.sp)
                        }
                    }
                    Button(
                        onClick = { scope.launch { homeAppVM.showTherapySuggestionsView.emit(true) } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "View All (${activeSuggestions.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View All",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
