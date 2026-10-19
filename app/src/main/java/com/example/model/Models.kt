package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "messages")
data class MessageItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sourceApp: String, // Gmail, WhatsApp, Instagram, Facebook, SMS, Manual
    val sender: String,
    val senderAddress: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String,
    val rawContent: String,
    val summary: String,
    val importanceScore: Int = 50, // 0 to 100
    val urgency: String = "NORMAL", // CRITICAL, HIGH, NORMAL, LOW
    val category: String = "WORK", // EDUCATION, WORK, FINANCE, FAMILY, PERSONAL, SECURITY, OPPORTUNITY, PROMOTION, OTHER
    val deadline: String? = null,
    val appointmentDetails: String? = null,
    val isReplyNeeded: Boolean = false,
    val recommendedAction: String = "Review message",
    val confidenceScore: Float = 0.90f,
    val classificationReason: String = "Standard heuristic analysis",
    val tone: String = "Neutral", // Professional, Friendly, Urgent, Hesitant, Promotional
    val intentAnalysis: String = "",
    val isRead: Boolean = false,
    val isReplied: Boolean = false,
    val isArchived: Boolean = false,
    val isImportant: Boolean = false,
    val isMockData: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val sourceMessageId: String? = null,
    val sourceApp: String? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val urgency: String = "NORMAL", // CRITICAL, HIGH, NORMAL, LOW
    val category: String = "WORK",
    val isCompleted: Boolean = false,
    val isSnoozed: Boolean = false,
    val snoozeUntil: Long? = null,
    val isUrgentNow: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class MemoryItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val keyFact: String,
    val contextDetails: String = "",
    val category: String = "PERSONAL", // LANGUAGE, PREFERENCE, ROUTINE, FACT
    val sourceApp: String = "JARVIS",
    val sourceSender: String = "User",
    val originalMessageId: String? = null,
    val isConfirmed: Boolean = true, // confirmed facts vs tentative inferences
    val confidence: Float = 0.95f,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(tableName = "briefings")
data class BriefingItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String = "MORNING", // MORNING, EVENING, ON_DEMAND
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val overview: String,
    val keyHighlights: String, // Newline separated points
    val criticalCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val unansweredCount: Int = 0,
    val isRead: Boolean = false
)

@Entity(tableName = "dialogues")
data class VoiceDialogue(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val speaker: String, // "USER" or "JARVIS"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "en",
    val actionTriggered: String? = null
)

@Entity(tableName = "user_preferences")
data class UserPreferenceItem(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class SuggestedReply(
    val style: String, // "Friendly & Warm", "Formal & Professional", "Short & Direct", "Polite Decline"
    val text: String
)

data class MessageAnalysisDetail(
    val explanation: String,
    val tone: String,
    val likelyIntent: String,
    val suggestedReplies: List<SuggestedReply>,
    val urgency: String,
    val hasDeadline: Boolean,
    val deadlineNote: String?
)

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class SupportedLanguage(val code: String, val displayName: String, val speechLocaleTag: String) {
    AUTO("auto", "Auto-Detect", "en-US"),
    ENGLISH("en", "English", "en-US"),
    SWAHILI("sw", "Kiswahili (Swahili)", "sw-KE"),
    KIKAMBA("kam", "Kikamba", "kam-KE")
}
