package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageItem
import com.example.ui.JarvisViewModel
import com.example.ui.components.AppSourceIcon
import com.example.ui.components.ImportanceMeter
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.*

@Composable
fun PriorityInboxScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.allMessages.collectAsState()
    var selectedUrgency by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMessages = remember(messages, selectedUrgency, searchQuery) {
        messages.filter { msg ->
            val matchUrgency = if (selectedUrgency == "ALL") true else msg.urgency.equals(selectedUrgency, ignoreCase = true)
            val matchQuery = if (searchQuery.isBlank()) true else {
                msg.sender.contains(searchQuery, ignoreCase = true) ||
                msg.subject.contains(searchQuery, ignoreCase = true) ||
                msg.summary.contains(searchQuery, ignoreCase = true) ||
                msg.sourceApp.contains(searchQuery, ignoreCase = true)
            }
            matchUrgency && matchQuery && !msg.isArchived
        }
    }

    val criticalCount = remember(messages) { messages.count { it.urgency == "CRITICAL" && !it.isArchived } }
    val unansweredCount = remember(messages) { messages.count { it.isReplyNeeded && !it.isReplied && !it.isArchived } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAnalyzeDialog() },
                containerColor = JarvisCyan,
                contentColor = JarvisBackground,
                modifier = Modifier.testTag("analyze_message_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "Analyze New Message"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRIORITY INBOX",
                        color = JarvisCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Intelligent Communications",
                        color = JarvisTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.openAnalyzeDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = "Import Message",
                        tint = JarvisCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Critical Alerts",
                    value = "$criticalCount",
                    color = JarvisCriticalRed,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending Replies",
                    value = "$unansweredCount",
                    color = JarvisHighAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Indexed",
                    value = "${messages.size}",
                    color = JarvisCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search sender, subject, or keywords...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = JarvisTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = JarvisTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inbox_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("ALL", "CRITICAL", "HIGH", "NORMAL", "LOW")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedUrgency == filter,
                        onClick = { selectedUrgency = filter },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JarvisSurfaceVariant,
                            selectedLabelColor = JarvisCyan,
                            containerColor = JarvisCardBg,
                            labelColor = JarvisTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedUrgency == filter,
                            borderColor = if (selectedUrgency == filter) JarvisCyan else JarvisCardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Messages List
            if (filteredMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No messages match your selected filters.",
                        color = JarvisTextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredMessages, key = { it.id }) { message ->
                        MessageCard(
                            message = message,
                            onClick = { viewModel.openMessageDetail(message) },
                            onDraftReply = { viewModel.startDraftReply(message) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisCardBg)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MessageCard(
    message: MessageItem,
    onClick: () -> Unit,
    onDraftReply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (message.urgency == "CRITICAL") JarvisCriticalRed.copy(alpha = 0.5f) else JarvisCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag("message_card_${message.id}"),
        colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
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
                            text = message.sender.substringBefore("<").trim(),
                            color = JarvisTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = message.sourceApp,
                            color = JarvisCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ImportanceMeter(score = message.importanceScore)
                    UrgencyBadge(urgency = message.urgency)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subject
            Text(
                text = message.subject,
                color = JarvisTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Summary
            Text(
                text = message.summary,
                color = JarvisTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Deadline / Event Badge
            if (message.deadline != null) {
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
                        text = "Deadline: ${message.deadline}",
                        color = JarvisHighAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (message.isMockData) {
                    Text(
                        text = "• DEMO FEED",
                        color = JarvisTextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Text(
                        text = "• LIVE NOTIFICATION",
                        color = JarvisNeonTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (message.isReplyNeeded && !message.isReplied) {
                        TextButton(
                            onClick = onDraftReply,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Draft Reply", color = JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = onClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Details", color = JarvisTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
