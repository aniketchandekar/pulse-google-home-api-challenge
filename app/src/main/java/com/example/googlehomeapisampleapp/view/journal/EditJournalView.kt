/* Copyright 2025 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.example.googlehomeapisampleapp.view.journal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import com.example.googlehomeapisampleapp.view.pulse.getEmojiForEmotion
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditJournalView(homeAppVM: HomeAppViewModel, journalId: String) {
    val scope: CoroutineScope = rememberCoroutineScope()
    
    // Get all journals to find the specific one
    val allJournals by homeAppVM.getAllJournals().collectAsState(initial = emptyList())
    val journal = remember(allJournals, journalId) {
        allJournals.find { it.id == journalId }
    }

    // State for editing
    var selectedEmotions by remember { mutableStateOf(setOf<String>()) }
    var thoughtsText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Initialize form data when journal is loaded
    LaunchedEffect(journal) {
        journal?.let {
            selectedEmotions = it.emotions.toSet()
            thoughtsText = it.thoughts ?: ""
        }
    }

    // Handle back navigation
    BackHandler {
        scope.launch { homeAppVM.showEditJournalView.emit(null) }
    }

    // Available emotions
    val availableEmotions = listOf(
        "Great", "Happy", "Calm", "Tired", "Upset", "Sad", 
        "Angry", "Anxious", "Thoughtful", "Confident", "Loved", "Sleepy"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top bar with back button and save action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    scope.launch { homeAppVM.showEditJournalView.emit(null) }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "Edit Journal", 
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            
            // Save button
            IconButton(
                onClick = {
                    if (selectedEmotions.isNotEmpty() && !isSaving) {
                        scope.launch {
                            isSaving = true
                            try {
                                homeAppVM.updateJournal(journalId, selectedEmotions, thoughtsText)
                                homeAppVM.showEditJournalView.emit(null)
                            } catch (e: Exception) {
                                // Handle error - could show a snackbar
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                enabled = selectedEmotions.isNotEmpty() && !isSaving
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = if (selectedEmotions.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (journal == null) {
            // Show loading or not found state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📝",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Journal entry not found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Show edit form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Original date info (read-only)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📅 ${journal.timestamp}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Emotions selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "How are you feeling?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableEmotions.forEach { emotion ->
                                FilterChip(
                                    onClick = {
                                        selectedEmotions = if (selectedEmotions.contains(emotion)) {
                                            selectedEmotions - emotion
                                        } else {
                                            selectedEmotions + emotion
                                        }
                                    },
                                    label = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = getEmojiForEmotion(emotion),
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = emotion,
                                                fontSize = 14.sp
                                            )
                                        }
                                    },
                                    selected = selectedEmotions.contains(emotion)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Thoughts input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Your thoughts (optional)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        OutlinedTextField(
                            value = thoughtsText,
                            onValueChange = { thoughtsText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("What's on your mind?") },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                
                // Add bottom padding
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}