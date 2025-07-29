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

package com.example.googlehomeapisampleapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.ui.theme.GoogleHomeAPISampleAppTheme
import com.example.googlehomeapisampleapp.view.automations.ActionView
import com.example.googlehomeapisampleapp.view.automations.AutomationView
import com.example.googlehomeapisampleapp.view.automations.AutomationsView
import com.example.googlehomeapisampleapp.view.automations.CandidatesView
import com.example.googlehomeapisampleapp.view.automations.DraftView
import com.example.googlehomeapisampleapp.view.automations.StarterView
import com.example.googlehomeapisampleapp.view.automation.MoodBasedAutomationView
import com.example.googlehomeapisampleapp.view.checkin.CheckInView
import com.example.googlehomeapisampleapp.view.therapy.TherapySuggestionsView
import com.example.googlehomeapisampleapp.view.devices.DeviceView
import com.example.googlehomeapisampleapp.view.devices.DevicesView
import com.example.googlehomeapisampleapp.view.journal.EditJournalView
import com.example.googlehomeapisampleapp.view.journal.JournalDetailView
import com.example.googlehomeapisampleapp.view.journal.AllJournalsView
import com.example.googlehomeapisampleapp.view.pulse.PulseView
import com.example.googlehomeapisampleapp.view.shared.TabbedMenuView
import com.example.googlehomeapisampleapp.view.smartenvironment.SmartEnvironmentView
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.ActionViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.AutomationViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.CandidateViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.DraftViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.StarterViewModel
import com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.example.googlehomeapisampleapp.utils.AppConstants
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button

@Composable
fun HomeAppView (homeAppVM: HomeAppViewModel) {
    /** Value tracking whether a user is signed-in on the app **/
    val isSignedIn: Boolean = homeAppVM.homeApp.permissionsManager.isSignedIn.collectAsState().value

    /** Values tracking what is being selected on the app **/
    val selectedTab: HomeAppViewModel.NavigationTab by homeAppVM.selectedTab.collectAsState()
    val selectedDeviceVM: DeviceViewModel? by homeAppVM.selectedDeviceVM.collectAsState()
    val selectedAutomationVM: AutomationViewModel? by homeAppVM.selectedAutomationVM.collectAsState()
    val selectedCandidateVMs: List<CandidateViewModel>? by homeAppVM.selectedCandidateVMs.collectAsState()
    val selectedDraftVM: DraftViewModel? by homeAppVM.selectedDraftVM.collectAsState()
    val selectedStarterVM: StarterViewModel? = selectedDraftVM?.selectedStarterVM?.collectAsState()?.value
    val selectedActionVM: ActionViewModel? = selectedDraftVM?.selectedActionVM?.collectAsState()?.value
    val showCheckInView: Boolean by homeAppVM.showCheckInView.collectAsState()
    val showJournalDetailView: String? by homeAppVM.showJournalDetailView.collectAsState()
    val showAllJournalsView: Boolean by homeAppVM.showAllJournalsView.collectAsState()
    val showEditJournalView: String? by homeAppVM.showEditJournalView.collectAsState()

    /**
     * Periodically refreshes permissions while the user is signed in.
     *
     * This loop helps ensure that permission state remains accurate in case
     * it changes outside the app(e.g., in Google Home or system settings).
     */
    LaunchedEffect(isSignedIn) {
        while (homeAppVM.homeApp.permissionsManager.isSignedIn.value) {
            homeAppVM.homeApp.permissionsManager.refreshPermissions()
            delay(AppConstants.API.PERMISSION_REFRESH_INTERVAL_MS)
        }
    }

    GoogleHomeAPISampleAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Primary content frame:
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    NavigationContent(
                        homeAppVM = homeAppVM,
                        isSignedIn = isSignedIn,
                        selectedTab = selectedTab,
                        selectedDeviceVM = selectedDeviceVM,
                        selectedAutomationVM = selectedAutomationVM,
                        selectedStarterVM = selectedStarterVM,
                        selectedActionVM = selectedActionVM,
                        selectedDraftVM = selectedDraftVM,
                        selectedCandidateVMs = selectedCandidateVMs,
                        showCheckInView = showCheckInView
                    )
                }
            }
            StatusModal(homeAppVM)
        }
    }
}

