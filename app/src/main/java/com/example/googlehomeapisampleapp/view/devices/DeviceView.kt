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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.MainActivity
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel
import com.google.home.ConnectivityState
import com.google.home.DeviceType
import com.google.home.Trait
import com.google.home.matter.standard.BooleanState
import com.google.home.matter.standard.LevelControl
import com.google.home.matter.standard.LevelControlTrait
import com.google.home.matter.standard.OccupancySensing
import com.google.home.matter.standard.OnOff
import com.google.home.matter.standard.Thermostat
import com.google.home.matter.standard.ThermostatTrait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Outlet
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.googlehomeapisampleapp.ui.theme.DeviceColors
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.google.home.matter.standard.ThermostatDevice
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun DeviceView (homeAppVM: HomeAppViewModel) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val deviceVM: DeviceViewModel? by homeAppVM.selectedDeviceVM.collectAsState()

    BackHandler {
        scope.launch { homeAppVM.selectedDeviceVM.emit(null) }
    }

    Column(modifier = Modifier.statusBarsPadding()) {
        // Header with back button and device info
        DeviceHeader(
            deviceVM = deviceVM,
            onBackClick = { 
                scope.launch { homeAppVM.selectedDeviceVM.emit(null) }
            }
        )

        // Device controls
        Box (modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ControlListComponent(homeAppVM)
            }
        }
    }
}

