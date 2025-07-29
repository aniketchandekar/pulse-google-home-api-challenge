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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Common UI patterns and reusable components
 */
object UIUtils {
    
    /**
     * Standard section header with consistent styling
     */
    @Composable
    fun SectionHeader(
        title: String,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    
    /**
     * Standard dropdown selector with consistent styling
     */
    @Composable
    fun <T> DropdownSelector(
        items: List<T>,
        selectedItem: T?,
        onItemSelected: (T) -> Unit,
        itemDisplay: (T) -> String,
        placeholder: String = "Select",
        showDropdownIndicator: Boolean = true,
        modifier: Modifier = Modifier
    ) {
        var expanded by remember { mutableStateOf(false) }
        
        val displayText = selectedItem?.let(itemDisplay) ?: placeholder
        val buttonText = if (showDropdownIndicator && items.size > 1) "$displayText ▾" else displayText
        
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { expanded = true }) {
                Text(text = buttonText, fontSize = 32.sp)
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(itemDisplay(item)) },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Standard content item with consistent styling
     */
    @Composable
    fun ContentItem(
        title: String,
        subtitle: String? = null,
        onClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .fillMaxWidth()
                .let { if (onClick != null) it.clickable { onClick() } else it }
        ) {
            Text(title, fontSize = 20.sp)
            subtitle?.let { 
                Text(it, fontSize = 16.sp) 
            }
        }
    }
}