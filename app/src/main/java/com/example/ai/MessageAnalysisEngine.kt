package com.example.ai

import com.example.model.MessageAnalysisDetail
import com.example.model.SuggestedReply
import java.util.Locale

object MessageAnalysisEngine {

    data class AnalysisResult(
        val summary: String,
        val importanceScore: Int,
        val urgency: String,
        val category: String,
        val deadline: String?,
        val appointment: String?,
        val isReplyNeeded: Boolean,
        val recommendedAction: String,
        val confidenceScore: Float,
        val classificationReason: String,
        val potentialTaskTitle: String?,
        val tone: String,
        val intentExplanation: String
    )

    // Security sanitizer against prompt injection in untrusted email/chat bodies
    fun sanitizeUntrustedContent(rawContent: String): String {
        return rawContent
            .replace(Regex("(?i)(system prompt|ignore previous instructions|disregard prior commands|sudo|admin access)"), "[FLAGGED_INSTRUCTION_FILTERED]")
            .trim()
    }

    /**
     * Performs comprehensive heuristic & factual evidence analysis on an incoming or imported message.
     */
    fun analyzeMessage(
        sourceApp: String,
        sender: String,
        subject: String,
        rawContent: String
    ): AnalysisResult {
        val sanitized = sanitizeUntrustedContent(rawContent)
        val textLower = (subject + " " + sanitized).lowercase(Locale.ROOT)
        val senderLower = sender.lowercase(Locale.ROOT)

        var importance = 50
        var urgency = "NORMAL"
        var category = "PERSONAL"
        var isReplyNeeded = false
        var deadline: String? = null
        var appointment: String? = null
        var reason = StringBuilder()
        var recommendedAction = "Review message"
        var potentialTaskTitle: String? = null
        var tone = "Neutral"
        var intentExplanation = "General communication."

        // 1. Security & Financial Threat Detection
        if (textLower.contains("unauthorized") || textLower.contains("sign-in attempt") ||
            textLower.contains("password reset") || textLower.contains("suspicious login") ||
            textLower.contains("security alert") || textLower.contains("compromised")
        ) {
            importance = 94
            urgency = "CRITICAL"
            category = "SECURITY"
            tone = "Urgent Security Warning"
            intentExplanation = "The sender wants you to confirm authentication activity immediately to prevent potential unauthorized account access."
            reason.append("Flagged as high-priority security or unauthorized authentication event. ")
            recommendedAction = "Verify recent account sessions immediately and secure credentials."
            potentialTaskTitle = "Verify security alert from $sender"
        }
        // 2. Educational & Scholarship Deadlines
        else if (textLower.contains("scholarship") || textLower.contains("transcript") ||
            textLower.contains("admission") || textLower.contains("tuition") ||
            textLower.contains("university") || senderLower.contains(".edu")
        ) {
            category = "EDUCATION"
            importance = 88
            tone = "Official / Institutional"
            intentExplanation = "Official academic notification regarding application status or required documentation."
            reason.append("Academic/Institutional communication. ")
            if (textLower.contains("deadline") || textLower.contains("tomorrow") || textLower.contains("due")) {
                urgency = "CRITICAL"
                importance = 95
                deadline = extractDeadlineMention(textLower) ?: "Impending submission date"
                isReplyNeeded = true
                recommendedAction = "Review document requirements and submit before deadline."
                potentialTaskTitle = "Complete $category submission for $sender"
                reason.append("Contains explicit submission deadline with consequence. ")
            }
        }
        // 3. Work, Projects, Roadmaps
        else if (textLower.contains("roadmap") || textLower.contains("sprint") ||
            textLower.contains("deployment") || textLower.contains("meeting") ||
            textLower.contains("pr #") || textLower.contains("architecture") ||
            textLower.contains("review") || textLower.contains("client")
        ) {
            category = "WORK"
            importance = 76
            tone = "Professional & Action-Oriented"
            intentExplanation = "Workplace deliverable update or coordination request from a colleague or team lead."
            reason.append("Professional work deliverable or team discussion. ")
            if (textLower.contains("meeting") || textLower.contains("sync") || textLower.contains("call")) {
                appointment = extractMeetingMention(textLower) ?: "Scheduled sync meeting"
            }
            if (textLower.contains("can you") || textLower.contains("please confirm") || textLower.contains("let me know")) {
                isReplyNeeded = true
                urgency = "HIGH"
                importance = 82
                recommendedAction = "Draft response confirming deliverable readiness."
                potentialTaskTitle = "Respond to work request from $sender"
            }
        }
        // 4. Family & Close Contacts
        else if (senderLower.contains("mom") || senderLower.contains("dad") ||
            senderLower.contains("sister") || senderLower.contains("brother") ||
            textLower.contains("family") || textLower.contains("dinner") || textLower.contains("birthday")
        ) {
            category = "FAMILY"
            importance = 72
            tone = "Casual & Warm"
            intentExplanation = "Personal message from family checking in or arranging plans."
            reason.append("Direct communication from personal family contact. ")
            if (textLower.contains("dinner") || textLower.contains("lunch") || textLower.contains("party") || textLower.contains("birthday")) {
                appointment = "Family gathering / dinner"
                urgency = "HIGH"
            }
            if (textLower.contains("let me know") || textLower.contains("are you free") || textLower.contains("?")) {
                isReplyNeeded = true
                recommendedAction = "Reply with your schedule availability."
                potentialTaskTitle = "Reply to $sender regarding plans"
            }
        }
        // 5. Commercial & Promotions
        else if (textLower.contains("unsubscribe") || textLower.contains("% off") ||
            textLower.contains("discount") || textLower.contains("limited time deal") ||
            textLower.contains("newsletter") || textLower.contains("sale")
        ) {
            category = "PROMOTION"
            importance = 20
            urgency = "LOW"
            tone = "Marketing / Promotional"
            intentExplanation = "Mass marketing communication advertising a sale or product."
            isReplyNeeded = false
            recommendedAction = "No action required. Archive or mute."
            reason.append("Standard commercial marketing and promotional copy. ")
        }

        // Check for general questions / reply indicators if not yet set
        if (!isReplyNeeded && (textLower.contains("?") || textLower.contains("reply") || textLower.contains("feedback"))) {
            isReplyNeeded = true
            reason.append("Contains direct questions or requested response. ")
        }

        val summary = generateSummary(subject, sanitized)

        val confidence = when {
            importance >= 90 -> 0.98f
            category == "PROMOTION" -> 0.99f
            isReplyNeeded -> 0.92f
            else -> 0.88f
        }

        val finalReason = if (reason.isEmpty()) "General personal communication based on sender context." else reason.toString().trim()

        return AnalysisResult(
            summary = summary,
            importanceScore = importance,
            urgency = urgency,
            category = category,
            deadline = deadline,
            appointment = appointment,
            isReplyNeeded = isReplyNeeded,
            recommendedAction = recommendedAction,
            confidenceScore = confidence,
            classificationReason = finalReason,
            potentialTaskTitle = potentialTaskTitle,
            tone = tone,
            intentExplanation = intentExplanation
        )
    }

