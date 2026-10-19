package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.SupportedLanguage
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentLanguage by viewModel.selectedLanguage.collectAsState()
    val customKey by viewModel.customApiKey.collectAsState()
    val emergencyNum by viewModel.emergencyNumber.collectAsState()
    val isMemoryEnabled by viewModel.isMemoryEnabled.collectAsState()
    val continuousWake by viewModel.continuousWakeWord.collectAsState()

    var pitch by remember { mutableFloatStateOf(viewModel.speechManager.speechPitch) }
    var speed by remember { mutableFloatStateOf(viewModel.speechManager.speechRate) }
    var isMuted by remember { mutableStateOf(viewModel.speechManager.isVoiceMuted) }

    var apiKeyInput by remember(customKey) { mutableStateOf(customKey) }
    var emergencyInput by remember(emergencyNum) { mutableStateOf(emergencyNum) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Column {
            Text(
                text = "PERMISSIONS & CONFIGURATION",
                color = JarvisCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Voice, Language & Privacy",
                color = JarvisTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Multilingual Support
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = JarvisCyan)
                            Text("Language & Multilingual Speech", color = JarvisTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Select your preferred conversation language:",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        SupportedLanguage.entries.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(lang.displayName, color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    if (lang == SupportedLanguage.KIKAMBA) {
                                        Text("Speech input depends on device; text understanding supported", color = JarvisTextTertiary, fontSize = 10.sp)
                                    }
                                }

                                RadioButton(
                                    selected = currentLanguage == lang,
                                    onClick = { viewModel.setLanguage(lang) },
                                    colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Android Permissions Dashboard
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = JarvisCyan)
                            Text("Android Permissions Dashboard", color = JarvisTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Control what device capabilities JARVIS is permitted to access. No actions are run silently.",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Microphone Permission
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Microphone Access", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Required for push-to-talk speech recognition", color = JarvisTextTertiary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = hasAudioPermission,
                                onCheckedChange = {
                                    if (!hasAudioPermission) {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = JarvisCyan)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = JarvisTextTertiary, modifier = Modifier.size(14.dp))
                            Text("Voice listening runs in foreground. Android disables microphone access when the app is closed to protect privacy and save battery.", color = JarvisTextTertiary, fontSize = 10.sp, lineHeight = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Notification Listener Access
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Notification Ingestion", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Allow JARVIS to read notifications from authorized apps", color = JarvisTextTertiary, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    } catch (e: Exception) {
                                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Configure", color = JarvisCyan, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 3. AI Service Configuration & API Key
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = JarvisHighAmber)
                            Text("Gemini 2.5 Flash Engine", color = JarvisTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "JARVIS uses Google Gemini for natural, non-repetitive conversation. Key is automatically injected from AI Studio Secrets or can be set below:",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Custom Gemini API Key (Optional)", fontSize = 12.sp) },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_api_key_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.setCustomApiKey(apiKeyInput)
                                Toast.makeText(context, "API Key saved", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant)
                        ) {
                            Text("Save API Key", color = JarvisCyan)
                        }
                    }
                }
            }

            // 4. Voice Synthesis & Speed
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = JarvisCyan)
                            Text("Voice Output Calibration", color = JarvisTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Speech Feedback", color = JarvisTextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = !isMuted,
                                onCheckedChange = {
                                    isMuted = !it
                                    viewModel.speechManager.isVoiceMuted = isMuted
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = JarvisCyan)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Speech Rate (${"%.2f".format(speed)})", color = JarvisTextSecondary, fontSize = 12.sp)
                        Slider(
                            value = speed,
                            onValueChange = {
                                speed = it
                                viewModel.speechManager.speechRate = it
                            },
                            valueRange = 0.8f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val testPhrase = if (currentLanguage == SupportedLanguage.SWAHILI) {
                                    "Mifumo yote iko tayari. Mimi ni JARVIS, msaidizi wako wa kibinafsi."
                                } else {
                                    "All systems nominal. Voice calibration test complete."
                                }
                                viewModel.speechManager.speak(testPhrase)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = JarvisCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Voice Synthesis", color = JarvisCyan)
                        }
                    }
                }
            }

            // 5. Emergency Contact & Privacy Controls
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = null, tint = JarvisCriticalRed)
                            Text("Emergency Contact Shortcut", color = JarvisTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = emergencyInput,
                            onValueChange = {
                                emergencyInput = it
                                viewModel.setEmergencyNumber(it)
                            },
                            label = { Text("Emergency Phone Number") },
                            placeholder = { Text("112, 911, or contact number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Personal AI Memory", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Remember preferences and routine facts", color = JarvisTextTertiary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isMemoryEnabled,
                                onCheckedChange = { viewModel.setMemoryEnabled(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = JarvisCyan)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.clearDialogueHistory() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = JarvisTextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Chat Dialogue History", color = JarvisTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
