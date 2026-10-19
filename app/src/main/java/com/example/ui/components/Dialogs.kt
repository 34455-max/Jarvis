package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MessageItem
import com.example.ui.theme.*

@Composable
fun MessageDetailDialog(
    message: MessageItem,
    onDismiss: () -> Unit,
    onDraftReply: (MessageItem) -> Unit,
    onMarkReplied: (String) -> Unit,
    onArchive: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(18.dp)),
            color = JarvisSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppSourceIcon(sourceApp = message.sourceApp)
                        Column {
                            Text(
                                text = message.sourceApp,
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = message.sender.substringBefore("<").trim(),
                                color = JarvisTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    UrgencyBadge(urgency = message.urgency)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject
                Text(
                    text = message.subject,
                    color = JarvisTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // AI Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(JarvisSurfaceVariant)
                        .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "JARVIS INTELLIGENCE SUMMARY",
                                color = JarvisCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message.summary,
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Raw Content
                Text(
                    text = "ORIGINAL MESSAGE CONTENT",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.rawContent,
                    color = JarvisTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Extracted Deadlines / Appointments
                if (message.deadline != null || message.appointmentDetails != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (message.deadline != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = JarvisHighAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Deadline: ${message.deadline}",
                                        color = JarvisHighAmber,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (message.appointmentDetails != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = message.appointmentDetails,
                                        color = JarvisCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // AI Evidence Reasoning
                Text(
                    text = "ANALYSIS & EVIDENCE REASONING",
                    color = JarvisTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${message.classificationReason} (Confidence: ${(message.confidenceScore * 100).toInt()}%)",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Recommended Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = JarvisGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Next: ${message.recommendedAction}",
                        color = JarvisGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (message.isReplyNeeded && !message.isReplied) {
                        Button(
                            onClick = {
                                onDraftReply(message)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("draft_reply_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null,
                                tint = JarvisBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Draft Reply with JARVIS", color = JarvisBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onArchive(message.id)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Archive", color = JarvisTextSecondary)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant)
                        ) {
                            Text("Close", color = JarvisTextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyzeMessageDialog(
    onDismiss: () -> Unit,
    onAnalyze: (sourceApp: String, sender: String, subject: String, content: String) -> Unit
) {
    var sourceApp by remember { mutableStateOf("Gmail") }
    var sender by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val apps = listOf("Gmail", "WhatsApp", "Instagram", "Messenger", "Slack")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(18.dp)),
            color = JarvisSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = JarvisCyan
                    )
                    Text(
                        text = "Analyze New Message",
                        color = JarvisTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Source Platform",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    apps.forEach { app ->
                        FilterChip(
                            selected = sourceApp == app,
                            onClick = { sourceApp = app },
                            label = { Text(app, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("Sender (Name / Email / Phone)") },
                    placeholder = { Text("e.g. Professor Smith or Admissions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analyze_sender_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject / Topic") },
                    placeholder = { Text("e.g. Final Project Due Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analyze_subject_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Raw Message Body") },
                    placeholder = { Text("Paste email text or WhatsApp chat here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("analyze_content_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = JarvisTextSecondary)
                    }

                    Button(
                        onClick = {
                            if (content.isNotBlank() || subject.isNotBlank()) {
                                onAnalyze(
                                    sourceApp,
                                    sender.ifBlank { "Unknown Sender" },
                                    subject.ifBlank { "Untitled Communication" },
                                    content.ifBlank { subject }
                                )
                                onDismiss()
                            }
                        },
                        enabled = content.isNotBlank() || subject.isNotBlank(),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("submit_analyze_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Text("Process AI Analysis", color = JarvisBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DraftReplyDialog(
    initialDraft: String,
    recipient: String,
    onDismiss: () -> Unit,
    onConfirmSend: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialDraft) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(18.dp)),
            color = JarvisSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = JarvisCyan
                    )
                    Text(
                        text = "JARVIS Reply Assistant",
                        color = JarvisTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Prepared response for $recipient",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Response Draft") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .testTag("draft_reply_text_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisHighAmber.copy(alpha = 0.15f))
                        .border(1.dp, JarvisHighAmber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = JarvisHighAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Confirmation Gate: Messages will not be sent without your explicit approval.",
                            color = JarvisHighAmber,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = JarvisTextSecondary)
                    }

                    Button(
                        onClick = {
                            onConfirmSend(text)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_send_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Text("Confirm & Send", color = JarvisBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
