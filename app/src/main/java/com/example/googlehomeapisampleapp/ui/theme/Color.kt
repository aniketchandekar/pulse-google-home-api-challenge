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

package com.example.googlehomeapisampleapp.ui.theme

import androidx.compose.ui.graphics.Color

// Dark theme background colors
val DarkBackground = Color(0xFF0E0E0E)
val DarkSurface = Color(0xFF1C1C1E)
val DarkSurfaceVariant = Color(0xFF2C2C2E)

// Primary theme colors for dark mode
val PrimaryBlue = Color(0xFF4285F4)
val PrimaryBlueVariant = Color(0xFF1A73E8)
val OnPrimary = Color(0xFFFFFFFF)

// Secondary colors
val SecondaryGreen = Color(0xFF34A853)
val SecondaryOrange = Color(0xFFFF6D01)
val SecondaryRed = Color(0xFFEA4335)

// Text colors for dark theme
val OnSurfaceDark = Color(0xFFE5E5E7)
val OnSurfaceVariantDark = Color(0xFF8E8E93)
val OnBackgroundDark = Color(0xFFE5E5E7)

// Device-specific colors (adaptive for light/dark theme)
object DeviceColors {
    // Light devices - warm yellow/orange tones
    val LightOn = Color(0xFFFF9800)  // Warm orange
    val LightOff = Color(0xFF9E9E9E)  // Gray
    val LightBackground = Color(0xFFFFF3E0)  // Light orange background
    
    // Switch devices - blue tones
    val SwitchOn = Color(0xFF2196F3)  // Blue
    val SwitchOff = Color(0xFF9E9E9E)
    val SwitchBackground = Color(0xFFE3F2FD)  // Light blue background
    
    // Outlet devices - green tones
    val OutletOn = Color(0xFF4CAF50)  // Green
    val OutletOff = Color(0xFF9E9E9E)
    val OutletBackground = Color(0xFFE8F5E8)  // Light green background
    
    // Sensor devices - purple tones
    val SensorActive = Color(0xFF9C27B0)  // Purple
    val SensorInactive = Color(0xFF9E9E9E)
    val SensorBackground = Color(0xFFF3E5F5)  // Light purple background
    
    // Thermostat devices - orange/red tones
    val ThermostatHeating = Color(0xFFFF5722)  // Orange-red
    val ThermostatCooling = Color(0xFF03A9F4)  // Light blue
    val ThermostatOff = Color(0xFF9E9E9E)
    val ThermostatBackground = Color(0xFFFFF3E0)  // Light warm background
    
    // Offline state
    val OfflineColor = Color(0xFF757575)
    val OfflineBackground = Color(0xFFF5F5F5)
}

// Original colors (keeping for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)