package com.example.googlehomeapisampleapp.data.repository

import com.example.googlehomeapisampleapp.data.dao.CheckInDao
import com.example.googlehomeapisampleapp.data.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class MoodRepository(
    private val checkInDao: CheckInDao
) {
    /**
     * Get all check-ins as a Flow for real-time updates
     */
    fun getAllCheckIns(): Flow<List<CheckInEntity>> = checkInDao.getAllCheckIns()

    /**
     * Delete a check-in by ID
     */
    suspend fun deleteCheckIn(checkInId: String) {
        checkInDao.deleteCheckIn(checkInId)
    }

    /**
     * Update an existing check-in
     */
    suspend fun updateCheckIn(checkInId: String, emotions: Set<String>, thoughts: String) {
        val updatedTimestamp = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            .format(Date())

        checkInDao.updateCheckIn(
            id = checkInId,
            emotions = emotions.toList(),
            thoughts = thoughts.ifBlank { null },
            timestamp = updatedTimestamp
        )
    }

    suspend fun insertCheckIn(checkIn: CheckInEntity) {
        checkInDao.insertCheckIn(checkIn)
    }

    suspend fun getMoodAnalytics(): MoodAnalyticsData {
        val allCheckIns = checkInDao.getRecentCheckIns(100) // Get recent 100 check-ins

        if (allCheckIns.isEmpty()) {
            return getEmptyAnalytics()
        }

        // Calculate sentiment data
        val sentimentData = calculateSentimentData(allCheckIns)

        // Calculate emotion distribution
        val emotionCounts = mutableMapOf<String, Int>()
        allCheckIns.forEach { checkIn ->
            checkIn.emotions.forEach { emotion ->
                emotionCounts[emotion] = emotionCounts.getOrDefault(emotion, 0) + 1
            }
        }

        // Find most popular emotion
        val topEmotion = findTopEmotion(emotionCounts)

        return MoodAnalyticsData(
            sentimentData = sentimentData,
            emotionData = emotionCounts,
            topEmotion = topEmotion
        )
    }

    private fun calculateSentimentData(checkIns: List<CheckInEntity>): Map<String, Int> {
        val positiveEmotions = setOf("Great", "Happy", "Calm", "Confident", "Loved")
        val negativeEmotions = setOf("Upset", "Sad", "Angry", "Anxious")
        // Everything else is neutral: Tired, Thoughtful, Sleepy

        var positiveCount = 0
        var neutralCount = 0
        var negativeCount = 0

        checkIns.forEach { checkIn ->
            checkIn.emotions.forEach { emotion ->
                when {
                    positiveEmotions.contains(emotion) -> positiveCount++
                    negativeEmotions.contains(emotion) -> negativeCount++
                    else -> neutralCount++
                }
            }
        }

        return mapOf(
            "Positive" to positiveCount,
            "Neutral" to neutralCount,
            "Negative" to negativeCount
        )
    }

    private fun findTopEmotion(emotionCounts: Map<String, Int>): Pair<String, String> {
        val emotionToEmoji = mapOf(
            "Great" to "😊",
            "Happy" to "😄",
            "Calm" to "😌",
            "Tired" to "😴",
            "Upset" to "😕",
            "Sad" to "😢",
            "Angry" to "😠",
            "Anxious" to "😰",
            "Thoughtful" to "🤔",
            "Confident" to "😎",
            "Loved" to "🥰",
            "Sleepy" to "😪"
        )

        if (emotionCounts.isEmpty()) {
            return "😊" to "Happy"
        }

        val topEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "Happy"
        val emoji = emotionToEmoji[topEmotion] ?: "😊"

        return emoji to topEmotion
    }

    private fun getEmptyAnalytics(): MoodAnalyticsData {
        return MoodAnalyticsData(
            sentimentData = mapOf("Positive" to 0, "Neutral" to 0, "Negative" to 0),
            emotionData = emptyMap(),
            topEmotion = "😊" to "Happy"
        )
    }

    suspend fun createCheckIn(emotions: Set<String>, thoughts: String): CheckInEntity {
        val timestamp = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            .format(Date())

        val checkIn = CheckInEntity(
            id = UUID.randomUUID().toString(),
            emotions = emotions.toList(),
            thoughts = thoughts.takeIf { it.isNotBlank() },
            timestamp = timestamp
        )

        insertCheckIn(checkIn)
        return checkIn
    }
}

data class MoodAnalyticsData(
    val sentimentData: Map<String, Int>,
    val emotionData: Map<String, Int>,
    val topEmotion: Pair<String, String> // emoji to emotion name
)