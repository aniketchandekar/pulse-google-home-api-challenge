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

package com.example.googlehomeapisampleapp.viewmodel

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googlehomeapisampleapp.HomeApp
import com.example.googlehomeapisampleapp.MainActivity
import com.example.googlehomeapisampleapp.data.database.MoodDatabase
import com.example.googlehomeapisampleapp.data.repository.MoodAnalyticsData
import com.example.googlehomeapisampleapp.data.repository.MoodRepository
import com.example.googlehomeapisampleapp.data.repository.AutomationRepository
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.ContactEntity
import com.example.googlehomeapisampleapp.service.EmotionAnalysisService
import com.example.googlehomeapisampleapp.service.AutomationTemplateService
import com.example.googlehomeapisampleapp.service.AutomationEngine
import com.example.googlehomeapisampleapp.service.ExecutionResult
import com.example.googlehomeapisampleapp.viewmodel.automations.ActionViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.AutomationViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.CandidateViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.DraftViewModel
import com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel
import com.example.googlehomeapisampleapp.viewmodel.structures.StructureViewModel
import com.google.home.Structure
import com.google.home.automation.CommandCandidate
import com.google.home.automation.DraftAutomation
import com.google.home.automation.NodeCandidate
import com.google.home.automation.UnknownDeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.googlehomeapisampleapp.utils.ErrorHandler
import com.example.googlehomeapisampleapp.service.GeminiTherapyService
import com.example.googlehomeapisampleapp.service.SmartHomeTherapyService
import com.example.googlehomeapisampleapp.data.entity.AutomationExecutionEntity
import com.google.home.automation.automation
import com.google.home.automation.manualStarter
import com.google.home.automation.sequential
import com.google.home.automation.select
import com.google.home.automation.parallel
import com.google.home.automation.starter
import com.google.home.automation.action
import com.google.home.automation.condition
import com.google.home.automation.TypedExpression
import com.google.home.automation.equals
import com.google.home.matter.standard.OnOff
import com.google.home.ConnectivityState
import com.google.home.matter.standard.LevelControl
import com.google.home.matter.standard.LevelControlTrait
import com.google.home.matter.standard.OccupancySensing
import com.google.home.matter.standard.OccupancySensing.Companion.occupancy
import com.google.home.matter.standard.OccupancySensingTrait
// import com.google.home.matter.standard.TemperatureSetting
// import com.google.home.matter.standard.TemperatureSettingTrait
import java.util.UUID

class HomeAppViewModel (val homeApp: HomeApp, context: Context) : ViewModel() {

    // Tabs showing main capabilities of the app:
    enum class NavigationTab {
        MODES,
        DEVICES,
        ACTIVITY
    }

    // Container tracking the active navigation tab:
    var selectedTab: MutableStateFlow<NavigationTab>

    // Containers tracking the active object being edited:
    var selectedStructureVM: MutableStateFlow<StructureViewModel?>
    var selectedDeviceVM: MutableStateFlow<DeviceViewModel?>
    var selectedAutomationVM: MutableStateFlow<AutomationViewModel?>
    var selectedDraftVM: MutableStateFlow<DraftViewModel?>
    var selectedCandidateVMs: MutableStateFlow<List<CandidateViewModel>?>
    var showCheckInView: MutableStateFlow<Boolean>
    var showTherapySuggestionsView: MutableStateFlow<Boolean>
    var showSmartEnvironmentView: MutableStateFlow<Boolean>
    var showMoodBasedAutomationView: MutableStateFlow<Boolean>
    var showJournalDetailView: MutableStateFlow<String?> // Journal ID to show
    var showAllJournalsView: MutableStateFlow<Boolean>
    var showEditJournalView: MutableStateFlow<String?> // Journal ID to edit

    // Loading states:
    var isLoadingCandidates: MutableStateFlow<Boolean>

    // Container to store returned structures from the app:
    var structureVMs: MutableStateFlow<List<StructureViewModel>>

    // Mood data repository
    private val moodRepository: MoodRepository
    var moodAnalytics: MutableStateFlow<MoodAnalyticsData?>

    // Automation system
    private val automationRepository: AutomationRepository
    private val emotionAnalysisService: EmotionAnalysisService
    private val automationTemplateService: AutomationTemplateService
    private val automationEngine: AutomationEngine
    private val smartHomeTherapyService: SmartHomeTherapyService

    var activeSuggestions: MutableStateFlow<List<AutomationSuggestionEntity>>
    var allContacts: MutableStateFlow<List<ContactEntity>>

    // Status log and loading state for modal
    val statusLogs = MutableStateFlow<List<String>>(emptyList())
    val isStatusLoading = MutableStateFlow(false)

