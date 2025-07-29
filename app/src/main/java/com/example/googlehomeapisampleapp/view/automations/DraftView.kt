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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.R
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.ActionViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.DraftViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.StarterViewModel
import com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel
import com.google.home.Trait
import com.google.home.TraitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DraftView (homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    // Selected DraftViewModel on screen to create a new automation:
    val draftVM: DraftViewModel = homeAppVM.selectedDraftVM.collectAsState().value!!
    val starterVMs: List<StarterViewModel> = draftVM.starterVMs.collectAsState().value
    val actionVMs: List<ActionViewModel> = draftVM.actionVMs.collectAsState().value
    // Editable text fields for the automation draft:
    val draftName: String = draftVM.name.collectAsState().value
    val draftDescription: String = draftVM.description.collectAsState().value

    val isPending: MutableState<Boolean> = remember { mutableStateOf(false) }

    // Back action for closing view:
    BackHandler {
        scope.launch { homeAppVM.selectedDraftVM.emit(null) }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column {
            // App bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        scope.launch { homeAppVM.selectedDraftVM.emit(null) }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = stringResource(R.string.draft_title), 
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Input fields in cards for better visual separation
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Name Input:
                    OutlinedTextField(
                        value = draftName, 
                        onValueChange = { scope.launch { draftVM.name.emit(it) } }, 
                        label = { Text(text = stringResource(R.string.draft_label_name), color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Description Input:
                    OutlinedTextField(
                        value = draftDescription, 
                        onValueChange = { scope.launch { draftVM.description.emit(it) } }, 
                        label = { Text(text = stringResource(R.string.draft_label_description), color = MaterialTheme.colorScheme.onSurface) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Expanding Container:
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(weight = 1f, fill = false)) {
                // Draft Starters:
                DraftStarterList(draftVM)
                // Draft Actions:
                DraftActionList(draftVM)
                
                // Add bottom padding to account for create button
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Enhanced prominent button to save the draft automation:
        Card(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Check on whether at least a starter and an action are selected:
            val isOptionsSelected: Boolean = starterVMs.isNotEmpty() && actionVMs.isNotEmpty()
            // Check on whether a name and description are provided:
            val isValueProvided: Boolean = draftName.isNotBlank() || draftDescription.isNotBlank()
            
            Button(
                enabled = isOptionsSelected && isValueProvided && !isPending.value,
                onClick = { homeAppVM.createAutomation(isPending) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isPending.value) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                }
                Text(
                    text = if (!isPending.value) stringResource(R.string.draft_button_create) else stringResource(R.string.draft_text_creating),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DraftStarterList (draftVM: DraftViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    // List of existing starters in this automation draft:
    val starterVMs: List<StarterViewModel> = draftVM.starterVMs.collectAsState().value

    // Starters title:
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Starters",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(R.string.draft_text_starters),
            fontSize = 16.sp, 
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    
    // For each starter, creating a StarterItem view:
    for (starterVM in starterVMs)
        DraftStarterItem(starterVM, draftVM)
    
    // Button for adding a new starter:
    OutlinedButton(
        onClick = {
            scope.launch { draftVM.selectedStarterVM.emit(StarterViewModel(null)) }
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Starter",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.draft_new_starter_name),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun DraftStarterItem (starterVM: StarterViewModel, draftVM: DraftViewModel) {
    val scope = rememberCoroutineScope()
    // Attributes of the starter item:
    val starterDeviceVM: DeviceViewModel = starterVM.deviceVM.collectAsState().value!!
    val starterTrait: TraitFactory<out Trait>? = starterVM.trait.collectAsState().value

    // Item to view and select the starter:
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch { draftVM.selectedStarterVM.emit(starterVM) }
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = starterDeviceVM.name, 
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = starterTrait.toString(), 
                    fontSize = 14.sp, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            FilterChip(
                onClick = { }, // Non-clickable, just for display
                label = {
                    Text(
                        text = "Trigger",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                selected = false,
                enabled = false // Makes it non-interactive
            )
        }
    }
}

@Composable
fun DraftActionList (draftVM: DraftViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    // List of existing starters in this automation draft:
    val actionVMs: List<ActionViewModel> = draftVM.actionVMs.collectAsState().value

    // Actions title:
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Actions",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(R.string.draft_text_actions),
            fontSize = 16.sp, 
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    
    // For each action, creating an ActionItem view:
    for (actionVM in actionVMs)
        DraftActionItem(actionVM, draftVM)
    
    // Button for adding a new action:
    OutlinedButton(
        onClick = {
            scope.launch { draftVM.selectedActionVM.emit(ActionViewModel(null)) }
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Action",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.draft_new_action_name),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun DraftActionItem (actionVM: ActionViewModel, draftVM: DraftViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    // Attributes of the action item:
    val actionDeviceVM: DeviceViewModel = actionVM.deviceVM.collectAsState().value!!
    val actionTrait: Trait? = actionVM.trait.collectAsState().value

    // Item to view and select the action:
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch { draftVM.selectedActionVM.emit(actionVM) }
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = actionDeviceVM.name, 
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = actionTrait?.factory.toString(), 
                    fontSize = 14.sp, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            FilterChip(
                onClick = { }, // Non-clickable, just for display
                label = {
                    Text(
                        text = "Action",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                selected = false,
                enabled = false // Makes it non-interactive
            )
        }
    }
}