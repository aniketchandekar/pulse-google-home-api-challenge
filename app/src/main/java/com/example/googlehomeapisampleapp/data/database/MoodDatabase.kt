package com.example.googlehomeapisampleapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.googlehomeapisampleapp.data.dao.CheckInDao
import com.example.googlehomeapisampleapp.data.dao.AutomationDao
import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.ContactEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationExecutionEntity

@Database(
    entities = [
        CheckInEntity::class,
        AutomationSuggestionEntity::class,
        ContactEntity::class,
        AutomationExecutionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun automationDao(): AutomationDao

    companion object {
        @Volatile
        private var INSTANCE: MoodDatabase? = null

        fun getDatabase(context: Context): MoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodDatabase::class.java,
                    "mood_database"
                )
                .fallbackToDestructiveMigration() // For development - will recreate database
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}