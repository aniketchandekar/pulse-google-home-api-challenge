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

package com.example.googlehomeapisampleapp.view.automations

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.R
import com.example.googlehomeapisampleapp.utils.ErrorHandler.safelyLaunch
import com.example.googlehomeapisampleapp.view.shared.AccountButton
import com.example.googlehomeapisampleapp.view.shared.TabbedMenuView
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.AutomationViewModel
import com.example.googlehomeapisampleapp.viewmodel.structures.StructureViewModel
import com.google.home.automation.Action
import com.google.home.automation.Starter
import kotlinx.coroutines.CoroutineScope

@Composable
fun AutomationsView(homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val structureVMs: List<StructureViewModel> = homeAppVM.structureVMs.collectAsState().value
    val selectedStructureVM: StructureViewModel? = homeAppVM.selectedStructureVM.collectAsState().value
    val structureName: String = selectedStructureVM?.name ?: stringResource(R.string.automations_text_loading)
    val isLoadingCandidates: Boolean = homeAppVM.isLoadingCandidates.collectAsState().value
    
    Box(modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.fillMaxHeight()) {
            CombinedTopBar(
                homeAppVM = homeAppVM,
                structureVMs = structureVMs,
                structureName = structureName,
                onStructureSelected = { structure ->
                    scope.safelyLaunch(caller = "AutomationsView") {
                        homeAppVM.selectedStructureVM.emit(structure)
                    }
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                AutomationListComponent(homeAppVM, snackbarHostState)

                FloatingActionButton(
                    onClick = { homeAppVM.showCandidates() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    if (isLoadingCandidates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "Create automation")
                    }
                }
                
                // Full screen loading overlay when loading candidates
                if (isLoadingCandidates) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Loading automations...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Snackbar host positioned at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CombinedTopBar(
    homeAppVM: HomeAppViewModel,
    structureVMs: List<StructureViewModel>,
    structureName: String,
    onStructureSelected: (StructureViewModel) -> Unit
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    
    Box(
        Modifier
            .statusBarsPadding() // Add padding for status bar
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
            // Structure selector on the left
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { 
                    if (structureVMs.size > 1) expanded = true 
                }
            ) {
                Text(
                    text = structureName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (structureVMs.size > 1) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select structure",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Account button on the right
            AccountButton(homeAppVM)
        }
        
        // Dropdown menu for structure selection
        if (structureVMs.size > 1) {
            DropdownMenu(
                expanded = expanded, 
                onDismissRequest = { expanded = false },
                modifier = Modifier.padding(start = 0.dp)
            ) {
                for (structure in structureVMs) {
                    DropdownMenuItem(
                        text = { Text(structure.name) },
                        onClick = {
                            onStructureSelected(structure)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AutomationListItem(automationVM: AutomationViewModel, homeAppVM: HomeAppViewModel, snackbarHostState: SnackbarHostState) {
    val scope: CoroutineScope = rememberCoroutineScope()

    val automationName: String = automationVM.name.collectAsState().value
    val automationStarters: List<Starter> = automationVM.starters.collectAsState().value
    val automationActions: List<Action> = automationVM.actions.collectAsState().value
    val automationIsValid: Boolean = automationVM.isValid.collectAsState().value

    val status: String = "${automationStarters.size} starters • ${automationActions.size} actions"

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                scope.safelyLaunch(caller = "AutomationListItem") {
                    homeAppVM.selectedAutomationVM.emit(automationVM)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play button
            IconButton(
                onClick = {
                    scope.safelyLaunch(caller = "AutomationPlay") {
                        try {
                            automationVM.automation.execute()
                            // Show success snackbar
                            snackbarHostState.showSnackbar(
                                message = "$automationName ran successfully!",
                                withDismissAction = true
                            )
                        } catch (e: Exception) {
                            // Show error snackbar
                            snackbarHostState.showSnackbar(
                                message = "Failed to run $automationName: ${e.message}",
                                withDismissAction = true
                            )
                        }
                    }
                },
                enabled = automationIsValid,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run automation",
                    tint = if (automationIsValid) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Automation details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = automationName, 
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = status, 
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Settings icon
            IconButton(
                onClick = {
                    scope.safelyLaunch(caller = "AutomationSettings") {
                        homeAppVM.selectedAutomationVM.emit(automationVM)
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Automation settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AutomationListComponent(homeAppVM: HomeAppViewModel, snackbarHostState: SnackbarHostState) {
    val selectedStructureVM: StructureViewModel = 
        homeAppVM.selectedStructureVM.collectAsState().value ?: return

    val selectedAutomationVMs: List<AutomationViewModel> =
        selectedStructureVM.automationVMs.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // Automations section title
        Text(
            text = stringResource(R.string.automations_title), 
            fontSize = 18.sp, 
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // List of automations
        for (automationVM in selectedAutomationVMs) {
            AutomationListItem(automationVM, homeAppVM, snackbarHostState)
        }
        
        // Add bottom padding to account for FAB
        Box(modifier = Modifier.height(120.dp))
    }
}