@Composable
private fun NavigationContent(
    homeAppVM: HomeAppViewModel,
    isSignedIn: Boolean,
    selectedTab: HomeAppViewModel.NavigationTab,
    selectedDeviceVM: DeviceViewModel?,
    selectedAutomationVM: AutomationViewModel?,
    selectedStarterVM: StarterViewModel?,
    selectedActionVM: ActionViewModel?,
    selectedDraftVM: DraftViewModel?,
    selectedCandidateVMs: List<CandidateViewModel>?,
    showCheckInView: Boolean
) {
    // Get journal navigation states
    val showJournalDetailView: String? by homeAppVM.showJournalDetailView.collectAsState()
    val showEditJournalView: String? by homeAppVM.showEditJournalView.collectAsState()
    val showAllJournalsView: Boolean by homeAppVM.showAllJournalsView.collectAsState()
    val showTherapySuggestionsView: Boolean by homeAppVM.showTherapySuggestionsView.collectAsState()
    val showSmartEnvironmentView: Boolean by homeAppVM.showSmartEnvironmentView.collectAsState()
    val showMoodBasedAutomationView: Boolean by homeAppVM.showMoodBasedAutomationView.collectAsState()
    
    // Extract the journal IDs to local variables for smart casting
    val journalIdToShow = showJournalDetailView
    val journalIdToEdit = showEditJournalView
    
    when {
        !isSignedIn -> WelcomeView(homeAppVM)
        showCheckInView -> CheckInView(homeAppVM)
        showTherapySuggestionsView -> TherapySuggestionsView(homeAppVM)
        showMoodBasedAutomationView -> MoodBasedAutomationView(homeAppVM)
        journalIdToEdit != null -> EditJournalView(homeAppVM, journalIdToEdit)
        journalIdToShow != null -> JournalDetailView(homeAppVM, journalIdToShow)
        showAllJournalsView -> AllJournalsView(homeAppVM)
        selectedDeviceVM != null -> DeviceView(homeAppVM)
        selectedAutomationVM != null -> AutomationView(homeAppVM)
        selectedStarterVM != null -> StarterView(homeAppVM)
        selectedActionVM != null -> ActionView(homeAppVM)
        selectedDraftVM != null -> DraftView(homeAppVM)
        selectedCandidateVMs != null -> CandidatesView(homeAppVM)
        showSmartEnvironmentView -> SmartEnvironmentView(homeAppVM)
        else -> {
            when (selectedTab) {
                HomeAppViewModel.NavigationTab.MODES -> ModesView(homeAppVM)
                HomeAppViewModel.NavigationTab.DEVICES -> DevicesView(homeAppVM)
                HomeAppViewModel.NavigationTab.ACTIVITY -> ActivityView(homeAppVM)
            }
        }
    }
}

@Composable
fun ModesView(homeAppVM: HomeAppViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            PulseView(homeAppVM)
        }
        TabbedMenuView(homeAppVM)
    }
}

@Composable
fun ActivityView(homeAppVM: HomeAppViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AutomationsView(homeAppVM)
        }
        TabbedMenuView(homeAppVM)
    }
}

@Composable
private fun PlaceholderContent(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppConstants.Dimensions.buttonPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = AppConstants.Typography.titleFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(AppConstants.Dimensions.buttonPadding))
        Text(
            text = subtitle,
            fontSize = AppConstants.Typography.subBodyFontSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatusModal(homeAppVM: HomeAppViewModel) {
    val isLoading by homeAppVM.isStatusLoading.collectAsState()
    val logs by homeAppVM.statusLogs.collectAsState()
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(24.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating automation...", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFF5F5F5))
                            .padding(8.dp)
                    ) {
                        logs.forEach { log ->
                            Text(log, fontSize = 14.sp, color = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        homeAppVM.setStatusLoading(false)
                        homeAppVM.clearStatusLogs()
                    }) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
