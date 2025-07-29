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

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Enhanced dark theme color palette
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryBlueVariant,
    onPrimaryContainer = OnPrimary,
    
    secondary = SecondaryGreen,
    onSecondary = OnPrimary,
    secondaryContainer = SecondaryGreen.copy(alpha = 0.3f),
    onSecondaryContainer = OnSurfaceDark,
    
    tertiary = SecondaryOrange,
    onTertiary = OnPrimary,
    tertiaryContainer = SecondaryOrange.copy(alpha = 0.3f),
    onTertiaryContainer = OnSurfaceDark,
    
    background = DarkBackground,
    onBackground = OnBackgroundDark,
    
    surface = DarkSurface,
    onSurface = OnSurfaceDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    error = SecondaryRed,
    onError = OnPrimary,
    errorContainer = SecondaryRed.copy(alpha = 0.3f),
    onErrorContainer = OnSurfaceDark,
    
    outline = OnSurfaceVariantDark.copy(alpha = 0.5f),
    outlineVariant = OnSurfaceVariantDark.copy(alpha = 0.3f)
)

// Light theme color palette with pastel dark green
private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF4A6741),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE8F5E8),  // Light green container
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF2E4A2A),  // Darker green
    
    secondary = androidx.compose.ui.graphics.Color(0xFF388E3C),
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE8F5E8),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    
    tertiary = androidx.compose.ui.graphics.Color(0xFFFF7043),
    onTertiary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFE0B2),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFBF360C),
    
    background = androidx.compose.ui.graphics.Color(0xFFFFF9E3),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1C1C1C),
    
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1C1C),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF424242),
    
    error = androidx.compose.ui.graphics.Color(0xFFD32F2F),
    onError = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFFEBEE),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFB71C1C),
    
    outline = androidx.compose.ui.graphics.Color(0xFFBDBDBD),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFE0E0E0)
)

@Composable
fun GoogleHomeAPISampleAppTheme(
    darkTheme: Boolean = false, // Force light theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}