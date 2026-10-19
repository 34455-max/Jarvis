package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VoiceWaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    activeColor: Color = JarvisCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = modifier.height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animHeight by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + ((i % 5) * 110),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val heightMultiplier = if (isActive) animHeight else 0.15f

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightMultiplier.coerceIn(0.12f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(activeColor, activeColor.copy(alpha = 0.3f))
                        )
                    )
            )
        }
    }
}

@Composable
fun UrgencyBadge(urgency: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label, icon) = when (urgency.uppercase()) {
        "CRITICAL" -> Quadruple(JarvisCriticalRed.copy(alpha = 0.2f), JarvisCriticalRed, "CRITICAL", Icons.Default.PriorityHigh)
        "HIGH" -> Quadruple(JarvisHighAmber.copy(alpha = 0.2f), JarvisHighAmber, "HIGH", Icons.Default.Warning)
        "NORMAL" -> Quadruple(JarvisNormalGreen.copy(alpha = 0.2f), JarvisNormalGreen, "NORMAL", Icons.Default.CheckCircle)
        else -> Quadruple(JarvisLowBlue.copy(alpha = 0.2f), JarvisLowBlue, "LOW", Icons.Default.Info)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ImportanceMeter(score: Int, modifier: Modifier = Modifier) {
    val color = when {
        score >= 85 -> JarvisCriticalRed
        score >= 70 -> JarvisHighAmber
        score >= 40 -> JarvisNormalGreen
        else -> JarvisLowBlue
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$score",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(JarvisSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (score / 100f).coerceIn(0.05f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun AppSourceIcon(sourceApp: String, modifier: Modifier = Modifier) {
    val (icon, tint) = when (sourceApp.lowercase()) {
        "gmail" -> Icons.Default.Email to Color(0xFFEA4335)
        "whatsapp" -> Icons.Default.Chat to Color(0xFF25D366)
        "instagram" -> Icons.Default.CameraAlt to Color(0xFFE1306C)
        "facebook", "messenger" -> Icons.Default.Forum to Color(0xFF0084FF)
        "slack" -> Icons.Default.Work to Color(0xFF4A154B)
        else -> Icons.Default.Message to JarvisCyan
    }

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f))
            .border(1.dp, tint.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = sourceApp,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
