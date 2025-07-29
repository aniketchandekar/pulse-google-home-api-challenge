package com.example.googlehomeapisampleapp.service

import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationActionData
import com.example.googlehomeapisampleapp.data.entity.ContactEntity
import java.util.UUID

class AutomationTemplateService {
    
    fun generateFromTemplates(
        emotions: List<String>,
        analysis: EmotionAnalysis,
        contacts: List<ContactEntity>,
        checkInId: String,
        timeOfDay: String
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        when (analysis.supportNeeded) {
            SupportLevel.HIGH, SupportLevel.URGENT -> {
                suggestions.addAll(getHighSupportSuggestions(emotions, contacts, checkInId, timeOfDay))
            }
            SupportLevel.MEDIUM -> {
                suggestions.addAll(getMediumSupportSuggestions(emotions, contacts, checkInId, timeOfDay))
            }
            SupportLevel.LOW -> {
                suggestions.addAll(getLowSupportSuggestions(emotions, contacts, checkInId, timeOfDay))
            }
            SupportLevel.NONE -> {
                when (analysis.overallSentiment) {
                    Sentiment.POSITIVE -> suggestions.addAll(getPositiveMoodSuggestions(emotions, checkInId, timeOfDay))
                    Sentiment.NEUTRAL -> suggestions.addAll(getNeutralMoodSuggestions(emotions, checkInId, timeOfDay))
                    else -> suggestions.addAll(getBasicSupportSuggestions(emotions, contacts, checkInId, timeOfDay))
                }
            }
        }
        
        // Add crisis detection if needed
        if (analysis.riskLevel == RiskLevel.URGENT || analysis.riskLevel == RiskLevel.CONCERN) {
            suggestions.addAll(getCrisisSuggestions(emotions, contacts, checkInId))
        }
        
        return suggestions.take(3) // Limit to top 3 suggestions
    }
    
    private fun getHighSupportSuggestions(
        emotions: List<String>,
        contacts: List<ContactEntity>,
        checkInId: String,
        timeOfDay: String
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Priority 1: Contact support person
        val supportContact = contacts.firstOrNull()
        if (supportContact != null) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Reach Out for Support",
                    description = "Connect with someone who cares about you",
                    type = "SOCIAL_SUPPORT",
                    priority = "HIGH",
                    actions = listOf(
                        AutomationActionData(
                            type = "CALL_CONTACT",
                            targetId = supportContact.id,
                            parameters = mapOf(
                                "phoneNumber" to supportContact.phoneNumber,
                                "contactName" to supportContact.name,
                                "suggestedScript" to "I'm going through a tough time and could use some support."
                            ),
                            displayText = "Call ${supportContact.name}"
                        )
                    ),
                    geminiReasoning = "Social connection is crucial during difficult emotional moments",
                    estimatedDuration = "10-30 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        // Priority 2: Create calming environment + breathing exercise
        suggestions.add(
            AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = "Anxiety Relief Environment",
                description = "Create a calming space and guide you through breathing exercises",
                type = "SMART_ENVIRONMENT",
                priority = "HIGH",
                actions = listOf(
                    AutomationActionData(
                        type = "SMART_HOME_ENVIRONMENT",
                        parameters = mapOf(
                            "environment" to "anxiety_relief",
                            "duration" to "15"
                        ),
                        displayText = "Activate anxiety relief environment"
                    ),
                    AutomationActionData(
                        type = "THERAPEUTIC_ACTIVITY",
                        parameters = mapOf(
                            "activity" to "breathing_exercise",
                            "duration" to "5"
                        ),
                        displayText = "Start 5-minute breathing exercise"
                    )
                ),
                geminiReasoning = "Environmental changes combined with breathing exercises can quickly reduce anxiety and emotional distress",
                estimatedDuration = "5-15 minutes",
                createdAt = System.currentTimeMillis()
            )
        )
        
