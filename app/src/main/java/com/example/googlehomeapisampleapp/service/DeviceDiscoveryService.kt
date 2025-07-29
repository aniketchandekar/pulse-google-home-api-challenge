package com.example.googlehomeapisampleapp.service

import android.util.Log
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationActionData
import com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel
import com.example.googlehomeapisampleapp.viewmodel.structures.StructureViewModel
import com.google.home.matter.standard.*
import java.util.UUID

/**
 * Device Discovery Service
 * Analyzes available devices and generates device-aware automation suggestions
 */
class DeviceDiscoveryService {
    
    data class DeviceCapabilities(
        val hasLights: Boolean = false,
        val hasDimmableLights: Boolean = false,
        val hasColorLights: Boolean = false,
        val hasThermostats: Boolean = false,
        val hasSpeakers: Boolean = false,
        val hasMotionSensors: Boolean = false,
        val hasContactSensors: Boolean = false,
        val hasSwitches: Boolean = false,
        val hasOutlets: Boolean = false,
        val lightCount: Int = 0,
        val thermostatCount: Int = 0,
        val speakerCount: Int = 0,
        val sensorCount: Int = 0,
        val rooms: Set<String> = emptySet()
    )
    
    data class SmartSuggestion(
        val title: String,
        val description: String,
        val requiredDevices: List<String>,
        val actions: List<AutomationActionData>,
        val priority: String = "MEDIUM",
        val reasoning: String
    )
    
    /**
     * Analyze all available devices and their capabilities
     */
    fun analyzeDeviceCapabilities(structureVM: StructureViewModel?): DeviceCapabilities {
        val devices = structureVM?.deviceVMs?.value ?: emptyList()
        
        var hasLights = false
        var hasDimmableLights = false
        var hasColorLights = false
        var hasThermostats = false
        var hasSpeakers = false
        var hasMotionSensors = false
        var hasContactSensors = false
        var hasSwitches = false
        var hasOutlets = false
        
        var lightCount = 0
        var thermostatCount = 0
        var speakerCount = 0
        var sensorCount = 0
        
        val rooms = mutableSetOf<String>()
        
        for (device in devices) {
            val deviceType = device.type.value.factory
            val traits = device.traits.value
            
            // Add room information if available
            // Note: Room information would come from device.roomId if available
            
            when (deviceType) {
                OnOffLightDevice -> {
                    hasLights = true
                    lightCount++
                }
                DimmableLightDevice -> {
                    hasLights = true
                    hasDimmableLights = true
                    lightCount++
                }
                ColorTemperatureLightDevice -> {
                    hasLights = true
                    hasDimmableLights = true
                    hasColorLights = true
                    lightCount++
                }
                ExtendedColorLightDevice -> {
                    hasLights = true
                    hasDimmableLights = true
                    hasColorLights = true
                    lightCount++
                }
                ThermostatDevice -> {
                    hasThermostats = true
                    thermostatCount++
                }
                SpeakerDevice -> {
                    hasSpeakers = true
                    speakerCount++
                }
                OccupancySensorDevice -> {
                    hasMotionSensors = true
                    sensorCount++
                }
                ContactSensorDevice -> {
                    hasContactSensors = true
                    sensorCount++
                }
                GenericSwitchDevice, OnOffLightSwitchDevice -> {
                    hasSwitches = true
                }
                OnOffPluginUnitDevice -> {
                    hasOutlets = true
                }
            }
        }
        
        Log.d("DeviceDiscovery", "Device analysis complete: " +
                "Lights: $lightCount, Thermostats: $thermostatCount, " +
                "Speakers: $speakerCount, Sensors: $sensorCount")
        
        return DeviceCapabilities(
            hasLights = hasLights,
            hasDimmableLights = hasDimmableLights,
            hasColorLights = hasColorLights,
            hasThermostats = hasThermostats,
            hasSpeakers = hasSpeakers,
            hasMotionSensors = hasMotionSensors,
            hasContactSensors = hasContactSensors,
            hasSwitches = hasSwitches,
            hasOutlets = hasOutlets,
            lightCount = lightCount,
            thermostatCount = thermostatCount,
            speakerCount = speakerCount,
            sensorCount = sensorCount,
            rooms = rooms
        )
    }
    
