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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.googlehomeapisampleapp.BuildConfig
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel

/**
 * Shared account button component used across different views
 */
@Composable
fun AccountButton(homeAppVM: HomeAppViewModel) {
    var expanded by remember { mutableStateOf(false) }
    
    Row {
        IconButton(
            onClick = { homeAppVM.homeApp.permissionsManager.requestPermissions() },
            modifier = Modifier.size(48.dp).background(Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Account",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert, 
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { 
                    Text(
                        "Revoke Permissions",
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                onClick = {
                    expanded = false
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://myaccount.google.com/connections/link?project_number=${BuildConfig.GOOGLE_CLOUD_PROJECT_ID}")
                    )
                    homeAppVM.homeApp.context.startActivity(
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
        }
    }
}

/**
 * Shared header component with title and account button
 */
@Composable
fun HeaderWithAccount(
    title: String,
    homeAppVM: HomeAppViewModel
) {
    Row {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground
        )
        AccountButton(homeAppVM)
    }
}