        return suggestions
    }
    
    private fun getMediumSupportSuggestions(
        emotions: List<String>,
        contacts: List<ContactEntity>,
        checkInId: String,
        timeOfDay: String
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Gentle check-in with someone
        val contact = contacts.firstOrNull()
        if (contact != null) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Gentle Check-in",
                    description = "A light conversation might help lift your spirits",
                    type = "SOCIAL_SUPPORT",
                    priority = "MEDIUM",
                    actions = listOf(
                        AutomationActionData(
                            type = "CALL_CONTACT",
                            targetId = contact.id,
                            parameters = mapOf(
                                "phoneNumber" to contact.phoneNumber,
                                "contactName" to contact.name,
                                "suggestedScript" to "Hi! Just wanted to check in and see how you're doing today."
                            ),
                            displayText = "Call ${contact.name} for a chat"
                        )
                    ),
                    geminiReasoning = "Light social connection can provide emotional support without feeling overwhelming",
                    estimatedDuration = "10-20 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        // Mood boost environment
        suggestions.add(
            AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = "Mood Boost Environment",
                description = "Brighten your space to help lift your spirits",
                type = "SMART_ENVIRONMENT",
                priority = "MEDIUM",
                actions = listOf(
                    AutomationActionData(
                        type = "SMART_HOME_ENVIRONMENT",
                        parameters = mapOf(
                            "environment" to "mood_boost",
                            "duration" to "20"
                        ),
                        displayText = "Activate mood boost environment"
                    )
                ),
                geminiReasoning = "A comfortable physical environment can help improve emotional well-being",
                estimatedDuration = "Immediate",
                createdAt = System.currentTimeMillis()
            )
        )
        
        return suggestions
    }
    
    private fun getLowSupportSuggestions(
        emotions: List<String>,
        contacts: List<ContactEntity>,
        checkInId: String,
        timeOfDay: String
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Self-care activity based on time of day
        val activity = when (timeOfDay) {
            "morning" -> "energizing_playlist" to "Start your day with uplifting music"
            "afternoon" -> "short_walk_reminder" to "Take a refreshing 10-minute walk"
            "evening" -> "relaxing_routine" to "Wind down with gentle lighting and soft music"
            "night" -> "bedtime_routine" to "Prepare for restful sleep"
            else -> "mindfulness_moment" to "Take a moment for yourself"
        }
        
        suggestions.add(
            AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = "Self-Care Moment",
                description = activity.second,
                type = "WELLNESS",
                priority = "LOW",
                actions = listOf(
                    AutomationActionData(
                        type = "SMART_HOME",
                        parameters = mapOf(
                            "device" to "speaker",
                            "action" to "play_music",
                            "value" to activity.first
                        ),
                        displayText = activity.second
                    ),
                    AutomationActionData(
                        type = "REMINDER",
                        parameters = mapOf(
                            "message" to "Take a few deep breaths and appreciate this moment",
                            "when" to "now"
                        ),
                        displayText = "Mindfulness reminder"
                    )
                ),
                geminiReasoning = "Gentle self-care activities can help maintain emotional balance",
                estimatedDuration = "5-10 minutes",
                createdAt = System.currentTimeMillis()
            )
        )
        
        return suggestions
    }
    
    private fun getPositiveMoodSuggestions(
        emotions: List<String>,
        checkInId: String,
        timeOfDay: String
    ): List<AutomationSuggestionEntity> {
        return listOf(
            AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = "Amplify Your Good Vibes",
                description = "Let's make this positive moment even better",
                type = "SMART_HOME",
                priority = "MEDIUM",
                actions = listOf(
                    AutomationActionData(
                        type = "SMART_HOME",
                        parameters = mapOf(
                            "device" to "lights",
                            "action" to "set_color",
                            "value" to "vibrant"
                        ),
                        displayText = "Set vibrant lighting"
                    ),
                    AutomationActionData(
                        type = "SMART_HOME",
                        parameters = mapOf(
                            "device" to "speaker",
                            "action" to "play_music",
                            "value" to "upbeat_playlist"
                        ),
                        displayText = "Play upbeat music"
                    ),
                    AutomationActionData(
                        type = "REMINDER",
                        parameters = mapOf(
                            "message" to "Share this positive energy with someone you love",
                            "when" to "now"
                        ),
                        displayText = "Consider sharing your joy"
                    )
                ),
                geminiReasoning = "Amplifying positive emotions can create lasting mood improvements and strengthen social connections",
                estimatedDuration = "15-30 minutes",
                createdAt = System.currentTimeMillis()
            )
        )
    }
    
    private fun getNeutralMoodSuggestions(
        emotions: List<String>,
        checkInId: String,
        timeOfDay: String
    ): List<AutomationSuggestionEntity> {
        return listOf(
            AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = "Gentle Energy Boost",
                description = "Add a little spark to your day",
                type = "WELLNESS",
                priority = "LOW",
                actions = listOf(
                    AutomationActionData(
                        type = "SMART_HOME",
                        parameters = mapOf(
                            "device" to "lights",
                            "action" to "set_brightness",
                            "value" to "80"
                        ),
                        displayText = "Brighten the lights"
                    ),
                    AutomationActionData(
                        type = "THERAPEUTIC_ACTIVITY",
                        parameters = mapOf(
                            "activity" to "mood_boost_playlist",
                            "duration" to "10"
                        ),
                        displayText = "10-minute mood boost activity"
                    )
                ),
                geminiReasoning = "Small environmental changes can help shift neutral moods toward more positive states",
                estimatedDuration = "10 minutes",
                createdAt = System.currentTimeMillis()
            )
        )
    }
    
    private fun getBasicSupportSuggestions(
        emotions: List<String>,
        contacts: List<ContactEntity>,
        checkInId: String,
        timeOfDay: String
    ): List<AutomationSuggestionEntity> {
        return listOf(
            AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = "Comfort & Connection",
                description = "Create a supportive environment for yourself",
                type = "WELLNESS",
                priority = "MEDIUM",
                actions = listOf(
                    AutomationActionData(
                        type = "SMART_HOME",
                        parameters = mapOf(
                            "device" to "lights",
                            "action" to "set_color",
                            "value" to "soft_warm"
                        ),
                        displayText = "Set soft, warm lighting"
                    ),
                    AutomationActionData(
                        type = "REMINDER",
                        parameters = mapOf(
                            "message" to "It's okay to reach out to someone if you need support",
                            "when" to "in_30_minutes"
                        ),
                        displayText = "Gentle reminder about support"
                    )
                ),
                geminiReasoning = "Creating a supportive environment while gently encouraging connection can be helpful",
                estimatedDuration = "5 minutes",
                createdAt = System.currentTimeMillis()
            )
        )
    }
    
    private fun getCrisisSuggestions(
        emotions: List<String>,
        contacts: List<ContactEntity>,
        checkInId: String
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Crisis hotline first
        suggestions.add(
            AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = "🚨 Immediate Support Available",
                description = "You don't have to go through this alone. Professional help is available right now.",
                type = "EMERGENCY",
                priority = "URGENT",
                actions = listOf(
                    AutomationActionData(
                        type = "CALL_CONTACT",
                        parameters = mapOf(
                            "phoneNumber" to "988",
                            "contactName" to "Crisis Lifeline"
                        ),
                        displayText = "Call 988 - Suicide & Crisis Lifeline (24/7 support)"
                    )
                ),
                geminiReasoning = "Crisis-level distress detected. Immediate professional intervention recommended.",
                estimatedDuration = "Available now",
                createdAt = System.currentTimeMillis()
            )
        )
        
        // Emergency contact if available
        val emergencyContact = contacts.find { it.relationship == "emergency" }
        if (emergencyContact != null) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Emergency Contact",
                    description = "Call your designated emergency contact",
                    type = "EMERGENCY",
                    priority = "URGENT",
                    actions = listOf(
                        AutomationActionData(
                            type = "CALL_CONTACT",
                            targetId = emergencyContact.id,
                            parameters = mapOf(
                                "phoneNumber" to emergencyContact.phoneNumber,
                                "contactName" to emergencyContact.name
                            ),
                            displayText = "Call ${emergencyContact.name} (your emergency contact)"
                        )
                    ),
                    geminiReasoning = "Emergency contact can provide immediate personal support during crisis",
                    estimatedDuration = "Immediate",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
}