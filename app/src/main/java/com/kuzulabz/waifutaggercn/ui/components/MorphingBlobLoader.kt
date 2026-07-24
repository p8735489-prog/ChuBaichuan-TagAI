package com.kuzulabz.waifutaggercn.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
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
 * 轻量加载动画 — 物理弹簧风格三圆点波浪变形。
 *
 * 使用 keyframes 模拟弹簧物理曲线，每个圆点独立缩放和透明度变化，
 * 呈现类似 Pixel 系统的弹性加载动画效果。
 * 相比旧版 tween 线性动画，视觉更流畅自然且不会在低端设备掉帧。
 */
@Composable
fun MorphingBlobLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob")

    // 使用 keyframes 模拟弹簧物理曲线：缓入 → 过冲 → 回弹 → 稳定
    val springLikeEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0f at 0 using springLikeEasing
                0.45f at 350 using springLikeEasing
                0.72f at 580 using springLikeEasing
                0.88f at 780 using springLikeEasing
                1f at 1000 using springLikeEasing
            }
        ),
        label = "loaderProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val baseRadius = this.size.minDimension * 0.10f
        val spacing = this.size.minDimension * 0.22f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        repeat(3) { index ->
            // 每个圆点使用不同的相位偏移，制造波浪追逐效果
            val phase = ((progress + index * 0.22f) % 1f).toDouble()

            // 使用正弦波驱动弹跳，相位偏移使三个点形成波浪
            val wave = ((sin(phase * 2.0 * PI - PI / 2.0) + 1.0) / 2.0).toFloat()

            // 应用弹簧缓动，产生过冲和回弹的物理感
            val easedWave = springLikeEasing.transform(wave)

            // 缩放范围：从 0.75 到 1.35，产生更明显的弹跳效果
            val radius = baseRadius * (0.75f + easedWave * 0.60f)

            // 透明度范围：从 0.35 到 1.0，物理感更强
            val alpha = 0.35f + easedWave * 0.65f

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