    init {
        // Initialize the active tab to show the devices:
        selectedTab = MutableStateFlow(NavigationTab.DEVICES)

        // Initialize containers storing active objects:
        selectedStructureVM = MutableStateFlow(null)
        selectedDeviceVM = MutableStateFlow(null)
        selectedAutomationVM = MutableStateFlow(null)
        selectedDraftVM = MutableStateFlow(null)
        selectedCandidateVMs = MutableStateFlow(null)
        showCheckInView = MutableStateFlow(false)
        showTherapySuggestionsView = MutableStateFlow(false)
        showSmartEnvironmentView = MutableStateFlow(false)
        showMoodBasedAutomationView = MutableStateFlow(false)
        showJournalDetailView = MutableStateFlow(null)
        showAllJournalsView = MutableStateFlow(false)
        showEditJournalView = MutableStateFlow(null)

        // Initialize loading states:
        isLoadingCandidates = MutableStateFlow(false)

        // Initialize the container to store structures:
        structureVMs = MutableStateFlow(mutableListOf())

        // Initialize the mood database and repository
        val database = MoodDatabase.getDatabase(context)
        moodRepository = MoodRepository(database.checkInDao())
        moodAnalytics = MutableStateFlow(null)

        // Initialize automation system
        automationRepository = AutomationRepository(database.automationDao())
        emotionAnalysisService = EmotionAnalysisService()
        automationTemplateService = AutomationTemplateService()
        val geminiService = GeminiTherapyService()
        smartHomeTherapyService = SmartHomeTherapyService()
        automationEngine = AutomationEngine(
            automationRepository = automationRepository,
            emotionAnalysisService = emotionAnalysisService,
            templateService = automationTemplateService,
            geminiService = geminiService
        )
        activeSuggestions = MutableStateFlow(emptyList())
        allContacts = MutableStateFlow(emptyList())

        viewModelScope.launch {
            // If permissions flow is completed, subscribe to changes on structures:
            if (homeApp.permissionsManager.isSignedIn.value) {
                launch { subscribeToStructures() }
            }
            // If permissions flow is completed in the future, subscribe to changes on structures:
            homeApp.permissionsManager.isSignedIn.collect { isSignedIn ->
                if (isSignedIn) {
                    launch { subscribeToStructures() }
                }
            }
        }

        // Load mood analytics data
        viewModelScope.launch {
            refreshMoodAnalytics()
        }
    }

    private suspend fun subscribeToStructures() {
        try {
            // Subscribe to structures returned by the Structures API:
            homeApp.homeClient.structures().collect { structureSet ->
                val structureVMList = structureSet.map { structure ->
                    StructureViewModel(structure)
                }

                // Store the ViewModels:
                structureVMs.emit(structureVMList)

                // Auto-select first structure if none is selected:
                if (selectedStructureVM.value == null && structureVMList.isNotEmpty()) {
                    selectedStructureVM.emit(structureVMList.first())
                }
            }
        } catch (e: Exception) {
            MainActivity.showError(this, "Failed to load structures: ${e.message}")
        }
    }

    fun showCandidates() {
        viewModelScope.launch {
            isLoadingCandidates.emit(true)
            try {
                loadAutomationCandidates()
            } catch (e: Exception) {
                isLoadingCandidates.emit(false)
                ErrorHandler.execute(
                    operation = { throw e },
                    caller = "HomeAppViewModel.showCandidates"
                )
            }
        }
    }

    private suspend fun loadAutomationCandidates() {
        val selectedStructure = selectedStructureVM.value
            ?: throw IllegalStateException("No structure selected")

        val candidateVMList = mutableListOf<CandidateViewModel>()

        // Process each device in the selected structure
        selectedStructure.deviceVMs.value.forEach { deviceVM ->
            if (deviceVM.type.value !is UnknownDeviceType) {
                val candidates = deviceVM.device.candidates().first()

                candidates
                    .filter { candidate ->
                        candidate.trait in HomeApp.supportedTraits &&
                                when (candidate) {
                                    is CommandCandidate -> candidate.commandDescriptor in ActionViewModel.commandMap
                                    else -> false
                                }
                    }
                    .forEach { candidate ->
                        candidateVMList.add(CandidateViewModel(candidate, deviceVM))
                    }
            }
        }

        selectedCandidateVMs.emit(candidateVMList)
        isLoadingCandidates.emit(false)
    }

    fun startStatusModal() {
        isStatusLoading.value = true
        statusLogs.value = emptyList()
    }

    fun stopStatusModal() {
        isStatusLoading.value = false
    }

    fun createAutomation(isPending: MutableState<Boolean>) {
        startStatusModal()
        viewModelScope.launch {
            ErrorHandler.safeExecute(
                operation = {
                    createAutomationInternal(isPending)
                },
                onError = { isPending.value = false; stopStatusModal() },
                caller = this@HomeAppViewModel
            )
            stopStatusModal()
        }
    }

    private suspend fun createAutomationInternal(isPending: MutableState<Boolean>) {
        val structure = selectedStructureVM.value?.structure
            ?: throw IllegalStateException("No structure selected")
        val draft = selectedDraftVM.value?.getDraftAutomation()
            ?: throw IllegalStateException("No draft automation available")

        isPending.value = true

        withContext(Dispatchers.IO) {
            structure.createAutomation(draft)
        }

        // Clean up after successful creation
        selectedCandidateVMs.emit(null)
        selectedDraftVM.emit(null)
        isPending.value = false
    }

