package com.example.ai

import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisVoiceAssistantEngine(
    private val getActiveMessages: suspend () -> List<MessageItem>,
    private val getActiveTasks: suspend () -> List<TaskItem>,
    private val getActiveMemories: suspend () -> List<MemoryItem>,
    private val getLatestBriefing: suspend () -> BriefingItem?,
    private val getRecentDialogues: suspend () -> List<VoiceDialogue>
) {

    data class AssistantResponse(
        val spokenText: String,
        val displayText: String,
        val isCloudAi: Boolean = false,
        val engineLabel: String = "On-Device Offline",
        val engineNotice: String? = null,
        val actionType: String? = null,
        val actionData: String? = null,
        val relatedItem: Any? = null
    )

    suspend fun processUserSpeech(
        userSpeech: String,
        selectedLanguage: String = "auto",
        customApiKey: String? = null
    ): AssistantResponse = withContext(Dispatchers.Default) {
        val trimmed = userSpeech.trim()
        val queryLower = trimmed.lowercase(Locale.ROOT)

        val messages = getActiveMessages()
        val tasks = getActiveTasks()
        val memories = getActiveMemories()
        val recentDialogues = getRecentDialogues()

        // 1. Check for immediate explicit Android device action commands
        val detectedAction = detectDirectPhoneAction(queryLower, trimmed)
        if (detectedAction != null) {
            return@withContext detectedAction
        }

        // 2. Prepare context summary for AI
        val criticalMessages = messages.filter { it.urgency == "CRITICAL" || it.urgency == "HIGH" }
        val unansweredMessages = messages.filter { it.isReplyNeeded && !it.isReplied && !it.isArchived }
        val pendingTasks = tasks.filter { !it.isCompleted }
        val memoryFacts = memories.take(6).joinToString("; ") { it.keyFact }

        val contextSnapshot = buildString {
            append("Tasks: ${pendingTasks.size} pending")
            if (pendingTasks.isNotEmpty()) {
                append(" (Next: '${pendingTasks.first().title}')")
            }
            append(". Critical alerts: ${criticalMessages.size}")
            if (criticalMessages.isNotEmpty()) {
                append(" ('${criticalMessages.first().summary}')")
            }
            append(". Unreplied messages: ${unansweredMessages.size}")
            if (memoryFacts.isNotBlank()) {
                append(". Saved memories: [$memoryFacts]")
            }
        }

        // 3. System instruction enforcing non-repetitive, natural, multilingual behavior
        val systemInstruction = buildString {
            appendLine("You are JARVIS, an intelligent, helpful personal assistant running on the user's Android device.")
            appendLine("CORE DIRECTIVES:")
            appendLine("1. NEVER begin your answer with robotic filler phrases like 'Hello', 'Certainly', 'Sure', 'Of course', 'I am JARVIS', or 'As an AI'.")
            appendLine("2. NEVER repeat the user's question back to them before answering.")
            appendLine("3. Vary your wording naturally and avoid repetitive closing remarks.")
            appendLine("4. Answer in the same language the user speaks: if the user writes or speaks in Swahili (Kiswahili), reply in natural, fluent Swahili. If in Kikamba, reply in Kikamba or Swahili. If in English, reply in English.")
            appendLine("5. Keep conversational answers concise and punchy for text-to-speech voice playback.")
            appendLine("6. Current phone context: $contextSnapshot.")
            appendLine("7. If the user asks about deadlines, tasks, or messages, use the exact facts from context. Do not fabricate unmentioned messages or events.")
            appendLine("8. If asked to remember a preference or rule, confirm it succinctly.")
        }

        // 4. Try Gemini API
        val effectiveKey = GeminiApiClient.getEffectiveApiKey(customApiKey)
        if (effectiveKey.isNotBlank()) {
            val historyTurns = recentDialogues.map { dialogue ->
                val role = if (dialogue.speaker.equals("USER", ignoreCase = true)) "user" else "model"
                role to dialogue.text
            }

            val apiResult = GeminiApiClient.generateContent(
                apiKey = effectiveKey,
                systemInstruction = systemInstruction,
                conversationHistory = historyTurns,
                userMessage = trimmed
            )

            if (apiResult.isSuccess) {
                val responseText = apiResult.getOrNull() ?: ""
                if (responseText.isNotBlank()) {
                    return@withContext AssistantResponse(
                        spokenText = cleanForSpeech(responseText),
                        displayText = responseText,
                        isCloudAi = true,
                        engineLabel = "Gemini 2.5 Flash"
                    )
                }
            }
        }

        // 5. Intelligent Fallback (Context-aware, zero-repetition, multilingual)
        val fallback = generateIntelligentFallback(queryLower, trimmed, messages, pendingTasks, unansweredMessages)
        fallback.copy(
            isCloudAi = false,
            engineLabel = "On-Device Offline",
            engineNotice = if (effectiveKey.isBlank()) "Cloud AI key not set. Using on-device engine." else "Network issue or rate limit. Using on-device engine."
        )
    }

    private fun detectDirectPhoneAction(query: String, rawQuery: String): AssistantResponse? {
        // Emergency Call Intent
        if (query.contains("emergency") || query.contains("call 911") || query.contains("call 112") || query.contains("help me call")) {
            return AssistantResponse(
                spokenText = "Opening emergency dialer.",
                displayText = "Emergency protocol ready. Tap below to place the call.",
                actionType = "EMERGENCY_DIAL",
                actionData = "112"
            )
        }

        // General Call / Dialer
        if (query.startsWith("call ") || query.startsWith("dial ")) {
            val target = rawQuery.substringAfter("call ").substringAfter("dial ").trim()
            return AssistantResponse(
                spokenText = "Opening phone dialer for $target.",
                displayText = "Opening phone dialer for **$target**.",
                actionType = "OPEN_DIALER",
                actionData = target
            )
        }

        // Quick Task Creation: "remind me to [X] at [Y]" or "add task [X]"
        if (query.startsWith("remind me to ") || query.startsWith("add task ") || query.startsWith("create task ") || query.startsWith("kumbusho ")) {
            val taskText = rawQuery
                .removePrefix("JARVIS, ")
                .removePrefix("jarvis, ")
                .replace(Regex("(?i)^(remind me to|add task|create task|kumbusho la|nikumbushe ku)"), "")
                .trim()

            val title = taskText.substringBefore(" at ").substringBefore(" by ").substringBefore(" tomorrow").trim()
            val due = if (taskText.contains(" at ")) {
                "Today " + taskText.substringAfter(" at ").trim()
            } else if (taskText.contains(" tomorrow")) {
                "Tomorrow"
            } else null

            return AssistantResponse(
                spokenText = "Added commitment: $title.",
                displayText = "Task scheduled: **$title** ${if (due != null) "(Due: $due)" else ""}",
                actionType = "CREATE_TASK_ACTION",
                actionData = "$title|$due"
            )
        }

        // Open WhatsApp
        if (query.contains("open whatsapp") || query.contains("fungua whatsapp")) {
            return AssistantResponse(
                spokenText = "Launching WhatsApp.",
                displayText = "Opening WhatsApp.",
                actionType = "OPEN_APP_WHATSAPP"
            )
        }

        // Open Gmail
        if (query.contains("open gmail") || query.contains("check email") || query.contains("fungua barua pepe")) {
            return AssistantResponse(
                spokenText = "Opening Gmail.",
                displayText = "Opening Gmail inbox.",
                actionType = "OPEN_APP_GMAIL"
            )
        }

        // Open Calendar
        if (query.contains("open calendar") || query.contains("fungua kalenda") || query.contains("schedule a meeting")) {
            return AssistantResponse(
                spokenText = "Opening your Calendar.",
                displayText = "Opening device Calendar.",
                actionType = "OPEN_CALENDAR"
            )
        }

        return null
    }

    private fun generateIntelligentFallback(
        query: String,
        rawQuery: String,
        messages: List<MessageItem>,
        pendingTasks: List<TaskItem>,
        unanswered: List<MessageItem>
    ): AssistantResponse {
        val critical = messages.filter { it.urgency == "CRITICAL" || it.urgency == "HIGH" }

        // Swahili greetings and queries
        if (query.contains("habari") || query.contains("mambo") || query.contains("hujambo") || query.contains("shikamoo")) {
            val pendingCount = pendingTasks.size
            val greetingOptions = listOf(
                "Salama. Una kazi $pendingCount zinazosubiri. Ninaweza kukusaidia nini?",
                "Mzuri sana. Nipo tayari kukusaidia kupanga siku yako au kusoma jumbe.",
                "Hali ni shwari. Una jumbe ${unanswered.size} zinazohitaji majibu."
            )
            val selectedGreeting = greetingOptions[Math.abs(query.hashCode()) % greetingOptions.size]
            return AssistantResponse(
                spokenText = selectedGreeting,
                displayText = "$selectedGreeting\n\n*Ushauri:* Uliza kuhusu 'kazi zangu', 'jumbe za dharura', au 'fungua WhatsApp'."
            )
        }

        if (query.contains("kazi zangu") || query.contains("nini nifanye") || query.contains("kazi gani")) {
            if (pendingTasks.isEmpty()) {
                return AssistantResponse(
                    spokenText = "Huna kazi zozote zinazosubiri kwa sasa.",
                    displayText = "Hakuna kazi zinazosubiri. Ratiba yako iko wazi."
                )
            }
            val firstTask = pendingTasks.first()
            return AssistantResponse(
                spokenText = "Kazi yako ya kwanza ni ${firstTask.title}.",
                displayText = "Una kazi **${pendingTasks.size}** zinazosubiri:\n" + pendingTasks.take(3).joinToString("\n") { "• ${it.title} (${it.dueDate ?: "Leo"})" }
            )
        }

        // Time / Date queries
        if (query.contains("time") || query.contains("date") || query.contains("saa ngapi") || query.contains("leo ni")) {
            val timeFormatted = SimpleDateFormat("h:mm a, EEEE, MMM d", Locale.getDefault()).format(Date())
            return AssistantResponse(
                spokenText = "It is $timeFormatted.",
                displayText = "Current time is **$timeFormatted**."
            )
        }

        // Summarize important messages / priority
        if (query.contains("summarize") || (query.contains("important") && query.contains("message")) || query.contains("inbox")) {
            if (critical.isEmpty()) {
                return AssistantResponse(
                    spokenText = "Inbox is quiet. No urgent messages right now.",
                    displayText = "Your monitored channels are calm. No high-priority items detected."
                )
            }
            val top = critical.first()
            val sender = top.sender.substringBefore("<").trim()
            return AssistantResponse(
                spokenText = "${critical.size} urgent messages. The top one is from $sender: ${top.summary}",
                displayText = "### Priority Messages (${critical.size})\n" + critical.take(3).joinToString("\n") {
                    "• **${it.sourceApp} • ${it.sender.substringBefore("<").trim()}**: ${it.summary} *(Importance: ${it.importanceScore})*"
                },
                actionType = "SHOW_INBOX"
            )
        }

        // Urgent attention / what needs attention
        if (query.contains("attention") || query.contains("urgent") || query.contains("priority")) {
            val firstUrgentMsg = critical.firstOrNull()
            val firstUrgentTask = pendingTasks.firstOrNull { it.urgency == "CRITICAL" || it.isUrgentNow }

            if (firstUrgentMsg == null && firstUrgentTask == null) {
                return AssistantResponse(
                    spokenText = "Nothing urgent requires attention at this moment.",
                    displayText = "All clear. No critical alerts or urgent tasks require immediate action."
                )
            }

            val highlight = firstUrgentMsg?.summary ?: firstUrgentTask?.title ?: "Review priorities"
            return AssistantResponse(
                spokenText = "Main priority: $highlight.",
                displayText = "### Urgent Attention Items\n" +
                        (firstUrgentMsg?.let { "• **[${it.sourceApp}]** ${it.summary}\n" } ?: "") +
                        (firstUrgentTask?.let { "• **Task:** ${it.title} (Due: ${it.dueDate ?: "Immediate"})\n" } ?: "")
            )
        }

        // Unanswered messages
        if (query.contains("replied") || query.contains("unanswered") || query.contains("pending reply")) {
            if (unanswered.isEmpty()) {
                return AssistantResponse(
                    spokenText = "You're all caught up. No messages awaiting reply.",
                    displayText = "All messages requiring a response have been resolved."
                )
            }
            val count = unanswered.size
            val sender = unanswered.first().sender.substringBefore("<").trim()
            return AssistantResponse(
                spokenText = "$count messages need replies, starting with $sender.",
                displayText = "### Unanswered Messages ($count)\n" + unanswered.take(3).joinToString("\n") {
                    "• **${it.sourceApp}** from **${it.sender.substringBefore("<").trim()}**: ${it.subject}"
                },
                actionType = "SHOW_UNANSWERED"
            )
        }

        // Deadlines & Upcoming commitments
        if (query.contains("deadline") || query.contains("due") || query.contains("upcoming")) {
            val deadlineTasks = pendingTasks.filter { it.dueDate != null }
            if (deadlineTasks.isEmpty()) {
                return AssistantResponse(
                    spokenText = "No upcoming deadlines listed on your schedule.",
                    displayText = "No pending tasks with set due dates."
                )
            }
            val next = deadlineTasks.first()
            return AssistantResponse(
                spokenText = "Next deadline: ${next.title}, due ${next.dueDate}.",
                displayText = "### Upcoming Deadlines\n" + deadlineTasks.take(4).joinToString("\n") {
                    "• **${it.title}** — Due: ${it.dueDate}"
                }
            )
        }

        // General conversational response (varied, natural, zero robotic boilerplate)
        val shortAnswers = listOf(
            "Standing by. What would you like to review?",
            "Listening. You can check your messages, manage tasks, or draft a response.",
            "Ready. Tell me what needs to be done.",
            "All systems operational. How can I help right now?",
            "At your service. Would you like a briefing of today's schedule?"
        )
        val selected = shortAnswers[(Math.abs(query.hashCode()) % shortAnswers.size)]

        return AssistantResponse(
            spokenText = selected,
            displayText = "$selected\n\n*Quick actions:* Ask to summarize messages, list deadlines, add a task, or open WhatsApp/Gmail."
        )
    }

    private fun cleanForSpeech(text: String): String {
        return text
            .replace(Regex("#+\\s*"), "")
            .replace(Regex("\\*\\*|\\*"), "")
            .replace(Regex("\\[.*?\\]\\(.*?\\)"), "")
            .replace(Regex("•\\s*"), "")
            .trim()
            .take(300)
    }
}
