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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Outlet
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.R
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.CandidateViewModel
import com.example.googlehomeapisampleapp.viewmodel.automations.DraftViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CandidatesView (homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()

    BackHandler {
        scope.launch { homeAppVM.selectedCandidateVMs.emit(null) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                        scope.launch { homeAppVM.selectedCandidateVMs.emit(null) }
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
                    text = stringResource(R.string.candidate_button_create), 
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Candidates list
            CandidateListComponent(homeAppVM)
        }

        // FAB for creating new automation
        FloatingActionButton(
            onClick = { 
                scope.launch { homeAppVM.selectedDraftVM.emit(DraftViewModel(null)) }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create new automation")
        }
    }
}

@Composable
fun CandidateListComponent (homeAppVM: HomeAppViewModel) {
    val candidates: List<CandidateViewModel> = homeAppVM.selectedCandidateVMs.collectAsState().value ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Automation Suggestions", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(candidates.filter { it.name != "[]" }) { candidate ->
            CandidateListItem(candidate, homeAppVM)
        }

        // Add bottom padding to account for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CandidateListItem (candidateVM: CandidateViewModel, homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                scope.launch { homeAppVM.selectedDraftVM.emit(DraftViewModel(candidateVM)) }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Device icon based on candidate type
            Icon(
                imageVector = getCandidateIcon(candidateVM),
                contentDescription = candidateVM.name,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = candidateVM.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = getCandidateStatus(candidateVM),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

@Composable
fun getCandidateIcon(candidateVM: CandidateViewModel): ImageVector {
    return when {
        candidateVM.name.contains("Light", ignoreCase = true) -> Icons.Default.Lightbulb
        candidateVM.name.contains("Switch", ignoreCase = true) -> Icons.Default.ToggleOff
        candidateVM.name.contains("Outlet", ignoreCase = true) || 
        candidateVM.name.contains("Plug", ignoreCase = true) -> Icons.Default.Outlet
        candidateVM.name.contains("Thermostat", ignoreCase = true) -> Icons.Default.DeviceThermostat
        candidateVM.name.contains("Sensor", ignoreCase = true) -> Icons.Default.Sensors
        else -> Icons.Default.Home
    }
}

@Composable
fun getCandidateStatus(candidateVM: CandidateViewModel): String {
    return when {
        candidateVM.name.contains("ON", ignoreCase = true) -> "Turn On"
        candidateVM.name.contains("OFF", ignoreCase = true) -> "Turn Off"
        candidateVM.name.contains("HEAT", ignoreCase = true) -> "Heat Mode"
        candidateVM.name.contains("COOL", ignoreCase = true) -> "Cool Mode"
        candidateVM.name.contains("MOVE_TO_LEVEL", ignoreCase = true) -> "Set Level"
        else -> "Command"
    }
}