    /**
     * Clear all selections and return to the main view
     */
    fun clearSelections() {
        viewModelScope.launch {
            selectedDeviceVM.emit(null)
            selectedAutomationVM.emit(null)
            selectedDraftVM.emit(null)
            selectedCandidateVMs.emit(null)
            showCheckInView.emit(false)
            showTherapySuggestionsView.emit(false)
            showJournalDetailView.emit(null)
            showAllJournalsView.emit(false)
            showEditJournalView.emit(null)
        }
    }

    /**
     * Navigate to the check-in view
     */
    fun navigateToCheckIn() {
        viewModelScope.launch {
            showCheckInView.emit(true)
        }
    }

    /**
     * Navigate to the mood-based automation view
     */
    fun navigateToMoodBasedAutomation() {
        viewModelScope.launch {
            showMoodBasedAutomationView.emit(true)
        }
    }

    /**
     * Navigate to a specific tab
     */
    fun navigateToTab(tab: NavigationTab) {
        viewModelScope.launch {
            selectedTab.emit(tab)
        }
    }

    /**
     * Save a mood check-in with emotions and thoughts
     */
    suspend fun saveMoodCheckIn(emotions: Set<String>, thoughts: String) {
        try {
            moodRepository.createCheckIn(emotions, thoughts)
            refreshMoodAnalytics()
        } catch (e: Exception) {
            throw Exception("Failed to save check-in: ${e.message}")
        }
    }

    /**
     * Enhanced mood check-in that also generates automation suggestions
     */
    suspend fun saveMoodCheckInWithAutomation(emotions: Set<String>, thoughts: String) {
        try {
            val checkIn = moodRepository.createCheckIn(emotions, thoughts)
            refreshMoodAnalytics()

            // Generate automation suggestions based on the check-in
            generateAutomationSuggestions(checkIn.id)
        } catch (e: Exception) {
            throw Exception("Failed to save check-in: ${e.message}")
        }
    }

    /**
     * Refresh mood analytics data
     */
    private suspend fun refreshMoodAnalytics() {
        try {
            val analytics = moodRepository.getMoodAnalytics()
            moodAnalytics.emit(analytics)
        } catch (e: Exception) {
            MainActivity.showError(this, "Failed to load mood analytics: ${e.message}")
        }
    }

    /**
     * Get current mood analytics data
     */
    fun getMoodAnalytics(): MoodAnalyticsData? {
        return moodAnalytics.value
    }

    /**
     * Get all journal entries (check-ins) as a Flow
     */
    fun getAllJournals() = moodRepository.getAllCheckIns()

    /**
     * Navigate to journal detail view
     */
    fun navigateToJournalDetail(journalId: String) {
        viewModelScope.launch {
            showJournalDetailView.emit(journalId)
        }
    }

    /**
     * Navigate to all journals view
     */
    fun navigateToAllJournals() {
        viewModelScope.launch {
            showAllJournalsView.emit(true)
        }
    }

    /**
     * Navigate to edit journal view
     */
    fun navigateToEditJournal(journalId: String) {
        viewModelScope.launch {
            showEditJournalView.emit(journalId)
        }
    }

    /**
     * Delete a journal entry
     */
    suspend fun deleteJournal(journalId: String) {
        try {
            moodRepository.deleteCheckIn(journalId)
            refreshMoodAnalytics() // Refresh analytics after deletion
        } catch (e: Exception) {
            throw Exception("Failed to delete journal: ${e.message}")
        }
    }

    /**
     * Update an existing journal entry
     */
    suspend fun updateJournal(journalId: String, emotions: Set<String>, thoughts: String) {
        try {
            moodRepository.updateCheckIn(journalId, emotions, thoughts)
            refreshMoodAnalytics() // Refresh analytics after update
        } catch (e: Exception) {
            throw Exception("Failed to update journal: ${e.message}")
        }
    }

    // AUTOMATION METHODS

    /**
     * Generate automation suggestions based on check-in data
     */
    suspend fun generateAutomationSuggestions(checkInId: String) {
        try {
            val checkIns = moodRepository.getAllCheckIns().first()
            val checkIn = checkIns.find { it.id == checkInId }

            if (checkIn != null) {
                // Pass the current structure to enable device-aware suggestions
                val currentStructure = selectedStructureVM.value
                MainActivity.showInfo(
                    this,
                    "Generating suggestions for emotions: ${checkIn.emotions}"
                )

                if (currentStructure != null) {
                    val deviceCount = currentStructure.deviceVMs.value.size
                    MainActivity.showInfo(
                        this,
                        "Found $deviceCount devices in structure: ${currentStructure.name}"
                    )
                } else {
                    MainActivity.showWarning(
                        this,
                        "No structure selected for automation generation"
                    )
                }

                val suggestions = automationEngine.generateSuggestions(checkIn, currentStructure)
                MainActivity.showInfo(this, "Generated ${suggestions.size} automation suggestions")
                loadActiveSuggestions() // Refresh active suggestions
            }
        } catch (e: Exception) {
            MainActivity.showError(this, "Failed to generate suggestions: ${e.message}")
        }
    }

