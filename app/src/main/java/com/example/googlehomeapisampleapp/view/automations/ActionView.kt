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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.R
import com.example.googlehomeapisampleapp.view.devices.LevelSlider
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.ActionViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.DraftViewModel
import com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel
import com.google.home.Trait
import com.google.home.matter.standard.LevelControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ActionView (homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    // Selected DraftViewModel and ActionViewModel on screen to select an action:
    val draftVM: DraftViewModel = homeAppVM.selectedDraftVM.collectAsState().value!!
    val actionVM: ActionViewModel = draftVM.selectedActionVM.collectAsState().value!!
    val actionVMs: List<ActionViewModel> = draftVM.actionVMs.collectAsState().value
    // Selected StructureViewModel and DeviceViewModels to provide options:
    val structureVM = homeAppVM.selectedStructureVM.collectAsState().value!!
    val deviceVMs = structureVM.deviceVMs.collectAsState().value
    // Selected values for ActionView on screen:
    val actionDeviceVM: MutableState<DeviceViewModel?> = remember {
        mutableStateOf(actionVM.deviceVM.value) }
    val actionTrait: MutableState<Trait?> = remember {
        mutableStateOf(actionVM.trait.value) }
    val actionAction: MutableState<ActionViewModel.Action?> = remember {
        mutableStateOf(actionVM.action.value) }
    val actionValueLevel: MutableState<UByte?> = remember {
        mutableStateOf(actionVM.valueLevel.value) }
    // Variables to track UI state for dropdown views:
    var expandedDeviceSelection: Boolean by remember { mutableStateOf(false) }
    var expandedTraitSelection: Boolean by remember { mutableStateOf(false) }
    var expandedActionSelection: Boolean by remember { mutableStateOf(false) }

    // Back action for closing view:
    BackHandler {
        scope.launch { draftVM.selectedActionVM.emit(null) }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
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
                        scope.launch { draftVM.selectedActionVM.emit(null) }
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
                    text = stringResource(R.string.action_title_select), 
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Device Selection Card
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeviceHub,
                            contentDescription = "Device",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_title_device),
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { expandedDeviceSelection = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = actionDeviceVM.value?.name ?: stringResource(R.string.action_text_select),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                        Box {
                            DropdownMenu(expanded = expandedDeviceSelection, onDismissRequest = { expandedDeviceSelection = false }) {
                                for (deviceVM in deviceVMs) {
                                    DropdownMenuItem(
                                        text = { Text(deviceVM.name) },
                                        onClick = {
                                            scope.launch {
                                                actionDeviceVM.value = deviceVM
                                                actionTrait.value = null
                                                actionAction.value = null
                                            }
                                            expandedDeviceSelection = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Trait Selection Card
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Trait",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_title_trait),
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { expandedTraitSelection = true },
                        enabled = actionDeviceVM.value != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = actionTrait.value?.factory?.toString() ?: stringResource(R.string.action_text_select),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                        Box {
                            DropdownMenu(expanded = expandedTraitSelection, onDismissRequest = { expandedTraitSelection = false }) {
                                val deviceTraits: List<Trait> = actionDeviceVM.value?.traits?.collectAsState()?.value!!
                                for (trait in deviceTraits) {
                                    DropdownMenuItem(
                                        text = { Text(trait.factory.toString()) },
                                        onClick = {
                                            scope.launch {
                                                actionTrait.value = trait
                                                actionAction.value = null
                                            }
                                            expandedTraitSelection = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Command Selection Card
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Command",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_title_command),
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { expandedActionSelection = true },
                        enabled = actionTrait.value != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = actionAction.value?.toString() ?: stringResource(R.string.action_text_select),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                        Box {
                            DropdownMenu(expanded = expandedActionSelection, onDismissRequest = { expandedActionSelection = false }) {
                                if (!ActionViewModel.actionActions.containsKey(actionTrait.value?.factory))
                                    return@DropdownMenu

                                val actions: List<ActionViewModel.Action> = ActionViewModel.actionActions.get(actionTrait.value?.factory)?.actions!!
                                for (action in actions) {
                                    DropdownMenuItem(
                                        text = { Text(action.toString()) },
                                        onClick = {
                                            scope.launch {
                                                actionAction.value = action
                                            }
                                            expandedActionSelection = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Value Selection Card (only show for LevelControl)
            when (actionTrait.value?.factory) {
                LevelControl -> {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Value",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.action_title_value),
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            LevelSlider(
                                value = actionValueLevel.value?.toFloat()!!, 
                                low = 0f, 
                                high = 254f, 
                                steps = 0,
                                modifier = Modifier.padding(top = 8.dp),
                                onValueChange = { value: Float -> actionValueLevel.value = value.toUInt().toUByte() },
                                isEnabled = true
                            )
                        }
                    }
                }
                else -> { /* No additional value input needed for other trait types */ }
            }

            // Add bottom padding to account for buttons
            Spacer(modifier = Modifier.height(120.dp))
        }

        // Enhanced bottom buttons with card container - positioned at bottom of Box
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
            Column(modifier = Modifier.padding(16.dp)) {
                // Check on whether all options are selected:
                val isOptionsSelected: Boolean =
                            actionDeviceVM.value != null &&
                            actionTrait.value != null &&
                            actionAction.value != null

                if (actionVMs.contains(actionVM)) {
                    // Update action button:
                    Button(
                        enabled = isOptionsSelected,
                        onClick = {
                            scope.launch {
                                actionVM.deviceVM.emit(actionDeviceVM.value)
                                actionVM.trait.emit(actionTrait.value)
                                actionVM.action.emit(actionAction.value)
                                actionVM.valueLevel.emit(actionValueLevel.value)

                                draftVM.selectedActionVM.emit(null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Update",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_button_update),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Remove action button:
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                draftVM.actionVMs.value.remove(actionVM)
                                draftVM.selectedActionVM.emit(null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_button_remove),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    // Save action button:
                    Button(
                        enabled = isOptionsSelected,
                        onClick = {
                            scope.launch {
                                actionVM.deviceVM.emit(actionDeviceVM.value)
                                actionVM.trait.emit(actionTrait.value)
                                actionVM.action.emit(actionAction.value)
                                actionVM.valueLevel.emit(actionValueLevel.value)

                                draftVM.actionVMs.value.add(actionVM)
                                draftVM.selectedActionVM.emit(null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Create",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_button_create),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

