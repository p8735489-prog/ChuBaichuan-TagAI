package com.kuzulabz.waifutaggercn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A small, colorful, continuously wobbling blob — the same general idea as
 * the Play Store's loading indicator (a circle whose edge undulates and
 * slowly rotates), reimplemented from scratch here with a Canvas + a couple
 * of animated sine terms. Not a copy of anyone's asset or code.
 */
@Composable
fun MorphingBlobLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val wobble by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val lobes = 5
        // Amplitude breathes between two values via `wobble` so the blob
        // continuously swells and settles instead of holding a static shape.
        val amplitude = radius * (0.12f + 0.10f * wobble)

        val path = Path()
        val steps = 120
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val angleDeg = t * 360f + rotation
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val r = radius - amplitude + amplitude * cos(lobes * angleRad).toFloat()
            val x = center.x + r * cos(angleRad).toFloat()
            val y = center.y + r * sin(angleRad).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color = color)
    }
}
