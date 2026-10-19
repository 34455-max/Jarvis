package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AssistantState
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactorOrb(
    state: AssistantState,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_rotation")

    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantState.THINKING) 2500 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_ring"
    )

    val innerAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantState.THINKING) 2000 else 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_ring"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.LISTENING -> 800
                    AssistantState.SPEAKING -> 600
                    AssistantState.THINKING -> 500
                    else -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val dynamicScale = breathingScale + (rmsLevel * 0.25f)

    val primaryColor = when (state) {
        AssistantState.LISTENING -> JarvisNeonTeal
        AssistantState.THINKING -> JarvisPurpleGlow
        AssistantState.SPEAKING -> JarvisCyan
        AssistantState.IDLE -> JarvisCyan
    }

    val glowColor = when (state) {
        AssistantState.LISTENING -> JarvisHoloGlow.copy(alpha = 0.6f)
        AssistantState.THINKING -> JarvisPurpleGlow.copy(alpha = 0.6f)
        AssistantState.SPEAKING -> JarvisElectricBlue.copy(alpha = 0.7f)
        AssistantState.IDLE -> JarvisElectricBlue.copy(alpha = 0.4f)
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) * 0.88f

            // 1. Ambient Outer Radial Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = radius * 1.3f * dynamicScale
                ),
                radius = radius * 1.25f * dynamicScale,
                center = center
            )

            // 2. Outer Holographic Dashed Orbital Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = radius,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f), outerAngle)
                )
            )

            // 3. Segmented Arc Reactor Brackets
            val arcSegments = 4
            val arcSweep = 45f
            for (i in 0 until arcSegments) {
                val start = outerAngle + (i * 90f) + 20f
                drawArc(
                    color = primaryColor.copy(alpha = 0.85f),
                    startAngle = start,
                    sweepAngle = arcSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 3.5.dp.toPx())
                )
            }

            // 4. Middle Counter-Rotating Telemetry Ring
            val midRadius = radius * 0.76f
            drawCircle(
                color = JarvisElectricBlue.copy(alpha = 0.45f),
                radius = midRadius,
                center = center,
                style = Stroke(
                    width = 1.8.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), innerAngle)
                )
            )

            // Radial tick marks
            for (tick in 0 until 12) {
                val rad = Math.toRadians((tick * 30.0 + innerAngle).toDouble())
                val p1 = Offset(
                    center.x + (midRadius - 6f) * cos(rad).toFloat(),
                    center.y + (midRadius - 6f) * sin(rad).toFloat()
                )
                val p2 = Offset(
                    center.x + (midRadius + 6f) * cos(rad).toFloat(),
                    center.y + (midRadius + 6f) * sin(rad).toFloat()
                )
                drawLine(
                    color = primaryColor.copy(alpha = 0.7f),
                    start = p1,
                    end = p2,
                    strokeWidth = 2f
                )
            }

            // 5. Inner Core Orb with glowing radial gradient
            val coreRadius = radius * 0.52f * dynamicScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        JarvisDeepBlue,
                        JarvisBackground
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // Inner Core Arc Accent
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = coreRadius * 0.32f,
                center = center
            )
        }
    }
}