    /**
     * Generate device-aware automation suggestions based on emotional state and available devices
     */
    fun generateDeviceAwareSuggestions(
        emotions: List<String>,
        capabilities: DeviceCapabilities,
        checkInId: String,
        priority: String = "MEDIUM",
        structureVM: StructureViewModel? = null
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        val availableDevices = structureVM?.deviceVMs?.value ?: emptyList()
        
        // Analyze emotional state to determine needs
        val needsCalming = emotions.any { it.contains("anxious", true) || it.contains("stress", true) }
        val needsEnergy = emotions.any { it.contains("sad", true) || it.contains("tired", true) }
        val needsFocus = emotions.any { it.contains("distracted", true) || it.contains("overwhelmed", true) }
        val needsComfort = emotions.any { it.contains("lonely", true) || it.contains("down", true) }
        
        // Generate suggestions based on available devices and needs
        when {
            needsCalming -> suggestions.addAll(generateCalmingSuggestions(capabilities, checkInId, priority, availableDevices))
            needsEnergy -> suggestions.addAll(generateEnergizingSuggestions(capabilities, checkInId, priority, availableDevices))
            needsFocus -> suggestions.addAll(generateFocusSuggestions(capabilities, checkInId, priority, availableDevices))
            needsComfort -> suggestions.addAll(generateComfortSuggestions(capabilities, checkInId, priority, availableDevices))
            else -> suggestions.addAll(generateGeneralWellnessSuggestions(capabilities, checkInId, priority, availableDevices))
        }
        
        // Add device-specific automation suggestions
        suggestions.addAll(generateDeviceSpecificSuggestions(capabilities, emotions, checkInId, priority, availableDevices))
        
        return suggestions.distinctBy { it.title }.take(3) // Remove duplicates and limit to 3
    }
    