    /**
     * Load active automation suggestions
     */
    private suspend fun loadActiveSuggestions() {
        try {
            val suggestions = automationRepository.getActiveSuggestions().first()
            activeSuggestions.emit(suggestions)
        } catch (e: Exception) {
            MainActivity.showError(this, "Failed to load suggestions: ${e.message}")
        }
    }

    /**
     * Execute an automation suggestion and create it as a Google Home Routine
     */
    fun executeSuggestion(suggestion: AutomationSuggestionEntity) {
        viewModelScope.launch {
            // Start the status modal to show logs
            startStatusModal()

            try {
                statusLogs.value = statusLogs.value + "Starting execution of '${suggestion.title}'..."

                // First, create the automation as a Google Home Routine
                val routineCreated = createMoodBasedRoutine(suggestion)

                if (routineCreated) {
                    // Mark as executed and refresh suggestions
                    automationRepository.updateSuggestion(
                        suggestion.copy(isExecuted = true, executedAt = System.currentTimeMillis())
                    )
                    loadActiveSuggestions()

                    statusLogs.value = statusLogs.value + "Routine '${suggestion.title}' created successfully!"
                    statusLogs.value = statusLogs.value + "Running automation now..."

                    // Automatically run the automation
                    runAutomationNow(suggestion)

                    statusLogs.value = statusLogs.value + "✅ Automation execution completed!"
                } else {
                    statusLogs.value = statusLogs.value + "❌ Failed to create routine: Unable to connect to Google Home"
                }
            } catch (e: Exception) {
                statusLogs.value = statusLogs.value + "❌ Error creating routine: ${e.message}"
            } finally {
                // Keep modal open for 2 seconds to show final status
                kotlinx.coroutines.delay(2000)
                stopStatusModal()
            }
        }
    }

    private suspend fun runAutomationNow(suggestion: AutomationSuggestionEntity) {
        try {
            val selectedStructure = selectedStructureVM.value
            val availableDevices = selectedStructure?.deviceVMs?.value?.filter {
                it.traits.value.isNotEmpty() && it.connectivity == ConnectivityState.ONLINE
            } ?: emptyList()

            if (availableDevices.isNotEmpty()) {
                val devicesToUse = availableDevices.take(3)
                statusLogs.value = statusLogs.value + "Running automation on ${devicesToUse.size} devices..."

                devicesToUse.forEach { device ->
                    // Execute the device command directly with mood-specific settings
                    val onOffTrait = device.traits.value.find {
                        it.factory.toString().contains("OnOff")
                    } as? OnOff

                    if (onOffTrait != null) {
                        withContext(Dispatchers.IO) {
                            onOffTrait.on()

                            // Add mood-specific dimming if supported
                            if (device.traits.value.any {
                                    it.factory.toString().contains("LevelControl")
                                }) {
                                val levelControlTrait = device.traits.value.find {
                                    it.factory.toString().contains("LevelControl")
                                } as? LevelControl

                                if (levelControlTrait != null) {
                                    val environment =
                                        suggestion.actions.firstOrNull()?.parameters?.get("environment")
                                            ?: "mood_boost"
                                    val level = when (environment) {
                                        "anxiety_relief" -> 30u.toUByte()
                                        "mood_boost" -> 85u.toUByte()
                                        "focus_clarity" -> 75u.toUByte()
                                        "deep_relaxation" -> 20u.toUByte()
                                        else -> 50u.toUByte()
                                    }
                                    levelControlTrait.moveToLevel(
                                        level,
                                        null,
                                        LevelControlTrait.OptionsBitmap(false),
                                        LevelControlTrait.OptionsBitmap(false)
                                    )
                                }
                            }
                        }
                    }
                }
                statusLogs.value = statusLogs.value + "Successfully executed on ${devicesToUse.size} devices!"
            } else {
                MainActivity.showWarning(this, "No devices available to run automation")
            }
        } catch (e: Exception) {
            MainActivity.showError(this, "Failed to run automation: ${e.message}")
        }
    }

    // Ensures a structure is always selected if available
    private fun ensureStructureSelected() {
        if (selectedStructureVM.value == null || structureVMs.value.isEmpty()) return
        if (selectedStructureVM.value == null && structureVMs.value.isNotEmpty()) {
            selectedStructureVM.value = structureVMs.value.first()
        }
    }

