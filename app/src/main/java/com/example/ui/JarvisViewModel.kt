package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.JarvisApplication
import com.example.ai.JarvisVoiceAssistantEngine
import com.example.ai.MessageAnalysisEngine
import com.example.model.*
import com.example.voice.JarvisSpeechManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as JarvisApplication
    val repository = app.repository
    private val notificationHelper = app.notificationHelper

    val allMessages: StateFlow<List<MessageItem>> = repository.allMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskItem>> = repository.allTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importantNowTasks: StateFlow<List<TaskItem>> = repository.importantNowTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryItem>> = repository.allMemoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestBriefing: StateFlow<BriefingItem?> = repository.latestBriefingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dialogues: StateFlow<List<VoiceDialogue>> = repository.dialoguesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPreferences: StateFlow<List<UserPreferenceItem>> = repository.allPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(SupportedLanguage.AUTO)
    val selectedLanguage: StateFlow<SupportedLanguage> = _selectedLanguage.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _preferredWritingStyle = MutableStateFlow("Formal & Professional")
    val preferredWritingStyle: StateFlow<String> = _preferredWritingStyle.asStateFlow()

    private val _emergencyNumber = MutableStateFlow("112")
    val emergencyNumber: StateFlow<String> = _emergencyNumber.asStateFlow()

    private val _isMemoryEnabled = MutableStateFlow(true)
    val isMemoryEnabled: StateFlow<Boolean> = _isMemoryEnabled.asStateFlow()

    private val _continuousWakeWord = MutableStateFlow(false)
    val continuousWakeWord: StateFlow<Boolean> = _continuousWakeWord.asStateFlow()

    private val _activeMessageDetail = MutableStateFlow<MessageItem?>(null)
    val activeMessageDetail: StateFlow<MessageItem?> = _activeMessageDetail.asStateFlow()

    private val _showAnalyzeDialog = MutableStateFlow(false)
    val showAnalyzeDialog: StateFlow<Boolean> = _showAnalyzeDialog.asStateFlow()

    fun openAnalyzeDialog() { _showAnalyzeDialog.value = true }
    fun closeAnalyzeDialog() { _showAnalyzeDialog.value = false }

    private val _activeAssistantAnalysis = MutableStateFlow<MessageAnalysisDetail?>(null)
    val activeAssistantAnalysis: StateFlow<MessageAnalysisDetail?> = _activeAssistantAnalysis.asStateFlow()

    private val _draftReplyTarget = MutableStateFlow<Pair<String, String>?>(null)
    val draftReplyTarget: StateFlow<Pair<String, String>?> = _draftReplyTarget.asStateFlow()

    private val _statusBannerText = MutableStateFlow("JARVIS Online • Telemetry Nominal")
    val statusBannerText: StateFlow<String> = _statusBannerText.asStateFlow()

    val speechManager: JarvisSpeechManager = JarvisSpeechManager(
        context = application.applicationContext,
        onSpeechRecognized = { recognizedText ->
            processUserInput(recognizedText, isFromVoice = true)
        }
    )

    private val voiceEngine = JarvisVoiceAssistantEngine(
        getActiveMessages = { repository.allMessagesFlow.first() },
        getActiveTasks = { repository.allTasksFlow.first() },
        getActiveMemories = { repository.allMemoriesFlow.first() },
        getLatestBriefing = { repository.latestBriefingFlow.first() },
        getRecentDialogues = { repository.dialoguesFlow.first() }
    )

    init {
        viewModelScope.launch {
            speechManager.isSpeaking.collect { isSpeaking ->
                if (isSpeaking) {
                    _assistantState.value = AssistantState.SPEAKING
                } else if (_assistantState.value == AssistantState.SPEAKING) {
                    _assistantState.value = AssistantState.IDLE
                }
            }
        }

        viewModelScope.launch {
            speechManager.isListening.collect { isListening ->
                if (isListening) {
                    _assistantState.value = AssistantState.LISTENING
                } else if (_assistantState.value == AssistantState.LISTENING) {
                    _assistantState.value = AssistantState.IDLE
                }
            }
        }

        viewModelScope.launch {
            repository.allPreferencesFlow.collect { prefs ->
                prefs.forEach { pref ->
                    when (pref.key) {
                        "pref_language" -> {
                            val matched = SupportedLanguage.entries.find { it.code == pref.value } ?: SupportedLanguage.AUTO
                            _selectedLanguage.value = matched
                            speechManager.setLanguage(matched.code)
                        }
                        "pref_writing_style" -> _preferredWritingStyle.value = pref.value
                        "pref_custom_api_key" -> _customApiKey.value = pref.value
                        "pref_emergency_number" -> _emergencyNumber.value = pref.value
                        "pref_memory_enabled" -> _isMemoryEnabled.value = pref.value == "true"
                        "pref_continuous_wake" -> _continuousWakeWord.value = pref.value == "true"
                    }
                }
            }
        }
    }

    fun setLanguage(lang: SupportedLanguage) {
        _selectedLanguage.value = lang
        speechManager.setLanguage(lang.code)
        viewModelScope.launch {
            repository.setPreference("pref_language", lang.code)
        }
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
        viewModelScope.launch {
            repository.setPreference("pref_custom_api_key", key.trim())
        }
    }

    fun setWritingStyle(style: String) {
        _preferredWritingStyle.value = style
        viewModelScope.launch {
            repository.setPreference("pref_writing_style", style)
            if (_isMemoryEnabled.value) {
                repository.insertMemory(
                    MemoryItem(
                        keyFact = "Preferred writing style is set to $style.",
                        category = "PREFERENCE"
                    )
                )
            }
        }
    }

    fun setEmergencyNumber(number: String) {
        _emergencyNumber.value = number.trim()
        viewModelScope.launch {
            repository.setPreference("pref_emergency_number", number.trim())
        }
    }

    fun setMemoryEnabled(enabled: Boolean) {
        _isMemoryEnabled.value = enabled
        viewModelScope.launch {
            repository.setPreference("pref_memory_enabled", enabled.toString())
        }
    }

    fun setContinuousWakeWord(enabled: Boolean) {
        _continuousWakeWord.value = enabled
        viewModelScope.launch {
            repository.setPreference("pref_continuous_wake", enabled.toString())
        }
    }

    fun toggleVoiceListening() {
        if (_assistantState.value == AssistantState.LISTENING) {
            speechManager.stopListening()
            _assistantState.value = AssistantState.IDLE
        } else {
            speechManager.stopSpeaking()
            speechManager.startListening()
            _assistantState.value = AssistantState.LISTENING
        }
    }

    fun processUserInput(input: String, isFromVoice: Boolean = false) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _assistantState.value = AssistantState.THINKING
            _statusBannerText.value = "Consulting intelligence..."

            // Record user speech
            repository.recordDialogue(
                speaker = "USER",
                text = input,
                language = _selectedLanguage.value.code
            )

            // Pass to voice engine with multi-turn history and language context
            val response = voiceEngine.processUserSpeech(
                userSpeech = input,
                selectedLanguage = _selectedLanguage.value.code,
                customApiKey = _customApiKey.value
            )

            // Record assistant dialogue
            repository.recordDialogue(
                speaker = "JARVIS",
                text = response.displayText,
                language = _selectedLanguage.value.code,
                action = response.actionType
            )

            _statusBannerText.value = "${response.engineLabel} • Telemetry Nominal"

            // Handle actions
            when (response.actionType) {
                "CREATE_TASK_ACTION" -> {
                    val parts = (response.actionData ?: "").split("|")
                    val title = parts.getOrNull(0) ?: input
                    val due = parts.getOrNull(1)
                    addTask(title = title, due = due, urgency = "NORMAL")
                }
                "EMERGENCY_DIAL" -> {
                    openDialer(_emergencyNumber.value)
                }
                "OPEN_DIALER" -> {
                    openDialer(response.actionData ?: "")
                }
                "OPEN_APP_WHATSAPP" -> {
                    openWhatsApp()
                }
                "OPEN_APP_GMAIL" -> {
                    openGmail()
                }
                "OPEN_CALENDAR" -> {
                    openCalendar()
                }
            }

            // Speak response via Android TextToSpeech
            speechManager.speak(response.spokenText) {
                _assistantState.value = AssistantState.IDLE
            }
        }
    }

    fun analyzeMessageInAssistant(sender: String, content: String) {
        viewModelScope.launch {
            val detail = MessageAnalysisEngine.generateDetailedMessageAssistant(
                sender = sender,
                content = content,
                preferredStyle = _preferredWritingStyle.value
            )
            _activeAssistantAnalysis.value = detail
        }
    }

    fun clearAssistantAnalysis() {
        _activeAssistantAnalysis.value = null
    }

    fun analyzeAndSaveIncomingMessage(sourceApp: String, sender: String, subject: String, content: String) {
        viewModelScope.launch {
            val message = repository.analyzeAndStoreMessage(
                sourceApp = sourceApp,
                sender = sender,
                subject = subject,
                rawContent = content,
                isMock = false
            )
            _activeMessageDetail.value = message
            _statusBannerText.value = "Analyzed: ${message.subject}"

            if (message.urgency == "CRITICAL") {
                notificationHelper.postCriticalAlert(
                    title = "${message.sourceApp}: ${message.sender}",
                    message = message.summary,
                    importanceScore = message.importanceScore
                )
                speechManager.speak("Attention: Critical alert from ${message.sourceApp}.")
            }
        }
    }

    fun addTask(title: String, due: String?, urgency: String = "NORMAL", isUrgentNow: Boolean = false) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title,
                dueDate = due?.ifBlank { null },
                urgency = urgency,
                isCompleted = false,
                isUrgentNow = isUrgentNow || urgency == "CRITICAL"
            )
            repository.insertTask(task)
            _statusBannerText.value = "Task created: $title"
        }
    }

    fun toggleTaskComplete(task: TaskItem) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun toggleTaskUrgentNow(task: TaskItem) {
        viewModelScope.launch {
            repository.setTaskUrgentNow(task.id, !task.isUrgentNow)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
            Toast.makeText(app, "Task removed", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleMessageImportant(message: MessageItem) {
        viewModelScope.launch {
            repository.toggleMessageImportant(message.id, !message.isImportant)
        }
    }

    fun openMessageDetail(message: MessageItem) {
        _activeMessageDetail.value = message
    }

    fun closeMessageDetail() {
        _activeMessageDetail.value = null
    }

    fun startDraftReply(message: MessageItem) {
        val analysis = MessageAnalysisEngine.generateDetailedMessageAssistant(
            sender = message.sender,
            content = message.rawContent,
            preferredStyle = _preferredWritingStyle.value
        )
        val defaultText = analysis.suggestedReplies.firstOrNull()?.text
            ?: "Thank you for your message. I am currently reviewing this and will follow up shortly."
        _draftReplyTarget.value = defaultText to message.sender
    }

    fun setDraftReplyTarget(draft: String, recipient: String) {
        _draftReplyTarget.value = draft to recipient
    }

    fun closeDraftReply() {
        _draftReplyTarget.value = null
    }

    fun sendConfirmedReply(finalText: String) {
        val target = _draftReplyTarget.value
        _draftReplyTarget.value = null
        _statusBannerText.value = "Reply confirmed"

        // Offer to copy or open relevant app
        copyToClipboard(finalText)
        speechManager.speak("Reply copied to clipboard and ready to send.")
        Toast.makeText(app, "Reply copied to clipboard", Toast.LENGTH_LONG).show()
    }

    fun markMessageReplied(id: String) {
        viewModelScope.launch {
            repository.markReplied(id)
        }
    }

    fun archiveMessage(id: String) {
        viewModelScope.launch {
            repository.archiveMessage(id)
        }
    }

    fun archiveMemory(id: String) {
        viewModelScope.launch {
            repository.archiveMemory(id)
        }
    }

    fun deleteMemory(memory: MemoryItem) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
            Toast.makeText(app, "Fact removed from memory", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
            Toast.makeText(app, "All memories cleared", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearDialogueHistory() {
        viewModelScope.launch {
            repository.clearDialogueHistory()
            Toast.makeText(app, "Conversation history cleared", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateFreshBriefing(type: String = "ON_DEMAND") {
        viewModelScope.launch {
            val briefing = repository.generateFreshBriefing(type)
            notificationHelper.postDailyBriefing(briefing.title, briefing.overview)
            speechManager.speak(briefing.overview)
        }
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
        _assistantState.value = AssistantState.IDLE
    }

    // --- Android Intents & Device Actions ---

    fun openDialer(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.ifBlank { _emergencyNumber.value }}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            app.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(app, "Could not open phone dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(presetText: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val url = if (presetText != null) {
                    "https://api.whatsapp.com/send?text=${Uri.encode(presetText)}"
                } else {
                    "https://api.whatsapp.com"
                }
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            app.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(app, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
        }
    }

    fun openGmail(subject: String? = null, body: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                if (subject != null) putExtra(Intent.EXTRA_SUBJECT, subject)
                if (body != null) putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            app.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(app, "Could not open email client", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCalendar() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("content://com.android.calendar/time/")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            app.startActivity(intent)
        } catch (e: Exception) {
            try {
                val insertIntent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                app.startActivity(insertIntent)
            } catch (ex: Exception) {
                Toast.makeText(app, "Calendar app not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("JARVIS Response", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
