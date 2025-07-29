package com.example.googlehomeapisampleapp.view.smartenvironment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.service.SmartHomeTherapyService
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartEnvironmentView(homeAppVM: HomeAppViewModel) {
    val scope = rememberCoroutineScope()
    val smartHomeService = SmartHomeTherapyService()
    
    // Get available devices from the selected structure
    val selectedStructure by homeAppVM.selectedStructureVM.collectAsState()
    val allDevices = selectedStructure?.deviceVMs?.collectAsState()?.value ?: emptyList()
    
    // Get available therapy environments based on current devices
    val availableEnvironments = remember(allDevices) {
        smartHomeService.getAvailableEnvironments(allDevices)
    }
    
    var selectedEnvironment by remember { mutableStateOf<SmartHomeTherapyService.TherapyEnvironment?>(null) }
    var showCustomization by remember { mutableStateOf(false) }
    var isExecuting by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            homeAppVM.clearSelections()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Text(
                        text = "Smart Environments",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${allDevices.size} devices available",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Quick mood adjustment button
            IconButton(
                onClick = {
                    scope.launch {
                        val result = smartHomeService.executeQuickMoodAdjustment("calm", allDevices)
                        // Show feedback based on result
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Quick Calm",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (availableEnvironments.isEmpty()) {
            // No compatible devices found
            NoDevicesView()
        } else {
            // Environment selection
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Choose Your Therapy Environment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(availableEnvironments) { environment ->
                    TherapyEnvironmentCard(
                        environment = environment,
                        devices = allDevices,
                        isSelected = selectedEnvironment == environment,
                        isExecuting = isExecuting && selectedEnvironment == environment,
                        onSelect = { selectedEnvironment = environment },
                        onExecute = {
                            scope.launch {
                                isExecuting = true
                                // Execute the environment
                                // This would be called through a suggestion execution
                                isExecuting = false
                            }
                        },
                        onCustomize = {
                            selectedEnvironment = environment
                            showCustomization = true
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Customization bottom sheet
    if (showCustomization && selectedEnvironment != null) {
        EnvironmentCustomizationSheet(
            environment = selectedEnvironment!!,
            devices = allDevices,
            onDismiss = { showCustomization = false },
            onApply = { customizedEnvironment ->
                scope.launch {
                    // Execute customized environment
                    showCustomization = false
                }
            }
        )
    }
}

@Composable
fun TherapyEnvironmentCard(
    environment: SmartHomeTherapyService.TherapyEnvironment,
    devices: List<com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel>,
    isSelected: Boolean,
    isExecuting: Boolean,
    onSelect: () -> Unit,
    onExecute: () -> Unit,
    onCustomize: () -> Unit
) {
    val cardColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with icon and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                getEnvironmentColor(environment.name),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getEnvironmentIcon(environment.name),
                            contentDescription = environment.name,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = environment.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (environment.duration > 0) {
                                "${environment.duration / 60000} min session"
                            } else {
                                "Continuous"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Priority/mood indicator
                Badge(
                    containerColor = getEnvironmentColor(environment.name).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = getEnvironmentMoodTag(environment.name),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = getEnvironmentColor(environment.name)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description
            Text(
                text = environment.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Environment details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lighting info
                EnvironmentDetail(
                    icon = Icons.Default.Lightbulb,
                    label = "Lighting",
                    value = "${environment.lightingConfig.brightness}% brightness"
                )
                
                // Temperature info
                environment.temperatureConfig?.let { tempConfig ->
                    EnvironmentDetail(
                        icon = Icons.Default.DeviceThermostat,
                        label = "Temperature",
                        value = "${tempConfig.targetTemp}°C"
                    )
                }
                
                // Audio info
                environment.audioConfig?.let { audioConfig ->
                    EnvironmentDetail(
                        icon = Icons.Default.VolumeUp,
                        label = "Audio",
                        value = "${audioConfig.volume}% volume"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Execute button
                Button(
                    onClick = onExecute,
                    modifier = Modifier.weight(1f),
                    enabled = !isExecuting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getEnvironmentColor(environment.name)
                    )
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Execute",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isExecuting) "Starting..." else "Start Environment",
                        fontSize = 14.sp
                    )
                }
                
                // Customize button
                OutlinedButton(
                    onClick = onCustomize,
                    modifier = Modifier.weight(0.7f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Customize",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Customize",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EnvironmentDetail(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NoDevicesView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DevicesOther,
            contentDescription = "No Devices",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Compatible Devices Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Smart environments require lights, thermostat, or speaker devices. Please ensure your smart home devices are connected and try again.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { /* Navigate to device setup */ }
        ) {
            Text("Set Up Devices")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentCustomizationSheet(
    environment: SmartHomeTherapyService.TherapyEnvironment,
    devices: List<com.example.googlehomeapisampleapp.viewmodel.devices.DeviceViewModel>,
    onDismiss: () -> Unit,
    onApply: (SmartHomeTherapyService.TherapyEnvironment) -> Unit
) {
    var brightness by remember { mutableStateOf(environment.lightingConfig.brightness.toFloat()) }
    var warmth by remember { mutableStateOf(environment.lightingConfig.warmth.toFloat()) }
    var temperature by remember { mutableStateOf(environment.temperatureConfig?.targetTemp?.toFloat() ?: 22f) }
    var volume by remember { mutableStateOf(environment.audioConfig?.volume?.toFloat() ?: 30f) }
    var duration by remember { mutableStateOf((environment.duration / 60000).toFloat()) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Customize ${environment.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Brightness control
            Text(
                text = "Brightness: ${brightness.toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = brightness,
                onValueChange = { brightness = it },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Warmth control
            Text(
                text = "Warmth: ${warmth.toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = warmth,
                onValueChange = { warmth = it },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Temperature control (if available)
            if (environment.temperatureConfig != null) {
                Text(
                    text = "Temperature: ${temperature.toInt()}°C",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 16f..28f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Volume control (if available)
            if (environment.audioConfig != null) {
                Text(
                    text = "Volume: ${volume.toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Duration control
            Text(
                text = "Duration: ${if (duration.toInt() == 0) "Continuous" else "${duration.toInt()} minutes"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = duration,
                onValueChange = { duration = it },
                valueRange = 0f..60f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Apply button
            Button(
                onClick = {
                    val customizedEnvironment = environment.copy(
                        lightingConfig = environment.lightingConfig.copy(
                            brightness = brightness.toInt(),
                            warmth = warmth.toInt()
                        ),
                        temperatureConfig = environment.temperatureConfig?.copy(
                            targetTemp = temperature.toInt()
                        ),
                        audioConfig = environment.audioConfig?.copy(
                            volume = volume.toInt()
                        ),
                        duration = duration.toLong() * 60000
                    )
                    onApply(customizedEnvironment)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Customization")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Helper functions
private fun getEnvironmentIcon(name: String): ImageVector {
    return when (name) {
        "Anxiety Relief" -> Icons.Default.SelfImprovement
        "Mood Boost" -> Icons.Default.WbSunny
        "Deep Relaxation" -> Icons.Default.Spa
        "Focus & Clarity" -> Icons.Default.Visibility
        "Social Preparation" -> Icons.Default.Groups
        "Bedtime Routine" -> Icons.Default.Bedtime
        else -> Icons.Default.Home
    }
}

private fun getEnvironmentColor(name: String): Color {
    return when (name) {
        "Anxiety Relief" -> Color(0xFF4FC3F7) // Light Blue
        "Mood Boost" -> Color(0xFFFFB74D) // Orange
        "Deep Relaxation" -> Color(0xFF81C784) // Green
        "Focus & Clarity" -> Color(0xFF9575CD) // Purple
        "Social Preparation" -> Color(0xFFFF8A65) // Deep Orange
        "Bedtime Routine" -> Color(0xFF7986CB) // Indigo
        else -> Color(0xFF90A4AE) // Blue Grey
    }
}

private fun getEnvironmentMoodTag(name: String): String {
    return when (name) {
        "Anxiety Relief" -> "CALM"
        "Mood Boost" -> "ENERGY"
        "Deep Relaxation" -> "PEACE"
        "Focus & Clarity" -> "FOCUS"
        "Social Preparation" -> "CONFIDENCE"
        "Bedtime Routine" -> "SLEEP"
        else -> "WELLNESS"
    }
}