package com.example.googlehomeapisampleapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Insert
    suspend fun insertCheckIn(checkIn: CheckInEntity)

    @Query("SELECT * FROM check_ins ORDER BY id DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentCheckIns(limit: Int): List<CheckInEntity>

    @Query("SELECT COUNT(*) FROM check_ins")
    suspend fun getCheckInCount(): Int

    @Query("DELETE FROM check_ins WHERE id = :checkInId")
    suspend fun deleteCheckIn(checkInId: String)

    @Query("""
        UPDATE check_ins 
        SET emotions = :emotions, thoughts = :thoughts, timestamp = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateCheckIn(
        id: String,
        emotions: List<String>,
        thoughts: String?,
        timestamp: String
    )

    @Query("SELECT * FROM check_ins WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    suspend fun getCheckInsBetween(startTime: Long, endTime: Long): List<CheckInEntity>
}