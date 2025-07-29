package com.example.googlehomeapisampleapp.service

data class EmotionAnalysis(
    val overallSentiment: Sentiment,
    val intensity: EmotionIntensity,
    val supportNeeded: SupportLevel,
    val dominantEmotions: List<String>,
    val riskLevel: RiskLevel
)

enum class Sentiment { POSITIVE, NEUTRAL, NEGATIVE }
enum class EmotionIntensity { LOW, MEDIUM, HIGH, EXTREME }
enum class SupportLevel { NONE, LOW, MEDIUM, HIGH, URGENT }
enum class RiskLevel { SAFE, MONITOR, CONCERN, URGENT }

class EmotionAnalysisService {
    
    fun analyze(emotions: List<String>, thoughts: String?): EmotionAnalysis {
        val sentiment = calculateSentiment(emotions)
        val intensity = calculateIntensity(emotions, thoughts)
        val supportNeeded = calculateSupportLevel(emotions, thoughts)
        val riskLevel = assessRiskLevel(emotions, thoughts)
        
        return EmotionAnalysis(
            overallSentiment = sentiment,
            intensity = intensity,
            supportNeeded = supportNeeded,
            dominantEmotions = emotions.take(3),
            riskLevel = riskLevel
        )
    }
    
    private fun calculateSentiment(emotions: List<String>): Sentiment {
        val positiveEmotions = setOf("happy", "great", "confident", "loved", "calm")
        val negativeEmotions = setOf("sad", "angry", "anxious", "upset")
        
        val positiveCount = emotions.count { it.lowercase() in positiveEmotions }
        val negativeCount = emotions.count { it.lowercase() in negativeEmotions }
        
        return when {
            positiveCount > negativeCount -> Sentiment.POSITIVE
            negativeCount > positiveCount -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
    }
    
    private fun calculateSupportLevel(emotions: List<String>, thoughts: String?): SupportLevel {
        val highSupportEmotions = setOf("sad", "angry", "anxious", "upset")
        val concerningWords = setOf("hopeless", "alone", "worthless", "trapped", "overwhelmed")
        
        val emotionScore = emotions.count { it.lowercase() in highSupportEmotions }
        val thoughtScore = thoughts?.lowercase()?.let { text ->
            concerningWords.count { word -> text.contains(word) }
        } ?: 0
        
        return when {
            thoughtScore >= 2 || emotionScore >= 3 -> SupportLevel.HIGH
            thoughtScore >= 1 || emotionScore >= 2 -> SupportLevel.MEDIUM
            emotionScore >= 1 -> SupportLevel.LOW
            else -> SupportLevel.NONE
        }
    }
    
    private fun assessRiskLevel(emotions: List<String>, thoughts: String?): RiskLevel {
        val crisisKeywords = setOf("harm", "hurt", "end", "suicide", "die", "kill")
        val concernKeywords = setOf("hopeless", "trapped", "worthless", "burden")
        
        thoughts?.lowercase()?.let { text ->
            if (crisisKeywords.any { text.contains(it) }) {
                return RiskLevel.URGENT
            }
            if (concernKeywords.count { text.contains(it) } >= 2) {
                return RiskLevel.CONCERN
            }
        }
        
        val severeEmotions = listOf("angry", "sad", "anxious", "upset")
        if (emotions.count { it.lowercase() in severeEmotions } >= 3) {
            return RiskLevel.CONCERN
        }
        
        return RiskLevel.SAFE
    }
    
    private fun calculateIntensity(emotions: List<String>, thoughts: String?): EmotionIntensity {
        val intensityWords = setOf("very", "extremely", "completely", "totally", "absolutely")
        val thoughtIntensity = thoughts?.lowercase()?.let { text ->
            intensityWords.count { word -> text.contains(word) }
        } ?: 0
        
        return when {
            thoughtIntensity >= 3 || emotions.size >= 5 -> EmotionIntensity.EXTREME
            thoughtIntensity >= 2 || emotions.size >= 4 -> EmotionIntensity.HIGH
            thoughtIntensity >= 1 || emotions.size >= 2 -> EmotionIntensity.MEDIUM
            else -> EmotionIntensity.LOW
        }
    }
}