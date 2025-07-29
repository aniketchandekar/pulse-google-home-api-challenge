package com.example.googlehomeapisampleapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "automation_suggestions")
@TypeConverters(AutomationConverters::class)
data class AutomationSuggestionEntity(
    @PrimaryKey val id: String,
    val checkInId: String,
    val title: String,
    val description: String,
    val type: String, // SMART_HOME, SOCIAL_SUPPORT, WELLNESS, THERAPEUTIC, EMERGENCY
    val priority: String, // LOW, MEDIUM, HIGH, URGENT
    val actions: List<AutomationActionData>,
    val geminiReasoning: String,
    val estimatedDuration: String,
    val createdAt: Long,
    val isExecuted: Boolean = false,
    val isDismissed: Boolean = false,
    val executedAt: Long? = null
)

data class AutomationActionData(
    val type: String, // CALL_CONTACT, SMART_HOME, THERAPEUTIC_ACTIVITY, REMINDER, GEMINI_CHAT
    val targetId: String? = null, // contact ID, device ID, etc.
    val parameters: Map<String, String> = emptyMap(),
    val displayText: String,
    val isCompleted: Boolean = false
)

class AutomationConverters {
    @TypeConverter
    fun fromActionsList(actions: List<AutomationActionData>): String {
        return Gson().toJson(actions)
    }

    @TypeConverter
    fun toActionsList(actionsString: String): List<AutomationActionData> {
        val listType = object : TypeToken<List<AutomationActionData>>() {}.type
        return Gson().fromJson(actionsString, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toStringMap(mapString: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(mapString, mapType) ?: emptyMap()
    }
}