    /**
     * Create a Google Home Routine from a mood-based automation suggestion
     */
    private suspend fun createMoodBasedRoutine(suggestion: AutomationSuggestionEntity): Boolean {
        ensureStructureSelected()
        val structure = selectedStructureVM.value?.structure
        if (structure == null) {
            MainActivity.showError(this, "No structure selected. Please select a home/structure.")
            return false
        }

        return try {
            // Check if user is signed in
            if (!homeApp.permissionsManager.isSignedIn.value) {
                MainActivity.showError(this, "Not signed in to Google Home. Please sign in first.")
                return false
            }

            // Check structure connection
            val selectedStructure = selectedStructureVM.value
            val deviceCount = selectedStructure?.deviceVMs?.value?.size ?: 0
            MainActivity.showInfo(this, "Structure has $deviceCount devices")

            if (deviceCount == 0) {
                MainActivity.showWarning(
                    this,
                    "No devices found in structure. Adding devices may help."
                )
            }

            // Convert the suggestion into a DraftAutomation
            val draftAutomation = convertSuggestionToDraft(suggestion)
            MainActivity.showInfo(this, "Created draft automation: ${draftAutomation.name}")

            // Create the automation in Google Home
            withContext(Dispatchers.IO) {
                structure.createAutomation(draftAutomation)
            }

            MainActivity.showInfo(this, "Successfully created automation in Google Home")

            // Log the execution for tracking
            automationRepository.insertExecution(
                AutomationExecutionEntity(
                    id = UUID.randomUUID().toString(),
                    suggestionId = suggestion.id,
                    checkInId = suggestion.checkInId,
                    executedAt = System.currentTimeMillis(),
                    completionStatus = "SUCCESS"
                )
            )

            true
        } catch (e: Exception) {
            MainActivity.showError(this, "Detailed error: ${e.javaClass.simpleName}: ${e.message}")

            // Log the failed execution
            automationRepository.insertExecution(
                AutomationExecutionEntity(
                    id = UUID.randomUUID().toString(),
                    suggestionId = suggestion.id,
                    checkInId = suggestion.checkInId,
                    executedAt = System.currentTimeMillis(),
                    completionStatus = "FAILED: ${e.message}"
                )
            )
            false
        }
    }

