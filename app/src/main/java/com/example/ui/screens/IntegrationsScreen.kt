package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.components.AppSourceIcon
import com.example.ui.theme.*

@Composable
fun IntegrationsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var gmailConnected by remember { mutableStateOf(true) }
    var whatsappListenerActive by remember { mutableStateOf(true) }
    var instagramConnected by remember { mutableStateOf(true) }

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
                text = "COMMUNICATION CHANNELS",
                color = JarvisCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Integrations & Access Control",
                color = JarvisTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Android Notification Listener Gateway
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(JarvisCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Android Notification Listener",
                                        color = JarvisTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Direct on-device notification parsing",
                                        color = JarvisTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = whatsappListenerActive,
                                onCheckedChange = { whatsappListenerActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = JarvisBackground,
                                    checkedTrackColor = JarvisCyan,
                                    uncheckedThumbColor = JarvisTextSecondary,
                                    uncheckedTrackColor = JarvisSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enables JARVIS to detect urgent incoming WhatsApp, Gmail, and Instagram messages as they arrive without scraping private data or bypassing encryption.",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to general settings
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_notification_access_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configure Android Notification Access", color = JarvisCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Gmail Channel
            item {
                IntegrationCard(
                    sourceApp = "Gmail",
                    title = "Google Workspace & Gmail",
                    description = "Official Google Gmail REST API & OAuth 2.0 with push notification webhooks.",
                    isConnected = gmailConnected,
                    statusText = if (gmailConnected) "Connected (Watch Active)" else "Disconnected",
                    onToggle = { gmailConnected = !gmailConnected }
                )
            }

            // WhatsApp Channel
            item {
                IntegrationCard(
                    sourceApp = "WhatsApp",
                    title = "WhatsApp Communications",
                    description = "Official Business API & Opt-in Android Notification Gateway for personal chat alerts.",
                    isConnected = whatsappListenerActive,
                    statusText = if (whatsappListenerActive) "Listener Monitored" else "Paused",
                    onToggle = { whatsappListenerActive = !whatsappListenerActive }
                )
            }

            // Instagram & Meta
            item {
                IntegrationCard(
                    sourceApp = "Instagram",
                    title = "Instagram & Meta Messenger",
                    description = "Official Graph API webhooks & supported push notification ingestion.",
                    isConnected = instagramConnected,
                    statusText = if (instagramConnected) "Connected" else "Disconnected",
                    onToggle = { instagramConnected = !instagramConnected }
                )
            }

            // Security & Privacy Verification Panel
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
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = JarvisNormalGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "CYBERSECURITY & ZERO-TRUST PROTOCOL",
                                color = JarvisNormalGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SecurityCheckItem("Least-Privilege Scopes", "Read-only access to message headers and summaries")
                        SecurityCheckItem("Zero Secret Leakage", "API keys secured via Secrets panel, never in logs")
                        SecurityCheckItem("Untrusted Input Sandbox", "Emails filtered against prompt injection exploits")
                        SecurityCheckItem("Human-in-the-Loop Gate", "No messages sent or deleted without your confirmation")
                    }
                }
            }
        }
    }
}

@Composable
fun IntegrationCard(
    sourceApp: String,
    title: String,
    description: String,
    isConnected: Boolean,
    statusText: String,
    onToggle: () -> Unit
) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppSourceIcon(sourceApp = sourceApp)
                    Column {
                        Text(
                            text = title,
                            color = JarvisTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) JarvisNormalGreen else JarvisTextTertiary)
                            )
                            Text(
                                text = statusText,
                                color = if (isConnected) JarvisNormalGreen else JarvisTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Switch(
                    checked = isConnected,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisBackground,
                        checkedTrackColor = JarvisCyan,
                        uncheckedThumbColor = JarvisTextSecondary,
                        uncheckedTrackColor = JarvisSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = JarvisTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SecurityCheckItem(title: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = JarvisNormalGreen,
            modifier = Modifier.size(14.dp)
        )
        Column {
            Text(
                text = title,
                color = JarvisTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                color = JarvisTextTertiary,
                fontSize = 11.sp
            )
        }
    }
}
