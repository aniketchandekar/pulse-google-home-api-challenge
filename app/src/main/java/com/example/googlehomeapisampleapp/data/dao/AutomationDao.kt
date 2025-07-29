package com.example.googlehomeapisampleapp.data.dao

import androidx.room.*
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.ContactEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_suggestions WHERE checkInId = :checkInId AND isDismissed = 0 ORDER BY priority DESC, createdAt DESC")
    fun getSuggestionsForCheckIn(checkInId: String): Flow<List<AutomationSuggestionEntity>>

    @Query("SELECT * FROM automation_suggestions WHERE isDismissed = 0 AND isExecuted = 0 ORDER BY priority DESC, createdAt DESC LIMIT 5")
    fun getActiveSuggestions(): Flow<List<AutomationSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: AutomationSuggestionEntity)

    @Update
    suspend fun updateSuggestion(suggestion: AutomationSuggestionEntity)

    @Query("UPDATE automation_suggestions SET isDismissed = 1 WHERE id = :suggestionId")
    suspend fun dismissSuggestion(suggestionId: String)

    @Query("SELECT * FROM contacts WHERE isFrequent = 1 ORDER BY lastContactedAt DESC")
    fun getFrequentContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)

    @Query("UPDATE contacts SET lastContactedAt = :timestamp WHERE id = :contactId")
    suspend fun updateLastContacted(contactId: String, timestamp: Long)

    @Insert
    suspend fun insertExecution(execution: AutomationExecutionEntity)

    @Query("SELECT * FROM automation_executions WHERE suggestionId = :suggestionId")
    suspend fun getExecutionsForSuggestion(suggestionId: String): List<AutomationExecutionEntity>

    @Query("SELECT * FROM automation_executions ORDER BY executedAt DESC LIMIT :limit")
    suspend fun getRecentExecutions(limit: Int): List<AutomationExecutionEntity>
}