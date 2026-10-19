package com.example.data.repository

import com.example.ai.MessageAnalysisEngine
import com.example.data.JarvisDatabase
import com.example.data.MockDataGenerator
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class JarvisRepository(
    private val database: JarvisDatabase
) {
    private val messageDao = database.messageDao()
    private val taskDao = database.taskDao()
    private val memoryDao = database.memoryDao()
    private val briefingDao = database.briefingDao()
    private val dialogueDao = database.dialogueDao()
    private val preferenceDao = database.userPreferenceDao()

    val allMessagesFlow: Flow<List<MessageItem>> = messageDao.getAllMessagesFlow()
    val allTasksFlow: Flow<List<TaskItem>> = taskDao.getAllTasksFlow()
    val activeTasksFlow: Flow<List<TaskItem>> = taskDao.getActiveTasksFlow()
    val importantNowTasksFlow: Flow<List<TaskItem>> = taskDao.getImportantNowTasksFlow()
    val allMemoriesFlow: Flow<List<MemoryItem>> = memoryDao.getAllMemoriesFlow()
    val latestBriefingFlow: Flow<BriefingItem?> = briefingDao.getLatestBriefingFlow()
    val dialoguesFlow: Flow<List<VoiceDialogue>> = dialogueDao.getRecentDialoguesFlow()
    val allPreferencesFlow: Flow<List<UserPreferenceItem>> = preferenceDao.getAllPreferencesFlow()

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (messageDao.getCount() == 0) {
            messageDao.insertMessages(MockDataGenerator.generateInitialMessages())
            taskDao.insertTasks(MockDataGenerator.generateInitialTasks())
            memoryDao.insertMemories(MockDataGenerator.generateInitialMemories())
            briefingDao.insertBriefing(MockDataGenerator.generateInitialBriefing())
            dialogueDao.insertDialogue(
                VoiceDialogue(
                    speaker = "JARVIS",
                    text = "Systems online. Good day, sir. All neural engines and communication monitors are active. How may I be of service?"
                )
            )
            // Initial default preferences
            preferenceDao.setPreference(UserPreferenceItem("pref_language", "en"))
            preferenceDao.setPreference(UserPreferenceItem("pref_writing_style", "Formal & Professional"))
            preferenceDao.setPreference(UserPreferenceItem("pref_memory_enabled", "true"))
            preferenceDao.setPreference(UserPreferenceItem("pref_emergency_number", "112"))
        }
    }

    suspend fun analyzeAndStoreMessage(
        sourceApp: String,
        sender: String,
        subject: String,
        rawContent: String,
        isMock: Boolean = false
    ): MessageItem = withContext(Dispatchers.IO) {
        val analysis = MessageAnalysisEngine.analyzeMessage(
            sourceApp = sourceApp,
            sender = sender,
            subject = subject,
            rawContent = rawContent
        )

        val messageItem = MessageItem(
            id = UUID.randomUUID().toString(),
            sourceApp = sourceApp,
            sender = sender,
            timestamp = System.currentTimeMillis(),
            subject = subject,
            rawContent = rawContent,
            summary = analysis.summary,
            importanceScore = analysis.importanceScore,
            urgency = analysis.urgency,
            category = analysis.category,
            deadline = analysis.deadline,
            appointmentDetails = analysis.appointment,
            isReplyNeeded = analysis.isReplyNeeded,
            recommendedAction = analysis.recommendedAction,
            confidenceScore = analysis.confidenceScore,
            classificationReason = analysis.classificationReason,
            tone = analysis.tone,
            intentAnalysis = analysis.intentExplanation,
            isImportant = analysis.urgency == "CRITICAL",
            isMockData = isMock
        )

        messageDao.insertMessage(messageItem)

        // If high importance or has potential task, automatically record commitment to task list and memory
        if (analysis.potentialTaskTitle != null) {
            val task = TaskItem(
                title = analysis.potentialTaskTitle,
                description = analysis.summary,
                sourceMessageId = messageItem.id,
                sourceApp = sourceApp,
                dueDate = analysis.deadline,
                urgency = analysis.urgency,
                category = analysis.category,
                isUrgentNow = analysis.urgency == "CRITICAL"
            )
            taskDao.insertTask(task)
        }

        val memoryEnabled = preferenceDao.getPreference("pref_memory_enabled") ?: "true"
        if (memoryEnabled == "true" && analysis.importanceScore >= 70) {
            val memory = MemoryItem(
                keyFact = "${analysis.category}: ${analysis.summary}",
                contextDetails = "Extracted from $sourceApp message by $sender.",
                category = analysis.category,
                sourceApp = sourceApp,
                sourceSender = sender,
                originalMessageId = messageItem.id,
                isConfirmed = true,
                confidence = analysis.confidenceScore
            )
            memoryDao.insertMemory(memory)
        }

        messageItem
    }

    suspend fun insertTask(task: TaskItem) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskItem) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun setTaskCompleted(id: String, completed: Boolean) = withContext(Dispatchers.IO) {
        taskDao.setTaskCompleted(id, completed)
    }

    suspend fun setTaskUrgentNow(id: String, isUrgent: Boolean) = withContext(Dispatchers.IO) {
        taskDao.setTaskUrgentNow(id, isUrgent)
    }

    suspend fun deleteTask(task: TaskItem) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    suspend fun insertMemory(memory: MemoryItem) = withContext(Dispatchers.IO) {
        memoryDao.insertMemory(memory)
    }

    suspend fun archiveMemory(id: String) = withContext(Dispatchers.IO) {
        memoryDao.archiveMemory(id)
    }

    suspend fun deleteMemory(memory: MemoryItem) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun clearAllMemories() = withContext(Dispatchers.IO) {
        memoryDao.clearAllMemories()
    }

    suspend fun markReplied(messageId: String) = withContext(Dispatchers.IO) {
        messageDao.markAsReplied(messageId)
    }

    suspend fun archiveMessage(messageId: String) = withContext(Dispatchers.IO) {
        messageDao.archiveMessage(messageId)
    }

    suspend fun toggleMessageImportant(id: String, important: Boolean) = withContext(Dispatchers.IO) {
        messageDao.toggleImportant(id, important)
    }

    suspend fun deleteMessage(message: MessageItem) = withContext(Dispatchers.IO) {
        messageDao.deleteMessage(message)
    }

    suspend fun recordDialogue(speaker: String, text: String, language: String = "en", action: String? = null) = withContext(Dispatchers.IO) {
        dialogueDao.insertDialogue(
            VoiceDialogue(
                speaker = speaker,
                text = text,
                language = language,
                actionTriggered = action
            )
        )
    }

    suspend fun clearDialogueHistory() = withContext(Dispatchers.IO) {
        dialogueDao.clearHistory()
    }

    suspend fun getPreference(key: String): String? = withContext(Dispatchers.IO) {
        preferenceDao.getPreference(key)
    }

    suspend fun setPreference(key: String, value: String) = withContext(Dispatchers.IO) {
        preferenceDao.setPreference(UserPreferenceItem(key, value))
    }

    suspend fun generateFreshBriefing(type: String = "ON_DEMAND"): BriefingItem = withContext(Dispatchers.IO) {
        val messages = messageDao.getAllMessagesFlow().first()
        val tasks = taskDao.getActiveTasksFlow().first()

        val critical = messages.filter { it.urgency == "CRITICAL" }
        val unanswered = messages.filter { it.isReplyNeeded && !it.isReplied }

        val highlights = StringBuilder()
        if (critical.isNotEmpty()) {
            highlights.appendLine("• CRITICAL ALERT: ${critical.first().summary}")
        }
        if (tasks.isNotEmpty()) {
            highlights.appendLine("• NEXT TASK: ${tasks.first().title} (Due: ${tasks.first().dueDate ?: "Today"})")
        }
        if (unanswered.isNotEmpty()) {
            highlights.appendLine("• PENDING REPLIES: ${unanswered.size} messages awaiting your response.")
        }
        highlights.appendLine("• INTEGRATIONS: Monitored via on-device notification listener.")

        val briefing = BriefingItem(
            type = type,
            title = if (type == "MORNING") "JARVIS Morning Briefing" else "JARVIS On-Demand Intelligence Update",
            overview = "Summary: ${critical.size} critical alerts, ${tasks.size} active tasks, ${unanswered.size} pending replies.",
            keyHighlights = highlights.toString().trim(),
            criticalCount = critical.size,
            pendingTasksCount = tasks.size,
            unansweredCount = unanswered.size
        )
        briefingDao.insertBriefing(briefing)
        briefing
    }
}