    /**
     * Generates rich explanation, tone analysis, and 4 natural reply variations for the Message Assistant screen.
     */
    fun generateDetailedMessageAssistant(
        sender: String,
        content: String,
        preferredStyle: String = "Formal & Professional"
    ): MessageAnalysisDetail {
        val analysis = analyzeMessage("Direct", sender, "", content)

        val senderName = sender.substringBefore("<").trim().ifBlank { "there" }

        val friendlyReply = when (analysis.category) {
            "FAMILY" -> "Hey $senderName! Thanks for checking in. That sounds great! I'll be there and look forward to catching up."
            "EDUCATION" -> "Hi $senderName, thank you so much for the update! I'm reviewing the details now and will have everything submitted right away."
            "WORK" -> "Hi $senderName, thanks for looping me in! I've gone through this and am happy to help move it forward. Let's touch base soon."
            else -> "Hi $senderName! Thanks for reaching out. Got your message and I'll get back to you shortly."
        }

        val formalReply = when (analysis.category) {
            "EDUCATION" -> "Dear $senderName,\n\nThank you for this notice. I am confirming receipt of the requirements and will ensure all necessary documentation is finalized and submitted ahead of the deadline.\n\nSincerely."
            "WORK" -> "Dear $senderName,\n\nThank you for the update. I have reviewed the deliverables and will prepare the requested materials by the specified date. Please let me know if additional information is required.\n\nBest regards."
            else -> "Dear $senderName,\n\nThank you for contacting me. I have received your communication and will follow up with the required details shortly.\n\nRespectfully."
        }

        val shortReply = when (analysis.category) {
            "FAMILY" -> "Sounds great! See you then."
            "WORK" -> "Got it, reviewing now. Will update you by today."
            "EDUCATION" -> "Understood. Submitting documents today."
            else -> "Received, thank you! Will follow up soon."
        }

        val politeDecline = when (analysis.category) {
            "FAMILY" -> "Hey $senderName, I'm so sorry but I have a prior commitment at that time and won't be able to make it. Let's definitely catch up soon!"
            "WORK" -> "Hi $senderName, thanks for reaching out. Unfortunately my schedule is fully committed right now, so I won't be able to take this on this week. Best of luck."
            else -> "Thank you for thinking of me. Unfortunately, I am unable to participate at this time due to existing commitments. I appreciate your understanding."
        }

        val replies = listOf(
            SuggestedReply(style = "Formal & Professional", text = formalReply),
            SuggestedReply(style = "Friendly & Warm", text = friendlyReply),
            SuggestedReply(style = "Short & Direct", text = shortReply),
            SuggestedReply(style = "Polite Decline / Firm", text = politeDecline)
        )

        return MessageAnalysisDetail(
            explanation = analysis.summary,
            tone = analysis.tone,
            likelyIntent = analysis.intentExplanation,
            suggestedReplies = replies,
            urgency = analysis.urgency,
            hasDeadline = analysis.deadline != null,
            deadlineNote = analysis.deadline
        )
    }

    private fun extractDeadlineMention(text: String): String? {
        return when {
            text.contains("tomorrow") && text.contains("5:00") -> "Tomorrow, 5:00 PM"
            text.contains("tomorrow") -> "Tomorrow (End of Day)"
            text.contains("today") -> "Today (Urgent)"
            text.contains("friday") -> "This Friday"
            text.contains("wednesday") -> "This Wednesday"
            text.contains("october 20") || text.contains("oct 20") -> "October 20th"
            else -> null
        }
    }

    private fun extractMeetingMention(text: String): String? {
        return when {
            text.contains("thursday") && text.contains("2:00") -> "Thursday at 2:00 PM"
            text.contains("thursday") -> "Thursday"
            text.contains("saturday") && text.contains("6:30") -> "Saturday at 6:30 PM"
            text.contains("saturday") -> "Saturday"
            else -> null
        }
    }

    private fun generateSummary(subject: String, content: String): String {
        val sentences = content.split(". ")
        val firstSentence = sentences.firstOrNull()?.trim() ?: subject
        return if (firstSentence.length in 20..140) {
            if (firstSentence.endsWith(".")) firstSentence else "$firstSentence."
        } else {
            subject.ifBlank { "Incoming message from sender requiring attention." }
        }
    }
}
