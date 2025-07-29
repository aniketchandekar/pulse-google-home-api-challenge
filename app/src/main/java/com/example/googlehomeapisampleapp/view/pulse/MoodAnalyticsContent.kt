package com.example.googlehomeapisampleapp.view.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import com.example.googlehomeapisampleapp.data.repository.MoodAnalyticsData
import com.example.googlehomeapisampleapp.view.automation.AutomationSuggestionsSection
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.view.pulse.cards.TopEmotionHighlightCard
import com.example.googlehomeapisampleapp.view.pulse.cards.MoodOverviewCard
import com.example.googlehomeapisampleapp.view.pulse.cards.TherapySummaryCard
import com.example.googlehomeapisampleapp.view.pulse.cards.MoodBasedAutomationCard

@Composable
fun MoodAnalyticsContent(homeAppVM: HomeAppViewModel) {
    val moodAnalytics by homeAppVM.moodAnalytics.collectAsState()
    val analyticsData = moodAnalytics ?: run {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No mood data yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start by recording your first check-in!",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TherapySummaryCard(homeAppVM = homeAppVM)
        TopEmotionHighlightCard(analyticsData)
        Spacer(modifier = Modifier.height(16.dp))
        MoodOverviewCard(analyticsData)
        Spacer(modifier = Modifier.height(16.dp))
        if (analyticsData.emotionData.isNotEmpty()) {
            EmotionPieChartCard(analyticsData.emotionData)
            Spacer(modifier = Modifier.height(16.dp))
        }
        AutomationSuggestionsSection(homeAppVM)
        MoodBasedAutomationCard(homeAppVM)
        MyJournalsSection(homeAppVM)
        Spacer(modifier = Modifier.height(120.dp))
    }
}

fun getEncouragingMessage(emotion: String): String {
    return when (emotion.lowercase()) {
        "happy" -> "Your positive energy is contagious! Keep spreading those good vibes."
        "great" -> "You're radiating greatness! Your enthusiasm is inspiring."
        "calm" -> "Your inner peace is a strength. Tranquility suits you beautifully."
        "confident" -> "Your self-assurance is admirable! Confidence is your superpower."
        "loved" -> "Feeling loved shows in your warmth. You're surrounded by care."
        "tired" -> "Rest is productive too. You're doing great, take care of yourself."
        "thoughtful" -> "Your reflective nature brings wisdom. Deep thinking is a gift."
        "sleepy" -> "Rest well! Good sleep leads to great days ahead."
        "upset" -> "It's okay to feel upset. These feelings will pass, you're stronger than you know."
        "sad" -> "Sadness is part of healing. Be gentle with yourself, brighter days are coming."
        "angry" -> "Your feelings are valid. Channel this energy into positive change."
        "anxious" -> "Anxiety shows you care deeply. Take it one breath at a time."
        else -> "Every emotion is valid and part of your unique journey. You're doing amazing!"
    }
}
