package com.example.data

import com.example.model.*
import java.util.UUID

object MockDataGenerator {

    fun generateInitialMessages(): List<MessageItem> {
        val now = System.currentTimeMillis()
        val hour = 3600_000L
        val day = 86400_000L

        return listOf(
            MessageItem(
                id = "msg-univ-01",
                sourceApp = "Gmail",
                sender = "Admissions Office <admissions@university.edu>",
                senderAddress = "admissions@university.edu",
                timestamp = now - (2 * hour),
                subject = "URGENT: Fall Semester Scholarship Document Verification Deadline",
                rawContent = "Dear Applicant, Your application portfolio has been reviewed. Please upload your certified transcripts and secondary identification documents to the applicant portal by tomorrow, October 20th at 5:00 PM EST. Failure to submit will result in forfeiture of the fellowship award.",
                summary = "Scholarship document submission required by tomorrow, Oct 20th at 5:00 PM EST to retain fellowship award.",
                importanceScore = 96,
                urgency = "CRITICAL",
                category = "EDUCATION",
                deadline = "Tomorrow, 5:00 PM EST",
                appointmentDetails = null,
                isReplyNeeded = true,
                recommendedAction = "Upload certified transcripts to applicant portal immediately and email confirmation.",
                confidenceScore = 0.98f,
                classificationReason = "Official institutional domain, impending 24-hr hard deadline, explicit financial penalty if omitted.",
                isRead = false,
                isReplied = false,
                isMockData = true
            ),
            MessageItem(
                id = "msg-bank-02",
                sourceApp = "Gmail",
                sender = "Apex Security Alert <security@apexbank.com>",
                senderAddress = "security@apexbank.com",
                timestamp = now - (5 * hour),
                subject = "New Device Login Verification: Paris, France",
                rawContent = "We detected an unauthorized sign-in attempt from IP 195.154.122.9 (Firefox on Linux, Paris). If this was not you, lock your online banking access and update your credentials immediately via your mobile banking app.",
                summary = "Suspicious login attempt detected from Paris, France. Verify or lock banking credentials.",
                importanceScore = 92,
                urgency = "CRITICAL",
                category = "SECURITY",
                deadline = "Immediate Action Required",
                appointmentDetails = null,
                isReplyNeeded = false,
                recommendedAction = "Review account access logs and verify whether login was authorized.",
                confidenceScore = 0.99f,
                classificationReason = "High-risk anomalous location security alert from financial institution.",
                isRead = false,
                isReplied = false,
                isMockData = true
            ),
            MessageItem(
                id = "msg-work-03",
                sourceApp = "Gmail",
                sender = "Sarah Chen (Director of Eng) <sarah.c@techcorp.io>",
                senderAddress = "sarah.c@techcorp.io",
                timestamp = now - (7 * hour),
                subject = "Q4 Architecture Milestone Review & Roadmap Approval",
                rawContent = "Hi team, Can you review the revised microservice migration RFC before our sync on Thursday at 2:00 PM? Please confirm whether you can prepare the performance benchmark slides by Wednesday evening.",
                summary = "Review RFC before Thursday 2:00 PM sync; confirm if performance benchmark slides can be ready Wednesday.",
                importanceScore = 82,
                urgency = "HIGH",
                category = "WORK",
                deadline = "Wednesday 6:00 PM",
                appointmentDetails = "Sync Meeting: Thursday at 2:00 PM",
                isReplyNeeded = true,
                recommendedAction = "Draft response confirming availability of benchmark slides for Wednesday.",
                confidenceScore = 0.94f,
                classificationReason = "Management direct inquiry, deliverable confirmation request, upcoming calendar meeting.",
                isRead = false,
                isReplied = false,
                isMockData = true
            ),
            MessageItem(
                id = "msg-wa-04",
                sourceApp = "WhatsApp",
                sender = "Mom",
                senderAddress = "+1 555-019-2834",
                timestamp = now - (1 * hour),
                subject = "Family Weekend Dinner Planning",
                rawContent = "Hey sweetie! Are you free this Saturday around 6:30 PM for grandpa's birthday dinner? Let me know so I can reserve the table at Bella Italia.",
                summary = "Dinner reservation inquiry for grandpa's birthday this Saturday at 6:30 PM.",
                importanceScore = 74,
                urgency = "HIGH",
                category = "FAMILY",
                deadline = "Before Friday reservation",
                appointmentDetails = "Grandpa's Birthday: Saturday at 6:30 PM (Bella Italia)",
                isReplyNeeded = true,
                recommendedAction = "Reply to Mom confirming whether Saturday 6:30 PM works.",
                confidenceScore = 0.92f,
                classificationReason = "Direct personal family engagement requiring schedule RSVP for restaurant booking.",
                isRead = false,
                isReplied = false,
                isMockData = true
            ),
            MessageItem(
                id = "msg-wa-05",
                sourceApp = "WhatsApp",
                sender = "Project Devs Group",
                senderAddress = "group.chat.892",
                timestamp = now - (12 * hour),
                subject = "PR #418 Deployment Complete",
                rawContent = "Deployment to staging passed all end-to-end regression tests. Service health is green. Docker container image tagged as v2.4.1.",
                summary = "Staging deployment successful with green test suite.",
                importanceScore = 52,
                urgency = "NORMAL",
                category = "WORK",
                deadline = null,
                appointmentDetails = null,
                isReplyNeeded = false,
                recommendedAction = "Archive or note staging build version for release notes.",
                confidenceScore = 0.88f,
                classificationReason = "Informational CI/CD update without blockers or questions.",
                isRead = true,
                isReplied = false,
                isMockData = true
            ),
            MessageItem(
                id = "msg-meta-06",
                sourceApp = "Instagram",
                sender = "Elena Rostova (@elena_design)",
                senderAddress = "@elena_design",
                timestamp = now - (18 * hour),
                subject = "Collaboration on Mobile Design System",
                rawContent = "Loved your recent article on cybernetic UI design! Would love to invite you to speak at our virtual design panel next month. Are you accepting guest speaker slots right now?",
                summary = "Invitation to speak as guest speaker at virtual design panel next month.",
                importanceScore = 68,
                urgency = "NORMAL",
                category = "OPPORTUNITY",
                deadline = null,
                appointmentDetails = "Virtual Design Panel: Next month",
                isReplyNeeded = true,
                recommendedAction = "Draft polite reply thanking Elena and requesting event agenda and dates.",
                confidenceScore = 0.91f,
                classificationReason = "Professional networking inquiry with potential speaking opportunity.",
                isRead = false,
                isReplied = false,
                isMockData = true
            ),
            MessageItem(
                id = "msg-promo-07",
                sourceApp = "Gmail",
                sender = "CloudStack Newsletter <deals@cloudstack.com>",
                senderAddress = "deals@cloudstack.com",
                timestamp = now - (1 * day),
                subject = "Flash Sale: 40% Off Cloud Database Tiers",
                rawContent = "Upgrade your cloud instances this week and save 40% on annual commitments. Check out our new serverless Postgres tier.",
                summary = "Promotional discount for annual cloud database tiers.",
                importanceScore = 18,
                urgency = "LOW",
                category = "PROMOTION",
                deadline = null,
                appointmentDetails = null,
                isReplyNeeded = false,
                recommendedAction = "No action required.",
                confidenceScore = 0.99f,
                classificationReason = "Mass commercial marketing blast identified via standard promotional markers.",
                isRead = true,
                isReplied = false,
                isMockData = true
            )
        )
    }

