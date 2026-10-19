package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.core.content.ContextCompat
import com.example.model.AssistantState
import com.example.model.SupportedLanguage
import com.example.model.VoiceDialogue
import com.example.ui.JarvisViewModel
import com.example.ui.components.JarvisArcReactorOrb
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.*

@Composable
fun VoiceCommandCenterScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assistantState by viewModel.assistantState.collectAsState()
    val rmsLevel by viewModel.speechManager.rmsLevel.collectAsState()
    val dialogues by viewModel.dialogues.collectAsState()
    val statusBanner by viewModel.statusBannerText.collectAsState()
    val currentLanguage by viewModel.selectedLanguage.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening()
        }
    }

    LaunchedEffect(dialogues.size) {
        if (dialogues.isNotEmpty()) {
            listState.animateScrollToItem(dialogues.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // System Telemetry Bar & Language Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(JarvisSurfaceVariant.copy(alpha = 0.7f))
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (assistantState) {
                                    AssistantState.LISTENING -> JarvisNeonTeal
                                    AssistantState.THINKING -> JarvisPurpleGlow
                                    AssistantState.SPEAKING -> JarvisHighAmber
                                    AssistantState.IDLE -> JarvisNormalGreen
                                }
                            )
                    )
                    Text(
                        text = statusBanner,
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentLanguage.displayName,
                            color = JarvisCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (assistantState == AssistantState.SPEAKING) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeOff,
                                contentDescription = "Interrupt speaking",
                                tint = JarvisHighAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Glowing Siri / Arc Reactor Voice Assistant Orb
        Box(
            modifier = Modifier.padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            JarvisArcReactorOrb(
                state = assistantState,
                rmsLevel = rmsLevel,
                size = 175.dp,
                onClick = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPerm) {
                        viewModel.toggleVoiceListening()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }

        // Voice state text & waveform visualizer
        Text(
            text = when (assistantState) {
                AssistantState.LISTENING -> "Listening to your voice..."
                AssistantState.THINKING -> "Thinking..."
                AssistantState.SPEAKING -> "Speaking (Tap orb or icon to interrupt)"
                AssistantState.IDLE -> "Tap Orb or Mic to Talk"
            },
            color = when (assistantState) {
                AssistantState.LISTENING -> JarvisNeonTeal
                AssistantState.THINKING -> JarvisPurpleGlow
                AssistantState.SPEAKING -> JarvisCyan
                AssistantState.IDLE -> JarvisTextSecondary
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        VoiceWaveformVisualizer(
            isActive = assistantState == AssistantState.LISTENING || assistantState == AssistantState.SPEAKING,
            activeColor = if (assistantState == AssistantState.LISTENING) JarvisNeonTeal else JarvisCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Command Suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chips = listOf(
                "Summarize Inbox" to "Summarize today's important messages",
                "Urgent Needs" to "What requires my attention right now?",
                "Habari (Swahili)" to "Habari JARVIS, nisaidie na kazi zangu",
                "Deadlines" to "What deadlines are coming up this week?"
            )
            chips.forEach { (label, query) ->
                SuggestionChip(
                    onClick = { viewModel.processUserInput(query) },
                    label = { Text(label, fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = JarvisCardBg,
                        labelColor = JarvisCyan
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = JarvisCardBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dialogue Transcripts List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(JarvisCardBg)
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(14.dp))
                .padding(10.dp)
        ) {
            if (dialogues.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Talk to JARVIS or tap any command chip above.",
                        color = JarvisTextTertiary,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dialogues) { dialogue ->
                        DialogueBubble(dialogue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input bar with text and microphone button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask JARVIS or give a command...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_center_text_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary
                ),
                shape = RoundedCornerShape(24.dp)
            )

            // Send Button if typed
            if (textInput.isNotBlank()) {
                IconButton(
                    onClick = {
                        viewModel.processUserInput(textInput)
                        textInput = ""
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan)
                        .testTag("send_query_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send text query",
                        tint = JarvisBackground
                    )
                }
            } else {
                // Mic button for push-to-talk
                IconButton(
                    onClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            viewModel.toggleVoiceListening()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (assistantState == AssistantState.LISTENING) JarvisNeonTeal else JarvisSurfaceVariant
                        )
                        .border(1.dp, JarvisCyan.copy(alpha = 0.5f), CircleShape)
                        .testTag("voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (assistantState == AssistantState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Push to talk",
                        tint = if (assistantState == AssistantState.LISTENING) JarvisBackground else JarvisCyan
                    )
                }
            }
        }
    }
}

@Composable
fun DialogueBubble(dialogue: VoiceDialogue) {
    val isUser = dialogue.speaker == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isUser) "YOU" else "JARVIS",
                color = if (isUser) JarvisElectricBlue else JarvisCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .background(if (isUser) JarvisDeepBlue.copy(alpha = 0.5f) else JarvisSurfaceVariant)
                .border(
                    1.dp,
                    if (isUser) JarvisElectricBlue.copy(alpha = 0.4f) else JarvisCyan.copy(alpha = 0.25f),
                    RoundedCornerShape(14.dp)
                )
                .padding(10.dp)
        ) {
            Text(
                text = dialogue.text,
                color = JarvisTextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

