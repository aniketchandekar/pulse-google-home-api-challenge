package com.example.googlehomeapisampleapp.view.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationActionData
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel

@Composable
fun AutomationSuggestionsSection(homeAppVM: HomeAppViewModel) {
    val activeSuggestions by homeAppVM.activeSuggestions.collectAsState()
    
    if (activeSuggestions.isNotEmpty()) {
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
                Text(
                    text = "🤖",
                    fontSize = 24.sp
                )
                Text(
                    text = "AI Therapy Suggestions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                // Active count badge
                if (activeSuggestions.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = activeSuggestions.size.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // AI Suggestions Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activeSuggestions.forEach { suggestion ->
                    AutomationSuggestionCard(
                        suggestion = suggestion,
                        onExecute = { homeAppVM.executeSuggestion(suggestion) },
                        onDismiss = { homeAppVM.dismissSuggestion(suggestion.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AutomationSuggestionCard(
    suggestion: AutomationSuggestionEntity,
    onExecute: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = getPriorityColor(suggestion.priority).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with priority and type
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
                
                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = suggestion.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = suggestion.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            
            // AI Reasoning (if available)
            suggestion.geminiReasoning?.let { reasoning ->
                if (reasoning.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI Insight",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = reasoning,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Device Information Section
            DeviceInfoSection(suggestion = suggestion)
            
            // Actions Section
            if (suggestion.actions.isNotEmpty()) {
                Text(
                    text = "Actions:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestion.actions.forEach { action ->
                        ActionItem(action = action)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Footer with duration and execute button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    suggestion.estimatedDuration?.let { duration ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Duration",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = duration,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Execute Button
                Button(
                    onClick = onExecute,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getPriorityColor(suggestion.priority)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Execute",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ActionItem(action: AutomationActionData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = getActionIcon(action.type),
            contentDescription = action.type,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        
        Text(
            text = action.displayText,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        // Show contact name for call actions
        if (action.type == "CALL_CONTACT") {
            action.parameters["contactName"]?.let { contactName ->
                Text(
                    text = contactName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
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

private fun getTypeIcon(type: String): ImageVector {
    return when (type) {
        "SOCIAL_SUPPORT" -> Icons.Default.People
        "SMART_HOME" -> Icons.Default.Home
        "WELLNESS" -> Icons.Default.Favorite
        "THERAPEUTIC" -> Icons.Default.Psychology
        "EMERGENCY" -> Icons.Default.Emergency
        else -> Icons.Default.AutoFixHigh
    }
}

@Composable
fun DeviceInfoSection(suggestion: AutomationSuggestionEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Starters Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Starter",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Starter:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = getStarterInfo(suggestion),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Action Devices Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Devices,
                contentDescription = "Devices",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Devices:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = getDeviceInfo(suggestion),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

private fun getStarterInfo(suggestion: AutomationSuggestionEntity): String {
    return when (suggestion.type) {
        "SMART_ENVIRONMENT" -> "Motion sensor or manual trigger"
        "WELLNESS" -> "Manual trigger"
        "SOCIAL_SUPPORT" -> "Manual trigger"
        else -> "Manual trigger"
    }
}

private fun getDeviceInfo(suggestion: AutomationSuggestionEntity): String {
    val deviceCount = suggestion.actions.firstOrNull()?.parameters?.get("deviceCount")?.toIntOrNull() ?: 0
    val environment = suggestion.actions.firstOrNull()?.parameters?.get("environment")
    
    return when {
        deviceCount > 0 && environment != null -> {
            when (environment) {
                "anxiety_relief" -> "$deviceCount lights (dimmed to 30%)"
                "mood_boost" -> "$deviceCount lights (brightened to 85%)"
                "focus_clarity" -> "$deviceCount lights (set to 75%)"
                "deep_relaxation" -> "$deviceCount lights (dimmed to 20%)"
                else -> "$deviceCount smart devices"
            }
        }
        suggestion.actions.any { it.type == "TEMPERATURE_CONTROL" } -> "Thermostats"
        suggestion.actions.any { it.type == "COLOR_THERAPY" } -> "Color-changing lights"
        else -> "Available smart home devices"
    }
}

private fun getActionIcon(actionType: String): ImageVector {
    return when (actionType) {
        "CALL_CONTACT" -> Icons.Default.Call
        "SMART_HOME" -> Icons.Default.Home
        "THERAPEUTIC_ACTIVITY" -> Icons.Default.SelfImprovement
        "REMINDER" -> Icons.Default.Notifications
        "GEMINI_CHAT" -> Icons.Default.Chat
        else -> Icons.Default.PlayArrow
    }
}