    fun generateInitialTasks(): List<TaskItem> {
        val now = System.currentTimeMillis()
        val day = 86400_000L

        return listOf(
            TaskItem(
                id = "task-01",
                title = "Upload Scholarship Transcripts to University Portal",
                description = "Required to secure fellowship. Deadline: Tomorrow 5:00 PM EST.",
                sourceMessageId = "msg-univ-01",
                sourceApp = "Gmail",
                dueDate = "Tomorrow, 5:00 PM EST",
                urgency = "CRITICAL",
                category = "EDUCATION",
                isCompleted = false,
                createdAt = now
            ),
            TaskItem(
                id = "task-02",
                title = "Lock or Verify Suspicious Paris Login Attempt",
                description = "Check Apex Online Banking security portal and verify active sessions.",
                sourceMessageId = "msg-bank-02",
                sourceApp = "Gmail",
                dueDate = "Today (Immediate)",
                urgency = "CRITICAL",
                category = "SECURITY",
                isCompleted = false,
                createdAt = now
            ),
            TaskItem(
                id = "task-03",
                title = "Confirm Performance Benchmark Slides with Sarah",
                description = "Prepare benchmark slides for Wednesday evening ahead of Thursday 2 PM meeting.",
                sourceMessageId = "msg-work-03",
                sourceApp = "Gmail",
                dueDate = "Wednesday, 6:00 PM",
                urgency = "HIGH",
                category = "WORK",
                isCompleted = false,
                createdAt = now
            ),
            TaskItem(
                id = "task-04",
                title = "RSVP to Mom for Grandpa's Birthday Dinner",
                description = "Confirm Saturday 6:30 PM table at Bella Italia.",
                sourceMessageId = "msg-wa-04",
                sourceApp = "WhatsApp",
                dueDate = "Friday Morning",
                urgency = "HIGH",
                category = "FAMILY",
                isCompleted = false,
                createdAt = now
            )
        )
    }

