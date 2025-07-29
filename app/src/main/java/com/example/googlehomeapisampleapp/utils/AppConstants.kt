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

package com.example.googlehomeapisampleapp.utils

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Application constants for consistent theming and configuration
 */
object AppConstants {
    
    // UI Dimensions
    object Dimensions {
        val topBarHeight = 64.dp
        val statusBarHeight = 48.dp
        val buttonPadding = 16.dp
        val itemPadding = 8.dp
        val contentPadding = 24.dp
        val sectionPadding = 16.dp
        val iconSize = 48.dp
    }
    
    // Typography
    object Typography {
        val titleFontSize = 32.sp
        val headingFontSize = 24.sp
        val bodyFontSize = 20.sp
        val subBodyFontSize = 16.sp
    }
    
    // API Configuration
    object API {
        const val PERMISSION_REFRESH_INTERVAL_MS = 2000L
        const val MAX_AUTOMATION_NAME_LENGTH = 100
        const val MAX_AUTOMATION_DESCRIPTION_LENGTH = 500
    }
    
    // Error Messages
    object ErrorMessages {
        const val STRUCTURE_LOAD_FAILED = "Failed to load structures"
        const val DEVICE_OPERATION_FAILED = "Device operation failed"
        const val AUTOMATION_CREATE_FAILED = "Failed to create automation"
        const val PERMISSION_REQUEST_FAILED = "Permission request failed"
        const val NO_STRUCTURE_SELECTED = "No structure selected"
        const val NO_DRAFT_AVAILABLE = "No draft automation available"
    }
    
    // Feature Flags
    object Features {
        const val ENABLE_DEBUG_LOGGING = false
        const val ENABLE_OFFLINE_MODE = false
        const val ENABLE_ADVANCED_DEVICE_CONTROLS = true
    }
}