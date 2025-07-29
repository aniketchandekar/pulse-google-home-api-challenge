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

package com.example.googlehomeapisampleapp.view.pulse

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import com.example.googlehomeapisampleapp.view.automation.AutomationSuggestionsSection
import com.example.googlehomeapisampleapp.view.pulse.cards.TopEmotionHighlightCard
import com.example.googlehomeapisampleapp.view.pulse.cards.MoodOverviewCard
import com.example.googlehomeapisampleapp.view.shared.AccountButton
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.structures.StructureViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars

@Composable
fun PulseView(homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val structureVMs: List<StructureViewModel> = homeAppVM.structureVMs.collectAsState().value
    val selectedStructureVM: StructureViewModel? = homeAppVM.selectedStructureVM.collectAsState().value
    val structureName: String = selectedStructureVM?.name ?: "Loading..."

    // Filter chip state - Fixed the delegate property issue
    var selectedTab by remember { mutableIntStateOf(0) }
    val chipLabels = listOf("All", "AI Therapy Routines", "Charts", "Journal")

    Column(modifier = Modifier.fillMaxHeight()) {
        CombinedTopBar(
            homeAppVM = homeAppVM,
            structureVMs = structureVMs,
            structureName = structureName,
            onStructureSelected = { structure -> /* TODO: Update structure selection logic */ }
        )

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chipLabels.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    label = { 
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> { // All
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        val moodAnalytics = homeAppVM.moodAnalytics.collectAsState().value
                        if (moodAnalytics != null) {
                            TopEmotionHighlightCard(moodAnalytics)
                            Spacer(modifier = Modifier.height(16.dp))
                            MoodOverviewCard(moodAnalytics)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (moodAnalytics.emotionData.isNotEmpty()) {
                                EmotionPieChartCard(moodAnalytics.emotionData)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        // AI Therapy Suggestions
                        AutomationSuggestionsSection(homeAppVM)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mood Based Automation Card
                        com.example.googlehomeapisampleapp.view.pulse.cards.MoodBasedAutomationCard(homeAppVM)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // My Journals Section
                        MyJournalsSection(homeAppVM)
                        
                        Spacer(modifier = Modifier.height(80.dp)) // FAB padding
                    }
                }
                1 -> { // AI Therapy Routines
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        AutomationSuggestionsSection(homeAppVM)
                        Spacer(modifier = Modifier.height(16.dp))
                        com.example.googlehomeapisampleapp.view.pulse.cards.MoodBasedAutomationCard(homeAppVM)
                        Spacer(modifier = Modifier.height(80.dp)) // FAB padding
                    }
                }
                2 -> { // Charts
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        val moodAnalytics = homeAppVM.moodAnalytics.collectAsState().value
                        if (moodAnalytics != null) {
                            // Top Emotion Highlight
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${moodAnalytics.topEmotion.first}",
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Most Common: ${moodAnalytics.topEmotion.second}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = getEncouragingMessage(moodAnalytics.topEmotion.second),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // Mood Overview
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "📊 Your Mood Overview",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 20.dp)
                                    )
                                    SimpleMoodBars(moodAnalytics.sentimentData)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total Check-ins",
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${moodAnalytics.emotionData.values.sum()}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (moodAnalytics.emotionData.isNotEmpty()) {
                                EmotionPieChartCard(moodAnalytics.emotionData)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(80.dp)) // FAB padding
                    }
                }
                3 -> { // Journal
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        MyJournalsSection(homeAppVM)
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { homeAppVM.navigateToCheckIn() },
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.BottomEnd),
                icon = {
                    val transition = rememberInfiniteTransition(label = "emoji_animation")
                    val scale = transition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1500,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "emoji_scale"
                    ).value
                    Text(
                        text = "😊",
                        fontSize = 24.sp,
                        modifier = Modifier.scale(scale)
                    )
                },
                text = { Text("Check-in") }
            )
        }
    }
}

@Composable
private fun CombinedTopBar(
    homeAppVM: HomeAppViewModel,
    structureVMs: List<StructureViewModel>,
    structureName: String,
    onStructureSelected: (StructureViewModel) -> Unit
) {    
    Box(
        Modifier
            .statusBarsPadding()
            .height(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier
                .height(64.dp)
                .fillMaxWidth()
                .background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title on the left
            Text(
                text = "Pulse",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Account button on the right
            AccountButton(homeAppVM)
        }
    }
}

// Remove extracted composables and utility functions:
// - MoodAnalyticsContent, TopEmotionHighlightCard, MoodOverviewCard, MyJournalsSection, MinimalJournalEntry, getEmojiForEmotion, getEncouragingMessage, SimpleMoodBars, EmotionPieChartCard, getEmotionColor, TherapySummaryCard, getPriorityColor, getTypeIcon, MoodBasedAutomationCard, FeatureHighlight