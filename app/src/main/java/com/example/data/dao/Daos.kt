package com.example.data.dao

import androidx.room.*
import com.example.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY importanceScore DESC, timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageItem>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): MessageItem?

    @Query("SELECT * FROM messages WHERE isReplyNeeded = 1 AND isReplied = 0 AND isArchived = 0 ORDER BY timestamp DESC")
    fun getUnansweredMessagesFlow(): Flow<List<MessageItem>>

    @Query("SELECT * FROM messages WHERE urgency = 'CRITICAL' OR urgency = 'HIGH' OR isImportant = 1 ORDER BY importanceScore DESC")
    fun getUrgentMessagesFlow(): Flow<List<MessageItem>>

    @Query("SELECT * FROM messages WHERE isImportant = 1 OR urgency = 'CRITICAL' ORDER BY timestamp DESC")
    fun getImportantNowMessagesFlow(): Flow<List<MessageItem>>

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageItem>)

    @Update
    suspend fun updateMessage(message: MessageItem)

    @Delete
    suspend fun deleteMessage(message: MessageItem)

    @Query("UPDATE messages SET isReplied = 1 WHERE id = :id")
    suspend fun markAsReplied(id: String)

    @Query("UPDATE messages SET isArchived = 1 WHERE id = :id")
    suspend fun archiveMessage(id: String)

    @Query("UPDATE messages SET isImportant = :important WHERE id = :id")
    suspend fun toggleImportant(id: String, important: Boolean)

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, isUrgentNow DESC, dueDate ASC, createdAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY isUrgentNow DESC, dueDate ASC")
    fun getActiveTasksFlow(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (isUrgentNow = 1 OR urgency = 'CRITICAL' OR urgency = 'HIGH') ORDER BY dueDate ASC")
    fun getImportantNowTasksFlow(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItem>)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: String, completed: Boolean)

    @Query("UPDATE tasks SET isSnoozed = 1, snoozeUntil = :snoozeTime WHERE id = :id")
    suspend fun snoozeTask(id: String, snoozeTime: Long)

    @Query("UPDATE tasks SET isUrgentNow = :isUrgent WHERE id = :id")
    suspend fun setTaskUrgentNow(id: String, isUrgent: Boolean)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllMemoriesFlow(): Flow<List<MemoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryItem>)

    @Update
    suspend fun updateMemory(memory: MemoryItem)

    @Delete
    suspend fun deleteMemory(memory: MemoryItem)

    @Query("UPDATE memories SET isArchived = 1 WHERE id = :id")
    suspend fun archiveMemory(id: String)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()
}

@Dao
interface BriefingDao {
    @Query("SELECT * FROM briefings ORDER BY timestamp DESC")
    fun getAllBriefingsFlow(): Flow<List<BriefingItem>>

    @Query("SELECT * FROM briefings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBriefingFlow(): Flow<BriefingItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBriefing(briefing: BriefingItem)
}

@Dao
interface DialogueDao {
    @Query("SELECT * FROM dialogues ORDER BY timestamp ASC")
    fun getAllDialoguesFlow(): Flow<List<VoiceDialogue>>

    @Query("SELECT * FROM dialogues ORDER BY timestamp DESC LIMIT 40")
    fun getRecentDialoguesFlow(): Flow<List<VoiceDialogue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDialogue(dialogue: VoiceDialogue)

    @Query("DELETE FROM dialogues")
    suspend fun clearHistory()
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferencesFlow(): Flow<List<UserPreferenceItem>>

    @Query("SELECT value FROM user_preferences WHERE `key` = :key")
    suspend fun getPreference(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: UserPreferenceItem)

    @Query("DELETE FROM user_preferences WHERE `key` = :key")
    suspend fun deletePreference(key: String)
}