@Composable
fun DeviceHeader(
    deviceVM: DeviceViewModel?,
    onBackClick: () -> Unit
) {
    if (deviceVM == null) return
    
    val deviceType = deviceVM.type.collectAsState().value
    val deviceStatus = deviceVM.status.collectAsState().value
    val connectivity = deviceVM.connectivity
    
    val isOnline = connectivity == ConnectivityState.ONLINE || 
                   connectivity == ConnectivityState.PARTIALLY_ONLINE
    
    val isOn = deviceStatus == "On" || deviceStatus.contains("Occupied") || 
               deviceStatus.contains("Closed") || deviceStatus.contains("Heat") || 
               deviceStatus.contains("Cool")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Device icon based on device type with status-based colors
        val (deviceIcon, iconColor) = getDeviceIconAndColor(deviceType.factory, isOn, isOnline, deviceStatus)

        Icon(
            imageVector = deviceIcon,
            contentDescription = "Device Icon",
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = deviceVM.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = deviceStatus,
                color = if (isOnline) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
        
        if (!isOnline) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.OfflinePin,
                contentDescription = "Offline",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ControlListComponent (homeAppVM: HomeAppViewModel) {

    val deviceVM: DeviceViewModel = homeAppVM.selectedDeviceVM.collectAsState().value ?: return
    val deviceType: DeviceType = deviceVM.type.collectAsState().value
    val deviceTypeName: String = deviceVM.typeName.collectAsState().value
    val deviceTraits: List<Trait> = deviceVM.traits.collectAsState().value

    Column (Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
        Text(deviceTypeName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }

    for (trait in deviceTraits) {
        ControlListItem(trait, deviceType)
    }
}

@Composable
fun ControlListItem (trait: Trait, type: DeviceType) {
    val scope: CoroutineScope = rememberCoroutineScope()

    val isConnected : Boolean =
        type.metadata.sourceConnectivity.connectivityState == ConnectivityState.ONLINE ||
                type.metadata.sourceConnectivity.connectivityState == ConnectivityState.PARTIALLY_ONLINE

    Box (Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        when (trait) {
            is OnOff -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (trait.onOff == true) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            contentDescription = "OnOff",
                            tint = if (trait.onOff == true) DeviceColors.SwitchOn else DeviceColors.SwitchOff,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(trait.factory.toString(), fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(DeviceViewModel.getTraitStatus(trait, type), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                    Switch (checked = (trait.onOff == true),
                        onCheckedChange = { state ->
                            scope.launch { if (state) trait.on() else trait.off() }
                        },
                        enabled = isConnected
                    )
                }
            }
            is LevelControl -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Level Control",
                            tint = if (trait.currentLevel != null && trait.currentLevel!! > 0u) DeviceColors.LightOn else DeviceColors.LightOff,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(trait.factory.toString(), fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    LevelSlider(value = trait.currentLevel?.toFloat()!!, low = 0f, high = 254f, steps = 0,
                        modifier = Modifier.padding(top = 16.dp),
                        onValueChange = { value : Float ->
                            scope.launch {
                                trait.moveToLevelWithOnOff(
                                    level = value.toInt().toUByte(),
                                    transitionTime = null,
                                    optionsMask = LevelControlTrait.OptionsBitmap(),
                                    optionsOverride = LevelControlTrait.OptionsBitmap()
                                ) }
                        },
                        isEnabled = isConnected
                    )
                }
            }
            is BooleanState -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Boolean State",
                        tint = if (trait.stateValue == true) DeviceColors.SensorActive else DeviceColors.SensorInactive,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(trait.factory.toString(), fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(DeviceViewModel.getTraitStatus(trait, type), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
            is OccupancySensing -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Occupancy Sensing",
                        tint = if (trait.occupancy?.occupied == true) DeviceColors.SensorActive else DeviceColors.SensorInactive,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(trait.factory.toString(), fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(DeviceViewModel.getTraitStatus(trait, type), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
            is Thermostat -> {
                val supportedModes = arrayOf(
                    ThermostatTrait.SystemModeEnum.Heat,
                    ThermostatTrait.SystemModeEnum.Cool,
                    ThermostatTrait.SystemModeEnum.Off
                )
                val workingModes = arrayOf(
                    ThermostatTrait.SystemModeEnum.Heat,
                    ThermostatTrait.SystemModeEnum.Cool
                )
                var expanded: Boolean by remember { mutableStateOf(false) }
                var vlow = 0f
                var vhigh = 0f
                var vset = 0f
                
                val thermostatColor = when (trait.systemMode) {
                    ThermostatTrait.SystemModeEnum.Heat -> DeviceColors.ThermostatHeating
                    ThermostatTrait.SystemModeEnum.Cool -> DeviceColors.ThermostatCooling
                    else -> DeviceColors.ThermostatOff
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeviceThermostat,
                            contentDescription = "Thermostat",
                            tint = thermostatColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Thermostat", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${trait.localTemperature?.div(100)?.toFloat()}° | ${trait.systemMode}", 
                                 fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                    
                    // Mode selector button
                    TextButton(onClick = { expanded = true }) {
                        Text(text = "${trait.systemMode} ▾", fontSize = 16.sp, color = thermostatColor)
                    }
                    
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        for (mode in supportedModes) {
                            DropdownMenuItem(
                                text = { Text(mode.toString(), color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    scope.launch { trait.update { setSystemMode(mode) } }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Temperature setpoint slider
                when (trait.systemMode) {
                    ThermostatTrait.SystemModeEnum.Heat -> {
                        vset = trait.occupiedHeatingSetpoint?.toFloat()!!
                        vlow = trait.absMinHeatSetpointLimit?.toFloat()!!
                        vhigh = trait.absMaxHeatSetpointLimit?.toFloat()!!
                    }
                    ThermostatTrait.SystemModeEnum.Cool -> {
                        vset = trait.occupiedCoolingSetpoint?.toFloat()!!
                        vlow = trait.absMinCoolSetpointLimit?.toFloat()!!
                        vhigh = trait.absMaxCoolSetpointLimit?.toFloat()!!
                    }
                    else -> {
                        vset = trait.occupiedHeatingSetpoint?.toFloat()!!
                        vlow = trait.absMinHeatSetpointLimit?.toFloat()!!
                        vhigh = trait.absMaxHeatSetpointLimit?.toFloat()!!
                    }
                }
                
                if (trait.systemMode in workingModes) {
                    LevelSlider(value = vset, low = vlow, high = vhigh,
                        steps = (vhigh - vlow).div(100f).toInt().minus(1),
                        modifier = Modifier.padding(top = 16.dp),
                        onValueChange = { value : Float ->
                            scope.launch {
                                try {
                                    trait.setpointRaiseLower(
                                        ThermostatTrait.SetpointRaiseLowerModeEnum.Both,
                                        (value - vset).div(10).toInt().toByte()
                                    )
                                } catch (e:Exception) {
                                    MainActivity.showWarning(this, "Exception: " + e.message)
                                }
                            }
                        },
                        isEnabled = isConnected
                    )
                }
            }
            else -> return
        }
    }
}

@Composable
fun LevelSlider(value: Float, low: Float, high: Float, steps: Int, onValueChange: (Float) -> Unit, modifier: Modifier, isEnabled: Boolean) {
    var level: Float by remember { mutableStateOf(value) }
    var oldValue: Float by remember { mutableStateOf(value) }

    Slider(
        value = level,
        valueRange = low..high,
        steps = steps,
        modifier = modifier,
        onValueChange = { level = it },
        onValueChangeFinished = { onValueChange(level) },
        enabled = isEnabled
    )

    // Register external value change:
    if(value != oldValue) {
        oldValue = value
        level = value
    }
}

@Composable
fun getDeviceIconAndColor(deviceFactory: Any, isOn: Boolean, isOnline: Boolean, deviceStatus: String): Pair<ImageVector, Color> {
    if (!isOnline) {
        return Pair(Icons.Default.OfflinePin, Color.Red)
    }
    
    return when (deviceFactory) {
        OnOffLightDevice, DimmableLightDevice, 
        ColorTemperatureLightDevice, ExtendedColorLightDevice -> {
            val icon = Icons.Default.Lightbulb
            val color = if (isOn) DeviceColors.LightOn else DeviceColors.LightOff
            Pair(icon, color)
        }
        
        GenericSwitchDevice, OnOffLightSwitchDevice -> {
            val icon = if (isOn) Icons.Default.ToggleOn else Icons.Default.ToggleOff
            val color = if (isOn) DeviceColors.SwitchOn else DeviceColors.SwitchOff
            Pair(icon, color)
        }
        
        OnOffPluginUnitDevice -> {
            val icon = Icons.Default.Outlet
            val color = if (isOn) DeviceColors.OutletOn else DeviceColors.OutletOff
            Pair(icon, color)
        }
        
        OnOffSensorDevice, ContactSensorDevice, 
        OccupancySensorDevice -> {
            val icon = Icons.Default.Sensors
            val color = if (isOn) DeviceColors.SensorActive else DeviceColors.SensorInactive
            Pair(icon, color)
        }
        
        ThermostatDevice -> {
            val icon = Icons.Default.DeviceThermostat
            val color = when {
                isOn && (deviceStatus.contains("Heat") || deviceStatus.contains("heating", true)) -> 
                    DeviceColors.ThermostatHeating
                isOn && (deviceStatus.contains("Cool") || deviceStatus.contains("cooling", true)) -> 
                    DeviceColors.ThermostatCooling
                else -> DeviceColors.ThermostatOff
            }
            Pair(icon, color)
        }
        
        else -> {
            val icon = Icons.Default.Home
            val color = if (isOn) MaterialTheme.colorScheme.onSurface else Color.Gray
            Pair(icon, color)
        }
    }
}