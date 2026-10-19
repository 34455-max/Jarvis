package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.MemoryItem
import com.example.model.MessageItem
import com.example.model.TaskItem
import com.example.ui.JarvisViewModel
import com.example.ui.components.AppSourceIcon
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.*

@Composable
fun MemoryTimelineScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tasks by viewModel.allTasks.collectAsState()
    val importantNowTasks by viewModel.importantNowTasks.collectAsState()
    val memories by viewModel.allMemories.collectAsState()
    val messages by viewModel.allMessages.collectAsState()
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val latestBriefing by viewModel.latestBriefing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }
    var memoryToDelete by remember { mutableStateOf<MemoryItem?>(null) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, searchQuery) {
        if (searchQuery.isBlank()) tasks else tasks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val unansweredMessages = remember(messages) {
        messages.filter { it.isReplyNeeded && !it.isReplied && !it.isArchived }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Screen Header with Emergency Shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TASKS, MEMORY & PRIORITIES",
                    color = JarvisCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Daily Action Plan",
                    color = JarvisTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Emergency Assistance Button
                IconButton(
                    onClick = { showEmergencyDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(JarvisCriticalRed.copy(alpha = 0.2f))
                        .border(1.dp, JarvisCriticalRed, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergency Assistance",
                        tint = JarvisCriticalRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add Task Button
                IconButton(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddTask,
                        contentDescription = "Add Task",
                        tint = JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = JarvisCardBg,
            contentColor = JarvisCyan,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Tasks (${tasks.count { !it.isCompleted }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Memory (${memories.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Unanswered (${unansweredMessages.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                // Tasks Tab with "Important Now" Section
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // "Important Now" Section
                    if (importantNowTasks.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, JarvisCriticalRed.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PriorityHigh,
                                            contentDescription = null,
                                            tint = JarvisCriticalRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "IMPORTANT NOW • URGENT ATTENTION",
                                            color = JarvisCriticalRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    importantNowTasks.take(3).forEach { task ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• ${task.title}",
                                                color = JarvisTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = task.dueDate ?: "Immediate",
                                                color = JarvisHighAmber,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search box
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search tasks and reminders...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = JarvisTextSecondary, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisCardBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    if (filteredTasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tasks found. Tap + to add a commitment.", color = JarvisTextSecondary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskRowCard(
                                task = task,
                                onToggleComplete = { viewModel.toggleTaskComplete(task) },
                                onToggleUrgentNow = { viewModel.toggleTaskUrgentNow(task) },
                                onDelete = { taskToDelete = task }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Memory Vault Tab
                MemoryVaultTabEnhanced(
                    memories = memories,
                    onDelete = { memoryToDelete = it },
                    onClearAll = { viewModel.clearAllMemories() }
                )
            }

            2 -> {
                // Unanswered Messages Tab
                UnansweredTab(
                    unanswered = unansweredMessages,
                    onDraftReply = { viewModel.startDraftReply(it) },
                    onMarkReplied = { viewModel.markMessageReplied(it) }
                )
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialogEnhanced(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, due, urgency, isUrgentNow ->
                viewModel.addTask(title, due, urgency, isUrgentNow)
                showAddTaskDialog = false
            }
        )
    }

    // Delete Task Confirmation
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Commitment?", color = JarvisTextPrimary) },
            text = { Text("Are you sure you want to remove \"${task.title}\"?", color = JarvisTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(task)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCriticalRed)
                ) {
                    Text("Delete", color = JarvisTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            },
            containerColor = JarvisSurface
        )
    }

    // Delete Memory Confirmation
    memoryToDelete?.let { memory ->
        AlertDialog(
            onDismissRequest = { memoryToDelete = null },
            title = { Text("Remove Stored Memory?", color = JarvisTextPrimary) },
            text = { Text("Are you sure you want JARVIS to forget this fact?", color = JarvisTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMemory(memory)
                        memoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCriticalRed)
                ) {
                    Text("Forget Fact", color = JarvisTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { memoryToDelete = null }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            },
            containerColor = JarvisSurface
        )
    }

    // Emergency Assistance Dialog
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Emergency, contentDescription = null, tint = JarvisCriticalRed)
                    Text("Emergency Contact Shortcut", color = JarvisCriticalRed, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "JARVIS will open the Android phone dialer with emergency number ($emergencyNumber).",
                        color = JarvisTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Note: For safety and privacy, JARVIS does not place silent calls or dispatch emergency services automatically. You must confirm the call in your device dialer.",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.openDialer(emergencyNumber)
                        showEmergencyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCriticalRed)
                ) {
                    Text("Open Dialer ($emergencyNumber)", color = JarvisTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            },
            containerColor = JarvisSurface
        )
    }
}

@Composable
fun TaskRowCard(
    task: TaskItem,
    onToggleComplete: () -> Unit,
    onToggleUrgentNow: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (task.isUrgentNow) JarvisCriticalRed.copy(alpha = 0.5f) else JarvisCardBorder,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = JarvisCyan,
                    uncheckedColor = JarvisTextSecondary,
                    checkmarkColor = JarvisBackground
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) JarvisTextTertiary else JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = JarvisHighAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Due: ${task.dueDate}",
                            color = JarvisHighAmber,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleUrgentNow,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (task.isUrgentNow) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Mark Important Now",
                        tint = if (task.isUrgentNow) JarvisCriticalRed else JarvisTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = JarvisTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryVaultTabEnhanced(
    memories: List<MemoryItem>,
    onDelete: (MemoryItem) -> Unit,
    onClearAll: () -> Unit
) {
    Column {
        if (memories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stored Preferences & Context (${memories.size})",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = onClearAll,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Clear All", color = JarvisCriticalRed, fontSize = 11.sp)
                }
            }
        }

        if (memories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Memory vault is empty. Preferences and learned facts appear here.", color = JarvisTextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(memories, key = { it.id }) { memory ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = memory.category,
                                    color = JarvisCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { onDelete(memory) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Forget fact",
                                        tint = JarvisTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = memory.keyFact,
                                color = JarvisTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (memory.contextDetails.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = memory.contextDetails,
                                    color = JarvisTextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialogEnhanced(
    onDismiss: () -> Unit,
    onAddTask: (title: String, due: String, urgency: String, isUrgentNow: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("NORMAL") }
    var isUrgentNow by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Commitment / Reminder", color = JarvisTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. Study for finals or Call landlord") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = due,
                    onValueChange = { due = it },
                    label = { Text("Due Date / Time") },
                    placeholder = { Text("e.g. Today 7 PM or Tomorrow") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin to 'Important Now'", color = JarvisTextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = isUrgentNow,
                        onCheckedChange = { isUrgentNow = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = JarvisCriticalRed)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, due, urgency, isUrgentNow)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
            ) {
                Text("Schedule", color = JarvisBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JarvisTextSecondary)
            }
        },
        containerColor = JarvisSurface
    )
}

@Composable
fun UnansweredTab(
    unanswered: List<MessageItem>,
    onDraftReply: (MessageItem) -> Unit,
    onMarkReplied: (String) -> Unit
) {
    if (unanswered.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("All messages requiring a response have been handled!", color = JarvisNormalGreen, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(unanswered, key = { it.id }) { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
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
                                AppSourceIcon(sourceApp = msg.sourceApp)
                                Text(
                                    text = msg.sender.substringBefore("<").trim(),
                                    color = JarvisTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            UrgencyBadge(urgency = msg.urgency)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "\"${msg.subject}\"",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Next: ${msg.recommendedAction}",
                            color = JarvisGold,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onMarkReplied(msg.id) },
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text("Mark Handled", color = JarvisTextSecondary, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onDraftReply(msg) },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                            ) {
                                Text("Draft Reply", color = JarvisBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

