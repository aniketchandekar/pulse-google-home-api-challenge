package com.example.googlehomeapisampleapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String, // "family", "friend", "therapist", "emergency"
    val isFrequent: Boolean = false,
    val lastContactedAt: Long? = null,
    val addedAt: Long = System.currentTimeMillis()
)