package com.example.googlehomeapisampleapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_executions")
data class AutomationExecutionEntity(
    @PrimaryKey val id: String,
    val suggestionId: String,
    val checkInId: String,
    val executedAt: Long,
    val wasHelpful: Boolean? = null,
    val userFeedback: String? = null,
    val completionStatus: String // COMPLETED, PARTIALLY_COMPLETED, CANCELLED
)