package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DraftReplyDialog
import com.example.ui.components.MessageDetailDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class JarvisNavTab(val title: String, val icon: ImageVector) {
    ASSISTANT("Assistant", Icons.Default.GraphicEq),
    MESSAGE_ASSISTANT("Messages", Icons.Default.Forum),
    TASKS("Tasks", Icons.Default.Checklist),
    INBOX("Inbox", Icons.Default.Inbox),
    SETTINGS("Settings", Icons.Default.Tune)
}

@Composable
fun MainScreen(viewModel: JarvisViewModel) {
    var currentTab by remember { mutableStateOf(JarvisNavTab.ASSISTANT) }

    val activeMessageDetail by viewModel.activeMessageDetail.collectAsState()
    val draftReplyTarget by viewModel.draftReplyTarget.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        bottomBar = {
            NavigationBar(
                containerColor = JarvisSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(1.dp, JarvisCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                tonalElevation = 8.dp
            ) {
                JarvisNavTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selected) JarvisCyan else JarvisTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) JarvisCyan else JarvisTextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = JarvisSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                JarvisNavTab.ASSISTANT -> VoiceCommandCenterScreen(viewModel = viewModel)
                JarvisNavTab.MESSAGE_ASSISTANT -> MessageAssistantScreen(viewModel = viewModel)
                JarvisNavTab.TASKS -> MemoryTimelineScreen(viewModel = viewModel)
                JarvisNavTab.INBOX -> PriorityInboxScreen(viewModel = viewModel)
                JarvisNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Active Dialog Overlays
    activeMessageDetail?.let { message ->
        MessageDetailDialog(
            message = message,
            onDismiss = { viewModel.closeMessageDetail() },
            onDraftReply = { viewModel.startDraftReply(it) },
            onMarkReplied = { viewModel.markMessageReplied(it) },
            onArchive = { viewModel.archiveMessage(it) }
        )
    }

    val showAnalyzeDialog by viewModel.showAnalyzeDialog.collectAsState()

    if (showAnalyzeDialog) {
        com.example.ui.components.AnalyzeMessageDialog(
            onDismiss = { viewModel.closeAnalyzeDialog() },
            onAnalyze = { sourceApp, sender, subject, content ->
                viewModel.analyzeAndSaveIncomingMessage(sourceApp, sender, subject, content)
            }
        )
    }

    draftReplyTarget?.let { (draft, recipient) ->
        DraftReplyDialog(
            initialDraft = draft,
            recipient = recipient,
            onDismiss = { viewModel.closeDraftReply() },
            onConfirmSend = { confirmedText ->
                viewModel.sendConfirmedReply(confirmedText)
            }
        )
    }
}