    private fun generateCalmingSuggestions(
        capabilities: DeviceCapabilities,
        checkInId: String,
        priority: String,
        availableDevices: List<DeviceViewModel>
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Get specific light devices
        val lightDevices = availableDevices.filter { 
            it.type.value.toString().contains("Light") 
        }
        
        // Lighting-based calming
        if (capabilities.hasDimmableLights && lightDevices.isNotEmpty()) {
            val deviceNames = lightDevices.take(3).joinToString(", ") { it.name }
            val moreDevices = if (lightDevices.size > 3) " and ${lightDevices.size - 3} more" else ""
            
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Calming Light Environment",
                    description = "Dim lights for relaxation: $deviceNames$moreDevices",
                    type = "SMART_ENVIRONMENT",
                    priority = priority,
                    actions = listOf(
                        AutomationActionData(
                            type = "SMART_HOME_ENVIRONMENT",
                            parameters = mapOf(
                                "environment" to "anxiety_relief",
                                "duration" to "15",
                                "deviceCount" to capabilities.lightCount.toString(),
                                "deviceNames" to deviceNames,
                                "starterDevices" to getStarterDeviceInfo(availableDevices)
                            ),
                            displayText = "Dim $deviceNames to 30% for calming effect"
                        )
                    ),
                    geminiReasoning = "Warm, dim lighting creates a soothing environment that naturally reduces anxiety and stress",
                    estimatedDuration = "15 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        // Temperature-based calming
        val thermostatDevices = availableDevices.filter { 
            it.type.value.toString().contains("Thermostat") 
        }
        
        if (capabilities.hasThermostats && thermostatDevices.isNotEmpty()) {
            val thermostatNames = thermostatDevices.joinToString(", ") { it.name }
            
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Comfort Temperature",
                    description = "Set comfortable temperature on: $thermostatNames",
                    type = "SMART_ENVIRONMENT",
                    priority = priority,
                    actions = listOf(
                        AutomationActionData(
                            type = "TEMPERATURE_CONTROL",
                            parameters = mapOf(
                                "targetTemp" to "22",
                                "mode" to "auto",
                                "deviceNames" to thermostatNames
                            ),
                            displayText = "Set $thermostatNames to 22°C for comfort"
                        )
                    ),
                    geminiReasoning = "Comfortable temperature supports emotional regulation and physical comfort",
                    estimatedDuration = "Immediate",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
    
    private fun generateEnergizingSuggestions(
        capabilities: DeviceCapabilities,
        checkInId: String,
        priority: String,
        availableDevices: List<DeviceViewModel>
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        if (capabilities.hasLights) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Energizing Light Boost",
                    description = "Brighten ${capabilities.lightCount} lights to boost energy and mood",
                    type = "SMART_ENVIRONMENT",
                    priority = priority,
                    actions = listOf(
                        AutomationActionData(
                            type = "SMART_HOME_ENVIRONMENT",
                            parameters = mapOf(
                                "environment" to "mood_boost",
                                "duration" to "20",
                                "brightness" to "85"
                            ),
                            displayText = "Activate energizing lighting"
                        )
                    ),
                    geminiReasoning = "Bright, cool lighting across ${capabilities.lightCount} devices helps combat low energy and improves alertness",
                    estimatedDuration = "20 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
    
    private fun generateFocusSuggestions(
        capabilities: DeviceCapabilities,
        checkInId: String,
        priority: String,
        availableDevices: List<DeviceViewModel>
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        if (capabilities.hasLights && capabilities.hasThermostats) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Optimal Focus Environment",
                    description = "Create ideal conditions for concentration using ${capabilities.lightCount} lights and ${capabilities.thermostatCount} thermostats",
                    type = "SMART_ENVIRONMENT",
                    priority = priority,
                    actions = listOf(
                        AutomationActionData(
                            type = "SMART_HOME_ENVIRONMENT",
                            parameters = mapOf(
                                "environment" to "focus_clarity",
                                "duration" to "25"
                            ),
                            displayText = "Activate focus environment"
                        )
                    ),
                    geminiReasoning = "Bright, cool lighting and optimal temperature create an environment that enhances cognitive function and reduces distractions",
                    estimatedDuration = "25 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
    
    private fun generateComfortSuggestions(
        capabilities: DeviceCapabilities,
        checkInId: String,
        priority: String,
        availableDevices: List<DeviceViewModel>
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Multi-device comfort environment
        if (capabilities.hasLights || capabilities.hasThermostats) {
            val deviceTypes = mutableListOf<String>()
            if (capabilities.hasLights) deviceTypes.add("${capabilities.lightCount} lights")
            if (capabilities.hasThermostats) deviceTypes.add("${capabilities.thermostatCount} thermostats")
            if (capabilities.hasSpeakers) deviceTypes.add("${capabilities.speakerCount} speakers")
            
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Cozy Comfort Environment",
                    description = "Create a warm, comforting atmosphere using ${deviceTypes.joinToString(", ")}",
                    type = "SMART_ENVIRONMENT",
                    priority = priority,
                    actions = listOf(
                        AutomationActionData(
                            type = "SMART_HOME_ENVIRONMENT",
                            parameters = mapOf(
                                "environment" to "deep_relaxation",
                                "duration" to "30"
                            ),
                            displayText = "Activate comfort environment"
                        )
                    ),
                    geminiReasoning = "A comprehensive comfort environment using multiple device types addresses emotional needs holistically",
                    estimatedDuration = "30 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
    
    private fun generateGeneralWellnessSuggestions(
        capabilities: DeviceCapabilities,
        checkInId: String,
        priority: String,
        availableDevices: List<DeviceViewModel>
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Adaptive environment based on available devices
        val deviceCount = capabilities.lightCount + capabilities.thermostatCount + capabilities.speakerCount
        
        if (deviceCount > 0) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Adaptive Wellness Environment",
                    description = "Optimize your environment using all $deviceCount available smart devices",
                    type = "SMART_ENVIRONMENT",
                    priority = priority,
                    actions = listOf(
                        AutomationActionData(
                            type = "SMART_HOME_ENVIRONMENT",
                            parameters = mapOf(
                                "environment" to "mood_boost",
                                "adaptive" to "true",
                                "deviceCount" to deviceCount.toString()
                            ),
                            displayText = "Optimize all smart devices for wellness"
                        )
                    ),
                    geminiReasoning = "Using all $deviceCount available devices creates a comprehensive environment that supports overall well-being",
                    estimatedDuration = "20 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
    
    private fun generateDeviceSpecificSuggestions(
        capabilities: DeviceCapabilities,
        emotions: List<String>,
        checkInId: String,
        priority: String,
        availableDevices: List<DeviceViewModel>
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        // Sensor-based automation suggestions
        if (capabilities.hasMotionSensors && capabilities.hasLights) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Gentle Movement Response",
                    description = "Set lights to respond gently to movement for comfort",
                    type = "AUTOMATION_RULE",
                    priority = "LOW",
                    actions = listOf(
                        AutomationActionData(
                            type = "CREATE_MOTION_AUTOMATION",
                            parameters = mapOf(
                                "trigger" to "motion_detected",
                                "response" to "gentle_lighting",
                                "sensorCount" to capabilities.sensorCount.toString()
                            ),
                            displayText = "Create gentle motion-activated lighting"
                        )
                    ),
                    geminiReasoning = "Motion-activated gentle lighting provides comfort and security without being jarring during emotional distress",
                    estimatedDuration = "Setup: 2 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        // Color light specific suggestions
        if (capabilities.hasColorLights) {
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Color Therapy Lighting",
                    description = "Use color-changing lights for mood enhancement",
                    type = "SMART_ENVIRONMENT",
                    priority = priority,
                    actions = listOf(
                        AutomationActionData(
                            type = "COLOR_THERAPY",
                            parameters = mapOf(
                                "colorMode" to "therapeutic",
                                "emotions" to emotions.joinToString(","),
                                "colorLightCount" to capabilities.lightCount.toString()
                            ),
                            displayText = "Activate therapeutic color lighting"
                        )
                    ),
                    geminiReasoning = "Color therapy using your color-changing lights can positively influence mood and emotional state",
                    estimatedDuration = "15 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
    
    /**
     * Generate fallback suggestions when limited devices are available
     */
    fun generateFallbackSuggestions(
        capabilities: DeviceCapabilities,
        checkInId: String
    ): List<AutomationSuggestionEntity> {
        val suggestions = mutableListOf<AutomationSuggestionEntity>()
        
        if (!capabilities.hasLights && !capabilities.hasThermostats && !capabilities.hasSpeakers) {
            // No smart devices available - suggest manual alternatives
            suggestions.add(
                AutomationSuggestionEntity(
                    id = UUID.randomUUID().toString(),
                    checkInId = checkInId,
                    title = "Manual Environment Optimization",
                    description = "Simple steps to improve your environment without smart devices",
                    type = "MANUAL_SUGGESTION",
                    priority = "MEDIUM",
                    actions = listOf(
                        AutomationActionData(
                            type = "MANUAL_GUIDANCE",
                            parameters = mapOf(
                                "suggestion" to "manual_environment_tips"
                            ),
                            displayText = "View environment optimization tips"
                        )
                    ),
                    geminiReasoning = "Even without smart devices, simple environmental changes can significantly impact mood and well-being",
                    estimatedDuration = "5 minutes",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        
        return suggestions
    }
    
    private fun getStarterDeviceInfo(availableDevices: List<DeviceViewModel>): String {
        val starterInfo = mutableListOf<String>()
        
        availableDevices.take(2).forEach { deviceVM ->
            val deviceStatus = deviceVM.status.value
            val deviceName = deviceVM.name
            val traits = deviceVM.traits.value
            
            traits.forEach { trait ->
                val traitName = trait.factory.toString()
                when {
                    traitName.contains("OnOff") -> {
                        val isOn = deviceStatus.contains("On", ignoreCase = true)
                        starterInfo.add("$deviceName (OnOff: ${if (isOn) "On" else "Off"})")
                    }
                    traitName.contains("OccupancySensing") -> {
                        val isOccupied = deviceStatus.contains("Occupied", ignoreCase = true)
                        starterInfo.add("$deviceName (Occupancy: ${if (isOccupied) "Occupied" else "Vacant"})")
                    }
                    traitName.contains("BooleanState") -> {
                        starterInfo.add("$deviceName (Contact: $deviceStatus)")
                    }
                }
            }
        }
        
        return starterInfo.joinToString(", ")
    }
}