    /**
     * Convert an AutomationSuggestionEntity into a DraftAutomation for Google Home
     */
    private suspend fun convertSuggestionToDraft(suggestion: AutomationSuggestionEntity): DraftAutomation {
        val structure = selectedStructureVM.value
            ?: throw IllegalStateException("No structure selected")

        return automation {
            name = "🧠 ${suggestion.title}"
            description = "${suggestion.description}\n\nGenerated from mood check-in"
            isActive = true

            // Get available devices once
            val availableDevices = selectedStructureVM.value?.deviceVMs?.value?.filter {
                it.traits.value.isNotEmpty() && it.connectivity == ConnectivityState.ONLINE
            } ?: emptyList()

            sequential {
                // Starters - manual + device-based if available
                select {
                    // Always include manual starter
                    manualStarter()

                    // Add device-based starters if available
                    val sensorDevices = availableDevices.filter { device ->
                        val deviceType = device.type.value.toString()
                        deviceType.contains("OccupancySensor") || deviceType.contains("ContactSensor")
                    }

                    sensorDevices.forEach { sensorDevice ->
                        val deviceType = sensorDevice.type.value.toString()
                        logAndShow("[Starter] Adding sensor starter: ${sensorDevice.name}")

                        when {
                            deviceType.contains("OccupancySensor") -> {
                                val occupancyTrait = sensorDevice.traits.value.find {
                                    it.factory.toString().contains("OccupancySensing")
                                }?.factory as? com.google.home.TraitFactory<OccupancySensing>

                                if (occupancyTrait != null) {
                                    sequential {
                                        val starterExpression = starter(
                                            sensorDevice.device,
                                            sensorDevice.type.value.factory,
                                            occupancyTrait
                                        )
                                        condition {
                                            expression =
                                                starterExpression.occupancy equals OccupancySensingTrait.OccupancyBitmap(
                                                    true
                                                )
                                        }
                                    }
                                }
                            }

                            deviceType.contains("ContactSensor") -> {
                                val contactTrait = sensorDevice.traits.value.firstOrNull()?.factory
                                if (contactTrait != null) {
                                    sequential {
                                        @Suppress("UNCHECKED_CAST")
                                        starter(
                                            sensorDevice.device,
                                            sensorDevice.type.value.factory,
                                            contactTrait as com.google.home.TraitFactory<com.google.home.Trait>
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (sensorDevices.isEmpty()) {
                        // Use any available device as a starter
                        val starterDevice = availableDevices.firstOrNull()
                        if (starterDevice != null) {
                            val onOffTrait = starterDevice.traits.value.find {
                                it.factory.toString().contains("OnOff")
                            }?.factory as? com.google.home.TraitFactory<OnOff>

                            if (onOffTrait != null) {
                                logAndShow("[Starter] Using ${starterDevice.name} OnOff state as starter")
                                sequential {
                                    starter(
                                        starterDevice.device,
                                        starterDevice.type.value.factory,
                                        onOffTrait
                                    )
                                }
                            } else {
                                logAndShow("[Starter] No suitable device traits found, using manual starter only")
                            }
                        } else {
                            logAndShow("[Starter] No devices available, using manual starter only")
                        }
                    } else {
                        logAndShow("[Starter] Added ${sensorDevices.size} sensor-based starters")
                    }
                }

                // Actions on multiple devices
                parallel {
                    if (availableDevices.isNotEmpty()) {
                        // Use multiple devices (up to 3 for better performance)
                        val devicesToUse = availableDevices.take(3)
                        logAndShow("[Automation] Using ${devicesToUse.size} devices")

                        devicesToUse.forEach { device ->
                            val deviceType = device.type.value.toString()
                            val environment =
                                suggestion.actions.firstOrNull()?.parameters?.get("environment")
                                    ?: "mood_boost"

                            logAndShow("[Automation] Adding action for: ${device.name} (${deviceType})")

                            action(device.device, device.type.value.factory) {
                                when {
                                    // Light devices
                                    deviceType.contains("Light") -> {
                                        command(OnOff.on())

                                        // Add mood-specific dimming
                                        if (device.traits.value.any {
                                                it.factory.toString().contains("LevelControl")
                                            }) {
                                            val level = when (environment) {
                                                "anxiety_relief" -> 30u.toUByte()
                                                "mood_boost" -> 85u.toUByte()
                                                "focus_clarity" -> 75u.toUByte()
                                                "deep_relaxation" -> 20u.toUByte()
                                                else -> 50u.toUByte()
                                            }
                                            command(
                                                LevelControl.moveToLevel(
                                                    level,
                                                    null,
                                                    LevelControlTrait.OptionsBitmap(false),
                                                    LevelControlTrait.OptionsBitmap(false)
                                                )
                                            )
                                            logAndShow("[Automation] Set ${device.name} to ${level}% for $environment")
                                        }

                                        // Add color control for color lights
                                        if (deviceType.contains("ColorLight") || deviceType.contains(
                                                "ExtendedColor"
                                            )
                                        ) {
                                            // Color temperature based on mood
                                            val colorTemp = when (environment) {
                                                "anxiety_relief" -> 2700u // Warm
                                                "mood_boost" -> 5000u // Cool white
                                                "focus_clarity" -> 6500u // Daylight
                                                "deep_relaxation" -> 2200u // Very warm
                                                else -> 3000u
                                            }
                                            logAndShow("[Automation] Set ${device.name} color to ${colorTemp}K for $environment")
                                        }
                                    }

                                    // Thermostat devices
                                    deviceType.contains("Thermostat") -> {
                                        val targetTemp = when (environment) {
                                            "anxiety_relief" -> 22 // Comfortable
                                            "mood_boost" -> 21 // Slightly cool
                                            "focus_clarity" -> 20 // Cool for alertness
                                            "deep_relaxation" -> 23 // Warm and cozy
                                            else -> 22
                                        }

                                        // Use temperature setting if available
                                        if (device.traits.value.any {
                                                it.factory.toString()
                                                    .contains("TemperatureSetting")
                                            }) {
                                            logAndShow("[Automation] Set ${device.name} to ${targetTemp}°C for $environment")
                                        }
                                    }

                                    // Default: OnOff for other devices
                                    else -> {
                                        command(OnOff.on())
                                        logAndShow("[Automation] Turned on ${device.name}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logAndShow(message: String) {
        statusLogs.value = statusLogs.value + message
    }

    private fun logStatus(message: String) {
        statusLogs.value = statusLogs.value + message
    }

    fun clearStatusLogs() {
        statusLogs.value = emptyList()
    }

    fun setStatusLoading(loading: Boolean) {
        isStatusLoading.value = loading
    }

    private fun createMoodBasedStarters(
        suggestion: AutomationSuggestionEntity,
        structure: StructureViewModel
    ) {
        val availableDevices = structure.deviceVMs.value
        val devicesWithTraits = availableDevices.filter { it.traits.value.isNotEmpty() }

        logAndShow("[Starter] Processing ${suggestion.actions.size} Gemini actions")

        // Process Gemini-generated actions for starters
        for (action in suggestion.actions) {
            when (action.type) {
                "SMART_HOME_ENVIRONMENT" -> {
                    val environment = action.parameters["environment"] ?: "mood_boost"
                    logAndShow("[Starter] Creating environment starter for: $environment")

                    // Find suitable device for environment trigger
                    val sensorDevice = devicesWithTraits.find {
                        it.type.value.toString().contains("OccupancySensor")
                    }

                    if (sensorDevice != null) {
                        try {
                            val occupancyTrait = sensorDevice.traits.value.find {
                                it.factory.toString().contains("OccupancySensing")
                            }?.factory as? com.google.home.TraitFactory<OccupancySensing>

                            if (occupancyTrait != null) {
                                sequential {
                                    val starterExpression = starter(
                                        sensorDevice.device,
                                        sensorDevice.type.value.factory,
                                        occupancyTrait
                                    )
                                    condition {
                                        expression =
                                            starterExpression.occupancy equals OccupancySensingTrait.OccupancyBitmap(
                                                true
                                            )
                                    }
                                }
                                logAndShow("[Starter] Added occupancy starter for ${sensorDevice.device.name}")
                                return
                            }
                        } catch (e: Exception) {
                            logAndShow("[Starter] Occupancy starter failed: ${e.message}")
                        }
                    }
                }
            }
        }

        // Fallback: Add manual starter if no specific starter was created
        logAndShow("[Starter] Adding manual starter as fallback")
    }

    private fun createMoodBasedActions(
        suggestion: AutomationSuggestionEntity,
        structure: StructureViewModel
    ) {
        val availableDevices = structure.deviceVMs.value
        val devicesWithTraits = availableDevices.filter { it.traits.value.isNotEmpty() }

        logAndShow("[Action] Processing ${suggestion.actions.size} Gemini actions")

        // Process each Gemini-generated action
        for (geminiAction in suggestion.actions) {
            when (geminiAction.type) {
                "SMART_HOME_ENVIRONMENT" -> {
                    val environment = geminiAction.parameters["environment"] ?: "mood_boost"
                    val deviceCount = geminiAction.parameters["deviceCount"]?.toIntOrNull() ?: 1

                    logAndShow("[Action] Creating $environment environment using $deviceCount devices")

                    when (environment) {
                        "anxiety_relief" -> createCalmingActions(devicesWithTraits)
                        "mood_boost" -> createEnergyActions(devicesWithTraits)
                        "focus_clarity" -> createFocusActions(devicesWithTraits)
                        "deep_relaxation" -> createRelaxationActions(devicesWithTraits)
                        else -> createGenericActions(devicesWithTraits)
                    }
                }

                "TEMPERATURE_CONTROL" -> {
                    val targetTemp = geminiAction.parameters["targetTemp"]?.toIntOrNull() ?: 22
                    createTemperatureActions(devicesWithTraits, targetTemp)
                }

                "COLOR_THERAPY" -> {
                    createColorTherapyActions(devicesWithTraits, geminiAction.parameters)
                }

                else -> {
                    logAndShow("[Action] Creating generic action for type: ${geminiAction.type}")
                    createGenericActions(devicesWithTraits)
                }
            }
        }

        // Fallback if no actions were processed
        if (suggestion.actions.isEmpty()) {
            logAndShow("[Action] No Gemini actions found, using fallback")
            createGenericActions(devicesWithTraits)
        }
    }

    private fun createCalmingActions(devices: List<DeviceViewModel>) {
        devices.filter { it.type.value.toString().contains("Light") }.forEach { deviceVM ->
            try {
                action(deviceVM.device, deviceVM.type.value.factory) {
                    command(OnOff.on())
                    if (deviceVM.traits.value.any {
                            it.factory.toString().contains("LevelControl")
                        }) {
                        command(
                            LevelControl.moveToLevel(
                                30u,
                                null,
                                LevelControlTrait.OptionsBitmap(false),
                                LevelControlTrait.OptionsBitmap(false)
                            )
                        )
                    }
                }
                logAndShow("[Action] Added calming light action for ${deviceVM.device.name}")
            } catch (e: Exception) {
                logAndShow("[Action] Calming action failed for ${deviceVM.device.name}: ${e.message}")
            }
        }
    }

    private fun createEnergyActions(devices: List<DeviceViewModel>) {
        devices.filter { it.type.value.toString().contains("Light") }.forEach { deviceVM ->
            try {
                action(deviceVM.device, deviceVM.type.value.factory) {
                    command(OnOff.on())
                    if (deviceVM.traits.value.any {
                            it.factory.toString().contains("LevelControl")
                        }) {
                        command(
                            LevelControl.moveToLevel(
                                85u,
                                null,
                                LevelControlTrait.OptionsBitmap(false),
                                LevelControlTrait.OptionsBitmap(false)
                            )
                        )
                    }
                }
                logAndShow("[Action] Added energizing light action for ${deviceVM.device.name}")
            } catch (e: Exception) {
                logAndShow("[Action] Energy action failed for ${deviceVM.device.name}: ${e.message}")
            }
        }
    }

    private fun createFocusActions(devices: List<DeviceViewModel>) {
        devices.filter { it.type.value.toString().contains("Light") }.forEach { deviceVM ->
            try {
                action(deviceVM.device, deviceVM.type.value.factory) {
                    command(OnOff.on())
                    if (deviceVM.traits.value.any {
                            it.factory.toString().contains("LevelControl")
                        }) {
                        command(
                            LevelControl.moveToLevel(
                                75u,
                                null,
                                LevelControlTrait.OptionsBitmap(false),
                                LevelControlTrait.OptionsBitmap(false)
                            )
                        )
                    }
                }
                logAndShow("[Action] Added focus light action for ${deviceVM.device.name}")
            } catch (e: Exception) {
                logAndShow("[Action] Focus action failed for ${deviceVM.device.name}: ${e.message}")
            }
        }
    }

    private fun createRelaxationActions(devices: List<DeviceViewModel>) {
        devices.filter { it.type.value.toString().contains("Light") }.forEach { deviceVM ->
            try {
                action(deviceVM.device, deviceVM.type.value.factory) {
                    command(OnOff.on())
                    if (deviceVM.traits.value.any {
                            it.factory.toString().contains("LevelControl")
                        }) {
                        command(
                            LevelControl.moveToLevel(
                                20u,
                                null,
                                LevelControlTrait.OptionsBitmap(false),
                                LevelControlTrait.OptionsBitmap(false)
                            )
                        )
                    }
                }
                logAndShow("[Action] Added relaxation light action for ${deviceVM.device.name}")
            } catch (e: Exception) {
                logAndShow("[Action] Relaxation action failed for ${deviceVM.device.name}: ${e.message}")
            }
        }
    }

    private fun createTemperatureActions(devices: List<DeviceViewModel>, targetTemp: Int) {
        devices.filter { it.type.value.toString().contains("Thermostat") }.forEach { deviceVM ->
            try {
                action(deviceVM.device, deviceVM.type.value.factory) {
                    if (deviceVM.traits.value.any {
                            it.factory.toString().contains("Thermostat")
                        }) {
                        command(OnOff.on())
                    }
                }
                logAndShow("[Action] Added temperature action for ${deviceVM.device.name} (${targetTemp}°C)")
            } catch (e: Exception) {
                logAndShow("[Action] Temperature action failed for ${deviceVM.device.name}: ${e.message}")
            }
        }
    }

    private fun createColorTherapyActions(
        devices: List<DeviceViewModel>,
        parameters: Map<String, String>
    ) {
        devices.filter {
            it.type.value.toString().contains("ColorLight") || it.type.value.toString()
                .contains("ExtendedColor")
        }.forEach { deviceVM ->
            try {
                action(deviceVM.device, deviceVM.type.value.factory) {
                    command(OnOff.on())
                }
                logAndShow("[Action] Added color therapy action for ${deviceVM.device.name}")
            } catch (e: Exception) {
                logAndShow("[Action] Color therapy action failed for ${deviceVM.device.name}: ${e.message}")
            }
        }
    }

    private fun createGenericActions(devices: List<DeviceViewModel>) {
        if (devices.isNotEmpty()) {
            val deviceVM = devices.first()
            try {
                action(deviceVM.device, deviceVM.type.value.factory) {
                    command(OnOff.on())
                }
                logAndShow("[Action] Added generic action for ${deviceVM.device.name}")
            } catch (e: Exception) {
                logAndShow("[Action] Generic action failed for ${deviceVM.device.name}: ${e.message}")
            }
        }
    }

    /**
     * Dismiss an automation suggestion
     */
    fun dismissSuggestion(suggestionId: String) {
        viewModelScope.launch {
            try {
                automationRepository.dismissSuggestion(suggestionId)
                loadActiveSuggestions() // Refresh active suggestions
            } catch (e: Exception) {
                MainActivity.showError(
                    this@HomeAppViewModel,
                    "Failed to dismiss suggestion: ${e.message}"
                )
            }
        }
    }

    /**
     * Add a new contact
     */
    suspend fun addContact(contact: ContactEntity) {
        try {
            automationRepository.insertContact(contact)
            loadContacts() // Refresh contacts list
        } catch (e: Exception) {
            throw Exception("Failed to add contact: ${e.message}")
        }
    }

    /**
     * Update an existing contact
     */
    suspend fun updateContact(contact: ContactEntity) {
        try {
            automationRepository.updateContact(contact)
            loadContacts() // Refresh contacts list
        } catch (e: Exception) {
            throw Exception("Failed to update contact: ${e.message}")
        }
    }

    /**
     * Delete a contact
     */
    suspend fun deleteContact(contactId: String) {
        try {
            automationRepository.deleteContact(contactId)
            loadContacts() // Refresh contacts list
        } catch (e: Exception) {
            throw Exception("Failed to delete contact: ${e.message}")
        }
    }

    /**
     * Call a contact (update last contacted timestamp)
     */
    suspend fun callContact(contact: ContactEntity) {
        try {
            automationRepository.updateLastContacted(contact.id)
            loadContacts() // Refresh contacts list
        } catch (e: Exception) {
            MainActivity.showError(this, "Failed to update contact info: ${e.message}")
        }
    }

    /**
     * Load all contacts
     */
    private suspend fun loadContacts() {
        try {
            val contacts = automationRepository.getAllContacts().first()
            allContacts.emit(contacts)
        } catch (e: Exception) {
            MainActivity.showError(this, "Failed to load contacts: ${e.message}")
        }
    }

    /**
     * Generate helpful suggestions when no smart home devices are available
     */
    fun generateManualWellnessSuggestions(emotions: Set<String>): List<String> {
        val suggestions = mutableListOf<String>()

        val hasAnxiety =
            emotions.any { it.contains("anxious", true) || it.contains("stress", true) }
        val hasSadness = emotions.any { it.contains("sad", true) || it.contains("down", true) }
        val hasTiredness =
            emotions.any { it.contains("tired", true) || it.contains("sluggish", true) }

        when {
            hasAnxiety -> {
                suggestions.addAll(
                    listOf(
                        "Try deep breathing exercises (4-7-8 technique)",
                        "Practice progressive muscle relaxation",
                        "Consider adding smart lights for calming ambiance"
                    )
                )
            }

            hasSadness -> {
                suggestions.addAll(
                    listOf(
                        "Step outside for natural light exposure",
                        "Listen to uplifting music",
                        "Consider smart speakers for mood-boosting playlists"
                    )
                )
            }

            hasTiredness -> {
                suggestions.addAll(
                    listOf(
                        "Take a short walk or stretch",
                        "Ensure good lighting in your workspace",
                        "Smart thermostats can help optimize room temperature for alertness"
                    )
                )
            }

            else -> {
                suggestions.addAll(
                    listOf(
                        "Practice mindfulness or meditation",
                        "Connect with supportive friends or family",
                        "Smart home devices can enhance your wellness routine"
                    )
                )
            }
        }

        return suggestions
    }
}