package com.example.googlehomeapisampleapp.service

import com.example.googlehomeapisampleapp.BuildConfig
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationActionData
import com.example.googlehomeapisampleapp.data.entity.ContactEntity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class GeminiTherapyService {
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )
    
    suspend fun generatePersonalizedSuggestions(
        emotions: List<String>,
        thoughts: String?,
        contacts: List<ContactEntity>,
        checkInId: String,
        timeOfDay: String,
        recentHistory: List<String> = emptyList(),
        deviceCapabilities: DeviceDiscoveryService.DeviceCapabilities? = null
    ): List<AutomationSuggestionEntity> = withContext(Dispatchers.IO) {
        
        try {
            val prompt = buildPrompt(emotions, thoughts, contacts, timeOfDay, recentHistory, deviceCapabilities)
            val response = generativeModel.generateContent(prompt)
            
            return@withContext parseGeminiResponse(
                response.text ?: "",
                checkInId,
                contacts
            )
        } catch (e: Exception) {
            // Fallback to basic suggestions if Gemini fails
            return@withContext emptyList()
        }
    }
    
    private fun buildPrompt(
        emotions: List<String>,
        thoughts: String?,
        contacts: List<ContactEntity>,
        timeOfDay: String,
        recentHistory: List<String>,
        deviceCapabilities: DeviceDiscoveryService.DeviceCapabilities?
    ): String {
        val contactInfo = contacts.take(3).joinToString(", ") { 
            "${it.name} (${it.relationship})" 
        }
        
        // Build device context string
        val deviceContext = deviceCapabilities?.let { caps ->
            buildString {
                append("AVAILABLE SMART HOME DEVICES:\n")
                if (caps.lightCount > 0) {
                    append("- ${caps.lightCount} lights")
                    if (caps.hasDimmableLights) append(" (dimmable)")
                    if (caps.hasColorLights) append(" (color-changing)")
                    append("\n")
                }
                if (caps.thermostatCount > 0) {
                    append("- ${caps.thermostatCount} thermostats\n")
                }
                if (caps.speakerCount > 0) {
                    append("- ${caps.speakerCount} speakers\n")
                }
                if (caps.sensorCount > 0) {
                    append("- ${caps.sensorCount} sensors (motion/contact)\n")
                }
                if (caps.hasSwitches) append("- Smart switches\n")
                if (caps.hasOutlets) append("- Smart outlets\n")
                
                if (caps.lightCount == 0 && caps.thermostatCount == 0 && caps.speakerCount == 0) {
                    append("- No smart home devices detected\n")
                }
                append("\n")
            }
        } ?: "SMART HOME DEVICES: Not available\n\n"
        
        return """
You are a compassionate AI therapy assistant helping someone who just completed an emotional check-in. 
Provide 1-3 specific, actionable suggestions for immediate support.

EMOTIONAL STATE:
- Current emotions: ${emotions.joinToString(", ")}
- Thoughts: ${thoughts ?: "Not provided"}
- Time of day: $timeOfDay
- Available contacts: $contactInfo
- Recent patterns: ${recentHistory.joinToString("; ")}

$deviceContext

SMART HOME AUTOMATION GUIDELINES:
- If lights are available: suggest calming/energizing lighting based on emotions
- If thermostats available: optimize temperature for comfort (22°C for relaxation, 23°C for energy)
- If multiple devices: create comprehensive environment scenarios
- If no devices: focus on manual environment tips and social/wellness suggestions
- Use specific device counts in descriptions (e.g., "across your 3 lights")

RESPONSE FORMAT (JSON):
{
  "suggestions": [
    {
      "title": "Brief, caring title",
      "description": "Empathetic explanation mentioning specific devices if available",
      "type": "SOCIAL_SUPPORT|SMART_ENVIRONMENT|WELLNESS|THERAPEUTIC|EMERGENCY",
      "priority": "LOW|MEDIUM|HIGH|URGENT", 
      "actions": [
        {
          "type": "CALL_CONTACT|SMART_HOME_ENVIRONMENT|THERAPEUTIC_ACTIVITY|REMINDER|GEMINI_CHAT|MANUAL_GUIDANCE",
          "displayText": "What the user sees",
          "parameters": {
            "environment": "anxiety_relief|mood_boost|focus_clarity|deep_relaxation",
            "duration": "15",
            "deviceCount": "number_of_devices_involved"
          }
        }
      ],
      "reasoning": "Brief therapeutic rationale explaining device choice",
      "duration": "estimated time"
    }
  ]
}

GUIDELINES:
- Prioritize safety: detect crisis indicators and suggest professional help
- Be specific about available devices and their therapeutic benefits
- Consider time of day and device capabilities together
- Match lighting/temperature to emotional needs
- For severe distress: include crisis hotline (988) regardless of devices
- Use warm, non-clinical language
- Limit to 3 suggestions maximum

Respond with valid JSON only.
        """.trimIndent()
    }
    
    private fun parseGeminiResponse(
        response: String,
        checkInId: String,
        contacts: List<ContactEntity>
    ): List<AutomationSuggestionEntity> {
        return try {
            // Extract JSON from response (in case there's extra text)
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return emptyList()
            }
            
            val jsonResponse = response.substring(jsonStart, jsonEnd)
            val suggestions = parseJsonSuggestions(jsonResponse, checkInId, contacts)
            
            // Validate and limit suggestions
            suggestions.take(3).filter { it.title.isNotBlank() }
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseJsonSuggestions(
        json: String,
        checkInId: String,
        contacts: List<ContactEntity>
    ): List<AutomationSuggestionEntity> {
        // This is a simplified JSON parser for the specific format
        // In production, you'd use a proper JSON library like Gson with data classes
        
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Extract suggestions array (simplified parsing)
        val suggestionsMatch = Regex(""""suggestions":\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
            .find(json) ?: return emptyList()
            
        val suggestionsContent = suggestionsMatch.groupValues[1]
        
        // Split individual suggestions (basic approach)
        val individualSuggestions = suggestionsContent.split("},{").map { 
            if (!it.startsWith("{")) "{$it" else it
        }.map { 
            if (!it.endsWith("}")) "$it}" else it
        }
        
        for (suggestionJson in individualSuggestions) {
            try {
                val suggestion = parseSingleSuggestion(suggestionJson, checkInId, contacts)
                if (suggestion != null) {
                    suggestions.add(suggestion)
                }
            } catch (e: Exception) {
                continue // Skip malformed suggestions
            }
        }
        
        return suggestions
    }
    
    private fun parseSingleSuggestion(
        json: String,
        checkInId: String,
        contacts: List<ContactEntity>
    ): AutomationSuggestionEntity? {
        try {
            // Extract basic fields using regex (simplified approach)
            val title = extractJsonField(json, "title") ?: return null
            val description = extractJsonField(json, "description") ?: return null
            val type = extractJsonField(json, "type") ?: "WELLNESS"
            val priority = extractJsonField(json, "priority") ?: "MEDIUM"
            val reasoning = extractJsonField(json, "reasoning") ?: "AI-generated suggestion"
            val duration = extractJsonField(json, "duration") ?: "5-10 minutes"
            
            // Parse actions (simplified)
            val actions = parseActions(json, contacts)
            
            return AutomationSuggestionEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkInId,
                title = title,
                description = description,
                type = type,
                priority = priority,
                actions = actions,
                geminiReasoning = reasoning,
                estimatedDuration = duration,
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun extractJsonField(json: String, fieldName: String): String? {
        val pattern = """"$fieldName":\s*"([^"]*?)"""
        return Regex(pattern).find(json)?.groupValues?.get(1)
    }
    
    private fun parseActions(json: String, contacts: List<ContactEntity>): List<AutomationActionData> {
        val actions = mutableListOf<AutomationActionData>()
        
        // Extract actions array (very simplified)
        val actionsMatch = Regex(""""actions":\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
            .find(json)
        
        actionsMatch?.let { match ->
            val actionsContent = match.groupValues[1]
            
            // For each action in the array (simplified split)
            val actionItems = actionsContent.split("},{").map { 
                if (!it.startsWith("{")) "{$it" else it
            }.map { 
                if (!it.endsWith("}")) "$it}" else it
            }
            
            for (actionJson in actionItems) {
                val actionType = extractJsonField(actionJson, "type") ?: "REMINDER"
                val displayText = extractJsonField(actionJson, "displayText") ?: "Take action"
                
                // Handle contact-based actions
                val parameters = when (actionType) {
                    "CALL_CONTACT" -> {
                        val contact = contacts.firstOrNull()
                        contact?.let {
                            mapOf(
                                "phoneNumber" to it.phoneNumber,
                                "contactName" to it.name
                            )
                        } ?: emptyMap()
                    }
                    else -> emptyMap()
                }
                
                actions.add(
                    AutomationActionData(
                        type = actionType,
                        targetId = if (actionType == "CALL_CONTACT") contacts.firstOrNull()?.id else null,
                        parameters = parameters,
                        displayText = displayText
                    )
                )
            }
        }
        
        return actions
    }
}