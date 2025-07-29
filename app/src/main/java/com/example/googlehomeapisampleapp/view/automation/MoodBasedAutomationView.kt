package com.example.googlehomeapisampleapp.view.automation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import kotlinx.coroutines.launch

@Composable
fun MoodBasedAutomationView(homeAppVM: HomeAppViewModel) {
    val scope = rememberCoroutineScope()
    
    // State for mood input
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var selectedIntensity by remember { mutableIntStateOf(3) }
    var selectedEmotions by remember { mutableStateOf(setOf<String>()) }
    var contextNotes by remember { mutableStateOf("") }
    var isGeneratingSuggestions by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Get active suggestions
    val activeSuggestions by homeAppVM.activeSuggestions.collectAsState()
    
    // Handle back navigation
    BackHandler {
        scope.launch {
            homeAppVM.showMoodBasedAutomationView.emit(false)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Mood-Based Automations",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = {
                    scope.launch {
                        homeAppVM.showMoodBasedAutomationView.emit(false)
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
        
        when (currentStep) {
            1 -> MoodSelectionStep(
                selectedMood = selectedMood,
                onMoodSelected = { selectedMood = it },
                onNext = { currentStep = 2 }
            )
            2 -> IntensitySelectionStep(
                selectedIntensity = selectedIntensity,
                onIntensityChanged = { selectedIntensity = it },
                onNext = { currentStep = 3 },
                onBack = { currentStep = 1 }
            )
            3 -> EmotionDetailsStep(
                selectedEmotions = selectedEmotions,
                contextNotes = contextNotes,
                onEmotionsChanged = { selectedEmotions = it },
                onNotesChanged = { contextNotes = it },
                onNext = { 
                    currentStep = 4
                    scope.launch {
                        isGeneratingSuggestions = true
                        try {
                            // Generate suggestions based on mood input
                            val checkIn = homeAppVM.saveMoodCheckInWithAutomation(selectedEmotions, contextNotes)
                            // Suggestions are automatically loaded by saveMoodCheckInWithAutomation
                        } catch (e: Exception) {
                            // Handle error if needed
                        } finally {
                            isGeneratingSuggestions = false
                        }
                    }
                },
                onBack = { currentStep = 2 }
            )
            4 -> AutomationSuggestionsStep(
                suggestions = activeSuggestions,
                isLoading = isGeneratingSuggestions,
                onApplySuggestion = { suggestion ->
                    scope.launch {
                        homeAppVM.executeSuggestion(suggestion)
                    }
                },
                onSkipSuggestion = { suggestion ->
                    scope.launch {
                        homeAppVM.dismissSuggestion(suggestion.id)
                    }
                },
                onBack = { currentStep = 3 },
                onFinish = {
                    scope.launch {
                        homeAppVM.showMoodBasedAutomationView.emit(false)
                    }
                }
            )
        }
    }
}

@Composable
fun MoodSelectionStep(
    selectedMood: String?,
    onMoodSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val moods = listOf(
        "😊" to "Great",
        "🙂" to "Good", 
        "😐" to "Okay",
        "😔" to "Down",
        "😰" to "Anxious",
        "😴" to "Tired",
        "😠" to "Frustrated"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How are you feeling right now?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(moods.chunked(2)) { rowMoods ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMoods.forEach { (emoji, mood) ->
                        MoodCard(
                            emoji = emoji,
                            mood = mood,
                            isSelected = selectedMood == mood,
                            onClick = { onMoodSelected(mood) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowMoods.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Button(
            onClick = onNext,
            enabled = selectedMood != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
fun MoodCard(
    emoji: String,
    mood: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = mood,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun IntensitySelectionStep(
    selectedIntensity: Int,
    onIntensityChanged: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How intense is this feeling?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "Rate from 1 (barely noticeable) to 5 (very intense)",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..5).forEach { intensity ->
                IntensityButton(
                    intensity = intensity,
                    isSelected = selectedIntensity == intensity,
                    onClick = { onIntensityChanged(intensity) }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun IntensityButton(
    intensity: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val size = (40 + (intensity * 8)).dp
    
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = intensity.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmotionDetailsStep(
    selectedEmotions: Set<String>,
    contextNotes: String,
    onEmotionsChanged: (Set<String>) -> Unit,
    onNotesChanged: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val emotions = listOf(
        "Happy", "Sad", "Angry", "Anxious", "Excited", "Calm",
        "Frustrated", "Hopeful", "Lonely", "Grateful", "Overwhelmed", "Confident"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Tell us more about how you're feeling",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "Select all emotions that apply:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(emotions.chunked(3)) { rowEmotions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowEmotions.forEach { emotion ->
                        EmotionChip(
                            emotion = emotion,
                            isSelected = selectedEmotions.contains(emotion),
                            onClick = {
                                val newEmotions = if (selectedEmotions.contains(emotion)) {
                                    selectedEmotions - emotion
                                } else {
                                    selectedEmotions + emotion
                                }
                                onEmotionsChanged(newEmotions)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowEmotions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Additional context (optional):",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = contextNotes,
                    onValueChange = onNotesChanged,
                    placeholder = { Text("What's happening in your life right now?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                enabled = selectedEmotions.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Generate Suggestions")
            }
        }
    }
}

@Composable
fun EmotionChip(
    emotion: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Text(
                emotion, 
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            ) 
        },
        modifier = modifier
    )
}

@Composable
fun AutomationSuggestionsStep(
    suggestions: List<AutomationSuggestionEntity>,
    isLoading: Boolean,
    onApplySuggestion: (AutomationSuggestionEntity) -> Unit,
    onSkipSuggestion: (AutomationSuggestionEntity) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = if (isLoading) "Generating personalized suggestions..." else "AI Automation Suggestions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing your mood and available devices...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (suggestions.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🤖",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "No suggestions available",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Try checking in later or ensure your smart devices are connected",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(suggestions) { suggestion ->
                    MoodAutomationSuggestionCard(
                        suggestion = suggestion,
                        onApply = { onApplySuggestion(suggestion) },
                        onSkip = { onSkipSuggestion(suggestion) }
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isLoading) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(if (isLoading) 1f else 1f)
            ) {
                Text(if (isLoading) "Cancel" else "Done")
            }
        }
    }
}

@Composable
fun MoodAutomationSuggestionCard(
    suggestion: AutomationSuggestionEntity,
    onApply: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = getSuggestionIcon(suggestion.type),
                    contentDescription = null,
                    tint = getSuggestionColor(suggestion.priority),
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = suggestion.priority,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = getSuggestionColor(suggestion.priority),
                    modifier = Modifier
                        .background(
                            getSuggestionColor(suggestion.priority).copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = suggestion.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = suggestion.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (suggestion.estimatedDuration.isNotEmpty()) {
                Text(
                    text = "Duration: ${suggestion.estimatedDuration}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Skip")
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

private fun getSuggestionIcon(type: String): ImageVector {
    return when (type) {
        "SMART_ENVIRONMENT" -> Icons.Default.Home
        "SOCIAL_SUPPORT" -> Icons.Default.People
        "WELLNESS" -> Icons.Default.Favorite
        "EMERGENCY" -> Icons.Default.Warning
        else -> Icons.Default.AutoAwesome
    }
}

private fun getSuggestionColor(priority: String): Color {
    return when (priority) {
        "URGENT" -> Color(0xFFD32F2F)
        "HIGH" -> Color(0xFFFF9800)
        "MEDIUM" -> Color(0xFF2196F3)
        "LOW" -> Color(0xFF4CAF50)
        else -> Color(0xFF757575)
    }
}