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

package com.example.googlehomeapisampleapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googlehomeapisampleapp.R
import com.example.googlehomeapisampleapp.viewmodel.HomeAppViewModel

@Composable
fun WelcomeView (homeAppVM: HomeAppViewModel) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp), 
        verticalArrangement = Arrangement.Center
    ) {

        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Pulse", 
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(32.dp))

        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Image (
                painter = painterResource(R.drawable.icon_app),
                contentDescription = stringResource(R.string.app_name)
            )
        }

        Spacer(Modifier.height(32.dp))

        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "AI-Powered Mood-Based Smart Home Automation", 
                fontSize = 20.sp, 
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "• Check in with your emotions\n• Get personalized automation suggestions\n• Create mood-based smart home routines", 
                fontSize = 16.sp, 
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            // Sign-in button to trigger Permissions API and start the sign-in flow:
            Button(onClick = { homeAppVM.homeApp.permissionsManager.requestPermissions() }) {
                Text("Sign in with Google")
            }
        }
    }
}