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

package com.example.googlehomeapisampleapp.view.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Outlet
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.R
import com.example.googlehomeapisampleapp.ui.theme.DeviceColors
import com.example.googlehomeapisampleapp.utils.ErrorHandler
import com.example.googlehomeapisampleapp.utils.ErrorHandler.safelyLaunch
import com.example.googlehomeapisampleapp.view.shared.AccountButton
import com.example.googlehomeapisampleapp.view.shared.TabbedMenuView
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel
import com.example.googlehomeapisampleapp.viewmodel.structures.RoomViewModel
import com.example.googlehomeapisampleapp.viewmodel.structures.StructureViewModel
import com.google.home.ConnectivityState
import com.google.home.Trait
import com.google.home.matter.standard.ColorTemperatureLightDevice
import com.google.home.matter.standard.ContactSensorDevice
import com.google.home.matter.standard.DimmableLightDevice
import com.google.home.matter.standard.ExtendedColorLightDevice
import com.google.home.matter.standard.GenericSwitchDevice
import com.google.home.matter.standard.OccupancySensorDevice
import com.google.home.matter.standard.OnOffLightDevice
import com.google.home.matter.standard.OnOffLightSwitchDevice
import com.google.home.matter.standard.OnOffPluginUnitDevice
import com.google.home.matter.standard.OnOffSensorDevice
import com.google.home.matter.standard.OnOff
import com.google.home.matter.standard.ThermostatDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DevicesView(homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()

    val structureVMs: List<StructureViewModel> = homeAppVM.structureVMs.collectAsState().value
    val selectedStructureVM: StructureViewModel? = homeAppVM.selectedStructureVM.collectAsState().value
    val structureName: String = selectedStructureVM?.name ?: stringResource(R.string.devices_structure_loading)

    Column(modifier = Modifier.fillMaxHeight()) {
        CombinedTopBar(
            homeAppVM = homeAppVM,
            structureVMs = structureVMs,
            structureName = structureName,
            onStructureSelected = { structure ->
                scope.safelyLaunch(caller = "DevicesView") {
                    homeAppVM.selectedStructureVM.emit(structure)
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            DeviceGridComponent(homeAppVM)

            FloatingActionButton(
                onClick = { 
                    ErrorHandler.execute(
                        operation = { homeAppVM.homeApp.commissioningManager.requestCommissioning() },
                        caller = "DevicesView"
                    )
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.devices_button_add))
            }
        }

        TabbedMenuView(homeAppVM)
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
fun DeviceListItem(deviceVM: DeviceViewModel, homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val deviceStatus: String = deviceVM.status.collectAsState().value

    Column(
        Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                scope.safelyLaunch(caller = "DeviceListItem") {
                    homeAppVM.selectedDeviceVM.emit(deviceVM)
                }
            }
    ) {
        Text(deviceVM.name, fontSize = 20.sp)
        Text(deviceStatus, fontSize = 16.sp)
    }
}

@Composable
fun RoomListItem(roomVM: RoomViewModel) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
        Text(roomVM.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DeviceGridComponent(homeAppVM: HomeAppViewModel) {
    val selectedStructureVM: StructureViewModel =
        homeAppVM.selectedStructureVM.collectAsState().value ?: return

    val selectedRoomVMs: List<RoomViewModel> =
        selectedStructureVM.roomVMs.collectAsState().value

    val selectedDeviceVMsWithoutRooms: List<DeviceViewModel> =
        selectedStructureVM.deviceVMsWithoutRooms.collectAsState().value

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Devices in rooms
        items(selectedRoomVMs) { roomVM ->
            val deviceVMsInRoom: List<DeviceViewModel> = roomVM.deviceVMs.collectAsState().value
            
            if (deviceVMsInRoom.isNotEmpty()) {
                RoomSection(roomVM = roomVM, devices = deviceVMsInRoom, homeAppVM = homeAppVM)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Devices not in any room
        if (selectedDeviceVMsWithoutRooms.isNotEmpty()) {
            item {
                RoomSection(
                    roomName = "Other devices", 
                    devices = selectedDeviceVMsWithoutRooms, 
                    homeAppVM = homeAppVM
                )
            }
        }
    }
}

@Composable
fun RoomSection(
    roomVM: RoomViewModel? = null,
    roomName: String? = null,
    devices: List<DeviceViewModel>,
    homeAppVM: HomeAppViewModel
) {
    Column {
        Text(
            text = roomName ?: roomVM?.name ?: "",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        val gridHeight = ((devices.size / 2 + devices.size % 2) * 140).dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(gridHeight)
        ) {
            items(devices) { deviceVM ->
                DeviceCard(deviceVM = deviceVM, homeAppVM = homeAppVM)
            }
        }
    }
}

@Composable
fun DeviceCard(deviceVM: DeviceViewModel, homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val deviceStatus: String = deviceVM.status.collectAsState().value
    val deviceType = deviceVM.type.collectAsState().value
    val deviceTraits: List<Trait> = deviceVM.traits.collectAsState().value
    val connectivity = deviceVM.connectivity
    
    val isOnline = connectivity == ConnectivityState.ONLINE || 
                   connectivity == ConnectivityState.PARTIALLY_ONLINE
    
    val isOn = deviceStatus == "On" || deviceStatus.contains("Occupied") || 
               deviceStatus.contains("Closed") || deviceStatus.contains("Heat") || 
               deviceStatus.contains("Cool")

    // Find OnOff trait for switch control
    val onOffTrait = deviceTraits.find { it is OnOff } as? OnOff
    val hasOnOffControl = onOffTrait != null

    // Get device-specific colors
    val (iconColor, backgroundColor) = getDeviceColors(deviceType.factory, isOn, isOnline, deviceStatus)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Increased height to accommodate switch
            .clickable {
                scope.safelyLaunch(caller = "DeviceCard") {
                    homeAppVM.selectedDeviceVM.emit(deviceVM)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = getDeviceIcon(deviceType.factory),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconColor
                )
                
                if (!isOnline) {
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = "Offline",
                        modifier = Modifier.size(16.dp),
                        tint = DeviceColors.OfflineColor
                    )
                }
            }
            
            Column {
                Text(
                    text = deviceVM.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isOnline) MaterialTheme.colorScheme.onSurface 
                           else DeviceColors.OfflineColor
                )
                
                Text(
                    text = deviceStatus,
                    fontSize = 12.sp,
                    color = if (isOnline) MaterialTheme.colorScheme.onSurfaceVariant
                           else DeviceColors.OfflineColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Add switch control for devices that support OnOff
                if (hasOnOffControl && onOffTrait != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = onOffTrait.onOff == true,
                            onCheckedChange = { state ->
                                scope.launch {
                                    try {
                                        if (state) {
                                            onOffTrait.on()
                                        } else {
                                            onOffTrait.off()
                                        }
                                    } catch (e: Exception) {
                                        // Handle error silently or show a toast
                                    }
                                }
                            },
                            enabled = isOnline,
                            modifier = Modifier
                                .clickable(enabled = false) { } // Prevent card click when switch is clicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getDeviceColors(deviceFactory: Any, isOn: Boolean, isOnline: Boolean, deviceStatus: String = ""): Pair<Color, Color> {
    if (!isOnline) {
        return Pair(DeviceColors.OfflineColor, DeviceColors.OfflineBackground)
    }
    
    return when (deviceFactory) {
        OnOffLightDevice, DimmableLightDevice, 
        ColorTemperatureLightDevice, ExtendedColorLightDevice -> {
            if (isOn) {
                Pair(DeviceColors.LightOn, DeviceColors.LightBackground)
            } else {
                Pair(DeviceColors.LightOff, MaterialTheme.colorScheme.surface)
            }
        }
        
        GenericSwitchDevice, OnOffLightSwitchDevice -> {
            if (isOn) {
                Pair(DeviceColors.SwitchOn, DeviceColors.SwitchBackground)
            } else {
                Pair(DeviceColors.SwitchOff, MaterialTheme.colorScheme.surface)
            }
        }
        
        OnOffPluginUnitDevice -> {
            if (isOn) {
                Pair(DeviceColors.OutletOn, DeviceColors.OutletBackground)
            } else {
                Pair(DeviceColors.OutletOff, MaterialTheme.colorScheme.surface)
            }
        }
        
        OnOffSensorDevice, ContactSensorDevice, 
        OccupancySensorDevice -> {
            if (isOn) {
                Pair(DeviceColors.SensorActive, DeviceColors.SensorBackground)
            } else {
                Pair(DeviceColors.SensorInactive, MaterialTheme.colorScheme.surface)
            }
        }
        
        ThermostatDevice -> {
            when {
                isOn && (deviceStatus.contains("Heat") || deviceStatus.contains("heating", true)) -> {
                    Pair(DeviceColors.ThermostatHeating, DeviceColors.ThermostatBackground)
                }
                isOn && (deviceStatus.contains("Cool") || deviceStatus.contains("cooling", true)) -> {
                    Pair(DeviceColors.ThermostatCooling, DeviceColors.ThermostatBackground)
                }
                else -> {
                    Pair(DeviceColors.ThermostatOff, MaterialTheme.colorScheme.surface)
                }
            }
        }
        
        else -> {
            if (isOn) {
                Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
            } else {
                Pair(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.surface)
            }
        }
    }
}

fun getDeviceIcon(deviceFactory: Any): ImageVector {
    return when (deviceFactory) {
        OnOffLightDevice, DimmableLightDevice, 
        ColorTemperatureLightDevice, ExtendedColorLightDevice -> Icons.Default.Lightbulb
        
        GenericSwitchDevice, OnOffLightSwitchDevice -> Icons.Default.ToggleOff
        
        OnOffPluginUnitDevice -> Icons.Default.Outlet
        
        OnOffSensorDevice, ContactSensorDevice, 
        OccupancySensorDevice -> Icons.Default.Sensors
        
        ThermostatDevice -> Icons.Default.DeviceThermostat
        
        else -> Icons.Default.Home
    }
}
