package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SuggestedReply
import com.example.ui.JarvisViewModel
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.*

@Composable
fun MessageAssistantScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.allMessages.collectAsState()
    val activeAnalysis by viewModel.activeAssistantAnalysis.collectAsState()
    val preferredStyle by viewModel.preferredWritingStyle.collectAsState()

    var inputSender by remember { mutableStateOf("") }
    var inputContent by remember { mutableStateOf("") }
    var editedReply by remember { mutableStateOf("") }
    var selectedReplyStyle by remember { mutableStateOf("") }

    // When new analysis arrives, default the editedReply to preferred or first reply
    LaunchedEffect(activeAnalysis) {
        val replies = activeAnalysis?.suggestedReplies
        if (!replies.isNullOrEmpty()) {
            val matched = replies.find { it.style.contains(preferredStyle, ignoreCase = true) } ?: replies.first()
            selectedReplyStyle = matched.style
            editedReply = matched.text
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Screen Header
        Column {
            Text(
                text = "MESSAGE INTELLIGENCE",
                color = JarvisCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Smart Reply & Intent Assistant",
                color = JarvisTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Quick Picker from recent messages
            if (messages.isNotEmpty()) {
                item {
                    Text(
                        text = "Or pick from recent inbox:",
                        color = JarvisTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(messages.take(6)) { msg ->
                            FilterChip(
                                selected = inputContent == msg.rawContent,
                                onClick = {
                                    inputSender = msg.sender.substringBefore("<").trim()
                                    inputContent = msg.rawContent
                                    viewModel.analyzeMessageInAssistant(inputSender, inputContent)
                                },
                                label = {
                                    Text(
                                        text = "${msg.sourceApp} • ${msg.sender.substringBefore("<").trim()}",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JarvisSurfaceVariant,
                                    selectedLabelColor = JarvisCyan,
                                    containerColor = JarvisCardBg,
                                    labelColor = JarvisTextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Input Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, JarvisCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Paste / Enter Message to Decode",
                                color = JarvisTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputSender,
                            onValueChange = { inputSender = it },
                            label = { Text("Sender / Contact", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Boss, Mom, or Admissions") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("message_assistant_sender_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputContent,
                            onValueChange = { inputContent = it },
                            label = { Text("Message Body", fontSize = 12.sp) },
                            placeholder = { Text("Paste incoming text or email body here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("message_assistant_content_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (inputContent.isNotBlank()) {
                                    viewModel.analyzeMessageInAssistant(
                                        inputSender.ifBlank { "Sender" },
                                        inputContent
                                    )
                                }
                            },
                            enabled = inputContent.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("decode_message_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = JarvisBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Tone & Draft Replies", color = JarvisBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Analysis & Explanation Section
            activeAnalysis?.let { analysis ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Tone: ${analysis.tone}",
                                        color = JarvisCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                UrgencyBadge(urgency = analysis.urgency)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "What this message means:",
                                color = JarvisTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = analysis.explanation,
                                color = JarvisTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Likely Intent:",
                                color = JarvisTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = analysis.likelyIntent,
                                color = JarvisTextTertiary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            if (analysis.hasDeadline && analysis.deadlineNote != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = JarvisHighAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Deadline detected: ${analysis.deadlineNote}",
                                        color = JarvisHighAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // 4 Suggested Reply Variations
                item {
                    Text(
                        text = "Suggested Replies (Select Style):",
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(analysis.suggestedReplies) { reply ->
                            val isSelected = selectedReplyStyle == reply.style
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedReplyStyle = reply.style
                                    editedReply = reply.text
                                },
                                label = { Text(reply.style, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JarvisCyan,
                                    selectedLabelColor = JarvisBackground,
                                    containerColor = JarvisCardBg,
                                    labelColor = JarvisTextSecondary
                                )
                            )
                        }
                    }
                }

                // Editable Draft Box & Action Buttons
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, JarvisCardBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Edit Response ($selectedReplyStyle)",
                                    color = JarvisTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(
                                    onClick = {
                                        viewModel.setWritingStyle(selectedReplyStyle)
                                        Toast.makeText(context, "Saved as preferred writing style", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = JarvisGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Set Preferred", color = JarvisGold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editedReply,
                                onValueChange = { editedReply = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .testTag("editable_reply_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisCardBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // One-Tap Delivery Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.copyToClipboard(editedReply)
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", color = JarvisCyan, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.openWhatsApp(presetText = editedReply)
                                    },
                                    modifier = Modifier.weight(1.1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = JarvisNormalGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", color = JarvisNormalGreen, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.openGmail(subject = "Re: ${inputSender}", body = editedReply)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant)
                                ) {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = JarvisHighAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gmail", color = JarvisHighAmber, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
