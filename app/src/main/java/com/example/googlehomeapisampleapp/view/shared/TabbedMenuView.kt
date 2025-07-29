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

package com.example.googlehomeapisampleapp.view.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.googlehomeapisampleapp.R
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel

@Composable
fun TabbedMenuView (homeAppVM: HomeAppViewModel) {
    val selectedTab: HomeAppViewModel.NavigationTab = homeAppVM.selectedTab.collectAsState().value
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val tabColors = androidx.compose.material3.NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primary
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Pulse") },
            selected = selectedTab == HomeAppViewModel.NavigationTab.MODES,
            onClick = { homeAppVM.navigateToTab(HomeAppViewModel.NavigationTab.MODES) },
            colors = tabColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Smartphone, contentDescription = null) },
            label = { Text("Devices") },
            selected = selectedTab == HomeAppViewModel.NavigationTab.DEVICES,
            onClick = { homeAppVM.navigateToTab(HomeAppViewModel.NavigationTab.DEVICES) },
            colors = tabColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.DirectionsRun, contentDescription = null) },
            label = { Text("Routines") },
            selected = selectedTab == HomeAppViewModel.NavigationTab.ACTIVITY,
            onClick = { homeAppVM.navigateToTab(HomeAppViewModel.NavigationTab.ACTIVITY) },
            colors = tabColors
        )
    }
}