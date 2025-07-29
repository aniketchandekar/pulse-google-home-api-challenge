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

import com.example.googlehomeapisampleapp.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Centralized error handling utility for consistent error management
 */
object ErrorHandler {
    
    /**
     * Execute a suspending operation with proper error handling
     */
    suspend fun <T> safeExecute(
        operation: suspend () -> T,
        onError: ((Exception) -> Unit)? = null,
        caller: Any
    ): T? {
        return try {
            operation()
        } catch (e: Exception) {
            val errorMessage = "Operation failed: ${e.message}"
            MainActivity.showError(caller, errorMessage)
            onError?.invoke(e)
            null
        }
    }
    
    /**
     * Execute a regular operation with proper error handling
     */
    fun <T> execute(
        operation: () -> T,
        onError: ((Exception) -> Unit)? = null,
        caller: Any
    ): T? {
        return try {
            operation()
        } catch (e: Exception) {
            val errorMessage = "Operation failed: ${e.message}"
            MainActivity.showError(caller, errorMessage)
            onError?.invoke(e)
            null
        }
    }
    
    /**
     * Execute with coroutine scope and error handling
     */
    fun CoroutineScope.safelyLaunch(
        caller: Any,
        onError: ((Exception) -> Unit)? = null,
        operation: suspend () -> Unit
    ) {
        launch {
            safeExecute(operation, onError, caller)
        }
    }
}