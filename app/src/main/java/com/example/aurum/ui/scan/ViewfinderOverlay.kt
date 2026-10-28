package com.example.aurum.ui.scan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurum.ui.theme.AurumTheme

/**
 * Renders a high-contrast viewfinder overlay over the camera preview:
 * - Scrim overlay around the scanning square
 * - Aurum gold corner reticles
 * - Animated laser line indicator
 * - Dynamic instruction label below the scanning frame
 */
@Composable
fun ViewfinderOverlay(
    modifier: Modifier = Modifier,
    instructionText: String = "Align UPI QR code within the frame"
) {
    val accentColor = AurumTheme.colors.accent
    val scrimColor = Color.Black.copy(alpha = 0.65f)

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    val density = LocalDensity.current
    val boxSizeDp = 260.dp
    val cornerLengthDp = 28.dp
    val strokeWidthDp = 4.dp

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val boxSize = density.run { boxSizeDp.toPx() }
            val cornerLength = density.run { cornerLengthDp.toPx() }
            val strokeWidth = density.run { strokeWidthDp.toPx() }

            val left = (width - boxSize) / 2f
            val top = (height - boxSize) / 2f
            val right = left + boxSize
            val bottom = top + boxSize

            // 1. Draw 4 outer scrim rectangles around the cutout
            drawRect(color = scrimColor, topLeft = Offset(0f, 0f), size = Size(width, top))
            drawRect(color = scrimColor, topLeft = Offset(0f, bottom), size = Size(width, height - bottom))
            drawRect(color = scrimColor, topLeft = Offset(0f, top), size = Size(left, boxSize))
            drawRect(color = scrimColor, topLeft = Offset(right, top), size = Size(width - right, boxSize))

            // 2. Draw 4 corner reticles in Aurum Gold
            // Top-Left
            drawLine(
                color = accentColor,
                start = Offset(left, top),
                end = Offset(left + cornerLength, top),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor,
                start = Offset(left, top),
                end = Offset(left, top + cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Top-Right
            drawLine(
                color = accentColor,
                start = Offset(right, top),
                end = Offset(right - cornerLength, top),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor,
                start = Offset(right, top),
                end = Offset(right, top + cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Left
            drawLine(
                color = accentColor,
                start = Offset(left, bottom),
                end = Offset(left + cornerLength, bottom),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor,
                start = Offset(left, bottom),
                end = Offset(left, bottom - cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Right
            drawLine(
                color = accentColor,
                start = Offset(right, bottom),
                end = Offset(right - cornerLength, bottom),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor,
                start = Offset(right, bottom),
                end = Offset(right, bottom - cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // 3. Draw animated laser scanning beam inside the frame
            val laserY = top + (boxSize * laserProgress)
            val laserBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.85f),
                    Color.Transparent
                ),
                startX = left,
                endX = right
            )
            drawLine(
                brush = laserBrush,
                start = Offset(left + 8f, laserY),
                end = Offset(right - 8f, laserY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // Instructional label situated beneath the scanning window
        Text(
            text = instructionText,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 164.dp)
                .padding(horizontal = 24.dp)
        )
    }
}

