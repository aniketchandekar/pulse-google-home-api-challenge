package com.example.googlehomeapisampleapp.service

import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.ContactEntity
import com.example.googlehomeapisampleapp.data.repository.AutomationRepository
import com.example.googlehomeapisampleapp.viewmodel.structures.StructureViewModel
import kotlinx.coroutines.flow.first
import java.util.UUID

class AutomationEngine(
    private val automationRepository: AutomationRepository,
    private val emotionAnalysisService: EmotionAnalysisService,
    private val templateService: AutomationTemplateService,
    private val geminiService: GeminiTherapyService,
    private val deviceDiscoveryService: DeviceDiscoveryService = DeviceDiscoveryService()
) {
    
    suspend fun generateSuggestions(
        checkIn: CheckInEntity,
        structureVM: StructureViewModel? = null
    ): List<AutomationSuggestionEntity> {
        val emotionAnalysis = emotionAnalysisService.analyze(checkIn.emotions, checkIn.thoughts)
        val frequentContacts = automationRepository.getFrequentContacts().first()
        val timeOfDay = getCurrentTimeOfDay()
        
        // DEVICE DISCOVERY: Analyze available devices first
        val deviceCapabilities = deviceDiscoveryService.analyzeDeviceCapabilities(structureVM)
        
        // Debug logging
        android.util.Log.d("AutomationEngine", "Device capabilities: lights=${deviceCapabilities.lightCount}, thermostats=${deviceCapabilities.thermostatCount}, speakers=${deviceCapabilities.speakerCount}")
        android.util.Log.d("AutomationEngine", "Emotions to analyze: ${checkIn.emotions}")
        android.util.Log.d("AutomationEngine", "Emotion analysis result: $emotionAnalysis")
        
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Phase 1: Generate device-aware suggestions first
        suggestions.addAll(
            deviceDiscoveryService.generateDeviceAwareSuggestions(
                emotions = checkIn.emotions,
                capabilities = deviceCapabilities,
                checkInId = checkIn.id,
                priority = when (emotionAnalysis.supportNeeded) {
                    SupportLevel.HIGH, SupportLevel.URGENT -> "HIGH"
                    SupportLevel.MEDIUM -> "MEDIUM"
                    else -> "LOW"
                },
                structureVM = structureVM
            )
        )
        
        // Phase 2: Try Gemini AI for personalized suggestions with device context
        try {
            val geminiSuggestions = geminiService.generatePersonalizedSuggestions(
                emotions = checkIn.emotions,
                thoughts = checkIn.thoughts,
                contacts = frequentContacts,
                checkInId = checkIn.id,
                timeOfDay = timeOfDay,
                recentHistory = getRecentPatterns(checkIn.id),
                deviceCapabilities = deviceCapabilities // Pass device info to Gemini
            )
            
            if (geminiSuggestions.isNotEmpty()) {
                suggestions.addAll(geminiSuggestions)
            } else {
                // Fallback to template-based suggestions
                suggestions.addAll(
                    templateService.generateFromTemplates(
                        emotions = checkIn.emotions,
                        analysis = emotionAnalysis,
                        contacts = frequentContacts,
                        checkInId = checkIn.id,
                        timeOfDay = timeOfDay
                    )
                )
            }
        } catch (e: Exception) {
            // If Gemini fails, use template-based suggestions as fallback
            suggestions.addAll(
                templateService.generateFromTemplates(
                    emotions = checkIn.emotions,
                    analysis = emotionAnalysis,
                    contacts = frequentContacts,
                    checkInId = checkIn.id,
                    timeOfDay = timeOfDay
                )
            )
        }
        
        // Phase 3: Add fallback suggestions if no devices are available
        if (deviceCapabilities.lightCount == 0 && 
            deviceCapabilities.thermostatCount == 0 && 
            deviceCapabilities.speakerCount == 0) {
            suggestions.addAll(
                deviceDiscoveryService.generateFallbackSuggestions(
                    deviceCapabilities,
                    checkIn.id
                )
            )
        }
        
        // Save suggestions to database
        suggestions.forEach { automationRepository.insertSuggestion(it) }
        
        return suggestions.distinctBy { it.title }.take(5) // Remove duplicates and limit to 5
    }
    
    private suspend fun getRecentPatterns(currentCheckInId: String): List<String> {
        // Get recent check-ins to provide context to Gemini
        return try {
            automationRepository.getRecentExecutions(5).map { execution ->
                "Previous suggestion: ${execution.completionStatus}"
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun executeSuggestion(suggestionId: String): ExecutionResult {
        // Implementation for executing automations
        // This will be expanded in Phase 3
        return ExecutionResult.SUCCESS
    }
    
    private fun getCurrentTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
    }
}

enum class ExecutionResult {
    SUCCESS, PARTIAL_SUCCESS, FAILED, USER_CANCELLED
}