    fun generateInitialMemories(): List<MemoryItem> {
        return listOf(
            MemoryItem(
                id = "mem-01",
                keyFact = "Grandpa's birthday dinner is scheduled for this Saturday at 6:30 PM at Bella Italia.",
                contextDetails = "Extracted from Mom's WhatsApp message.",
                category = "FAMILY",
                sourceApp = "WhatsApp",
                sourceSender = "Mom",
                originalMessageId = "msg-wa-04",
                isConfirmed = true,
                confidence = 0.98f
            ),
            MemoryItem(
                id = "mem-02",
                keyFact = "Director Sarah Chen expects performance benchmark slides before Wednesday evening.",
                contextDetails = "Extracted from TechCorp Q4 Architecture review email.",
                category = "WORK",
                sourceApp = "Gmail",
                sourceSender = "Sarah Chen",
                originalMessageId = "msg-work-03",
                isConfirmed = true,
                confidence = 0.95f
            ),
            MemoryItem(
                id = "mem-03",
                keyFact = "University Fellowship document verification deadline is strictly October 20th at 5:00 PM EST.",
                contextDetails = "Extracted from University Admissions email.",
                category = "EDUCATION",
                sourceApp = "Gmail",
                sourceSender = "Admissions Office",
                originalMessageId = "msg-univ-01",
                isConfirmed = true,
                confidence = 0.99f
            )
        )
    }

    fun generateInitialBriefing(): BriefingItem {
        return BriefingItem(
            id = "briefing-morning-01",
            type = "MORNING",
            title = "JARVIS Morning Intelligence Briefing",
            overview = "Good morning. You have 2 critical items requiring immediate action, 4 active tasks, and 3 messages awaiting your reply.",
            keyHighlights = "• PRIORITY 1: University fellowship transcript deadline expires tomorrow at 5:00 PM EST.\n• SECURITY: Unauthorized sign-in attempt detected on your Apex Bank account from Paris.\n• WORK: Sarah Chen requested confirmation for Wednesday's performance benchmark slides.\n• FAMILY: Mom is waiting for your RSVP for Saturday's 6:30 PM birthday dinner.",
            criticalCount = 2,
            pendingTasksCount = 4,
            unansweredCount = 3,
            isRead = false
        )
    }
}
