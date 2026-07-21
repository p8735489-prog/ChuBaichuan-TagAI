package com.kuzulabz.waifutaggercn.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * 轻量加载动画。旧版每帧都要重建 120 个点的变形路径，
 * 在低端设备或 ONNX 模型工作时容易掉帧。
 * 这里改为三个圆点共用一个进度值，绘制更轻，节奏也更稳定。
 */
@Composable
fun MorphingBlobLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing)
        ),
        label = "loaderProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val baseRadius = this.size.minDimension * 0.10f
        val spacing = this.size.minDimension * 0.22f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        repeat(3) { index ->
            val phase = ((progress + index * 0.18f) % 1f).toDouble()
            val wave = ((sin(phase * 2.0 * PI - PI / 2.0) + 1.0) / 2.0).toFloat()
            val easedWave = FastOutSlowInEasing.transform(wave)
            val radius = baseRadius * (0.82f + easedWave * 0.34f)
            val alpha = 0.46f + easedWave * 0.54f
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(
                    x = center.x + (index - 1) * spacing,
                    y = center.y
                )
            )
        }
    }
}
