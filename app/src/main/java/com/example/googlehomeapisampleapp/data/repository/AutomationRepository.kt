package com.example.googlehomeapisampleapp.data.repository

import com.example.googlehomeapisampleapp.data.dao.AutomationDao
import com.example.googlehomeapisampleapp.data.entity.AutomationSuggestionEntity
import com.example.googlehomeapisampleapp.data.entity.ContactEntity
import com.example.googlehomeapisampleapp.data.entity.AutomationExecutionEntity
import kotlinx.coroutines.flow.Flow

class AutomationRepository(
    private val automationDao: AutomationDao
) {
    
    // Automation Suggestions
    fun getSuggestionsForCheckIn(checkInId: String): Flow<List<AutomationSuggestionEntity>> =
        automationDao.getSuggestionsForCheckIn(checkInId)
    
    fun getActiveSuggestions(): Flow<List<AutomationSuggestionEntity>> =
        automationDao.getActiveSuggestions()
    
    suspend fun insertSuggestion(suggestion: AutomationSuggestionEntity) =
        automationDao.insertSuggestion(suggestion)
    
    suspend fun updateSuggestion(suggestion: AutomationSuggestionEntity) =
        automationDao.updateSuggestion(suggestion)
    
    suspend fun dismissSuggestion(suggestionId: String) =
        automationDao.dismissSuggestion(suggestionId)
    
    // Contacts
    fun getFrequentContacts(): Flow<List<ContactEntity>> =
        automationDao.getFrequentContacts()
    
    fun getAllContacts(): Flow<List<ContactEntity>> =
        automationDao.getAllContacts()
    
    suspend fun insertContact(contact: ContactEntity) =
        automationDao.insertContact(contact)
    
    suspend fun updateContact(contact: ContactEntity) =
        automationDao.updateContact(contact)
    
    suspend fun deleteContact(contactId: String) =
        automationDao.deleteContact(contactId)
    
    suspend fun updateLastContacted(contactId: String, timestamp: Long = System.currentTimeMillis()) =
        automationDao.updateLastContacted(contactId, timestamp)
    
    // Execution Tracking
    suspend fun insertExecution(execution: AutomationExecutionEntity) =
        automationDao.insertExecution(execution)
    
    suspend fun getExecutionsForSuggestion(suggestionId: String): List<AutomationExecutionEntity> =
        automationDao.getExecutionsForSuggestion(suggestionId)
    
    suspend fun getRecentExecutions(limit: Int = 10): List<AutomationExecutionEntity> =
        automationDao.getRecentExecutions(limit)
}