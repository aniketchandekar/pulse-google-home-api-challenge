package com.example.googlehomeapisampleapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "check_ins")
@TypeConverters(CheckInConverters::class)
data class CheckInEntity(
    @PrimaryKey
    val id: String,
    val emotions: List<String>,
    val thoughts: String?,
    val timestamp: String,
    val createdAt: Long = System.currentTimeMillis()
)

class CheckInConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}