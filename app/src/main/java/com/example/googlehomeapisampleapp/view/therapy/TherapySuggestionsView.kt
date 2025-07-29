package com.example.googlehomeapisampleapp.view.therapy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import kotlinx.coroutines.launch

@Composable
fun TherapySuggestionsView(homeAppVM: HomeAppViewModel) {
    val scope = rememberCoroutineScope()
    val activeSuggestions by homeAppVM.activeSuggestions.collectAsState()
    
    // Handle back navigation
    BackHandler {
        scope.launch {
            homeAppVM.showTherapySuggestionsView.emit(false)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "AI Therapy Suggestions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = {
                    scope.launch {
                        homeAppVM.showTherapySuggestionsView.emit(false)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        if (activeSuggestions.isEmpty()) {
            // Empty state while AI is generating suggestions
            EmptyTherapyState()
        } else {
            // Display suggestions with apply/skip options
            TherapySuggestionsContent(
                suggestions = activeSuggestions,
                onApply = { suggestion ->
                    scope.launch {
                        homeAppVM.executeSuggestion(suggestion)
                        homeAppVM.showTherapySuggestionsView.emit(false)
                    }
                },
                onSkip = { suggestion ->
                    scope.launch {
                        homeAppVM.dismissSuggestion(suggestion.id)
                    }
                },
                onSkipAll = {
                    scope.launch {
                        activeSuggestions.forEach { suggestion ->
                            homeAppVM.dismissSuggestion(suggestion.id)
                        }
                        homeAppVM.showTherapySuggestionsView.emit(false)
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyTherapyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Loading animation with AI brain emoji
        Text(
            text = "🧠",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "AI is analyzing your emotions...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Creating personalized therapy suggestions based on your check-in",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TherapySuggestionsContent(
    suggestions: List<AutomationSuggestionEntity>,
    onApply: (AutomationSuggestionEntity) -> Unit,
    onSkip: (AutomationSuggestionEntity) -> Unit,
    onSkipAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Success header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨",
                    fontSize = 48.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Check-in Complete!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Our AI has created ${suggestions.size} personalized suggestions to support your wellbeing",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Individual suggestion cards
        suggestions.forEach { suggestion ->
            TherapySuggestionCard(
                suggestion = suggestion,
                onApply = { onApply(suggestion) },
                onSkip = { onSkip(suggestion) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Skip all option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Not interested in any suggestions right now?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onSkipAll,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Skip All & Continue")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TherapySuggestionCard(
    suggestion: AutomationSuggestionEntity,
    onApply: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with priority indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = getTypeIcon(suggestion.type),
                        contentDescription = suggestion.type,
                        tint = getPriorityColor(suggestion.priority),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Badge(
                        containerColor = getPriorityColor(suggestion.priority),
                        modifier = Modifier
                    ) {
                        Text(
                            text = suggestion.priority,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                suggestion.estimatedDuration?.let { duration ->
                    Text(
                        text = duration,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title and description
            Text(
                text = suggestion.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 26.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
            
            // AI reasoning if available
            suggestion.geminiReasoning?.let { reasoning ->
                if (reasoning.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Insight",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = reasoning,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Skip",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Skip")
                }
                
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getPriorityColor(suggestion.priority)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Apply",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply Now",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun getPriorityColor(priority: String): Color {
    return when (priority.uppercase()) {
        "URGENT" -> Color(0xFFD32F2F)  // Red
        "HIGH" -> Color(0xFFFF5722)    // Deep Orange
        "MEDIUM" -> Color(0xFFFF9800)  // Orange
        "LOW" -> Color(0xFF4CAF50)     // Green
        else -> Color.Gray
    }
}

private fun getTypeIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        "SOCIAL_SUPPORT" -> Icons.Default.People
        "SMART_HOME" -> Icons.Default.Home
        "WELLNESS" -> Icons.Default.Favorite
        "THERAPEUTIC" -> Icons.Default.Psychology
        "EMERGENCY" -> Icons.Default.Emergency
        else -> Icons.Default.AutoFixHigh
    }
}